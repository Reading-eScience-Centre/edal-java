/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.rdg.resc.edal.coverage.metadata;

import uk.ac.rdg.resc.edal.Phenomenon;

/**
 * A descriptor for a statistical measure
 * @param <N> the type of the values used to describe the statistic
 * (will usually be Double, maybe occasionally Float or even BigDecimal).
 * @author Jon
 */
public interface Statistic extends ScalarMetadata {
    
    /**
     * Returns an identifier indicating the type of the statistic (mean, variance,
     * median etc).
     * @todo return a stronger type, e.g. from UncertML
     */
    public String getStatisticType();
    
    @Override
    public StatisticsCollection getParent();
    
    /**
     * {@inheritDoc}
     * <p>This will usually match the Phenomenon of the parent
     * {@link StatisticsCollection}.  However, in some vocabularies, different
     * statistical quantities may be expressed using different terms.</p>
     */
    @Override
    public Phenomenon getParameter();
    
}
