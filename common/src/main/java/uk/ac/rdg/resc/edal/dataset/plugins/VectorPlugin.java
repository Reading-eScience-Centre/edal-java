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

package uk.ac.rdg.resc.edal.dataset.plugins;

import org.geotoolkit.geometry.DirectPosition2D;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.Matrix;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.rdg.resc.edal.domain.HorizontalDomain;
import uk.ac.rdg.resc.edal.domain.TemporalDomain;
import uk.ac.rdg.resc.edal.domain.VerticalDomain;
import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.grid.AbstractTransformedGrid;
import uk.ac.rdg.resc.edal.metadata.Parameter;
import uk.ac.rdg.resc.edal.metadata.VariableMetadata;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.util.GISUtils;

/**
 * A plugin to generate magnitude and direction fields from x- and y-components,
 * and to group them logically.
 * 
 * Direction fields are always generated as headings, in degrees, in WGS84 (i.e.
 * lat-lon), regardless of the CRS of the original data.
 * 
 * @author Guy Griffiths
 */
public class VectorPlugin extends VariablePlugin {
    private static final Logger log = LoggerFactory.getLogger(VectorPlugin.class);

    public final static String MAG = "mag";
    public final static String DIR = "dir";
    public final static String GROUP = "group";
    private String title;
    /*
     * Indicates that no matter what the CRS, the components should be treated
     * as eastwards/northwards
     */
    private boolean eastNorthComps;
    /*
     * Used to transform positions when we have a recognised different CRS
     */
    private MathTransform trans = null;
    /*
     * Used to transform positions when we have an AbstractTransformedGrid which
     * reports WGS84 and does transformations behind the scenes
     */
    private AbstractTransformedGrid gridTransform = null;

    /**
     * Construct a new {@link VectorPlugin}
     * 
     * @param xComponentId
     *            The ID of the variable representing the x-component
     * @param yComponentId
     *            The ID of the variable representing the y-component
     * @param title
     *            The title of the quantity which the components represent
     * @param eastNorthComps
     *            <code>true</code> if the components supplied are
     *            eastwards/northwards (which may not necessarily be x/y
     *            components in the native grid system)
     */
    public VectorPlugin(String xComponentId, String yComponentId, String title,
            boolean eastNorthComps) {
        super(new String[] { xComponentId, yComponentId }, new String[] { MAG, DIR, GROUP });
        this.title = title;
        this.eastNorthComps = eastNorthComps;
    }

    @Override
    protected VariableMetadata[] doProcessVariableMetadata(VariableMetadata... metadata)
            throws EdalException {
        /*
         * We get the same components we supply in the constructor, so this is
         * safe.
         */
        VariableMetadata xMetadata = metadata[0];
        VariableMetadata yMetadata = metadata[1];

        /*
         * Get domains where both components are valid
         */
        HorizontalDomain hDomain = getIntersectionOfHorizontalDomains(
                xMetadata.getHorizontalDomain(), yMetadata.getHorizontalDomain());
        VerticalDomain vDomain = getIntersectionOfVerticalDomains(xMetadata.getVerticalDomain(),
                yMetadata.getVerticalDomain());
        TemporalDomain tDomain = getIntersectionOfTemporalDomains(xMetadata.getTemporalDomain(),
                yMetadata.getTemporalDomain());

        /*
         * Generate metadata for new components
         */
        VariableMetadata magMetadata = new VariableMetadata(getFullId(MAG), new Parameter(
                getFullId(MAG), "Magnitude of " + title, "Magnitude of components:\n"
                        + xMetadata.getParameter().getDescription() + " and\n"
                        + yMetadata.getParameter().getDescription(), xMetadata.getParameter()
                        .getUnits()), hDomain, vDomain, tDomain);
        VariableMetadata dirMetadata = new VariableMetadata(getFullId(DIR), new Parameter(
                getFullId(DIR), "Direction of " + title, "Direction of components:\n"
                        + xMetadata.getParameter().getDescription() + " and\n"
                        + yMetadata.getParameter().getDescription(), "degrees"), hDomain, vDomain,
                tDomain);

        /*
         * Find the original parent which the x-component belongs to (and almost
         * certainly the y-component)
         */
        VariableMetadata parentMetadata = xMetadata.getParent();

        /*
         * Create a new container metadata object
         */
        VariableMetadata containerMetadata = new VariableMetadata(getFullId(GROUP), new Parameter(
                getFullId(GROUP), title, "Vector fields for " + title, null), hDomain, vDomain,
                tDomain, false);

        /*
         * Set all components to have a new parent
         */
        magMetadata.setParent(containerMetadata, MAG);
        dirMetadata.setParent(containerMetadata, DIR);
        xMetadata.setParent(containerMetadata, "x");
        yMetadata.setParent(containerMetadata, "y");

        /*
         * Add the container to the original parent
         */
        containerMetadata.setParent(parentMetadata, null);

        /*
         * We have now finished manipulating the metadata. However, to generate
         * direction fields properly, we need to find out the CRS we are in (and
         * whether we have a curvilinear grid). This is the ideal place to do
         * so, since we have access to the variable metadata and this method is
         * guaranteed to only be called once.
         */
        HorizontalDomain xDomain = xMetadata.getHorizontalDomain();
        HorizontalDomain yDomain = yMetadata.getHorizontalDomain();
        if (!GISUtils.crsMatch(xDomain.getCoordinateReferenceSystem(),
                yDomain.getCoordinateReferenceSystem())) {
            throw new EdalException(
                    "Cannot generate vectors from two components with different co-ordinate reference systems");
        }
        if (!eastNorthComps) {
            CoordinateReferenceSystem sourceCrs = xDomain.getCoordinateReferenceSystem();
            try {
                trans = CRS.findMathTransform(sourceCrs, DefaultGeographicCRS.WGS84, true);
            } catch (FactoryException e) {
                throw new EdalException("Cannot calculate transform between 2 CRSs", e);
            }
            if (trans.isIdentity()) {
                /*
                 * The transform is an identity one. This means that we either
                 * have:
                 * 
                 * A ProjectedGrid which is reporting WGS84, but which actually
                 * works in its own native system
                 * 
                 * A CurvlinearGrid which is reporting WGS84, for which there is
                 * no mathematical transform to WGS84 (using e.g. a lookup
                 * table)
                 * 
                 * A WGS84 grid
                 * 
                 * Whichever, we don't need the transform object any more.
                 */
                trans = null;
                if (xDomain instanceof AbstractTransformedGrid) {
                    /*
                     * We have a transformed grid. Save the domain object so
                     * that we can use it to transform positions
                     */
                    eastNorthComps = false;
                    gridTransform = (AbstractTransformedGrid) xDomain;
                } else {
                    /*
                     * We have a standard WGS84 grid. We don't need to do any
                     * transformation - vector directions will drop out nicely
                     */
                    eastNorthComps = true;
                }
            } else {
                /*
                 * We have a mathematical transform which we have now stored.
                 * This will be used to transform vector directions when
                 * required
                 */
                eastNorthComps = false;
            }
        } else {
            /*
             * Unnecessary - just here for clarity.
             * 
             * We have specified that components are east/north, not just x/y.
             * We don't need to do any transformation to calculate vector
             * directions.
             */
            eastNorthComps = true;
            trans = null;
            gridTransform = null;
        }

        /*
         * Return the newly-added VariableMetadata objects, as required
         */
        return new VariableMetadata[] { containerMetadata, magMetadata, dirMetadata };
    }

