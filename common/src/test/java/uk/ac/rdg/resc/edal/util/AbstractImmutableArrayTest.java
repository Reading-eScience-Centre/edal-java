package uk.ac.rdg.resc.edal.util;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class AbstractImmutableArrayTest {

    private Array<Float> array;

    private static final int XSIZE = 5;
    private static final int YSIZE = 7;

    @Before
    public void setUp() throws Exception {
        array = new AbstractImmutableArray<Float>(Float.class, YSIZE, XSIZE) {
            @Override
            public Float get(int... coords) {
                int x = coords[1];
                int y = coords[0];
                return y + 0.1f * x;
            }

            @Override
            public Class<Float> getValueClass() {
                return Float.class;
            }
        };

    }

    @Test
    public void testIterator() {
        /*
         * This tests that the iterator for an AbstractImmutableArray behaves
         * properly (correct values, and varies the final dimension fastest)
         */
        List<Float> expected = new ArrayList<Float>();
        for (int j = 0; j < YSIZE; j++) {
            for (int i = 0; i < XSIZE; i++) {
                expected.add(j + 0.1f * i);
            }
        }

        Iterator<Float> iterator = array.iterator();
        int index = 0;
        while (iterator.hasNext() && index < 1000) {
            assertEquals(expected.get(index), iterator.next());
            index++;
        }
    }
    
    @Test
    public void testGetValueClass() {
        assertEquals(Float.class, array.getValueClass());
    }
    
    @Test
    public void testGet() {
        /*
         * This tests that the get method returns the expected values
         */
        for (int j = 0; j < YSIZE; j++) {
            for (int i = 0; i < XSIZE; i++) {
                Float expected = j + 0.1f * i;
                assertEquals(expected, array.get(j, i));
            }
        }
    }
}
