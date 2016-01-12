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

import uk.ac.rdg.resc.edal.covjson.StreamingEncoder.MapEncoder;
import uk.ac.rdg.resc.edal.feature.Feature;

/**
 * 
 * @author Maik Riechert
 *
 */
public class FeatureWriter <T> {

	private final MapEncoder<T> map;
	private final boolean root;

	/**
	 * 
	 * @param encoder
	 * @param root If true, then the feature is the root element in the document
	 * 	that is written.
	 */
	public FeatureWriter(MapEncoder<T> encoder, boolean root) {
		this.map = encoder;
		this.root = root;
	}

	public void write(Feature<?> feature) throws IOException {
		if (root) {
			Util.addJsonLdContext(map);
		}
		map
		  .put("type", "Coverage")
		  .startMap("title").put("en", feature.getName()).end();
		
		MapEncoder<MapEncoder<T>> domain = map.startMap("domain");
		new DomainWriter<>(domain).write(feature);
		domain.end();
		
		MapEncoder<MapEncoder<T>> parameters = map.startMap("parameters");
		new ParametersWriter<>(parameters).write(feature.getParameterMap().values());
		parameters.end();
		
		MapEncoder<MapEncoder<T>> ranges = map.startMap("ranges");
		new RangesWriter<>(ranges).write(feature);
		ranges.end();
	}

}
