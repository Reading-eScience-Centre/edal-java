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
import uk.ac.rdg.resc.edal.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.position.VerticalPosition;

/**
 * Interface for reading gridded data and associated metadata.
 * 
 * @author Jon
 */
public interface GridDataset extends Dataset<GridFeature> {

    /**
     * Gets the identifiers of each of the underlying grids of data. In some
     * cases these may be the same as the set of variable IDs (
     * {@link #getVariableIds()}). However, sometimes a variable may be composed
     * from more than one underlying grid (e.g. sea water density calculated
     * from temperature and pressure "primitives") and so not all variables will
     * have a corresponding data grid. Conversely, a Dataset may choose not to
     * expose all the underlying data grids as publicly-visible variables.
     * 
     * @todo: Do we need these?
     * @return
     */
    public Set<String> getDataGridIds();

    /**
     * Gets the metadata associated with the underlying data grid with the given
     * identifier, which must appear in {@link #getDataGridIds()}.
     */
    public GridMetadata getGridMetadata(String dataGridId);

    public GridFeature readMapData(Set<String> varIds, DateTime time, VerticalPosition zPos,
            HorizontalGrid hGrid) throws IOException;

}
