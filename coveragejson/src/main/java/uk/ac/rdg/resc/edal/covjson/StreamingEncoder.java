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

/**
 * A streaming encoder interface for JSON-compatible object structures
 * with additional hints for more advanced formats like CBOR encoders.
 * 
 * This interface is a mix of cbor-java's CborBuilder and Jackson's Streaming API,
 * with the goal of being compatible to both and being able to write
 * simple adapters for them.
 *  
 * @author Maik Riechert
 */
public interface StreamingEncoder {
	public class ArrayHints {
		private final Long size;
		private final Class<? extends Number> type;
		/**
		 * 
		 * @param size can be null
		 * @param type If not null, then all array elements must be of the given type and cannot be null.
		 */
		public ArrayHints(Long size, Class<? extends Number> type) {
			this.size = size;
			this.type = type;
		}
		boolean hasSize() {
			return size != null;
		}
		long getSize() {
			return size;
		}
		boolean hasType() {
			return type != null;
		}
		Class<? extends Number> getType() {
			return type;
		}
	}
		
	public interface ArrayEncoder <T> {
		ArrayEncoder<T> add(String value) throws IOException;
		ArrayEncoder<T> add(boolean value) throws IOException;
		ArrayEncoder<T> add(int value) throws IOException;
		ArrayEncoder<T> add(long value) throws IOException;
		ArrayEncoder<T> add(float value) throws IOException;
		ArrayEncoder<T> add(double value) throws IOException;
		ArrayEncoder<ArrayEncoder<T>> startArray() throws IOException;
		ArrayEncoder<ArrayEncoder<T>> startArray(ArrayHints hints) throws IOException;
		MapEncoder<ArrayEncoder<T>> startMap() throws IOException;
		T end() throws IOException;
	}
	
	public interface MapEncoder <T> {
		MapEncoder<T> put(String key, String value) throws IOException;
		MapEncoder<T> put(String key, boolean value) throws IOException;
		MapEncoder<T> put(String key, int value) throws IOException;
		MapEncoder<T> put(String key, long value) throws IOException;
		MapEncoder<T> put(String key, float value) throws IOException;
		MapEncoder<T> put(String key, double value) throws IOException;
		ArrayEncoder<MapEncoder<T>> startArray(String key) throws IOException;
		ArrayEncoder<MapEncoder<T>> startArray(String key, ArrayHints hints) throws IOException;
		MapEncoder<MapEncoder<T>> startMap(String key) throws IOException;
		T end() throws IOException;
	}
	
	MapEncoder<StreamingEncoder> startMap() throws IOException;
	
	void end() throws IOException;
	
	// at the root level we only support Maps for now.	
}
