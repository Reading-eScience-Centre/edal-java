package uk.ac.rdg.resc.edal.util;

import static org.junit.Assert.*;

import java.util.List;
import java.util.HashSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;

import uk.ac.rdg.resc.edal.util.CollectionUtils;

public class ColletionUtilsTest {
    private static int SIZE = 100;
    private float[] floatData = new float[SIZE];
    private Float[] floatDataAsObject = new Float[SIZE];
    private double[] doubleData = new double[SIZE];
    private Double[] doubleDataAsObject = new Double[SIZE];
    private Set<Double> doubleDataSet = new HashSet<Double>();
    private Set<Float> floatDataSet = new HashSet<Float>();

    @Before
    public void setUp() throws Exception {
        for (int i = 0; i < SIZE - 10; i++) {
            floatData[i] = 1.0f * i + 1.0f;
            doubleData[i] = 1.0 * i + 1.0;
            floatDataAsObject[i] = 1.0f * i + 1.0f;
            doubleDataAsObject[i] = 1.0 * i + 1.0;
            floatDataSet.add(1.0f * i + 1.0f);
            doubleDataSet.add(1.0 * i + 1.0);

        }
        floatDataSet.add(null);
        doubleDataSet.add(null);
    }

    @Test
    public void testListFromFloatArray() {
        List<Float> floatDataAsList = CollectionUtils.listFromFloatArray(floatData);
        assertEquals(floatData.length, floatDataAsList.size());
        for (int i = 0; i < floatData.length; i++) {
            assertEquals(floatDataAsList.get(i), floatData[i], 1e-6);
        }
    }

    @Test
    public void testListFromDoubleArray() {
        List<Double> doubleDataAsList = CollectionUtils.listFromDoubleArray(doubleData);
        assertEquals(doubleData.length, doubleDataAsList.size());
        for (int i = 0; i < doubleData.length; i++) {
            assertEquals(doubleDataAsList.get(i), new Double(doubleData[i]));
        }
    }

    @Test
    public void testSetOf() {
        Set<Float> expectedFloatSet = CollectionUtils.setOf(floatDataAsObject);
        Set<Double> expectedDoubleSet = CollectionUtils.setOf(doubleDataAsObject);
        assertEquals(expectedFloatSet, floatDataSet);
        assertEquals(expectedDoubleSet, doubleDataSet);
    }

    @Test(expected = NullPointerException.class)
    public void testNull() {
        CollectionUtils.listFromFloatArray(null);
        CollectionUtils.listFromDoubleArray(null);
    }
}
