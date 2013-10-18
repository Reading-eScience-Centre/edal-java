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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.geotoolkit.referencing.crs.DefaultGeographicCRS;

import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.domain.MapDomain;
import uk.ac.rdg.resc.edal.domain.MapDomainImpl;
import uk.ac.rdg.resc.edal.feature.MapFeature;
import uk.ac.rdg.resc.edal.graphics.style.Drawable.NameAndRange;
import uk.ac.rdg.resc.edal.grid.RegularAxisImpl;
import uk.ac.rdg.resc.edal.util.Array2D;
import uk.ac.rdg.resc.edal.util.Extents;

/**
 * A class to generate the correct data for a legend.
 * 
 * @author guy
 * 
 */
public class LegendDataGenerator {

    private RegularAxisImpl xAxis;
    private RegularAxisImpl yAxis;
    private MapDomain domain;
    private Set<NameAndRange> dataFields;
    private boolean[][] missingBits;

    private float fractionExtra;

    public LegendDataGenerator(Set<NameAndRange> dataFields, int width, int height,
            BufferedImage backgroundMask, float fractionExtra) {
        /*
         * We use 0.0001 as the spacing. Since we're working in WGS84 (for
         * convenience - it doesn't matter what CRS we use, but we need to work
         * in one) - anything outside normal lat/lon range will not be rendered.
         * 0.0001 spacing allows us to have each legend component be sized up to
         * (90 / 0.0001) pixels.
         */
        xAxis = new RegularAxisImpl("", 0, 0.001, width, false);
        yAxis = new RegularAxisImpl("", 0, 0.001, height, false);

        domain = new MapDomainImpl(xAxis, yAxis, DefaultGeographicCRS.WGS84, null, null, null);

        this.dataFields = dataFields;

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
                    if (data[i + width * (height - 1 - j)] == 0) {
                        missingBits[i][j] = true;
                    } else {
                        missingBits[i][j] = false;
                    }
                }
            }
        }
    }

    public GlobalPlottingParams getGlobalParams() {
        return new GlobalPlottingParams(xAxis.size(), yAxis.size(), domain.getBoundingBox(), null,
                null, null, null);
    }

    public FeatureCatalogue getFeatureCatalogue(String xFieldName, String yFieldName) {
        final MapFeature feature = getFeature(xFieldName, yFieldName);
        return new FeatureCatalogue() {
            @Override
            public MapFeatureAndMember getFeatureAndMemberName(String id,
                    GlobalPlottingParams params) {
                return new MapFeatureAndMember(feature, id);
            }
        };
    }

    private MapFeature getFeature(String xFieldName, String yFieldName) {
        ArrayList<NameAndRange> dataRangesList = new ArrayList<NameAndRange>(dataFields);

        Map<String, Array2D<Number>> values = new HashMap<String, Array2D<Number>>();

        /*
         * We need to initialise 3 2D-arrays here, and add them to the values
         * map
         */
        for (NameAndRange nameAndRange : dataRangesList) {
            if (nameAndRange == null) {
                continue;
            }
            
            Array2D<Number> valuesArray;
            if (nameAndRange.getFieldLabel().equals(xFieldName)) {
                valuesArray = new XYNull(MatrixType.X, nameAndRange.getScaleRange());
            } else if (nameAndRange.getFieldLabel().equals(yFieldName)) {
                valuesArray = new XYNull(MatrixType.Y, nameAndRange.getScaleRange());
            } else {
                valuesArray = new XYNull(MatrixType.NAN, null);
            }

            values.put(nameAndRange.getFieldLabel(), valuesArray);
        }

        MapFeature feature = new MapFeature("", "", "", domain, null, values);
        return feature;
    }

    private enum MatrixType {
        X, Y, NAN
    };

    private class XYNull extends Array2D<Number> {
        private MatrixType type;
        private Extent<Float> scaleRange = null;

        public XYNull(MatrixType type, Extent<Float> scaleRange) {
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
                return scaleRange.getLow() + coords[X_IND]
                        * (scaleRange.getHigh() - scaleRange.getLow()) / xAxis.size();
            case Y:
                return scaleRange.getLow() + (yAxis.size() - coords[Y_IND] - 1)
                        * (scaleRange.getHigh() - scaleRange.getLow()) / yAxis.size();
            case NAN:
            default:
                return Float.NaN;
            }
        }

        @Override
        public void set(Number value, int... coords) {
            throw new UnsupportedOperationException("This Array2D is immutable");
        }

        @Override
        public Class<Number> getValueClass() {
            return Number.class;
        }
    }
}
