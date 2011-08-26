package uk.ac.rdg.resc.edal.coverage.grid.impl;

import java.util.List;

import org.opengis.referencing.cs.CoordinateSystemAxis;

import uk.ac.rdg.resc.edal.coverage.grid.VerticalAxis;
import uk.ac.rdg.resc.edal.position.VerticalCrs;
import uk.ac.rdg.resc.edal.position.VerticalPosition;

public class VerticalAxisImpl extends ReferenceableAxisImpl implements VerticalAxis {

    private VerticalCrs vCrs;
    
    public VerticalAxisImpl(CoordinateSystemAxis coordSysAxis, double[] values, VerticalCrs vCrs) {
        super(coordSysAxis, values, false);
        this.vCrs = vCrs;
    }
    
    public VerticalAxisImpl(String axisName, List<Double> values, VerticalCrs vCrs) {
        super(axisName, values, false);
        this.vCrs = vCrs;
    }

    public VerticalAxisImpl(CoordinateSystemAxis coordSysAxis, VerticalPosition[] values) {
        this(coordSysAxis, getValuesFromVerticalPositions(values), values[0].getCoordinateReferenceSystem());
    }

    private static double[] getValuesFromVerticalPositions(VerticalPosition[] positions) {
        double[] axisValues = new double[positions.length];
        for (int i = 0; i < positions.length; i++) {
            axisValues[i] = positions[i].getZ();
        }
        return axisValues;
    }

    @Override
    public VerticalCrs getVerticalCrs() {
        return vCrs;
    }

}
