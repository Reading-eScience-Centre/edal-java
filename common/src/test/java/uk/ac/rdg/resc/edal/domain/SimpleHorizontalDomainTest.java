package uk.ac.rdg.resc.edal.domain;

import static org.junit.Assert.*;

import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.junit.Test;

import uk.ac.rdg.resc.edal.position.HorizontalPosition;

public class SimpleHorizontalDomainTest {

	@Test
	public void testContains() {
		SimpleHorizontalDomain sdm = new SimpleHorizontalDomain(120, 30, 150, 40);
		HorizontalPosition position =new HorizontalPosition (125, 32, DefaultGeographicCRS.WGS84);
		assertEquals(sdm.contains(position), true);
		
		position =new HorizontalPosition (125, 42, DefaultGeographicCRS.WGS84);
		assertFalse(sdm.contains(position));
		
		position =new HorizontalPosition (305, 32, DefaultGeographicCRS.WGS84);
		assertFalse(sdm.contains(position));
		
		sdm = new SimpleHorizontalDomain(-80, -30, -10, -5);
		position =new HorizontalPosition (295, -20, DefaultGeographicCRS.WGS84);
		assertTrue(sdm.contains(position));

		position =new HorizontalPosition (-60, 20.7, DefaultGeographicCRS.WGS84);
		assertFalse(sdm.contains(position));
	}

}
