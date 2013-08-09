package uk.ac.rdg.resc.edal.graphics.style;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import uk.ac.rdg.resc.edal.graphics.style.util.DataReadingTypes.PlotType;
import uk.ac.rdg.resc.edal.graphics.style.util.PlottingDatum;
import uk.ac.rdg.resc.edal.graphics.style.util.StyleXMLParser.ColorAdapter;
import uk.ac.rdg.resc.edal.util.Extents;

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
        this.arrowSize = arrowSize;
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
        this.arrowColour = arrowColour;
        setArrowSize(arrowSize);
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
            int i = datum.getGridCoords().getX();
            int j = datum.getGridCoords().getY();
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

    @Override
    protected Set<NameAndRange> getFieldsWithScales() {
        Set<NameAndRange> ret = new HashSet<Drawable.NameAndRange>();
        ret.add(new NameAndRange(directionFieldName, Extents.newExtent(0f, new Float(2*Math.PI))));
        return ret;
    }
}
