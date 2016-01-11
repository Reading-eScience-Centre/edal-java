package uk.ac.rdg.resc.edal.covjson;

import java.io.IOException;
import java.io.OutputStream;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

public class JsonStreamingEncoder implements StreamingEncoder {

	private final OutputStream stream;
	private final JsonGenerator generator;
	
	public JsonStreamingEncoder(OutputStream os) throws IOException {
		stream = os;
		JsonFactory jsonFactory = new JsonFactory();
		generator = jsonFactory.createGenerator(os, JsonEncoding.UTF8);
	}
	
	@Override
	public MapEncoder<StreamingEncoder> startMap() throws IOException {
		generator.writeStartObject();
		return new JsonMapEncoder<StreamingEncoder>(this);
	}

	@Override
	public void end() throws IOException {
		// TODO are we supposed to close the stream or not? maybe this should be optional
		stream.close();
	}

	class JsonMapEncoder <T> implements MapEncoder<T> {
		private final T parent;
		public JsonMapEncoder(T parent) {
			this.parent = parent;
		}

		@Override
		public MapEncoder<T> put(String key, String value) throws IOException {
			if (value == null) {
				generator.writeNullField(key);
			} else {
				generator.writeStringField(key, value);
			}
			return this;
		}

		@Override
		public MapEncoder<T> put(String key, boolean value) throws IOException {
			generator.writeBooleanField(key, value);
			return this;
		}

		@Override
		public MapEncoder<T> put(String key, int value) throws IOException {
			generator.writeNumberField(key, value);
			return this;
		}

		@Override
		public MapEncoder<T> put(String key, long value) throws IOException {
			generator.writeNumberField(key, value);
			return this;
		}

		@Override
		public MapEncoder<T> put(String key, float value) throws IOException {
			generator.writeNumberField(key, value);
			return this;
		}

		@Override
		public MapEncoder<T> put(String key, double value) throws IOException {
			generator.writeNumberField(key, value);
			return this;
		}

		@Override
		public ArrayEncoder<MapEncoder<T>> startArray(String key) throws IOException {
			generator.writeArrayFieldStart(key);
			return new JsonArrayEncoder<MapEncoder<T>>(this);
		}

		@Override
		public ArrayEncoder<MapEncoder<T>> startArray(String key, ArrayHints hints) throws IOException {
			generator.writeArrayFieldStart(key);
			return new JsonArrayEncoder<MapEncoder<T>>(this);
		}

		@Override
		public MapEncoder<MapEncoder<T>> startMap(String key) throws IOException {
			generator.writeObjectFieldStart(key);
			return new JsonMapEncoder<MapEncoder<T>>(this);
		}

		@Override
		public T end() throws IOException {
			generator.writeEndObject();
			return parent;
		}
		
	}
	
	class JsonArrayEncoder<T> implements ArrayEncoder<T> {
		private final T parent;
		
		public JsonArrayEncoder(T parent) {
			this.parent = parent;
		}
		
		@Override
		public ArrayEncoder<T> add(String value) throws IOException {
			if (value == null) {
				generator.writeNull();
			} else {
				generator.writeString(value);
			}
			return this;
		}

		@Override
		public ArrayEncoder<T> add(boolean value) throws IOException {
			generator.writeBoolean(value);
			return this;
		}

		@Override
		public ArrayEncoder<T> add(int value) throws IOException {
			generator.writeNumber(value);
			return this;
		}

		@Override
		public ArrayEncoder<T> add(long value) throws IOException {
			generator.writeNumber(value);
			return this;
		}

		@Override
		public ArrayEncoder<T> add(float value) throws IOException {
			generator.writeNumber(value);
			return this;
		}

		@Override
		public ArrayEncoder<T> add(double value) throws IOException {
			generator.writeNumber(value);
			return this;
		}

		@Override
		public ArrayEncoder<ArrayEncoder<T>> startArray() throws IOException {
			generator.writeStartArray();
			return new JsonArrayEncoder<ArrayEncoder<T>>(this);
		}

		@Override
		public ArrayEncoder<ArrayEncoder<T>> startArray(ArrayHints hints) throws IOException {
			return startArray();
		}

		@Override
		public MapEncoder<ArrayEncoder<T>> startMap() throws IOException {
			generator.writeStartObject();
			return new JsonMapEncoder<ArrayEncoder<T>>(this);
		}

		@Override
		public T end() throws IOException {
			generator.writeEndArray();
			return parent;
		}
		
	}
	
}
