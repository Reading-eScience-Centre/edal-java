package uk.ac.rdg.resc.edal.graphics.style.datamodel.impl;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import uk.ac.rdg.resc.edal.graphics.style.DataReadingTypes.PlotType;
import uk.ac.rdg.resc.edal.graphics.style.PlottingDatum;
import uk.ac.rdg.resc.edal.graphics.style.StyleXMLParser.ColorAdapter;

@XmlType(namespace = Image.NAMESPACE, propOrder = { "directionFieldName", "arrowSize", "arrowColour" }, name = "ArrowLayerType")
public class ArrowLayer extends ImageLayer {
    @XmlElement(name = "DirectionFieldName", required = true)
    private String directionFieldName;
    @XmlElement(name = "ArrowColour")
    @XmlJavaTypeAdapter(ColorAdapter.class)
    private Color arrowColour = Color.black;
    
    private Integer arrowSize = 8;
    @XmlElement(name = "ArrowSize")
    public void setArrowSize(Integer arrowSize) {
        /*
         * This is annotated on the setter, because the it checks for a valid size
         */
        if(arrowSize < 1 || arrowSize == null) {
            throw new IllegalArgumentException("Arrow size must be non-null and > 0");
        }
        setXSampleSize((int) (arrowSize*1.5));
        setYSampleSize((int) (arrowSize*1.5));
    }
    
    private ArrowLayer(){
        super(PlotType.SUBSAMPLE);
    }
    
    public ArrowLayer(String directionFieldName, Integer arrowSize, Color arrowColour) {
        super(PlotType.SUBSAMPLE);
        this.directionFieldName = directionFieldName;
        this.arrowSize = arrowSize;
        this.arrowColour = arrowColour;
    }

    public String getDirectionFieldName() {
        return directionFieldName;
    }

    public Integer getArrowSize() {
        return arrowSize;
    }

    public Color getArrowColour() {
        return arrowColour;
    }
    
    @Override
    protected void drawIntoImage(BufferedImage image, DataReader dataReader) {
        List<PlottingDatum> plotData = dataReader.getDataForLayerName(directionFieldName);

        Graphics2D g = image.createGraphics();
        g.setColor(arrowColour);
        
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
                g.setStroke(new BasicStroke(1));
                g.drawLine(i, j, (int) Math.round(iEnd), (int) Math.round(jEnd));
            }
        }
    }
}
