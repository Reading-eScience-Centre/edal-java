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

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import uk.ac.rdg.resc.edal.exceptions.BadTimeFormatException;
import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.graphics.style.util.DataReadingTypes.SubsampleType;
import uk.ac.rdg.resc.edal.graphics.style.util.FeatureCatalogue;
import uk.ac.rdg.resc.edal.graphics.style.util.FeatureCatalogue.MapFeatureAndMember;
import uk.ac.rdg.resc.edal.graphics.style.util.PlottingDomainParams;
import uk.ac.rdg.resc.edal.util.Array2D;

@XmlType(namespace = MapImage.NAMESPACE, name = "ImageLayerType")
public abstract class ImageLayer extends Drawable {

    protected interface DataReader {
        public Array2D<Number> getDataForLayerName(String layerId) throws EdalException;
    }

    /*
     * For when the plot type is SUBSAMPLE
     */
    private int xSampleSize = 8;
    private int ySampleSize = 8;
    private SubsampleType subsampleType = SubsampleType.CLOSEST;

    protected ImageLayer() {
    }

    @Override
    public BufferedImage drawImage(final PlottingDomainParams params,
            final FeatureCatalogue catalogue) throws EdalException {
        BufferedImage image = new BufferedImage(params.getWidth(), params.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        drawIntoImage(image, params, catalogue);
        return image;
    }

    protected void drawIntoImage(BufferedImage image, final PlottingDomainParams params,
            final FeatureCatalogue catalogue) throws EdalException {
        drawIntoImage(image, new DataReader() {
            @Override
            public Array2D<Number> getDataForLayerName(String layerId) throws BadTimeFormatException {
                MapFeatureAndMember featureAndMemberName = catalogue.getFeatureAndMemberName(
                        layerId, params);
                final Array2D<Number> values = featureAndMemberName.getMapFeature().getValues(
                        featureAndMemberName.getMember());
                /*
                 * Since BufferedImages have the y-axis increasing downwards,
                 * wrap the returned values in an Array2D with a flipped y-axis
                 */
                return new Array2D<Number>(values.getYSize(), values.getXSize()) {
                    @Override
                    public void set(Number value, int... coords) {
                        throw new UnsupportedOperationException("This is an immutable Array2D");
                    }
                    
                    @Override
                    public Number get(int... coords) {
                        return values.get(params.getHeight() - coords[0] - 1, coords[1]);
                    }

                    @Override
                    public Class<Number> getValueClass() {
                        return Number.class;
                    }
                };
            }
        });
    }

    protected abstract void drawIntoImage(BufferedImage image, DataReader dataReader)
            throws EdalException;

    public void setXSampleSize(int xSampleSize) {
        this.xSampleSize = xSampleSize;
    }

    @XmlTransient
    public int getXSampleSize() {
        return xSampleSize;
    }

    public void setYSampleSize(int ySampleSize) {
        this.ySampleSize = ySampleSize;
    }

    @XmlTransient
    public int getYSampleSize() {
        return ySampleSize;
    }

    public void setSubsampleType(SubsampleType subsampleType) {
        this.subsampleType = subsampleType;
    }

    @XmlTransient
    public SubsampleType getSubsampleType() {
        return subsampleType;
    }
}
