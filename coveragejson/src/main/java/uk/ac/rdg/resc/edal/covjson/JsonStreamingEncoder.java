/*******************************************************************************
 * Copyright (c) 2016 The University of Reading
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the University of Reading, nor the names of the
 *    authors or contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/

package uk.ac.rdg.resc.edal.covjson;

import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

/**
 * @author Maik Riechert
 *
 */
public class JsonStreamingEncoder implements StreamingEncoder {
    private static final DecimalFormat FORMAT = new DecimalFormat("0.0000");

	private final JsonGenerator generator;
	
	public JsonStreamingEncoder(OutputStream os) throws IOException {
		JsonFactory jsonFactory = new JsonFactory();
		generator = jsonFactory.createGenerator(os, JsonEncoding.UTF8);
		generator.useDefaultPrettyPrinter();
	}
	
	@Override
	public MapEncoder<StreamingEncoder> startMap() throws IOException {
		generator.writeStartObject();
		return new JsonMapEncoder<StreamingEncoder>(this);
	}

	@Override
	public void end() throws IOException {
		generator.flush();
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
//			generator.writeNumberField(key, value);
			generator.writeFieldName(key);
			generator.writeNumber(FORMAT.format(value));
			return this;
		}

		@Override
		public MapEncoder<T> put(String key, double value) throws IOException {
//			generator.writeNumberField(key, value);
			generator.writeFieldName(key);
			generator.writeNumber(FORMAT.format(value));
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
//			generator.writeNumber(value);
            generator.writeNumber(FORMAT.format(value));
			return this;
		}

		@Override
		public ArrayEncoder<T> add(double value) throws IOException {
//			generator.writeNumber(value);
			generator.writeNumber(FORMAT.format(value));
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
