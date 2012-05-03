/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.rdg.resc.edal.coverage.metadata;

import uk.ac.rdg.resc.edal.Phenomenon;

/**
 * A {@link RangeMetadata} object that describes a probability distribution as
 * a set of components.
 * @author Jon
 */
public interface ProbabilityDistribution extends RangeMetadata {
    
    /**
     * Returns an identifier describing the type of the distribution (e.g.
     * normal, Weibull etc).
     * @todo return a stronger type, e.g. from UncertML
     */
    public String getDistributionType();
    
    /**
     * Returns the metadata descriptor for the given parameter of the distribution.
     * This will always be a scalar quantity.
     */
    @Override
    public ScalarMetadata<?> getMemberMetadata(String parameter);
    
    /**
     * Returns the quantity being described by the probability distribution
     * function.
     * @todo Maybe the name need to change: "parameter" could be taken to mean
     * a parameter of a PDF.
     */
    public Phenomenon getParameter();
}
