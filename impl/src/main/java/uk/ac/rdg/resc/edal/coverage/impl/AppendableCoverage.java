package uk.ac.rdg.resc.edal.coverage.impl;

import uk.ac.rdg.resc.edal.Phenomenon;
import uk.ac.rdg.resc.edal.Unit;
import uk.ac.rdg.resc.edal.coverage.Coverage;
import uk.ac.rdg.resc.edal.util.BigList;

public interface AppendableCoverage<P, D> extends Coverage<P>{
    public void addMember(String memberName, D domain, String description, Phenomenon parameter,
            Unit units, BigList<?> values, Class<?> valueType);
}
