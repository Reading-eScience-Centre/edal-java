package uk.ac.rdg.resc.edal.graphics.style;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import uk.ac.rdg.resc.edal.graphics.style.StyleXMLParser.ColorAdapter;

@XmlRootElement
public class ArrowPlotter extends Plotter {
    private int arrowSize = 8;
    private Color arrowColor = Color.black;
    
    public ArrowPlotter() {
        /*
         * To plot raster data, we only need one data field. We also want to
         * extract one data value per pixel.
         */
        super(1, PlotType.SUBSAMPLE);
    }

    @XmlElement
    public void setArrowSize(int arrowSize) {
        if(arrowSize < 1) {
            throw new IllegalArgumentException("Arrow size must be > 0");
        }
        this.arrowSize = arrowSize;
        setXSampleSize((int) (arrowSize*1.5));
        setYSampleSize((int) (arrowSize*1.5));
    }

    @XmlElement
    @XmlJavaTypeAdapter(ColorAdapter.class)
    public void setArrowColor(Color arrowColor) {
        this.arrowColor = arrowColor;
    }

    @Override
    protected void drawIntoImage(BufferedImage image, List<PlottingDatum>[] data) {
        List<PlottingDatum> plotData = data[0];

        Graphics2D g = image.createGraphics();
        g.setColor(arrowColor);
        
        for(PlottingDatum datum : plotData) {
            Number angle = datum.getValue();
            int i = datum.getGridCoords().getXIndex();
            int j = datum.getGridCoords().getYIndex();
            if (angle != null && !Float.isNaN(angle.floatValue())) {
                // Calculate the end point of the arrow
                double iEnd = i + arrowSize * Math.cos(angle.doubleValue());
                // Screen coordinates go down, but north is up, hence
                // the minus sign
                double jEnd = j - arrowSize * Math.sin(angle.doubleValue());
                // Draw a dot representing the data location
                g.fillOval(i - 2, j - 2, 4, 4);
                // Draw a line representing the vector direction
                g.setColor(arrowColor);
                g.setStroke(new BasicStroke(1));
                g.drawLine(i, j, (int) Math.round(iEnd), (int) Math.round(jEnd));
            }
        }
    }
}
