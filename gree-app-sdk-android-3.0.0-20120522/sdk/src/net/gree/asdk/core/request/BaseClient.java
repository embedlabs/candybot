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

package net.gree.asdk.core.request;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultRedirectHandler;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import android.content.ContentResolver;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.webkit.WebSettings;
import android.webkit.WebView;

import net.gree.asdk.core.Core;
import net.gree.asdk.core.GLog;
import net.gree.asdk.core.Util;
import net.gree.asdk.core.auth.AuthorizerCore;
import net.gree.asdk.core.storage.CookieStorage;
import net.gree.oauth.signpost.exception.OAuthCommunicationException;
import net.gree.oauth.signpost.exception.OAuthExpectationFailedException;
import net.gree.oauth.signpost.exception.OAuthMessageSignerException;
import net.gree.vendor.com.google.gson.Gson;
import net.gree.vendor.com.google.gson.JsonSyntaxException;
import net.gree.vendor.com.google.gson.reflect.TypeToken;

/**
 * Base class for HTTP client implementations.
 * 
 * @author GREE, Inc.
 * 
 */
public abstract class BaseClient<T> {
  private static final String TAG = "GreeHttpClient";
  public static boolean isDebug = false;
  public static boolean isVerbose = false;
  
  { // http://stackoverflow.com/questions/4280330/onpostexecute-not-being-called-in-asynctask-handler-runtime-exception
    Looper looper = Looper.getMainLooper();
    Handler handler = new Handler(looper);
    handler.post(new Runnable() {
      public void run() {
        try {
          Class.forName("android.os.AsyncTask");
        } catch (ClassNotFoundException e) {
          e.printStackTrace();
        }
      }
    });
  }

  public static void setDebug(boolean debug) {
    isDebug = debug;
  }

  public static void setVerbose(boolean verbose) {
    isVerbose = verbose;
  }

  private static final String LOGGING_PREFIX = "Gree[URL:HttpRequest]: ";
  public static final String DisabledMessage = "Network requests disabled by application.";
  public static final String GRADE0_ERROR_MESSAGE = "User is not logged in";

  private static final int OAUTH_TYPE_3LEGGED = 3;
  private static final int OAUTH_TYPE_2LEGGED = 2;
  private static final int OAUTH_TYPE_NONE = 0;

  public static final int METHOD_GET = 0;
  public static final int METHOD_POST = 1;
  public static final int METHOD_PUT = 2;
  public static final int METHOD_DELETE = 3;
  public static final String[] methods = {"GET","POST","PUT","DELETE"};
  
  protected static final int CONNECTION_TIMEOUT = 30 * 1000; // msec

  protected OnResponseCallback<T> mListener;

  // If static user agent is set, use that by defualt, otherwise construct and wait for per-instance
// set.
  static String sUserAgent = null;
  protected String mUserAgent = "GREEApp/" + Core.getSdkVersion() + " (" + Build.MODEL
      + "; Android " + Build.VERSION.RELEASE + ")";
  static Gson gson = new Gson();

  public BaseClient() {
    if (sUserAgent != null) mUserAgent = sUserAgent;
    isDebug = Core.debugging();
  }

  static Context context = null;

  public static void init(Context context) {
    BaseClient.context = context;
    sUserAgent = BaseClient.getSystemDefaultUserAgent(context);
  }

  public static final Map<String, Integer> cmd = new TreeMap<String, Integer>() {
    private static final long serialVersionUID = 1L;
    {
      put("GET", 0);
      put("POST", 1);
      put("PUT", 2);
      put("DELETE", 3);
      put("get", 0);
      put("post", 1);
      put("put", 2);
      put("delete", 3);
    }
  };

