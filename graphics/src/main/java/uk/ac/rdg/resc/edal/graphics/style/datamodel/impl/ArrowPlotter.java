package uk.ac.rdg.resc.edal.graphics.style.datamodel.impl;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

import uk.ac.rdg.resc.edal.graphics.style.DataReadingTypes.PlotType;
import uk.ac.rdg.resc.edal.graphics.style.PlottingDatum;
import uk.ac.rdg.resc.edal.graphics.style.datamodel.model.ArrowData;

public class ArrowPlotter extends ImageLayer {
    private ArrowData arrowData;
    
    public ArrowPlotter(ArrowData arrowData) {
        /*
         * To plot raster data, we only need one data field. We also want to
         * extract one data value per pixel.
         */
        super(PlotType.SUBSAMPLE);
        
        this.arrowData = arrowData;
        
        if(arrowData.getArrowSize() < 1 || arrowData.getArrowSize() == null) {
            throw new IllegalArgumentException("Arrow size must be non-null and > 0");
        }
        setXSampleSize((int) (arrowData.getArrowSize()*1.5));
        setYSampleSize((int) (arrowData.getArrowSize()*1.5));
    }

    @Override
    protected void drawIntoImage(BufferedImage image, DataReader dataReader) {
        List<PlottingDatum> plotData = dataReader.getDataForLayerName(arrowData.getDirectionFieldName());

        Graphics2D g = image.createGraphics();
        g.setColor(arrowData.getArrowColour());
        
        for(PlottingDatum datum : plotData) {
            Number angle = datum.getValue();
            int i = datum.getGridCoords().getXIndex();
            int j = datum.getGridCoords().getYIndex();
            if (angle != null && !Float.isNaN(angle.floatValue())) {
                // Calculate the end point of the arrow
                double iEnd = i + arrowData.getArrowSize() * Math.cos(angle.doubleValue());
                // Screen coordinates go down, but north is up, hence
                // the minus sign
                double jEnd = j - arrowData.getArrowSize() * Math.sin(angle.doubleValue());
                // Draw a dot representing the data location
                g.fillOval(i - 2, j - 2, 4, 4);
                // Draw a line representing the vector direction
                g.setStroke(new BasicStroke(1));
                g.drawLine(i, j, (int) Math.round(iEnd), (int) Math.round(jEnd));
            }
        }
    }
}
