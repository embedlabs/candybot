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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;

import net.gree.asdk.core.Url;

public class EmojiController {
  private static String EMOJI_DIR;
  
  public EmojiController(Context context) {
    EMOJI_DIR = Environment.getExternalStorageDirectory().toString() + "/Android/data/"+context.getPackageName().toString()+"/files/gree/pictogram";
    String endpoint = Url.getImageUrl(); 
    new EmojiDownloadTask().execute(endpoint);
  }

  public static int getEmojiCount(Context context) {
    EMOJI_DIR = Environment.getExternalStorageDirectory().toString() + "/Android/data/"+context.getPackageName().toString()+"/files/gree/pictogram";
    File dir = new File(EMOJI_DIR);
    if (!dir.exists()) {
      return 0;
    }
    return dir.listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String filename) {
        return filename.endsWith(".png");
      }
    }).length;
  }

  private class EmojiDownloadTask extends AsyncTask<String, Void, Void> {
    private static final int BUFFER_SIZE = 4096;

    @Override
    protected void onPreExecute() {
      File dir = new File(EMOJI_DIR);
      if (!dir.exists()) {
        dir.mkdirs();
      }
      super.onPreExecute();
    }

    @Override
    protected Void doInBackground(String... params) {
      try {
        ArrayList<String> list = checkManifestfile(params[0] + "/dat/");
        if (list == null)
          return null;
        for (String filename : list) {
          File file = new File(EMOJI_DIR, filename);
          requestAndSaveFile(params[0] + "/img/", file, filename);
        }
      } catch (URISyntaxException e) {
        e.printStackTrace();
      } catch (ClientProtocolException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
      return null;
    }

    private ArrayList<String> checkManifestfile(String path) throws IOException, FileNotFoundException, URISyntaxException {
      String filename = "emoji_manifest.txt";
      File file = new File(EMOJI_DIR, filename);
      URI uri = new URI(path + filename);
      HttpClient client = new DefaultHttpClient();
      HttpGet get = new HttpGet();
      get.setURI(uri);
      HttpResponse response = client.execute(get);

      int code = response.getStatusLine().getStatusCode();
      if (code == 200) {
        Header[] headers = response.getHeaders("Last-Modified");
        if (file.exists() && headers.length > 0 && new Date(headers[0].getValue()).getTime() < file.lastModified()) {
          return null;
        }
        file.createNewFile();
        InputStream inputStream = response.getEntity().getContent();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false)));
        ArrayList<String> list = new ArrayList<String>();
        String line = null;
        while ((line = reader.readLine()) != null) {
          if (line.endsWith(".png")) {
            list.add(line);
            writer.write(line);
          }
        }
        writer.flush();
        writer.close();
        reader.close();
        return list;
      } else {
        return null;
      }
    }

    private boolean requestAndSaveFile(String path, File file, String filename) throws IOException, FileNotFoundException, URISyntaxException {
      URI uri = new URI(path + filename);
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
        return true;
      } else {
        return false;
      }
    }

    @Override
    protected void onPostExecute(Void result) {
      super.onPostExecute(result);
    }
  }
}
