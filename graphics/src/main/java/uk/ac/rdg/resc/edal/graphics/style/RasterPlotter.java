package uk.ac.rdg.resc.edal.graphics.style;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.List;

import uk.ac.rdg.resc.edal.Extent;
import uk.ac.rdg.resc.edal.util.Extents;

public class RasterPlotter extends Plotter {
    
    private ColourScheme colourScheme;

    public RasterPlotter() {
        /*
         * To plot raster data, we only need one data field. We also want to
         * extract one data value per pixel.
         */
        super(1, PlotType.RASTER);
    }

    @Override
    protected void drawIntoImage(BufferedImage image, List<PlottingDatum>[] data) {
    	Extent<Float> scaleRange = Extents.newExtent(270.0f, 310.0f);
    	colourScheme = new ColourScheme1D(scaleRange, null, null, Color.black, "redblue", 100, 254, false);
    	/*
         * We can directly access data[0], because the data array is guaranteed
         * to be of size 1 (because it is specified in the constructor, and
         * checks occur in the superclass)
         */
        for(PlottingDatum datum : data[0]){
        	image.setRGB(datum.getGridCoords().getXIndex(), datum.getGridCoords().getYIndex(),
                    colourScheme.doGetColor(datum.getValue()).getRGB());
        }
    }
}
