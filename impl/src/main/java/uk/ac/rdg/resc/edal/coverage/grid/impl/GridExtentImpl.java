/*******************************************************************************
 * Copyright (c) 2012 The University of Reading
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

package uk.ac.rdg.resc.edal.coverage.grid.impl;

import uk.ac.rdg.resc.edal.Extent;
import uk.ac.rdg.resc.edal.ExtentImpl;
import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates;
import uk.ac.rdg.resc.edal.coverage.grid.GridExtent;

/**
 * 
 * Implementation of {@link GridExtent}
 * 
 * @author Jon
 * @author Guy Griffiths
 * 
 */
public final class GridExtentImpl implements GridExtent {

    private final GridCoordinatesImpl low;
    private final GridCoordinatesImpl high;
    // These are calculated from the GridCoordinates upon construction
    private transient long size;

    /**
     * Creates a new GridExtent with the given low and high coordinates. Note
     * that both sets of coordinates are <b>inclusive</b>. The grid coordinate
     * information is copied to internal data structures so modifying these
     * parameters externally has no effect on this object.
     * 
     * @param low
     *            the low coordinates of the grid
     * @param high
     *            the high coordinates of the grid
     * @throws IllegalArgumentException
     *             if {@code low.getDimension() != high.getDimension()} or if
     *             any of the high coordinates is lower than its corresponding
     *             low coordinate.
     */
    public GridExtentImpl(GridCoordinates low, GridCoordinates high) {
        if (low.getNDim() != high.getNDim()) {
            throw new IllegalArgumentException(
                    "High and low coordinates must have the same dimensionality");
        }

        size = 1L;
        /*
         * int * int -> int, EVEN WHEN RESULT IS TOO BIG
         * 
         * Therefore we must cast (at least one of the values) to long
         */
        for (int i = 0; i < low.getNDim(); i++) {
            if (high.getIndex(i) < low.getIndex(i)) {
                throw new IllegalArgumentException(String.format("A high coordinate is lower"
                        + " than a low coordinate"));
            }
            size *= (high.getIndex(i) - low.getIndex(i) + 1);
        }
        // We ensure that the internal GridCoordinates objects are instances of
        // GridCoordinatesImpl to ensure that they are immutable
        this.low = GridCoordinatesImpl.convert(low);
        this.high = GridCoordinatesImpl.convert(high);
    }

    public GridExtentImpl(Extent<Integer>... extents) {
        this(new GridCoordinatesImpl(getLows(extents)), new GridCoordinatesImpl(getHighs(extents)));
    }
    
    private static int[] getLows(Extent<Integer>... extents) {
        int[] lows = new int[extents.length];
        for (int i = 0; i < extents.length; i++) {
            lows[i] = extents[i].getLow();
        }
        return lows;
    }
    
    private static int[] getHighs(Extent<Integer>... extents) {
        int[] highs = new int[extents.length];
        for (int i = 0; i < extents.length; i++) {
            highs[i] = extents[i].getHigh();
        }
        return highs;
    }

    /**
     * Creates a new GridExtent with the given high coordinates with all low
     * coordinates equal to zero.
     * 
     * @param high
     *            the high coordinates of the grid
     * @throws IllegalArgumentException
     *             if any of the high coordinates is less than zero.
     */
    public GridExtentImpl(GridCoordinates high) {
        this(GridCoordinatesImpl.zero(high.getNDim()), high);
    }

    /**
     * Creates a new GridExtent with the given high coordinates with all low
     * coordinates equal to zero.
     * 
     * @param highCoordX
     *            The high coordinate of the x-axis
     * @param highCoordY
     *            The high coordinate of the y-axis
     * @throws IllegalArgumentException
     *             if any of the high coordinates is less than zero.
     */
    public GridExtentImpl(int... highCoords) {
        this(new GridCoordinatesImpl(highCoords));
    }

    /**
     * Returns true if this envelope contains the given coordinates.
     * 
     * @param coordinates
     *            The coordinates to test
     * @return true if this envelope contains the given coordinates.
     */
    @Override
    public boolean contains(GridCoordinates coords) {
        int[] components = new int[coords.getNDim()];
        for (int i = 0; i < coords.getNDim(); i++) {
            components[i] = coords.getIndex(i);
        }
        return this.contains(components);
    }

    /**
     * Returns true if this envelope contains the given coordinates
     * 
     * @param coords
     *            The coordinates to test
     * @throws IllegalArgumentException
     *             if the number of coordinates given does not match the
     *             dimensionality of the grid
     * @return true if this envelope contains the given coordinates
     */
    @Override
    public boolean contains(int... coords) {
        /*
         * We can use the size of low or high, since they are guaranteed to have
         * the same length
         */
        if (low.getNDim() != coords.length) {
            throw new IllegalArgumentException("Wrong number of coordinates supplied.  Need "
                    + low.getNDim() + ", but you supplied " + coords.length);
        }
        boolean contains = true;
        for(int i=0; i<coords.length; i++){
            contains = contains && coords[i] >= getLow().getIndex(i)
                    && coords[i] <= getHigh().getIndex(i);
        }
        return contains;
    }

    /**
     * <p>
     * Creates a new GridExtentImpl from the given GridExtent object. This
     * may be used to convert a GridExtent object of unknown type to one of
     * this type, perhaps to make an object that is guaranteed immutable.
     * </p>
     * <p>
     * If {@code env} is already an instance of GridExtentImpl, this method
     * simply returns it: no new objects are created.
     * </p>
     * 
     * @param env
     *            The GridExtent to copy.
     * @return a GridExtentImpl object containing the same information as the
     *         provided GridExtent object.
     */
    public static GridExtentImpl convert(GridExtent env) {
        if (env instanceof GridExtentImpl)
            return (GridExtentImpl) env;
        return new GridExtentImpl(env.getLow(), env.getHigh());
    }

    /**
     * Returns the minimal coordinate values for all grid points within the
     * grid. The returned object is immutable.
     * 
     * @return The minimal coordinate values for all grid points,
     *         <b>inclusive</b>.
     */
    @Override
    public GridCoordinates getLow() {
        return low;
    }

    /**
     * Returns the maximal coordinate values for all grid points within the
     * grid. The returned object is immutable.
     * 
     * @return The maximal coordinate values for all grid points,
     *         <b>inclusive</b>.
     */
    @Override
    public GridCoordinates getHigh() {
        return high;
    }

    private int hashCode = Integer.MAX_VALUE;

    @Override
    public int hashCode() {
        if (hashCode == Integer.MAX_VALUE) {
            hashCode = 17;
            hashCode = 31 * hashCode + low.hashCode();
            hashCode = 31 * hashCode + high.hashCode();
        }
        return hashCode;
    }

    /**
     * Compares for equality only with other {@link GridExtentImpl} objects;
     * {@literal i.e.}, other implementations of the {@link GridExtent}
     * interface are not considered equal to implementations of
     * {@link GridExtentImpl}. (This is because the {@link GridExtent} interface
     * does not defined a contract for equals() or hashCode().)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof GridExtentImpl))
            return false;
        GridExtentImpl other = (GridExtentImpl) obj;
        return low.equals(other.low) && high.equals(other.high);
    }

    @Override
    public String toString() {
        return low.toString() + ":" + high.toString();
    }

    @Override
    public long size() {
        return size;
    }

    @Override
    public Extent<Integer> getExtent(int dim) {
        return new ExtentImpl(low.getIndex(dim), high.getIndex(dim));
    }
}
