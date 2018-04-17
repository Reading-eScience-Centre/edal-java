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

package uk.ac.rdg.resc.edal.graphics.style;

import java.awt.image.BufferedImage;
import java.util.Set;

import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.metadata.VariableMetadata;
import uk.ac.rdg.resc.edal.util.Array2D;
import uk.ac.rdg.resc.edal.util.CollectionUtils;
import uk.ac.rdg.resc.edal.util.Extents;

public class RasterLayer extends GriddedImageLayer {

    private String dataFieldName;
    private ColourScheme colourScheme;

    public RasterLayer(String dataFieldName, ColourScheme colourScheme) {
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
    protected void drawIntoImage(BufferedImage image, MapFeatureDataReader dataReader)
            throws EdalException {
        /*
         * Initialise the array to store colour values
         */
        int[] pixels = new int[image.getWidth() * image.getHeight()];

        /*
         * Extract the data from the catalogue
         */
        Array2D<Number> values = dataReader.getDataForLayerName(dataFieldName);

        /*
         * The iterator iterates over the x-dimension first, which is the same
         * convention as expected for the colour-values array in image.setRGB
         * below
         */
        int index = 0;
        for (Number value : values) {
            pixels[index++] = colourScheme.getColor(value).getRGB();
        }
        image.setRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());
    }

    @Override
    public Set<NameAndRange> getFieldsWithScales() {
        if (colourScheme instanceof EnumeratedColourScheme) {
            return CollectionUtils.setOf(new NameAndRange(dataFieldName,
                    Extents.newExtent(colourScheme.getScaleMin(), colourScheme.getScaleMax()),
                    ((EnumeratedColourScheme) colourScheme).getEnumeratedPoints()));
        } else {
            return CollectionUtils.setOf(new NameAndRange(dataFieldName,
                    Extents.newExtent(colourScheme.getScaleMin(), colourScheme.getScaleMax())));
        }
    }

    @Override
    public MetadataFilter getMetadataFilter() {
        return new MetadataFilter() {
            @Override
            public boolean supportsMetadata(VariableMetadata metadata) {
                if (metadata.getParameter().getUnits().equalsIgnoreCase("degrees")) {
                    /*
                     * We want to exclude directional fields - they look
                     * terrible plotted as rasters, and we already have
                     * ArrowLayers for that...
                     */
                    return false;
                }
                if (metadata.getParameter().getCategories() != null
                        && !(getColourScheme() instanceof MappedColourScheme)) {
                    /*
                     * If we have categorical data we only want to support it if
                     * we're using a MappedColourScheme
                     */
                    return false;
                }
                return true;
            }
        };
    }

}
