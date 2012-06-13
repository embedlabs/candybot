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
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

import net.gree.vendor.com.google.gson.JsonSyntaxException;
import net.gree.vendor.com.google.gson.internal.$Gson$Types;
import net.gree.vendor.com.google.gson.internal.ConstructorConstructor;
import net.gree.vendor.com.google.gson.internal.ObjectConstructor;
import net.gree.vendor.com.google.gson.internal.Primitives;
import net.gree.vendor.com.google.gson.reflect.TypeToken;
import net.gree.vendor.com.google.gson.stream.JsonReader;
import net.gree.vendor.com.google.gson.stream.JsonToken;
import net.gree.vendor.com.google.gson.stream.JsonWriter;

/**
 * Type adapter that reflects over the fields and methods of a class.
 */
public class ReflectiveTypeAdapterFactory implements TypeAdapter.Factory {
  private final ConstructorConstructor constructorConstructor;

  public ReflectiveTypeAdapterFactory(ConstructorConstructor constructorConstructor) {
    this.constructorConstructor = constructorConstructor;
  }

  protected boolean serializeField(Class<?> declaringClazz, Field f, Type declaredType) {
    return !f.isSynthetic();
  }

  protected boolean deserializeField(Class<?> declaringClazz, Field f, Type declaredType) {
    return !f.isSynthetic();
  }

  protected String getFieldName(Class<?> declaringClazz, Field f, Type declaredType) {
    return f.getName();
  }

  public <T> TypeAdapter<T> create(MiniGson context, final TypeToken<T> type) {
    Class<? super T> raw = type.getRawType();

    if (!Object.class.isAssignableFrom(raw)) {
      return null; // it's a primitive!
    }

    ObjectConstructor<T> constructor = constructorConstructor.getConstructor(type);
    return new Adapter<T>(constructor, getBoundFields(context, type, raw));
  }

  private ReflectiveTypeAdapterFactory.BoundField createBoundField(
      final MiniGson context, final Field field, final String name,
      final TypeToken<?> fieldType, boolean serialize, boolean deserialize) {
    final boolean isPrimitive = Primitives.isPrimitive(fieldType.getRawType());

    // special casing primitives here saves ~5% on Android...
    return new ReflectiveTypeAdapterFactory.BoundField(name, serialize, deserialize) {
      final TypeAdapter<?> typeAdapter = context.getAdapter(fieldType);
      @SuppressWarnings({"unchecked", "rawtypes"}) // the type adapter and field type always agree
      @Override void write(JsonWriter writer, Object value)
          throws IOException, IllegalAccessException {
        Object fieldValue = field.get(value);
        TypeAdapter t =
          new TypeAdapterRuntimeTypeWrapper(context, this.typeAdapter, fieldType.getType());
        t.write(writer, fieldValue);
      }
      @Override void read(JsonReader reader, Object value)
          throws IOException, IllegalAccessException {
        Object fieldValue = typeAdapter.read(reader);
        if (fieldValue != null || !isPrimitive) {
          field.set(value, fieldValue);
        }
      }
    };
  }

  private Map<String, BoundField> getBoundFields(
      MiniGson context, TypeToken<?> type, Class<?> raw) {
    Map<String, BoundField> result = new LinkedHashMap<String, BoundField>();
    if (raw.isInterface()) {
      return result;
    }

    Type declaredType = type.getType();
    while (raw != Object.class) {
      Field[] fields = raw.getDeclaredFields();
      AccessibleObject.setAccessible(fields, true);
      for (Field field : fields) {
        boolean serialize = serializeField(raw, field, declaredType);
        boolean deserialize = deserializeField(raw, field, declaredType);
        if (!serialize && !deserialize) {
          continue;
        }
        Type fieldType = $Gson$Types.resolve(type.getType(), raw, field.getGenericType());
        BoundField boundField = createBoundField(context, field, getFieldName(raw, field, declaredType),
            TypeToken.get(fieldType), serialize, deserialize);
        BoundField previous = result.put(boundField.name, boundField);
        if (previous != null) {
          throw new IllegalArgumentException(declaredType
              + " declares multiple JSON fields named " + previous.name);
        }
      }
      type = TypeToken.get($Gson$Types.resolve(type.getType(), raw, raw.getGenericSuperclass()));
      raw = type.getRawType();
    }
    return result;
  }

  static abstract class BoundField {
    final String name;
    final boolean serialized;
    final boolean deserialized;

    protected BoundField(String name, boolean serialized, boolean deserialized) {
      this.name = name;
      this.serialized = serialized;
      this.deserialized = deserialized;
    }

    abstract void write(JsonWriter writer, Object value) throws IOException, IllegalAccessException;
    abstract void read(JsonReader reader, Object value) throws IOException, IllegalAccessException;
  }

  public final class Adapter<T> extends TypeAdapter<T> {
    private final ObjectConstructor<T> constructor;
    private final Map<String, BoundField> boundFields;

    private Adapter(ObjectConstructor<T> constructor, Map<String, BoundField> boundFields) {
      this.constructor = constructor;
      this.boundFields = boundFields;
    }

    @Override
    public T read(JsonReader reader) throws IOException {
      if (reader.peek() == JsonToken.NULL) {
        reader.nextNull();
        return null;
      }

      T instance = constructor.construct();

     

      try {
        reader.beginObject();
        while (reader.hasNext()) {
          String name = reader.nextName();
          BoundField field = boundFields.get(name);
          if (field == null || !field.deserialized) {
           
            reader.skipValue();
          } else {
            field.read(reader, instance);
          }
        }
      } catch (IllegalStateException e) {
        throw new JsonSyntaxException(e);
      } catch (IllegalAccessException e) {
        throw new AssertionError(e);
      }
      reader.endObject();
      return instance;
    }

    @Override
    public void write(JsonWriter writer, T value) throws IOException {
      if (value == null) {
        writer.nullValue();
        return;
      }

      writer.beginObject();
      try {
        for (BoundField boundField : boundFields.values()) {
          if (boundField.serialized) {
            writer.name(boundField.name);
            boundField.write(writer, value);
          }
        }
      } catch (IllegalAccessException e) {
        throw new AssertionError();
      }
      writer.endObject();
    }
  }
}
