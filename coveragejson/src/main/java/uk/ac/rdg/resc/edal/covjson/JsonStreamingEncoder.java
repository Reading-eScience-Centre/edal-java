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
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

/**
 * @author Maik Riechert
 *
 */
public class JsonStreamingEncoder implements StreamingEncoder {
    private final JsonGenerator generator;
    private final Map<String, DecimalFormat> field2dp;

    /**
     * 
     * @param os       The {@link OutputStream} to write to
     * @param field2dp a mapping of field to the number of decimal places required.
     */
    public JsonStreamingEncoder(OutputStream os, Map<String, DecimalFormat> field2dp) throws IOException {
        JsonFactory jsonFactory = new JsonFactory();
        generator = jsonFactory.createGenerator(os, JsonEncoding.UTF8);
        generator.useDefaultPrettyPrinter();
        if (field2dp == null) {
            this.field2dp = new HashMap<>();
        } else {
            this.field2dp = field2dp;
        }
    }

    @Override
    public MapEncoder<StreamingEncoder> startMap() throws IOException {
        generator.writeStartObject();
        return new JsonMapEncoder<StreamingEncoder>(this, null);
    }

    @Override
    public void end() throws IOException {
        generator.flush();
    }

    class JsonMapEncoder<T> implements MapEncoder<T> {
        private final T parent;
        private DecimalFormat format;

        public JsonMapEncoder(T parent, DecimalFormat format) {
            this.parent = parent;
            this.format = format;
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
            if (field2dp.containsKey(key)) {
                /*
                 * If this field has a specific format, use it
                 */
                generator.writeFieldName(key);
                generator.writeNumber(field2dp.get(key).format(value));
            } else if(format != null) {
                /*
                 * Otherwise if an ancestor has a format, use that
                 */
                generator.writeFieldName(key);
                generator.writeNumber(format.format(value));
            } else {
                /*
                 * Otherwise use the default format
                 */
                generator.writeNumberField(key, value);
            }
            return this;
        }

        @Override
        public MapEncoder<T> put(String key, double value) throws IOException {
            if (field2dp.containsKey(key)) {
                /*
                 * If this field has a specific format, use it
                 */
                generator.writeFieldName(key);
                generator.writeNumber(field2dp.get(key).format(value));
            } else if(format != null) {
                /*
                 * Otherwise if an ancestor has a format, use that
                 */
                generator.writeFieldName(key);
                generator.writeNumber(format.format(value));
            } else {
                /*
                 * Otherwise use the default format
                 */
                generator.writeNumberField(key, value);
            }
            return this;
        }

        @Override
        public ArrayEncoder<MapEncoder<T>> startArray(String key) throws IOException {
            generator.writeArrayFieldStart(key);
            final DecimalFormat format;
            if(field2dp.containsKey(key)) {
                format = field2dp.get(key);
            } else {
                format = this.format;
            }
            return new JsonArrayEncoder<MapEncoder<T>>(this, format);
        }

        @Override
        public ArrayEncoder<MapEncoder<T>> startArray(String key, ArrayHints hints) throws IOException {
            generator.writeArrayFieldStart(key);
            final DecimalFormat format;
            if(field2dp.containsKey(key)) {
                format = field2dp.get(key);
            } else {
                format = this.format;
            }
            return new JsonArrayEncoder<MapEncoder<T>>(this, format);
        }

        @Override
        public MapEncoder<MapEncoder<T>> startMap(String key) throws IOException {
            generator.writeObjectFieldStart(key);
            final DecimalFormat format;
            if(field2dp.containsKey(key)) {
                format = field2dp.get(key);
            } else {
                format = this.format;
            }
            return new JsonMapEncoder<MapEncoder<T>>(this, format);
        }

        @Override
        public T end() throws IOException {
            generator.writeEndObject();
            return parent;
        }

    }

    class JsonArrayEncoder<T> implements ArrayEncoder<T> {
        private final T parent;
        private DecimalFormat format;

        public JsonArrayEncoder(T parent, DecimalFormat format) {
            this.parent = parent;
            this.format = format;
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
            if (format != null) {
                generator.writeNumber(format.format(value));
            } else {
                generator.writeNumber(value);
            }
            return this;
        }

        @Override
        public ArrayEncoder<T> add(double value) throws IOException {
            if (format != null) {
                generator.writeNumber(format.format(value));
            } else {
                generator.writeNumber(value);
            }
            return this;
        }

        @Override
        public ArrayEncoder<ArrayEncoder<T>> startArray() throws IOException {
            generator.writeStartArray();
            return new JsonArrayEncoder<ArrayEncoder<T>>(this, format);
        }

        @Override
        public ArrayEncoder<ArrayEncoder<T>> startArray(ArrayHints hints) throws IOException {
            return startArray();
        }

        @Override
        public MapEncoder<ArrayEncoder<T>> startMap() throws IOException {
            generator.writeStartObject();
            return new JsonMapEncoder<ArrayEncoder<T>>(this, format);
        }

        @Override
        public T end() throws IOException {
            generator.writeEndArray();
            return parent;
        }

    }

}
