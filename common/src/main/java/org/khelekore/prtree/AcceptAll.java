package org.khelekore.prtree;

/**
 * A filter that accepts all elements
 */
public class AcceptAll<T> implements NodeFilter<T> {
    public boolean accept(T t) {
        return true;
    }
}
