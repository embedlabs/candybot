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

package net.gree.platformsample;

import java.util.TreeMap;

import android.app.Application;
import android.util.Log;

import net.gree.asdk.api.GreePlatform;
import net.gree.asdk.api.GreePlatformSettings;
import net.gree.platformsample.util.AppSimpleCache;

/**
 * Sample App Application Class
 * 
 */
public class GreePlatformSampleApplication extends Application {
  @Override
  public void onCreate() {
    super.onCreate();
    GreePlatform.setDebug(true);
    boolean resourceInit = true;
    boolean doScramble = false;
    // For development / debugging:
    // This takes configuration from an XML file (could be .json too).

    if (resourceInit) {
      GreePlatform.initialize(getApplicationContext(), R.xml.gree_platform_configuration, null);
    }

    if (!resourceInit && !doScramble) {
      // GreePlatform.initializeWithUnencryptedConsumerKeyAndSecret(
      // getApplicationContext(), "11720", "GREE", "2...ab7c",
      // "edc39.......5bd7972bbeaf", null, true);
    }
    // Use the following call for inline initialization. options can be null. The key and secret are not obscured.
    // It is able to initialize application.
    // But it is not recommended when you publish your application.
    // GreePlatform.initializeWithUnencryptedConsumerKeyAndSecret(context, appId, name, key, secret, options, debug);
    
    if (!resourceInit && doScramble) {
      // GreePlatform.initialize(getApplicationContext(), "11720", "GREE", "fUgw...Kg==",
      // "KhAO........uEgoFhutMj00G", null, true);
    }
    // Use the following call for inline initialization. options can be null. The encrypted_key and
// encrypted_secret should be obscured output.
    // GreePlatform.initialize(context, appId, name, encrypted_key, encrypted_secret, options,
// debug);
    // You can use devtools/greeEncrypt.rb to create encrypted consumer key and encrypted consumer secret like this.
    // $ ruby devtools/greeEncrypt.rb "path to keysore" "alias" "password of store" "password of key" "consumer key" "consumer secret"
    // For example,
    // $ ruby tools/greeEncrypt.rb ~/.android/debug.keystore androiddebugkey android android ec26c5b8495b 8b76971b196a05737c4f667fb5bcb5b2
   

    Log.v("GreePlatformSampleApp",
        "key=" + GreePlatform.getOption(GreePlatformSettings.ConsumerKey));
    Log.v("GreePlatformSampleApp",
        "sucret=" + GreePlatform.getOption(GreePlatformSettings.ConsumerSecret));

    // turn off the simple application cache by default
    AppSimpleCache.setOn(false);
  }
}
