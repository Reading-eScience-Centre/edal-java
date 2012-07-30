package uk.ac.rdg.resc.edal.graphics;

/**
 * An abstract class representing data to be rendered onto a map. Subclasses
 * should have no reference to real-world positions, only image co-ordinates.
 * 
 * @author Guy Griffiths
 * 
 */
public abstract class FrameData {
    private PlotStyle plotStyle;

    public FrameData(PlotStyle plotStyle) {
        this.plotStyle = plotStyle;
    }

    public PlotStyle getPlotStyle() {
        return plotStyle;
    }
}
