package uk.ac.rdg.resc.edal.domain;

import static org.junit.Assert.*;

import org.junit.Test;

import uk.ac.rdg.resc.edal.util.chronologies.NoLeapChronology;
import org.joda.time.DateTime;

public class SimpleTemporaDomainTest {

	@Test
	public void testContains() {
		NoLeapChronology nlc =NoLeapChronology.getInstanceUTC();
		DateTime start =new DateTime(2012, 1, 1, 0, 0);
		DateTime end =new DateTime(2012,12,31, 23, 59);
		SimpleTemporalDomain std = new SimpleTemporalDomain(start, end, nlc);
		DateTime dt1 =new DateTime(2012,2,29, 12,10);
		DateTime dt2 =new DateTime(2011,2,27, 12,10);
		DateTime dt3 =new DateTime(2012,2,27, 12,10);
		
		assertFalse(std.contains(dt1));
		assertFalse(std.contains(dt2));
		assertTrue(std.contains(dt3));
		
	}

}
