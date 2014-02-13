package uk.ac.rdg.resc.edal.grid;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.util.Extents;

public class RegularAxisImplTest {
    private RegularAxis longSide;
    private RegularAxis latSide;
    private static final double SPACE = 1.0 / 3;
    private static final int LONGSIZE = 12;
    private static final int LATSIZE = 24;

    @Before
    public void setUp() throws Exception {
        longSide = new RegularAxisImpl("longitude", 100, SPACE, LONGSIZE, true);
        latSide = new RegularAxisImpl("latitude", 20, SPACE, LATSIZE, false);
    }

    @Test
    public void testGetCoordinateValue() {
        for (int i = 0; i < LONGSIZE; i++) {
            Double longPoint = longSide.getCoordinateValue(i);
            Double expected = 100.0 + i * SPACE;
            assertEquals(longPoint, expected);
        }
        for (int i = 0; i < LATSIZE; i++) {
            Double latPoint = latSide.getCoordinateValue(i);
            Double expected = 20.0 + i * SPACE;
            assertEquals(latPoint, expected);
        }
    }

    @Test
    public void testGetCoordinateBounds() {
        for (int i = 0; i < LONGSIZE; i++) {
            Extent<Double> bound = longSide.getCoordinateBounds(i);
            Extent<Double> expected = Extents.newExtent(100.0 + (i - 0.5) * SPACE, 100.0
                    + (i + 0.5) * SPACE);
            assertEquals(bound.getLow(), expected.getLow(), 1e-6);
            assertEquals(bound.getHigh(), expected.getHigh(), 1e-6);
        }
        for (int i = 0; i < LATSIZE; i++) {
            Extent<Double> bound = latSide.getCoordinateBounds(i);
            Extent<Double> expected = Extents.newExtent(20.0 + (i - 0.5) * SPACE, 20.0 + (i + 0.5)
                    * SPACE);
            assertEquals(bound.getLow(), expected.getLow(), 1e-6);
            assertEquals(bound.getHigh(), expected.getHigh(), 1e-6);
        }
    }

    @Test
    public void testFindIndexOf() {
        assertEquals(-1, longSide.findIndexOf(0.0));
        assertEquals(-1, longSide.findIndexOf(105.0));
        assertEquals(-1, longSide.findIndexOf(null));
        assertEquals(-1, longSide.findIndexOf(Double.NaN));

        assertEquals(-1, latSide.findIndexOf(10.0));
        assertEquals(-1, latSide.findIndexOf(30.0));
        assertEquals(-1, latSide.findIndexOf(null));
        assertEquals(-1, latSide.findIndexOf(Double.NaN));

        double step = 1 / 5.0;

        for (int i = 0; i < (int) LONGSIZE * SPACE / step; i++) {
            int index = longSide.findIndexOf(100 + i * step);
            int expected = (int) Math.round(i * step / SPACE);
            assertEquals(index, expected);
        }
        for (int i = 0; i < (int) LATSIZE * SPACE / step; i++) {
            int index = latSide.findIndexOf(20 + i * step);
            int expected = (int) Math.round(i * step / SPACE);
            assertEquals(index, expected);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindIndexOfUnconstrainedException() {
        longSide.findIndexOfUnconstrained(null);
        latSide.findIndexOfUnconstrained(Double.NaN);
    }

    @Test
    public void testFindIndexOfUnconstrained() {
        double step = 1 / 5.0;
        for (int i = 0; i < 101; i++) {
            double position = -20.0 + i * step;
            int expect = (int) Math.round((position - 20.0) / SPACE);
            assertEquals(expect, latSide.findIndexOfUnconstrained(position));
        }
        for (int i = 0; i < 101; i++) {
            double position = -20.0 + i * step;
            int expect = (int) Math.round((position - 100.0) / SPACE);
            assertEquals(expect, longSide.findIndexOfUnconstrained(position));
        }
        for (int i = 0; i < 101; i++) {
            double position = 420.0 + i * step;
            int expect = (int) Math.round((position - 360.0 - 100.0) / SPACE);
            assertEquals(expect, longSide.findIndexOfUnconstrained(position));
        }
    }
}
