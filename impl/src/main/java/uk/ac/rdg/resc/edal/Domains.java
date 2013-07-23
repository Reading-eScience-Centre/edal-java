/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.rdg.resc.edal;

/**
 * Contains static methods for creating common types of {@link Domain}.
 * @author Jon
 */
public final class Domains {
    
    /** A Domain that contains all positions.  This is implemented as a
        singleton object. */
    @SuppressWarnings("unchecked")
    private static final Domain TOTAL_DOMAIN = new Domain() {
        @Override public boolean contains(Object position) {
            return true;
        }
    };
    
    public Domains() { throw new AssertionError("Not instantiable"); }
    
    /**
     * Returns a Domain that contains all values of the given type.  The
     * {@link Domain#contains(java.lang.Object) contains()} method of the domain
     * will always return true.
     */
    @SuppressWarnings("unchecked")
    public static <T> Domain<T> totalDomain() {
        return (Domain<T>)TOTAL_DOMAIN;
    }
    
    
    
}
