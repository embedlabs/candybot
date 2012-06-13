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

import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONObject;

import net.gree.asdk.core.RR;
import net.gree.asdk.core.ui.CommandInterface;
import net.gree.asdk.core.ui.CommandInterfaceWebView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.util.AttributeSet;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DashboardContentView extends CommandInterfaceView {

  @SuppressWarnings("unused")
  private static final String TAG = "DashboardContentView";

  private static final String PULL_TO_REFRESH_LAST_UPDATE_ = "gree_pull_to_refresh_last_update";
  
  private static final SimpleDateFormat DATE_FORMAT_ = new SimpleDateFormat("yyyy/MM/dd HH:mm");
  private static final JSONObject EMPTY_JSON_OBJECT = new JSONObject();

  private LinearLayout mLinearLayout = null;
  private PullToRefreshWebView mPullToRefreshWebView = null;
  private SubNaviView mSubNaviView;
  private Handler mUiThreadHandler;

  public DashboardContentView(Context context, String baseUrl) {
    super(context, baseUrl);
    mSubNaviView = new SubNaviView(context);
    mUiThreadHandler = new Handler(context.getMainLooper());
  }

  @Override
  public CommandInterfaceWebView getWebView() {
    return mPullToRefreshWebView.getRefreshableView();
  }

  @Override
  public void setPullToRefreshEnabled(boolean enabled) {
    mPullToRefreshWebView.setPullToRefreshEnabled(enabled);
  }

  @Override
  protected void initializeImpl(Context context) {

    setBackgroundColor(Color.parseColor("#e7e7e7"));

    mLinearLayout = new LinearLayout(context);
    mLinearLayout.setOrientation(LinearLayout.VERTICAL);
    addView(mLinearLayout, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

    mPullToRefreshWebView = new PullToRefreshWebView(context);
    mPullToRefreshWebView.setDisableScrollingWhileRefreshing(false);
    mCommandInterface.setWebView(mPullToRefreshWebView.getRefreshableView());
  }

  public void setOnRefreshListener(OnRefreshListener listener) {
    mPullToRefreshWebView.setOnRefreshListener(listener);
  }

  public void onRefreshComplete() {
    mPullToRefreshWebView.onRefreshComplete();
  }

  public void updateLastUpdateTextView() {
    TextView textView = (TextView)(mPullToRefreshWebView.findViewById(RR.id(PULL_TO_REFRESH_LAST_UPDATE_)));
    synchronized(DATE_FORMAT_) {
      textView.setText(
          getContext().getResources().getString(RR.string(PULL_TO_REFRESH_LAST_UPDATE_)) +
          DATE_FORMAT_.format(new Date())
          );
    }
  }

  public void executeCallback(final String statement, final JSONObject object) {
    mUiThreadHandler.post(new Runnable() {
      public void run() {
        CommandInterface commandInterface = DashboardContentView.this.getCommandInterface();
        commandInterface.executeCallback(statement, object);
      }
    });
  }

  public void initialize() {
    super.initialize(true);
    mSubNaviView.setUp();
    mSubNaviView.addObserver(new SubNaviView.SubNaviObserver() {
      @Override
      public void notify(String name) {
        executeCallback("subNavigationPressed_" + name, EMPTY_JSON_OBJECT);
      }
    });
    mSubNaviView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
    mLinearLayout.addView(mSubNaviView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

    mLinearLayout.addView(mPullToRefreshWebView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
  }

  public boolean canGoBackSubNaviHistory() {
    return mSubNaviView.canGoBackSubNaviHistory();
  }

  public void goBackSubNaviHistory() {
    mSubNaviView.goBackSubNaviHistory();
  }

  public void updateSubNavi(JSONObject params, boolean isOpenFromMenu) {
    mSubNaviView.update(params, isOpenFromMenu);
  }

  public void subNaviDataSetChange() {
    mSubNaviView.dataSetChange();
  }

  public void clearSubNavi() {
    mSubNaviView.clearSubNavi();
  }

  private class PullToRefreshWebView extends PullToRefreshBase<CommandInterfaceWebView> {

    public PullToRefreshWebView(Context context) {
      super(context);
    }

    @Override
    protected CommandInterfaceWebView createRefreshableView(Context context, AttributeSet attrs) {
      CommandInterfaceWebView webView = new CommandInterfaceWebView(context);
      webView.setWebChromeClient(new WebChromeClient() {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
          if (newProgress == 100) {
            onRefreshComplete();
          }
        }
      });

      return webView;
    }

    @Override
    protected boolean isReadyForPullDown() {
      return getRefreshableView().getScrollY() < 2;
    }

    @Override
    protected boolean isReadyForPullUp() {
      CommandInterfaceWebView webView = getRefreshableView();
      return webView.getScrollY() >= (webView.getContentHeight() - webView.getHeight() -1);
    }
    
  }

}