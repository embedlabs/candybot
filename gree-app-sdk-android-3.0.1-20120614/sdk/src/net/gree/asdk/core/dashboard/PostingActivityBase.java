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

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import net.gree.asdk.core.GLog;
import net.gree.asdk.core.RR;

public abstract class PostingActivityBase extends Activity implements InputFilter{

  protected Button postButton_;
  protected Button cancelButton_;
  protected Button imageButton_;
  protected Button emojiButton_;
  protected int messageLimit_;
  protected EditText title_;
  protected EditText message_;
  protected Intent intentData_;
  protected View photoStackBar_;
  protected ImageView thumbnail_;
  protected EmojiPaletteView emojiPalette_;
  protected int stackNumber_ = 1;
  protected String[] imageData_;
  protected ImageUploader imageUploader_ = null;
  protected boolean titleRequired_;
  protected boolean textRequired_;
  protected boolean photoRequired_;
  private static final String TAG = "PostingActivityBase";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setUp();

    imageData_ = new String[5];

    intentData_ = getIntent();
    if (intentData_.getStringExtra("error") != null) {
      new AlertDialog.Builder(PostingActivityBase.this)
      .setMessage(intentData_.getStringExtra("error"))
      .setPositiveButton(android.R.string.ok, null)
      .show();
    }

    titleRequired_ = intentData_.getBooleanExtra("titleRequired", false);
    textRequired_ = intentData_.getBooleanExtra("textRequired", false);
    photoRequired_ =  intentData_.getBooleanExtra("photoRequired", false);

    String title_string = intentData_.getStringExtra("title");
    if (title_string != null) {
      TextView title = (TextView)findViewById(RR.id("gree_title"));
      title.setText(intentData_.getStringExtra("title"));
    }


