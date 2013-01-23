package uk.ac.rdg.resc.edal.graphics.style;

import java.awt.image.BufferedImage;
import java.util.List;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class RasterPlotter extends Plotter {
    private ColourScheme1D colourScheme = new ColourScheme1D();

    public RasterPlotter() {
        /*
         * To plot raster data, we only need one data field. We also want to
         * extract one data value per pixel.
         */
        super(1, PlotType.RASTER);
    }

    @XmlElementRef
    public void setColourScheme(ColourScheme1D colourScheme) {
        this.colourScheme = colourScheme;
    }

    @Override
    protected void drawIntoImage(BufferedImage image, List<PlottingDatum>[] data) {
        /*
         * We can directly access data[0], because the data array is guaranteed
         * to be of size 1 (because it is specified in the constructor, and
         * checks occur in the superclass)
         */
        for (PlottingDatum datum : data[0]) {
            image.setRGB(datum.getGridCoords().getXIndex(), datum.getGridCoords().getYIndex(),
                    colourScheme.doGetColor(datum.getValue()).getRGB());
        }
    }
}
