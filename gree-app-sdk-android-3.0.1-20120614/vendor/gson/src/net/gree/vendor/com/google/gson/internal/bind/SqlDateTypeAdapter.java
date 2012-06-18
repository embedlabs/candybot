/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.gree.vendor.com.google.gson.internal.bind;


import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import net.gree.vendor.com.google.gson.JsonSyntaxException;
import net.gree.vendor.com.google.gson.reflect.TypeToken;
import net.gree.vendor.com.google.gson.stream.JsonReader;
import net.gree.vendor.com.google.gson.stream.JsonToken;
import net.gree.vendor.com.google.gson.stream.JsonWriter;

/**
 * Adapter for java.sql.Date. Although this class appears stateless, it is not.
 * DateFormat captures its time zone and locale when it is created, which gives
 * this class state. DateFormat isn't thread safe either, so this class has
 * to synchronize its read and write methods.
 */
public final class SqlDateTypeAdapter extends TypeAdapter<java.sql.Date> {
  public static final Factory FACTORY = new Factory() {
    @SuppressWarnings("unchecked") // we use a runtime check to make sure the 'T's equal
    public <T> TypeAdapter<T> create(MiniGson context, TypeToken<T> typeToken) {
      return typeToken.getRawType() == java.sql.Date.class
          ? (TypeAdapter<T>) new SqlDateTypeAdapter() : null;
    }
  };

  private final DateFormat format = new SimpleDateFormat("MMM d, yyyy");

  @Override
  public synchronized java.sql.Date read(JsonReader reader) throws IOException {
    if (reader.peek() == JsonToken.NULL) {
      reader.nextNull();
      return null;
    }
    try {
      final long utilDate = format.parse(reader.nextString()).getTime();
      return new java.sql.Date(utilDate);
    } catch (ParseException e) {
      throw new JsonSyntaxException(e);
    }
  }

  @Override
  public synchronized void write(JsonWriter writer, java.sql.Date value) throws IOException {
    writer.value(value == null ? null : format.format(value));
  }
}
