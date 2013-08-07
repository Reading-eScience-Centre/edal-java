package uk.ac.rdg.resc.edal.domain;

import org.joda.time.DateTime;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.grid.HorizontalGridImpl;
import uk.ac.rdg.resc.edal.grid.ReferenceableAxis;
import uk.ac.rdg.resc.edal.position.VerticalCrs;

/**
 * Implementation of a {@link MapDomain}
 *
 * @author Guy
 */
public class MapDomainImpl extends HorizontalGridImpl implements MapDomain {
    private Double z;
    private VerticalCrs vCrs;
    private DateTime time;
    
    public MapDomainImpl(HorizontalGrid hGrid, Double z, VerticalCrs vCrs, DateTime time) {
        super(hGrid.getXAxis(), hGrid.getYAxis(), hGrid.getCoordinateReferenceSystem());
        this.z = z;
        this.vCrs = vCrs;
        this.time = time;
    }

    public MapDomainImpl(ReferenceableAxis<Double> xAxis, ReferenceableAxis<Double> yAxis,
            CoordinateReferenceSystem crs, Double z, VerticalCrs vCrs, DateTime time) {
        super(xAxis, yAxis, crs);
        this.z = z;
        this.vCrs = vCrs;
        this.time = time;
    }

    @Override
    public Double getZ() {
        return z;
    }

    @Override
    public VerticalCrs getVerticalCrs() {
        return vCrs;
    }

    @Override
    public DateTime getTime() {
        return time;
    }
}
