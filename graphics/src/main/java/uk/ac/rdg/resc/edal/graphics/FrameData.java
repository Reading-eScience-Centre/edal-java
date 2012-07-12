package uk.ac.rdg.resc.edal.graphics;

public abstract class FrameData {
    private PlotStyle plotStyle;
    
    public FrameData(PlotStyle plotStyle) {
        this.plotStyle = plotStyle;
    }
    
    public PlotStyle getPlotStyle() {
        return plotStyle;
    }
}
