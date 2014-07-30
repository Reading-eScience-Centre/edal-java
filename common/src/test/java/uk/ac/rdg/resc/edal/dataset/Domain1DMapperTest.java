package uk.ac.rdg.resc.edal.dataset;

import static org.junit.Assert.*;

import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.junit.Before;
import org.junit.Test;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.dataset.DomainMapper.DomainMapperEntry;
import uk.ac.rdg.resc.edal.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.grid.RectilinearGridImpl;
import uk.ac.rdg.resc.edal.grid.ReferenceableAxis;
import uk.ac.rdg.resc.edal.grid.RegularAxisImpl;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Domain1DMapperTest {
    // details about the grid
    private HorizontalGrid hGrid;
    private CoordinateReferenceSystem crs = DefaultGeographicCRS.WGS84;
    private int xSize = 160;
    private int ySize = 200;
    private double resolution = 1.0 / 4;
    private double leftLowXPos = 80.0;
    private double leftLowYPos = 0.0;
    
    // points are mapped onto a line, the line is defined as y=a*x+b
    private double a = -0.25;
    private double b = 40.0;
    
    private List<HorizontalPosition> targetPositions;
    private long expectedTartgetPosNumber = 0;

    private int expectedUniqueIJPair = 0;
    private int expectedminI = Integer.MAX_VALUE;
    private int expectedminJ = Integer.MAX_VALUE;
    private int expectedmaxI = Integer.MIN_VALUE;
    private int expectedmaxJ = Integer.MIN_VALUE;
    private Domain1DMapper mapper;
    /*
     * the container contains entries, each of them maps a (x,y) pair to points
     * on the line
     */
    Map<Pair, ArrayList<Integer>> mappings = new TreeMap<>();

    @Before
    public void setUp() throws Exception {
        ReferenceableAxis<Double> longAxis = new RegularAxisImpl("longitude", leftLowXPos
                + resolution / 2.0, resolution, xSize, true);
        ReferenceableAxis<Double> latAxis = new RegularAxisImpl("latitude", leftLowYPos
                + resolution / 2.0, resolution, ySize, false);
        hGrid = new RectilinearGridImpl(longAxis, latAxis, crs);

        targetPositions = new ArrayList<>();
        // x,y define the points on the line
        double x = 79.6;
        double y = 0.0;
        double xUpperBound = leftLowXPos + resolution * xSize;
        while (x < xUpperBound) {
            y = a * x + b;
            HorizontalPosition hPos = new HorizontalPosition(x, y, crs);
            if (hGrid.contains(hPos)) {
                targetPositions.add(hPos);
                expectedTartgetPosNumber++;
            }
            x += (resolution / 2.0);
        }
        ArrayList<Integer> targetIndices = new ArrayList<>();
        Pair p = null;
        boolean begin = true;
        int iPos = -1;
        int jPos = -1;

        for (int i = 0; i < targetPositions.size(); i++) {
            HorizontalPosition hPos = targetPositions.get(i);
            double xValue = hPos.getX();
            double yValue = hPos.getY();
            int iIndex = (new Double((xValue - leftLowXPos) / resolution)).intValue();
            int jIndex = (new Double(yValue / resolution)).intValue();

            if (iIndex < expectedminI)
                expectedminI = iIndex;
            if (iIndex > expectedmaxI)
                expectedmaxI = iIndex;
            if (jIndex < expectedminJ)
                expectedminJ = jIndex;
            if (jIndex > expectedmaxJ)
                expectedmaxJ = jIndex;
            if (iIndex == iPos && jIndex == jPos) {
                targetIndices.add(i);
            } else {
                iPos = iIndex;
                jPos = jIndex;
                expectedUniqueIJPair++;
                if (begin) {
                    p = new Pair(iIndex, jIndex);
                    targetIndices.add(i);
                    begin = false;
                } else {
                    mappings.put(p, targetIndices);
                    p = new Pair(iIndex, jIndex);
                    targetIndices = new ArrayList<>();
                    targetIndices.add(i);
                }
            }
        }
        //add the last mapping
        mappings.put(p, targetIndices);
        mapper = Domain1DMapper.forList(hGrid, targetPositions);
    }

    @Test
    public void testGetXXXmethods() {
        assertEquals(expectedUniqueIJPair, mapper.getNumUniqueIJPairs());
        assertEquals(expectedminI, mapper.getMinIIndex());
        assertEquals(expectedminJ, mapper.getMinJIndex());
        assertEquals(expectedmaxI, mapper.getMaxIIndex());
        assertEquals(expectedmaxJ, mapper.getMaxJIndex());
        assertEquals(expectedTartgetPosNumber, mapper.getTargetDomainSize());
    }

    @Test
    public void testIterator() {
        Pair[] keys = mappings.keySet().toArray(new Pair[0]);
        Iterator<DomainMapperEntry<Integer>> iterator = mapper.iterator();
        int lastKeyPos = keys.length - 1;
        while (iterator.hasNext()) {
            DomainMapperEntry<Integer> entry = iterator.next();
            List<Integer> targets = entry.getTargetIndices();
            int expectJ = keys[lastKeyPos].j;
            int expectI = keys[lastKeyPos--].i;
            assertEquals(expectJ, entry.getSourceGridJIndex());
            assertEquals(expectI, entry.getSourceGridIIndex());
            assertEquals(mappings.get(new Pair(expectI, expectJ)), targets);
        }
    }
    
    @Test
    public void testIsEmpty() {
        assertFalse(mapper.isEmpty());
    }
    
    class Pair implements Comparable<Pair> {
        final int i;
        final int j;

        Pair(final int i, final int j) {
            this.i = i;
            this.j = j;
        }

        Pair() {
            i = 0;
            j = 0;
        }

        public int compareTo(Pair p) {
            if (j < p.j) {
                return 1;
            } else if (j == p.j && i < p.i) {
                return 1;
            } else if (i == p.i && j == p.j) {
                return 0;
            } else {
                return -1;
            }
        }
    }
}
