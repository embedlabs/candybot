/*
 * Copyright 2012 GREE, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.gree.asdk.core.dashboard;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import net.gree.asdk.core.GLog;
import net.gree.asdk.core.RR;

public class SubNaviView extends GridView {
  private int mSelectedItem;
  private static final String TAG = "SubNaviView";

  public interface SubNaviObserver {
    void notify(String name);
  }

  public interface SubNaviOnItemChangeListener {
    void itemChanged(int prePosition, int position);
  }

  private ArrayList<SubNaviObserver> mObservers;
  private ArrayList<SubNaviOnItemChangeListener> mItemChangeListeners;
  private SubNaviAdapter mAdapter;
  private JSONObject mInfo;
  private String SUBNAVI_DIR;
  private static String NOSD_SUBNAVI_DIR;
  private static final int LABEL_FONTSIZE_THRESHOLD = 12;
  private static final float LABEL_FONTSIZE_NORMAL = 12f;
  private static final float LABEL_FONTSIZE_SMALL = 10f;
  private boolean mPositionManagedByNative = false;

  public SubNaviView(Context context) {
    super(context);
    SUBNAVI_DIR = Environment.getExternalStorageDirectory().toString() + "/Android/data/"+context.getPackageName().toString()+"/files/gree/subNavi";
  }

  public SubNaviView(Context context, AttributeSet attrs) {
    super(context, attrs);
    SUBNAVI_DIR = Environment.getExternalStorageDirectory().toString() + "/Android/data/"+context.getPackageName().toString()+"/files/gree/subNavi";
  }

  public void addObserver(SubNaviObserver observer) {
    mObservers.add(observer);
  }

  public void removeObserver(SubNaviObserver observer) {
    mObservers.remove(observer);
  }

  public void addOnItemChangeListener(SubNaviOnItemChangeListener listener) {
    mItemChangeListeners.add(listener);
  }

  public void removeOnItemChangeListener(SubNaviOnItemChangeListener listener) {
    mItemChangeListeners.remove(listener);
  }

  // Initializes subnavi
  public void setUp() {
    NOSD_SUBNAVI_DIR = getContext().getFilesDir().getAbsolutePath();
    mAdapter = new SubNaviAdapter();
    setVisibility(View.GONE);
    setNumColumns(0);
    setAdapter(mAdapter);
    setOnItemClickListener(new OnItemClickListener() {
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        BindData data = (BindData) mAdapter.getItem(position);
        for (SubNaviObserver observer : mObservers) {
          observer.notify(data.mName);
        }

        for (SubNaviOnItemChangeListener listener : mItemChangeListeners) {
          listener.itemChanged(mSelectedItem, position);
        }

        // mSelectedItem is initialized in update().
        if (mSelectedItem != position) {
          mPositionManagedByNative = true;
        }
        mSelectedItem = position;
        dataSetChange();
      }
    });
    mObservers = new ArrayList<SubNaviView.SubNaviObserver>();
    mItemChangeListeners = new ArrayList<SubNaviView.SubNaviOnItemChangeListener>();
  }

  public void clearSubNavi() {
    mInfo = null;
    mPositionManagedByNative = false;
    dataSetChange();
  }

  // Called every page change
  public void update(JSONObject params, boolean isOpenFromMenu) {
    try {
      mInfo = params.getJSONObject("subNavigation");
      JSONArray array = mInfo.getJSONArray("subNavigation");
      if (!isOpenFromMenu && array.length() > 0) {
        setVisibility(View.VISIBLE);
      } else {
        setVisibility(View.GONE);
      }

      if (mPositionManagedByNative) {
        return;
      }

      for (int i = 0; i < array.length(); i++) {
        JSONObject item = array.getJSONObject(i);
        if (item.getBoolean("selected")) {
          mSelectedItem = i;
        }
      }
    } catch (JSONException e) {
      setVisibility(View.GONE);
    }
  }

  // Called when subnavi item is tapped
  public void dataSetChange() {
    mAdapter.clear();
    if (mInfo == null) {
      mAdapter.notifyDataSetChanged();
      return;
    }

    try {
      JSONArray array = mInfo.getJSONArray("subNavigation");
      int length = array.length();
      setVisibility(length > 0 ? View.VISIBLE : View.GONE);
      for (int i = 0; i < array.length(); i++) {
        JSONObject item = array.getJSONObject(i);
        if (item.getString("label").getBytes().length >= LABEL_FONTSIZE_THRESHOLD) {
          mAdapter.setSmallFontSize();
        }
        mAdapter.add(
            item.getString("id"),
            item.getString("label"),
            item.getString("iconNormal"),
            item.getString("iconHighlighted"),
            item.getBoolean("selected"),
            i == (array.length() - 1));
      }
    } catch (JSONException e) {
      setVisibility(View.GONE);
      mAdapter.notifyDataSetChanged();
      return;
    }
    setNumColumns(mAdapter.getCount());
    mAdapter.notifyDataSetChanged();
  }

  public void selectCurrentItem() {
    selectItem(mSelectedItem);
  }

  public void selectItem(int position) {
    BindData data = (BindData) mAdapter.getItem(position);
    String name = data.mName;
    for (SubNaviObserver observer : mObservers) {
      observer.notify(name);
    }
    mSelectedItem = position;
    dataSetChange();
  }

  private class BindData {
    String mName;
    String mLabel;
    String mIconNormalId;
    String mIconHighlightedId;
    boolean mIsLastItem;
    @SuppressWarnings("unused")
    boolean mSelected;

    BindData(String name, String label, String iconNormalId, String iconHighlightedId, boolean selected , boolean isLastItem) { 
      mName = name;
      mLabel = label;
      mIconNormalId = iconNormalId;
      mIconHighlightedId = iconHighlightedId;
      mSelected = selected;
      mIsLastItem = isLastItem;
      
    }
  }

  public class SubNaviAdapter extends BaseAdapter {
    private ArrayList<BindData> mArray;
    private LayoutInflater mInflater;
    private SubNaviIconController mIconController;
    private float mFontSize;

    public SubNaviAdapter() {
      mArray = new ArrayList<BindData>();
      mInflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      mIconController = new SubNaviIconController(getContext());
      mFontSize = LABEL_FONTSIZE_NORMAL;
    }

    public void add(String name, String label, String iconNormalId, String iconHighlightedId, boolean selected, boolean isLastItem) {
        mArray.add(new BindData(name, label, iconNormalId, iconHighlightedId, selected, isLastItem));
    }

    public void clear() {
      mArray.clear();
      mFontSize = LABEL_FONTSIZE_NORMAL;
    }

    public void setSmallFontSize() {
      mFontSize = LABEL_FONTSIZE_SMALL;
    }

    public int getCount() {
      return mArray.size();
    }

    public Object getItem(int position) {
      return mArray.get(position);
    }

    public long getItemId(int position) {
      return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
      if (convertView != null) {
        ImageView convertImageView = (ImageView)convertView.findViewById(RR.id("gree_sub_item_image"));
        convertImageView.setImageDrawable(null);
      }
      View view = mInflater.inflate(RR.layout("gree_subnavi_item"), null);

      if (mSelectedItem == position) {
        view.setBackgroundResource(RR.drawable("gree_subnavi_background_highlighted"));
      } else {
        view.setBackgroundResource(RR.drawable("gree_subnavi_background_default"));
      }
      TextView text = (TextView)view.findViewById(RR.id("gree_sub_item_text"));
      BindData data = (BindData)getItem(position);
      ImageView image = (ImageView) view.findViewById(RR.id("gree_sub_item_image"));
      if (data.mIconNormalId.startsWith("http://")) {
          Bitmap bitmap = mSelectedItem == position ? mIconController.getIconBitmap(data.mIconHighlightedId) : mIconController.getIconBitmap(data.mIconNormalId);
          if (bitmap == null) {
            image.setImageResource(mSelectedItem == position ? RR.drawable("gree_btn_subnavi_home_highlight") : RR.drawable("gree_btn_subnavi_home_default"));
          } else {
            image.setImageBitmap(bitmap);
          }
      } else {
        image.setImageResource(mSelectedItem == position ? mIconController.getSelectedIconResource(data.mIconHighlightedId) : mIconController.getDefaultIconResource(data.mIconNormalId));
      }

      if (data.mIsLastItem) {
        mIconController.startSubNaviDownload();
      }

      text.setText(data.mLabel);
      text.setSingleLine();
      text.setEllipsize(TextUtils.TruncateAt.END);
      text.setTextSize(mFontSize);
      text.setTextColor(mSelectedItem == position ? Color.WHITE : Color.rgb(140, 145, 148));

      return view;
    }
  }

  private class SubNaviIconController {

    JSONObject mIconObject;
    ArrayList<String> mDownloadList;
    HashMap<String, Integer> mDefaultIconMap = new HashMap<String, Integer>();
    HashMap<String, Integer> mSelectedIconMap = new HashMap<String, Integer>();

    public SubNaviIconController(Context context) {
      File file;
      if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
        file = new File(NOSD_SUBNAVI_DIR, "subNavi_icon.json");
      } else {
        file = new File(SUBNAVI_DIR, "subNavi_icon.json");
      }
      if (file.exists()) {
        try {
          FileInputStream input;
          input = new FileInputStream(file);
          byte[] buffer = new byte[input.available()];
          input.read(buffer);
          mIconObject = (JSONObject) new JSONTokener(new String(buffer)).nextValue();
          input.close();
        } catch (FileNotFoundException e) {
          GLog.printStackTrace(TAG, e);
          mIconObject = new JSONObject();
        } catch (IOException e) {
          GLog.printStackTrace(TAG, e);
          mIconObject = new JSONObject();
        } catch (JSONException e) {
          GLog.printStackTrace(TAG, e);
          mIconObject = new JSONObject();
        }
      } else {
        mIconObject = new JSONObject();
      }
      mDownloadList = new ArrayList<String>();

      mDefaultIconMap.put("profile_info.png", RR.drawable("gree_btn_subnavi_info_default"));
      mDefaultIconMap.put("profile_wall.png", RR.drawable("gree_btn_subnavi_home_default"));
      mDefaultIconMap.put("profile_messageboard.png", RR.drawable("gree_btn_subnavi_message_board_default"));
      mDefaultIconMap.put("community_join_list.png", RR.drawable("gree_btn_subnavi_users_default"));
      mDefaultIconMap.put("community_search.png", RR.drawable("gree_btn_subnavi_search_default"));
      mDefaultIconMap.put("community_top.png", RR.drawable("gree_btn_subnavi_featured_default"));
      mDefaultIconMap.put("community_updated_list.png", RR.drawable("gree_btn_subnavi_new_default"));
      mDefaultIconMap.put("community_genre.png", RR.drawable("gree_btn_subnavi_categories_default"));
      mDefaultIconMap.put("stream_profile.png", RR.drawable("gree_btn_subnavi_updates_default"));
      mDefaultIconMap.put("friend_list.png", RR.drawable("gree_btn_subnavi_friends_list_default"));
      mDefaultIconMap.put("friend_requests.png", RR.drawable("gree_btn_subnavi_requests_default"));
      mDefaultIconMap.put("friend_footprint.png", RR.drawable("gree_btn_subnavi_footprints_default"));
      mDefaultIconMap.put("findfriends_top.png", RR.drawable("gree_btn_subnavi_find_friends_default"));
      

      mSelectedIconMap.put("profile_info.png", RR.drawable("gree_btn_subnavi_info_highlight"));
      mSelectedIconMap.put("profile_wall.png", RR.drawable("gree_btn_subnavi_home_highlight"));
      mSelectedIconMap.put("profile_messageboard.png", RR.drawable("gree_btn_subnavi_message_board_highlight"));
      mSelectedIconMap.put("community_join_list.png", RR.drawable("gree_btn_subnavi_users_highlight"));
      mSelectedIconMap.put("community_search.png", RR.drawable("gree_btn_subnavi_search_highlight"));
      mSelectedIconMap.put("community_top.png", RR.drawable("gree_btn_subnavi_featured_highlight"));
      mSelectedIconMap.put("community_updated_list.png", RR.drawable("gree_btn_subnavi_new_highlight"));
      mSelectedIconMap.put("community_genre.png", RR.drawable("gree_btn_subnavi_categories_highlight"));
      mSelectedIconMap.put("stream_profile.png", RR.drawable("gree_btn_subnavi_updates_highlight"));
      mSelectedIconMap.put("friend_list.png", RR.drawable("gree_btn_subnavi_friends_list_highlight"));
      mSelectedIconMap.put("friend_requests.png", RR.drawable("gree_btn_subnavi_requests_highlight"));
      mSelectedIconMap.put("friend_footprint.png", RR.drawable("gree_btn_subnavi_footprints_highlight"));
      mSelectedIconMap.put("findfriends_top.png", RR.drawable("gree_btn_subnavi_find_friends_highlight"));
    }

    private int getDefaultIconResource(String key) {
      return (mDefaultIconMap.get(key) != null) ? mDefaultIconMap.get(key) : RR.drawable("gree_btn_subnavi_home_default");
    }

    private int getSelectedIconResource(String key) {
      return (mSelectedIconMap.get(key) != null) ? mSelectedIconMap.get(key) : RR.drawable("gree_btn_subnavi_home_default");
    }

    private Bitmap getIconBitmap(String key) {

      // change image usage priority temporary.
      Matcher m = Pattern.compile("\\w+\\.png").matcher(key.replace("@2x.", "."));
      if(m.find()){
        try {
          return BitmapFactory.decodeResource(getContext().getResources(), RR.drawable("gree_" + m.group().replace(".png", "")));
        } catch (Exception e) {
          GLog.printStackTrace(TAG, e);
        }
      }

      try {
        if (mIconObject.has(key)) {
          String icon = mIconObject.getString(key);
          File file;
          if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            file = new File(NOSD_SUBNAVI_DIR, icon);
          } else {
            file = new File(SUBNAVI_DIR, icon);
          }
          return BitmapFactory.decodeFile(file.getPath());
        } else {

          if (key.startsWith("http")) {
            mDownloadList.add(key);
          }

        }
      } catch (JSONException e) {
        GLog.printStackTrace(TAG, e);
      }

      return null;
    }

    public void startSubNaviDownload() {
      if (!mDownloadList.isEmpty()) {
        new subNaviDownloadTask().execute();
      }
    }

    private class subNaviDownloadTask extends AsyncTask<Void, Void, Void> {
      private static final int BUFFER_SIZE = 4096;

      @Override
      protected void onPreExecute() {
        File dir;
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
          dir = new File(NOSD_SUBNAVI_DIR);
        } else {
          dir = new File(SUBNAVI_DIR);
        }
        if (!dir.exists()) {
          dir.mkdirs();
        }
        super.onPreExecute();
      }

      @Override
      protected Void doInBackground(Void... params) {
        for (String uri_string : mDownloadList) {
          try {
            URI uri = new URI(uri_string);

            String[] filepath = uri.getPath().split("/");
            String filename = filepath[filepath.length - 1];
            File file;
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
              file = new File(NOSD_SUBNAVI_DIR, filename);
            } else {
              file = new File(SUBNAVI_DIR, filename);
            }
            HttpClient client = new DefaultHttpClient();
            HttpGet get = new HttpGet();
            get.setURI(uri);
            HttpResponse response = client.execute(get);

            int code = response.getStatusLine().getStatusCode();
            if (code == 200) {
              file.createNewFile();
              InputStream inputStream = response.getEntity().getContent();
              BufferedInputStream in = new BufferedInputStream(inputStream, BUFFER_SIZE);
              BufferedOutputStream out =
                  new BufferedOutputStream(new FileOutputStream(file, false), BUFFER_SIZE);

              byte buf[] = new byte[4096];
              int size = -1;
              while ((size = in.read(buf)) != -1) {
                out.write(buf, 0, size);
              }
              out.flush();
              out.close();
              in.close();
              mIconObject.put(uri_string, filename);
            }
          } catch (URISyntaxException e) {
            GLog.printStackTrace(TAG, e);
          } catch (ClientProtocolException e) {
            GLog.printStackTrace(TAG, e);
          } catch (IOException e) {
            GLog.printStackTrace(TAG, e);
          } catch (JSONException e) {
            GLog.printStackTrace(TAG, e);
          }
        }
        mDownloadList.clear();
        
        return null;
      }

      @Override
      protected void onPostExecute(Void result) {
        File file;
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
          file = new File(NOSD_SUBNAVI_DIR, "subNavi_icon.json");
        } else {
          file = new File(SUBNAVI_DIR, "subNavi_icon.json");
        }
        FileOutputStream stream;
        try {
          stream = new FileOutputStream(file);
          stream.write(mIconObject.toString().getBytes());
          stream.close();
        } catch (FileNotFoundException e) {
          GLog.printStackTrace(TAG, e);
        } catch (NumberFormatException e) {
          GLog.printStackTrace(TAG, e);
        } catch (IOException e) {
          GLog.printStackTrace(TAG, e);
        }
        
        if(mAdapter != null){
          mAdapter.notifyDataSetChanged();
        }
        
        super.onPostExecute(result);
      }
    }
  }
}
