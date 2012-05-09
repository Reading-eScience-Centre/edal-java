package uk.ac.rdg.resc;

import java.util.ArrayList;
import java.util.List;

import uk.ac.rdg.resc.edal.coverage.grid.TimeAxis;
import uk.ac.rdg.resc.edal.coverage.grid.impl.TimeAxisImpl;
import uk.ac.rdg.resc.edal.position.TimePosition;
import uk.ac.rdg.resc.edal.position.impl.TimePositionJoda;

public class TimeAxisTest {
    public static void main(String[] args) {
        List<TimePosition> tVals = new ArrayList<TimePosition>();
        for (int i = 0; i < 10; i++) {
            tVals.add(new TimePositionJoda(100000 + i * 10000));
        }
        TimeAxis tAxis = new TimeAxisImpl("time", tVals);
        for (TimePosition tPos : tVals) {
            int index = tAxis.findIndexOf(tPos);
            System.out.println(index + ":" + tPos + ","
                    + tAxis.getCoordinateBounds(index).contains(tPos));
        }
        System.out.println();
        for(int i=0; i<10; i++){
            TimePosition tPos = new TimePositionJoda(190000 + 1000*i);
            int index = tAxis.findIndexOf(tPos);
            System.out.println(index + ":" + tPos + "," + tAxis.getCoordinateBounds(0).contains(tPos));
        }
    }
}
