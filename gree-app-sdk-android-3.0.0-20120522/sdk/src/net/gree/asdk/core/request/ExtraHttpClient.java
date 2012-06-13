/*
 * ==================================================================== Licensed to the Apache
 * Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in
 * writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 * ==================================================================== This software consists of
 * voluntary contributions made by many individuals on behalf of the Apache Software Foundation. For
 * more information on the Apache Software Foundation, please see <http://www.apache.org/>.
 */
package net.gree.asdk.core.request;

import java.io.*;
import java.security.KeyStore;
import java.security.cert.*;
import java.util.zip.GZIPInputStream;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import net.gree.asdk.core.GLog;

import org.apache.http.*;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.*;
import org.apache.http.conn.scheme.*;
import org.apache.http.entity.*;
import org.apache.http.impl.client.*;
import org.apache.http.impl.conn.SingleClientConnManager;
// import org.apache.http.impl.io.ChunkedInputStream;
import org.apache.http.params.*;
import org.apache.http.protocol.*;


public class ExtraHttpClient extends DefaultHttpClient {
  final static String TAG = "ExtraHttpClient";
  InputStream in;

  public ExtraHttpClient(ClientConnectionManager ccm, HttpParams params) {
    super(ccm, params);
    in = null;
    handleGzip(this);
  }

  public ExtraHttpClient() {
    in = null;
    handleGzip(this);
  }

  public ExtraHttpClient(InputStream in) {
    this.in = in;
    handleGzip(this);
  }

  public static void handleGzip(DefaultHttpClient httpclient) {
    try {
      // Handle sending and receiving gzipped content.
      httpclient.addRequestInterceptor(new HttpRequestInterceptor() {
        public void process(final HttpRequest request, final HttpContext context)
            throws HttpException, IOException {
          if (!request.containsHeader("Accept-Encoding"))
            request.addHeader("Accept-Encoding", "gzip");
        }
      });
      // org.apache.http.impl.io.ChunkedInputStream
      // org.apache.commons.io.input.AuthoCloseInputStream
      httpclient.addResponseInterceptor(new HttpResponseInterceptor() {
        public void process(final HttpResponse response, final HttpContext context)
            throws HttpException, IOException {
          HttpEntity entity = response.getEntity();
          String contentLenStr =
              response.containsHeader("Content-Length") ? response.getFirstHeader("Content-Length")
                  .getValue() : "-1";
          @SuppressWarnings("unused")
          long contentLen = -1;
          if (contentLenStr != null) try {
            contentLen = Long.parseLong(contentLenStr);
          } catch (Exception e) {}
          if (entity.isChunked()) {
            // entity.setContent(new ChunkedInputStream(entity.getContent()));
          }
          Header ceheader = entity.getContentEncoding();
          if (ceheader != null) {
            HeaderElement[] codecs = ceheader.getElements();
            for (int i = 0; i < codecs.length; i++) {
              if (codecs[i].getName().equalsIgnoreCase("gzip")) {
                response.setEntity(new GzipDecompressingEntity(response.getEntity()));
                return;
              }
            }
          }
        }
      });
    } catch (Exception e) {
      GLog.e(TAG, "handleGzip failed in calls to addRequestInterceptor:" + e.toString());
    }
  }

  static class GzipDecompressingEntity extends HttpEntityWrapper {
    public GzipDecompressingEntity(final HttpEntity entity) {
      super(entity);
    }

    @Override
    public InputStream getContent() throws IOException, IllegalStateException {
      // the wrapped entity's getContent() decides about repeatability
      InputStream wrappedin = wrappedEntity.getContent();
      return new GZIPInputStream(wrappedin);
    }

    @Override
    public long getContentLength() {
      // length of ungzipped content is not known
      return -1;
    }
  }

  public void setConnectionTimeout(long millis) {
    // this.getConnectionManager().getParams().setConnectionTimeout(millis);
    // this.getParams().setParameter(HttpClientParams.CONNECTION_MANAGER_TIMEOUT, new Long(millis));
    // this.getParams().setParameter(HttpClientParams.SO_TIMEOUT, new Integer(millis));
    this.getParams().setParameter(HttpConnectionParams.CONNECTION_TIMEOUT,
         Integer.valueOf((int) millis));
    this.getParams().setParameter(HttpConnectionParams.SO_TIMEOUT, Integer.valueOf((int) millis));
  }

  public void setProxy(String host, int port) {
    HttpHost proxy = new HttpHost(host, port);
    this.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
  }

  @Override
  protected ClientConnectionManager createClientConnectionManager() {
    SchemeRegistry registry = new SchemeRegistry();
    registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
    registry.register(new Scheme("https", (in == null)
        ? AltSSLSocketFactory.getSocketFactory()
        : newSslSocketFactory(), 443));
    return new SingleClientConnManager(getParams(), registry);
  }

  private AltSSLSocketFactory newSslSocketFactory() {
    try {
      KeyStore trusted;
      try { // If on Android
        trusted = KeyStore.getInstance("BKS");
        if (trusted == null) throw new Exception();
      } catch (Exception e) { // try the normal JKS instead
        trusted = KeyStore.getInstance(KeyStore.getDefaultType()); // "jks" normally.
      }
      // InputStream in = context.getResources().openRawResource(R.raw.keystore);
      try {
        if (in != null) trusted.load(in, "ez24get".toCharArray());
      } finally {
        if (in != null) in.close();
      }
      return new AltSSLSocketFactory(trusted);
    } catch (Exception e) {
      e.printStackTrace();
      throw new AssertionError(e);
    }
  }

  // This method from: http://old.nabble.com/using-SSL-in-a-development-environment-td19001545.html
  public static ExtraHttpClient useTrustingTrustManager(ExtraHttpClient httpClient) {
    try {
      // First create a trust manager that won't care.
      X509TrustManager trustManager = new X509TrustManager() {
        public void checkClientTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
          // Don't do anything.
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
          // Don't do anything.
        }

        public X509Certificate[] getAcceptedIssuers() {
          // Don't do anything.
          return null;
        }
      };

      // Now put the trust manager into an SSLContext.
      SSLContext sslcontext = SSLContext.getInstance("TLS");
      sslcontext.init(null, new TrustManager[] {trustManager}, null);

      // Use the above SSLContext to create your socket factory
      // (I found trying to extend the factory a bit difficult due to a
      // call to createSocket with no arguments, a method which doesn't
      // exist anywhere I can find, but hey-ho).
      AltSSLSocketFactory sf = new AltSSLSocketFactory(sslcontext);
      sf.setHostnameVerifier(AltSSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

      // If you want a thread safe client, use the ThreadSafeConManager, but
      // otherwise just grab the one from the current client, and get hold of its
      // schema registry. THIS IS THE KEY THING.
      ClientConnectionManager ccm = httpClient.getConnectionManager();
      SchemeRegistry schemeRegistry = ccm.getSchemeRegistry();

      // Register our new socket factory with the typical SSL port and the
      // correct protocol name.
      schemeRegistry.register(new Scheme("https", sf, 443));

      // Finally, apply the ClientConnectionManager to the Http Client
      // or, as in this example, create a new one.
      return new ExtraHttpClient(ccm, httpClient.getParams());
    } catch (Throwable t) {
      t.printStackTrace();
      return null;
    }
  }
}
