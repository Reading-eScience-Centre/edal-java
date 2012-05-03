/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.rdg.resc.edal.coverage.metadata;

import uk.ac.rdg.resc.edal.Phenomenon;

/**
 * A descriptor for a component of a {@link ProbabilityDistribution}.
 * @param <N> the type of the values used to describe the component
 * (will usually be Double, maybe occasionally Float or even BigDecimal).
 * @author Jon
 */
public interface PdfComponent<N extends Number> extends ScalarMetadata<N> {
    
    /**
     * Returns an identifier indicating the type of the component (mean, variance,
     * etc).
     * @todo return a stronger type, e.g. from UncertML
     */
    public String getComponentType();
    
    @Override
    public ProbabilityDistribution getParent();
    
    /**
     * {@inheritDoc}
     * <p>This will usually match the Phenomenon of the parent
     * {@link ProbabilityDistribution}.  However, in some vocabularies, different
     * components may be expressed using different terms.</p>
     */
    @Override
    public Phenomenon getParameter();
    
}
