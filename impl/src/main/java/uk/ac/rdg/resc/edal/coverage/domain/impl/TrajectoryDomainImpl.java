package uk.ac.rdg.resc.edal.coverage.domain.impl;

import java.util.List;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.Extent;
import uk.ac.rdg.resc.edal.coverage.domain.TrajectoryDomain;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.geometry.impl.BoundingBoxImpl;
import uk.ac.rdg.resc.edal.position.CalendarSystem;
import uk.ac.rdg.resc.edal.position.GeoPosition;
import uk.ac.rdg.resc.edal.position.TimePosition;
import uk.ac.rdg.resc.edal.position.VerticalCrs;
import uk.ac.rdg.resc.edal.position.VerticalPosition;
import uk.ac.rdg.resc.edal.util.Extents;

public class TrajectoryDomainImpl extends AbstractPointDomain<GeoPosition> implements TrajectoryDomain {

    private final CalendarSystem calSys;
    private final VerticalCrs vCrs;
    private final CoordinateReferenceSystem hCrs;
    private final BoundingBox bbox;
    private final Extent<TimePosition> tExtent;
    private final Extent<VerticalPosition> zExtent;

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
            double minx = Double.MAX_VALUE;
            double maxx = -Double.MAX_VALUE;
            double miny = Double.MAX_VALUE;
            double maxy = -Double.MAX_VALUE;
            VerticalPosition minz = null;
            VerticalPosition maxz = null;
            TimePosition mint = null;
            TimePosition maxt = null;
            
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
                
                double x = pos.getHorizontalPosition().getX();
                double y = pos.getHorizontalPosition().getY();
                VerticalPosition z = pos.getVerticalPosition();
                TimePosition t = pos.getTimePosition();
                if(x < minx){
                    minx = x;
                }
                if(x > maxx){
                    maxx = x;
                }
                if(y < miny){
                    miny = y;
                }
                if(y > maxy){
                    maxy = y;
                }
                if(minz == null || z.compareTo(minz) < 0){
                    minz = z;
                }
                if(maxz == null || z.compareTo(maxz) > 0){
                    maxz = z;
                }
                if(mint == null || t.compareTo(mint) < 0){
                    mint = t;
                }
                if(maxt == null || t.compareTo(maxt) > 0){
                    maxt = t;
                }
            }
            calSys = positions.get(0).getTimePosition().getCalendarSystem();
            vCrs = positions.get(0).getVerticalPosition().getCoordinateReferenceSystem();
            hCrs = positions.get(0).getHorizontalPosition().getCoordinateReferenceSystem();
            
            bbox = new BoundingBoxImpl(minx, miny, maxx, maxy, hCrs);
            zExtent = Extents.newExtent(minz, maxz);
            tExtent = Extents.newExtent(mint, maxt);
        } else {
            calSys = null;
            vCrs = null;
            hCrs = null;
            bbox = null;
            tExtent = null;
            zExtent = null;
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

    @Override
    public BoundingBox getCoordinateBounds() {
        return bbox;
    }

    @Override
    public Extent<TimePosition> getTimeExtent() {
        return tExtent;
    }

    @Override
    public Extent<VerticalPosition> getVerticalExtent() {
        return zExtent;
    }
}
