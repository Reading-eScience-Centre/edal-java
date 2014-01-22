package uk.ac.rdg.resc.edal.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class ValuesArray2DTest {

	@Test
	public void test() {
		ValuesArray2D data = new ValuesArray2D(3, 4);
		data.set(12.0f, 2, 3);
		assertEquals(data.get(2,3), new Float(12));
		data.set(12L, 1, 3);
		assertEquals(data.get(1,3), new Long(12));
		data.set(5, 2,2);
		assertEquals(data.get(2, 2), new Integer(5));
		data.set(100.0, 1, 2);
		assertEquals(data.get(1, 2), new Double(100.0));
	}
	

}
