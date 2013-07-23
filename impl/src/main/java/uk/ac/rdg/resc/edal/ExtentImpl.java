/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.rdg.resc.edal;

/**
 * A simple immutable implementation of an {@link Extent}.
 * @author Jon
 */
public class ExtentImpl<T extends Comparable<? super T>> implements Extent<T> {
    
    private T low = null;
    private T high = null;
    
    public ExtentImpl(T low, T high) {
        this.low = low;
        this.high = high;
    }

    @Override
    public boolean contains(T val) {
        if (low == null && high == null) {
            return true;
        }
        if (high == null) {
            // There is no upper bound.
            return val.compareTo(low) >= 0;
        }
        if (low == null) {
            // there is no lower bound
            return val.compareTo(high) <= 0;
        }
        return val.compareTo(low) >= 0 && val.compareTo(high) <= 0;
    }

    @Override
    public T getLow() {
        return this.low;
    }

    @Override
    public T getHigh() {
        return this.high;
    }
    
    // See definition in Extent interface
    @Override
    public int hashCode() {
        int hash = 17;
        if (low != null) {
           hash = hash * 31 + low.hashCode();
        }
        if (high != null) {
           hash = hash * 31 + high.hashCode();
        }
        return hash;
    }
    
    /**
     * The definition here allows ExtentImpl objects to be compared for equality
     * with any other implementation of the Extent interface.
     * @todo restrict this?
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof Extent)) return false;
        Extent other = (Extent)obj;
        boolean lowEquals = this.low == null ? other.getLow() == null : this.low.equals(other.getLow());
        boolean highEquals = this.high == null ? other.getHigh() == null : this.high.equals(other.getHigh());
        return lowEquals && highEquals;
    }
    
}
