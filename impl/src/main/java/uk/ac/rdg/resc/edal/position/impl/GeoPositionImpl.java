package uk.ac.rdg.resc.edal.position.impl;

import uk.ac.rdg.resc.edal.position.GeoPosition;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.TimePosition;
import uk.ac.rdg.resc.edal.position.VerticalCrs;
import uk.ac.rdg.resc.edal.position.VerticalPosition;

/**
 * Implementation of {@link GeoPosition} that is immutable if the CRS of the
 * {@link HorizontalPosition} and the {@link VerticalPosition} are also
 * immutable
 * 
 * @author Guy Griffiths
 * 
 */
public class GeoPositionImpl implements GeoPosition {

    private final HorizontalPosition hPos;
    private final VerticalPosition vPos;
    private final TimePosition tPos;

    /**
     * Creates a new GeoPositionImpl with the given 4D coordinates
     * 
     * @param hPos
     *            The horizontal position
     * @param vPos
     *            The vertical position
     * @param tPos
     *            The time
     */
    public GeoPositionImpl(HorizontalPosition hPos, VerticalPosition vPos, TimePosition tPos) {
        this.hPos = hPos;
        this.vPos = vPos;
        this.tPos = tPos;
    }

    public GeoPositionImpl(HorizontalPosition hPos, Double vPos, VerticalCrs vCrs, TimePosition tPos) {
        this.hPos = hPos;
        this.vPos = new VerticalPositionImpl(vPos, vCrs);
        this.tPos = tPos;
    }

    @Override
    public HorizontalPosition getHorizontalPosition() {
        return hPos;
    }

    @Override
    public TimePosition getTimePosition() {
        return tPos;
    }

    @Override
    public VerticalPosition getVerticalPosition() {
        return vPos;
    }
}
