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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.rdg.resc.edal.Phenomenon;
import uk.ac.rdg.resc.edal.Unit;
import uk.ac.rdg.resc.edal.coverage.BaseGridCoverage;
import uk.ac.rdg.resc.edal.coverage.domain.GridDomain;
import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates;
import uk.ac.rdg.resc.edal.coverage.grid.GridValuesMatrix;
import uk.ac.rdg.resc.edal.coverage.metadata.PlotStyle;
import uk.ac.rdg.resc.edal.coverage.plugins.Plugin;
import uk.ac.rdg.resc.edal.util.BigList;

/**
 * A partial implementation of a multimember coverage on a {@link GridDomain}.
 * This provides a method to add members by supplying a {@link GridValuesMatrix}
 * and some basic metadata
 * 
 * @author Guy Griffiths
 * 
 * 
 * @param <P>
 *            The type of object used to identify positions within the
 *            coverage's domain. This may be a spatial, temporal, or combined
 *            spatiotemporal position.
 * @param <DO>
 *            The type of domain object
 * @param <GD>
 *            The type of domain which members must be on
 */
public abstract class AbstractMultimemberDiscreteGridCoverage<P, DO, GD extends GridDomain<P, DO>>
        extends AbstractMultimemberDiscreteCoverage<P, DO, GD> implements BaseGridCoverage<P, DO> {

    private final Map<String, GridValuesMatrix<?>> gridValuesMatrices;

    public AbstractMultimemberDiscreteGridCoverage(String description, GD domain) {
        super(description, domain);
        gridValuesMatrices = new HashMap<String, GridValuesMatrix<?>>();
    }

    @Override
    public GridValuesMatrix<?> getGridValues(final String memberName) {
        if (!gridValuesMatrices.containsKey(memberName) && !plugins.containsKey(memberName)) {
            throw new IllegalArgumentException(memberName + " is not present in this coverage");
        }
        if (plugins.containsKey(memberName)) {
            final Plugin plugin = plugins.get(memberName);
            final List<GridValuesMatrix<?>> pluginInputs = new ArrayList<GridValuesMatrix<?>>();
            for (String neededId : plugin.uses()) {
                pluginInputs.add(gridValuesMatrices.get(neededId));
            }
            return plugin.getProcessedValues(memberName, pluginInputs);
        } else {
            return gridValuesMatrices.get(memberName);
        }
    }

    public void addMember(String memberName, GD domain, String description, Phenomenon parameter,
            Unit units, GridValuesMatrix<?> gridValueMatrix, List<PlotStyle> availablePlotStyles) {
        addMemberToMetadata(memberName, domain, description, parameter, units,
                gridValueMatrix.getValueType(), availablePlotStyles);
        gridValuesMatrices.put(memberName, gridValueMatrix);
    }

    @Override
    public final BigList<?> getValuesList(final String memberName) {
        return new AbstractBigList2<Object>() {
            @Override
            public Object get(long index) {
                GridValuesMatrix<?> gridValues = getGridValues(memberName);
                GridCoordinates coords = gridValues.getCoords(index);
                Object value = gridValues.readPoint(coords.getIndices());
                return value;
            }

            @Override
            public List<Object> getAll(List<Long> indices) {
                GridValuesMatrix<?> gridValues = getGridValues(memberName);
                List<Object> values = new ArrayList<Object>(indices.size());
                for (long index : indices) {
                    GridCoordinates coords = gridValues.getCoords(index);
                    Object value = gridValues.readPoint(coords.getIndices());
                    values.add(value);
                }
                return values;
            }
        };
    }
}
