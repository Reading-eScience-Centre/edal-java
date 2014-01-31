package uk.ac.rdg.resc.edal.util;

import static org.junit.Assert.*;
import org.junit.Test;

import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import uk.ac.rdg.resc.edal.position.*;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class ImmutableArrayTest {
	private final int SIZE=5;
	
	@Test
	public void test() {
		CoordinateReferenceSystem crs= DefaultGeographicCRS.WGS84;		
		HorizontalPosition [] hPosArray =new HorizontalPosition [SIZE];
		for(int i=0; i<SIZE; i++){
			HorizontalPosition hPos = new HorizontalPosition(100.0 +i*0.2, 120.0+i*0.3, crs);
			hPosArray [i] =hPos;
		}
		ImmutableArray1D<HorizontalPosition> hArray = new ImmutableArray1D<HorizontalPosition>(hPosArray);
		assertEquals(hArray.get(3), hPosArray[3]);
		
		Integer [] data ={1, 2, 3, 4, 5,};
		ImmutableArray1D<Integer> iArray = new ImmutableArray1D<Integer>(data);
		assertEquals(iArray.get(2), data[2]);
	}

}
