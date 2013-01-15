package uk.ac.rdg.resc.edal.graphics.style;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * A plotter class. Each plotter should deal with transforming a number (usually
 * one) of input fields into an output image. This should perform a single
 * conceptual task, such as "plot contours"
 * 
 * @author guy
 * 
 */
public abstract class Plotter {
    public enum PlotType {
        RASTER, GLYPH, TRAJECTORY
    }
    private int n = 1;
    private PlotType plotType;

    public Plotter(int n, PlotType plotType) {
        this.n = n;
        this.plotType = plotType;
    }

    public void drawImage(BufferedImage image, final List<PlottingDatum>[] data) {
        if (data.length != n) {
            throw new IllegalArgumentException("Wrong number of args");
        }
        
        drawIntoImage(image, data);
    }
    
    public PlotType getPlotType() {
        return plotType;
    }
    
    protected abstract void drawIntoImage(BufferedImage image, List<PlottingDatum>[] data);
}
