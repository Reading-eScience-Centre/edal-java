package uk.ac.rdg.resc.edal.graphics;


public class GriddedFrameData extends FrameData {
    private Number[][] data;
    
    public GriddedFrameData(PlotStyle plotStyle, Number[][] data) {
        super(plotStyle);
        this.data = data;
    }

    public Number[][] getData() {
        return data;
    }
}
