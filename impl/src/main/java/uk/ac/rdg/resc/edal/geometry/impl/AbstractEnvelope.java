package uk.ac.rdg.resc.edal.geometry.impl;

import org.opengis.geometry.Envelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Partial implementation of the {@link Envelope} interface
 * 
 * @author Jon
 */
public abstract class AbstractEnvelope implements Envelope {
    private final CoordinateReferenceSystem crs;

    public AbstractEnvelope(CoordinateReferenceSystem crs) {
        this.crs = crs;
    }

    @Override
    public final double getMedian(int i) {
        return (this.getMinimum(i) + this.getMaximum(i)) / 2.0;
    }

    @Override
    public final double getSpan(int i) {
        return this.getMaximum(i) - this.getMinimum(i);
    }

    @Override
    public final CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return this.crs;
    }

}