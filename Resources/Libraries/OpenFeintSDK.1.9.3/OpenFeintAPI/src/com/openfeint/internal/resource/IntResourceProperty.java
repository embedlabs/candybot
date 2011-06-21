package com.openfeint.internal.resource;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;

public abstract class IntResourceProperty extends PrimitiveResourceProperty {
	public abstract void set(Resource obj, int val);
	public abstract int get(Resource obj);

	@Override public void copy(Resource lhs, Resource rhs) {
		set(lhs, get(rhs));
	}

	@Override
	public void parse(Resource obj, JsonParser jp) throws JsonParseException, IOException {
	    int value;
	    try {
	        value = jp.getIntValue();
	    } catch (Exception e) {
	        value = 0;
	    }
		set(obj, value);
	}

	@Override public void generate(Resource obj, JsonGenerator generator, String key) throws JsonGenerationException, IOException {
        generator.writeFieldName(key);
		generator.writeNumber(get(obj));
	}
}
