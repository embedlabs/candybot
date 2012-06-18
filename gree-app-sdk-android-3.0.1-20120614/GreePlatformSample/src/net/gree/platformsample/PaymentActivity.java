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

import java.util.ArrayList;

import net.gree.asdk.api.GreePlatform;
import net.gree.asdk.api.wallet.Payment;
import net.gree.asdk.api.wallet.Payment.PaymentListener;
import net.gree.asdk.api.wallet.PaymentItem;
import net.gree.platformsample.adapter.PaymentItemAdapter;

import org.apache.http.HeaderIterator;
import org.apache.http.HttpStatus;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Payment Activity
 */
public class PaymentActivity extends BaseActivity {
  private static final String TAG = "PaymentActivity";
  private PaymentItemAdapter mAdapter;

  private ArrayList<PaymentItem> mPaymentItems = new ArrayList<PaymentItem>();

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    GreePlatform.activityOnCreate(this, true);
    setCustomizeStyle();

    setContentView(R.layout.payment);
    final TextView validateText = (TextView) findViewById(R.id.paymentVerifyText);
    final EditText callBackUrlText = (EditText) findViewById(R.id.callBackUrlText);
    
    Button validateButton = (Button) findViewById(R.id.paymentVerifyButton);
    validateButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        String id = validateText.getText().toString();
        if (!TextUtils.isEmpty(id)) {
          Payment.verify(id, new Payment.VerifyListener() {
            public void onSuccess(int responseCode, HeaderIterator headers, String paymentId) {
              Toast.makeText(PaymentActivity.this, "success", Toast.LENGTH_SHORT).show();
            }

            public void onCancel(int responseCode, HeaderIterator headers, String paymentId) {
              Toast.makeText(PaymentActivity.this, "cancel", Toast.LENGTH_SHORT).show();
            }

            public void onFailure(int responseCode, HeaderIterator headers, String paymentId,
                String response) {
              Log.e(TAG, "status code:"+responseCode+", body: "+response);
              if (HttpStatus.SC_OK <= responseCode && responseCode < HttpStatus.SC_BAD_REQUEST) {
                Toast.makeText(PaymentActivity.this, "transaction fail: "+response, Toast.LENGTH_SHORT).show();
              }
              else {
                Toast.makeText(PaymentActivity.this, "connection fail", Toast.LENGTH_SHORT).show();
              }
            }
          });
        }
      }
    });

    final EditText messageText = (EditText) findViewById(R.id.paymentMessageText);
    Button addButton = (Button) findViewById(R.id.paymentAddButton);
    addButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        new PaymentItemDialog(PaymentActivity.this).show();
      }
    });
    Button requestButton = (Button) findViewById(R.id.paymentRequestButton);
    requestButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        if (0 < mPaymentItems.size()) {
          Payment payment = new Payment(messageText.getText().toString(), mPaymentItems);
          
          if ((callBackUrlText.getText() != null)&&(callBackUrlText.getText().toString().length() > 0)){
            payment.setCallbackUrl(callBackUrlText.getText().toString());
          }
          
          payment.setHandler(new Handler() {
            public void handleMessage(Message message) {
              switch (message.what) {
                case Payment.OPENED:
                  Toast.makeText(PaymentActivity.this, "dialog opened", Toast.LENGTH_SHORT).show();
                  break;
                case Payment.CANCELLED:
                case Payment.ABORTED:
                  Toast.makeText(PaymentActivity.this, "dialog closed", Toast.LENGTH_SHORT).show();
                  break;
                default:
                  break;
              }
            }
          });
          payment.request(PaymentActivity.this, new PaymentListener() {
            public void onSuccess(int responseCode, HeaderIterator headers, String paymentId) {
              validateText.setText(paymentId);
              Toast.makeText(PaymentActivity.this, "success", Toast.LENGTH_SHORT).show();
            }

            public void onFailure(int responseCode, HeaderIterator headers, String paymentId,
                String response) {
              validateText.setText(paymentId);
              Log.e(TAG, "status code:"+responseCode+", body: "+response);
              if (HttpStatus.SC_OK <= responseCode && responseCode < HttpStatus.SC_BAD_REQUEST) {
                Toast.makeText(PaymentActivity.this, "transaction fail: "+response, Toast.LENGTH_SHORT).show();
              }
              else {
                Toast.makeText(PaymentActivity.this, "connection fail", Toast.LENGTH_SHORT).show();
              }
            }

            public void onCancel(int responseCode, HeaderIterator headers, String paymentId) {
              validateText.setText(paymentId);
              Toast.makeText(PaymentActivity.this, "cancel", Toast.LENGTH_SHORT).show();
            }
          });
        }
      }
    });

    ListView listView = (ListView) findViewById(R.id.paymentListView);
    mAdapter = new PaymentItemAdapter(this, mPaymentItems);
    listView.setAdapter(mAdapter);
    listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
      public boolean onItemLongClick(AdapterView<?> arg0, View arg1, final int arg2, long arg3) {
        if (arg2 < mPaymentItems.size()) {
          AlertDialog.Builder builder = new AlertDialog.Builder(PaymentActivity.this);
          builder.setTitle("Confirmation");
          builder.setMessage("Remove OK?");
          builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
              mPaymentItems.remove(arg2);
              mAdapter.notifyDataSetChanged();
            }
          });
          builder.setNegativeButton("No", null);
          builder.create().show();
        }
        return false;
      }
    });
  }

  @Override
  public void onDetachedFromWindow() {
    try {
      super.onDetachedFromWindow();
    } catch (IllegalArgumentException e) {
      Log.e(TAG, "IllegalArgumentException");
    }
  }

  private void addPaymentItem(PaymentItem item) {
    mPaymentItems.add(item);
  }

  /**
   * Payment Item Dialog
   * 
   */
  private class PaymentItemDialog extends Dialog {
    public PaymentItemDialog(Context context) {
      super(context);
      requestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.paymentitem_dialog);
      setupWidget();
    }

    private void setupWidget() {
      final EditText itemIdText = (EditText) findViewById(R.id.paymentItemIdText);
      final EditText itemNameText = (EditText) findViewById(R.id.paymentItemNameText);
      final EditText unitPriceText = (EditText) findViewById(R.id.paymentUnitPriceText);
      final EditText quantityText = (EditText) findViewById(R.id.paymentQuantityText);
      final EditText imageUrlText = (EditText) findViewById(R.id.paymentImageUrlText);
      final EditText descriptionText = (EditText) findViewById(R.id.paymentDescriptionText);
      Button addButton = (Button) findViewById(R.id.paymentItemAddButton);
      addButton.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
          String id = itemIdText.getText().toString();
          if (TextUtils.isEmpty(id)) { return; }
          String name = itemNameText.getText().toString();
          if (TextUtils.isEmpty(name)) { return; }
          String unitPrice = unitPriceText.getText().toString();
          if (TextUtils.isEmpty(unitPrice)) { return; }
          String quantity = quantityText.getText().toString();
          if (TextUtils.isEmpty(quantity)) { return; }
          Double unitPriceD = 0.0;
          try {
            unitPriceD = Double.parseDouble(unitPrice);
          } catch (Exception e) {
            e.printStackTrace();
          }
          int quantityI = 0;
          try {
            quantityI = Integer.parseInt(quantity);
          } catch (Exception e) {
            e.printStackTrace();
          }
          PaymentItem item = new PaymentItem(id, name, unitPriceD, quantityI);

          String imageUrl = imageUrlText.getText().toString();
          if (!TextUtils.isEmpty(imageUrl)) {
            item.setImageUrl(imageUrl);
          }
          String description = descriptionText.getText().toString();
          if (!TextUtils.isEmpty(description)) {
            item.setDescription(description);
          }

          addPaymentItem(item);
          mAdapter.notifyDataSetChanged();
          dismiss();
        }
      });
      Button cancelButton = (Button) findViewById(R.id.paymentItemCancelButton);
      cancelButton.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
          dismiss();
        }
      });
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    if (!tryLoginAndLoadProfilePage()) {
      return;
    }
    setUpBackButton();
    setUpAutoLoadMore();
  }

  @Override
  protected void sync(boolean fromStart) {
  }
}
