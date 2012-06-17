/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.gree.vendor.com.google.gson.internal.bind;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;

import net.gree.vendor.com.google.gson.internal.$Gson$Types;
import net.gree.vendor.com.google.gson.internal.ConstructorConstructor;
import net.gree.vendor.com.google.gson.internal.ObjectConstructor;
import net.gree.vendor.com.google.gson.reflect.TypeToken;
import net.gree.vendor.com.google.gson.stream.JsonReader;
import net.gree.vendor.com.google.gson.stream.JsonToken;
import net.gree.vendor.com.google.gson.stream.JsonWriter;

/**
 * Adapt a homogeneous collection of objects.
 */
public final class CollectionTypeAdapterFactory implements TypeAdapter.Factory {
  private final ConstructorConstructor constructorConstructor;

  public CollectionTypeAdapterFactory(ConstructorConstructor constructorConstructor) {
    this.constructorConstructor = constructorConstructor;
  }

  public <T> TypeAdapter<T> create(MiniGson context, TypeToken<T> typeToken) {
    Type type = typeToken.getType();

    Class<? super T> rawType = typeToken.getRawType();
    if (!Collection.class.isAssignableFrom(rawType)) {
      return null;
    }

    Type elementType = $Gson$Types.getCollectionElementType(type, rawType);
    TypeAdapter<?> elementTypeAdapter = context.getAdapter(TypeToken.get(elementType));
    ObjectConstructor<T> constructor = constructorConstructor.getConstructor(typeToken);

    @SuppressWarnings({"unchecked", "rawtypes"}) // create() doesn't define a type parameter
    TypeAdapter<T> result = new Adapter(context, elementType, elementTypeAdapter, constructor);
    return result;
  }

  private final class Adapter<E> extends TypeAdapter<Collection<E>> {
    private final TypeAdapter<E> elementTypeAdapter;
    private final ObjectConstructor<? extends Collection<E>> constructor;

    public Adapter(MiniGson context, Type elementType,
        TypeAdapter<E> elementTypeAdapter,
        ObjectConstructor<? extends Collection<E>> constructor) {
      this.elementTypeAdapter =
          new TypeAdapterRuntimeTypeWrapper<E>(context, elementTypeAdapter, elementType);
      this.constructor = constructor;
    }

    public Collection<E> read(JsonReader reader) throws IOException {
      if (reader.peek() == JsonToken.NULL) {
        reader.nextNull();
        return null;
      }

      Collection<E> collection = constructor.construct();
      reader.beginArray();
      while (reader.hasNext()) {
        E instance = elementTypeAdapter.read(reader);
        collection.add(instance);
      }
      reader.endArray();
      return collection;
    }

    public void write(JsonWriter writer, Collection<E> collection) throws IOException {
      if (collection == null) {
        writer.nullValue();
        return;
      }

      writer.beginArray();
      for (E element : collection) {
        elementTypeAdapter.write(writer, element);
      }
      writer.endArray();
    }
  }
}
