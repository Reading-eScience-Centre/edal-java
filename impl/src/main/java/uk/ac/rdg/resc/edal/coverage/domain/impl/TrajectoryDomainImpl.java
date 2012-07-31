package uk.ac.rdg.resc.edal.coverage.domain.impl;

import java.util.List;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.coverage.domain.TrajectoryDomain;
import uk.ac.rdg.resc.edal.position.CalendarSystem;
import uk.ac.rdg.resc.edal.position.GeoPosition;
import uk.ac.rdg.resc.edal.position.VerticalCrs;

public class TrajectoryDomainImpl extends AbstractPointDomain<GeoPosition> implements TrajectoryDomain {

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
        super(positions);
        if(positions != null){
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
    public VerticalCrs getVerticalCrs() {
        return vCrs;
    }

    @Override
    public CoordinateReferenceSystem getHorizontalCrs() {
        return hCrs;
    }
}
