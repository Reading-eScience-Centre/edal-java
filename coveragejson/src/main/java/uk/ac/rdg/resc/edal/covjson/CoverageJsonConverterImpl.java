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
import java.util.Collection;

import uk.ac.rdg.resc.edal.covjson.writers.Coverage;
import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.feature.Feature;

/**
 * 
 * @author Maik Riechert
 *
 */
public class CoverageJsonConverterImpl implements CoverageJsonConverter {

	@Override
	public void convertFeatureToJson(OutputStream os, Feature<?> feature) {
		StreamingEncoder encoder;
		try {
			encoder = new JsonStreamingEncoder(os);
			CoverageJsonWriter writer = new CoverageJsonWriter(encoder);
			writer.write(feature);
		} catch (IOException e) {
			throw new EdalException("Error writing CoverageJSON", e);
		}
	}

	@Override
	public void convertFeaturesToJson(OutputStream os, Collection<? extends Feature<?>> features) {
		StreamingEncoder encoder;
		try {
			encoder = new JsonStreamingEncoder(os);
			CoverageJsonWriter writer = new CoverageJsonWriter(encoder);
			writer.write(features);
		} catch (IOException e) {
			throw new EdalException("Error writing CoverageJSON", e);
		}
	}

	@Override
	public void checkFeatureSupported(Feature<?> feature) {
		// wrapping a Feature into a Coverage object will throw exceptions if unsupported
		new Coverage(feature);
	}

	@Override
	public void checkFeaturesSupported(Collection<? extends Feature<?>> features) {
		for (Feature<?> feature : features) {
			new Coverage(feature);
		}
	}

}
