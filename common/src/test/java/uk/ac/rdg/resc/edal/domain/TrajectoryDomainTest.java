package uk.ac.rdg.resc.edal.domain;

import static org.junit.Assert.*;

import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.junit.Test;
import org.junit.Before;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.joda.time.*;
import uk.ac.rdg.resc.edal.position.*;
import uk.ac.rdg.resc.edal.exceptions.*;

import java.util.*;

public class TrajectoryDomainTest {
	private final int SIZE=100;
	private List<GeoPosition> positions =new ArrayList<GeoPosition>(SIZE);
	private CoordinateReferenceSystem crs;
	private VerticalCrs height;
	private DateTime beginDate;
	private TrajectoryDomain tDomain;
	
	@Before
	public void setUp() throws MismatchedCrsException{
		crs= DefaultGeographicCRS.WGS84;		
		height= new VerticalCrsImpl("meter", false, false, true);
		beginDate =new DateTime(1990,5,8,0, 0);
		for(int i=0; i<SIZE; i++){
			HorizontalPosition hPos =new HorizontalPosition(10 +i*0.1, 20 +i*0.2, crs);
			VerticalPosition vPos =new VerticalPosition(100+ i*10.0, height);
			DateTime dt = beginDate.plusDays(i);
			positions.add(new GeoPosition(hPos, vPos, dt));
		}
		tDomain = new TrajectoryDomain(positions);
	}
	
	
	@Test
	public void testContains() {
		HorizontalPosition hPos =new HorizontalPosition(11.0, 22.0, crs);
		VerticalPosition vPos =new VerticalPosition(200.0, height);
		DateTime dt = beginDate.plusDays(10);
		assertTrue( tDomain.contains(new GeoPosition(hPos, vPos, dt)));
	}
}
