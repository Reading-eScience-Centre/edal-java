/*******************************************************************************
 * Copyright (c) 2011 The University of Reading
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
 *******************************************************************************/

package uk.ac.rdg.resc.edal.cdm.coverage.grid;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DirectColorModel;
import java.awt.image.WritableRaster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.rdg.resc.edal.cdm.coverage.grid.CurvilinearCoords.Cell;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;

/**
 * An object that provides an approximate means for mapping from
 * longitude-latitude coordinates to i and j index coordinates in a curvilinear
 * grid.
 * 
 * @todo Some duplication of {@link HorizontalGrid}? There's a difference in how
 *       the "tick marks" along the axes are set up: see how the
 *       Regular1DCoordAxes are created.
 * @author Jon Blower
 */
final class LookUpTable {
    private static final Logger logger = LoggerFactory.getLogger(LookUpTable.class);

    /*
     * The contents of the look-up table: i.e. the i and j indices of each
     * lon-lat point in the LUT. These are flattened from a 2D to a 1D array. We
     * store these as shorts to save disk space. The LUT would need to be
     * extremely large before we would have to worry about overflows. Each array
     * has the size nLon * nLat
     */
    private DataBuffer iIndices;
    private DataBuffer jIndices;

    private final int nLon;
    private final int nLat;

    // Converts from lat-lon coordinates to index space in the LUT.
    private final AffineTransform transform = new AffineTransform();

    /**
     * A {@link DirectColorModel} that holds data as unsigned shorts, ignoring
     * the red and alpha components. (int value = green &lt;&lt; 8 | blue)
     */
    /*
     * Store the data as unsigned shorts
     */
    private static final ColorModel COLOR_MODEL = new DirectColorModel(16, 0x00000000, 0x0000ff00,
            0x000000ff, 0x00000000);

    /** This value in the look-up table means "missing value" */
    private static final int MISSING_VALUE = 65535;

    /** This is the maximum index that can be stored in the LUT */
    private static final int MAX_INDEX = 65534;

    /**
     * Creates an empty look-up table (with all indices set to -1).
     * 
     * @param curvGrid
     *            The CurvilinearGrid which this LUT will approximate
     * @param minResolution
     *            The minimum resolution of the LUT in degrees
     */
    public LookUpTable(CurvilinearCoords curvGrid, double minResolution) {
        BoundingBox bbox = curvGrid.getBoundingBox();

        double lonDiff = bbox.getMaxX() - bbox.getMinX();
        double latDiff = bbox.getMaxY() - bbox.getMinY();

        // Now calculate the number of points in the LUT along the longitude
        // and latitude directions
        this.nLon = (int) Math.ceil(lonDiff / minResolution);
        this.nLat = (int) Math.ceil(latDiff / minResolution);
        if (this.nLon <= 0 || this.nLat <= 0) {
            String msg = String.format("nLon (=%d) and nLat (=%d) must be positive and > 0",
                    this.nLon, this.nLat);
            throw new IllegalStateException(msg);
        }

        // This ensures that the highest value of longitude (corresponding
        // with nLon - 1) is getLonMax()
        double lonStride = lonDiff / (this.nLon - 1);
        double latStride = latDiff / (this.nLat - 1);

        // Create the transform. We scale by the inverse of the stride length
        this.transform.scale(1.0 / lonStride, 1.0 / latStride);
        // Then we translate by the minimum coordinate values
        this.transform.translate(-bbox.getMinX(), -bbox.getMinY());

        // Populate the look-up tables
        this.makeLuts(curvGrid);
    }

