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

package uk.ac.rdg.resc.edal.graphics.style.util;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.geotoolkit.referencing.crs.DefaultGeographicCRS;

import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.domain.MapDomain;
import uk.ac.rdg.resc.edal.domain.MapDomainImpl;
import uk.ac.rdg.resc.edal.feature.DiscreteFeature;
import uk.ac.rdg.resc.edal.feature.MapFeature;
import uk.ac.rdg.resc.edal.feature.PointFeature;
import uk.ac.rdg.resc.edal.graphics.style.Drawable.NameAndRange;
import uk.ac.rdg.resc.edal.graphics.style.MapImage;
import uk.ac.rdg.resc.edal.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.grid.RegularAxisImpl;
import uk.ac.rdg.resc.edal.grid.RegularGridImpl;
import uk.ac.rdg.resc.edal.position.GeoPosition;
import uk.ac.rdg.resc.edal.util.Array1D;
import uk.ac.rdg.resc.edal.util.Array2D;
import uk.ac.rdg.resc.edal.util.Extents;
import uk.ac.rdg.resc.edal.util.ImmutableArray1D;
import uk.ac.rdg.resc.edal.util.PlottingDomainParams;

/**
 * A class to generate the correct data for a legend. This provides methods to
 * get a set of {@link PlottingDomainParams} and a corresponding
 * {@link FeatureCatalogue}. When these two objects are passed to a
 * {@link MapImage#drawImage(PlottingDomainParams, FeatureCatalogue)} an
 * appropriate map will be drawn with linearly-varying fields, suitable for use
 * as a legend. The names of the fields to use are set in the
 * {@link LegendDataGenerator#getFeatureCatalogue(String, String)} method
 * 
 * @author Guy Griffiths
 */
public class LegendDataGenerator {

    private RegularAxisImpl xAxis;
    private RegularAxisImpl yAxis;
    private MapDomain domain;
    private boolean[][] missingBits;

    private float fractionExtra;

