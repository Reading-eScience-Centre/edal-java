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
import java.util.Collection;
import java.util.Map.Entry;

import uk.ac.rdg.resc.edal.covjson.StreamingEncoder.ArrayEncoder;
import uk.ac.rdg.resc.edal.covjson.StreamingEncoder.MapEncoder;
import uk.ac.rdg.resc.edal.covjson.writers.Constants.Keys;
import uk.ac.rdg.resc.edal.covjson.writers.Constants.Vals;
import uk.ac.rdg.resc.edal.metadata.Parameter;
import uk.ac.rdg.resc.edal.metadata.Parameter.Category;

/**
 * 
 * @author Maik Riechert
 */
public class ParametersWriter <T> {

	private final MapEncoder<T> map;

	public ParametersWriter(MapEncoder<T> encoder) {
		this.map = encoder;
	}

	public void write(Collection<Parameter> parameters) throws IOException {
		for (Parameter parameter : parameters) {
			MapEncoder<?> paramMap = map.startMap(parameter.getVariableId());
			write(paramMap, parameter);
			paramMap.end();
		}
	}
	
	private void write(MapEncoder<?> paramMap, Parameter parameter) throws IOException {
		paramMap
		  .put(Keys.TYPE, Vals.PARAMETER);
		
		if (parameter.getDescription() != null) {
			paramMap.startMap(Keys.DESCRIPTION).put(Keys.EN, parameter.getDescription()).end();
		}
		  
		if (parameter.getCategories() != null) {
		  paramMap.startMap(Keys.UNIT).put(Keys.SYMBOL, parameter.getUnits()).end();
		}
		
		String observedPropertyUri = null;
		if (parameter.getStandardName() != null) {
			observedPropertyUri = Vals.getStandardNameUri(parameter.getStandardName());
		}
		MapEncoder<?> obsProp = paramMap.startMap(Keys.OBSERVEDPROPERTY);
		obsProp.startMap(Keys.LABEL).put(Keys.EN, parameter.getTitle()).end();
		if (observedPropertyUri != null) {
			obsProp.put(Keys.ID, observedPropertyUri);
		}
		if (parameter.getCategories() != null) {
			ArrayEncoder<?> cats = obsProp.startArray(Keys.CATEGORIES);
			for (Category category : parameter.getCategories().values()) {
				MapEncoder<?> catMap = cats.startMap()
				  .put(Keys.ID, category.getId())
				  .startMap(Keys.LABEL).put(Keys.EN, category.getLabel()).end();
				if (category.getColour() != null) {
					catMap.put(Keys.PREFERREDCOLOR, category.getColour());
				}
				catMap.end();
			}
			cats.end();
		}
		obsProp.end();
		
		if (parameter.getCategories() != null) {
			MapEncoder<?> catEnc = map.startMap(Keys.CATEGORYENCODING);
			for (Entry<Integer,Category> entry : parameter.getCategories().entrySet()) {
				catEnc.put(entry.getValue().getId(), entry.getKey());
			}
			catEnc.end();
		}
	}

}
