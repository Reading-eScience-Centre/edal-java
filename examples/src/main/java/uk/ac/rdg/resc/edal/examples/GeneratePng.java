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

package uk.ac.rdg.resc.edal.examples;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.FactoryException;

import uk.ac.rdg.resc.edal.dataset.Dataset;
import uk.ac.rdg.resc.edal.dataset.cdm.CdmGridDatasetFactory;
import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.geometry.BoundingBoxImpl;
import uk.ac.rdg.resc.edal.graphics.style.ColourScheme;
import uk.ac.rdg.resc.edal.graphics.style.MapImage;
import uk.ac.rdg.resc.edal.graphics.style.RasterLayer;
import uk.ac.rdg.resc.edal.graphics.style.ScaleRange;
import uk.ac.rdg.resc.edal.graphics.style.SegmentColourScheme;
import uk.ac.rdg.resc.edal.graphics.utils.PlottingDomainParams;
import uk.ac.rdg.resc.edal.graphics.utils.SimpleFeatureCatalogue;
import uk.ac.rdg.resc.edal.util.GISUtils;

/**
 * Example code showing how to use EDAL libraries to read a NetCDF file and
 * write it out to a PNG file in a chosen projection.
 */
public class GeneratePng {

    public static void main(String[] args) throws EdalException, IOException,
            NoSuchAuthorityCodeException, FactoryException {
        /*
         * Get the data file location
         */
        URL resource = ReadPointData.class.getResource("/synthetic_sea_temperature.nc");

        /*
         * Creates a dataset factory for reading NetCDF datasets
         */
        CdmGridDatasetFactory datasetFactory = new CdmGridDatasetFactory();

        /*
         * Use the dataset factory to create a gridded dataset from the NetCDF
         * file, with the ID "dataset"
         */
        Dataset dataset = datasetFactory.createDataset("dataset", resource.getFile());

        /*
         * Choose a variable from the datafile to plot
         */
        String varId = "temperature";

        /*
         * Create a colour scheme to plot the data
         */

        /*
         * The scale range to use in the colour scheme.
         * 
         * This is a scale range of 0-100, non-logarithmic
         */
        ScaleRange scaleRange = new ScaleRange(270f, 310f, false);

        /*
         * A SegmentColourScheme is the standard colour scheme. It divides a
         * palette of into a number of bands, which are linearly mapped to the
         * provided scale range. The arguments in turn are:
         * 
         * - The scale range object
         * 
         * - The colour to plot for values below the minimum (null = extend the
         * range below, i.e. use the same colour as for the minimum allowed
         * value)
         * 
         * - The colour to plot for values above the maximum (null = extend the
         * range above, i.e. use the same colour as for the maximum allowed
         * value)
         * 
         * - The colour to plot for missing data values
         * 
         * - The name of the palette to use.
         * ColourPalette.getAvailablePalettes() can be used to find all
         * available colour palettes. They can also be generated from the
         * uk.ac.rdg.resc.edal.examples.DrawPalettes application.
         * 
         * You can also use a string of the form "#RRGGBB,#RRGGBB,#RRGGBB..."
         * where supplied colours are interpolated between to create a full
         * palette.
         * 
         * - The number of colour bands to use
         * 
         * Other ColourSchemes which can be used:
         * 
         * ThresholdColourScheme - where specific values represent thresholds at
         * which the colours should change
         * 
         * MappedColourScheme - where specific integer values map to specific
         * colours (e.g. in categorical datasets) and all other values take a
         * background colour
         * 
         * RGBColourScheme - where integer values are interpreted as the Java
         * Color.getRGB() colours.
         */
        ColourScheme colourScheme = new SegmentColourScheme(scaleRange, null, null, new Color(0,
                true), "default", 250);

        /*
         * A MapImage is an object which consists of a stack of layers which
         * will be rendered on top of one another. Each layer can depend on one
         * or more data layer names (i.e. variables).
         * 
         * Once the MapImage is fully constructed, the drawImage method can be
         * called. This accepts a set of parameters specifying the
         * (spatiotemporal) region to draw, and a data catalogue which maps data
         * layer names to actual data.
         */
        MapImage imageGenerator = new MapImage();

        /*
         * For this example, we add a single RasterLayer to our MapImage. This
         * RasterLayer will plot the data layer named "vLon" (as stored in
         * varId), using the colourScheme we defined earlier
         */
        RasterLayer rasterLayer = new RasterLayer(varId, colourScheme);
        imageGenerator.getLayers().add(rasterLayer);

        /*
         * The spatiotemporal domain to plot.
         */

        /*-
         * Here, we get the desired CRS and create a bounding box of its limits
         * of validity.
         * 
         * We can instead create the bounding box from a CRS and the desired
         * bounds. Note that the bounding box coordinates need to be in the
         * coordinate system of the supplied CRS.
         * 
         * For example:
         * 
         * Region around the UK in CRS:84:
         * BoundingBox bbox = new BoundingBoxImpl(-12, 48, 5, 60, DefaultGeographicCRS.WGS84);
         * 
         * North pole in north polar stereographic projection:
         * BoundingBox bbox = new BoundingBoxImpl(-6000000, -6000000, 6000000,
         *         6000000, GISUtils.getCrs("EPSG:3408"));
         */
        CoordinateReferenceSystem crs = GISUtils.getCrs("EPSG:3857");
        BoundingBox bbox = new BoundingBoxImpl(crs);

        /*
         * Now we create the domain parameters
         */
        PlottingDomainParams params = new PlottingDomainParams(1024, 1024, bbox, null, null, null,
                null, null);

        /*
         * A general FeatureCatalogue is an object which when supplied with a
         * data layer name and a set of plotting parameters, will return a set
         * of features, and the associated parameter ID within those features.
         * This allows for a lot of flexibility in providing data in EDAL.
         * 
         * However, for the simple case of plotting a variable from a Dataset,
         * the SimpleFeatureCatalogue should be used. This accepts a Dataset
         * object and an option to enable in-memory caching.
         */
        SimpleFeatureCatalogue<Dataset> featureCatalogue = new SimpleFeatureCatalogue<Dataset>(
                dataset, true);

        /*
         * Generate the image.
         */
        BufferedImage image = imageGenerator.drawImage(params, featureCatalogue);

        /*
         * Write the image to disk
         */
        ImageIO.write(image, "png", new File("output.png"));
    }
}
