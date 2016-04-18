package uk.ac.rdg.resc.edal.covjson.writers;

import org.geotoolkit.metadata.iso.citation.Citations;
import org.geotoolkit.referencing.IdentifiedObjects;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.FactoryException;

import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.feature.Feature;
import uk.ac.rdg.resc.edal.feature.GridFeature;
import uk.ac.rdg.resc.edal.feature.MapFeature;
import uk.ac.rdg.resc.edal.feature.PointFeature;
import uk.ac.rdg.resc.edal.feature.ProfileFeature;
import uk.ac.rdg.resc.edal.feature.TrajectoryFeature;

public final class Constants {
	
	/**
	 * Property names.
	 */
	public static final class Keys {			
		// Axis names
		public static final String X = "x";
		public static final String Y = "y";
		public static final String Z = "z";
		public static final String T = "t";
		public static final String COMPOSITE = "composite";
		
		// Other properties
		public static final String TYPE = "type";
		public static final String ID = "id";
		public static final String PROFILE = "profile";
		public static final String AXES = "axes";
		public static final String RANGEAXISORDER = "rangeAxisOrder";
		public static final String DATATYPE = "dataType";
		public static final String VALUES = "values";
		public static final String BOUNDS = "bounds";
		public static final String START = "start";
		public static final String STOP = "stop";
		public static final String NUM = "num";
		public static final String COMPONENTS = "components";		
		public static final String REFERENCING = "referencing";
		public static final String SYSTEM = "system";
		public static final String CALENDAR = "calendar";
		public static final String BASECRS = "baseCRS";
		public static final String CS = "cs";
		public static final String NAME = "name";
		public static final String EN = "en";
		public static final String DIRECTION = "direction";
		public static final String UNIT = "unit";
		public static final String SYMBOL = "symbol";
		public static final String DOMAIN = "domain";
		public static final String COVERAGES = "coverages";
		public static final String PARAMETERS = "parameters";
		public static final String RANGES = "ranges";
		public static final String DESCRIPTION = "description";
		public static final String LABEL = "label";
		public static final String OBSERVEDPROPERTY = "observedProperty";
		public static final String CATEGORIES = "categories";
		public static final String CATEGORYENCODING = "categoryEncoding";
		
		// Extension properties
		public static final String TITLE = "title";
		public static final String PREFERREDCOLOR = "preferredColor";
	
	}
	
	/**
	 * Property values.
	 */
	public static final class Vals {
		// Referencing system values
		public static final String TEMPORALRS = "TemporalRS";
		public static final String GREGORIAN = "Gregorian";
		public static final String GEODETICCRS = "GeodeticCRS";
		public static final String PROJECTEDCRS = "ProjectedCRS";
		public static final String VERTICALCRS = "VerticalCRS";
		public static final String UP = "up";
		public static final String DOWN = "down";
		public static final String CRS84 = "http://www.opengis.net/def/crs/OGC/1.3/CRS84";
		
		// Labels
		public static final String VERTICAL = "Vertical";
		public static final String PRESSURE = "Pressure";
		public static final String HEIGHT = "Height";
		public static final String DEPTH = "Depth";
		
		// Other values	
		public static final String COVERAGE = "Coverage";
		public static final String COVERAGECOLLECTION = "CoverageCollection";
		public static final String DOMAIN = "Domain";
		public static final String PARAMETER = "Parameter";
		public static final String RANGE = "Range";
		public static final String TUPLE = "Tuple";
		public static final String POLYGON = "Polygon";
		public static final String INTEGER = "integer";
		public static final String FLOAT = "float";
		public static final String GRID = "Grid";
		public static final String VERTICALPROFILE = "VerticalProfile";
		public static final String POINT = "Point";
		public static final String TRAJECTORY = "Trajectory";		
	
		public static final String getStandardNameUri (String standardName) {
			return "http://vocab.nerc.ac.uk/standard_name/" + standardName;
		}
		
		public static String getDomainProfile(Feature<?> feature) {
			if (feature instanceof GridFeature) {
				return GRID;
			} else if (feature instanceof MapFeature) {
				return GRID;
			} else if (feature instanceof ProfileFeature) {
				return VERTICALPROFILE;
			} else if (feature instanceof PointFeature) {
				return POINT;
			} else if (feature instanceof TrajectoryFeature) {
				return TRAJECTORY;
			} else {
				throw new EdalException("Unsupported feature type: " + feature.getClass().getSimpleName());
			}
		}
		
		public static String getCrsUri(CoordinateReferenceSystem crs) {
			String crsUri;
			try {
				crsUri = IdentifiedObjects.lookupIdentifier(Citations.HTTP_OGC, crs, true);
				if (crsUri == null) {
					// geotoolkit doesn't return this URI yet
					if (crs.getName().toString() == "WGS84(DD)") {
						crsUri = CRS84;
					}
				} else if (crsUri.equals("http://www.opengis.net/gml/srs/crs.xml#84")) {
					// TODO where is this weird URI coming from?!
					crsUri = CRS84;
				}
			} catch (FactoryException e) {
				throw new RuntimeException(e); 
			}
			return crsUri;
		}
	}
	
}
