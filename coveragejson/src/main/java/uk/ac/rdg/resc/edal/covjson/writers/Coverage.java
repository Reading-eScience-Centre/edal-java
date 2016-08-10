package uk.ac.rdg.resc.edal.covjson.writers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.covjson.writers.Constants.Keys;
import uk.ac.rdg.resc.edal.covjson.writers.Constants.Vals;
import uk.ac.rdg.resc.edal.domain.GridDomain;
import uk.ac.rdg.resc.edal.domain.TrajectoryDomain;
import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.feature.DiscreteFeature;
import uk.ac.rdg.resc.edal.feature.Feature;
import uk.ac.rdg.resc.edal.feature.GridFeature;
import uk.ac.rdg.resc.edal.feature.MapFeature;
import uk.ac.rdg.resc.edal.feature.PointFeature;
import uk.ac.rdg.resc.edal.feature.ProfileFeature;
import uk.ac.rdg.resc.edal.feature.TrajectoryFeature;
import uk.ac.rdg.resc.edal.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.grid.RectilinearGrid;
import uk.ac.rdg.resc.edal.grid.ReferenceableAxis;
import uk.ac.rdg.resc.edal.grid.ReferenceableAxisImpl;
import uk.ac.rdg.resc.edal.grid.TimeAxis;
import uk.ac.rdg.resc.edal.grid.TimeAxisImpl;
import uk.ac.rdg.resc.edal.grid.VerticalAxis;
import uk.ac.rdg.resc.edal.grid.VerticalAxisImpl;
import uk.ac.rdg.resc.edal.metadata.Parameter;
import uk.ac.rdg.resc.edal.position.GeoPosition;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.VerticalCrs;
import uk.ac.rdg.resc.edal.position.VerticalPosition;
import uk.ac.rdg.resc.edal.util.Array;
import uk.ac.rdg.resc.edal.util.Array1D;

/**
 * Wraps an EDAL Feature into a CoverageJSON compatible Coverage object.
 * 
 * @author Maik Riechert
 *
 */
public class Coverage {
	
	final DiscreteFeature<?,?> feature;
	
	Domain domain;
	Map<String, Parameter> parameters = new HashMap<>();
	Map<String, NdArray> ranges = new HashMap<>();
	
	static class Domain {
		String domainType;
		Map<String, Axis> axes;
		Set<ReferenceSystemConnection> refSystemConnections;
		public Domain(Map<String, Axis> axes, Set<ReferenceSystemConnection> refSystemConnections, String domainType) {
			this.axes = axes;
			this.refSystemConnections = refSystemConnections;
			this.domainType = domainType;
		}
	}
	
	static class Axis {
		/**
		 * Either ReferenceableAxis or TupleAxis.
		 */
		Object wrappedAxis;
		
		List<String> coordinateIds;
		
		public Axis(Object wrappedAxis, List<String> coordinateIds) {
			this.wrappedAxis = wrappedAxis;
			this.coordinateIds = coordinateIds;
		}
		
		int size() {
			if (this.wrappedAxis instanceof ReferenceableAxis<?>) {
				return ((ReferenceableAxis<?>) this.wrappedAxis).size();
			} else if (this.wrappedAxis instanceof TupleAxis) {
				return ((TupleAxis) this.wrappedAxis).size;
			} else {
				throw new RuntimeException("should not happen");
			}
		}
	}
	
	static class TupleAxis {
		/**
		 * A list of ReferenceableAxis objects.
		 * For example [t, x, y] which would be [TimeAxis, ReferenceableAxis, ReferenceableAxis].  
		 */
		List<ReferenceableAxis<?>> nestedAxes;
		int size;
		public TupleAxis(List<ReferenceableAxis<?>> nestedAxes, int size) {
			this.nestedAxes = nestedAxes;
			this.size = size;
		}
	}
	
	static class ReferenceSystemConnection {
		List<String> coordinates;
		Object system;
		public ReferenceSystemConnection(List<String> coordinateIds, Object system) {
			this.coordinates = coordinateIds;
			this.system = system;
		}
	}
		
	static class NdArray implements Iterable<Number> {
		static final List<String> axisOrder = Arrays.asList(Keys.T, Keys.Z, Keys.Y, Keys.X, Keys.COMPOSITE);
		
		List<String> axisNames = new LinkedList<>();
		List<Integer> shape = new LinkedList<>();
		int size = 0;
		DataType dataType;
		private Array<Number> values;
		
