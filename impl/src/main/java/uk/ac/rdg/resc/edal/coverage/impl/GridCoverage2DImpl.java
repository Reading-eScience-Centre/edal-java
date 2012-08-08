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

package uk.ac.rdg.resc.edal.coverage.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.rdg.resc.edal.coverage.GridCoverage2D;
import uk.ac.rdg.resc.edal.coverage.grid.GridCell2D;
import uk.ac.rdg.resc.edal.coverage.grid.GridValuesMatrix;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.coverage.metadata.RangeMetadata;
import uk.ac.rdg.resc.edal.coverage.metadata.ScalarMetadata;
import uk.ac.rdg.resc.edal.coverage.metadata.impl.RangeMetadataImpl;
import uk.ac.rdg.resc.edal.coverage.plugins.Plugin;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.util.CollectionUtils;

/**
 * An implementation of a {@link GridCoverage2D}. By basing this on
 * {@link AbstractMultimemberDiscreteGridCoverage}, we can add {@link Plugin}s
 * and add members arbitrarily by just supplying the correct
 * {@link GridValuesMatrix}
 * 
 * @author Guy Griffiths
 * 
 */
public class GridCoverage2DImpl extends
        AbstractMultimemberDiscreteGridCoverage<HorizontalPosition, GridCell2D, HorizontalGrid>
        implements GridCoverage2D {

    private final DataReadingStrategy strategy;

    public GridCoverage2DImpl(String description, HorizontalGrid domain,
            DataReadingStrategy strategy) {
        super(description, domain);
        this.strategy = strategy;
    }

    @Override
    public HorizontalGrid getDomain() {
        /*
         * This cast is fine, because the domain has been set from the
         * constructor (which constrains it to a HorizontalGrid)
         */
        return super.getDomain();
    }

    @Override
    public GridCoverage2D extractGridCoverage(HorizontalGrid targetGrid, Set<String> memberNames) {
        if (targetGrid.size() > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Target grid too large");
        }

        Map<String, List<Object>> valuesMap = CollectionUtils.newLinkedHashMap();
        Map<String, ScalarMetadata> metadataMap = CollectionUtils.newLinkedHashMap();
        
        if(memberNames == null){
            memberNames = getScalarMemberNames();
        }
        
        // Read the data from the source coverage
        for (String name : memberNames) {
            List<Object> values = strategy.readValues(this.getGridValues(name), this.getDomain(),
                    targetGrid);

            valuesMap.put(name, values);
            metadataMap.put(name, this.getScalarMetadata(name));
        }
        
        RangeMetadata rangeMetadata = getRangeMetadata();
        RangeMetadataImpl.getCopyOfMetadataContaining(rangeMetadata, memberNames);

        // Now assemble the remaining properties of the target coverage
        return new InMemoryGridCoverage2D(targetGrid, valuesMap, metadataMap, rangeMetadata,
                "Interpolated grid from " + getDescription());
    }
}
