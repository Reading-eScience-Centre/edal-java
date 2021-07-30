/*******************************************************************************
 * Copyright (c) 2016 The University of Reading
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

package uk.ac.rdg.resc.edal.covjson.writers;

import java.io.IOException;
import java.util.Map.Entry;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.DerivedCRS;
import org.opengis.referencing.crs.GeocentricCRS;
import org.opengis.referencing.crs.GeodeticCRS;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.crs.ProjectedCRS;

import uk.ac.rdg.resc.edal.covjson.StreamingEncoder.ArrayEncoder;
import uk.ac.rdg.resc.edal.covjson.StreamingEncoder.ArrayHints;
import uk.ac.rdg.resc.edal.covjson.StreamingEncoder.MapEncoder;
import uk.ac.rdg.resc.edal.covjson.writers.Constants.Keys;
import uk.ac.rdg.resc.edal.covjson.writers.Constants.Vals;
import uk.ac.rdg.resc.edal.covjson.writers.Coverage.Axis;
import uk.ac.rdg.resc.edal.covjson.writers.Coverage.Domain;
import uk.ac.rdg.resc.edal.covjson.writers.Coverage.ReferenceSystemConnection;
import uk.ac.rdg.resc.edal.covjson.writers.Coverage.TupleAxis;
import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.grid.ReferenceableAxis;
import uk.ac.rdg.resc.edal.grid.RegularAxis;
import uk.ac.rdg.resc.edal.grid.TimeAxis;
import uk.ac.rdg.resc.edal.grid.VerticalAxis;
import uk.ac.rdg.resc.edal.position.VerticalCrs;

/**
 * 
 * @author Maik Riechert
 */
public class DomainWriter<T> {

    private final MapEncoder<T> map;

    public DomainWriter(MapEncoder<T> encoder) {
        this.map = encoder;
    }

    public void write(Domain domain) throws IOException {
        map.put(Keys.TYPE, Vals.DOMAIN);
        map.put(Keys.DOMAINTYPE, domain.domainType);

        MapEncoder<?> axes = map.startMap(Keys.AXES);
        writeAxes(axes, domain);
        axes.end();

        ArrayEncoder<?> referencing = map.startArray(Keys.REFERENCING);
        writeReferencing(referencing, domain);
        referencing.end();
    }

    private static void writeReferencing(ArrayEncoder<?> referencing, Domain domain) throws IOException {
        for (ReferenceSystemConnection refSysConnection : domain.refSystemConnections) {
            MapEncoder<?> connection = referencing.startMap();
            ArrayEncoder<?> coordinateIds = connection.startArray(Keys.COORDINATES);
            for (String coordinateId : refSysConnection.coordinates) {
                coordinateIds.add(coordinateId);
            }
            coordinateIds.end();

            MapEncoder<?> system = connection.startMap(Keys.SYSTEM);

            if (refSysConnection.system instanceof Chronology) {
                writeTemporalReferenceSystem(system, (Chronology) refSysConnection.system);
            } else if (refSysConnection.system instanceof VerticalCrs) {
                writeVerticalCRS(system, (VerticalCrs) refSysConnection.system);
            } else if (refSysConnection.system instanceof CoordinateReferenceSystem) {
                writeHorizontalCrs(system, (CoordinateReferenceSystem) refSysConnection.system);
            } else {
                throw new RuntimeException();
            }

            system.end();
            connection.end();
        }
    }

    private static void writeAxes(MapEncoder<?> axes, Domain domain) throws IOException {
        for (Entry<String, Axis> entry : domain.axes.entrySet()) {
            String axisName = entry.getKey();
            Axis axis = entry.getValue();
            MapEncoder<?> axisMap = axes.startMap(axisName);

            if (axis.wrappedAxis instanceof TupleAxis) {
                axisMap.put(Keys.DATATYPE, Vals.TUPLEDATATYPE);
            }

            if (axis.coordinateIds != null) {
                ArrayEncoder<?> comp = axisMap.startArray(Keys.COORDINATES);
                for (String coordinateId : axis.coordinateIds) {
                    comp.add(coordinateId);
                }
                comp.end();
            }

            if (axis.wrappedAxis instanceof ReferenceableAxis<?>) {
                ReferenceableAxis<?> refAxis = (ReferenceableAxis<?>) axis.wrappedAxis;
                writeReferenceableAxisValues(axisMap, refAxis);
            } else if (axis.wrappedAxis instanceof TupleAxis) {
                TupleAxis tupleAxis = (TupleAxis) axis.wrappedAxis;
                writeTupleAxisValues(axisMap, tupleAxis);
            } else {
                throw new RuntimeException("Unsupported axis type");
            }

            axisMap.end();
        }
    }

    private static void writeReferenceableAxisValues(MapEncoder<?> axisMap, ReferenceableAxis<?> axis)
            throws IOException {
        if (axis instanceof TimeAxis) {
            writeTimeAxisValues(axisMap, (TimeAxis) axis);
        } else if (axis instanceof VerticalAxis) {
            writeNumericAxisValues(axisMap, (VerticalAxis) axis);
        } else if (axis.getCoordinateValue(0) instanceof Double) {
            @SuppressWarnings("unchecked")
            ReferenceableAxis<Double> numericAxis = (ReferenceableAxis<Double>) axis;
            writeNumericAxisValues(axisMap, numericAxis);
        } else {
            throw new RuntimeException();
        }
    }

