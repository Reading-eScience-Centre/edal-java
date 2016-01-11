package uk.ac.rdg.resc.edal.covjson.writers;

import java.io.IOException;

import org.geotoolkit.metadata.iso.citation.Citations;
import org.geotoolkit.referencing.IdentifiedObjects;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.FactoryException;

import uk.ac.rdg.resc.edal.covjson.StreamingEncoder.MapEncoder;

public class Util {
	public static final String CoverageJSONContext =
			"https://rawgit.com/reading-escience-centre/coveragejson/master/contexts/coveragejson-base.jsonld";
	
	public static <T> void  addJsonLdContext (MapEncoder<T> map) throws IOException {
		map.put("@context", CoverageJSONContext);
	}
	
	public static String getCrsUri(CoordinateReferenceSystem crs) {
		String crsUri;
		try {
			crsUri = IdentifiedObjects.lookupIdentifier(Citations.HTTP_OGC, crs, true);
			if (crsUri == null) {
				// geotoolkit doesn't return this URI yet
				if (crs.getName().toString() == "WGS84(DD)") {
					crsUri = "http://www.opengis.net/def/crs/OGC/1.3/CRS84";
				}
			}
		} catch (FactoryException e) {
			throw new RuntimeException(e); 
		}
		return crsUri;
	}
}
