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

import java.util.List;

import uk.ac.rdg.resc.edal.dataset.Dataset;
import uk.ac.rdg.resc.edal.dataset.GridDataset;
import uk.ac.rdg.resc.edal.exceptions.DataReadingException;
import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.exceptions.InvalidCrsException;
import uk.ac.rdg.resc.edal.feature.MapFeature;
import uk.ac.rdg.resc.edal.graphics.style.util.FeatureCatalogue;
import uk.ac.rdg.resc.edal.graphics.style.util.GlobalPlottingParams;
import uk.ac.rdg.resc.edal.metadata.VariableMetadata;
import uk.ac.rdg.resc.edal.util.CollectionUtils;
import uk.ac.rdg.resc.edal.wms.util.WmsUtils;

/**
 * This class encapsulates the elements needed to implement a specific WMS.
 * Subclasses should extend this to implement their own configuration system.
 * 
 * This includes things like:
 * 
 * Global server settings
 * 
 * Overriding default WMS layer values (scale range, palette etc)
 * 
 * Whatever else I come across whilst coding WmsServlet
 * 
 * TODO This Javadoc is a bit crap...
 * 
 * TODO Make WmsCatalogue an interface, and rename this AbstractWmsCatalogue?
 * TODO Actually, perhaps this should be a combination of several different
 * interfaces?
 * 
 * @author Guy
 */
public abstract class WmsCatalogue implements FeatureCatalogue {

    @Override
    public MapFeatureAndMember getFeatureAndMemberName(String id, GlobalPlottingParams params) {
        Dataset dataset = getDatasetFromId(id);
        String variable = getVariableFromId(id);
        if (dataset instanceof GridDataset) {
            GridDataset gridDataset = (GridDataset) dataset;
            try {
                MapFeature mapData = gridDataset.readMapData(CollectionUtils.setOf(variable),
                        WmsUtils.getImageGrid(params), params.getTargetZ(), params.getTargetT());
                return new MapFeatureAndMember(mapData, variable);
            } catch (InvalidCrsException e) {
                /*
                 * TODO Make this method throw an appropriate exception
                 */
                e.printStackTrace();
                return null;
            } catch (DataReadingException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            throw new UnsupportedOperationException("Currently only gridded data is supported");
        }
        /*
         * TODO process other types of Dataset here (i.e. InSituDataset which
         * doesn't yet exist)
         */
    }

    public VariableMetadata getVariableMetadataFromId(String layerName) throws EdalException {
        Dataset dataset = getDatasetFromId(layerName);
        String variableFromId = getVariableFromId(layerName);
        if (dataset != null && variableFromId != null) {
            return dataset.getVariableMetadata(variableFromId);
        } else {
            throw new EdalException("The layer name " + layerName + " doesn't map to a variable");
        }
    }

    /*
     * TODO These things are global server settings. Perhaps we should have a
     * getServerSettings() method which holds these properties?
     */

    /**
     * @return The maximum number of layers which can be requested in the same
     *         image.
     */
    public abstract int getMaxSimultaneousLayers();

    /**
     * @return The maximum image width this server supports
     */
    public abstract int getMaxImageWidth();

    /**
     * @return The maximum image height this server supports
     */
    public abstract int getMaxImageHeight();

    /**
     * @return The name of this server
     */
    public abstract String getServerName();
    
    /**
     * @return Short descriptive text about this server
     */
    public abstract String getServerAbstract();
    
    /**
     * @return A list of keywords which apply to this server
     */
    public abstract List<String> getServerKeywords();
    
    /**
     * @return The main contact name for this server
     */
    public abstract String getServerContactName();
    
    /**
     * @return The main contact organisation for this server
     */
    public abstract String getServerContactOrganisation();
    
    /**
     * @return The main contact telephone number for this server
     */
    public abstract String getServerContactTelephone();
    
    /**
     * @return The main contact email address for this server
     */
    public abstract String getServerContactEmail();

    /**
     * @return All available {@link Dataset}s on this server
     */
    public abstract List<Dataset> getAllDatasets();
    
    /**
     * @param datasetId
     *            The ID of the dataset
     * @return The server-configured title of this dataset
     */
    public abstract String getDatasetTitle(String datasetId);

    /*
     * End of global server settings
     */

    /**
     * Returns a {@link Dataset} based on a given layer name
     * 
     * @param layerName
     *            The full layer name
     * @return The desired dataset
     */
    public abstract Dataset getDatasetFromId(String layerName);

    /**
     * Returns a variable ID based on a given layer name
     * 
     * @param layerName
     *            The full layer name
     * @return The ID of the variable (within its {@link Dataset})
     */
    public abstract String getVariableFromId(String layerName);

    /**
     * Returns the layer name based on the dataset and variable ID
     * 
     * @param dataset
     *            The ID of dataset containing the layer
     * @param variableId
     *            The ID of the variable within the dataset
     * @return The WMS layer name of this variable
     */
    public abstract String getLayerName(String datasetId, String variableId);

    /**
     * Returns server-configured metadata for a given layer
     * 
     * @param layerName
     *            The full layer name
     * @return Default metadata values for the desired layer
     */
    public abstract WmsLayerMetadata getLayerMetadata(String layerName);
}