    /**
     * Generates the data for the look-up tables
     */
    private void makeLuts(CurvilinearCoords curvGrid) {
        // Create BufferedImages on which we will paint the i and j indices of
        // cells
        BufferedImage iIm = this.createBufferedImage();
        BufferedImage jIm = this.createBufferedImage();

        // Get graphics contexts onto which we'll paint
        Graphics2D ig2d = iIm.createGraphics();
        Graphics2D jg2d = jIm.createGraphics();

        // Apply a transform so that we can paint in lat-lon coordinates
        ig2d.setTransform(this.transform);
        jg2d.setTransform(this.transform);

        // Populate the BufferedImages using the information from the
        // curvilinear grid
        // Iterate over all the cells in the grid, painting the i and j indices
        // of the
        // cell onto the BufferedImage
        for (Cell cell : curvGrid.getCells()) {
            // Get a Path representing the boundary of the cell
            Path2D path = cell.getBoundaryPath();
            // Paint the path onto the BufferedImages as polygons
            // Use the i and j indices of the cell as the colours
            if (cell.getI() > MAX_INDEX || cell.getJ() > MAX_INDEX) {
                // Very unlikely to happen!
                throw new IllegalStateException("Can't store indices greater than " + MAX_INDEX);
            }
            ig2d.setPaint(new Color(cell.getI()));
            jg2d.setPaint(new Color(cell.getJ()));
            ig2d.fill(path);
            jg2d.fill(path);

            // We paint a second copy of the cell, shifted by 360 degrees, to
            // handle
            // the anti-meridian
            double shiftLon = cell.getCentre().getLongitude() > 0.0 ? -360.0 : 360.0;
            path.transform(AffineTransform.getTranslateInstance(shiftLon, 0.0));
            ig2d.fill(path);
            jg2d.fill(path);
        }

        // We only need to store the data buffers, not the whole BufferedImages
        this.iIndices = iIm.getRaster().getDataBuffer();
        this.jIndices = jIm.getRaster().getDataBuffer();
    }

    /**
     * Creates and returns a new {@link BufferedImage} that stores pixel data as
     * unsigned shorts. Initializes all pixel values to {@link #MISSING_VALUE}.
     * 
     * @return
     */
    private BufferedImage createBufferedImage() {
        WritableRaster raster = COLOR_MODEL.createCompatibleWritableRaster(this.nLon, this.nLat);
        BufferedImage im = new BufferedImage(COLOR_MODEL, raster, true, null);
        for (int y = 0; y < im.getHeight(); y++) {
            for (int x = 0; x < im.getWidth(); x++) {
                im.setRGB(x, y, MISSING_VALUE);
            }
        }
        logger.debug("Created BufferedImage of size {},{}, data buffer type {}",
                new Object[] { im.getWidth(), im.getHeight(),
                        im.getRaster().getDataBuffer().getClass() });
        return im;
    }

    /**
     * Returns the nearest coordinates in the original CurvilinearGrid to the
     * given longitude-latitude point, or null if the given longitude-latitude
     * point is not in the domain of this look-up table.
     * 
     * @param longitude
     *            The longitude of the point of interest
     * @param latitude
     *            The latitude of the point of interest
     * @return A newly-created integer array with two values: the first value is
     *         the i coordinate in the grid, the second is the j coordinate.
     *         Returns null if the given longitude-latitude point is not in the
     *         domain of this LUT.
     */
    public int[] getGridCoordinates(double longitude, double latitude) {
        // Convert from longitude-latitude to index space in this LUT
        Point2D indexPoint = this.transform
                .transform(new Point2D.Double(longitude, latitude), null);
        int iLon = (int) Math.round(indexPoint.getX());
        int iLat = (int) Math.round(indexPoint.getY());

        if (iLon < 0 || iLat < 0 || iLon >= this.nLon || iLat >= this.nLat) {
            return null;
        }

        // Find the index within the LUT
        int index = iLon + (iLat * this.nLon);
        // Extract the i and j indices of the nearest grid point
        int iIndex = this.iIndices.getElem(index);
        int jIndex = this.jIndices.getElem(index);

        // Check for missing values
        if (iIndex == MISSING_VALUE || jIndex == MISSING_VALUE) {
            return null;
        }
        return new int[] { iIndex, jIndex };
    }

    /**
     * Gets the number of points in this look-up table along its longitude axis
     */
    public int getNumLonPoints() {
        return this.nLon;
    }

    /**
     * Gets the number of points in this look-up table along its latitude axis
     */
    public int getNumLatPoints() {
        return this.nLat;
    }

}
