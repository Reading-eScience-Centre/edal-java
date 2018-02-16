package uk.ac.rdg.resc.edal.covjson.writers;

import java.util.Locale;

import org.geotoolkit.metadata.iso.citation.Citations;
import org.geotoolkit.referencing.IdentifiedObjects;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.FactoryException;

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
        public static final String DOMAINTYPE = "domainType";
        public static final String AXES = "axes";
        public static final String AXISNAMES = "axisNames";
        public static final String SHAPE = "shape";
        public static final String DATATYPE = "dataType";
        public static final String VALUES = "values";
        public static final String BOUNDS = "bounds";
        public static final String START = "start";
        public static final String STOP = "stop";
        public static final String NUM = "num";
        public static final String COORDINATES = "coordinates";
        public static final String REFERENCING = "referencing";
        public static final String SYSTEM = "system";
        public static final String CALENDAR = "calendar";
        public static final String BASECRS = "baseCRS";
        public static final String CS = "cs";
        public static final String CSAXES = "csAxes";
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
        public static final String GEOGRAPHICCRS = "GeographicCRS";
        public static final String GEOCENTRICCRS = "GeocentricCRS";
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
        public static final String NDARRAY = "NdArray";
        public static final String TUPLEDATATYPE = "tuple";
        public static final String INTEGER = "integer";
        public static final String FLOAT = "float";
        public static final String GRID = "Grid";
        public static final String POINTSERIES = "PointSeries";
        public static final String VERTICALPROFILE = "VerticalProfile";
        public static final String POINT = "Point";
        public static final String TRAJECTORY = "Trajectory";

        public static final String getStandardNameUri(String standardName) {
            return "http://vocab.nerc.ac.uk/standard_name/" + standardName + "/";
        }

        public static String getCrsUri(CoordinateReferenceSystem crs) {
            String crsUri;
            try {
                crsUri = IdentifiedObjects.lookupIdentifier(Citations.HTTP_OGC, crs, true);
            } catch (FactoryException e) {
                throw new RuntimeException(e);
            }
            if (crsUri != null && crsUri.startsWith("urn:ogc:")) {
                crsUri = "http://www.opengis.net/"
                        + crsUri.substring(8).replace(':', '/').toLowerCase(Locale.US);
            }
            return crsUri;
        }
    }

}
