package uk.ac.rdg.resc;

import java.util.ArrayList;
import java.util.List;

import uk.ac.rdg.resc.edal.Unit;
import uk.ac.rdg.resc.edal.coverage.grid.VerticalAxis;
import uk.ac.rdg.resc.edal.coverage.grid.impl.VerticalAxisImpl;
import uk.ac.rdg.resc.edal.position.VerticalCrs;
import uk.ac.rdg.resc.edal.position.VerticalCrs.PositiveDirection;
import uk.ac.rdg.resc.edal.position.impl.VerticalCrsImpl;

public class VerticalAxisTest {
    public static void main(String[] args) {
        VerticalCrs crs = new VerticalCrsImpl(Unit.getUnit("m"), PositiveDirection.DOWN, false);
        List<Double> vVals = new ArrayList<Double>();
        for (int i = 0; i < 10; i++) {
            vVals.add(i*1.0);
        }
        VerticalAxis vAxis = new VerticalAxisImpl("height", vVals, crs);
        System.out.println(vAxis.getCoordinateExtent());
        System.out.println(vAxis.getCoordinateBounds(0));
        
        for (Double zPos : vVals) {
            vAxis.findIndexOf(zPos);
            int index = vAxis.findIndexOf(zPos);
            System.out.println(index + ":" + zPos + ","
                    + vAxis.getCoordinateBounds(index).contains(zPos));
        }
        System.out.println();
        for(int i=0; i<10; i++){
            Double zPos = -1.0 + 0.2*i;
            int index = vAxis.findIndexOf(zPos);
            System.out.println(index + ":" + zPos);
        }
        for(int i=0; i<10; i++){
            Double zPos = 8.5 + 0.2*i;
            int index = vAxis.findIndexOf(zPos);
            System.out.println(index + ":" + zPos);
        }
    }

}