  /**
   * Default Redirect Handler It will call OnResponseCallbackInternal.onRedirect(String).
   */
  protected DefaultRedirectHandler mRedirectHandler = new DefaultRedirectHandler() {
    public boolean isRedirectRequested(HttpResponse response, HttpContext context) {
      int statusCode = response.getStatusLine().getStatusCode();
      switch (statusCode) {
        case HttpStatus.SC_MOVED_TEMPORARILY:
        case HttpStatus.SC_MOVED_PERMANENTLY:
        case HttpStatus.SC_SEE_OTHER:
        case HttpStatus.SC_TEMPORARY_REDIRECT:
          Header locationHeader = response.getFirstHeader("location");
          String redirectLocation = locationHeader.getValue().replaceAll(" ", "%20");
          if (mListener != null && mListener instanceof OnResponseListenerInternal) {
            ((OnResponseListenerInternal<?>) mListener).onRedirect(redirectLocation);
          }
          return false;
        default:
          return false;
      }
    }
  };

  /**
   * Set callback listener object.
   * 
   * @param listener
   */
  public void setResponseListener(OnResponseCallback<T> listener) {
    mListener = listener;
  }

  public BaseClient<T> setUserAgent(String userAgent) {
    mUserAgent = userAgent;
    return this;
  }

  public static void setDefaultUserAgent(String userAgent) {
    sUserAgent = userAgent;
  }

  /**
   * This class implements AsyncExecutor.
   * 
   * @author GREE, Inc.
   * 
   */
  protected class HttpRequestExecutor extends AsyncTask<HttpUriRequest, Integer, HttpResponse> {
    @Override
    protected HttpResponse doInBackground(HttpUriRequest... params) {
      for (HttpUriRequest param : params) {
        return BaseClient.this.execute(param);
      }
      return null;
    }

    @Override
    protected void onPostExecute(HttpResponse result) {
      onResponse(result);
    }
  }

  /**
   * Executes a HTTP request to the target URL with OAuth Header. This request executes in a
   * background thread.
   * 
   * @param url A target URL
   * @param methodType BaseClient#METHOD_*
   * @param entity POST/PUT entity body object
   * @param listener Implements OnResponseCallback
   */
  public <E> void oauth(String url, String method, Map<String, String> headers, E entity,
      boolean sync, OnResponseCallback<T> listener) {
    int imethod;
    try {
      imethod = BaseClient.cmd.get(method);
    } catch (Exception e) {
      throw new IllegalArgumentException();
    }
    oauth(url, imethod, headers, entity, sync, listener);
  }

  public <E> void oauth(String url, int imethod, Map<String, String> headers, E entity,
      boolean sync, final OnResponseCallback<T> listener) {
    if (skipRequestWithoutAccessToken(sync, listener)) { return; }
    _http(url, imethod, headers, entity, sync, new OnResponseCallback<T>() {
      public void onSuccess(int responseCode, HeaderIterator headers, T response) {
        listener.onSuccess(responseCode, headers, response);
      }

      public void onFailure(int responseCode, HeaderIterator headers, String response) {
        listener.onFailure(responseCode, headers, response);
        if (response != null) {
          try {
            FailureResponse failure = gson.fromJson(response, FailureResponse.class);
            if (failure != null) failure.handleError();
          } catch(JsonSyntaxException e) {}
        }
      }
    }, OAUTH_TYPE_3LEGGED);
  }

  /*
   * Executes an HTTP request to the target URL with OAuth Header.
   * @see BaseClient#makeRequest(String, int, E, OnResponseCallback)
   * @param url
   * @param methodType
   * @param listener
   */
  public void oauth(String url, String method, Map<String, String> headers, boolean sync,
      OnResponseCallback<T> listener) {
    int imethod;
    try {
      imethod = BaseClient.cmd.get(method);
    } catch (Exception e) {
      throw new IllegalArgumentException();
    }
    oauth(url, imethod, headers, sync, listener);
  }

