package uk.ac.rdg.resc.edal.util;

import static org.junit.Assert.*;

import org.junit.Test;
import uk.ac.rdg.resc.edal.domain.*;
import java.util.*;

public class ExtentsTest {

    @Test
    public void testFindMinMax() {
        List<Integer> intList = new ArrayList<Integer>();
        List<Double> doubleList = new LinkedList<Double>();
        Set<Float> floatSet = new HashSet<Float>();
        Vector<Long> longVector = new Vector<Long>();

        for (int i = 0; i < 10; i++) {
            intList.add(i);
            doubleList.add(i * 1.0);
            floatSet.add(i * 1.0f);
            longVector.add(i * 1L);
        }
        Extent<Integer> intExtent = Extents.newExtent(0, 9);
        Extent<Double> doubleExtent = Extents.newExtent(0.0, 9.0);
        Extent<Float> floatExtent = Extents.newExtent(0.0f, 9.0f);
        Extent<Long> longExtent = Extents.newExtent(0L, 9L);

        assertEquals(intExtent, Extents.findMinMax(intList));
        assertEquals(doubleExtent, Extents.findMinMax(doubleList));
        assertEquals(floatExtent, Extents.findMinMax(floatSet));
        assertEquals(longExtent, Extents.findMinMax(longVector));

        assertFalse(intExtent.contains(null));
    }

}