    private static void writeTupleAxisValues(MapEncoder<?> axisMap, TupleAxis axis) throws IOException {
        int axisSize = (int) axis.positions.size();

        ArrayEncoder<?> vals = axisMap.startArray(Keys.VALUES, new ArrayHints((long) axisSize, null));
        for (int i = 0; i < axisSize; i++) {
            ArrayEncoder<?> tuple = vals.startArray(new ArrayHints((long) (axis.hasZ ? 4 : 3), null));
            tuple.add(dateToString(axis.positions.get(i).getTime()));
            tuple.add(axis.positions.get(i).getHorizontalPosition().getX());
            tuple.add(axis.positions.get(i).getHorizontalPosition().getY());
            if (axis.hasZ) {
                tuple.add(axis.positions.get(i).getVerticalPosition().getZ());
            }
            tuple.end();
        }
        vals.end();
    }

    private static void writeNumericAxisValues(MapEncoder<?> axis, ReferenceableAxis<Double> ax) throws IOException {
        long size = ax.size();

        if (ax instanceof RegularAxis) {
            if (size == 1) {
                double halfstep = ((RegularAxis) ax).getCoordinateSpacing() / 2;
                double val = ax.getCoordinateValue(0);
                axis.startArray(Keys.VALUES).add(val).end().startArray(Keys.BOUNDS).add(val - halfstep)
                        .add(val + halfstep).end();
            } else {
                axis.put(Keys.START, ax.getCoordinateValue(0)).put(Keys.STOP, ax.getCoordinateValue(ax.size() - 1))
                        .put(Keys.NUM, size);
            }
        } else {
            ArrayEncoder<?> vals = axis.startArray(Keys.VALUES, new ArrayHints(size, Double.class));
            for (double val : ax.getCoordinateValues()) {
                vals.add(val);
            }
            vals.end();

            Extent<Double> testExtent = ax.getDomainObjects().get(0);
            boolean hasBounds = !testExtent.getLow().equals(testExtent.getHigh());

            if (hasBounds) {
                ArrayEncoder<?> bounds = axis.startArray(Keys.BOUNDS, new ArrayHints(size * 2, Double.class));
                for (Extent<Double> extent : ax.getDomainObjects()) {
                    bounds.add(extent.getLow());
                    bounds.add(extent.getHigh());
                }
                bounds.end();
            }
        }
    }

    private static void writeTimeAxisValues(MapEncoder<?> axis, TimeAxis t) throws IOException {
        ArrayEncoder<?> vals = axis.startArray(Keys.VALUES, new ArrayHints((long) t.size(), null));
        for (DateTime date : t.getCoordinateValues()) {
            vals.add(dateToString(date));
        }
        vals.end();

        Extent<DateTime> testExtent = t.getDomainObjects().get(0);
        boolean hasBounds = !testExtent.getLow().equals(testExtent.getHigh());

        if (hasBounds) {
            ArrayEncoder<?> bounds = axis.startArray(Keys.BOUNDS);
            for (Extent<DateTime> extent : t.getDomainObjects()) {
                bounds.add(dateToString(extent.getLow()));
                bounds.add(dateToString(extent.getHigh()));
            }
            bounds.end();
        }
    }

    private static String dateToString(DateTime date) {
        if (date.getMillis() == 0) {
            return date.toString(ISODateTimeFormat.dateTimeNoMillis());
        } else {
            return date.toString();
        }
    }

    private static void writeTemporalReferenceSystem(MapEncoder<?> system, Chronology chronology) throws IOException {
        // TODO what does Chronology actually tell us?
        system.put(Keys.TYPE, Vals.TEMPORALRS).put(Keys.CALENDAR, Vals.GREGORIAN);
    }

    private static void writeHorizontalCrs(MapEncoder<?> map, CoordinateReferenceSystem crs) throws IOException {
        String crsType;
        if (crs instanceof GeographicCRS) {
            crsType = Vals.GEOGRAPHICCRS;
        } else if (crs instanceof GeocentricCRS) {
            crsType = Vals.GEOCENTRICCRS;
        } else if (crs instanceof GeodeticCRS) {
            crsType = Vals.GEODETICCRS;
        } else if (crs instanceof ProjectedCRS) {
            crsType = Vals.PROJECTEDCRS;
        } else {
            throw new RuntimeException("Unsupported CRS type: " + crs.getClass().getSimpleName());
        }
        map.put(Keys.TYPE, crsType);

        if (crs instanceof DerivedCRS) {
            CoordinateReferenceSystem baseCrs = ((DerivedCRS) crs).getBaseCRS();
            MapEncoder<?> baseMap = map.startMap(Keys.BASECRS);
            writeHorizontalCrs(baseMap, baseCrs);
            baseMap.end();
        }

        String crsUri = Vals.getCrsUri(crs);
        if (crsUri != null) {
            map.put(Keys.ID, crsUri);
        }
    }

    private static void writeVerticalCRS(MapEncoder<?> system, VerticalCrs crs) throws IOException {
        String axisName = Vals.VERTICAL;
        if (crs.isPressure()) {
            axisName = Vals.PRESSURE;
        } else if ("m".equals(crs.getUnits())) {
            if (crs.isPositiveUpwards()) {
                axisName = Vals.HEIGHT;
            } else {
                axisName = Vals.DEPTH;
            }
        }
        system.put(Keys.TYPE, Vals.VERTICALCRS).startMap(Keys.CS).startArray(Keys.CSAXES).startMap().startMap(Keys.NAME)
                .put(Keys.EN, axisName).end().put(Keys.DIRECTION, crs.isPositiveUpwards() ? Vals.UP : Vals.DOWN)
                .startMap(Keys.UNIT).put(Keys.SYMBOL, crs.getUnits()).end().end().end().end();
    }
}
