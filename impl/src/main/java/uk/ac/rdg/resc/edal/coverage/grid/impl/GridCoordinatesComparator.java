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

import java.util.Comparator;

import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates;

/**
 * <p>
 * A {@link Comparator} for {@link GridCoordinates} objects that implements the
 * ordering defined in
 * {@link uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates#compareTo(uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates)}
 * . Collections of {@link GridCoordinates} objects that are sorted using this
 * comparator will end up with coordinates sorted such that the last coordinate
 * varies fastest, which is likely to match the order in which corresponding
 * data values are stored (e.g. on disk).
 * </p>
 * <p>
 * This object is stateless and therefore immutable, hence a single object is
 * created that can be reused freely.
 * </p>
 * 
 * @author Jon
 * @author Guy Griffiths
 */
public enum GridCoordinatesComparator implements Comparator<GridCoordinates> {

    /** Singleton instance */
    INSTANCE;

    /**
     * <p>
     * Compares two {@link GridCoordinates} objects for order. We define this
     * ordering as follows:
     * </p>
     * <ul>
     * <li>If the two objects have a different number of dimensions, the one
     * with more dimensions is considered to be larger
     * <li>If the x-index of c1 is greater than the x-index of c2, then c1 is
     * considered to be larger than c1</li>
     * <li>If the x-indices of c1 and c2 are identical, comparison is done on
     * the y-index</li>
     * </ul>
     * <p>
     * This ordering ensures that collections of GridCoordinates objects will be
     * ordered with the y-coordinate varying fastest.
     * </p>
     * 
     * @param c1
     *            The first set of coordinates to be compared
     * @param c2
     *            The second set of coordinates to be compared
     * @return a negative integer, zero, or a positive integer as {@code c1} is
     *         less than, equal to, or greater than {@code c2}.
     */
    @Override
    public int compare(GridCoordinates c1, GridCoordinates c2) {
        if (c1.getNDim() != c2.getNDim()) {
            return new Integer(c1.getNDim()).compareTo(c2.getNDim());
        }
        for (int i = 0; i < c1.getNDim(); i++) {
            int diff = c1.getIndex(i) - c2.getIndex(i);
            if (diff != 0)
                return diff;
        }
        return 0; // If we get this far the objects are equal
    }

}
