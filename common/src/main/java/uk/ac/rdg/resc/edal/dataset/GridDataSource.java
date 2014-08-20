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

package uk.ac.rdg.resc.edal.dataset;

import java.io.IOException;

import uk.ac.rdg.resc.edal.exceptions.DataReadingException;
import uk.ac.rdg.resc.edal.util.Array4D;

/**
 * Low-level interface to multidimensional grids, used by
 * {@link DataReadingStrategy}.
 * 
 * TODO axis order issues: should we define that the returned Array must have
 * the x axis varying fastest, irrespective of the ordering of the underlying
 * data grid? GG: Yes, probably...
 * 
 * @author Jon
 * @author Guy
 */
public interface GridDataSource {

    /**
     * Read an {@link Array4D} of data from the underlying data source
     * 
     * @param variableId
     *            The variable ID to read
     * @param tmin
     *            The minimum time index in the underlying data
     * @param tmax
     *            The maximum time index in the underlying data
     * @param zmin
     *            The minimum z index in the underlying data
     * @param zmax
     *            The maximum z index in the underlying data
     * @param ymin
     *            The minimum y index in the underlying data
     * @param ymax
     *            The maximum y index in the underlying data
     * @param xmin
     *            The minimum x index in the underlying data
     * @param xmax
     *            The maximum x index in the underlying data
     * @return An {@link Array4D} containing the data which was read
     * @throws IOException
     *             If there is an IO problem accessing the data
     * @throws DataReadingException
     *             If there is another issue reading the data
     */
    public Array4D<Number> read(String variableId, int tmin, int tmax, int zmin, int zmax,
            int ymin, int ymax, int xmin, int xmax) throws IOException, DataReadingException;

    /**
     * Close all resources associated with the underlying data.
     * 
     * @throws IOException
     *             If the underlying data cannot be closed for some reason
     */
    public void close() throws IOException;

}
