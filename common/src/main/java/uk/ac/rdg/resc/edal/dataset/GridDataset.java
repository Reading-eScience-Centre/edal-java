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
import java.util.Set;

import org.joda.time.DateTime;

import uk.ac.rdg.resc.edal.feature.GridFeature;
import uk.ac.rdg.resc.edal.feature.MapFeature;
import uk.ac.rdg.resc.edal.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.metadata.GridVariableMetadata;

/**
 * Interface for reading gridded data and associated metadata.
 * 
 * @author Jon
 * @author Guy
 */
public interface GridDataset extends Dataset {
    /**
     * Returns the {@link GridVariableMetadata} associated with a particular
     * variable ID
     */
    @Override
    public GridVariableMetadata getVariableMetadata(String variableId);

    /**
     * Returns the {@link Set} of {@link GridVariableMetadata} objects which are
     * at the top level of this {@link GridDataset}
     */
    @Override
    public Set<GridVariableMetadata> getTopLevelVariables();

    /**
     * Extracts a {@link GridFeature} from this {@link GridDataset}
     * 
     * @param varIds
     *            The variables to extract
     * @param hGrid
     *            The target {@link HorizontalGrid}
     * @param zPos
     *            The target z-position
     * @param time
     *            The target {@link DateTime}
     * @return The extracted {@link GridFeature}
     * @throws IOException
     *             If the underlying data cannot be read for any reason
     */
    public MapFeature readMapData(Set<String> varIds, HorizontalGrid hGrid, Double zPos,
            DateTime time) throws IOException;

//    public PointSeriesFeature readTimeSeriesData(Set<String> varIds, HorizontalPosition hPos,
//            Double zPos, TimeAxis timeAxis) throws IOException;

//    public ProfileFeature readProfileData(Set<String> varIds, HorizontalPosition hPos,
//            VerticalAxis zAxis, DateTime time) throws IOException;
}