    @Override
    protected Number generateValue(String varSuffix, HorizontalPosition position,
            Number... sourceValues) {
        if (sourceValues[0] == null || sourceValues[1] == null) {
            return null;
        }
        double xVal = sourceValues[0].doubleValue();
        double yVal = sourceValues[1].doubleValue();
        if (MAG.equals(varSuffix)) {
            return Math.sqrt(xVal * xVal + yVal * yVal);
        } else if (DIR.equals(varSuffix)) {
            if (eastNorthComps) {
                /*
                 * We have components which are lon/lat, so this is simple
                 */
                return Math.atan2(xVal, yVal) * GISUtils.RAD2DEG;
            } else {
                /*
                 * Our source grids are not lon/lat, but we either have a
                 * MathTransform object, or a
                 * ProjectedGrid/AbstractCurvilinearGrid, which can perform its
                 * own transformations
                 * 
                 * Transform the position to WGS84 (if required). This is
                 * necessary since we always want to return the direction as a
                 * heading in WGS84-space
                 */
                position = GISUtils.transformPosition(position, DefaultGeographicCRS.WGS84);
                double lon = position.getX();
                double lat = position.getY();
                if (trans != null) {
                    try {
                        /*
                         * We have a mathematical transform
                         * 
                         * This means that the native grid is non-lat-lon but
                         * contains a Geotoolkit mathematical transform
                         */
                        MathTransform ll2Native = trans.inverse();
                        DirectPosition centre = ll2Native.transform(new DirectPosition2D(lon, lat),
                                null);

                        Matrix derivative = trans.derivative(centre);

                        double newX = xVal * derivative.getElement(0, 0) + yVal
                                * derivative.getElement(0, 1);
                        double newY = xVal * derivative.getElement(1, 0) + yVal
                                * derivative.getElement(1, 1);

                        return GISUtils.RAD2DEG * Math.atan2(newX, newY);
                    } catch (TransformException e) {
                        log.error("Problem generating vector heading for non lat-lon native grid",
                                e);
                        return null;
                    }
                } else if (gridTransform != null) {
                    /*
                     * Our source grid is non-lat-lon, but is defined by a
                     * transformation, held in the AbstractTransformedGrid.
                     */
                    return gridTransform.transformNativeHeadingToWgs84(xVal, yVal, lon, lat);
                } else {
                    /*
                     * Should never get here.
                     */
                    assert false;
                    return null;
                }
            }
        } else {
            /*
             * Should never get here.
             */
            assert false;
            return null;
        }
    }
}
