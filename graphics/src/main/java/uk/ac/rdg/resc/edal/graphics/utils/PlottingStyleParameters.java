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

import java.awt.Color;

import uk.ac.rdg.resc.edal.domain.Extent;

/**
 * Class defining the parameters which affect the style of a plot and which can
 * be substituted into a style template.
 * 
 * @author Guy Griffiths
 */
public class PlottingStyleParameters {
    private final Extent<Float> scaleRange;
    private final String palette;
    private final Color aboveMaxColour;
    private final Color belowMinColour;
    private final Color noDataColour;
    private final Boolean logScaling;
    private final Integer numColourBands;
    private final Float opacity;

    public PlottingStyleParameters(Extent<Float> scaleRange, String palette, Color aboveMaxColour,
            Color belowMinColour, Color noDataColour, Boolean logScaling, Integer numColourBands,
            Float opacity) {
        this.scaleRange = scaleRange;
        this.palette = palette;
        this.aboveMaxColour = aboveMaxColour;
        this.belowMinColour = belowMinColour;
        this.noDataColour = noDataColour;
        this.logScaling = logScaling;
        this.numColourBands = numColourBands;
        this.opacity = opacity;
    }

    /**
     * @return The scale range of this layer
     */
    public Extent<Float> getColorScaleRange() {
        return scaleRange;
    }

    /**
     * @return The palette to use for this layer. This can be an existing
     *         palette name, or a palette definition in the form
     *         #[aa]rrggbb,#[aa]rrggbb,#[aa]rrggbb..., where each element is a
     *         hexadecimal value
     */
    public String getPalette() {
        return palette;
    }

    /**
     * @return The colour to use for values which are higher the the maximum
     *         scale value.
     */
    public Color getAboveMaxColour() {
        return aboveMaxColour;
    }

    /**
     * @return The colour to use for values which are lower the the minimum
     *         scale value.
     */
    public Color getBelowMinColour() {
        return belowMinColour;
    }

    /**
     * @return The colour to use for values which have no data.
     */
    public Color getNoDataColour() {
        return noDataColour;
    }

    /**
     * @return <code>true</code> if this variable is to use logarithmic scaling
     *         by default
     */
    public Boolean isLogScaling() {
        return logScaling;
    }

    /**
     * @return The number of colour bands to use for this layer's palette
     */
    public Integer getNumColorBands() {
        return numColourBands;
    }

    public Float getOpacity() {
        return opacity;
    }
}
