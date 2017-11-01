/*******************************************************************************
 * Copyright (c) 2017 The University of Reading
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

package uk.ac.rdg.resc.edal.graphics.style;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Set;

import org.joda.time.DateTime;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.feature.DiscreteFeature;
import uk.ac.rdg.resc.edal.feature.Feature;
import uk.ac.rdg.resc.edal.feature.TrajectoryFeature;
import uk.ac.rdg.resc.edal.graphics.utils.FeatureCatalogue;
import uk.ac.rdg.resc.edal.graphics.utils.FeatureCatalogue.FeaturesAndMemberName;
import uk.ac.rdg.resc.edal.graphics.utils.PlottingDomainParams;
import uk.ac.rdg.resc.edal.grid.RegularAxis;
import uk.ac.rdg.resc.edal.grid.RegularAxisImpl;
import uk.ac.rdg.resc.edal.grid.RegularGridImpl;
import uk.ac.rdg.resc.edal.position.GeoPosition;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.util.Array1D;
import uk.ac.rdg.resc.edal.util.CollectionUtils;
import uk.ac.rdg.resc.edal.util.Extents;
import uk.ac.rdg.resc.edal.util.GISUtils;
import uk.ac.rdg.resc.edal.util.ImmutableArray1D;

public class ColouredTrajectoryLayer extends ImageLayer {
    private static final Logger log = LoggerFactory.getLogger(ColouredTrajectoryLayer.class);

    private String dataFieldName;
    private ColourScheme colourScheme;

    public ColouredTrajectoryLayer(String dataFieldName, ColourScheme colourScheme) {
        this.dataFieldName = dataFieldName;
        this.colourScheme = colourScheme;
    }

    public String getDataFieldName() {
        return dataFieldName;
    }

    public ColourScheme getColourScheme() {
        return colourScheme;
    }

    public void setColourScheme(ColourScheme colourScheme) {
        this.colourScheme = colourScheme;
    }

    @Override
    protected void drawIntoImage(BufferedImage image, PlottingDomainParams params,
            FeatureCatalogue catalogue) throws EdalException {
        FeaturesAndMemberName featureAndMemberName = catalogue.getFeaturesForLayer(dataFieldName,
                params);
        String member = featureAndMemberName.getMember();

        RegularGridImpl hGrid = new RegularGridImpl(params.getBbox(), params.getWidth(),
                params.getHeight());
        RegularAxisImpl xAxis = (RegularAxisImpl) hGrid.getXAxis();
        RegularAxis yAxis = hGrid.getYAxis();
        Extent<Double> zExtent = params.getZExtent();
        Extent<DateTime> tExtent = params.getTExtent();

        Collection<? extends DiscreteFeature<?, ?>> features = featureAndMemberName.getFeatures();

        double arrowSize = 10.0;
        Graphics2D canvas = image.createGraphics();
        canvas.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        CoordinateReferenceSystem imageCRS = params.getBbox().getCoordinateReferenceSystem();

        for (DiscreteFeature<?, ?> f : features) {
            /*
             * No reason that this should be the case for a well-implemented
             * FeatureCatalogue...
             */
            if (!(f instanceof TrajectoryFeature)) {
                log.warn("Expecting trajectory features for the ColouredTrajectoryLayer.  Got "
                        + f.getClass() + " instead");
                continue;
            }
            TrajectoryFeature feature = (TrajectoryFeature) f;

            /*
             * Now draw the trajectory
             */

            Array1D<GeoPosition> positions = feature.getDomain().getDomainObjects();
            Array1D<Number> values = feature.getValues(member);

            /*
             * Transform all positions in the trajectory domain if required
             */
            if (!GISUtils.crsMatch(imageCRS, feature.getDomain().getHorizontalCrs())) {
                GeoPosition[] transformedPositions = new GeoPosition[(int) positions.size()];
                for (int i = 0; i < positions.size(); i++) {
                    GeoPosition geoPos = positions.get(i);
                    HorizontalPosition hPos = geoPos.getHorizontalPosition();

                    transformedPositions[i] = new GeoPosition(
                            GISUtils.transformPosition(hPos, imageCRS),
                            positions.get(i).getVerticalPosition(), positions.get(i).getTime());
                }
                positions = new ImmutableArray1D<>(transformedPositions);
            }

            HorizontalPosition pos = positions.get(0).getHorizontalPosition();

            int lastPointX = xAxis.findIndexOfUnconstrained(pos.getX());
            int lastPointY = image.getHeight() - yAxis.findIndexOfUnconstrained(pos.getY()) - 1;
            int lastArrowX = lastPointX;
            int lastArrowY = lastPointY;

            canvas.setPaint(colourScheme.getColor(values.get(0)));

            for (int i = 1; i < positions.size(); i++) {
                pos = positions.get(i).getHorizontalPosition();
                HorizontalPosition lastPos = positions.get(i - 1).getHorizontalPosition();

                int currPointX = xAxis.findIndexOfUnconstrained(pos.getX());
                int currPointY = image.getHeight() - yAxis.findIndexOfUnconstrained(pos.getY()) - 1;

                /*
                 * Get image co-ordinates and calculate midpoint of line
                 */
                int midX = (lastPointX + currPointX) / 2;
                int midY = (lastPointY + currPointY) / 2;

                /*-
                 * Don't draw if:
                 * 
                 * Current position is outside the time range
                 * 
                 * Current position is outside the vertical range
                 * 
                 * The line would go from a negative index to a positive one,
                 * whilst the actual position is decreasing, or vice versa.
                 *  
                 * This latter condition can occur with longitude wraps etc.     
                 */
                if (!((tExtent != null && !tExtent.contains(positions.get(i).getTime()))
                        || (zExtent != null
                                && !zExtent.contains(positions.get(i).getVerticalPosition().getZ()))
                        || (Math.signum(currPointX - lastPointX) != Math
                                .signum(pos.getX() - lastPos.getX())))) {
                    /*
                     * Draw a line from the last point to the mid point, in the
                     * colour representing the last point's value
                     */
                    canvas.drawLine(lastPointX, lastPointY, midX, midY);

                    /*
                     * Set the paint to the colour for the current point
                     */
                    canvas.setPaint(colourScheme.getColor(values.get(i)));

                    /*
                     * Draw the line from the midpoint to the end of the line
                     */
                    canvas.drawLine(midX, midY, currPointX, currPointY);

                    /*
                     * Puts arrow heads on the line segments if required
                     */
                    double distFromLastArrow = Math
                            .sqrt((currPointX - lastArrowX) * (currPointX - lastArrowX)
                                    + (currPointY - lastArrowY) * (currPointY - lastArrowY));
                    if (distFromLastArrow > 30) {
                        /*
                         * Use the actual positions (rather than the pixel
                         * positions) to calculate the angle of the arrowhead.
                         * This is more accurate and looks a lot better.
                         */
                        double lineAngle = 2 * Math.PI - Math.atan2((lastPos.getY() - pos.getY()),
                                (pos.getX() - lastPos.getX()));
                        double headAngle1 = lineAngle + 0.3;
                        double headAngle2 = lineAngle - 0.3;
                        double xh1 = arrowSize * Math.cos(headAngle1);
                        double xh2 = arrowSize * Math.cos(headAngle2);
                        double yh1 = arrowSize * Math.sin(headAngle1);
                        double yh2 = arrowSize * Math.sin(headAngle2);
                        canvas.drawLine(-(int) xh1 + currPointX, (int) yh1 + currPointY, currPointX,
                                currPointY);
                        canvas.drawLine(-(int) xh2 + currPointX, (int) yh2 + currPointY, currPointX,
                                currPointY);
                        lastArrowX = currPointX;
                        lastArrowY = currPointY;
                    }
                }
                /*
                 * Store the point coordinates for the next loop
                 */
                lastPointX = currPointX;
                lastPointY = currPointY;
            }

        }
    }

    @Override
    public Collection<Class<? extends Feature<?>>> supportedFeatureTypes() {
        return CollectionUtils.setOf(TrajectoryFeature.class);
    }

    @Override
    public Set<NameAndRange> getFieldsWithScales() {
        return CollectionUtils.setOf(new NameAndRange(dataFieldName,
                Extents.newExtent(colourScheme.getScaleMin(), colourScheme.getScaleMax())));
    }

}
