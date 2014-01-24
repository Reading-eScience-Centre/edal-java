package uk.ac.rdg.resc.edal.grid;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.util.*;

import org.joda.time.*; 
import org.joda.time.chrono.ISOChronology;

import uk.ac.rdg.resc.edal.domain.*;
import uk.ac.rdg.resc.edal.util.Extents;

public class TimeAxisTest {
	private TimeAxis tAxis;
	private Chronology chronology;
	
	@Before
	public void setUp() throws Exception {
		chronology =ISOChronology.getInstance();
		DateTime start =new DateTime(1999, 12,1,0,0, chronology);
		List<DateTime> datetimes =new ArrayList<DateTime>();
		for (int i=0; i<11; i++){
			DateTime dt = start.plusDays(2*i);
			datetimes.add(dt);
		}
		tAxis =new TimeAxisImpl("Sample TimeAxis", datetimes);
	}

	@Test
	public void test() {
		Extent<DateTime> dateBound =tAxis.getExtent();
		DateTime firstDate =new DateTime(1999, 12,1,0,0, chronology);;
		DateTime lastDate =firstDate.plusDays(20);
		Extent<DateTime> expected =Extents.newExtent(firstDate, lastDate);
		assertEquals(dateBound, expected);
		
		int n=4;
		DateTime fifthDate = tAxis.getCoordinateValue(n);
		assertEquals(tAxis.findIndexOf(fifthDate), n);
		
		assertTrue(tAxis.isAscending());
		
	}

}
