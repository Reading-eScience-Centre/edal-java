package uk.ac.rdg.resc.edal.position.impl;

import uk.ac.rdg.resc.edal.Unit;
import uk.ac.rdg.resc.edal.position.VerticalCrs;

public class VerticalCrsImpl implements VerticalCrs {

    private final PositiveDirection posDir;
    private final Unit units;
    private final Boolean isPressure;
    
    public VerticalCrsImpl(Unit units, PositiveDirection posDir, boolean isPressure) {
        this.units = units;
        this.posDir = posDir;
        this.isPressure = isPressure;
    }
    
    @Override
    public PositiveDirection getPositiveDirection() {
        return posDir;
    }

    @Override
    public Unit getUnits() {
        return units;
    }

    @Override
    public boolean isDimensionless() {
        if(units == null)
            return true;
        else
            return units.getUnitString().equals("");
    }

    @Override
    public boolean isPressure() {
        return isPressure;
    }
}
