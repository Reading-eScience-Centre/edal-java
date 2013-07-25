/*
 * Copyright (c) 2013 The University of Reading
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of The University of Reading, nor the names of the
 *    authors or contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
 * THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package uk.ac.rdg.resc.edal.dataset;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import org.joda.time.DateTime;
import uk.ac.rdg.resc.edal.feature.GridFeature;
import uk.ac.rdg.resc.edal.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.grid.TimeAxis;
import uk.ac.rdg.resc.edal.grid.VerticalAxis;
import uk.ac.rdg.resc.edal.position.VerticalPosition;
import uk.ac.rdg.resc.edal.util.Array;
import uk.ac.rdg.resc.edal.util.CollectionUtils;

/**
 * 
 * @author Jon
 */
public abstract class AbstractGridDataset implements GridDataset
{
    protected final DataReadingStrategy strategy = null; // TODO populate this
    
    @Override
    public final GridFeature readMapData(Set<String> varIds, DateTime time,
            VerticalPosition zPos, HorizontalGrid targetGrid) throws IOException
    {
        // Open the source of data
        GridDataSource dataSource = this.openGridDataSource();
        
        // The procedure below can be optimized: if we know that multiple variables
        // share the same source grid then we don't have to perform the conversion
        // from natural coordinates to grid indices multiple times.
        // HOWEVER, we might have to beware of this in the case of aggregation,
        // in which different variables may have different mappings from time
        // values to filename/tIndex.
        
        Map<String, Array<? extends Number>> values = CollectionUtils.newHashMap();
        Map<String, VariableMetadata> metadata = CollectionUtils.newHashMap();
        
        for (String varId : varIds)
        {
            VariableMetadata vm = this.getVariableMetadata(varId);
            
            // TODO: if this is a variable whose values are derived (rather than
            // being read directly) we need to work out which 
            
            // Cast down the horizontal, vertical and temporal domain objects
            // to HorizontalGrid, VerticalAxis and TimeAxis.  This should always
            // be successful because we define GridDatasets to always use these
            // domain subclasses.
            HorizontalGrid sourceGrid = (HorizontalGrid) vm.getHorizontalDomain();
            VerticalAxis zAxis        = (VerticalAxis)   vm.getVerticalDomain();
            TimeAxis tAxis            = (TimeAxis)       vm.getTemporalDomain();
            
            // Use this object to convert natural coordinates to grid indices
            int tIndex = tAxis.findIndexOf(time);
            int zIndex = zAxis.findIndexOf(zPos);
            // Create a PixelMap from the source and target grids
            PixelMap pixelMap = PixelMap.forGrid(sourceGrid, targetGrid);
            
            // Now use the approprate DataReadingStrategy to read data
            Array<? extends Number> data = this.strategy.readMapData(dataSource,
                    varId, tIndex, zIndex, pixelMap);
            
            // Create new VariableMetadata object with the new domain, units etc
            VariableMetadata newVm = null; // TODO
            
            values.put(varId, gvm);
            metadata.put(varId, newVm);
        }
        
        // Release resources held by the DataSource
        dataSource.close();
        
        // Construct the GridFeature from the t and z values and the horizontal grid
        // and the VariableMetadata objects
        GridFeature gf = null; // TODO
        
        return gf;
    }
    
    protected abstract GridDataSource openGridDataSource() throws IOException;
}