    postButton_ = (Button) findViewById(RR.id("gree_postButton"));
    postButton_.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        //assert postStringLength_ <= messageLimit_;
        assert message_.getText().length() > 0;
        if (intentData_.getStringExtra("callback") != null) {
          Intent intent = new Intent(PostingActivityBase.this, DashboardActivity.class);
          if (title_ != null) {
            intent.putExtra("title", title_.getText().toString());
          }
          intent.putExtra("text", message_.getText().toString());

          for (int i = 0; i < imageData_.length; i++) {
            if (imageData_[i] != null) {
              DashboardStorage.putString(PostingActivityBase.this, "image" + i, imageData_[i]);
            }
          }

          intent.putExtra("callbackId", intentData_.getStringExtra("callback"));
          setResult(RESULT_OK, intent);
          finish();
        } else {
          setResult(RESULT_CANCELED, null);
          finish();
        }
      }
    });
    String button_label = intentData_.getStringExtra("button");
    if (button_label != null) {
      postButton_.setText(button_label);
    }

    postButton_.setEnabled(false);

    cancelButton_ = (Button) findViewById(RR.id("gree_cancelButton"));
    cancelButton_.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        cancelDialog();
      }
    });
    
    boolean usePhoto = intentData_.getBooleanExtra("usePhoto", false);
    imageButton_ = (Button) findViewById(RR.id("gree_imageButton"));
    if (usePhoto) {
      imageButton_.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          stackNumber_ = 1;
          imageUploader_.showSelectionDialog();
        }
      });
    } else {
      imageButton_.setVisibility(View.GONE);
    }

    emojiPalette_ = (EmojiPaletteView) findViewById(RR.id("gree_emoji_palette_view"));
    boolean useEmoji = intentData_.getBooleanExtra("useEmoji", false);
    emojiButton_ = (Button) findViewById(RR.id("gree_emojiButton"));
    if (useEmoji) {
      emojiButton_.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          if (EmojiController.getEmojiCount(getApplicationContext()) <= 0) {
            Toast.makeText(PostingActivityBase.this,
              PostingActivityBase.this.getString(RR.string("gree_posting_no_emoji_message")), Toast.LENGTH_SHORT)
              .show();
          }
          View bar = findViewById(RR.id("gree_posting_toolbar"));
          bar.setVisibility(View.GONE);
          emojiPalette_.setVisibility(View.VISIBLE);
          InputMethodManager inputMethodManager =
              (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
          inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
      });
    } else {
      emojiButton_.setVisibility(View.GONE);
    }

    View upper = findViewById(RR.id("gree_posting_layout_upper"));
    upper.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        message_.requestFocus();
        InputMethodManager inputMethodManager =
            (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(message_, 0);
      }
    });

    thumbnail_ = (ImageView) findViewById(RR.id("gree_thumbnail"));
    String imageUrl = intentData_.getStringExtra("image");
    if (imageUrl != null && imageUrl.startsWith("http")) {
      thumbnail_.setVisibility(View.VISIBLE);
      try {
        DefaultHttpClient client = new DefaultHttpClient();
        HttpResponse httpResponse;
        httpResponse = client.execute(new HttpGet(imageUrl));
        if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            HttpEntity httpEntity = httpResponse.getEntity();
            InputStream in;
            in = httpEntity.getContent();

            thumbnail_.setImageBitmap(BitmapFactory.decodeStream(in));
            thumbnail_.invalidate();
          }
        
      } catch (ClientProtocolException e) {
        GLog.printStackTrace(TAG, e);
      } catch (IOException e) {
        GLog.printStackTrace(TAG, e);
      } catch (IllegalStateException e) {
        GLog.printStackTrace(TAG, e);
      }  catch (Exception e) {
        GLog.printStackTrace(TAG, e);
      }
    }

    photoStackBar_ = findViewById(RR.id("gree_photo_stack_bar"));
    int photoCount = intentData_.getIntExtra("photoCount", 0);
    if (photoCount > 1) {
      photoStackBar_.setVisibility(View.VISIBLE);
      if (photoCount > 5)
        photoCount = 5;
      switch (photoCount) {
        case 5:
          ImageView image5 = (ImageView)findViewById(RR.id("gree_photo_stack_5"));
          image5.setVisibility(View.VISIBLE);
          image5.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
              stackNumber_ = 5;
              imageUploader_.showSelectionDialog();
            }
          });
        case 4:
          ImageView image4 = (ImageView)findViewById(RR.id("gree_photo_stack_4"));
          image4.setVisibility(View.VISIBLE);
          image4.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
              stackNumber_ = 4;
              imageUploader_.showSelectionDialog();
            }
          });
        case 3:
          ImageView image3 = (ImageView)findViewById(RR.id("gree_photo_stack_3"));
          image3.setVisibility(View.VISIBLE);
          image3.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
              stackNumber_ = 3;
              imageUploader_.showSelectionDialog();
            }
          });
        case 2:
          ImageView image2 = (ImageView)findViewById(RR.id("gree_photo_stack_2"));
          image2.setVisibility(View.VISIBLE);
          image2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
              stackNumber_ = 2;
              imageUploader_.showSelectionDialog();
            }
          });
          ImageView image1 = (ImageView)findViewById(RR.id("gree_photo_stack_1"));
          image1.setVisibility(View.VISIBLE);
          image1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
              stackNumber_ = 1;
              imageUploader_.showSelectionDialog();
            }
          });
      }
    }
    
    imageUploader_ = new ImageUploader(this, new ImageUploader.ImageUploaderCallback() {
      @Override
      public void callback(ImageUploader.ImageData data) {
        photoStackBar_.setVisibility(View.VISIBLE);
        final ImageView image;
        switch(stackNumber_) {
          case 5:
            image = (ImageView)findViewById(RR.id("gree_photo_stack_5"));
            imageData_[4] = data.mBase64;
            break;
          case 4:
            image = (ImageView)findViewById(RR.id("gree_photo_stack_4"));
            imageData_[3] = data.mBase64;
            break;
          case 3:
            image = (ImageView)findViewById(RR.id("gree_photo_stack_3"));
            imageData_[2] = data.mBase64;
            break;
          case 2:
            image = (ImageView)findViewById(RR.id("gree_photo_stack_2"));
            imageData_[1] = data.mBase64;
            break;
          default:
            image = (ImageView)findViewById(RR.id("gree_photo_stack_1"));
            imageData_[0] = data.mBase64;
            break;
        }
        image.setVisibility(View.VISIBLE);
        image.setImageBitmap(data.mBitmap);
        image.setBackgroundDrawable(null);
        image.setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View v) {
            new AlertDialog.Builder(PostingActivityBase.this)
                .setTitle(android.R.string.dialog_alert_title)
                .setMessage(RR.string("gree_posting_discard_image_message"))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialog, int which) {
                    image.setImageResource(RR.drawable("gree_image_stack"));
                    image.setVisibility(View.GONE);
                    imageData_[stackNumber_ - 1] = null;
                    afterImageDiscarded();
                  }
                }).setNegativeButton(android.R.string.cancel, null).show();
          }
        });

        afterImageSelected();
      }
    });
  }

  protected void afterImageDiscarded() {
    photoStackBar_.setVisibility(View.GONE);

    Editable messageEditable = message_.getText();
    postButton_.setEnabled(messageEditable.toString().length() > 0);
  }

  protected void afterImageSelected() {
    postButton_.setEnabled(isEnablePost());
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (resultCode == RESULT_OK) {
      if (requestCode == RR.integer("gree_request_code_get_image")) {
        imageUploader_.uploadUri(null, data);
      } else if (requestCode == RR.integer("gree_request_code_capture_image")) {
        imageUploader_.uploadImage(null, data);
      } else {
        super.onActivityResult(requestCode, resultCode, data);
      }
    }
  }

  @Override
  public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
    if (source.toString().matches("\\n")) {
      return "";
    } else {
      return source;
    }
  }

  protected void cancelDialog() {
    new AlertDialog.Builder(this)
    .setTitle(android.R.string.dialog_alert_title)
    .setMessage(RR.string("gree_posting_cancel_dialog_message"))
    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        setResult(RESULT_CANCELED, null);
        finish();
      }
    }).setNegativeButton(android.R.string.cancel, null).show();
  }

  protected boolean isEnablePost() {
    if (photoRequired_) {
      return photoStackBar_.getVisibility() == View.VISIBLE;
    }
    return true;
  }

  abstract protected void setUp();
}
