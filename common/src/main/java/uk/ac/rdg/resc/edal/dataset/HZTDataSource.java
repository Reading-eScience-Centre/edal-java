/*******************************************************************************
 * Copyright (c) 2015 The University of Reading
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

import java.util.List;

import uk.ac.rdg.resc.edal.exceptions.DataReadingException;

/**
 * A {@link DataSource} which reads data from a domain where the horizontal
 * layers are based on an unstructured mesh, and the vertical / time dimensions
 * are discrete.
 *
 * @author Guy Griffiths
 */
public interface HZTDataSource extends DataSource {
    /**
     * Read the underlying data
     * 
     * @param variableId
     *            The variable to read
     * @param coordsToRead
     *            A {@link List} of co-ordinates to read
     * @return A {@link List} of data corresponding to the provided coordinates
     * @throws DataReadingException
     *             If there is a problem reading the underlying data
     */
    public List<Number> read(String variableId, List<MeshCoordinates3D> coordsToRead)
            throws DataReadingException;

    /**
     * Class representing a set of 3 integer co-ordinates.
     * 
     * @author Guy Griffiths
     */
    public final class MeshCoordinates3D {
        public final int h;
        public final int z;
        public final int t;

        /**
         * Create a new {@link MeshCoordinates3D} object
         * 
         * @param h
         *            The horizontal index
         * @param z
         *            The z-index
         * @param t
         *            The t-index
         */
        public MeshCoordinates3D(int h, int z, int t) {
            this.h = h;
            this.z = z;
            this.t = t;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + h;
            result = prime * result + z;
            result = prime * result + t;
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
            MeshCoordinates3D other = (MeshCoordinates3D) obj;
            if (h != other.h)
                return false;
            if (z != other.z)
                return false;
            if (t != other.t)
                return false;
            return true;
        }
    }
}