  public void oauth(String url, int methodType, Map<String, String> headers, boolean sync,
      final OnResponseCallback<T> listener) {
    if (skipRequestWithoutAccessToken(sync, listener)) { return; }
    _http(url, methodType, headers, null, sync, new OnResponseCallback<T>() {
      public void onSuccess(int responseCode, HeaderIterator headers, T response) {
        listener.onSuccess(responseCode, headers, response);
      }

      public void onFailure(int responseCode, HeaderIterator headers, String response) {
        listener.onFailure(responseCode, headers, response);
        if (response != null) {
          try {
            FailureResponse failure = gson.fromJson(response, FailureResponse.class);
            if (failure != null) failure.handleError();
          } catch(JsonSyntaxException e) {}
        }
      }
    }, OAUTH_TYPE_3LEGGED);
  }

  private boolean skipRequestWithoutAccessToken(boolean synchronous, OnResponseCallback<T> listener) {
    if (Util.isAvailableGrade0() && !AuthorizerCore.getInstance().hasOAuthAccessToken()) {
      if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
        // On UI thread, force to async.
        synchronous = false;
      } else {
        // Not on UI thread.
      }
      this.synchronous = synchronous;
      mListener = listener;
      GLog.d(TAG, "Request failed. Status Code: 0, reason:" + GRADE0_ERROR_MESSAGE + " headers: null");
      onFailure(0, null, GRADE0_ERROR_MESSAGE);
      return true;
    }
    return false;
  }

  /**
   * Executes a HTTP request to the target URL with 2-legged OAuth Header.
   * 
   * @see BaseClient#makeRequest(String, int, E, OnResponseCallback)
   * @param url
   * @param methodType
   * @param listener
   */
  public void oauth2(String url, String method, Map<String, String> headers, boolean sync,
      OnResponseCallback<T> listener) {
    int imethod;
    try {
      imethod = BaseClient.cmd.get(method);
    } catch (Exception e) {
      throw new IllegalArgumentException();
    }
    oauth2(url, imethod, headers, sync, listener);
  }

  public void oauth2(String url, int methodType, Map<String, String> headers, boolean sync,
      OnResponseCallback<T> listener) {
    _http(url, methodType, headers, null, sync, listener, OAUTH_TYPE_2LEGGED);
  }

  /**
   * Executes a HTTP request to the target URL with 2-legged OAuth Header.
   * 
   * @see BaseClient#makeRequest(String, int, E, OnResponseCallback)
   * @param url
   * @param methodType
   * @param entity
   * @param listener
   */
  public <E> void oauth2(String url, String method, Map<String, String> headers, E entity,
      boolean sync, OnResponseCallback<T> listener) {
    int imethod;
    try {
      imethod = BaseClient.cmd.get(method);
    } catch (Exception e) {
      throw new IllegalArgumentException();
    }
    oauth2(url, imethod, headers, entity, sync, listener);
  }

  public <E> void oauth2(String url, int methodType, Map<String, String> headers, E entity,
      boolean sync, OnResponseCallback<T> listener) {
    _http(url, methodType, headers, entity, sync, listener, OAUTH_TYPE_2LEGGED);
  }

  /**
   * Executes a HTTP request to the target URL with 2-legged OAuth Header.
   * 
   * @see BaseClient#makeRequest(String, int, E, OnResponseCallback)
   * @param url
   * @param methodType
   * @param listener
   */
  public void http(String url, String method, Map<String, String> headers, boolean sync,
      OnResponseCallback<T> listener) {
    int imethod;
    try {
      imethod = BaseClient.cmd.get(method);
    } catch (Exception e) {
      throw new IllegalArgumentException();
    }
    http(url, imethod, headers, sync, listener);
  }

  public void http(String url, int methodType, Map<String, String> headers, boolean sync,
      OnResponseCallback<T> listener) {
    _http(url, methodType, headers, null, sync, listener, OAUTH_TYPE_NONE);
  }

  /**
   * Executes a HTTP request to the target URL with 2-legged OAuth Header.
   * 
   * @see BaseClient#makeRequest(String, int, E, OnResponseCallback)
   * @param url
   * @param methodType
   * @param entity
   * @param listener
   */
  public <E> void http(String url, String method, Map<String, String> headers, E entity,
      boolean sync, OnResponseCallback<T> listener) {
    int imethod;
    try {
      imethod = BaseClient.cmd.get(method);
    } catch (Exception e) {
      throw new IllegalArgumentException();
    }
    http(url, imethod, headers, entity, sync, listener);
  }

  public <E> void http(String url, int methodType, Map<String, String> headers, E entity,
      boolean sync, OnResponseCallback<T> listener) {
    _http(url, methodType, headers, entity, sync, listener, OAUTH_TYPE_NONE);
  }

  /**
   * Executes a HTTP request in the other thread.
   * 
   * @param url
   * @param methodType
   * @param entity
   * @param listener
   * @param oAuthType
   */
  protected <E> void _http(final String url, final String method, Map<String, String> headers,
      final E entity, boolean sync, final OnResponseCallback<T> listener, int oAuthType) {
    int imethod;
    try {
      imethod = BaseClient.cmd.get(method);
    } catch (Exception e) {
      throw new IllegalArgumentException();
    }
    _http(url, imethod, headers, entity, sync, listener, oAuthType);
  }

  static boolean forceError = false;

  /**
   * During development, this allows all Request/Client networking to be disabled in a clean way.
   * 
   * @param force
   */
  public static void setNetworkDisabled(boolean disabled) {
    forceError = disabled;
  }

  public static boolean isNetworkDisabled() {
    return forceError;
  }

  boolean synchronous = false;

  protected <E> void _http(final String url, final int methodType, Map<String, String> headers,
      final E entity, boolean synchronous, final OnResponseCallback<T> listener, int oAuthType) {
    setRequestHeaders(headers);
    if (listener != null) mListener = listener;
    HttpUriRequest request = null;
    try {
      request = generateRequest(url, methodType, entity);
      sign(request, oAuthType);
      // Automatic sync/async selection based on whether we are UI thread.
      if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
        // On UI thread, force to async.
        synchronous = false;
      } else {
        // Not on UI thread.
      }
      this.synchronous = synchronous;
      if (forceError) {
        onResponse(HttpStatus.SC_BAD_REQUEST, null, DisabledMessage, null);
        return;
      }
      if (isDebug) {
        String es = entity != null ? (entity instanceof String ? ((String)entity) : "[!string]") : "";
        StringBuffer sb = new StringBuffer();
        if (headers != null) for (Map.Entry<String,String> entry : headers.entrySet()) {
          sb.append((sb.length()>0 ? ", ":"")+entry.getKey()+":"+entry.getValue());
        }
        GLog.d(TAG, methods[methodType]+" "+url+" Headers:"+sb.toString()+es);
      }
      if (synchronous) {
        HttpResponse response = execute(request);
        @SuppressWarnings("unused")
        T result = onResponse(response);
      } else {
        HttpRequestExecutor executor = new HttpRequestExecutor();
        executor.execute(request);
      }
    } catch (Exception e) {
      e.printStackTrace();
      GLog.d(TAG, e.toString());
      onResponse(HttpStatus.SC_BAD_REQUEST, null, e.toString(), null);
      return;
    }
  }

  /**
   * OAuth Sign with HttpUriRequest.
   * 
   * @param request
   * @param oAuthType
   * @return
   * @throws OAuthMessageSignerException
   * @throws OAuthExpectationFailedException
   * @throws OAuthCommunicationException
   */
  protected HttpUriRequest sign(HttpUriRequest request, int oAuthType)
      throws OAuthMessageSignerException, OAuthExpectationFailedException,
      OAuthCommunicationException {
    switch (oAuthType) {
      case OAUTH_TYPE_3LEGGED:
        AuthorizerCore.getInstance().signFor3Legged(request);
        break;
      case OAUTH_TYPE_2LEGGED:
        AuthorizerCore.getInstance().signFor2Legged(request);
        break;
      case OAUTH_TYPE_NONE:
        break;
    }
    return request;
  }

  /**
   * Creates a HttpUriRequest.
   * 
   * @param url
   * @param methodType
   * @param entity
   * @return
   * @throws URISyntaxException
   * @throws MalformedURLException
   */
  protected <E> HttpUriRequest generateRequest(String url, int methodType, E entity)
      throws URISyntaxException, MalformedURLException {
    URI uri = null;
    try {
      URL myUrl = new URL(url);
      uri = myUrl.toURI();
    } catch (URISyntaxException e) {
      GLog.d(TAG, "Specify a invalid URI for HTTP request: " + url);
      throw e;
    } catch (MalformedURLException e) {
      GLog.d(TAG, "Specify a invalid URL for HTTP request: " + url);
      throw e;
    }

    HttpUriRequest request = null;
    switch (methodType) {
      case METHOD_GET:
        request = new HttpGet(uri);
        break;
      case METHOD_POST:
        HttpPost post = new HttpPost(uri);
        HttpEntity postEntity = generateEntity(entity);
        if (postEntity != null) {
          post.setEntity(postEntity);
        }
        request = post;
        break;
      case METHOD_PUT:
        HttpPut put = new HttpPut(uri);
        HttpEntity putEntity = generateEntity(entity);
        if (putEntity != null) {
          put.setEntity(putEntity);
        }
        request = put;
        break;
      case METHOD_DELETE:
        request = new HttpDelete(uri);
        break;
      default:
        throw new RuntimeException("Specify BaseClient.METHOD_TYPE to methodType: " + methodType);
    }
    return onGenerateRequest(request);
  }
  
  /**
   * Event on {@link #generateRequest()}.
   * 
   * @param request
   * @return
   */
  protected HttpUriRequest onGenerateRequest(HttpUriRequest request) {
    return request;
  };

  /**
   * Creates POST/PUT entity from the object. It should implement on child clients.
   * 
   * @param parameter
   * @return
   */
  protected <E> HttpEntity generateEntity(E parameter) {
    if (parameter == null) { return null; }

    HttpEntity entity = null;
    try {
      if (parameter instanceof HttpEntity) {
        entity = (HttpEntity) parameter;
      } else {
        entity = new StringEntity((String) parameter, "UTF-8");
      }
      if (isDebug && entity.isRepeatable()) {
        try {
          String estring = entity.getContent().toString();
          GLog.d(TAG, "Entity:" + estring);
        } catch (IOException ioe) {}
      }
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
      GLog.d(TAG, e.getMessage());
    } catch (ClassCastException e) {
      e.printStackTrace();
      GLog.d(TAG, e.getMessage());
    }
    return entity;
  }

  /**
   * Set HTTP parameters
   * 
   * @param params
   */
  protected void setHttpParams(HttpParams params) {
    if (mUserAgent != null && (mHeader == null || mHeader.get("User-Agent") == null))
      params.setParameter("http.useragent", mUserAgent);
  }

  /**
   * Set request header.
   * 
   * @param request
   */
  protected void setRequestHeader(HttpUriRequest request) {
    String cookie = CookieStorage.getCookie();
    boolean sawCookie = false;
    if (mHeader != null) {
      for (Map.Entry<String, String> entry : mHeader.entrySet()) {
        String key = entry.getKey();
        if (key.equals("Cookie")) sawCookie = true;
        String value = entry.getValue();
        if (value != null) request.setHeader(key, value);
      }
    }
    if (!sawCookie && cookie != null && cookie.length() > 0) request.setHeader("Cookie", cookie);
    request.setHeader("Accept-Language",Locale.getDefault().toString());
  }

  private Map<String, String> mHeader = null;

  public void setRequestHeaders(Map<String, String> header) {
    mHeader = header;
  }

  static boolean haveProxy = false;
  static String proxyHost = null;
  static int proxyPort = 0;
  static boolean ignoreSslErrors = false;

  /**
   * If the system has a proxy defined, activate it for us too.
   * 
   * @throws Exception
   */
  public static void activateDefaultProxy(Context ctx, String url) throws Exception {
    URI uri = new URI(url == null ? "http://www.google.com" : url);
    Proxy proxy = null;
    String proxyHost = null;
    boolean done = false;
    int proxyPort = 0;
    if (Build.VERSION.SDK_INT >= 11) {
      ProxySelector proxySelector = ProxySelector.getDefault();
      List<Proxy> proxyList = proxySelector.select(uri);
      if (proxyList.size() > 0) {
        proxy = proxyList.get(0);
        GLog.d(TAG, "Default Proxy Configuration: " + proxy.toString());
      } else throw new Exception("No valid proxy configuration.");
      InetSocketAddress sa = (InetSocketAddress)proxy.address();
      if (sa != null) {
        proxyHost = sa.getHostName();
        proxyPort = sa.getPort();
        done = true;
        GLog.d(TAG, "proxyHost:" + proxyHost + ":"+proxyPort);
      }
    }
    if (!done) {
      ContentResolver contentResolver = ctx.getContentResolver();
      String ps = Settings.Secure.getString(contentResolver, Settings.Secure.HTTP_PROXY);
      if (ps != null && ps != "" && ps.contains(":")) {
        String[] proxys = ps.split(":");
        if (proxys.length == 2) {
          proxyHost = proxys[0];
          try {
            proxyPort = Integer.parseInt(proxys[1]);
          } catch (NumberFormatException e) {
            GLog.d(TAG, "Port is not a number: " + proxys[1]);
          }
        }
      }
    }
    if (proxyHost != null && proxyPort != 0) setProxy(ctx, proxyHost, proxyPort);
  }

  public static void setProxy(Context ctx, String hostPort) {
    try {
      int colon = hostPort.indexOf(':');
      if (colon < 1) return;
      String host = hostPort.substring(0, colon);
      String portStr = hostPort.substring(colon + 1);
      int port = Integer.parseInt(portStr);
      setProxy(ctx, host, port);
    } catch (Exception ex) {
      GLog.e(TAG, Util.stack2string(ex));
    }
  }

  public static void setProxy(Context ctx, String host, int port) {
    proxyHost = host;
    proxyPort = port;
    haveProxy = (host != null && host.length() > 0 && port > 0);
    setProxySystemProperties(host, port);
    setWebkitProxy(ctx, host, port);
  }

  /**
   * Set properties that might be picked up by WebKit or other subsystems.
   * 
   * @param host
   * @param port
   */
  private static void setProxySystemProperties(String host, int port) {
    System.setProperty("http.proxyHost", host);
    System.setProperty("http.proxyPort", Integer.toString(port));
    System.setProperty("https.proxyHost", host);
    System.setProperty("https.proxyPort", Integer.toString(port));
    // System.setProperty("socks.proxyHost", host);
    // System.setProperty("socks.proxyPort", port + "");
  }

  /**
   * Configure webkit to use the defined proxy.
   * 
   * @param ctx
   * @param host
   * @param port
   * @return
   */
  private static boolean setWebkitProxy(Context ctx, String host, int port) {
    boolean ok = false;
    try {
      Object rq = getRequestQueue(ctx);
      if (rq != null) {
        HttpHost httpHost = new HttpHost(host, port, "http");
        setField(rq, "mProxyHost", httpHost);
      }
    } catch (Exception e) {
      GLog.d(TAG, "setWebkitProxy failure:" + e.toString());
    }
    return ok;
  }

  public static Object getRequestQueue(Context ctx) throws Exception {
    Object ret = null;
    Class networkClass = Class.forName("android.webkit.Network");
    if (networkClass != null) {
      Object networkObj = invoke(networkClass, "getInstance", new Object[] {ctx}, Context.class);
      if (networkObj != null) {
        ret = getField(networkObj, "mRequestQueue");
      }
    }
    return ret;
  }

  private static Object getField(Object obj, String name) throws IllegalArgumentException,
      IllegalAccessException, NoSuchFieldException, SecurityException {
    Field field = obj.getClass().getDeclaredField(name);
    field.setAccessible(true);
    Object ret = field.get(obj);
    return ret;
  }

  private static void setField(Object obj, String name, Object value)
      throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException,
      SecurityException {
    Field field = obj.getClass().getDeclaredField(name);
    field.setAccessible(true);
    field.set(obj, value);
  }

  private static Object invoke(Object obj, String methodName, Object[] params, Class... types)
      throws Exception {
    Object ret = null;
    Class c = obj instanceof Class ? (Class) obj : obj.getClass();
    if (types != null) {
      Method method = c.getMethod(methodName, types);
      ret = method.invoke(obj, params);
    } else {
      Method method = c.getMethod(methodName);
      ret = method.invoke(obj);
    }
    return ret;
  }


  public static void setIgnoreSslErrors(boolean yes) {
    ignoreSslErrors = yes;
  }

  /**
   * Executes the HTTP request.
   * 
   * @param request
   * @return
   */
  // Informed by these pages and many others:
  // http://www.jmarshall.com/easy/http/
  protected HttpResponse execute(HttpUriRequest request) {
    HttpResponse response = null;
    try {
      // DefaultHttpClient client = new DefaultHttpClient();
      ExtraHttpClient client = new ExtraHttpClient();
      if (ignoreSslErrors) client = ExtraHttpClient.useTrustingTrustManager(client);

      if (haveProxy) {
        client.setProxy(proxyHost, proxyPort);
        try { // Tries to set system proxy, which should affect web views.
          // This needs android.permission.WRITE_SETTINGS in AndroidManifest.xml to work.
          Settings.System.putString(context.getContentResolver(), Settings.System.HTTP_PROXY,
              proxyHost + ":" + proxyPort);
        } catch (Exception ex) {}
      }

      client.setConnectionTimeout(CONNECTION_TIMEOUT);
      // HttpConnectionParams.setConnectionTimeout(client.getParams(), CONNECTION_TIMEOUT);
      setHttpParams(client.getParams());
      HttpParams params = client.getParams();
      HttpProtocolParams.setVersion(params, new ProtocolVersion("HTTP", 1, 1));
      params.setBooleanParameter(ClientPNames.HANDLE_AUTHENTICATION, false);
      params.setParameter(HttpProtocolParams.USE_EXPECT_CONTINUE, false);
      params.setParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, 4);
      params.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE, 2);
      HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
      // client.getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);
      client.setRedirectHandler(mRedirectHandler);
      setRequestHeader(request);
      request.setHeader("Accept-Encoding", "gzip");
      request.setHeader("Connection", "close"); // Prevent HTTP 1.1 pipelining.
      try {
        response = client.execute(request);
      } catch (Exception ex) {
        Core.log(TAG,  "BaseClient.execute failed:"+ex.toString());
      }

      if (isDebug)
        Core.log(TAG, LOGGING_PREFIX + request.getMethod() + ":: " + request.getURI().toString());
      if (isDebug && response != null) {
        Core.log(TAG, LOGGING_PREFIX + response.getStatusLine().getStatusCode() + ":: "
            + response.getStatusLine().getReasonPhrase());
      }
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
    return response;
  }

  /**
   * Pick up the response body strings from HttpResponse.
   * 
   * @param response
   * @return
   */
  protected String getResponseBody(HttpResponse response) {
    if (response == null) { return null; }

    HttpEntity entity = response.getEntity();
    if (entity == null) { return null; }

    String result = null;
    try {
      result = EntityUtils.toString(entity);
    } catch (ParseException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return result;
  }

  /**
   * A event on receiving response.
   * 
   * @param response
   * @return
   */
  protected T onResponse(HttpResponse response) {
    final String responseBody = getResponseBody(response);
    HeaderIterator headers = response != null ? response.headerIterator() : null;
    final int responseCode =
        (response != null) ? response.getStatusLine().getStatusCode() : HttpStatus.SC_BAD_REQUEST;
    final String reason = (response != null) ? response.getStatusLine().getReasonPhrase() : "";
    return onResponse(responseCode, responseBody, reason, headers);
  }

  /**
   * An event on receiving response.
   * 
   * @param responseCode
   * @param responseBody
   * @param reason
   * @return
   */
  protected T onResponse(int responseCode, String responseBody, String reason,
      HeaderIterator headers) {
    if (HttpStatus.SC_OK <= responseCode && responseCode < HttpStatus.SC_BAD_REQUEST
        && responseBody != null) {
      // if (typeTest instanceof String);
      final T convertedResponse = convertResponseBody(responseBody);
      if (isDebug && convertedResponse != null && (convertedResponse instanceof String))
        GLog.d(TAG, "Response Body:"+((String)convertedResponse));
      onSuccess(responseCode, headers, convertedResponse);
      return convertedResponse;
    } else {
      StringBuffer sb = new StringBuffer();
      while (headers != null && headers.hasNext()) {
        Header h = headers.nextHeader();
        sb.append((sb.length()>0 ? ", ":"")+h.getName()+":"+h.getValue());
      }
      GLog.d(TAG, "Request failed. Status Code: " + responseCode + ", reason:" + reason+" headers:"+sb.toString());
      onFailure(responseCode, headers, responseBody);
      return null;
    }
  }

  public static String toQueryString(Map<String, Object> params) {
    if (params == null) return "";
    String queryParam = "";
    if (0 < params.size()) {
      for (Map.Entry<String, Object> entry : params.entrySet()) {
        queryParam += entry.getKey() + "=" + entry.getValue() + "&";
      }
      queryParam = queryParam.substring(0, queryParam.length() - 1); // trim last &
    }
    return queryParam;
  }


  public static String toEntityJson(Map<String, Object> params) {
    Type mapType = new TypeToken<Map<String, String>>() {}.getType();
    String ret = null;
    if (params == null) return null;
    if (0 < params.size()) {
      ret = gson.toJson(params, mapType);
    }
    return ret;
  }

  /**
   * Converts the response body strings to the specified Object.
   * 
   * @param responseBody
   * @return
   */
  protected abstract T convertResponseBody(String responseBody);

  protected void onSuccess(final int responseCode, final HeaderIterator headers,
      final T responseBody) {
    if (mListener != null) {
      if (synchronous) mListener.onSuccess(responseCode, headers, responseBody);
      else {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
          public void run() {
            mListener.onSuccess(responseCode, headers, responseBody);
          }
        });
      }
    }
  }

  protected void onFailure(final int responseCode, final HeaderIterator headers,
      final String responseBody) {
    if (mListener != null) {
      if (synchronous) mListener.onFailure(responseCode, headers, responseBody);
      else {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
          public void run() {
            mListener.onFailure(responseCode, headers, responseBody);
          }
        });
      }
    }
  }

  /**
   * Returns valid userAgent string if init(context) has been called or setDefaultUserAgent(String)
   * has been called.
   * 
   * @return Current user agent static value;
   */
  public static String getUserAgent() {
    return sUserAgent;
  }

  /**
   * Finds user agent and updates static sUserAgent variable.
   * http://stackoverflow.com/questions/3626071/retrieve-user-agent-programatically/5261472#5261472
   * 
   * @param context
   * @return
   */
  public static String getSystemDefaultUserAgent(final Context context) {
    try {
      Constructor<WebSettings> constructor =
          WebSettings.class.getDeclaredConstructor(Context.class, WebView.class);
      constructor.setAccessible(true);
      try {
        WebSettings settings = constructor.newInstance(context, null);
        return (sUserAgent = settings.getUserAgentString());
      } finally {
        constructor.setAccessible(false);
      }
    } catch (Exception e) {
      String ua;
      if (Thread.currentThread().getName().equalsIgnoreCase("main")) {
        WebView m_webview = new WebView(context);
        ua = m_webview.getSettings().getUserAgentString();
      } else {
        Thread thread = new Thread() {
          public void run() {
            Looper.prepare();
            WebView m_webview = new WebView(context);
            sUserAgent = m_webview.getSettings().getUserAgentString();
            Looper.loop();
          }
        };
        thread.start();
        return sUserAgent;
      }
      return (sUserAgent = ua);
    }
  }


}
