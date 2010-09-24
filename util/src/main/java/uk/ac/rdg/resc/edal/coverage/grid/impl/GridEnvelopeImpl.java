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

package uk.ac.rdg.resc.edal.coverage.grid.impl;

import org.opengis.coverage.grid.GridCoordinates;
import org.opengis.coverage.grid.GridEnvelope;

/**
 * Immutable implementation of {@link GridEnvelope}.
 * @author Jon
 */
public final class GridEnvelopeImpl implements GridEnvelope {

    private final GridCoordinatesImpl low;
    private final GridCoordinatesImpl high;

    // These are calculated from the GridCoordinates upon construction
    private transient final int[] combos;
    private transient final int size;

    /**
     * Creates a new GridEnvelope with the given low and high coordinates.  Note
     * that both sets of coordinates are <b>inclusive</b>.  The grid coordinate
     * information is copied to internal data structures so modifying these
     * parameters externally has no effect on this object.
     * @param low the low coordinates of the grid
     * @param high the high coordinates of the grid
     * @throws IllegalArgumentException if {@code low.getDimension() != high.getDimension}
     * or if any of the high coordinates is lower than its corresponding low
     * coordinate.
     */
    public GridEnvelopeImpl(GridCoordinates low, GridCoordinates high) {
        if (low.getDimension() != high.getDimension()) {
            throw new IllegalArgumentException("Dimensions of low and high " +
                "GridCoordinates must be equal");
        }
        for (int i = 0; i < low.getDimension(); i++) {
            if (high.getCoordinateValue(i) < low.getCoordinateValue(i)) {
                String msg = String.format("High coordinate at index %d is lower"
                    + " than the low coordinate", i);
                throw new IllegalArgumentException(msg);
            }
        }
        // We ensure that the internal GridCoordinates objects are instances of
        // GridCoordinatesImpl to ensure that they are immutable
        this.low = GridCoordinatesImpl.convert(low);
        this.high = GridCoordinatesImpl.convert(high);

        // Calculate the number of combinations of coordinates for each
        // dimension.  We need this for indexOf() and getCoordinates(), which may
        // be called many times, so this pre-calculation is sensible.
        // We calculate the number of coordinates in this grid (the size) at the
        // same time.
        this.combos = new int[this.getDimension()];
        this.combos[this.getDimension() - 1] = 1;
        int n = this.getSpan(this.getDimension() - 1);
        for (int j = this.getDimension() - 2; j >= 0; j--) {
            this.combos[j] = this.combos[j + 1] * this.getSpan(j + 1);
            n *= this.getSpan(j);
        }
        this.size = n;
    }

    /**
     * Creates a new GridEnvelope with the given high coordinates with all
     * low coordinates equal to zero.
     * @param high the high coordinates of the grid
     * @throws IllegalArgumentException if any of the high coordinates is less
     * than zero.
     */
    public GridEnvelopeImpl(GridCoordinates high) {
        this(GridCoordinatesImpl.zero(high.getDimension()), high);
    }

    /**
     * Creates a new GridEnvelope with the given high coordinates with all
     * low coordinates equal to zero.
     * @param highCoord1 The first high coordinate
     * @param otherHighCoords The remaining high coordinates
     * @throws IllegalArgumentException if any of the high coordinates is less
     * than zero.
     */
    public GridEnvelopeImpl(int highCoord1, int... otherHighCoords) {
        this(new GridCoordinatesImpl(highCoord1, otherHighCoords));
    }

    /**
     * <p>Creates a new GridEnvelopeImpl from the given GridEnvelope
     * object.  This may be used to convert a GridEnvelope object of
     * unknown type to one of this type, perhaps to make an object that is
     * guaranteed immutable.</p>
     * <p>If {@code env} is already an instance of GridEnvelopeImpl, this method
     * simply returns it: no new objects are created.</p>
     * @param env The GridEnvelope to copy.
     * @return a GridEnvelopeImpl object containing the same information as
     * the provided GridEnvelope object.
     */
    public static GridEnvelopeImpl convert(GridEnvelope env) {
        if (env instanceof GridEnvelopeImpl) return (GridEnvelopeImpl)env;
        return new GridEnvelopeImpl(env.getLow(), env.getHigh());
    }

    @Override
    public int getDimension() {
        return this.low.getDimension();
    }

    /**
     * Returns the minimal coordinate values for all grid points within the grid.
     * The returned object is immutable.
     * @return The minimal coordinate values for all grid points, <b>inclusive</b>.
     */
    @Override
    public GridCoordinatesImpl getLow() {
        return this.low;
    }

    /**
     * Returns the maximal coordinate values for all grid points within the grid.
     * The returned object is immutable.
     * @return The maximal coordinate values for all grid points, <b>inclusive</b>.
     */
    @Override
    public GridCoordinatesImpl getHigh() {
        return this.high;
    }

    @Override
    public int getLow(int dimension) {
        this.checkIndex(dimension);
        return this.low.getCoordinateValue(dimension);
    }

