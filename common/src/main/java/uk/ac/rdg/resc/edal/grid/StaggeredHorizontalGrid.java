/*******************************************************************************
 * Copyright (c) 2016 The University of Reading
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

package uk.ac.rdg.resc.edal.grid;


/**
 * A {@link HorizontalGrid} which has a staggered relationship to another grid.
 *
 * @author Guy Griffiths
 */
public interface StaggeredHorizontalGrid extends HorizontalGrid {

    /**
     * A staggered grid will consist of cells which are defined as being between
     * the nodes of the original grid. The cell centres are at the midpoints of
     * the nodes of the original grid. Whether or not these cells extend beyond
     * the original grid is defined by the padding.
     * 
     * The defined paddings for {@link StaggeredHorizontalGrid}s are:
     * 
     * <ul>
     * <li>{@link #LOW}</li>
     * <li>{@link #HIGH}</li>
     * <li>{@link #BOTH}</li>
     * <li>{@link #NO_PADDING}</li>
     * <li>{@link #NO_OFFSET}</li>
     * </ul>
     *
     * @author Guy Griffiths
     */
    public enum SGridPadding {
        /**
         * The axis has a cell below the minimum node
         */
        LOW,
        /**
         * The axis has a cell above the maximum node
         */
        HIGH,
        /**
         * The axis has cells below the minimum node and above the maximum node
         * - i.e. there is one more cell than there are nodes on the original
         * axis
         */
        BOTH,
        /**
         * There is no padding - i.e. the cells are only between nodes, and
         * there is one fewer cells than there are nodes on the original axis
         */
        NO_PADDING,
        /**
         * There is no offset. i.e. this axis is NOT staggered.
         */
        NO_OFFSET;
        public static SGridPadding fromString(String s) {
            if (s.equalsIgnoreCase("low")) {
                return LOW;
            }
            if (s.equalsIgnoreCase("high")) {
                return HIGH;
            }
            if (s.equalsIgnoreCase("both")) {
                return BOTH;
            }
            if (s.equalsIgnoreCase("none")) {
                return NO_PADDING;
            }
            return NO_OFFSET;
        }
    };

    /**
     * @return The {@link HorizontalGrid} which this grid is staggered in
     *         relation to
     */
    public HorizontalGrid getOriginalGrid();

    /**
     * @return The {@link SGridPadding} associated with the x-axis of this grid
     */
    public SGridPadding getXPadding();

    /**
     * @return The {@link SGridPadding} associated with the y-axis of this grid
     */
    public SGridPadding getYPadding();
}
