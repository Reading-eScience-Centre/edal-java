/*
 * Copyright (c) 2009 The University of Reading
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
package uk.ac.rdg.resc.edal.util.position;

import java.util.Arrays;
import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * <p>Implementation of a {@link DirectPosition} that is immutable <i>provided that
 * the passed-in coordinate reference system is also immutable</i> or null.</p>
 * <p>Although instances of this class are immutable, instances of subclasses may
 * not be.</p>
 * @author Jon
 */
public class DirectPositionImpl implements DirectPosition {

    private final CoordinateReferenceSystem crs;
    private final double[] coords;

    /**
     * Creates a new DirectPositionImpl with the given coordinate reference
     * system and the given coordinates.
     * @param crs The coordinate reference system.  If this is an immutable
     * object then this DirectPositionImpl will also be immutable.  This
     * may be null (in which case the CRS of this DirectPosition must be given
     * by some containing object).
     * @param coords The coordinates of this position.
     * @throws NullPointerException if {@code coords == null)
     * @throws IllegalArgumentException if the length of the coordinate array
     * does not match the number of dimensions in the CRS, or if
     * {@code coords.length == 0}.
     */
    public DirectPositionImpl(CoordinateReferenceSystem crs, double[] coords) {
        if (coords == null) {
            throw new NullPointerException();
        }
        if (coords.length == 0) {
            throw new IllegalArgumentException("Zero-length coordinates array");
        }
        checkCrs(crs, coords.length);
        this.crs = crs;
        this.coords = coords.clone(); // Defensive copy to preserve immutability
    }

    /**
     * Creates a new DirectPositionImpl with the given coordinate reference
     * system and the given coordinates.
     *
     * @param crs
     *            The coordinate reference system. If this is an immutable
     *            object then this DirectPositionImpl will also be immutable.
     *            This may be null (in which case the CRS of this DirectPosition
     *            must be given by some containing object).
     * @param coord1
     *            The first coordinate of this position
     * @param otherCoords
     *            The remaining coordinates of this position
     * @throws IllegalArgumentException
     *             if the length of the coordinate array does not match the
     *             number of dimensions in the CRS.
     */
    public DirectPositionImpl(CoordinateReferenceSystem crs, double coord1,
            double... otherCoords) {
        int coordsLength = 1;
        if (otherCoords != null) {
            coordsLength += otherCoords.length;
        }
        this.coords = new double[coordsLength];
        this.coords[0] = coord1;
        if (otherCoords != null) {
            for (int i = 0; i < otherCoords.length; i++) {
                this.coords[i + 1] = otherCoords[i];
            }
        }
        checkCrs(crs, this.coords.length);
        this.crs = crs;
    }

    private static void checkCrs(CoordinateReferenceSystem crs, int coordsLength) {
        if (crs != null
                && crs.getCoordinateSystem().getDimension() != coordsLength) {
            String msg = String.format(
                    "Dimensionality of CRS (%d) must match length of coordinate array (%d)",
                    crs.getCoordinateSystem().getDimension(),
                    coordsLength);
            throw new IllegalArgumentException(msg);
        }
    }

    @Override
    public final CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return this.crs;
    }

    @Override
    public final int getDimension() {
        return this.coords.length;
    }

    /** Returns a newly-created array of coordinates */
    @Override
    public final double[] getCoordinate() {
        return this.coords.clone(); // Defensive copy returned to preserve
        // immutability
    }

    @Override
    public final double getOrdinate(int index) {
        if (index < 0 || index >= this.coords.length) {
            String msg = String.format(
                    "Attempt to access element at index %d in array of length %d",
                    index, this.coords.length);
            throw new IndexOutOfBoundsException(msg);
        }
        return this.coords[index];
    }

    /**
     * Always throws {@link UnsupportedOperationException}. Instances of this
     * class are immutable.
     */
    @Override
    public final void setOrdinate(int index, double coordValue) {
        throw new UnsupportedOperationException("Not supported.");
    }

    /** Returns this object */
    @Override
    public final DirectPosition getDirectPosition() {
        return this;
    }

    @Override
    public String toString() {
        // TODO: this could be pre-calculated since this object is immutable.
        String s = String.format("Coords: %s ", Arrays.toString(this.coords));
        s += this.crs == null ? "(null CRS)" : String.format("(CRS: %s)",
                this.crs.toString());
        return s;
    }

    /** Follows the contract defined by {@link DirectPosition} */
    @Override
    public final int hashCode() {
        // TODO: this could be pre-calculated since this object is immutable,
        // or at least cached once calculated
        int result = Arrays.hashCode(this.coords);
        if (this.crs != null) {
            result += this.crs.hashCode();
        }
        return result;
    }

    /** Follows the contract defined by {@link DirectPosition} */
    @Override
    public final boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof DirectPosition)) {
            return false;
        }
        DirectPosition other = (DirectPosition) obj;
        if (!Arrays.equals(this.coords, other.getCoordinate())) {
            return false;
        }
        CoordinateReferenceSystem otherCrs = other.getCoordinateReferenceSystem();
        return this.crs == null ? otherCrs == null : this.crs.equals(otherCrs);
    }
}
