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

package uk.ac.rdg.resc.edal.feature;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;

import uk.ac.rdg.resc.edal.dataset.DataReadingStrategy;
import uk.ac.rdg.resc.edal.dataset.Domain2DMapper;
import uk.ac.rdg.resc.edal.dataset.GridDataSource;
import uk.ac.rdg.resc.edal.domain.GridDomain;
import uk.ac.rdg.resc.edal.domain.MapDomain;
import uk.ac.rdg.resc.edal.exceptions.DataReadingException;
import uk.ac.rdg.resc.edal.grid.GridCell4D;
import uk.ac.rdg.resc.edal.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.grid.RectilinearGrid;
import uk.ac.rdg.resc.edal.grid.TimeAxis;
import uk.ac.rdg.resc.edal.grid.VerticalAxis;
import uk.ac.rdg.resc.edal.metadata.Parameter;
import uk.ac.rdg.resc.edal.position.GeoPosition;
import uk.ac.rdg.resc.edal.util.Array2D;
import uk.ac.rdg.resc.edal.util.Array4D;

/**
 * An implementation of a {@link Feature} whose domain is a {@link GridDomain}
 * 
 * @author Guy
 */
public class GridFeature extends AbstractDiscreteFeature<GeoPosition, GridCell4D> {

    public GridFeature(String id, String name, String description, GridDomain domain,
            Map<String, Parameter> parameters, Map<String, Array4D<Number>> valuesMap) {
        super(id, name, description, domain, parameters, valuesMap);
    }

    @Override
    public Array4D<Number> getValues(String paramId) {
        return (Array4D<Number>) super.getValues(paramId);
    }

    @Override
    public GridDomain getDomain() {
        return (GridDomain) super.getDomain();
    }

    /**
     * Extracts a {@link MapFeature} from this {@link GridFeature}
     * 
     * @param varIds
     *            The variable IDs to extract. If <code>null</code>, all
     *            variables are extracted.
     * @param targetGrid
     *            The desired {@link HorizontalGrid} of the resulting
     *            {@link MapFeature}
     * @param elevation
     *            The elevation at which to extract data, can be
     *            <code>null</code> only if this {@link GridFeature} has no
     *            vertical domain
     * @param time
     *            The time at which to extract data, can be <code>null</code>
     *            only if this {@link GridFeature} has no vertical domain
     * @return The extracted {@link MapFeature}
     * @throws DataReadingException
     *             If there is a problem reading the required data
     */
    public MapFeature extractMapFeature(Set<String> varIds, RectilinearGrid targetGrid,
            Double elevation, DateTime time) throws DataReadingException {
        GridDomain domain = getDomain();

        /*
         * Find the elevation index, if required
         */
        int zIndex = 0;
        VerticalAxis zAxis = domain.getVerticalAxis();
        if (zAxis != null) {
            zIndex = zAxis.findIndexOf(elevation);
        }

        /*
         * Find the time index, if required
         */
        int tIndex = 0;
        TimeAxis timeAxis = domain.getTimeAxis();
        if (timeAxis != null) {
            tIndex = timeAxis.findIndexOf(time);
        }

        if (varIds == null) {
            varIds = getParameterIds();
        }

        Domain2DMapper mapper = Domain2DMapper.forGrid(domain.getHorizontalGrid(), targetGrid);
        Map<String, Parameter> parameters = new HashMap<>();
        Map<String, Array2D<Number>> values = new HashMap<>();
        for (String varId : varIds) {
            if (!getParameterIds().contains(varId)) {
                throw new DataReadingException("Variable " + varId
                        + " is not present in this feature");
            }

            parameters.put(varId, getParameter(varId));

            final Array4D<Number> fullValues = getValues(varId);
            DataReadingStrategy dataReadingStrategy = DataReadingStrategy.PIXEL_BY_PIXEL;
            GridDataSource dataSource = new GridDataSource() {
                @Override
                public Array4D<Number> read(String variableId, final int tmin, int tmax,
                        final int zmin, int zmax, final int ymin, int ymax, final int xmin, int xmax) {
                    return new Array4D<Number>(tmax - tmin + 1, zmax - zmin + 1, ymax - ymin + 1,
                            xmax - xmin + 1) {
                        @Override
                        public Number get(int... coords) {
                            return fullValues.get(coords[0] + tmin, coords[1] + zmin, coords[2]
                                    + ymin, coords[3] + xmin);
                        }

                        @Override
                        public void set(Number value, int... coords) {
                        }
                    };
                }

                @Override
                public void close() throws DataReadingException {
                }
            };
            Array2D<Number> readMapData;
            try {
                readMapData = dataReadingStrategy.readMapData(dataSource, varId, tIndex, zIndex,
                        mapper);
            } catch (IOException e) {
                throw new DataReadingException("Problem reading data", e);
            }
            values.put(varId, readMapData);
        }

        MapDomain mapDomain = new MapDomain(targetGrid, elevation, zAxis == null ? null
                : zAxis.getVerticalCrs(), time);
        MapFeature feature = new MapFeature(getId() + "_subfeature", getName(), getDescription(),
                mapDomain, parameters, values);
        return feature;
    }
}
