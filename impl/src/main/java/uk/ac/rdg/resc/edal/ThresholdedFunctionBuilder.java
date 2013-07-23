/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.rdg.resc.edal;

import java.util.ArrayList;
import java.util.List;

/**
 * A Builder for {@link ThresholdedFunction}s.  Typical usage might be:
 * <pre>
 * Function<Double, String> func = new ThresholdedFunctionBuilder<Double, String>()
 *     .value("very cold")
 *     .threshold(0.0)
 *     .value("cold")
 *     .threshold(10.0)
 *     .value("cool")
 *     .threshold(20.0)
 *     .value("warm")
 *     .threshold(30.0)
 *     .value("hot")
 *     .build();
 * 
 * func(-5.0) == "very cold"
 * func(0.0) == "cold"
 * func(24.3) == "warm"
 * func(35.0) == "hot"
 * </pre>
 * Note that the first and last calls to the builder (apart from {@link #build()})
 * must be to {@link #value(java.lang.Object) value()}.
 * @author Jon
 */
public final class ThresholdedFunctionBuilder<T extends Comparable<T>, V>  {
    
    private final List<T> thresholds = new ArrayList<T>();
    private final List<V> values = new ArrayList<V>();
    private V valueForNull = null;
    
    public ThresholdedFunctionBuilder<T, V> value(V value) {
        // Consistency check
        if (values.size() != thresholds.size()) {
            throw new IllegalStateException("To add a value, there must be equal numbers of thresholds and values");
        }
        this.values.add(value);
        return this;
    }
    
    public ThresholdedFunctionBuilder<T, V> threshold(T threshold)
    {
        // Consistency checks
        if (values.size() != thresholds.size() + 1) {
            throw new IllegalStateException("To add a threshold, there must be exactly one more value than threshold");
        }
        if (!thresholds.isEmpty()) {
            T lastThreshold = thresholds.get(thresholds.size() - 1);
            if (threshold.compareTo(lastThreshold) <= 0) {
                throw new IllegalArgumentException("Threshold must be greater than the previous threshold");
            }
        }
        
        this.thresholds.add(threshold);
        return this;
    }
    
    /**
     * Sets the value to be returned in case of null input to the function.
     * If this is not set, the default is for the function to return null.
     */
    public ThresholdedFunctionBuilder<T, V> valueForNullInput(V value) {
        this.valueForNull = value;
        return this;
    }
    
    public Function<T, V> build()
    {
        return new ThresholdedFunction<T, V>(thresholds, values, valueForNull);
    }
    
}
