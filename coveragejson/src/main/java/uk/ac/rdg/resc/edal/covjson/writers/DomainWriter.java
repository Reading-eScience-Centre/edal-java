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
import java.util.Arrays;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.DerivedCRS;
import org.opengis.referencing.crs.GeodeticCRS;
import org.opengis.referencing.crs.ProjectedCRS;

import uk.ac.rdg.resc.edal.covjson.StreamingEncoder.ArrayEncoder;
import uk.ac.rdg.resc.edal.covjson.StreamingEncoder.ArrayHints;
import uk.ac.rdg.resc.edal.covjson.StreamingEncoder.MapEncoder;
import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.domain.GridDomain;
import uk.ac.rdg.resc.edal.domain.TrajectoryDomain;
import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.feature.Feature;
import uk.ac.rdg.resc.edal.feature.GridFeature;
import uk.ac.rdg.resc.edal.feature.PointFeature;
import uk.ac.rdg.resc.edal.feature.ProfileFeature;
import uk.ac.rdg.resc.edal.feature.TrajectoryFeature;
import uk.ac.rdg.resc.edal.geometry.BoundingBoxImpl;
import uk.ac.rdg.resc.edal.grid.AbstractTransformedGrid;
import uk.ac.rdg.resc.edal.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.grid.RectilinearGrid;
import uk.ac.rdg.resc.edal.grid.ReferenceableAxis;
import uk.ac.rdg.resc.edal.grid.RegularGrid;
import uk.ac.rdg.resc.edal.grid.RegularGridImpl;
import uk.ac.rdg.resc.edal.grid.TimeAxis;
import uk.ac.rdg.resc.edal.grid.TimeAxisImpl;
import uk.ac.rdg.resc.edal.grid.VerticalAxis;
import uk.ac.rdg.resc.edal.grid.VerticalAxisImpl;
import uk.ac.rdg.resc.edal.position.GeoPosition;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.VerticalCrs;
import uk.ac.rdg.resc.edal.position.VerticalPosition;
import uk.ac.rdg.resc.edal.util.Array1D;

/**
 * 
 * @author Maik Riechert
 */
public class DomainWriter <T> {

	private final MapEncoder<T> map;

	public DomainWriter(MapEncoder<T> encoder) {
		this.map = encoder;
	}

	public void write(Feature<?> feature) throws IOException {
		map.put("type", "Domain");
		
		if (feature instanceof GridFeature) {
			doWrite((GridFeature) feature);
		} else if (feature instanceof ProfileFeature) {
			doWrite((ProfileFeature) feature);
		} else if (feature instanceof PointFeature) {
			doWrite((PointFeature) feature);
		} else if (feature instanceof TrajectoryFeature) {
			doWrite((TrajectoryFeature) feature);
		} else {
			throw new EdalException("Unsupported feature type: " + feature.getClass().getSimpleName());
		}
	}
	
	private void doWrite(GridFeature feature) throws IOException {
		map.put("profile", "Grid");
		
		GridDomain domain = feature.getDomain();
		VerticalAxis z = domain.getVerticalAxis();
		HorizontalGrid xy = domain.getHorizontalGrid();
		TimeAxis t = domain.getTimeAxis();
		
		MapEncoder<?> axes = map.startMap("axes");
		writeTimeAxis(axes, t);
		writeHorizontalAxes(axes, xy);
		writeVerticalAxis(axes, z);
		axes.end();
		
		ArrayEncoder<?> order = map.startArray("rangeAxisOrder");
		if (t != null) {
			order.add("t");
		}
		if (z != null) {
			order.add("z");
		}
		order.add("y").add("x");
		order.end();
		
		writeReferencing(t != null ? t.getChronology() : null, 
				xy.getCoordinateReferenceSystem(),
				z != null ? z.getVerticalCrs(): null);
	}

	private void doWrite(ProfileFeature feature) throws IOException {
		map.put("profile", "Profile");
		
		VerticalAxis z = feature.getDomain();
		HorizontalPosition xy = feature.getHorizontalPosition();
		DateTime time = feature.getTime();
		TimeAxis t = time != null ? new TimeAxisImpl("t", Arrays.asList(time)) : null;
		
		MapEncoder<?> axes = map.startMap("axes");
		writeTimeAxis(axes, t);
		writeHorizontalAxes(axes, xy);
		writeVerticalAxis(axes, z);
		axes.end();
		
		writeReferencing(t != null ? t.getChronology() : null,
				xy.getCoordinateReferenceSystem(),
				z.getVerticalCrs());
	}
	
