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

import uk.ac.rdg.resc.edal.dataset.Dataset;
import uk.ac.rdg.resc.edal.graphics.exceptions.EdalLayerNotFoundException;

/**
 * Interface defining the mapping of image layer names (i.e. single
 * {@link String} identifiers) to a {@link Dataset} ID and a Variable ID
 *
 * @author Guy Griffiths
 */
public interface LayerNameMapper {
    /**
     * Returns a {@link Dataset} based on a given image layer name
     * 
     * @param layerName
     *            The name of the image layer
     * @return The ID of the desired dataset
     */
    public String getDatasetIdFromLayerName(String layerName) throws EdalLayerNotFoundException;

    /**
     * Returns a variable ID based on a given image layer name
     * 
     * @param layerName
     *            The name of the image layer
     * @return The ID of the variable (within its {@link Dataset})
     * @throws EdalLayerNotFoundException
     *             if the given layer name does not exist within this catalogue
     */
    public String getVariableIdFromLayerName(String layerName) throws EdalLayerNotFoundException;

    /**
     * Returns the image layer name based on the dataset and variable ID
     * 
     * @param datasetId
     *            The ID of dataset containing the layer
     * @param variableId
     *            The ID of the variable within the dataset
     * @return The WMS layer name of this variable
     */
    public String getLayerName(String datasetId, String variableId);
}
