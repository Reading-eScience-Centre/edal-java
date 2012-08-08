package uk.ac.rdg.resc.edal.coverage.grid.impl;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import uk.ac.rdg.resc.edal.coverage.grid.Grid;
import uk.ac.rdg.resc.edal.coverage.grid.GridAxis;

/**
 * Test of conversions between indices and grid coordinates in the AbstractGrid
 * classes.
 * 
 * @author Jon
 */
public class AbstractGridTest {

    private final Grid grid = new AbstractGrid() {
        @Override
        public GridAxis getAxis(int n) {
            if (n == 0)
                return new GridAxisImpl("x", 3);
            if (n == 1)
                return new GridAxisImpl("y", 5);
            throw new IndexOutOfBoundsException();
        }

        @Override
        public int getNDim() {
            return 2;
        }

    };

    @Test
    public void coordsTest() {
        assertEquals(0, grid.getIndex(0, 0));
        assertEquals(1, grid.getIndex(1, 0));
        assertEquals(3, grid.getIndex(0, 1));
        assertEquals(14, grid.getIndex(2, 4));

        assertEquals(new GridCoordinates2DImpl(0, 0), grid.getCoords(0));
        assertEquals(new GridCoordinates2DImpl(1, 0), grid.getCoords(1));
        assertEquals(new GridCoordinates2DImpl(0, 1), grid.getCoords(3));
        assertEquals(new GridCoordinates2DImpl(2, 4), grid.getCoords(14));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void outOfRangeTest() {
        grid.getIndex(-1, 0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void outOfRangeTest2() {
        grid.getIndex(0, -1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void outOfRangeTest3() {
        grid.getIndex(3, 0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void outOfRangeTest4() {
        grid.getIndex(0, 5);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void outOfRangeTest5() {
        grid.getCoords(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void outOfRangeTest6() {
        grid.getCoords(15);
    }
}