	private void doWrite(PointFeature feature) throws IOException {
		map.put("profile", "Point");
		
		GeoPosition domain = feature.getGeoPosition();
		VerticalPosition zpos = domain.getVerticalPosition();
		VerticalAxis z = new VerticalAxisImpl("z", 
				Arrays.asList(zpos.getZ()), zpos.getCoordinateReferenceSystem());
		HorizontalPosition xy = domain.getHorizontalPosition();
		DateTime time = domain.getTime();
		TimeAxis t = time != null ? new TimeAxisImpl("time", Arrays.asList(domain.getTime())) : null;
		
		MapEncoder<?> axes = map.startMap("axes");
		writeTimeAxis(axes, t);
		writeHorizontalAxes(axes, xy);
		writeVerticalAxis(axes, z);
		axes.end();
		
		writeReferencing(t != null ? t.getChronology() : null, 
				xy.getCoordinateReferenceSystem(), 
				z != null ? z.getVerticalCrs() : null);
	}
	
	private void doWrite(TrajectoryFeature feature) throws IOException {
		map.put("profile", "Trajectory");
		
		TrajectoryDomain domain = feature.getDomain();
		Array1D<GeoPosition> points = domain.getDomainObjects();
		
		MapEncoder<MapEncoder<MapEncoder<T>>> axis = 
		  map.startMap("axes")
		    .startMap("composite")
		      .put("compositeType", "Simple");
		
		ArrayEncoder<?> comp = 
		  axis.startArray("components")
		    .add("t").add("x").add("y");
		boolean hasZ = domain.getVerticalCrs() != null;
		if (hasZ) {
			comp.add("z");
		}
		comp.end();
		
		ArrayEncoder<?> vals = axis.startArray("values", new ArrayHints((long) domain.size(), null));
		long componentSize = hasZ ? 4 : 3;
		for (GeoPosition point : points) {
			ArrayEncoder<?> val = vals.startArray(new ArrayHints(componentSize, null))
			  .add(dateToString(point.getTime()))
			  .add(point.getHorizontalPosition().getX())
			  .add(point.getHorizontalPosition().getY());
			if (hasZ) {
				val.add(point.getVerticalPosition().getZ());
			}
			val.end();
		}
		vals.end();
		
		axis.end().end();
		
		writeReferencing(domain.getChronology(), domain.getHorizontalCrs(), domain.getVerticalCrs());
	}
	
	private void writeReferencing(Chronology t, CoordinateReferenceSystem xy, VerticalCrs z) throws IOException {
		ArrayEncoder<?> ref = map.startArray("referencing");
		writeTimeReferencing(ref, t);
		writeHorizontalReferencing(ref, xy);
		writeVerticalReferencing(ref, z);
		ref.end();
	}
	
	private <P> void writeTimeAxis(MapEncoder<P> axes, TimeAxis t) throws IOException {
		if (t == null) return;
		
		MapEncoder<?> axis = axes.startMap("t");
		
		ArrayEncoder<?> vals = axis.startArray("values", new ArrayHints((long) t.size(), null));
		for (DateTime date : t.getCoordinateValues()) {
			vals.add(dateToString(date));
		}
		vals.end();
		
		Extent<DateTime> testExtent = t.getDomainObjects().get(0);
		boolean hasBounds = !testExtent.getLow().equals(testExtent.getHigh());
		
		if (hasBounds) {
			ArrayEncoder<?> bounds = axis.startArray("bounds");
			for (Extent<DateTime> extent : t.getDomainObjects()) {
				bounds.add(dateToString(extent.getLow()));
				bounds.add(dateToString(extent.getHigh()));
			}
			bounds.end();
		}
		
		axis.end();
	}
	
	private static String dateToString(DateTime date) {
		if (date.getMillis() == 0) {
			return date.toString(ISODateTimeFormat.dateTimeNoMillis());
		} else {
			return date.toString();
		}
	}
	
	private void writeTimeReferencing(ArrayEncoder<?> ref, Chronology chronology) throws IOException {
		if (chronology == null) return;
		// TODO what does Chronology actually tell us?
		ref.startMap()
		  .startArray("identifiers")
		    .add("t")
		  .end()
		  .startMap("trs")
		    .put("type", "TemporalRS")
		    .put("calendar", "Gregorian")
		  .end()
		.end();
	}
	
	private void writeHorizontalAxes(MapEncoder<?> axes, HorizontalGrid xy) throws IOException {
		if (xy instanceof RegularGrid) {
			writeHorizontalAxes(axes, (RegularGrid) xy);
		} else if (xy instanceof RectilinearGrid) {
			writeHorizontalAxes(axes, (RectilinearGrid) xy);
		} else if (xy instanceof AbstractTransformedGrid) {
			RegularGrid grid = new RegularGridImpl(
					new BoundingBoxImpl(0, 0, xy.getXSize()-1, xy.getYSize()-1, null),
					xy.getXSize(), xy.getYSize());
			writeHorizontalAxes(axes, grid);
		} else {
			throw new RuntimeException("Unknown grid type: " + xy.getClass().getSimpleName());
		}
	}
	
