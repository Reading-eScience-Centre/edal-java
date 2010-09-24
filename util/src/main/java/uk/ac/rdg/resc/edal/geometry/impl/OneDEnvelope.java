/*
 * Copyright (c) 2010 The University of Reading
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
 */

package uk.ac.rdg.resc.edal.geometry.impl;

import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * An immutable one-dimensional envelope
 * @author Jon
 */
public final class OneDEnvelope extends AbstractEnvelope
{
    private final double min;
    private final double max;

    public OneDEnvelope(double min, double max, CoordinateReferenceSystem crs) {
        super(crs);
        if (crs != null && crs.getCoordinateSystem().getDimension() != 1) {
            throw new IllegalArgumentException("CRS must be one-dimensional");
        }
        this.min = min;
        this.max = max;
    }

    /** Creates a one-dimensional envelope with a null coordinate reference system */
    public OneDEnvelope(double min, double max) {
        this(min, max, null);
    }

    /** returns 1 */
    @Override
    public int getDimension() { return 1; }

    @Override
    public final double getMinimum(int i) {
        if (i == 0) return this.min;
        throw new IndexOutOfBoundsException();
    }

    @Override
    public final double getMaximum(int i) {
        if (i == 0) return this.max;
        throw new IndexOutOfBoundsException();
    }

    @Override
    public DirectPosition getLowerCorner() {
        return new DirectPositionImpl(this.getCoordinateReferenceSystem(), this.min);
    }

    @Override
    public DirectPosition getUpperCorner() {
        return new DirectPositionImpl(this.getCoordinateReferenceSystem(), this.max);
    }

}
