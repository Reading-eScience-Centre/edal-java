package uk.ac.rdg.resc.edal.graphics;

import java.util.List;

import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates2D;

public class MultiPointFrameData extends FrameData {

    private List<GridCoordinates2D> coords;
    private List<Number> values;
    private int size;
    
    public MultiPointFrameData(PlotStyle plotStyle, List<GridCoordinates2D> coords,
            List<Number> values) {
        super(plotStyle);
        if(values.size() != coords.size()){
            throw new IllegalArgumentException("Values and coords must be the same size");
        }
        this.coords = coords;
        this.values = values;
        this.size = values.size();
    }
    
    public int size(){
        return size;
    }
    
    public PointFrameData getPointData(int i){
        if(i < 0 || i >= size){
            throw new IndexOutOfBoundsException();
        }
        Number n = values.get(i);
        return new PointFrameData(getPlotStyle(), coords.get(i).getXIndex(), coords.get(i)
                .getYIndex(), n != null ? n : Float.NaN);
    }
}
