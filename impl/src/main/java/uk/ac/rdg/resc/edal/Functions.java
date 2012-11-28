/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.rdg.resc.edal;

import java.util.AbstractList;
import java.util.List;
import java.util.Map;
import uk.ac.rdg.resc.edal.util.CollectionUtils;

/**
 * Static methods for creating certain common types of {@link Function}.
 * @author Jon
 */
public final class Functions {
    
    public Functions() { throw new AssertionError("Not instantiable"); }
    
    /**
     * Returns a total function whose return values are always the same
     */
    public static <T> Function<Object, T> constantFunction(final T value) {
        return new Function<Object, T>() {

            @Override public T evaluate(Object val) {
                return value;
            }

            @Override
            public Domain<Object> getDomain() {
                return Domains.totalDomain();
            }
            
        };
    }
    
    /**
     * Returns a total function whose values are the result of the calculation
     * {@code m * val + c}.  If the input to the function is null, the output
     * will also be null.
     */
    public static Function<Double, Double> linearFunction(final double m, final double c) {
        return new Function<Double, Double>() {

            @Override public Double evaluate(Double val) {
                if (val == null) { return null; }
                return m * val.doubleValue() + c;
            }

            @Override
            public Domain<Double> getDomain() {
                return Domains.totalDomain();
            }
            
        };
    }
    
    /**
     * <p>Returns a partial function whose values are taken from the given Map.
     * If the input to the function is a key in the Map, the function will
     * return the corresponding value, otherwise it will return null.</p>
     * <p>This method does not take a copy of the provided Map, therefore if the
     * Map is changed by other code, any changes will be reflected in this method.</p>
     * @todo Consider taking a copy of the provided Map for increased thread safety?
     */
    public static <K, V> Function<K, V> fromMap(final Map<K, V> map) {
        return new Function<K, V>() {

            @Override
            public V evaluate(K val) {
                return map.get(val);
            }

            @Override
            public Domain<K> getDomain() {
                return new Domain<K>() {
                    @Override public boolean contains(K key) {
                        return map.containsKey(key);
                    }
                };
            }
            
        };
    }
    
    /**
     * <p>Returns a List containing values that result from applying the given 
     * function to all the elements in the given List.</p>
     * <p>The function is applied lazily, i.e. only when get() is called on the
     * returned list.  If you want the values to be calculated up-front, call
     * {@link CollectionUtils#copy(java.util.Collection)} on the resulting List.</p>
     */
    public static <A, B> List<B> apply(final List<A> inputs, final Function<A, B> func) {
        return new AbstractList<B>() {

            @Override public B get(int index) {
                A input = inputs.get(index);
                return func.evaluate(input);
            }

            @Override public int size() {
                return inputs.size();
            }
            
        };
    }
}
