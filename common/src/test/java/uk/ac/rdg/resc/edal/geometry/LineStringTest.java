package uk.ac.rdg.resc.edal.geometry;

import static org.junit.Assert.*;

import java.util.*;

import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.junit.Before;
import org.junit.Test;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.position.HorizontalPosition;

public class LineStringTest {
	private ArrayList<LineString> lineStrings =new ArrayList<LineString>();
	private final CoordinateReferenceSystem crs =DefaultGeographicCRS.WGS84;
	
	@Before
	public void setUp() throws Exception {
		String lineStringSpecOne="10 20,11 20,12 20,13 20,14 20,15 20,16 20,17 20,18 20,19 20,"
				                 +"20 20"; 
		String lineStringSpecTwo="10 20,11 21,12 20,13 21,14 20,15 21,16 20";
        String lineStringSpecThree="10 20,11 21,12 22,13 23,14 24,15 25,16 26";
		lineStrings.add(new LineString(lineStringSpecOne, crs));
		lineStrings.add(new LineString(lineStringSpecTwo, crs));
		lineStrings.add(new LineString(lineStringSpecThree, crs));
	}

	@Test
	public void testGetPointsOnPath(){
		int n=5;
		List<HorizontalPosition> fivePoints =lineStrings.get(0).getPointsOnPath(n);
		HorizontalPosition secondPoint= fivePoints.get(1);
		double xValue = secondPoint.getX();
		double pathLength =10.0;
		double expected = fivePoints.get(0).getX() + pathLength /(n-1);
		assertEquals(xValue, expected, 1e-6);
		
		n =3;
		List<HorizontalPosition> threePoints =lineStrings.get(1).getPointsOnPath(n);
		HorizontalPosition midPoint= threePoints.get(1);
		xValue = midPoint.getX();
		expected = 13.0;
		assertEquals(xValue, expected, 1e-6);
		

		threePoints =lineStrings.get(2).getPointsOnPath(3);
		midPoint= threePoints.get(1);
		xValue = midPoint.getX();
		expected = 13.0;
		assertEquals(xValue, expected, 1e-6);
	}
	
	@Test
	public void testgGetFractionalControlPointDistance(){
		int n=6;
		double fDistance = lineStrings.get(0).getFractionalControlPointDistance(n);
		double expected = (double) n/10;
		assertEquals(fDistance, expected, 1e-6);
		
		n=5;
		fDistance = lineStrings.get(1).getFractionalControlPointDistance(n);
		expected = n/6.0;
		assertEquals(fDistance, expected, 1e-6);
		
		n=3;
		fDistance = lineStrings.get(2).getFractionalControlPointDistance(n);
		expected = n/6.0;
		assertEquals(fDistance, expected, 1e-6);		
	}
}
