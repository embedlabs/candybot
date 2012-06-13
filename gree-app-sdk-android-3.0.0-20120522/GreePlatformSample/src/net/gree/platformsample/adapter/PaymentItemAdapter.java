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

package net.gree.platformsample.adapter;

import java.util.List;

import net.gree.asdk.api.wallet.PaymentItem;
import net.gree.platformsample.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * Adapter for payment
 * 
 */
public class PaymentItemAdapter extends BaseAdapter {

  private LayoutInflater mInflater;
  private List<PaymentItem> mPaymentItems;

  /**
   * Initializer
   * 
   * @param context
   * @param newData
   */
  public PaymentItemAdapter(Context context, List<PaymentItem> newData) {
    mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    mPaymentItems = newData;
  }

  @Override
  public int getCount() {
    if (mPaymentItems == null) {
      return 0;
    } else {
      return mPaymentItems.size();
    }
  }

  @Override
  public Object getItem(int position) {
    if (mPaymentItems == null) {
      return null;
    } else {
      return mPaymentItems.get(position);
    }
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public View getView(final int position, View convertView, ViewGroup parent) {
    convertView = mInflater.inflate(R.layout.paymentitem_row, null);
    TextView nameText = (TextView) convertView.findViewById(R.id.paymentItemRowNameText);
    TextView descriptionText =
        (TextView) convertView.findViewById(R.id.paymentItemRowDescriptionText);

    if (mPaymentItems != null) {
      PaymentItem item = mPaymentItems.get(position);
      nameText.setText(item.getItemName());
      descriptionText.setText("id:" + item.getItemId() + ", quantity: " + item.getQuantity()
          + ", unit price: " + item.getUnitPrice());
    }
    return convertView;
  }
}