		public NdArray(DiscreteFeature<?,?> feature, String paramId, Domain domain) {
			int totalSize = 1;
			for (String axisKey : axisOrder) {
				if (domain.axes.containsKey(axisKey)) {
					axisNames.add(axisKey);
					int size = domain.axes.get(axisKey).size();
					shape.add(size);
					totalSize *= size;
				}
			}
			this.size = totalSize;
			
			Parameter param = feature.getParameter(paramId);
			boolean isCategorical = param.getCategories() != null;
			this.dataType = isCategorical ? DataType.Integer : DataType.Float;
			
			this.values = feature.getValues(paramId);
		}
		
		@Override
		public Iterator<Number> iterator() {
			// this relies on the fact that Array4D iterates in the order TZYX, otherwise we would have to wrap that
			// TODO add unit test to check that the order is not changed in some new release
			return this.values.iterator();
		}
	}
	
	enum DataType {
		Integer, Float
	}
	
	public Coverage(Feature<?> feature) {
		if (!(feature instanceof DiscreteFeature)) {
			throw new EdalException("Only discrete-type features are supported");
		}
		this.feature = (DiscreteFeature<?, ?>) feature;
		this.init();
	}

	private void init() {
		this.domain = getDomain(this.feature);
		
		for (Parameter param : Util.withoutParameterGroups(this.feature.getParameterMap().values(), this.feature)) {
			String key = param.getVariableId();
			this.parameters.put(key, param);
			this.ranges.put(key, new NdArray(this.feature, key, this.domain));
		}
	}

	private static Domain getDomain(DiscreteFeature<?, ?> feature) {
		if (feature instanceof GridFeature) {
			return doGetDomain((GridFeature) feature);
		} else if (feature instanceof MapFeature) {
			return doGetDomain((MapFeature) feature);
		} else if (feature instanceof ProfileFeature) {
			return doGetDomain((ProfileFeature) feature);
		} else if (feature instanceof PointFeature) {
			return doGetDomain((PointFeature) feature);
		} else if (feature instanceof TrajectoryFeature) {
			return doGetDomain((TrajectoryFeature) feature);
		} else {
			throw new EdalException("Unsupported feature type: " + feature.getClass().getSimpleName());
		}
	}
	
	private static Domain doGetDomain(MapFeature feature) {
		return doGetDomain(Util.convertToGridFeature(feature));
	}

	private static Domain doGetDomain(GridFeature feature) {
		GridDomain domain = feature.getDomain();
		VerticalAxis z = domain.getVerticalAxis();
		HorizontalGrid xy = domain.getHorizontalGrid();
		TimeAxis t = domain.getTimeAxis();
		
		RectilinearGrid rectGrid;
		if (xy instanceof RectilinearGrid) {
			rectGrid = (RectilinearGrid) xy;
		} else {
			throw new RuntimeException("Unsupported grid type: " + xy.getClass().getSimpleName());
		}
		ReferenceableAxis<?> x = rectGrid.getXAxis();
		ReferenceableAxis<?> y = rectGrid.getYAxis();
		
		Map<String, Axis> axes = getAxes(x, y, t, z);		
		Set<ReferenceSystemConnection> refSysConnections = getRefSysConnections(
				 xy.getCoordinateReferenceSystem(), t != null ? t.getChronology() : null, z != null ? z.getVerticalCrs() : null);
				
		return new Domain(axes, refSysConnections, Vals.GRID);
	}
	
	private static Domain doGetDomain(ProfileFeature feature) {
		VerticalAxis z = feature.getDomain();
		HorizontalPosition xy = feature.getHorizontalPosition();
		DateTime time = feature.getTime();
		
		TimeAxis t = time != null ? new TimeAxisImpl(Keys.T, Arrays.asList(time)) : null;
		ReferenceableAxis<?> x = new ReferenceableAxisImpl(Keys.X, Arrays.asList(xy.getX()), false);
		ReferenceableAxis<?> y = new ReferenceableAxisImpl(Keys.Y, Arrays.asList(xy.getY()), false);
		
		Map<String, Axis> axes = getAxes(x, y, t, z);		
		Set<ReferenceSystemConnection> refSysConnections = getRefSysConnections(
				 xy.getCoordinateReferenceSystem(), t != null ? t.getChronology() : null, z != null ? z.getVerticalCrs() : null);
		
		return new Domain(axes, refSysConnections, Vals.VERTICALPROFILE);
	}
	
