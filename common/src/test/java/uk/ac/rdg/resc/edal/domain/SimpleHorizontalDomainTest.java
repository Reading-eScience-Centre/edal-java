package uk.ac.rdg.resc.edal.domain;

import static org.junit.Assert.*;

import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.junit.Test;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.util.GISUtils;

public class SimpleHorizontalDomainTest {

	@Test
	public void testContains() throws Exception {
		SimpleHorizontalDomain sdm = new SimpleHorizontalDomain(120, 30, 150, 40);
		HorizontalPosition position = new HorizontalPosition (125, 32, DefaultGeographicCRS.WGS84);
		assertTrue(sdm.contains(position));
		
		position = new HorizontalPosition (120, 30, DefaultGeographicCRS.WGS84);
		assertTrue(sdm.contains(position));
		
		position = new HorizontalPosition (120, 40, DefaultGeographicCRS.WGS84);
        assertTrue(sdm.contains(position));
        
        position = new HorizontalPosition (150, 30, DefaultGeographicCRS.WGS84);
        assertTrue(sdm.contains(position));
        
        position = new HorizontalPosition (130, 35, DefaultGeographicCRS.WGS84);
        assertTrue(sdm.contains(position));
        
        position = new HorizontalPosition (150, 40, DefaultGeographicCRS.WGS84);
        assertTrue(sdm.contains(position));
        
        position = new HorizontalPosition (150, 35, DefaultGeographicCRS.WGS84);
        assertTrue(sdm.contains(position));
        
		position =new HorizontalPosition (125, 42, DefaultGeographicCRS.WGS84);
		assertFalse(sdm.contains(position));
		
		position =new HorizontalPosition (305, 32, DefaultGeographicCRS.WGS84);
		assertFalse(sdm.contains(position));
		
		position =new HorizontalPosition (295, -20, DefaultGeographicCRS.WGS84);
		assertFalse(sdm.contains(position));

		position =new HorizontalPosition (-60, 20.7, DefaultGeographicCRS.WGS84);
		assertFalse(sdm.contains(position));
		
		CoordinateReferenceSystem chinaArea =GISUtils.getCrs("EPSG:2426");
		assertFalse(sdm.contains(new HorizontalPosition(500000, 4261964.001513, chinaArea)));
		
		CoordinateReferenceSystem japanArea =GISUtils.getCrs("EPSG:2450");
        assertTrue(sdm.contains(new HorizontalPosition(17945.194292, 41625.344542, japanArea)));
    }

}