	private void writeHorizontalAxes(MapEncoder<?> axes, RegularGrid xy) throws IOException {
		// TODO bounds
		
		axes
		  .startMap("x")
		    .put("start", xy.getXAxis().getCoordinateValue(0))
		    .put("stop", xy.getXAxis().getCoordinateValue(xy.getXSize()-1))
		    .put("num", xy.getXSize())
		  .end()
		  .startMap("y")
		    .put("start", xy.getYAxis().getCoordinateValue(0))
		    .put("stop", xy.getYAxis().getCoordinateValue(xy.getYSize()-1))
		    .put("num", xy.getYSize())
		  .end();
	}
	
	private void writeHorizontalAxes(MapEncoder<?> axes, RectilinearGrid xy) throws IOException {
		writeNumericAxis(axes, "x", xy.getXAxis());
		writeNumericAxis(axes, "y", xy.getYAxis());
	}
	
	private void writeHorizontalAxes(MapEncoder<?> axes, HorizontalPosition xy) throws IOException {
		axes
		  .startMap("x")
		    .startArray("values").add(xy.getX()).end()
		  .end()
		  .startMap("y")
		    .startArray("values").add(xy.getY()).end()
		  .end()
		.end();
		
	}
	
	private <P> void  writeHorizontalReferencing (ArrayEncoder<P> ref, CoordinateReferenceSystem crs) throws IOException {
		MapEncoder<MapEncoder<ArrayEncoder<P>>> srs = ref.startMap()
		  .startArray("identifiers")
		    .add("x").add("y")
		  .end()
		  .startMap("srs");
		writeCrs(srs, crs);
		srs
		  .end()
		.end();
	}
	
	private void writeCrs(MapEncoder<?> map, CoordinateReferenceSystem crs) throws IOException {				
		String crsType;
		if (crs instanceof GeodeticCRS) {
			crsType = "GeodeticCRS";
		} else if (crs instanceof ProjectedCRS) {
			crsType = "ProjectedCRS";
		} else {
			throw new RuntimeException("Unsupported CRS type: " + crs.getClass().getSimpleName());
		}
		map.put("type", crsType);
		
		if (crs instanceof DerivedCRS) {
			CoordinateReferenceSystem baseCrs = ((DerivedCRS) crs).getBaseCRS();
			MapEncoder<?> baseMap = map.startMap("baseCRS");
			writeCrs(baseMap, baseCrs);
			baseMap.end();
		}
		
		String crsUri = Util.getCrsUri(crs);
		if (crsUri != null) {
			map.put("id", crsUri);
		}
	}
	
	private void writeVerticalAxis(MapEncoder<?> axes, VerticalAxis z) throws IOException {
		writeNumericAxis(axes, "z", z);
	}
	
	private void writeNumericAxis(MapEncoder<?> axes, String key, ReferenceableAxis<Double> ax) throws IOException {
		if (ax == null) return;
		
		MapEncoder<?> axis = axes.startMap(key);
		
		long size = ax.size();
		ArrayEncoder<?> vals = axis.startArray("values", new ArrayHints(size, Double.class));
		for (double val : ax.getCoordinateValues()) {
			vals.add(val);
		}
		vals.end();
		
		Extent<Double> testExtent = ax.getDomainObjects().get(0);
		boolean hasBounds = !testExtent.getLow().equals(testExtent.getHigh());
		
		if (hasBounds) {
			ArrayEncoder<?> bounds = axis.startArray("bounds", new ArrayHints(size*2, Double.class));
			for (Extent<Double> extent : ax.getDomainObjects()) {
				bounds.add(extent.getLow());
				bounds.add(extent.getHigh());
			}
			bounds.end();
		}
		
		axis.end();
	}
	
	private void writeVerticalReferencing(ArrayEncoder<?> ref, VerticalCrs crs) throws IOException {
		if (crs == null) return;

		String axisName = "Vertical";
		if (crs.isPressure()) {
			axisName = "Pressure";
		} else if ("m".equals(crs.getUnits())) {
			if (crs.isPositiveUpwards()) {
				axisName = "Height";
			} else {
				axisName = "Depth";
			}
		}
		ref.startMap()
		  .startArray("identifiers")
	        .add("z")
	      .end()
	      .startMap("srs")
	        .put("type", "VerticalCRS")
	        .startMap("cs")
	          .startArray("axes")
	            .startMap()
	              .startMap("name").put("en", axisName).end()
	              .put("direction", crs.isPositiveUpwards() ? "up" : "down")
	              .startMap("unit").put("symbol", crs.getUnits()).end()
				.end()
			  .end()
		    .end()
		  .end()
		.end();
	}
}
