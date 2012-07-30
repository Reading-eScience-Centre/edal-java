package uk.ac.rdg.resc.edal.feature;

import uk.ac.rdg.resc.edal.coverage.TrajectoryCoverage;

/**
 * A {@link Feature} representing values over a 1-dimensional domain of
 * time-ordered points, where each point has a distinct value in 4-dimensional
 * space
 * 
 * @author Guy Griffiths
 * 
 */
public interface TrajectoryFeature extends Feature {

    @Override
    public TrajectoryCoverage getCoverage();
}
