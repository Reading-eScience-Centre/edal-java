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

package uk.ac.rdg.resc.edal.util;

import org.joda.time.DateTime;

import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.grid.RegularGrid;
import uk.ac.rdg.resc.edal.grid.RegularGridImpl;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;

public class PlottingDomainParams {
    final private int width;
    final private int height;

    final private BoundingBox bbox;
    final private HorizontalPosition targetPos;

    final private Extent<Double> zExtent;
    final private Double targetZ;

    final private Extent<DateTime> tExtent;
    final private DateTime targetT;

    private volatile RegularGrid imageGrid = null;

    public PlottingDomainParams(int width, int height, BoundingBox bbox, Extent<Double> zExtent,
            Extent<DateTime> tExtent, HorizontalPosition targetPos, Double targetZ, DateTime targetT) {
        super();
        this.width = width;
        this.height = height;
        this.bbox = bbox;
        this.targetPos = targetPos;
        this.zExtent = zExtent;
        this.targetZ = targetZ;
        this.tExtent = tExtent;
        this.targetT = targetT;

        if (zExtent == null && targetZ != null) {
            zExtent = Extents.newExtent(targetZ, targetZ);
        }

        if (tExtent == null) {
            if (targetT != null) {
                tExtent = Extents.newExtent(targetT, targetT);
            } else {
                tExtent = Extents.emptyExtent();
            }
        }
    }

    public PlottingDomainParams(RegularGrid imageGrid, Extent<Double> zExtent,
            Extent<DateTime> tExtent, HorizontalPosition targetPos, Double targetZ, DateTime targetT) {
        super();
        this.imageGrid = imageGrid;
        this.width = imageGrid.getXSize();
        this.height = imageGrid.getYSize();
        this.bbox = imageGrid.getBoundingBox();
        this.targetPos = targetPos;
        this.zExtent = zExtent;
        this.targetZ = targetZ;
        this.tExtent = tExtent;
        this.targetT = targetT;

        if (zExtent == null && targetZ != null) {
            zExtent = Extents.newExtent(targetZ, targetZ);
        }

        if (tExtent == null) {
            if (targetT != null) {
                tExtent = Extents.newExtent(targetT, targetT);
            } else {
                tExtent = Extents.emptyExtent();
            }
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

    public HorizontalPosition getTargetHorizontalPosition() {
        return targetPos;
    }

    /**
     * Creates a {@link RegularGrid} based on the width, height and
     * {@link BoundingBox} of these parameters
     */
    public RegularGrid getImageGrid() {
        if (imageGrid == null) {
            imageGrid = new RegularGridImpl(bbox, width, height);
        }
        return imageGrid;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((bbox == null) ? 0 : bbox.hashCode());
        result = prime * result + height;
        result = prime * result + ((tExtent == null) ? 0 : tExtent.hashCode());
        result = prime * result + ((targetPos == null) ? 0 : targetPos.hashCode());
        result = prime * result + ((targetT == null) ? 0 : targetT.hashCode());
        result = prime * result + ((targetZ == null) ? 0 : targetZ.hashCode());
        result = prime * result + width;
        result = prime * result + ((zExtent == null) ? 0 : zExtent.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PlottingDomainParams other = (PlottingDomainParams) obj;
        if (bbox == null) {
            if (other.bbox != null)
                return false;
        } else if (!bbox.equals(other.bbox))
            return false;
        if (height != other.height)
            return false;
        if (tExtent == null) {
            if (other.tExtent != null)
                return false;
        } else if (!tExtent.equals(other.tExtent))
            return false;
        if (targetPos == null) {
            if (other.targetPos != null)
                return false;
        } else if (!targetPos.equals(other.targetPos))
            return false;
        if (targetT == null) {
            if (other.targetT != null)
                return false;
        } else if (!targetT.equals(other.targetT))
            return false;
        if (targetZ == null) {
            if (other.targetZ != null)
                return false;
        } else if (!targetZ.equals(other.targetZ))
            return false;
        if (width != other.width)
            return false;
        if (zExtent == null) {
            if (other.zExtent != null)
                return false;
        } else if (!zExtent.equals(other.zExtent))
            return false;
        return true;
    }
    
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("Width: ");
        str.append(width);
        str.append("\nHeight: ");
        str.append(height);
        str.append("\nBounding box: ");
        str.append(bbox);
        str.append("\nZ-extent: ");
        str.append(zExtent);
        str.append("\nT-extent: ");
        str.append(tExtent);
        str.append("\nTarget position: ");
        str.append(targetPos);
        str.append(", ");
        str.append(targetZ);
        str.append(", ");
        str.append(TimeUtils.dateTimeToISO8601(targetT));
        return str.toString();
    }
}
