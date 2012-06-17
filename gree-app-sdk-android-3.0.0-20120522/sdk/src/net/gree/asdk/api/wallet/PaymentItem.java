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

package net.gree.asdk.api.wallet;

  /**
   * <p>Class which represents an item to be purchased that will be used for Payment API.</p>
   * Used for GREE Platform Payment API with the Payment class.
   * @author GREE, Inc.
   */
public final class PaymentItem {
  String itemId;
  String itemName;
  double unitPrice;
  int quantity;
  String imageUrl;
  String description;

  /**
   * Constructor
   * @param id item ID
   * @param name item name
   * @param unitPrice the price of the item
   * @param quantity the item quantity to be purchased
   */
  public PaymentItem(String id, String name, double unitPrice, int quantity) {
    this.itemId = id;
    this.itemName = name;
    this.unitPrice = unitPrice;
    this.quantity = quantity;
  }

  /**
   * Set the URL of the item image.
   * @param url URL
   */
  public void setImageUrl(String url) { this.imageUrl = url; }
  /**
   * Set the description of the item.
   * @param description Description
   */
  public void setDescription(String description) { this.description = description; }

  /**
   * Get the item ID
   * @return Item ID
   */
  public String getItemId() { return itemId; }
  /**
   * Get the item name
   * @return Item name
   */
  public String getItemName() { return itemName; }
  /**
   * Get the price of the item
   * @return Price
   */
  public double getUnitPrice() { return unitPrice; }
  /**
   * Get the item quantity to be purchased
   * @return Item quantity to be purchased
   */
  public int getQuantity() { return quantity; }
  /**
   * Get the URL of the item image.
   * @return URL
   */
  public String getImageUrl() { return imageUrl; }
  /**
   * Get the description of the item.
   * @return Description
   */
  public String getDescription() { return description; }

}
