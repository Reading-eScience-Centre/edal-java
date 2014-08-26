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

import uk.ac.rdg.resc.edal.dataset.AbstractGridDataset;
import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.geometry.BoundingBoxImpl;
import uk.ac.rdg.resc.edal.grid.GridCell2D;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.util.PlottingDomainParams;
import uk.ac.rdg.resc.edal.wms.exceptions.InvalidPointException;

public class GetFeatureInfoParameters extends GetMapParameters {

    private String[] layers;
    private String infoFormat;

    private int featureCount;
    private String exceptionType;
    private HorizontalPosition clickedPos;
    protected boolean continuousDomainPresent;

    /**
     * Parses the parameters needed for a GetFeatureInfo request
     * 
     * @param params
     *            The {@link RequestParams} from the URL request
     * @param catalogue
     *            A {@link WmsCatalogue} - used to ensure that time strings are
     *            parsed correctly
     * @throws EdalException
     */
    public GetFeatureInfoParameters(RequestParams params, WmsCatalogue catalogue)
            throws EdalException {
        super(params, catalogue);
        layers = params.getMandatoryString("query_layers").split(",");
        infoFormat = params.getString("info_format", "text/xml");

        int i = params.getMandatoryPositiveInt("i");
        int j = getPlottingDomainParameters().getHeight() - 1 - params.getMandatoryPositiveInt("j");

        if (i < 0 || i >= getPlottingDomainParameters().getWidth() || j < 0
                || j >= getPlottingDomainParameters().getHeight()) {
            throw new InvalidPointException("Point " + i + ", " + j + " is outside of image (size "
                    + getPlottingDomainParameters().getWidth() + "x"
                    + getPlottingDomainParameters().getHeight() + ")");
        }

        featureCount = params.getPositiveInt("feature_count", 1);
        exceptionType = params.getString("exceptions", "XML");

        GridCell2D clickedGridCell = plottingDomainParams.getImageGrid().getDomainObjects()
                .get(j, i);
        clickedPos = clickedGridCell.getCentre();

        /*
         * We have parsed the URL parameters and created a PlottingDomainParams
         * object in the super-constructor.
         * 
         * However for FeatureInfo, if we have a non-gridded field we want a
         * very small area centred on the clicked point.
         * 
         * We can adjust this using the i, j, and grid parameters and create a
         * new PlottingDomainParams object
         */

        continuousDomainPresent = false;
        for (String layerName : layers) {
            if (!(catalogue.getDatasetFromLayerName(layerName) instanceof AbstractGridDataset)) {
                continuousDomainPresent = true;
                break;
            }
        }

        if (continuousDomainPresent) {
            /*
             * We have a continuous domain so we create a 9 pixel bounding box
             * for target points. Note that if we have a mixed domain this will
             * only find feature info for grid cells whose centre is within 5
             * pixels of the clicked position
             * 
             * Find the positions of a 9 pixel box surrounding the clicked point
             */
            GridCell2D llGridCell = plottingDomainParams.getImageGrid().getDomainObjects()
                    .get(j - 4, i - 4);
            GridCell2D urGridCell = plottingDomainParams.getImageGrid().getDomainObjects()
                    .get(j + 4, i + 4);
            HorizontalPosition llPos = llGridCell.getCentre();
            HorizontalPosition urPos = urGridCell.getCentre();

            /*
             * Create a new bounding box
             */
            BoundingBox newBbox = new BoundingBoxImpl(llPos.getX(), llPos.getY(), urPos.getX(),
                    urPos.getY(), plottingDomainParams.getBbox().getCoordinateReferenceSystem());

            /*
             * Create new PlottingDomainParams which represent a 9-pixel box
             * centred around the clicked point
             */
            plottingDomainParams = new PlottingDomainParams(9, 9, newBbox,
                    plottingDomainParams.getZExtent(), plottingDomainParams.getTExtent(),
                    clickedPos, plottingDomainParams.getTargetZ(),
                    plottingDomainParams.getTargetT());
        } else {
            /*
             * We have a gridded domain for every requested layer, so we want to
             * set the target position to the clicked position.
             */
            plottingDomainParams = new PlottingDomainParams(plottingDomainParams.getWidth(),
                    plottingDomainParams.getHeight(), plottingDomainParams.getBbox(),
                    plottingDomainParams.getZExtent(), plottingDomainParams.getTExtent(),
                    clickedPos, plottingDomainParams.getTargetZ(),
                    plottingDomainParams.getTargetT());
        }
    }

    public String[] getLayerNames() {
        return layers;
    }

    public String getInfoFormat() {
        return infoFormat;
    }

    public HorizontalPosition getClickedPosition() {
        return clickedPos;
    }

    public int getFeatureCount() {
        return featureCount;
    }

    public String getExceptionType() {
        return exceptionType;
    }
}
