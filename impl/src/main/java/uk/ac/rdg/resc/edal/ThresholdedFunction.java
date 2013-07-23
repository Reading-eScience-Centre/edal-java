/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.rdg.resc.edal;

import java.util.Collections;
import java.util.List;

/**
 * <p>A total function that is defined by a number of <i>thresholds</i> and <i>values</i>.
 * The return value of the function is given by the following algorithm:</p>
 * <ul>
 * <li>If the input to the function is less than the first threshold (or if no
 * thresholds are set) then return the first value.</li>
 * <li>If the input to the function is greater than or equal to the first
 * threshold but less than the second threshold, then return the second value.</li>
 * <li>If the input to the function is greater than or equal to the second
 * threshold but less than the third threshold, then return the third value.</li>
 * <li>...etc </li>
 * <li>If the input to the function is greater than or equal to the last threshold,
 * return the last value.</li>
 * </ul>
 * <p>ThresholdedFunctions may be created directly using this class's constructor,
 * or by using the {@link ThresholdedFunctionBuilder}.  The docs to 
 * {@link ThresholdedFunctionBuilder} contain an example that illustrates the
 * above algorithm.</p>
 * @author Jon
 */
public final class ThresholdedFunction<T extends Comparable<T>, V> implements Function<T,V> {
    
    private final List<T> thresholds;
    private final List<V> values;
    private final V valueForNull;
    
    /**
     * Creates a new ThresholdedFunction from the given lists of thresholds
     * and values.
     * @param thresholds List of thresholds, must be in ascending order according
     * to the natural ordering of its elements.
     * @param values List of values, size must be one greater than the list of 
     * thresholds.
     * @param valueForNull The value to be returned if the input to the function
     * is null
     * @throws IllegalArgumentException if the list of values does not have exactly
     * one more element than the list of thresholds.
     */
    public ThresholdedFunction(List<T> thresholds, List<V> values, V valueForNull)
    {
        if (thresholds == null || values == null) {
            throw new IllegalArgumentException("Thresholds and values cannot be null");
        }
        // Consistency check: there must be one more value than threshold
        if (values.size() != thresholds.size() + 1) {
            throw new IllegalArgumentException("There must be exactly one more value than threshold");
        }
        this.thresholds = thresholds;
        this.values = values;
        this.valueForNull = valueForNull;
    }
    
    /**
     * Creates a new ThresholdedFunction from the given lists of thresholds
     * and values.  Null input will result in null values.
     * @param thresholds List of thresholds, must be in ascending order according
     * to the natural ordering of its elements.  If this is empty, the function will
     * always return the same value for non-null input.
     * @param values List of values, size must be one greater than the list of 
     * thresholds.
     * @throws IllegalArgumentException if the list of values does not have exactly
     * one more element than the list of thresholds.
     */
    public ThresholdedFunction(List<T> thresholds, List<V> values)
    {
        this(thresholds, values, null);
    }
    
    /**
     * A ThresholdedFunction can be evaluated for all values of its input (of
     * the correct type) hence this domain always returns true from its
     * contains() function.
     * @return 
     */
    @Override
    public Domain<T> getDomain() {
        return Domains.totalDomain();
    }

    @Override
    public V evaluate(T input)
    {
        if (input == null) return this.valueForNull;
        
        // If we haven't set any thresholds we'll always return the same value
        if (this.thresholds.isEmpty())
        {
            return values.get(0);
        }
        // Find the lowest threshold that the given value is greater
        // than or equal to
        int index = Collections.binarySearch(this.thresholds, input);
        if (index >= 0)
        {
            // The value exactly matches one of the thresholds.
            return this.values.get(index + 1);
        }
        else
        {
            // index = (-(insertion point) - 1)
            int insertionPoint = -(index + 1);
            // insertionPoint is the index of the first threshold greater than
            //     the input value.
            return this.values.get(insertionPoint);
        }
    }
    
    @Override
    public int hashCode() {
        int hash = 17;
        if (this.valueForNull != null) {
            hash = hash * 31 + this.valueForNull.hashCode();
        }
        hash = hash * 31 + this.thresholds.hashCode();
        hash = hash * 31 + this.values.hashCode();
        return hash;
    }
    
    /**
     * Two ThresholdedFunctions will be equal if they have exactly the same
     * thresholds and values.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof ThresholdedFunction)) return false;
        ThresholdedFunction other = (ThresholdedFunction)obj;
        return this.valueForNull == null ? other.valueForNull == null : this.valueForNull.equals(other.valueForNull) &&
               this.thresholds.equals(other.thresholds) &&
               this.values.equals(other.values);
    }
}
