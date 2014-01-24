package uk.ac.rdg.resc.edal.geometry;

import static org.junit.Assert.*;

import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.junit.Before;
import org.junit.Test;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import uk.ac.rdg.resc.edal.position.*;

public class BoundingBoxImplTest {
	private final CoordinateReferenceSystem crs =DefaultGeographicCRS.WGS84;
	private BoundingBoxImpl bbox;
	
	@Before
	public void setUp() throws Exception {
		bbox =new BoundingBoxImpl(150.0, -40.0, 180.0, -20.0, crs);
	}

	@Test
	public void testContains() {
		assertTrue(bbox.contains(160.0, -30.0));
		assertTrue(bbox.contains(150.0, -40.0));
		assertFalse(bbox.contains(190, -30.0));
		assertFalse(bbox.contains(Double.NaN, -30.0));
		assertTrue(bbox.contains(new HorizontalPosition(165.0, -23.0, crs)));
		assertTrue(bbox.contains(null));
	}

}
