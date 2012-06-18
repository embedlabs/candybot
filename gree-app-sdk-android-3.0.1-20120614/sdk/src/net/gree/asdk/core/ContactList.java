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

package net.gree.asdk.core;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

public class ContactList {

  public static String getContactList(Context context) {
    String id = null;
    ContentResolver resolver = context.getContentResolver();

    JSONArray array = new JSONArray();

    String[] projection =
        new String[] {ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
            ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
            ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME};
    String where =
        ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";

    Cursor cur = resolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
    String firstName = "";
    String lastName = "";
    if (cur.getCount() > 0) {
      while (cur.moveToNext()) {
        Cursor contactsCur = null;
        try {
          JSONObject object = new JSONObject();
          id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
          String[] whereParameters =
              new String[] {id, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE};
          contactsCur =
              resolver.query(ContactsContract.Data.CONTENT_URI, projection, where, whereParameters,
                  null);
          if (contactsCur.moveToFirst()) {
            firstName =
                contactsCur.getString(contactsCur
                    .getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME));
            object.put("firstName", (firstName != null) ? firstName : "");
            lastName =
                contactsCur.getString(contactsCur
                    .getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME));
            object.put("lastName", (lastName != null) ? lastName : "");
            setEmailAddresses(object, context, id);
            setPhoneNumber(object, context, id);
          }
          array.put(object);
        } catch (JSONException e) {
          GLog.d("getContactList()", e.toString());
        } finally {
          if (contactsCur != null) {
            contactsCur.close();
          }
        }
      }
    }
    cur.close();
    return array.toString();
  }

  private static void setEmailAddresses(JSONObject object, Context context, String id)
      throws JSONException {
    ContentResolver resolver = context.getContentResolver();
    Cursor emailCur =
        resolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
            ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", new String[] {id}, null);
    JSONArray array = new JSONArray();
    while (emailCur.moveToNext()) {
    	array.put(emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA)));
    }
    emailCur.close();
    
    if(array.length() > 0)
    	object.put("emails", array);
  }

  private static void setPhoneNumber(JSONObject object, Context context, String id)
      throws JSONException {
    ContentResolver resolver = context.getContentResolver();
    Cursor phoneCur =
        resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
            ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", new String[] {id}, null);
    while (phoneCur.moveToNext()) {
      switch (phoneCur
          .getInt(phoneCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA2))) {
        case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
          object.put("mobilePhoneNumber", phoneCur.getString(phoneCur
              .getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA1)));
          break;
        case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
          object.put("homePhoneNumber", phoneCur.getString(phoneCur
              .getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA1)));
          break;
        default:
          object.put("phoneNumber", phoneCur.getString(phoneCur
              .getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA1)));
          break;
      }
    }
    phoneCur.close();
  }
}
