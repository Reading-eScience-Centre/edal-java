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

package uk.ac.rdg.resc.edal.graphics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.junit.Before;
import org.junit.Test;

import uk.ac.rdg.resc.edal.domain.MapDomain;
import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.feature.MapFeature;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.geometry.BoundingBoxImpl;
import uk.ac.rdg.resc.edal.graphics.style.ArrowLayer;
import uk.ac.rdg.resc.edal.graphics.style.ArrowLayer.ArrowStyle;
import uk.ac.rdg.resc.edal.graphics.style.ColourScheme;
import uk.ac.rdg.resc.edal.graphics.style.ColourScheme2D;
import uk.ac.rdg.resc.edal.graphics.style.ContourLayer;
import uk.ac.rdg.resc.edal.graphics.style.ContourLayer.ContourLineStyle;
import uk.ac.rdg.resc.edal.graphics.style.DensityMap;
import uk.ac.rdg.resc.edal.graphics.style.MapImage;
import uk.ac.rdg.resc.edal.graphics.style.Raster2DLayer;
import uk.ac.rdg.resc.edal.graphics.style.RasterLayer;
import uk.ac.rdg.resc.edal.graphics.style.ScaleRange;
import uk.ac.rdg.resc.edal.graphics.style.SegmentColourScheme;
import uk.ac.rdg.resc.edal.graphics.style.SegmentDensityMap;
import uk.ac.rdg.resc.edal.graphics.style.StippleLayer;
import uk.ac.rdg.resc.edal.graphics.style.ThresholdColourScheme2D;
import uk.ac.rdg.resc.edal.graphics.style.sld.SLDException;
import uk.ac.rdg.resc.edal.graphics.style.sld.SLDRange;
import uk.ac.rdg.resc.edal.graphics.style.sld.SLDRange.Spacing;
import uk.ac.rdg.resc.edal.graphics.style.util.FeatureCatalogue;
import uk.ac.rdg.resc.edal.graphics.utils.PlottingDomainParams;
import uk.ac.rdg.resc.edal.grid.RegularGrid;
import uk.ac.rdg.resc.edal.grid.RegularGridImpl;
import uk.ac.rdg.resc.edal.util.Array2D;

/**
 * These tests plot a predictable (in-memory) dataset with missing values, and
 * out-of-range data. The expected outputs (found in src/test/resources) have
 * been examined and are correct for the dataset.
 * 
 * The tests here generate the images and do a pixel-by-pixel comparison with
 * the expected images. If any of these tests fail, it means that the plotting
 * output has changed since a careful examination showed the test images to be
 * correct.
 * 
 * This would be expected if a plot style has changed at all, in which case the
 * new image can be manually examined substituted for the test example if
 * correct.
 * 
 * @author Guy
 */
public class PlotsTest {

    private static final int WIDTH = 500;
    private static final int HEIGHT = 250;
    /* The feature to test against */
    private MapFeature testFeature;
    /* Dummy variables needed for plotting */
    private FeatureCatalogue catalogue;
    private PlottingDomainParams params;
    /* ColourScale which can be used by any plotting layer which needs it */
    private ScaleRange scale = new ScaleRange(0f, 1f, false);

    @Before
    public void setup() {
        BoundingBox bbox = new BoundingBoxImpl(-180, -90, 180, 90, DefaultGeographicCRS.WGS84);

        RegularGrid hGrid = new RegularGridImpl(bbox, WIDTH, HEIGHT);
        MapDomain domain = new MapDomain(hGrid, null, null, null);
        Map<String, Array2D<Number>> valuesMap = new HashMap<String, Array2D<Number>>();

        /*
         * These reference static vars defined at the end of the file (for
         * readability)
         */
        valuesMap.put("testvar", arr);
        valuesMap.put("testvarx", xarr);
        valuesMap.put("testvary", yarr);
        valuesMap.put("testvarth", thetaarr);
        /*
         * This test feature now has one variable named "testvar" which varies
         * between 0 and 1
         */
        testFeature = new MapFeature("testfeature", "Test Feature",
                "This is a feature used for testing", domain, null, valuesMap);

        catalogue = new FeatureCatalogue() {
            @Override
            public FeaturesAndMemberName getFeaturesForLayer(String id, PlottingDomainParams params) {
                if (id.equals("test")) {
                    return new FeaturesAndMemberName(testFeature, "testvar");
                } else if (id.equals("xtest")) {
                    return new FeaturesAndMemberName(testFeature, "testvarx");
                } else if (id.equals("ytest")) {
                    return new FeaturesAndMemberName(testFeature, "testvary");
                } else if (id.equals("thetatest")) {
                    return new FeaturesAndMemberName(testFeature, "testvarth");
                } else {
                    return null;
                }
            }
        };
        params = new PlottingDomainParams(WIDTH, HEIGHT, bbox, null, null, null, null, null);

    }

    /*
     * Convenience method for reading a test image for comparison
     */
    private BufferedImage getComparisonImage(String name) {
        URL url = this.getClass().getResource("/" + name + ".png");
        try {
            return ImageIO.read(url);
        } catch (IOException e) {
            fail("Comparison image (" + name + ") not available.");
            return null;
        }
    }

    /*
     * Compares two images on a pixel-by-pixel basis
     */
    private void compareImages(BufferedImage expected, BufferedImage actual) {
        assertEquals(expected.getWidth(), actual.getWidth());
        assertEquals(expected.getHeight(), actual.getHeight());
        for (int i = 0; i < actual.getWidth(); i++) {
            for (int j = 0; j < actual.getHeight(); j++) {
                assertEquals(expected.getRGB(i, j), actual.getRGB(i, j));
            }
        }
    }

