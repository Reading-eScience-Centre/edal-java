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
import java.util.Collections;

import uk.ac.rdg.resc.edal.covjson.StreamingEncoder.ArrayEncoder;
import uk.ac.rdg.resc.edal.covjson.StreamingEncoder.ArrayHints;
import uk.ac.rdg.resc.edal.covjson.StreamingEncoder.MapEncoder;
import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.feature.DiscreteFeature;
import uk.ac.rdg.resc.edal.feature.Feature;
import uk.ac.rdg.resc.edal.metadata.Parameter;
import uk.ac.rdg.resc.edal.util.Array;
import uk.ac.rdg.resc.edal.util.Array1D;
import uk.ac.rdg.resc.edal.util.Array4D;

/**
 * 
 * @author Maik Riechert
 */
public class RangesWriter <T> {

	private final MapEncoder<T> map;

	public RangesWriter(MapEncoder<T> encoder) {
		this.map = encoder;
	}

	public void write(Feature<?> feature) throws IOException {
		if (!(feature instanceof DiscreteFeature)) {
			throw new EdalException("Only discrete-type features are supported");
		}
		DiscreteFeature<?,?> discreteFeature = (DiscreteFeature<?, ?>) feature;
		map.put("type", "RangeSet");
		for (String paramId : feature.getParameterIds()) {
			MapEncoder<?> rangeMap = map.startMap(paramId);
			write(rangeMap, discreteFeature, paramId);
			rangeMap.end();
		}
	}
	
	private void write(MapEncoder<?> rangeMap, DiscreteFeature<?,?> feature, String paramId) throws IOException {
		Parameter param = feature.getParameter(paramId);
		boolean isCategorical = param.getCategories() != null;
		String dtype = isCategorical ? "integer" : "float";
		
		rangeMap
		  .put("type", "Range")
		  .put("dataType", dtype);
		
		Array<Number> valsArr = feature.getValues(paramId);
		
		// Note: a type hint for the array can only be given if it is a typed array (without null's)
		// This will become relevant with CBOR encoding.
		ArrayEncoder<?> vals = rangeMap.startArray("values", new ArrayHints(valsArr.size(), null));
		
		float validMin = Float.MAX_VALUE;
		float validMax = Float.MIN_VALUE;
		
		// TODO is there a better way to do this? seems brittle as it assumes too much
		if (valsArr instanceof Array1D) {
			for (Number val : valsArr) {
				writeValue(vals, val, isCategorical);
				if (!isCategorical && val != null) {
					float fval = val.floatValue();
					if (fval < validMin) {
						validMin = fval;
					}
					if (fval > validMax) {
						validMax = fval;
					}
				}
			}
		} else if (valsArr instanceof Array4D) {
			Array4D<Number> vals4D = (Array4D<Number>) valsArr;
			int tSize = vals4D.getTSize();
			int zSize = vals4D.getZSize();
			int ySize = vals4D.getYSize();
			int xSize = vals4D.getXSize();
			for (int t=0; t < tSize; t++) {
				for (int z=0; z < zSize; z++) {
					for (int y=0; y < ySize; y++) {
						for (int x=0; x < xSize; x++) {
							Number val = vals4D.get(t, z, y, x);
							writeValue(vals, val, isCategorical);
							if (!isCategorical && val != null) {
								float fval = val.floatValue();
								if (fval < validMin) {
									validMin = fval;
								}
								if (fval > validMax) {
									validMax = fval;
								}
							}
						}
					}
				}
			}
		} else {
			throw new EdalException("Unsupported array class: " + valsArr.getClass().getSimpleName());
		}
		vals.end();
		
		if (isCategorical) {
			int min = Collections.min(param.getCategories().keySet());
			int max = Collections.max(param.getCategories().keySet());
			rangeMap
			  .put("validMin", min)
			  .put("validMax", max);
		} else {
			if (validMin != Float.MAX_VALUE) {
				rangeMap
				  .put("validMin", validMin)
				  .put("validMax", validMax);
			}
		}
	}
	
	private void writeValue(ArrayEncoder<?> vals, Number val, boolean isCategorical) throws IOException {
		if (val == null) {
			vals.add(null);
		} else if (isCategorical) {
			vals.add(val.intValue());
		} else if (val instanceof Float) {
			vals.add((float) val);
		} else {
			vals.add(val.floatValue());
		}
	}
}
