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

package uk.ac.rdg.resc.edal.coverage.grid;

import java.util.List;
import uk.ac.rdg.resc.edal.coverage.Record;
import uk.ac.rdg.resc.edal.coverage.RecordType;

/**
 * A {@link Grid} that contains values.
 * @todo What exception should be thrown if data are requested for a grid point
 * that is outside the grid envelope?
 * @todo should extend Collection%lt;Record&gt;?
 * @author Jon
 */
public interface GridValuesMatrix extends Grid {

    /**
     * <p>Returns a sequence of N feature attribute value records where N is the
     * number of grid points within the section of the grid specified by the
     * {@link #getExtent() extent}.</p>
     * <p>Note that this List does not have to exist as a purely in-memory
     * object.  For large grids it may be more efficient to implement a List
     * that wraps other storage, or generates values dynamically.</p>
     * @return a sequence of N feature attribute values.
     * @see #getValues(java.util.List)
     * @see #getOffset(uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates)
     */
    public List<Record> getValues();

    /**
     * @todo Does this belong in the Grid class, or in the relevant Coverage class?
     * We need to retrieve the list of valid member names from the Coverage so
     * it would be handy to have a link somewhere (this is the RecordType below I guess).
     */
    public List<?> getValues(String memberName);

    /**
     * Gets the offset of the given grid point within the
     * {@link #getValues() list of data records}.
     * @param coords The coordinates of the grid point
     * @return the offset of the given grid point within the
     * {@link #getValues() list of data records}.
     */
    public int getOffset(GridCoordinates coords);

    public RecordType getRangeType();

}
