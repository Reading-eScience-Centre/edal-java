package uk.ac.rdg.resc.edal.grid;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import uk.ac.rdg.resc.edal.position.*;
import uk.ac.rdg.resc.edal.util.Extents;
import uk.ac.rdg.resc.edal.domain.*;

import java.util.*;

public class VerticalAxisImplTest {
	private VerticalCrs height= new VerticalCrsImpl("meter", false, false, true);
	private VerticalAxis vAxis;
	
	@Before
	public void setUp() throws Exception {
		List<Double> values =new ArrayList<Double>();
		for(int i=0; i<=100; i++){
			values.add(1000+ i*10.0);
		}
		vAxis =new VerticalAxisImpl("elevation", values, height);
	}

	@Test
	public void testGetExtent() {
		Extent<Double> elevationExtent =vAxis.getExtent();
		Extent<Double> expected =Extents.newExtent(1000.0, 2000.0);
		assertEquals(expected, elevationExtent);
	}

}
