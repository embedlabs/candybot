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

package net.gree.asdk.api;

import java.security.InvalidParameterException;
import java.util.Map;
import java.util.TreeMap;

import net.gree.asdk.core.Core;
import net.gree.asdk.core.GLog;
import net.gree.asdk.core.InternalSettings;
import net.gree.asdk.core.Url;
import net.gree.asdk.core.request.BaseClient;
import net.gree.asdk.core.request.GeneralClient;
import net.gree.asdk.core.request.JsonClient;
import net.gree.asdk.core.request.OnResponseCallback;
import net.gree.asdk.core.request.SnsApiClient;
import net.gree.asdk.core.storage.Tracker;

import org.apache.http.HttpEntity;
import org.json.JSONObject;

/**
 * Class that provides a request with an OAuth signature. By verifying the OAuth signature in a
 * server which has received a request, it is authenticated in the server.
 * 
 * @author GREE, Inc.
 * @since 2.0.0
 * @see <a href="http://tools.ietf.org/html/rfc5849">OAuth 1.0 (RFC5849)</a>
 * 
 */
public final class Request {
  static final String TAG = "api.Request";
  Map<String, Object> oparams = null;
  boolean isDebug = Core.debugging();

/**
 * Contructor with additionnal parameters to be added to the notification.
*/
  public Request() {
    this.oparams = Core.getParams();
    this.isDebug = Core.debugging();
  }

/**
 * Contructor with additionnal parameters to be added to the notification.
*/
  public Request(Map<String, Object> params) {
    this.oparams = params;
  }

 
   
  /**
   * Sends a request with an OAuth signature to an external server.
   * <p>
   * This method is asynchronous or synchronous. opensocial_app_id, opensocial_owner_id, and
   * opensocial_viewer_id are always added to a query parameter.
   * 
   * <ul>
   * <li>opensocial_app_id ID of the accessed GREE application</li>
   * <li>opensocial_viewer_id ID of the user who has currently accessed the GREE application</li>
   * <li>opensocial_owner_id ID of the user who has installed the accessed GREE application</li>
   * </ul>
   * 
   * The following is an example of sending a GET request. <code><pre>
   * new GreeRequest.makeRequest("http://example.com?data=foobar", "GET", null, null, OnResponseCallback<String> listener) {
   * 	public void onResponse(HttpResponse response) {
   * 		// user code...
   * 	}
   * });
   * </pre></code>
   * 
   * @param url URL to which a request will be sent
   * @param method HTTP method (such as GET or POST)
   * @param entity HTTP entity to be added to the request (Specify null if the entity is not
   *        required because the HTTP method is GET or DELETE, etc.)
   * @param headers Parameter to be added to the HTTP header of the request
   * @param listener Listener that processes the result
   */
   
  public void oauth(String url, String method, HttpEntity entity, Map<String, String> headers,
      boolean sync, OnResponseCallback<String> listener) {
    // Make default simple and distinct from Gree API requests
    if (headers == null) {
      headers = new TreeMap<String, String>();
      headers.put("User-Agent", "Gree-enabled app");
      headers.put("Cookie", null);
    }
    new GeneralClient().oauth(appendOpensocialParameters(url), method, headers, entity, sync,
        listener);
  }

 
   
    /**
   *        Sends a request to access GREE Platform API provided by GREE. This method is
   *        asynchronous.
   * @param action Name of the API to be called
   * @param method HTTP method (such as GET or POST)
   * @param queryParameters Parameter to be passed to the API
   * @param listener Listener that processes the result
   */
   
  public void oauthGree(String action, String method, Map<String, Object> queryParameters,
      Map<String, String> headers, boolean sync, OnResponseCallback<String> listener) {
    int imethod;
    try {
      imethod = BaseClient.cmd.get(method);
    } catch (Exception e) {
      throw new IllegalArgumentException();
    }
    String url = Url.getApiEndpointWithAction(action);
    switch (imethod) {
      case BaseClient.METHOD_GET:
      case BaseClient.METHOD_DELETE:
        if ((queryParameters != null) && (!queryParameters.isEmpty())) {
          url += "/?" + BaseClient.toQueryString(queryParameters);
        }
        if (isDebug) {
          GLog.d(TAG, "Query string:" + url);
        }
        new JsonClient().oauth(url, method, headers, sync, listener);
        break;
      case BaseClient.METHOD_POST:
      case BaseClient.METHOD_PUT:
        String json = BaseClient.toEntityJson(queryParameters);
        if (isDebug) {
          GLog.d(TAG, "Json body:" + json);
        }
        new JsonClient().oauth(url, method, headers, json, sync, listener);
        break;
      default:
        throw new InvalidParameterException("Invalid method " + imethod);
    }
  }

 
 
