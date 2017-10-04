/*******************************************************************************
 * Copyright (c) 2017 The University of Reading
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
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;

import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.domain.SimpleHorizontalDomain;
import uk.ac.rdg.resc.edal.domain.SimpleTemporalDomain;
import uk.ac.rdg.resc.edal.domain.TrajectoryDomain;
import uk.ac.rdg.resc.edal.exceptions.DataReadingException;
import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.exceptions.VariableNotFoundException;
import uk.ac.rdg.resc.edal.feature.TrajectoryFeature;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.grid.TimeAxis;
import uk.ac.rdg.resc.edal.grid.TimeAxisImpl;
import uk.ac.rdg.resc.edal.metadata.Parameter;
import uk.ac.rdg.resc.edal.metadata.VariableMetadata;
import uk.ac.rdg.resc.edal.position.GeoPosition;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.util.Array1D;
import uk.ac.rdg.resc.edal.util.CollectionUtils;
import uk.ac.rdg.resc.edal.util.ValuesArray1D;

public class FakeTrajectoryDatasetFactory extends DatasetFactory {

    @Override
    public Dataset createDataset(String id, String location, boolean forceRefresh)
            throws IOException, EdalException {
        return new FakeTrajectoryDataset(id);
    }

    public static class FakeTrajectoryDataset extends TrajectoryDataset {
        private static final long serialVersionUID = 1L;
        private static final String FEATURE_ID = "faketrajectory";
        private static final Parameter PARAMETER = new Parameter(FEATURE_ID, "FAKE!",
                "Fake trajectory", "K", "sea_water_temperature");
        private static final int SIZE = 1200;
        private static TimeAxis TIME_AXIS;

        static {
            List<DateTime> axisValues = new ArrayList<>();
            for (int i = 0; i < SIZE; i++) {
                axisValues.add(new DateTime(1981, 5, 28, 7, 20, 00).plus(i * 60 * 1000));
            }
            TIME_AXIS = new TimeAxisImpl("time", axisValues);
        }

        public FakeTrajectoryDataset(String id) {
            super(id, new AbstractList<VariableMetadata>() {
                @Override
                public VariableMetadata get(int index) {
                    return new VariableMetadata(PARAMETER,
                            new SimpleHorizontalDomain(-SIZE / 2, -30, SIZE / 2, 30), null,
                            new SimpleTemporalDomain(TIME_AXIS.getCoordinateExtent()));
                }

                @Override
                public int size() {
                    return 1;
                }
            }, new DiscreteFeatureReader<TrajectoryFeature>() {
                @Override
                public TrajectoryFeature readFeature(String id, Set<String> variableIds)
                        throws DataReadingException {
                    if (!id.equals(FEATURE_ID)) {
                        throw new VariableNotFoundException(FEATURE_ID);
                    }
                    List<GeoPosition> positions = new ArrayList<>();
                    ValuesArray1D valueArr = new ValuesArray1D(SIZE);
                    // Fake up some data
                    for (int i = 0; i < SIZE; i++) {
                        double lfo = 30 * Math.cos(i * 8.8 / SIZE);
                        valueArr.set(273.0 + i * 30.0 / SIZE, i);
                        HorizontalPosition hPos = new HorizontalPosition(-i + SIZE / 2, lfo);

                        GeoPosition pos = new GeoPosition(hPos, null,
                                TIME_AXIS.getCoordinateValue(i));
                        positions.add(pos);
                    }

                    TrajectoryDomain domain = new TrajectoryDomain(positions);
                    Map<String, Parameter> parameters = new HashMap<>();
                    parameters.put(FEATURE_ID, PARAMETER);
                    Map<String, Array1D<Number>> values = new HashMap<>();
                    values.put(FEATURE_ID, valueArr);
                    return new TrajectoryFeature(FEATURE_ID, "Fake trajectory feature",
                            "This is a trajectory.  It's entirely invented for the purpose of testing",
                            domain, parameters, values);
                }

                @Override
                public Collection<TrajectoryFeature> readFeatures(Collection<String> ids,
                        Set<String> variableIds) throws DataReadingException {
                    List<TrajectoryFeature> ret = new ArrayList<>();
                    for(String id : ids) {
                        ret.add(readFeature(id, variableIds));
                    }
                    return ret;
                }
            }, new FeatureIndexer() {
                private static final long serialVersionUID = 1L;

                @Override
                public Set<String> getAllFeatureIds() {
                    return CollectionUtils.setOf(FEATURE_ID);
                }

                @Override
                public Collection<String> findFeatureIds(BoundingBox horizontalExtent,
                        Extent<Double> verticalExtent, Extent<DateTime> timeExtent,
                        Collection<String> variableIds) {
                    return CollectionUtils.setOf(FEATURE_ID);
                }

                @Override
                public void addFeatures(List<FeatureBounds> features) {
                }
            });
        }
    }
}
