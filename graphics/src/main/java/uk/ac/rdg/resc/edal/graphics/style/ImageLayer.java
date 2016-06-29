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
import java.util.Collection;

import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.feature.Feature;
import uk.ac.rdg.resc.edal.graphics.style.util.FeatureCatalogue;
import uk.ac.rdg.resc.edal.graphics.utils.PlottingDomainParams;
import uk.ac.rdg.resc.edal.metadata.VariableMetadata;

/**
 * Abstract class representing a layer within an image.
 *
 * @author Guy Griffiths
 */
public abstract class ImageLayer extends Drawable {
    @Override
    public BufferedImage drawImage(final PlottingDomainParams params,
            final FeatureCatalogue catalogue) throws EdalException {
        BufferedImage image = new BufferedImage(params.getWidth(), params.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        drawIntoImage(image, params, catalogue);
        return image;
    }

    /**
     * Draw the data into a supplied {@link BufferedImage}
     * 
     * @param image
     * @param params
     * @param catalogue
     * @throws EdalException
     */
    protected abstract void drawIntoImage(BufferedImage image, final PlottingDomainParams params,
            final FeatureCatalogue catalogue) throws EdalException;

    public abstract Collection<Class<? extends Feature<?>>> supportedFeatureTypes();
    
    public MetadataFilter getMetadataFilter() {
        return new MetadataFilter() {
            @Override
            public boolean supportsMetadata(VariableMetadata metadata) {
                return true;
            }
        };
    }
    
    public static interface MetadataFilter {
        public boolean supportsMetadata(VariableMetadata metadata);
    }
}
