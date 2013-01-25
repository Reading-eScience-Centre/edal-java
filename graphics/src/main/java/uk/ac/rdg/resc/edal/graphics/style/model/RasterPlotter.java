package uk.ac.rdg.resc.edal.graphics.style.model;

import java.awt.image.BufferedImage;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import uk.ac.rdg.resc.edal.graphics.style.PlottingDatum;

@XmlType(namespace=Image.NAMESPACE)
public class RasterPlotter extends ImageLayer {
    private ColourScheme1D colourScheme = new ColourScheme1D();

    public RasterPlotter() {
        /*
         * To plot raster data, we only need one data field. We also want to
         * extract one data value per pixel.
         */
        super(PlotType.RASTER);
    }

    @XmlElement
    public void setColourScheme(ColourScheme1D colourScheme) {
        this.colourScheme = colourScheme;
    }
    
    public ColourScheme1D getColourScheme() {
        return colourScheme;
    }

    @Override
    protected void drawIntoImage(BufferedImage image, DataReader dataReader) {
        /*
         * We can directly access data[0], because the data array is guaranteed
         * to be of size 1 (because it is specified in the constructor, and
         * checks occur in the superclass)
         */
        for (PlottingDatum datum : dataReader.getDataForLayerName(dataLayerId)) {
            image.setRGB(datum.getGridCoords().getXIndex(), datum.getGridCoords().getYIndex(),
                    colourScheme.getColor(datum.getValue()).getRGB());
        }
    }
}
