/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.rdg.resc.edal.coverage.metadata;

/**
 * <p>A metadata descriptor that contains children that are a set of realisations.
 * A realisation is a single instance of a random variable and can be used to
 * mean an observed value, or, as more widely used in 
 * UncertML, a single draw from a probability distribution.</p>
 * <p>Each child will have the same RangeMetadata type, although this is not
 * enforced in this interface.  Children will often be scalars, but could be
 * any other type too.</p>
 * @see http://www.uncertml.org/samples/realisation
 * @author Jon
 */
public interface Realisations extends RangeMetadata {
    
}
