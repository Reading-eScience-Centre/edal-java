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

package uk.ac.rdg.resc.edal.covjson.writers;

import java.io.IOException;

import uk.ac.rdg.resc.edal.covjson.StreamingEncoder.ArrayEncoder;
import uk.ac.rdg.resc.edal.covjson.StreamingEncoder.ArrayHints;
import uk.ac.rdg.resc.edal.covjson.StreamingEncoder.MapEncoder;
import uk.ac.rdg.resc.edal.covjson.writers.Constants.Keys;
import uk.ac.rdg.resc.edal.covjson.writers.Constants.Vals;
import uk.ac.rdg.resc.edal.covjson.writers.Coverage.DataType;
import uk.ac.rdg.resc.edal.covjson.writers.Coverage.NdArray;

/**
 * 
 * @author Maik Riechert
 */
public class RangeWriter <T> {

	private final MapEncoder<T> map;

	public RangeWriter(MapEncoder<T> map) {
		this.map = map;
	}

	public void write(NdArray ndarray) throws IOException {
		map
		  .put(Keys.TYPE, Vals.NDARRAY)
		  .put(Keys.DATATYPE, ndarray.dataType == DataType.Float ? Vals.FLOAT : Vals.INTEGER);
		
		ArrayEncoder<?> axisNames = map.startArray(Keys.AXISNAMES);
		for (String axisName : ndarray.axisNames) {
			axisNames.add(axisName);
		}
		axisNames.end();
		
		ArrayEncoder<?> shape = map.startArray(Keys.SHAPE);
		for (int size : ndarray.shape) {
			shape.add(size);
		}
		shape.end();
		
		ArrayEncoder<?> vals = map.startArray(Keys.VALUES, new ArrayHints((long) ndarray.size, null));
		
		float validMin = Float.MAX_VALUE;
		float validMax = Float.MIN_VALUE;
		
		boolean isInt = ndarray.dataType.equals(DataType.Integer);
		
		for (Number val : ndarray) {
			writeValue(vals, val, isInt);
			if (!isInt && val != null) {
				float fval = val.floatValue();
				if (fval < validMin) {
					validMin = fval;
				}
				if (fval > validMax) {
					validMax = fval;
				}
			}
		}
		
		vals.end();
		
		// validMin/Max is relevant for CBOR only
		// CovJSON does not define actualMin/Max yet
		// see https://github.com/Reading-eScience-Centre/coveragejson/issues/48
//			if (isCategorical) {
//				int min = Collections.min(param.getCategories().keySet());
//				int max = Collections.max(param.getCategories().keySet());
//				rangeMap
//				  .put("validMin", min)
//				  .put("validMax", max);
//			} else {
//				if (validMin != Float.MAX_VALUE) {
//					rangeMap
//					  .put("validMin", validMin)
//					  .put("validMax", validMax);
//				}
//			}
	}
	
	private void writeValue(ArrayEncoder<?> vals, Number val, boolean isInt) throws IOException {
		if (val == null) {
			vals.add(null);
		} else if (isInt) {
			vals.add(val.intValue());
		} else if (val instanceof Float) {
			vals.add((float) val);
		} else {
			vals.add(val.floatValue());
		}
	}
}
