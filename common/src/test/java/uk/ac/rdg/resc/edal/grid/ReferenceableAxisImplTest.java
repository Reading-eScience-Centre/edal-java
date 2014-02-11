package uk.ac.rdg.resc.edal.grid;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import uk.ac.rdg.resc.edal.util.CollectionUtils;

public class ReferenceableAxisImplTest {

    private ReferenceableAxisImpl longAxis;
    private ReferenceableAxisImpl latAxis;
    private double[] latValues = { 20.0, 20.5, 20.8, 23.0, 24.0, 24.2, 24.3, 30.0 };
    private double[] longValues = { 50.0, 51.3, 53.9, 55.4, 57.9, 66.9, 74.9, 80.4 };

    @Before
    public void setUp() throws Exception {
        List<Double> longAxisValues = CollectionUtils.listFromDoubleArray(longValues);
        longAxis = new ReferenceableAxisImpl("longitude", longAxisValues, true);
        List<Double> latAxisValues = CollectionUtils.listFromDoubleArray(latValues);
        latAxis = new ReferenceableAxisImpl("latitude", latAxisValues, false);
    }

    @Test
    public void testFindIndexOf() {
        assertEquals(-1, latAxis.findIndexOf(15.0));
        assertEquals(-1, latAxis.findIndexOf(135.0));
        assertEquals(-1, longAxis.findIndexOf(45.0));
        assertEquals(-1, longAxis.findIndexOf(85.0));
        assertEquals(-1, longAxis.findIndexOf(null));
        assertEquals(-1, latAxis.findIndexOf(Double.NaN));

        assertEquals(1, latAxis.findIndexOf(20.4));
        assertEquals(0, latAxis.findIndexOf(20.2));
        assertEquals(3, latAxis.findIndexOf(23.5));

        assertEquals(6, longAxis.findIndexOf(75.0));
        assertEquals(1, longAxis.findIndexOf(51.3));
        assertEquals(4, longAxis.findIndexOf(58.0));
    }

}