    /**
     * Accepts a buffered image as input and returns a boolean indicating if the
     * image is entirely the same colour.
     * 
     * @param image
     * @return blank
     */
    private boolean imageBlank(BufferedImage image) {
        int rgb = image.getRGB(0, 0);
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                if (image.getRGB(i, j) != rgb) {
                    return false;
                }
            }
        }
        return true;
    }

    @Test
    public void testRaster() throws EdalException {
        ColourScheme colourScheme = new SegmentColourScheme(scale, Color.blue, Color.red,
                new Color(0, true), "#000000,#00ff00", 10);
        RasterLayer rasterLayer = new RasterLayer("test", colourScheme);
        MapImage mapImage = new MapImage();
        mapImage.getLayers().add(rasterLayer);
        BufferedImage image = mapImage.drawImage(params, catalogue);
        BufferedImage comparisonImage = getComparisonImage("raster");
        compareImages(comparisonImage, image);
    }

    @Test
    public void testContour() throws EdalException {
        ContourLayer contourLayer = new ContourLayer("test", scale, false, 5, Color.cyan, 1,
                ContourLineStyle.SOLID, true);
        MapImage mapImage = new MapImage();
        mapImage.getLayers().add(contourLayer);
        BufferedImage image = mapImage.drawImage(params, catalogue);
        assertFalse(imageBlank(image));
    }

    @Test
    public void testStipple() throws EdalException {
        try {
            SLDRange range = new SLDRange(0f, 1f, Spacing.LINEAR);
            DensityMap function = new SegmentDensityMap(10, range, 0f, 1f, 0f, 1f, 0f);
            StippleLayer stippleLayer = new StippleLayer("test", function);
            MapImage mapImage = new MapImage();
            mapImage.getLayers().add(stippleLayer);
            BufferedImage image = mapImage.drawImage(params, catalogue);
            BufferedImage comparisonImage = getComparisonImage("stipple");
            compareImages(comparisonImage, image);
        } catch (SLDException slde) {
            throw new EdalException("Problem creating range.", slde);
        }
    }

    @Test
    public void testRaster2D() throws EdalException {
        ColourScheme2D colourScheme = new ThresholdColourScheme2D(Arrays.asList(0.1f, 0.5f, 0.9f),
                Arrays.asList(0.1f, 0.5f, 0.9f), Arrays.asList(new Color(0, 0, 0), new Color(0,
                        100, 0), new Color(0, 200, 0), new Color(0, 255, 0), new Color(100, 0, 0),
                        new Color(100, 100, 0), new Color(100, 200, 0), new Color(100, 255, 0),
                        new Color(200, 0, 0), new Color(200, 100, 0), new Color(200, 200, 0),
                        new Color(200, 255, 0), new Color(255, 0, 0), new Color(255, 100, 0),
                        new Color(255, 200, 0), new Color(255, 255, 0)), new Color(0, true));
        Raster2DLayer raster2dLayer = new Raster2DLayer("xtest", "ytest", colourScheme);

        MapImage mapImage = new MapImage();
        mapImage.getLayers().add(raster2dLayer);
        BufferedImage image = mapImage.drawImage(params, catalogue);
        BufferedImage comparisonImage = getComparisonImage("raster2d");
        compareImages(comparisonImage, image);
    }

    @Test
    public void testArrow() throws EdalException {
        ArrowLayer arrowLayer = new ArrowLayer("thetatest", 8, Color.black, new Color(0, true),
                ArrowStyle.UPSTREAM);
        MapImage mapImage = new MapImage();
        mapImage.getLayers().add(arrowLayer);
        BufferedImage image = mapImage.drawImage(params, catalogue);
        BufferedImage comparisonImage = getComparisonImage("upstream_dots");
        compareImages(comparisonImage, image);
    }

    /*
     * Static arrays defined at the bottom to stay out of the way
     */

    private final static Array2D<Number> arr = new Array2D<Number>(HEIGHT, WIDTH) {
        @Override
        public void set(Number value, int... coords) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public Number get(int... coords) {
            int x = coords[1];
            int y = coords[0];
            if (x == WIDTH / 2 || y == HEIGHT / 2) {
                return null;
            }
            if (x == WIDTH / 4 || y == HEIGHT / 4) {
                return -0.5;
            }
            if (x == 3 * WIDTH / 4 || y == 3 * HEIGHT / 4) {
                return 1.5;
            }
            double xComp = ((double) x) / WIDTH;
            double yComp = ((double) y) / HEIGHT;
            return xComp * yComp;
        }
    };

    private final static Array2D<Number> xarr = new Array2D<Number>(HEIGHT, WIDTH) {
        @Override
        public void set(Number value, int... coords) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public Number get(int... coords) {
            return ((double) coords[1]) / WIDTH;
        }
    };

    private final static Array2D<Number> yarr = new Array2D<Number>(HEIGHT, WIDTH) {
        @Override
        public void set(Number value, int... coords) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public Number get(int... coords) {
            return ((double) coords[0]) / HEIGHT;
        }
    };

    private final static Array2D<Number> thetaarr = new Array2D<Number>(HEIGHT, WIDTH) {
        @Override
        public void set(Number value, int... coords) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public Number get(int... coords) {
            return 360 * ((double) coords[1]) / WIDTH;
        }
    };
}
