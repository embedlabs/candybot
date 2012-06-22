/*
 * Copyright (C) 2008 Google Inc.
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

package net.gree.vendor.com.google.gson;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.gree.vendor.com.google.gson.internal.ConstructorConstructor;
import net.gree.vendor.com.google.gson.internal.ParameterizedTypeHandlerMap;
import net.gree.vendor.com.google.gson.internal.Primitives;
import net.gree.vendor.com.google.gson.internal.Streams;
import net.gree.vendor.com.google.gson.internal.bind.ArrayTypeAdapter;
import net.gree.vendor.com.google.gson.internal.bind.BigDecimalTypeAdapter;
import net.gree.vendor.com.google.gson.internal.bind.BigIntegerTypeAdapter;
import net.gree.vendor.com.google.gson.internal.bind.CollectionTypeAdapterFactory;
import net.gree.vendor.com.google.gson.internal.bind.DateTypeAdapter;
import net.gree.vendor.com.google.gson.internal.bind.ExcludedTypeAdapterFactory;
import net.gree.vendor.com.google.gson.internal.bind.JsonElementReader;
import net.gree.vendor.com.google.gson.internal.bind.JsonElementWriter;
import net.gree.vendor.com.google.gson.internal.bind.MapTypeAdapterFactory;
import net.gree.vendor.com.google.gson.internal.bind.MiniGson;
import net.gree.vendor.com.google.gson.internal.bind.ObjectTypeAdapter;
import net.gree.vendor.com.google.gson.internal.bind.ReflectiveTypeAdapterFactory;
import net.gree.vendor.com.google.gson.internal.bind.SqlDateTypeAdapter;
import net.gree.vendor.com.google.gson.internal.bind.TimeTypeAdapter;
import net.gree.vendor.com.google.gson.internal.bind.TypeAdapter;
import net.gree.vendor.com.google.gson.internal.bind.TypeAdapters;
import net.gree.vendor.com.google.gson.reflect.TypeToken;
import net.gree.vendor.com.google.gson.stream.JsonReader;
import net.gree.vendor.com.google.gson.stream.JsonToken;
import net.gree.vendor.com.google.gson.stream.JsonWriter;
import net.gree.vendor.com.google.gson.stream.MalformedJsonException;

/**
 * This is the main class for using Gson. Gson is typically used by first constructing a
 * Gson instance and then invoking {@link #toJson(Object)} or {@link #fromJson(String, Class)}
 * methods on it.
 *
 * <p>You can create a Gson instance by invoking {@code new Gson()} if the default configuration
 * is all you need. You can also use {@link GsonBuilder} to build a Gson instance with various
 * configuration options such as versioning support, pretty printing, custom
 * {@link JsonSerializer}s, {@link JsonDeserializer}s, and {@link InstanceCreator}s.</p>
 *
 * <p>Here is an example of how Gson is used for a simple Class:
 *
 * <pre>
 * Gson gson = new Gson(); // Or use new GsonBuilder().create();
 * MyType target = new MyType();
 * String json = gson.toJson(target); // serializes target to Json
 * MyType target2 = gson.fromJson(json, MyType.class); // deserializes json into target2
 * </pre></p>
 *
 * <p>If the object that your are serializing/deserializing is a {@code ParameterizedType}
 * (i.e. contains at least one type parameter and may be an array) then you must use the
 * {@link #toJson(Object, Type)} or {@link #fromJson(String, Type)} method.  Here is an
 * example for serializing and deserialing a {@code ParameterizedType}:
 *
 * <pre>
 * Type listType = new TypeToken&lt;List&lt;String&gt;&gt;() {}.getType();
 * List&lt;String&gt; target = new LinkedList&lt;String&gt;();
 * target.add("blah");
 *
 * Gson gson = new Gson();
 * String json = gson.toJson(target, listType);
 * List&lt;String&gt; target2 = gson.fromJson(json, listType);
 * </pre></p>
 *
 * <p>See the <a href="https://sites.google.com/site/gson/gson-user-guide">Gson User Guide</a>
 * for a more complete set of examples.</p>
 *
 * @see com.google.gson.reflect.TypeToken
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public final class Gson {
  @SuppressWarnings("rawtypes")
  static final ParameterizedTypeHandlerMap EMPTY_MAP =
    new ParameterizedTypeHandlerMap().makeUnmodifiable();

   static final boolean DEFAULT_JSON_NON_EXECUTABLE = false;

  // Default instances of plug-ins
  static final AnonymousAndLocalClassExclusionStrategy DEFAULT_ANON_LOCAL_CLASS_EXCLUSION_STRATEGY =
      new AnonymousAndLocalClassExclusionStrategy();
  static final SyntheticFieldExclusionStrategy DEFAULT_SYNTHETIC_FIELD_EXCLUSION_STRATEGY =
      new SyntheticFieldExclusionStrategy(true);
  static final ModifierBasedExclusionStrategy DEFAULT_MODIFIER_BASED_EXCLUSION_STRATEGY =
      new ModifierBasedExclusionStrategy(Modifier.TRANSIENT, Modifier.STATIC);
  static final FieldNamingStrategy2 DEFAULT_NAMING_POLICY =
      new SerializedNameAnnotationInterceptingNamingPolicy(new JavaFieldNamingPolicy());

  private static final ExclusionStrategy DEFAULT_EXCLUSION_STRATEGY = createExclusionStrategy();

  private static final String JSON_NON_EXECUTABLE_PREFIX = ")]}'\n";

  private final ExclusionStrategy deserializationExclusionStrategy;
  private final ExclusionStrategy serializationExclusionStrategy;
  private final ConstructorConstructor constructorConstructor;

  /** Map containing Type or Class objects as keys */
  private final ParameterizedTypeHandlerMap<JsonSerializer<?>> serializers;

  /** Map containing Type or Class objects as keys */
  private final ParameterizedTypeHandlerMap<JsonDeserializer<?>> deserializers;

  private final boolean serializeNulls;
  private final boolean htmlSafe;
  private final boolean generateNonExecutableJson;
  private final boolean prettyPrinting;

  private final MiniGson miniGson;

  /**
   * Constructs a Gson object with default configuration. The default configuration has the
   * following settings:
   * <ul>
   *   <li>The JSON generated by <code>toJson</code> methods is in compact representation. This
   *   means that all the unneeded white-space is removed. You can change this behavior with
   *   {@link GsonBuilder#setPrettyPrinting()}. </li>
   *   <li>The generated JSON omits all the fields that are null. Note that nulls in arrays are
   *   kept as is since an array is an ordered list. Moreover, if a field is not null, but its
   *   generated JSON is empty, the field is kept. You can configure Gson to serialize null values
   *   by setting {@link GsonBuilder#serializeNulls()}.</li>
   *   <li>Gson provides default serialization and deserialization for Enums, {@link Map},
   *   {@link java.net.URL}, {@link java.net.URI}, {@link java.util.Locale}, {@link java.util.Date},
   *   {@link java.math.BigDecimal}, and {@link java.math.BigInteger} classes. If you would prefer
   *   to change the default representation, you can do so by registering a type adapter through
   *   {@link GsonBuilder#registerTypeAdapter(Type, Object)}. </li>
   *   <li>The default Date format is same as {@link java.text.DateFormat#DEFAULT}. This format
   *   ignores the millisecond portion of the date during serialization. You can change
   *   this by invoking {@link GsonBuilder#setDateFormat(int)} or
   *   {@link GsonBuilder#setDateFormat(String)}. </li>
   *   <li>By default, Gson ignores the {@link net.gree.gson.annotations.google.gson.annotations.Expose} annotation.
   *   You can enable Gson to serialize/deserialize only those fields marked with this annotation
   *   through {@link GsonBuilder#excludeFieldsWithoutExposeAnnotation()}. </li>
   *   <li>By default, Gson ignores the {@link net.gree.gson.annotations.google.gson.annotations.Since} annotation. You
   *   can enable Gson to use this annotation through {@link GsonBuilder#setVersion(double)}.</li>
   *   <li>The default field naming policy for the output Json is same as in Java. So, a Java class
   *   field <code>versionNumber</code> will be output as <code>&quot;versionNumber@quot;</code> in
   *   Json. The same rules are applied for mapping incoming Json to the Java classes. You can
   *   change this policy through {@link GsonBuilder#setFieldNamingPolicy(FieldNamingPolicy)}.</li>
   *   <li>By default, Gson excludes <code>transient</code> or <code>static</code> fields from
   *   consideration for serialization and deserialization. You can change this behavior through
   *   {@link GsonBuilder#excludeFieldsWithModifiers(int...)}.</li>
   * </ul>
   */
  @SuppressWarnings("unchecked")
  public Gson() {
    this(DEFAULT_EXCLUSION_STRATEGY, DEFAULT_EXCLUSION_STRATEGY, DEFAULT_NAMING_POLICY,
        EMPTY_MAP, false, EMPTY_MAP, EMPTY_MAP, false, DEFAULT_JSON_NON_EXECUTABLE, true,
        false, false, LongSerializationPolicy.DEFAULT,
        Collections.<TypeAdapter.Factory>emptyList());
  }

  Gson(final ExclusionStrategy deserializationExclusionStrategy,
      final ExclusionStrategy serializationExclusionStrategy,
      final FieldNamingStrategy2 fieldNamingPolicy,
      final ParameterizedTypeHandlerMap<InstanceCreator<?>> instanceCreators, boolean serializeNulls,
      final ParameterizedTypeHandlerMap<JsonSerializer<?>> serializers,
      final ParameterizedTypeHandlerMap<JsonDeserializer<?>> deserializers,
      boolean complexMapKeySerialization, boolean generateNonExecutableGson, boolean htmlSafe,
      boolean prettyPrinting, boolean serializeSpecialFloatingPointValues,
      LongSerializationPolicy longSerializationPolicy,
      List<TypeAdapter.Factory> typeAdapterFactories) {
    this.deserializationExclusionStrategy = deserializationExclusionStrategy;
    this.serializationExclusionStrategy = serializationExclusionStrategy;
    this.constructorConstructor = new ConstructorConstructor(instanceCreators);
    this.serializeNulls = serializeNulls;
    this.serializers = serializers;
    this.deserializers = deserializers;
    this.generateNonExecutableJson = generateNonExecutableGson;
    this.htmlSafe = htmlSafe;
    this.prettyPrinting = prettyPrinting;

    /*
      TODO: for serialization, honor:
        serializationExclusionStrategy
        fieldNamingPolicy
        serializeNulls
        serializers
     */
    TypeAdapter.Factory reflectiveTypeAdapterFactory
        = new ReflectiveTypeAdapterFactory(constructorConstructor) {
      @Override
      public String getFieldName(Class<?> declaringClazz, Field f, Type declaredType) {
        return fieldNamingPolicy.translateName(new FieldAttributes(declaringClazz, f));
      }
      @Override
      public boolean serializeField(Class<?> declaringClazz, Field f, Type declaredType) {
        ExclusionStrategy strategy = Gson.this.serializationExclusionStrategy;
        return !strategy.shouldSkipClass(f.getType())
            && !strategy.shouldSkipField(new FieldAttributes(declaringClazz, f));
      }

      @Override
      public boolean deserializeField(Class<?> declaringClazz, Field f, Type declaredType) {
        ExclusionStrategy strategy = Gson.this.deserializationExclusionStrategy;
        return !strategy.shouldSkipClass(f.getType())
            && !strategy.shouldSkipField(new FieldAttributes(declaringClazz, f));
      }
    };

    MiniGson.Builder builder = new MiniGson.Builder()
        .withoutDefaultFactories()
        .factory(TypeAdapters.STRING_FACTORY)
        .factory(TypeAdapters.INTEGER_FACTORY)
        .factory(TypeAdapters.BOOLEAN_FACTORY)
        .factory(TypeAdapters.BYTE_FACTORY)
        .factory(TypeAdapters.SHORT_FACTORY)
        .factory(TypeAdapters.newFactory(long.class, Long.class,
            longAdapter(longSerializationPolicy)))
        .factory(TypeAdapters.newFactory(double.class, Double.class,
            doubleAdapter(serializeSpecialFloatingPointValues)))
        .factory(TypeAdapters.newFactory(float.class, Float.class,
            floatAdapter(serializeSpecialFloatingPointValues)))
        .factory(new ExcludedTypeAdapterFactory(
            serializationExclusionStrategy, deserializationExclusionStrategy))
        .factory(TypeAdapters.NUMBER_FACTORY)
        .factory(TypeAdapters.CHARACTER_FACTORY)
        .factory(TypeAdapters.STRING_BUILDER_FACTORY)
        .factory(TypeAdapters.STRING_BUFFER_FACTORY)
        .typeAdapter(BigDecimal.class, new BigDecimalTypeAdapter())
        .typeAdapter(BigInteger.class, new BigIntegerTypeAdapter())
        .factory(TypeAdapters.JSON_ELEMENT_FACTORY)
        .factory(ObjectTypeAdapter.FACTORY);

    for (TypeAdapter.Factory factory : typeAdapterFactories) {
      builder.factory(factory);
    }

    builder
        .factory(new GsonToMiniGsonTypeAdapterFactory(this, serializers, deserializers))
        .factory(new CollectionTypeAdapterFactory(constructorConstructor))
        .factory(TypeAdapters.URL_FACTORY)
        .factory(TypeAdapters.URI_FACTORY)
        .factory(TypeAdapters.UUID_FACTORY)
        .factory(TypeAdapters.LOCALE_FACTORY)
        .factory(TypeAdapters.INET_ADDRESS_FACTORY)
        .factory(TypeAdapters.BIT_SET_FACTORY)
        .factory(DateTypeAdapter.FACTORY)
        .factory(TypeAdapters.CALENDAR_FACTORY)
        .factory(TimeTypeAdapter.FACTORY)
        .factory(SqlDateTypeAdapter.FACTORY)
        .factory(TypeAdapters.TIMESTAMP_FACTORY)
        .factory(new MapTypeAdapterFactory(constructorConstructor, complexMapKeySerialization))
        .factory(ArrayTypeAdapter.FACTORY)
        .factory(TypeAdapters.ENUM_FACTORY)
        .factory(reflectiveTypeAdapterFactory);

    this.miniGson = builder.build();
  }

  private TypeAdapter<Number> doubleAdapter(boolean serializeSpecialFloatingPointValues) {
    if (serializeSpecialFloatingPointValues) {
      return TypeAdapters.DOUBLE;
    }
    return new TypeAdapter<Number>() {
      @Override public Double read(JsonReader reader) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
          reader.nextNull();
          return null;
        }
        return reader.nextDouble();
      }
      @Override public void write(JsonWriter writer, Number value) throws IOException {
        if (value == null) {
          writer.nullValue();
          return;
        }
        double doubleValue = value.doubleValue();
        checkValidFloatingPoint(doubleValue);
        writer.value(value);
      }
    };
  }

  private TypeAdapter<Number> floatAdapter(boolean serializeSpecialFloatingPointValues) {
    if (serializeSpecialFloatingPointValues) {
      return TypeAdapters.FLOAT;
    }
    return new TypeAdapter<Number>() {
      @Override public Float read(JsonReader reader) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
          reader.nextNull();
          return null;
        }
        return (float) reader.nextDouble();
      }
      @Override public void write(JsonWriter writer, Number value) throws IOException {
        if (value == null) {
          writer.nullValue();
          return;
        }
        float floatValue = value.floatValue();
        checkValidFloatingPoint(floatValue);
        writer.value(value);
      }
    };
  }

  private void checkValidFloatingPoint(double value) {
    if (Double.isNaN(value) || Double.isInfinite(value)) {
      throw new IllegalArgumentException(value
          + " is not a valid double value as per JSON specification. To override this"
          + " behavior, use GsonBuilder.serializeSpecialDoubleValues() method.");
    }
  }

  private TypeAdapter<Number> longAdapter(LongSerializationPolicy longSerializationPolicy) {
    if (longSerializationPolicy == LongSerializationPolicy.DEFAULT) {
      return TypeAdapters.LONG;
    }
    return new TypeAdapter<Number>() {
      @Override public Number read(JsonReader reader) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
          reader.nextNull();
          return null;
        }
        return reader.nextLong();
      }
      @Override public void write(JsonWriter writer, Number value) throws IOException {
        if (value == null) {
          writer.nullValue();
          return;
        }
        writer.value(value.toString());
      }
    };
  }

  private static ExclusionStrategy createExclusionStrategy() {
    List<ExclusionStrategy> strategies = new LinkedList<ExclusionStrategy>();
    strategies.add(DEFAULT_ANON_LOCAL_CLASS_EXCLUSION_STRATEGY);
    strategies.add(DEFAULT_SYNTHETIC_FIELD_EXCLUSION_STRATEGY);
    strategies.add(DEFAULT_MODIFIER_BASED_EXCLUSION_STRATEGY);
    return new DisjunctionExclusionStrategy(strategies);
  }

  /**
   * This method serializes the specified object into its equivalent representation as a tree of
   * {@link JsonElement}s. This method should be used when the specified object is not a generic
   * type. This method uses {@link Class#getClass()} to get the type for the specified object, but
   * the {@code getClass()} loses the generic type information because of the Type Erasure feature
   * of Java. Note that this method works fine if the any of the object fields are of generic type,
   * just the object itself should not be of a generic type. If the object is of generic type, use
   * {@link #toJsonTree(Object, Type)} instead.
   *
   * @param src the object for which Json representation is to be created setting for Gson
   * @return Json representation of {@code src}.
   * @since 1.4
   */
  public JsonElement toJsonTree(Object src) {
    if (src == null) {
      return JsonNull.INSTANCE;
    }
    return toJsonTree(src, src.getClass());
  }

  /**
   * This method serializes the specified object, including those of generic types, into its
   * equivalent representation as a tree of {@link JsonElement}s. This method must be used if the
   * specified object is a generic type. For non-generic objects, use {@link #toJsonTree(Object)}
   * instead.
   *
   * @param src the object for which JSON representation is to be created
   * @param typeOfSrc The specific genericized type of src. You can obtain
   * this type by using the {@link com.google.gson.reflect.TypeToken} class. For example,
   * to get the type for {@code Collection<Foo>}, you should use:
   * <pre>
   * Type typeOfSrc = new TypeToken&lt;Collection&lt;Foo&gt;&gt;(){}.getType();
   * </pre>
   * @return Json representation of {@code src}
   * @since 1.4
   */
  public JsonElement toJsonTree(Object src, Type typeOfSrc) {
    JsonElementWriter writer = new JsonElementWriter();
    toJson(src, typeOfSrc, writer);
    return writer.get();
  }

  /**
   * This method serializes the specified object into its equivalent Json representation.
   * This method should be used when the specified object is not a generic type. This method uses
   * {@link Class#getClass()} to get the type for the specified object, but the
   * {@code getClass()} loses the generic type information because of the Type Erasure feature
   * of Java. Note that this method works fine if the any of the object fields are of generic type,
   * just the object itself should not be of a generic type. If the object is of generic type, use
   * {@link #toJson(Object, Type)} instead. If you want to write out the object to a
   * {@link Writer}, use {@link #toJson(Object, Appendable)} instead.
   *
   * @param src the object for which Json representation is to be created setting for Gson
   * @return Json representation of {@code src}.
   */
  public String toJson(Object src) {
    if (src == null) {
      return toJson(JsonNull.INSTANCE);
    }
    return toJson(src, src.getClass());
  }

  /**
   * This method serializes the specified object, including those of generic types, into its
   * equivalent Json representation. This method must be used if the specified object is a generic
   * type. For non-generic objects, use {@link #toJson(Object)} instead. If you want to write out
   * the object to a {@link Appendable}, use {@link #toJson(Object, Type, Appendable)} instead.
   *
   * @param src the object for which JSON representation is to be created
   * @param typeOfSrc The specific genericized type of src. You can obtain
   * this type by using the {@link com.google.gson.reflect.TypeToken} class. For example,
   * to get the type for {@code Collection<Foo>}, you should use:
   * <pre>
   * Type typeOfSrc = new TypeToken&lt;Collection&lt;Foo&gt;&gt;(){}.getType();
   * </pre>
   * @return Json representation of {@code src}
   */
  public String toJson(Object src, Type typeOfSrc) {
    StringWriter writer = new StringWriter();
    toJson(src, typeOfSrc, writer);
    return writer.toString();
  }

  /**
   * This method serializes the specified object into its equivalent Json representation.
   * This method should be used when the specified object is not a generic type. This method uses
   * {@link Class#getClass()} to get the type for the specified object, but the
   * {@code getClass()} loses the generic type information because of the Type Erasure feature
   * of Java. Note that this method works fine if the any of the object fields are of generic type,
   * just the object itself should not be of a generic type. If the object is of generic type, use
   * {@link #toJson(Object, Type, Appendable)} instead.
   *
   * @param src the object for which Json representation is to be created setting for Gson
   * @param writer Writer to which the Json representation needs to be written
   * @throws JsonIOException if there was a problem writing to the writer
   * @since 1.2
   */
  public void toJson(Object src, Appendable writer) throws JsonIOException {
    if (src != null) {
      toJson(src, src.getClass(), writer);
    } else {
      toJson(JsonNull.INSTANCE, writer);
    }
  }

  /**
   * This method serializes the specified object, including those of generic types, into its
   * equivalent Json representation. This method must be used if the specified object is a generic
   * type. For non-generic objects, use {@link #toJson(Object, Appendable)} instead.
   *
   * @param src the object for which JSON representation is to be created
   * @param typeOfSrc The specific genericized type of src. You can obtain
   * this type by using the {@link com.google.gson.reflect.TypeToken} class. For example,
   * to get the type for {@code Collection<Foo>}, you should use:
   * <pre>
   * Type typeOfSrc = new TypeToken&lt;Collection&lt;Foo&gt;&gt;(){}.getType();
   * </pre>
   * @param writer Writer to which the Json representation of src needs to be written.
   * @throws JsonIOException if there was a problem writing to the writer
   * @since 1.2
   */
  public void toJson(Object src, Type typeOfSrc, Appendable writer) throws JsonIOException {
    try {
      JsonWriter jsonWriter = newJsonWriter(Streams.writerForAppendable(writer));
      toJson(src, typeOfSrc, jsonWriter);
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
  }

  /**
   * Writes the JSON representation of {@code src} of type {@code typeOfSrc} to
   * {@code writer}.
   * @throws JsonIOException if there was a problem writing to the writer
   */
  @SuppressWarnings("unchecked")
  public void toJson(Object src, Type typeOfSrc, JsonWriter writer) throws JsonIOException {
    TypeAdapter<?> adapter = miniGson.getAdapter(TypeToken.get(typeOfSrc));
    boolean oldLenient = writer.isLenient();
    writer.setLenient(true);
    boolean oldHtmlSafe = writer.isHtmlSafe();
    writer.setHtmlSafe(htmlSafe);
    boolean oldSerializeNulls = writer.getSerializeNulls();
    writer.setSerializeNulls(serializeNulls);
    try {
      ((TypeAdapter<Object>) adapter).write(writer, src);
    } catch (IOException e) {
      throw new JsonIOException(e);
    } finally {
      writer.setLenient(oldLenient);
      writer.setHtmlSafe(oldHtmlSafe);
      writer.setSerializeNulls(oldSerializeNulls);
    }
  }

  /**
   * Converts a tree of {@link JsonElement}s into its equivalent JSON representation.
   *
   * @param jsonElement root of a tree of {@link JsonElement}s
   * @return JSON String representation of the tree
   * @since 1.4
   */
  public String toJson(JsonElement jsonElement) {
    StringWriter writer = new StringWriter();
    toJson(jsonElement, writer);
    return writer.toString();
  }

  /**
   * Writes out the equivalent JSON for a tree of {@link JsonElement}s.
   *
   * @param jsonElement root of a tree of {@link JsonElement}s
   * @param writer Writer to which the Json representation needs to be written
   * @throws JsonIOException if there was a problem writing to the writer
   * @since 1.4
   */
  public void toJson(JsonElement jsonElement, Appendable writer) throws JsonIOException {
    try {
      JsonWriter jsonWriter = newJsonWriter(Streams.writerForAppendable(writer));
      toJson(jsonElement, jsonWriter);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Returns a new JSON writer configured for this GSON and with the non-execute
   * prefix if that is configured.
   */
  private JsonWriter newJsonWriter(Writer writer) throws IOException {
    if (generateNonExecutableJson) {
      writer.write(JSON_NON_EXECUTABLE_PREFIX);
    }
    JsonWriter jsonWriter = new JsonWriter(writer);
    if (prettyPrinting) {
      jsonWriter.setIndent("  ");
    }
    jsonWriter.setSerializeNulls(serializeNulls);
    return jsonWriter;
  }

  /**
   * Writes the JSON for {@code jsonElement} to {@code writer}.
   * @throws JsonIOException if there was a problem writing to the writer
   */
  public void toJson(JsonElement jsonElement, JsonWriter writer) throws JsonIOException {
    boolean oldLenient = writer.isLenient();
    writer.setLenient(true);
    boolean oldHtmlSafe = writer.isHtmlSafe();
    writer.setHtmlSafe(htmlSafe);
    boolean oldSerializeNulls = writer.getSerializeNulls();
    writer.setSerializeNulls(serializeNulls);
    try {
      Streams.write(jsonElement, writer);
    } catch (IOException e) {
      throw new JsonIOException(e);
    } finally {
      writer.setLenient(oldLenient);
      writer.setHtmlSafe(oldHtmlSafe);
      writer.setSerializeNulls(oldSerializeNulls);
    }
  }

  /**
   * This method deserializes the specified Json into an object of the specified class. It is not
   * suitable to use if the specified class is a generic type since it will not have the generic
   * type information because of the Type Erasure feature of Java. Therefore, this method should not
   * be used if the desired type is a generic type. Note that this method works fine if the any of
   * the fields of the specified object are generics, just the object itself should not be a
   * generic type. For the cases when the object is of generic type, invoke
   * {@link #fromJson(String, Type)}. If you have the Json in a {@link Reader} instead of
   * a String, use {@link #fromJson(Reader, Class)} instead.
   *
   * @param <T> the type of the desired object
   * @param json the string from which the object is to be deserialized
   * @param classOfT the class of T
   * @return an object of type T from the string
   * @throws JsonSyntaxException if json is not a valid representation for an object of type
   * classOfT
   */
  public <T> T fromJson(String json, Class<T> classOfT) throws JsonSyntaxException {
    Object object = fromJson(json, (Type) classOfT);
    return Primitives.wrap(classOfT).cast(object);
  }

  /**
   * This method deserializes the specified Json into an object of the specified type. This method
   * is useful if the specified object is a generic type. For non-generic objects, use
   * {@link #fromJson(String, Class)} instead. If you have the Json in a {@link Reader} instead of
   * a String, use {@link #fromJson(Reader, Type)} instead.
   *
   * @param <T> the type of the desired object
   * @param json the string from which the object is to be deserialized
   * @param typeOfT The specific genericized type of src. You can obtain this type by using the
   * {@link com.google.gson.reflect.TypeToken} class. For example, to get the type for
   * {@code Collection<Foo>}, you should use:
   * <pre>
   * Type typeOfT = new TypeToken&lt;Collection&lt;Foo&gt;&gt;(){}.getType();
   * </pre>
   * @return an object of type T from the string
   * @throws JsonParseException if json is not a valid representation for an object of type typeOfT
   * @throws JsonSyntaxException if json is not a valid representation for an object of type
   */
  @SuppressWarnings("unchecked")
  public <T> T fromJson(String json, Type typeOfT) throws JsonSyntaxException {
    if (json == null) {
      return null;
    }
    StringReader reader = new StringReader(json);
    T target = (T) fromJson(reader, typeOfT);
    return target;
  }

  /**
   * This method deserializes the Json read from the specified reader into an object of the
   * specified class. It is not suitable to use if the specified class is a generic type since it
   * will not have the generic type information because of the Type Erasure feature of Java.
   * Therefore, this method should not be used if the desired type is a generic type. Note that
   * this method works fine if the any of the fields of the specified object are generics, just the
   * object itself should not be a generic type. For the cases when the object is of generic type,
   * invoke {@link #fromJson(Reader, Type)}. If you have the Json in a String form instead of a
   * {@link Reader}, use {@link #fromJson(String, Class)} instead.
   *
   * @param <T> the type of the desired object
   * @param json the reader producing the Json from which the object is to be deserialized.
   * @param classOfT the class of T
   * @return an object of type T from the string
   * @throws JsonIOException if there was a problem reading from the Reader
   * @throws JsonSyntaxException if json is not a valid representation for an object of type
   * @since 1.2
   */
  public <T> T fromJson(Reader json, Class<T> classOfT) throws JsonSyntaxException, JsonIOException {
    JsonReader jsonReader = new JsonReader(json);
    Object object = fromJson(jsonReader, classOfT);
    assertFullConsumption(object, jsonReader);
    return Primitives.wrap(classOfT).cast(object);
  }

  /**
   * This method deserializes the Json read from the specified reader into an object of the
   * specified type. This method is useful if the specified object is a generic type. For
   * non-generic objects, use {@link #fromJson(Reader, Class)} instead. If you have the Json in a
   * String form instead of a {@link Reader}, use {@link #fromJson(String, Type)} instead.
   *
   * @param <T> the type of the desired object
   * @param json the reader producing Json from which the object is to be deserialized
   * @param typeOfT The specific genericized type of src. You can obtain this type by using the
   * {@link com.google.gson.reflect.TypeToken} class. For example, to get the type for
   * {@code Collection<Foo>}, you should use:
   * <pre>
   * Type typeOfT = new TypeToken&lt;Collection&lt;Foo&gt;&gt;(){}.getType();
   * </pre>
   * @return an object of type T from the json
   * @throws JsonIOException if there was a problem reading from the Reader
   * @throws JsonSyntaxException if json is not a valid representation for an object of type
   * @since 1.2
   */
  @SuppressWarnings("unchecked")
public <T> T fromJson(Reader json, Type typeOfT) throws JsonIOException, JsonSyntaxException {
    JsonReader jsonReader = new JsonReader(json);
    T object = (T) fromJson(jsonReader, typeOfT);
    assertFullConsumption(object, jsonReader);
    return object;
  }

  private static void assertFullConsumption(Object obj, JsonReader reader) {
    try {
      if (obj != null && reader.peek() != JsonToken.END_DOCUMENT) {
        throw new JsonIOException("JSON document was not fully consumed.");
      }
    } catch (MalformedJsonException e) {
      throw new JsonSyntaxException(e);
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
  }

  /**
   * Reads the next JSON value from {@code reader} and convert it to an object
   * of type {@code typeOfT}.
   * Since Type is not parameterized by T, this method is type unsafe and should be used carefully
   *
   * @throws JsonIOException if there was a problem writing to the Reader
   * @throws JsonSyntaxException if json is not a valid representation for an object of type
   */
  @SuppressWarnings("unchecked")
  public <T> T fromJson(JsonReader reader, Type typeOfT) throws JsonIOException, JsonSyntaxException {
    boolean isEmpty = true;
    boolean oldLenient = reader.isLenient();
    reader.setLenient(true);
    try {
      reader.peek();
      isEmpty = false;
      TypeAdapter<T> typeAdapter = (TypeAdapter<T>) miniGson.getAdapter(TypeToken.get(typeOfT));
      return typeAdapter.read(reader);
    } catch (EOFException e) {
      /*
       * For compatibility with JSON 1.5 and earlier, we return null for empty
       * documents instead of throwing.
       */
      if (isEmpty) {
        return null;
      }
      throw new JsonSyntaxException(e);
    } catch (IllegalStateException e) {
      throw new JsonSyntaxException(e);
    } catch (IOException e) {
     
      throw new JsonSyntaxException(e);
    } finally {
      reader.setLenient(oldLenient);
    }
  }

  /**
   * This method deserializes the Json read from the specified parse tree into an object of the
   * specified type. It is not suitable to use if the specified class is a generic type since it
   * will not have the generic type information because of the Type Erasure feature of Java.
   * Therefore, this method should not be used if the desired type is a generic type. Note that
   * this method works fine if the any of the fields of the specified object are generics, just the
   * object itself should not be a generic type. For the cases when the object is of generic type,
   * invoke {@link #fromJson(JsonElement, Type)}.
   * @param <T> the type of the desired object
   * @param json the root of the parse tree of {@link JsonElement}s from which the object is to
   * be deserialized
   * @param classOfT The class of T
   * @return an object of type T from the json
   * @throws JsonSyntaxException if json is not a valid representation for an object of type typeOfT
   * @since 1.3
   */
  public <T> T fromJson(JsonElement json, Class<T> classOfT) throws JsonSyntaxException {
    Object object = fromJson(json, (Type) classOfT);
    return Primitives.wrap(classOfT).cast(object);
  }

  /**
   * This method deserializes the Json read from the specified parse tree into an object of the
   * specified type. This method is useful if the specified object is a generic type. For
   * non-generic objects, use {@link #fromJson(JsonElement, Class)} instead.
   *
   * @param <T> the type of the desired object
   * @param json the root of the parse tree of {@link JsonElement}s from which the object is to
   * be deserialized
   * @param typeOfT The specific genericized type of src. You can obtain this type by using the
   * {@link com.google.gson.reflect.TypeToken} class. For example, to get the type for
   * {@code Collection<Foo>}, you should use:
   * <pre>
   * Type typeOfT = new TypeToken&lt;Collection&lt;Foo&gt;&gt;(){}.getType();
   * </pre>
   * @return an object of type T from the json
   * @throws JsonSyntaxException if json is not a valid representation for an object of type typeOfT
   * @since 1.3
   */
  @SuppressWarnings("unchecked")
public <T> T fromJson(JsonElement json, Type typeOfT) throws JsonSyntaxException {
    if (json == null) {
      return null;
    }
    return (T) fromJson(new JsonElementReader(json), typeOfT);
  }

  @Override
  public String toString() {
  	StringBuilder sb = new StringBuilder("{")
  	    .append("serializeNulls:").append(serializeNulls)
  	    .append(",serializers:").append(serializers)
  	    .append(",deserializers:").append(deserializers)

      	// using the name instanceCreator instead of ObjectConstructor since the users of Gson are
      	// more familiar with the concept of Instance Creators. Moreover, the objectConstructor is
      	// just a utility class around instance creators, and its toString() only displays them.
        .append(",instanceCreators:").append(constructorConstructor)
        .append("}");
  	return sb.toString();
  }

}
