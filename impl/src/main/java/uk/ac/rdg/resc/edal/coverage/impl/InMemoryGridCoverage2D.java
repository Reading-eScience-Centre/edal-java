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
import uk.ac.rdg.resc.edal.coverage.grid.GridAxis;
import uk.ac.rdg.resc.edal.coverage.grid.GridValuesMatrix;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.coverage.grid.impl.InMemoryGridValuesMatrix;
import uk.ac.rdg.resc.edal.coverage.metadata.RangeMetadata;
import uk.ac.rdg.resc.edal.coverage.metadata.ScalarMetadata;
import uk.ac.rdg.resc.edal.util.BigList;
import uk.ac.rdg.resc.edal.util.CollectionUtils;

/**
 * An in-memory implementation of {@link GridCoverage2D}
 * 
 * @author Guy Griffiths
 * 
 */
public class InMemoryGridCoverage2D extends GridCoverage2DImpl {

    private final Map<String, ScalarMetadata> metadata;
    private final Map<String, List<Object>> values;
    private final RangeMetadata metadataTree;

    /**
     * Instantiates a new {@link InMemoryGridCoverage2D}
     * 
     * @param domain
     *            the domain of the coverage
     * @param values
     *            a {@link Map} of member names to {@link List}s of values
     * @param scalarMetadataMap
     *            a {@link Map} of member names to {@link ScalarMetadata}
     * @param description
     *            a description of the coverage
     */
    public InMemoryGridCoverage2D(HorizontalGrid domain, Map<String, List<Object>> values,
            Map<String, ScalarMetadata> scalarMetadataMap, RangeMetadata metadataTree, String description) {
        super(description, domain, DataReadingStrategy.PIXEL_BY_PIXEL);
        if (values.size() != scalarMetadataMap.size() || !values.keySet().equals(scalarMetadataMap.keySet())) {
            throw new IllegalArgumentException(
                    "Both values and metadata must contain the same members");
        }
        this.values = values;
        this.metadata = scalarMetadataMap;
        this.metadataTree = metadataTree;
    }

    @Override
    public GridValuesMatrix<Object> getGridValues(final String memberName) {
        this.checkMemberName(memberName);

        return new InMemoryGridValuesMatrix<Object>() {
            @Override
            public Object doReadPoint(int[] coords) {
                int index = (int) getDomain().getIndex(coords[0], coords[1]);
                List<Object> vals = values.get(memberName);
                return vals.get(index);
            }

            @Override
            public BigList<Object> getValues() {
                return CollectionUtils.wrap(values.get(memberName));
            }

            @Override
            public GridAxis doGetAxis(int n) {
                switch (n) {
                case 0:
                    return getDomain().getXAxis();
                case 1:
                    return getDomain().getYAxis();
                default:
                    /*
                     * We should never reach this code, because getAxis will
                     * already have checked the bounds
                     */
                    throw new IllegalStateException("Axis index out of bounds");
                }
            }

            @Override
            public int getNDim() {
                return 2;
            }

            @Override
            @SuppressWarnings({ "rawtypes", "unchecked" })
            public Class<Object> getValueType() {
                return (Class) metadata.get(memberName).getValueType();
            }
        };
    }

    @Override
    public Set<String> getScalarMemberNames() {
        return values.keySet();
    }

    @Override
    public ScalarMetadata getScalarMetadata(String memberName) {
        this.checkMemberName(memberName);
        return this.metadata.get(memberName);
    }
    
    @Override
    public RangeMetadata getRangeMetadata() {
        return metadataTree;
    }
}
