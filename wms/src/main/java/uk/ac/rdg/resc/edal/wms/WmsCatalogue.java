package uk.ac.rdg.resc.edal.wms;

import java.io.IOException;

import uk.ac.rdg.resc.edal.dataset.Dataset;
import uk.ac.rdg.resc.edal.dataset.GridDataset;
import uk.ac.rdg.resc.edal.feature.MapFeature;
import uk.ac.rdg.resc.edal.graphics.style.util.FeatureCatalogue;
import uk.ac.rdg.resc.edal.graphics.style.util.GlobalPlottingParams;
import uk.ac.rdg.resc.edal.util.CollectionUtils;
import uk.ac.rdg.resc.edal.wms.exceptions.InvalidCrsException;
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
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        /*
         * TODO process other types of Dataset here (i.e. InSituDataset which
         * doesn't yet exist)
         */

        return null;
    }

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
     * Returns a {@link Dataset} based on a given layer name
     * 
     * @param layerName
     * @return The desired dataset
     */
    public abstract Dataset getDatasetFromId(String layerName);

    /**
     * Returns a variable ID based on a given layer name
     * 
     * @param layerName
     * @return The ID of the variable (within its {@link Dataset})
     */
    public abstract String getVariableFromId(String layerName);
}