    @Override
    public int getHigh(int dimension) {
        this.checkIndex(dimension);
        return this.high.getCoordinateValue(dimension);
    }

    @Override
    public int getSpan(int dimension) {
        this.checkIndex(dimension);
        return this.getHigh(dimension) - this.getLow(dimension) + 1;
    }

    private void checkIndex(int dimension) {
        if (dimension < 0 || dimension >= this.getDimension()) {
            String msg = String.format("Attempt to access element at dimension "
                + "%d in envelope of dimensionality %d", dimension, this.getDimension());
            throw new IndexOutOfBoundsException(msg);
        }
    }

    /**
     * <p>Returns the <i>i</i>th set of grid coordinates in this envelope, based
     * upon the ordering defined by
     * {@link org.jcsml.coverage.grid.GridCoordinates#compareTo(org.jcsml.coverage.grid.GridCoordinates)}.
     * {@code getCoordinates(0)} is therefore equivalent to {@link #getLow() getLow()}
     * and {@code getCoordinates(getSize() - 1)} is equivalent to {@link #getHigh() getHigh()}.</p>
     * <p>The returned {@link GridCoordinates} object will be immutable.</p>
     * <p>This method is the inverse of {@link #indexOf(org.opengis.coverage.grid.GridCoordinates)
     * indexOf(GridCoordinates)}.</p>
     * @param i the index through the grid coordinates in this envelope
     * @return the <i>i</i>th set of grid coordinates in this envelope
     * @throws IllegalArgumentException if {@code i < 0} or {@code i >= getSize()}.
     */
    public GridCoordinatesImpl getCoordinates(int i) {
        if (i < 0 || i >= this.getSize()) {
            String msg = String.format("The requested index (%d) is outside the"
                + " range of this envelope", i);
            throw new IllegalArgumentException(msg);
        }
        // Shortcuts for efficiency
        if (i == 0) return this.getLow();
        if (i == this.getSize() - 1) return this.getHigh();

        // Now we find the coordinate index for each axis
        int[] coords = new int[this.getDimension()];
        for (int j = 0; j < this.getDimension(); j++) {
            coords[j] = i / this.combos[j];
            i %= this.combos[j];
        }

        // Now we must add the low coordinates to these
        for (int j = 0; j < this.getDimension(); j++) {
            coords[j] += this.low.getCoordinateValue(j);
        }

        return new GridCoordinatesImpl(coords);
    }

    /**
     * <p>Returns the index at which the given coordinates appear in this envelope,
     * using the order defined by
     * {@link org.jcsml.coverage.grid.GridCoordinates#compareTo(org.jcsml.coverage.grid.GridCoordinates)}.</p>
     * <p>This method is the inverse of {@link #getCoordinates(int)}.</p>
     * @param coordinates The grid coordinates whose index is to be found.
     * @return the index at which the given coordinates appear in this envelope,
     * or -1 if the coordinates do not appear in this envelope.
     */
    public int indexOf(GridCoordinates coords) {
        if (!this.contains(coords)) return -1;
        int[] coordVals = coords.getCoordinateValues(); // Defined to return a new array

        int index = 0;
        for (int i = 0; i < this.getDimension(); i++) {
            // Subtract the low coordinate first
            coordVals[i] -= this.low.getCoordinateValue(i);
            assert(coordVals[i] >= 0);
            index += coordVals[i] * this.combos[i];
        }
        return index;
    }

    /**
     * Returns true if this envelope contains the given coordinates.
     * @param coordinates The coordinates to test
     * @return true if this envelope contains the given coordinates.
     */
    public boolean contains(GridCoordinates coords) {
        return this.contains(coords.getCoordinateValues());
    }

    public boolean contains(int... coords) {
        if (coords == null) throw new NullPointerException();
        if (coords.length != this.getDimension()) {
            throw new IllegalArgumentException("coords.length should be " + this.getDimension());
        }

        for (int i = 0; i < coords.length; i++) {
            if (coords[i] < this.low.getCoordinateValue(i) ||
                coords[i] > this.high.getCoordinateValue(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the total number of GridCoordinates objects in this envelope.
     * @return the total number of GridCoordinates objects in this envelope.
     */
    public int getSize() {
        return this.size; // pre-computed for efficiency
    }

    @Override public int hashCode() {
        // TODO could be pre-calculated
        int result = 17;
        result = 31 * result + this.low.hashCode();
        result = 31 * result + this.high.hashCode();
        return result;
    }

    /**
     * Compares for equality only with other {@link GridEnvelopeImpl} objects;
     * {@literal i.e.}, other implementations of the {@link GridEnvelope}
     * interface are not considered equal to implementations of {@link GridEnvelopeImpl}.
     * (This is because the {@link GridEnvelope} interface does not defined
     * a contract for equals() or hashCode().)
     */
    @Override public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof GridEnvelopeImpl)) return false;
        GridEnvelopeImpl other = (GridEnvelopeImpl)obj;
        return this.low.equals(other.low) && this.high.equals(other.high);
    }

    @Override public String toString() {
        return this.low.toString() + ":" + this.high.toString();
    }

}
