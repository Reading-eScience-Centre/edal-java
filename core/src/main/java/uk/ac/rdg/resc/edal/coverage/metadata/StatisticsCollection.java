/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.rdg.resc.edal.coverage.metadata;

import uk.ac.rdg.resc.edal.Phenomenon;

/**
 * <p>A collection of statistics that describe a measurement.  No particular
 * probability distribution is implied by the members of the collection.  (For
 * example, this may hold the mean and variance of a sample, but this does not
 * imply that the probability distribution of the population is Gaussian.)</p>
 * <p>To describe a known probability distribution, use {@link ProbabilityDistribution}.</p>
 * @author Jon
 */
public interface StatisticsCollection extends RangeMetadata {
    
    /**
     * Returns the quantity being described by the collection of statistics.
     * @return 
     */
    public Phenomenon getParameter();
    
}
