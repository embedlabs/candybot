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
import java.util.Stack;
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

import net.gree.asdk.core.RR;

public class SubNaviView extends GridView {
  private int mSelectedItem;

  public interface SubNaviObserver {
    void notify(String name);
  }

  private ArrayList<SubNaviObserver> observers;
  private SubNaviAdapter adapter_;
  private JSONObject info_;
  private String SUBNAVI_DIR;
  private static String NOSD_SUBNAVI_DIR;
  private Stack<Integer> mSubNaviHistory = new Stack<Integer>(); // keeps position in subnavi

  public SubNaviView(Context context) {
    super(context);
    SUBNAVI_DIR = Environment.getExternalStorageDirectory().toString() + "/Android/data/"+context.getPackageName().toString()+"/files/gree/subNavi";
  }

  public SubNaviView(Context context, AttributeSet attrs) {
    super(context, attrs);
    SUBNAVI_DIR = Environment.getExternalStorageDirectory().toString() + "/Android/data/"+context.getPackageName().toString()+"/files/gree/subNavi";
  }

  public void addObserver(SubNaviObserver observer) {
    observers.add(observer);
  }

  public void removeObserver(SubNaviObserver observer) {
    observers.remove(observer);
  }

  // Initializes subnavi
  public void setUp() {
    NOSD_SUBNAVI_DIR = getContext().getFilesDir().getAbsolutePath();
    adapter_ = new SubNaviAdapter();
    setVisibility(View.GONE);
    setNumColumns(0);
    setAdapter(adapter_);
    setOnItemClickListener(new OnItemClickListener() {
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        BindData data = (BindData) adapter_.getItem(position);
        for (SubNaviObserver observer : observers) {
          observer.notify(data.name_);
        }
        mSubNaviHistory.push(mSelectedItem);
        mSelectedItem = position;
        dataSetChange();
      }
    });
    observers = new ArrayList<SubNaviView.SubNaviObserver>();
  }

  public void clearSubNavi() {
    info_ = null;
    mSubNaviHistory.clear();
    dataSetChange();
  }

  // Called every page change
  public void update(JSONObject params, boolean isOpenFromMenu) {
    try {
      info_ = params.getJSONObject("subNavigation");
      JSONArray array = info_.getJSONArray("subNavigation");
      if (!isOpenFromMenu && array.length() > 0) {
        setVisibility(View.VISIBLE);
      } else {
        setVisibility(View.GONE);
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
    adapter_.clear();
    if (info_ == null) {
      adapter_.notifyDataSetChanged();
      return;
    }

    try {
      JSONArray array = info_.getJSONArray("subNavigation");
      int length = array.length();
      setVisibility(length > 0 ? View.VISIBLE : View.GONE);
      for (int i = 0; i < array.length(); i++) {
        JSONObject item = array.getJSONObject(i);
        adapter_.add(
            item.getString("id"),
            item.getString("label"),
            item.getString("iconNormal"),
            item.getString("iconHighlighted"),
            item.getBoolean("selected"),
            i == (array.length() - 1));
      }
    } catch (JSONException e) {
      setVisibility(View.GONE);
      adapter_.notifyDataSetChanged();
      return;
    }
    setNumColumns(adapter_.getCount());
    adapter_.notifyDataSetChanged();
  }

  public boolean canGoBackSubNaviHistory() {
    return !mSubNaviHistory.empty();
  }

  public void goBackSubNaviHistory() {
    int position = mSubNaviHistory.pop();
    BindData data = (BindData) adapter_.getItem(position);
    String name = data.name_;
    for (SubNaviObserver observer : observers) {
      observer.notify(name);
    }
    mSelectedItem = position;
    dataSetChange();
  }

  private class BindData {
    String name_;
    String label_;
    String iconNormalId_;
    String iconHighlightedId_;
    boolean isLastItem_;
    @SuppressWarnings("unused")
    boolean selected_;

    BindData(String name, String label, String iconNormalId, String iconHighlightedId, boolean selected , boolean isLastItem) { 
      name_ = name;
      label_ = label;
      iconNormalId_ = iconNormalId;
      iconHighlightedId_ = iconHighlightedId;
      selected_ = selected;
      isLastItem_ = isLastItem;
      
    }
  }

  public class SubNaviAdapter extends BaseAdapter {
    private ArrayList<BindData> array;
    private LayoutInflater inflater_;
    private SubNaviIconController iconController_;

    public SubNaviAdapter() {
      array = new ArrayList<BindData>();
      inflater_ = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      iconController_ = new SubNaviIconController(getContext());
    }

    public void add(String name, String label, String iconNormalId, String iconHighlightedId, boolean selected, boolean isLastItem) {
        array.add(new BindData(name, label, iconNormalId, iconHighlightedId, selected, isLastItem));
    }

    public void clear() {
      array.clear();
    }

    public int getCount() {
      return array.size();
    }

    public Object getItem(int position) {
      return array.get(position);
    }

    public long getItemId(int position) {
      return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
      View view = inflater_.inflate(RR.layout("gree_subnavi_item"), null);

      if (mSelectedItem == position) {
        view.setBackgroundResource(RR.drawable("gree_subnavi_background_highlighted"));
      } else {
        view.setBackgroundResource(RR.drawable("gree_subnavi_background_default"));
      }
      TextView text = (TextView)view.findViewById(RR.id("gree_sub_item_text"));
      BindData data = (BindData)getItem(position);
      ImageView image = (ImageView) view.findViewById(RR.id("gree_sub_item_image"));
      if (data.iconNormalId_.startsWith("http://")) {
          Bitmap bitmap = mSelectedItem == position ? iconController_.getIconBitmap(data.iconHighlightedId_) : iconController_.getIconBitmap(data.iconNormalId_);
          if (bitmap == null) {
            image.setImageResource(mSelectedItem == position ? RR.drawable("gree_btn_subnavi_home_selected") : RR.drawable("gree_btn_subnavi_home_default"));
          } else {
            image.setImageBitmap(bitmap);
          }
      } else {
        image.setImageResource(mSelectedItem == position ? iconController_.getSelectedIconResource(data.iconHighlightedId_) : iconController_.getDefaultIconResource(data.iconNormalId_));
      }

      if (data.isLastItem_) {
        iconController_.startSubNaviDownload();
      }

      text.setText(data.label_);
      text.setSingleLine();
      text.setEllipsize(TextUtils.TruncateAt.END);
      text.setTextSize(12f);
      text.setTextColor(mSelectedItem == position ? Color.WHITE : Color.rgb(140, 145, 148));

      return view;
    }
  }

  private class SubNaviIconController {

    JSONObject iconObject;
    ArrayList<String> downloadList_;
    HashMap<String, Integer> defaultIconMap = new HashMap<String, Integer>();
    HashMap<String, Integer> selectedIconMap = new HashMap<String, Integer>();

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
          iconObject = (JSONObject) new JSONTokener(new String(buffer)).nextValue();
          input.close();
        } catch (FileNotFoundException e) {
          e.printStackTrace();
          iconObject = new JSONObject();
        } catch (IOException e) {
          e.printStackTrace();
          iconObject = new JSONObject();
        } catch (JSONException e) {
          e.printStackTrace();
          iconObject = new JSONObject();
        }
      } else {
        iconObject = new JSONObject();
      }
      downloadList_ = new ArrayList<String>();

      defaultIconMap.put("profile_info.png", RR.drawable("gree_btn_subnavi_info_default"));
      defaultIconMap.put("profile_wall.png", RR.drawable("gree_btn_subnavi_home_default"));
      defaultIconMap.put("profile_messageboard.png", RR.drawable("gree_btn_subnavi_message_board_default"));
      defaultIconMap.put("community_join_list.png", RR.drawable("gree_btn_subnavi_users_default"));
      defaultIconMap.put("community_search.png", RR.drawable("gree_btn_subnavi_search_default"));
      defaultIconMap.put("community_top.png", RR.drawable("gree_btn_subnavi_featured_default"));
      defaultIconMap.put("community_updated_list.png", RR.drawable("gree_btn_subnavi_new_default"));
      defaultIconMap.put("community_genre.png", RR.drawable("gree_btn_subnavi_categories_default"));
      defaultIconMap.put("stream_profile.png", RR.drawable("gree_btn_subnavi_updates_default"));
      defaultIconMap.put("friend_list.png", RR.drawable("gree_btn_subnavi_friends_list_default"));
      defaultIconMap.put("friend_requests.png", RR.drawable("gree_btn_subnavi_requests_default"));
      defaultIconMap.put("friend_footprint.png", RR.drawable("gree_btn_subnavi_footprints_default"));
      defaultIconMap.put("findfriends_top.png", RR.drawable("gree_btn_subnavi_find_friends_default"));
      

      selectedIconMap.put("profile_info.png", RR.drawable("gree_btn_subnavi_info_selected"));
      selectedIconMap.put("profile_wall.png", RR.drawable("gree_btn_subnavi_home_selected"));
      selectedIconMap.put("profile_messageboard.png", RR.drawable("gree_btn_subnavi_message_board_selected"));
      selectedIconMap.put("community_join_list.png", RR.drawable("gree_btn_subnavi_users_selected"));
      selectedIconMap.put("community_search.png", RR.drawable("gree_btn_subnavi_search_selected"));
      selectedIconMap.put("community_top.png", RR.drawable("gree_btn_subnavi_featured_selected"));
      selectedIconMap.put("community_updated_list.png", RR.drawable("gree_btn_subnavi_new_selected"));
      selectedIconMap.put("community_genre.png", RR.drawable("gree_btn_subnavi_categories_selected"));
      selectedIconMap.put("stream_profile.png", RR.drawable("gree_btn_subnavi_updates_selected"));
      selectedIconMap.put("friend_list.png", RR.drawable("gree_btn_subnavi_friends_list_selected"));
      selectedIconMap.put("friend_requests.png", RR.drawable("gree_btn_subnavi_requests_selected"));
      selectedIconMap.put("friend_footprint.png", RR.drawable("gree_btn_subnavi_footprints_selected"));
      selectedIconMap.put("findfriends_top.png", RR.drawable("gree_btn_subnavi_find_friends_selected"));
    }

    private int getDefaultIconResource(String key) {
      return (defaultIconMap.get(key) != null) ? defaultIconMap.get(key) : RR.drawable("gree_btn_subnavi_home_default");
    }

    private int getSelectedIconResource(String key) {
      return (selectedIconMap.get(key) != null) ? selectedIconMap.get(key) : RR.drawable("gree_btn_subnavi_home_default");
    }

    private Bitmap getIconBitmap(String key) {

      // change image usage priority temporary.
      Matcher m = Pattern.compile("\\w+\\.png").matcher(key.replace("@2x.", "."));
      if(m.find()){
        try {
          return BitmapFactory.decodeResource(getContext().getResources(), RR.drawable("gree_" + m.group().replace(".png", "")));
        } catch (Exception e) {
          e.printStackTrace();
        }
      }

      try {
        if (iconObject.has(key)) {
          String icon = iconObject.getString(key);
          File file;
          if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            file = new File(NOSD_SUBNAVI_DIR, icon);
          } else {
            file = new File(SUBNAVI_DIR, icon);
          }
          return BitmapFactory.decodeFile(file.getPath());
        } else {

          if (key.startsWith("http")) {
            downloadList_.add(key);
          }

        }
      } catch (JSONException e) {
        e.printStackTrace();
      }

      return null;
    }

    public void startSubNaviDownload() {
      if (!downloadList_.isEmpty()) {
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
        for (String uri_string : downloadList_) {
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
              iconObject.put(uri_string, filename);
            }
          } catch (URISyntaxException e) {
            e.printStackTrace();
          } catch (ClientProtocolException e) {
            e.printStackTrace();
          } catch (IOException e) {
            e.printStackTrace();
          } catch (JSONException e) {
            e.printStackTrace();
          }
        }
        downloadList_.clear();
        
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
          stream.write(iconObject.toString().getBytes());
          stream.close();
        } catch (FileNotFoundException e) {
          e.printStackTrace();
        } catch (NumberFormatException e) {
          e.printStackTrace();
        } catch (IOException e) {
          e.printStackTrace();
        }
        
        if(adapter_ != null){
          adapter_.notifyDataSetChanged();
        }
        
        super.onPostExecute(result);
      }
    }
  }
}
