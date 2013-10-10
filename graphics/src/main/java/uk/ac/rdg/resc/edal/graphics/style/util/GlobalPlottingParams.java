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

import org.joda.time.DateTime;

import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.util.Extents;

public class GlobalPlottingParams {
    private int width = 256;
    private int height = 256;

    private BoundingBox bbox;
    private Extent<Double> zExtent;
    private Extent<DateTime> tExtent;
    private Double targetZ;
    private DateTime targetT;

    public GlobalPlottingParams(int width, int height, BoundingBox bbox, Extent<Double> zExtent,
            Extent<DateTime> tExtent, Double targetZ, DateTime targetT) {
        super();
        this.width = width;
        this.height = height;
        this.bbox = bbox;
        this.zExtent = zExtent;
        this.tExtent = tExtent;
        this.targetZ = targetZ;
        this.targetT = targetT;
        
        if(zExtent == null && targetZ != null) {
            zExtent = Extents.newExtent(targetZ, targetZ);
        }
        
        if(tExtent == null && targetT != null) {
            tExtent = Extents.newExtent(targetT, targetT);
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public BoundingBox getBbox() {
        return bbox;
    }

    public Extent<Double> getZExtent() {
        return zExtent;
    }

    public Extent<DateTime> getTExtent() {
        return tExtent;
    }

    public Double getTargetZ() {
        return targetZ;
    }

    public DateTime getTargetT() {
        return targetT;
    }
}
