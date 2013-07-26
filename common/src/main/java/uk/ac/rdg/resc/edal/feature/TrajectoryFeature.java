package uk.ac.rdg.resc.edal.feature;

import uk.ac.rdg.resc.edal.domain.TrajectoryDomain;
import uk.ac.rdg.resc.edal.position.GeoPosition;

/**
 * A {@link Feature} representing values over a 1-dimensional domain of
 * time-ordered points, where each point has a distinct value in 4-dimensional
 * space
 * 
 * @author Guy Griffiths
 * 
 */
public interface TrajectoryFeature extends DiscreteFeature<GeoPosition, GeoPosition>
{
    
    @Override
    public TrajectoryDomain getDomain();
    
}
