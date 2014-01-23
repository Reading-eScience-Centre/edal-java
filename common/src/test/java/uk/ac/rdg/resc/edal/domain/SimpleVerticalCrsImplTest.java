package uk.ac.rdg.resc.edal.domain;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.Before;
import uk.ac.rdg.resc.edal.position.VerticalCrsImpl;

public class SimpleVerticalCrsImplTest {
	private SimpleVerticalDomain svd;
	
	@Before
	public void setUp()
	{
		VerticalCrsImpl height= new VerticalCrsImpl("meter", false, false, true);
		svd =new SimpleVerticalDomain(100.0, 1000.0, height);
	}
	
	@Test
	public void testContain() {
		assertTrue(svd.contains(500.0));
		assertTrue(svd.contains(100.0));
		assertTrue(svd.contains(1000.0));
		assertFalse(svd.contains(-500.0));
		assertFalse(svd.contains(1500.0));
		assertFalse(svd.contains(Double.NaN));
	}

}
