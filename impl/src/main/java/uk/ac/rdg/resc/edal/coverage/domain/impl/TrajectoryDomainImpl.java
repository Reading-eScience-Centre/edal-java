package uk.ac.rdg.resc.edal.coverage.domain.impl;

import java.util.AbstractList;
import java.util.Collections;
import java.util.List;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.coverage.domain.TrajectoryDomain;
import uk.ac.rdg.resc.edal.position.CalendarSystem;
import uk.ac.rdg.resc.edal.position.GeoPosition;
import uk.ac.rdg.resc.edal.position.TimePosition;
import uk.ac.rdg.resc.edal.position.VerticalCrs;

public class TrajectoryDomainImpl implements TrajectoryDomain {

    private final List<GeoPosition> positions;
    private final CalendarSystem calSys;
    private final VerticalCrs vCrs;
    private final CoordinateReferenceSystem hCrs;

    /**
     * Instantiates a new {@link TrajectoryDomain} from a {@link List} of
     * {@link GeoPosition}s
     * 
     * @param positions
     *            the {@link List} of {@link GeoPosition}s which make up the domain
     */
    public TrajectoryDomainImpl(List<GeoPosition> positions) {
        if(positions != null){
            this.positions = positions;
            long lastTime = 0L;
            if (positions.size() == 0) {
                throw new IllegalArgumentException(
                        "Must have at least one position for a TrajectoryDomain");
            }
            for (GeoPosition pos : positions) {
                if (pos.getTimePosition().getValue() < lastTime) {
                    throw new IllegalArgumentException("List of times must be in ascending order");
                }
                lastTime = pos.getTimePosition().getValue();
            }
            calSys = positions.get(0).getTimePosition().getCalendarSystem();
            vCrs = positions.get(0).getVerticalPosition().getCoordinateReferenceSystem();
            hCrs = positions.get(0).getHorizontalPosition().getCoordinateReferenceSystem();
        } else {
            /*
             * We may want an empty trajectory domain
             */
            this.positions = Collections.emptyList();
            calSys = null;
            vCrs = null;
            hCrs = null;
        }
    }

    @Override
    public CalendarSystem getCalendarSystem() {
        return calSys;
    }

    @Override
    public List<GeoPosition> getDomainObjects() {
        return positions;
    }

    @Override
    public long findIndexOf(GeoPosition pos) {
        /*
         * For a trajectory domain, we want to treat this as a time axis.
         */
        TimePosition time = pos.getTimePosition();
        int index = Collections.binarySearch(new AbstractList<TimePosition>() {
            @Override
            public TimePosition get(int index) {
                if(positions.get(index) == null)
                    return null;
                else return positions.get(index).getTimePosition();
            }

            @Override
            public int size() {
                return positions.size();
            }
        }, time);
        if (index >= 0) {
            return index;
        } else {
            int insertionPoint = -(index + 1);
            if (insertionPoint == positions.size() || insertionPoint == 0) {
                return -1;
            }
            if (Math.abs(positions.get(insertionPoint).getTimePosition().getValue()
                    - time.getValue()) < Math.abs(positions.get(insertionPoint - 1)
                    .getTimePosition().getValue()
                    - time.getValue())) {
                return insertionPoint;
            } else {
                return insertionPoint - 1;
            }
        }
    }

    @Override
    public VerticalCrs getVerticalCrs() {
        return vCrs;
    }

    @Override
    public CoordinateReferenceSystem getHorizontalCrs() {
        return hCrs;
    }

    @Override
    public boolean contains(GeoPosition position) {
        return positions.contains(position);
    }

    @Override
    public long size() {
        return positions.size();
    }

}