	private static Domain doGetDomain(PointFeature feature) {
		GeoPosition domain = feature.getGeoPosition();
		VerticalPosition zpos = domain.getVerticalPosition();
		HorizontalPosition xy = domain.getHorizontalPosition();
		DateTime time = domain.getTime();
		
		VerticalAxis z = new VerticalAxisImpl(Keys.Z, 
				Arrays.asList(zpos.getZ()), zpos.getCoordinateReferenceSystem());
		TimeAxis t = time != null ? new TimeAxisImpl(Keys.T, Arrays.asList(domain.getTime())) : null;
		ReferenceableAxis<?> x = new ReferenceableAxisImpl(Keys.X, Arrays.asList(xy.getX()), false);
		ReferenceableAxis<?> y = new ReferenceableAxisImpl(Keys.Y, Arrays.asList(xy.getY()), false);

		Map<String, Axis> axes = getAxes(x, y, t, z);
		Set<ReferenceSystemConnection> refSysConnections = getRefSysConnections(
				 xy.getCoordinateReferenceSystem(), t != null ? t.getChronology() : null, z != null ? z.getVerticalCrs() : null);
		
		return new Domain(axes, refSysConnections, Vals.POINT);
	}
	
	private static Domain doGetDomain(TrajectoryFeature feature) {
		TrajectoryDomain domain = feature.getDomain();

		Map<String, Axis> axes = getAxes(domain);
		Set<ReferenceSystemConnection> refSysConnections = getRefSysConnections(
				 domain.getHorizontalCrs(), domain.getChronology(), domain.getVerticalCrs());
		
		return new Domain(axes, refSysConnections, Vals.TRAJECTORY);
	}
	
	private static Set<ReferenceSystemConnection> getRefSysConnections(CoordinateReferenceSystem xy, Chronology t, VerticalCrs z) {
		Set<ReferenceSystemConnection> refSysConnections = new HashSet<>();
		refSysConnections.add(new ReferenceSystemConnection(Arrays.asList(Keys.X, Keys.Y), xy));
		if (t != null) {
			refSysConnections.add(new ReferenceSystemConnection(Arrays.asList(Keys.T), t));
		}
		if (z != null) {
			refSysConnections.add(new ReferenceSystemConnection(Arrays.asList(Keys.Z), z));
		}
		return refSysConnections;
	}
	
	private static Map<String, Axis> getAxes(ReferenceableAxis<?> x, ReferenceableAxis<?> y, TimeAxis t, VerticalAxis z) {
		Map<String, Axis> axes = new HashMap<>();
		axes.put(Keys.X, new Axis(x, null));
		axes.put(Keys.Y, new Axis(y, null));
		if (t != null) {
			axes.put(Keys.T, new Axis(t, null));
		}
		if (z != null) {
			axes.put(Keys.Z, new Axis(z, null));
		}
		return axes;
	}
	
	private static Map<String, Axis> getAxes(TrajectoryDomain trajectoryDomain) {
		List<String> coordinateIds = new LinkedList<>();
		coordinateIds.add(Keys.T);
		coordinateIds.add(Keys.X);
		coordinateIds.add(Keys.Y);
		boolean hasZ = trajectoryDomain.getVerticalCrs() != null;
		if (hasZ) {
			coordinateIds.add(Keys.Z);
		}
		
		int size = trajectoryDomain.size();
		List<DateTime> ts = new ArrayList<>(size);
		List<Double> xs = new ArrayList<>(size);
		List<Double> ys = new ArrayList<>(size);
		List<Double> zs = hasZ ? new ArrayList<Double>(size) : null;
		
		Array1D<GeoPosition> positions = trajectoryDomain.getDomainObjects();
		for (GeoPosition pos : positions) {
			ts.add(pos.getTime());
			xs.add(pos.getHorizontalPosition().getX());
			ys.add(pos.getHorizontalPosition().getY());
			if (hasZ) {
				zs.add(pos.getVerticalPosition().getZ());	
			}
		}
		
		TimeAxis t = new TimeAxisImpl(Keys.T, ts);
		ReferenceableAxis<Double> x = new ReferenceableAxisImpl(Keys.X, xs, false);
		ReferenceableAxis<Double> y = new ReferenceableAxisImpl(Keys.Y, ys, false);
				
		List<ReferenceableAxis<?>> nestedAxes = new LinkedList<>();
		nestedAxes.add(t);
		nestedAxes.add(x);
		nestedAxes.add(y);
		if (hasZ) {
			ReferenceableAxis<Double> z = new ReferenceableAxisImpl(Keys.Z, zs, false);
			nestedAxes.add(z);
		}
		
		TupleAxis tupleAxis = new TupleAxis(nestedAxes, size);
		
		Map<String, Axis> axes = new HashMap<>();
		axes.put(Keys.COMPOSITE, new Axis(tupleAxis, coordinateIds));
		return axes;
	}
}
