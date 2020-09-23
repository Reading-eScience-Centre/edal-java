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

import java.io.OutputStream;
import java.util.Collection;

import uk.ac.rdg.resc.edal.feature.Feature;

public interface CoverageJsonConverter {
	/**
	 * Writes a Feature as a CoverageJSON document to the given OutputStream.
	 * 
	 * Note that the OutputStream is *not* closed at the end.
	 * 
	 * @param out The stream to write to.
	 * @param feature The feature to serialize.
	 */
    public void convertFeatureToJson(OutputStream out, Feature<?> feature);
    
    /**
     * Checks whether the converter will be able to convert the given feature successfully.
     * 
     * @param feature The feature to check.
     * @throws EdalException If the feature cannot be converted.
     */
    public void checkFeatureSupported(Feature<?> feature);

	/**
	 * Writes a collection of Features as a CoverageJSON document to the given OutputStream.
	 * 
	 * Note that the OutputStream is *not* closed at the end.
	 * 
	 * @param out The stream to write to.
	 * @param features The features to serialize.
	 */
    public void convertFeaturesToJson(OutputStream out, Collection<? extends Feature<?>> features);
    
    /**
     * Checks whether the converter will be able to convert the given features successfully.
     * 
     * @param features The features to check.
     * @throws EdalException If the features cannot be converted.
     */
    public void checkFeaturesSupported(Collection<? extends Feature<?>> features);
}
