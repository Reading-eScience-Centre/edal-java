/*******************************************************************************
 * Copyright (c) 2013 The University of Reading
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

package uk.ac.rdg.resc.edal.wms;

import uk.ac.rdg.resc.edal.dataset.Dataset;
import uk.ac.rdg.resc.edal.graphics.style.util.FeatureCatalogue;
import uk.ac.rdg.resc.edal.graphics.style.util.LayerNameMapper;
import uk.ac.rdg.resc.edal.graphics.style.util.DatasetCatalogue;
import uk.ac.rdg.resc.edal.graphics.style.util.StyleCatalogue;
import uk.ac.rdg.resc.edal.wms.util.ContactInfo;
import uk.ac.rdg.resc.edal.wms.util.ServerInfo;

/**
 * This class encapsulates the elements needed to implement a specific WMS.
 * This needs to be injected into a {@link WmsServlet} to provide:
 * 
 * <li>Global server settings
 * 
 * <li>Contact information for the server
 * 
 * <li>Retrieval/generation of layer names from dataset/variable IDs
 * 
 * <li>Defining default plotting parameters for WMS layers
 * 
 * <li>Defining available styles for WMS layers
 * 
 * @author Guy Griffiths
 */
public interface WmsCatalogue extends FeatureCatalogue, DatasetCatalogue {
    /**
     * @return The {@link LayerNameMapper} which maps WMS layer names to
     *         {@link Dataset}s and Variables
     */
    public LayerNameMapper getLayerNameMapper();

    /**
     * @return The {@link StyleCatalogue} giving available styles for this WMS
     */
    public StyleCatalogue getStyleCatalogue();

    /**
     * @return The main server metadata for this server
     */
    public abstract ServerInfo getServerInfo();

    /**
     * @return The main contact information for this server
     */
    public abstract ContactInfo getContactInfo();

    /**
     * @return <code>true</code> if this server allows capabilities documents to
     *         be generated for all datasets
     */
    public abstract boolean allowsGlobalCapabilities();

    /**
     * @param datasetId
     *            The ID of the dataset
     * @return The server-configured title of this dataset
     */
    public abstract String getDatasetTitle(String datasetId);

    /**
     * Checks whether a layer is downloadable
     * 
     * @param layerName
     *            The name of the layer
     * 
     * @return Whether or not the given layer can be downloaded in bulk (e.g.
     *         through a timeseries/profile request for CSV/XML/etc)
     */
    public abstract boolean isDownloadable(String layerName);

    /**
     * Checks whether a layer is queryable
     * 
     * @param layerName
     *            The name of the layer
     * @return Whether or not the given layer can be queried with GetFeatureInfo
     *         requests
     */
    public abstract boolean isQueryable(String layerName);

    /**
     * Checks whether a layer is enabled
     * 
     * @param layerName
     *            The name of the layer
     * @return Whether or not the given layer is enabled (i.e. visible in
     *         menu/GetCapabilities)
     */
    public abstract boolean isDisabled(String layerName);

}
