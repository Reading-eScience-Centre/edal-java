/*******************************************************************************
 * Copyright (c) 2015 The University of Reading
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

package uk.ac.rdg.resc.edal.graphics.utils;

import java.util.Collection;

import org.joda.time.DateTime;

import uk.ac.rdg.resc.edal.dataset.Dataset;
import uk.ac.rdg.resc.edal.graphics.exceptions.EdalLayerNotFoundException;
import uk.ac.rdg.resc.edal.metadata.VariableMetadata;

/**
 * A catalogue of {@link Dataset}s and the default plotting information for the
 * Variables contained within them
 *
 * @author Guy Griffiths
 */
public interface DatasetCatalogue {
    /**
     * @return All available {@link Dataset}s in this {@link DatasetCatalogue}. Will
     *         return <code>null</code> in cases where this information is not
     *         available (e.g. all {@link Dataset}s are dynamically generated)
     */
    public abstract Collection<Dataset> getAllDatasets();

    /**
     * Returns a {@link Dataset} from its ID
     * 
     * @param datasetId The ID of the dataset
     * @return The desired dataset, or <code>null</code> if it doesn't exist in the
     *         catalogue
     */
    public abstract Dataset getDatasetFromId(String datasetId);

    /**
     * Returns {@link EnhancedVariableMetadata} giving for a given layer. This gives
     * default values for scale range, palette etc., as well as metadata such as
     * title, description, etc.
     * 
     * @param variableMetadata The {@link VariableMetadata} of the desired layer
     * @return Default metadata values for the desired layer
     */
    public EnhancedVariableMetadata getLayerMetadata(VariableMetadata variableMetadata)
            throws EdalLayerNotFoundException;

    /**
     * @return The {@link DateTime} at which this {@link DatasetCatalogue} was last
     *         updated
     */
    public DateTime getLastUpdateTime();
}
