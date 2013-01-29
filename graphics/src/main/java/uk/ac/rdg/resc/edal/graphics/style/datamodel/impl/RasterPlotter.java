package uk.ac.rdg.resc.edal.graphics.style.datamodel.impl;

import java.awt.image.BufferedImage;

import uk.ac.rdg.resc.edal.graphics.style.DataReadingTypes.PlotType;
import uk.ac.rdg.resc.edal.graphics.style.PlottingDatum;
import uk.ac.rdg.resc.edal.graphics.style.datamodel.model.RasterData;

public class RasterPlotter extends ImageLayer {

    private RasterData rasterData;

    public RasterPlotter(RasterData rasterData) {
        /*
         * To plot raster data, we only need one data field. We also want to
         * extract one data value per pixel.
         */
        super(PlotType.RASTER);
        this.rasterData = rasterData;
    }

    @Override
    protected void drawIntoImage(BufferedImage image, DataReader dataReader) {
        /*
         * We can directly access data[0], because the data array is guaranteed
         * to be of size 1 (because it is specified in the constructor, and
         * checks occur in the superclass)
         */
        for (PlottingDatum datum : dataReader.getDataForLayerName(rasterData.getDataFieldName())) {
            image.setRGB(datum.getGridCoords().getXIndex(), datum.getGridCoords().getYIndex(),
                    rasterData.getColourScheme().getColor(datum.getValue()).getRGB());
        }
    }
}
