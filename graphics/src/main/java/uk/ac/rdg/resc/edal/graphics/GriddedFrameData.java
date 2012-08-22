package uk.ac.rdg.resc.edal.graphics;

import uk.ac.rdg.resc.edal.Extent;


public class GriddedFrameData extends FrameData {
    private Number[][] data;
    private Extent<Float> contourScaleRange;
    
    public GriddedFrameData(PlotStyle plotStyle, Number[][] data, Extent<Float> contourScaleRange) {
        super(plotStyle);
        this.data = data;
        this.contourScaleRange = contourScaleRange;
    }

    public Number[][] getData() {
        return data;
    }
    
    public Extent<Float> getContourScaleRange(){
        return contourScaleRange;
    }
}
