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
import java.util.HashMap;
import java.util.Map;

import uk.ac.rdg.resc.edal.covjson.StreamingEncoder.ArrayEncoder;
import uk.ac.rdg.resc.edal.covjson.StreamingEncoder.MapEncoder;
import uk.ac.rdg.resc.edal.covjson.writers.Constants.Keys;
import uk.ac.rdg.resc.edal.covjson.writers.Constants.Vals;
import uk.ac.rdg.resc.edal.metadata.Parameter;

/**
 * 
 * @author Maik Riechert
 */
public class CoverageCollectionWriter <T> {

	private final MapEncoder<T> map;

	public CoverageCollectionWriter(MapEncoder<T> encoder) {
		this.map = encoder;
	}

	public void write(Collection<Coverage> coverages) throws IOException {
		Util.addJsonLdContext(map);
		map.put(Keys.TYPE, Vals.COVERAGECOLLECTION);
		
		if (!coverages.isEmpty()) {
			Coverage firstCoverage = coverages.iterator().next();
			String domainType = firstCoverage.domain.domainType;
			boolean sameDomainType = true;
			for (Coverage coverage : coverages) {
				String otherDomainType = coverage.domain.domainType;
				if (!otherDomainType.equals(domainType)) {
					sameDomainType = false;
					break;
				}
			}
			if (sameDomainType) {
				map.put(Keys.DOMAINTYPE, domainType);
			}
		}
		
		// IMPORTANT: this assumes that different parameters have different IDs
		Map<String, Parameter> parameters = new HashMap<>();
		
		ArrayEncoder<?> covs = map.startArray(Keys.COVERAGES);
		for (Coverage coverage : coverages) {
			MapEncoder<?> cov = covs.startMap();
			new CoverageWriter<>(cov, false).write(coverage, true);
			cov.end();
			
			// TODO this may be slightly inefficient for big numbers of uniform coverages
			//   -> with Collection<Feature<?>> there is no easy way to avoid it
			
			for (Parameter param : coverage.parameters.values()) {
				if (!parameters.containsKey(param.getVariableId())) {
					parameters.put(param.getVariableId(), param);
				}
			}
		}
		covs.end();
		
		MapEncoder<?> params = map.startMap(Keys.PARAMETERS);
		new ParametersWriter<>(params).write(parameters.values());
		params.end();
	}

}