  /**
 * Sends a request to access GREE Platform API provided by GREE. 
 * @param action Name of the API to be called
 * @param method HTTP method (such as GET or POST)
 * @param queryParameters Parameter to be passed to the API
 * @param headers additional request headers
 * @param sync whether this request should be synchronous
 * @param isSecure whether this request should use https
 * @param listener Listener that processes the result
 */
 
  public void oauthGree(String action, String method, Map<String, Object> queryParameters,
      Map<String, String> headers, boolean sync, boolean isSecure, OnResponseCallback<String> listener) {
    int imethod;
    try {
      imethod = BaseClient.cmd.get(method);
    } catch (Exception e) {
      throw new IllegalArgumentException();
    }
    String url = null;
    if (isSecure) {
      url = Url.getSecureApiEndpointWithAction(action);
    } else {
      url = Url.getApiEndpointWithAction(action);
    }
    switch (imethod) {
      case BaseClient.METHOD_GET:
      case BaseClient.METHOD_DELETE:
        if ((queryParameters != null) && (!queryParameters.isEmpty())) {
          url += "/?" + BaseClient.toQueryString(queryParameters);
        }
        if (isDebug) {
          GLog.d(TAG, "Query string:" + url);
        }
        new JsonClient().oauth(url, method, headers, sync, listener);
        break;
      case BaseClient.METHOD_POST:
      case BaseClient.METHOD_PUT:
        String json = BaseClient.toEntityJson(queryParameters);
        if (isDebug) {
          GLog.d(TAG, "Json body:" + json);
        }
        new JsonClient().oauth(url, method, headers, json, sync, listener);
        break;
      default:
        throw new InvalidParameterException("Invalid method " + imethod);
    }
  }

 
 
  /**
 * Sends a request to access GREE Platform API provided by GREE. 
 * @param params request pa
 * @param headers additional request headers
 * @param sync whether this request should be synchronous
 * @param listener Listener that processes the result
 * @deprecated this method needs to be moved to core
 */
 
  public void oauthSnsApi(JSONObject params, Map<String, String> headers, boolean sync,
      OnResponseCallback<String> listener) {

    Object obj = params.opt("request");
    oauthSnsApi(obj != null ? obj.toString() : params.toString(), headers, sync, listener);
  }

 
 
  /**
 * Sends a request to access GREE Platform API provided by GREE. 
 * @param params request pa
 * @param headers additional request headers
 * @param sync whether this request should be synchronous
 * @param listener Listener that processes the result
 * @deprecated this method needs to be moved to core
 */
 
  public void oauthSnsApi(String params, Map<String, String> headers, boolean sync, OnResponseCallback<String> listener) {
    new SnsApiClient().oauth(Url.getSnsApiUrl(), BaseClient.METHOD_POST, headers, params, sync, listener);
  }

  private String appendOpensocialParameters(String url) {
    StringBuilder sb = new StringBuilder(url);
    sb.append((url.contains("?")) ? "&" : "?");
    sb.append("opensocial_app_id=" + oparams.get(InternalSettings.ApplicationId));
    sb.append("&opensocial_viewer_id=" + oparams.get("userId")); // current_user
    sb.append("&opensocial_owner_id=" + oparams.get("userId")); // current_user
    return sb.toString();
  }

  /**
   * Allow application to disable/enable networking. Used during development / test to simulate loss
   * of network.
   * 
   * @param disabled true if the network should simulate a down network.
   */
  public static void setNetworkDisabled(boolean disabled) {
    BaseClient.setNetworkDisabled(disabled);
    if (!disabled) {
      Tracker.checkNetwork(null);
    }
  }

  /**
   * Current fake network status. (for testing)
   * @return true if the network is disabled, false otherwise
   */
  public static boolean isNetworkDisabled() {
    return BaseClient.isNetworkDisabled();
  }

}
