package uk.ac.rdg.resc.edal.graphics;


public class PointFrameData extends FrameData {

    private int x;
    private int y;
    private Number value;

    public PointFrameData(PlotStyle plotStyle, int x, int y, Number value) {
        super(plotStyle);
        this.x = x;
        this.y = y;
        this.value = value;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Number getValue() {
        return value;
    }
}