    /**
     * Instantiate a new {@link LegendDataGenerator}
     * 
     * @param width
     *            The width of the domain (which will translate to the final
     *            image width in pixels)
     * @param height
     *            The height of the domain (which will translate to the final
     *            image height in pixels)
     * @param backgroundMask
     *            An image to use as a background mask - i.e. where the missing
     *            data should be generated. Pixels with value 0 (usually black)
     *            are interpreted as missing data
     * @param fractionExtra
     *            The fraction of the total data which should be counted as out
     *            of range data (both above the max and below the min)
     */
    public LegendDataGenerator(int width, int height, BufferedImage backgroundMask,
            float fractionExtra) {
        /*
         * We use 0.001 as the spacing. Since we're working in WGS84 (for
         * convenience - it doesn't matter what CRS we use, but we need to work
         * in one) - anything outside normal lat/lon range will not be rendered.
         * 0.0001 spacing allows us to have each legend component be sized up to
         * (90 / 0.001) pixels.
         */
        xAxis = new RegularAxisImpl("", 0, 0.001, width, false);
        yAxis = new RegularAxisImpl("", 0, 0.001, height, false);

        HorizontalGrid hGrid = new RegularGridImpl(xAxis, yAxis, DefaultGeographicCRS.WGS84);
        domain = new MapDomainImpl(hGrid, null, null, null);

        this.fractionExtra = fractionExtra;

        missingBits = new boolean[width][height];
        if (backgroundMask != null) {
            Image scaledInstance = backgroundMask.getScaledInstance(width, height,
                    BufferedImage.SCALE_FAST);
            BufferedImage bufferedImage = new BufferedImage(width, height,
                    BufferedImage.TYPE_BYTE_GRAY);
            bufferedImage.createGraphics().drawImage(scaledInstance, 0, 0, null);
            byte[] data = ((DataBufferByte) bufferedImage.getRaster().getDataBuffer()).getData();
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    if (data[i + width * j] == 0) {
                        missingBits[i][j] = true;
                    } else {
                        missingBits[i][j] = false;
                    }
                }
            }
        }
    }

    /**
     * @return A set of {@link PlottingDomainParams} which correspond to the
     *         entire range and given of the domain of the data returned by
     *         {@link LegendDataGenerator#getFeatureCatalogue(NameAndRange, NameAndRange)}
     */
    public PlottingDomainParams getPlottingDomainParams() {
        return new PlottingDomainParams(xAxis.size(), yAxis.size(), domain.getBoundingBox(), null,
                null, null, null, null);
    }

    /**
     * Gets a {@link FeatureCatalogue} to generate features with
     * linearly-varying numerical data and the correct members
     *
     * @param xField
     *            A {@link NameAndRange} object for the field which should vary
     *            linearly in the x-direction. Set to <code>null</code> for no x
     *            variation
     * @param yField
     *            A {@link NameAndRange} object for the field which should vary
     *            linearly in the y-direction. Set to <code>null</code> for no y
     *            variation
     * @return A {@link FeatureCatalogue} which when the
     *         {@link FeatureCatalogue#getFeaturesForLayer(String, PlottingDomainParams)}
     *         method is called will return a set of features containing both
     *         {@link MapFeature}s and {@link PointFeature}s suitable for
     *         plotting. Note that the {@link PlottingDomainParams} argument is
     *         ignored here, and the {@link PlottingDomainParams}s which should
     *         be used in the
     *         {@link MapImage#drawImage(PlottingDomainParams, FeatureCatalogue)}
     *         method are those returned by
     *         {@link LegendDataGenerator#getPlottingDomainParams()}
     */
    public FeatureCatalogue getFeatureCatalogue(NameAndRange xField, NameAndRange yField) {
        final Set<DiscreteFeature<?, ?>> features = new HashSet<>();
        features.add(getMapFeature(xField, yField));
        features.addAll(getPointFeatures(xField, yField));

        return new FeatureCatalogue() {
            @Override
            public FeaturesAndMemberName getFeaturesForLayer(String id, PlottingDomainParams params) {
                return new FeaturesAndMemberName(features, id);
            }
        };
    }

    /**
     * Generates a {@link MapFeature} containing the correctly named variables
     * 
     * @param xField
     *            The variable to vary linearly in the x-direction, or
     *            <code>null</code> if none is desired
     * @param yField
     *            The variable to vary linearly in the y-direction, or
     *            <code>null</code> if none is desired
     * @return A {@link MapFeature} containing the correctly named variables
     *         with the correct linearly-varying data
     */
    private MapFeature getMapFeature(NameAndRange xField, NameAndRange yField) {
        Map<String, Array2D<Number>> values = new HashMap<String, Array2D<Number>>();

        if (xField != null) {
            values.put(xField.getFieldLabel(), new XYNan(MatrixType.X, xField.getScaleRange()));
        }
        if (yField != null) {
            values.put(yField.getFieldLabel(), new XYNan(MatrixType.Y, yField.getScaleRange()));
        }
        MapFeature feature = new MapFeature("", "", "", domain, null, values);
        return feature;
    }

    private Collection<? extends DiscreteFeature<?, ?>> getPointFeatures(NameAndRange xField,
            NameAndRange yField) {
        List<PointFeature> features = new ArrayList<>();
        Random r = new Random();

        /*
         * Add 100 points randomly which vary in the x-direction
         * 
         * TODO - needs out-of-range stuff and 2D testing
         */
        if (xField != null) {
            for (int i = 0; i < 500; i++) {
                int xIndex = r.nextInt(xAxis.size());
                int yIndex = r.nextInt(yAxis.size());

                Map<String, Array1D<Number>> values = new HashMap<>();
                values.put(
                        xField.getFieldLabel(),
                        new ImmutableArray1D<>(new Number[] { getLinearInterpolatedValue(xIndex,
                                xField.getScaleRange(), xAxis.size()) }));
                PointFeature feature = new PointFeature("", "", "", new GeoPosition(domain
                        .getDomainObjects().get(yIndex, xIndex).getCentre(), null, null), null,
                        values);
                features.add(feature);
            }
        }
        /*
         * Add 100 points randomly which vary in the y-direction
         */
        if (yField != null) {
            for (int i = 0; i < 500; i++) {
                int xIndex = r.nextInt(xAxis.size());
                int yIndex = r.nextInt(yAxis.size());

                Map<String, Array1D<Number>> values = new HashMap<>();
                values.put(
                        yField.getFieldLabel(),
                        new ImmutableArray1D<>(new Number[] { getLinearInterpolatedValue(yIndex,
                                yField.getScaleRange(), yAxis.size()) }));
                PointFeature feature = new PointFeature("", "", "", new GeoPosition(domain
                        .getDomainObjects().get(yIndex, xIndex).getCentre(), null, null), null,
                        values);
                features.add(feature);
            }
        }
        return features;
    }

    private enum MatrixType {
        X, Y, NAN
    };

    /**
     * An {@link Array2D} which contains data either varying in just the
     * x-direction, just the y-direction, or entirely {@link Float#NaN} data
     *
     * @author Guy Griffiths
     */
    private class XYNan extends Array2D<Number> {
        private MatrixType type;
        private Extent<Float> scaleRange = null;

        public XYNan(MatrixType type, Extent<Float> scaleRange) {
            super(yAxis.size(), xAxis.size());
            this.type = type;
            /*
             * Expand scale range to include out-of-range data
             */
            if (scaleRange != null) {
                Float width = scaleRange.getHigh() - scaleRange.getLow();
                this.scaleRange = Extents.newExtent(scaleRange.getLow() - width * fractionExtra,
                        scaleRange.getHigh() + width * fractionExtra);
            }
        }

        @Override
        public Number get(int... coords) {
            if (missingBits[coords[X_IND]][yAxis.size() - coords[Y_IND] - 1]) {
                return Float.NaN;
            }
            switch (type) {
            case X:
                return getLinearInterpolatedValue(coords[X_IND], scaleRange, xAxis.size());
            case Y:
                return getLinearInterpolatedValue(coords[Y_IND], scaleRange, yAxis.size());
            case NAN:
            default:
                return Float.NaN;
            }
        }

        @Override
        public void set(Number value, int... coords) {
            throw new UnsupportedOperationException("This Array2D is immutable");
        }
    }

    private static Number getLinearInterpolatedValue(int value, Extent<Float> scaleRange,
            int axisSize) {
        return scaleRange.getLow() + value * (scaleRange.getHigh() - scaleRange.getLow())
                / axisSize;
    }
}
