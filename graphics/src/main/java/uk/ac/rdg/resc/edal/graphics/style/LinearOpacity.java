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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import uk.ac.rdg.resc.edal.util.Array2D;
import uk.ac.rdg.resc.edal.util.Extents;


@XmlType(namespace = MapImage.NAMESPACE, name="LinearOpacityType")
public class LinearOpacity extends OpacityTransform {
    @XmlElement(name = "DataFieldName", required = true)
    private String dataFieldName;
    @XmlElement(name = "OpaqueValue", required = true)
    private Float opaqueValue;
    @XmlElement(name = "TransparentValue", required = true)
    private Float transparentValue;
//    @XmlElement(name = "MissingDataOpacity")
//    private Float opacityForMissingData = 1.0f;

    LinearOpacity() { }

    public LinearOpacity(String dataFieldName, Float opaqueValue, Float transparentValue
            /*, Float opacityForMissingData*/) {
        super();
        this.dataFieldName = dataFieldName;
        this.opaqueValue = opaqueValue;
        this.transparentValue = transparentValue;
//        this.opacityForMissingData = opacityForMissingData;
    }

    private Float getOpacityForValue(Number value) {
        if (value == null || Float.isNaN(value.floatValue())) {
            return 1f;
//            return opacityForMissingData;
        }
        boolean highOpaque = opaqueValue > transparentValue;
        
        float floatValue = value.floatValue();
        if (highOpaque) {
            if (floatValue > opaqueValue) {
                return 1.0f;
            } else if (floatValue < transparentValue) {
                return 0f;
            } else {
                return (floatValue - transparentValue) / (opaqueValue - transparentValue);
            }
        } else {
            if (floatValue < opaqueValue) {
                return 1.0f;
            } else if (floatValue > transparentValue) {
                return 0f;
            } else {
                return 1f - ((floatValue - opaqueValue) / (transparentValue - opaqueValue));
            }
        }
    }

    @Override
    protected void applyOpacityToImage(BufferedImage image, DataReader dataReader) {
        int width = image.getWidth();
        int height = image.getHeight();

        int[] imagePixels = image.getRGB(0, 0, width, height, null, 0, width);

        Array2D values = dataReader.getDataForLayerName(dataFieldName);
        
        int index = 0;
        Iterator<Number> iterator = values.iterator();
        while(iterator.hasNext()) {
            int alpha = ((int) (getOpacityForValue(iterator.next()) * 255));
            imagePixels[index] = blendPixel(imagePixels[index], alpha);
            index++;
        }
        image.setRGB(0, 0, width, height, imagePixels, 0, width);
    }

    @Override
    protected Set<NameAndRange> getFieldsWithScales() {
        Set<NameAndRange> ret = new HashSet<Drawable.NameAndRange>();
        if(opaqueValue > transparentValue) {
            ret.add(new NameAndRange(dataFieldName, Extents.newExtent(transparentValue, opaqueValue)));
        } else {
            ret.add(new NameAndRange(dataFieldName, Extents.newExtent(opaqueValue, transparentValue)));
        }
        return ret;
    }
    
    public static void main(String[] args) {
        LinearOpacity o = new LinearOpacity("a", 0f, 1f);
        LinearOpacity o2 = new LinearOpacity("a", 1f, 0f);
        for(float i = -0.01f; i<=1.01f; i += 0.01) {
            System.out.println(i+", "+o.getOpacityForValue(i)+", "+o2.getOpacityForValue(i));
        }
    }
}
