package uk.ac.rdg.resc.edal;


import java.util.Collections;
import java.util.List;
import static org.junit.Assert.*;
import org.junit.Test;
import uk.ac.rdg.resc.edal.util.CollectionUtils;


/**
 * Test of {@link ThresholdedFunction} and {@link ThresholdedFunctionBuilder}
 * @author Jon
 */
public class ThresholdedFunctionTest {
    
    private final Function<Double, String> fromBuilder;
    private final Function<Double, String> fromLists;
    
    private final List<Double> thresholds = CollectionUtils.listOf(0.0, 10.0, 20.0, 30.0);
    private final List<String> values  = CollectionUtils.listOf("very cold", "cold", "cool", "warm", "hot");
    
    public ThresholdedFunctionTest() {
        this.fromLists = new ThresholdedFunction(thresholds, values, "invalid data");
        this.fromBuilder = new ThresholdedFunctionBuilder<Double, String>()
                .value("very cold")
                .threshold(0.0)
                .value("cold")
                .threshold(10.0)
                .value("cool")
                .threshold(20.0)
                .value("warm")
                .threshold(30.0)
                .value("hot")
                .valueForNullInput("invalid data")
                .build();
    }
    
    @Test
    public void testEquality() {
        assertEquals(fromBuilder, fromLists);
        assertEquals(fromLists, fromBuilder);
        assertEquals(fromBuilder.hashCode(), fromLists.hashCode());
    
        // Create a function that's equal except for the value for null input
        Function<Double, String> equalExceptNull = new ThresholdedFunction<Double, String>(thresholds, values, null);
        assertFalse(equalExceptNull.equals(fromBuilder));
        assertFalse(fromBuilder.equals(equalExceptNull));
        assertFalse(equalExceptNull.hashCode() == fromBuilder.hashCode());
    
    }
    
    @Test
    public void testFunctions() {
        assertEquals("invalid data", fromBuilder.evaluate(null));
        assertEquals("very cold", fromBuilder.evaluate(-1.0));
        assertEquals("very cold", fromBuilder.evaluate(-1.0));
        assertEquals("very cold", fromBuilder.evaluate(-0.000000001));
        assertEquals("cold", fromBuilder.evaluate(0.0));
        assertEquals("cold", fromBuilder.evaluate(9.99999999));
        assertEquals("cool", fromBuilder.evaluate(10.0));
        assertEquals("cool", fromBuilder.evaluate(15.0));
        assertEquals("cool", fromBuilder.evaluate(19.9999999));
        assertEquals("warm", fromBuilder.evaluate(20.0));
        assertEquals("warm", fromBuilder.evaluate(20.0));
        assertEquals("warm", fromBuilder.evaluate(25.0));
        assertEquals("warm", fromBuilder.evaluate(29.99999999));
        assertEquals("hot", fromBuilder.evaluate(30.0));
        assertEquals("hot", fromBuilder.evaluate(100.0));
    }
    
    @Test
    public void testConstantFunction()
    {
        Function<Float, Integer> f1 = new ThresholdedFunctionBuilder<Float, Integer>()
             .value(10)
             .build();
        assertEquals(10, f1.evaluate(13.2f).intValue());
        assertEquals(10, f1.evaluate(-13.2f).intValue());
        assertEquals(10, f1.evaluate(0.0f).intValue());
        
        List<Float> empty = Collections.emptyList();
        Function<Float, Integer> f2 = new ThresholdedFunction<Float, Integer>(
                empty, CollectionUtils.listOf(10));
        assertEquals(10, f2.evaluate(13.2f).intValue());
        assertEquals(10, f2.evaluate(-13.2f).intValue());
        assertEquals(10, f2.evaluate(0.0f).intValue());
        
        assertEquals(f1, f2);
        assertEquals(f2, f1);
        assertEquals(f1.hashCode(), f2.hashCode());
    }
    
    @Test(expected = IllegalStateException.class)
    public void testIllegalBuilder()
    {
        new ThresholdedFunctionBuilder<Double, Double>()
                .threshold(1.0)
                .build();
    }
    
    @Test(expected = IllegalStateException.class)
    public void testIllegalBuilder2()
    {
        new ThresholdedFunctionBuilder<Double, Double>()
                .value(0.5)
                .threshold(1.0)
                .threshold(2.0)
                .build();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testIllegalBuilder3()
    {
        new ThresholdedFunctionBuilder<Double, Double>()
                .value(0.5)
                .threshold(1.0)
                .build();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testIllegalBuilder4()
    {
        new ThresholdedFunctionBuilder<Double, Double>()
                .value(0.5)
                .threshold(1.0)
                .value(15.0)
                .threshold(0.9) // out of sequence
                .value(11.0)
                .build();
    }
}
