/*******************************************************************************
 * Copyright (c) 2014 The University of Reading
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

import java.awt.Color;

import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.util.Extents;

/**
 * A {@link ColourScheme} which treats all values as their equivalent RGB
 * integer value (see {@link Color#Color(int)})
 *
 * @author Guy Griffiths
 */
public class RGBBandColourScheme {
    private static final Color TRANSPARENT = new Color(0, true);
    private ScaleRange rRange;
    private ScaleRange gRange;
    private ScaleRange bRange;

    public RGBBandColourScheme(ScaleRange rRange, ScaleRange gRange, ScaleRange bRange) {
        this.rRange = rRange;
        this.gRange = gRange;
        this.bRange = bRange;
    }

    public Color getColor(Number red, Number green, Number blue) {
        if (red == null && green == null && blue == null) {
            return TRANSPARENT;
        }
        if (red == null) {
            red = 0;
        }
        if (green == null) {
            green = 0;
        }
        if (blue == null) {
            blue = 0;
        }
        Float r = rRange.scaleZeroToOne(red);
        if (r < 0)
            r = 0f;
        else if (r > 1)
            r = 1f;
        Float g = gRange.scaleZeroToOne(green);
        if (g < 0)
            g = 0f;
        else if (g > 1)
            g = 1f;
        Float b = bRange.scaleZeroToOne(blue);
        if (b < 0)
            b = 0f;
        else if (b > 1)
            b = 1f;
        return new Color(r, g, b);
    }

    public Extent<Float> getRedScale() {
        return Extents.newExtent(rRange.getScaleMin(), rRange.getScaleMax());
    }

    public Extent<Float> getGreenScale() {
        return Extents.newExtent(gRange.getScaleMin(), gRange.getScaleMax());
    }

    public Extent<Float> getBlueScale() {
        return Extents.newExtent(bRange.getScaleMin(), bRange.getScaleMax());
    }

}