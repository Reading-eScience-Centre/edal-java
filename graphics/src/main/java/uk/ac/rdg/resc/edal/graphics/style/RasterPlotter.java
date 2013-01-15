package uk.ac.rdg.resc.edal.graphics.style;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.List;

public class RasterPlotter extends Plotter {
    
    private ColourScheme colourScheme;

    public RasterPlotter() {
        /*
         * To plot raster data, we only need one data field. We also want to
         * extract one data value per pixel.
         */
        super(1, PlotType.RASTER);
        colourScheme = new ColourScheme(1) {
            @Override
            public Color doGetColor(Number... values) {
                if(Float.isNaN(values[0].floatValue()))
                    return Color.blue;
                float intensity = (values[0].floatValue() - 273)/40f;
                if(intensity > 1.0)
                    intensity = 1.0f;
                if(intensity < 0.0)
                    intensity = 0.0f;
                return new Color(intensity, 0f, 0f);
            }
        };
    }

    @Override
    protected void drawIntoImage(BufferedImage image, List<PlottingDatum>[] data) {
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
