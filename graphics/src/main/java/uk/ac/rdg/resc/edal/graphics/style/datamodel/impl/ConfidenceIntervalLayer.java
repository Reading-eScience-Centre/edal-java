package uk.ac.rdg.resc.edal.graphics.style.datamodel.impl;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;

import uk.ac.rdg.resc.edal.graphics.style.DataReadingTypes.PlotType;
import uk.ac.rdg.resc.edal.graphics.style.PlottingDatum;
import uk.ac.rdg.resc.edal.util.Extents;

/*
 * Plot confidence interval triangles.
 */
@XmlType(namespace = Image.NAMESPACE, name = "ConfidenceIntervalLayerType")
public class ConfidenceIntervalLayer extends ImageLayer {

	// The name of the field of lower bounds
	@XmlElement(name = "LowerFieldName", required = true)
    protected String lowerFieldName;
	// The name of the field of upper bounds
	@XmlElement(name = "UpperFieldName", required = true)
    protected String upperFieldName;
	@XmlElement(name = "GlyphSize")
	private Integer glyphSize = 9;
	// The colour scheme to use
	@XmlElements({@XmlElement(name = "PaletteColourScheme", type = PaletteColourScheme.class),
        @XmlElement(name = "ThresholdColourScheme", type = ThresholdColourScheme.class)})
    protected ColourScheme colourScheme = new PaletteColourScheme();
	/*
     * This gets called after being unmarshalled from XML. This sets the
     * glyph size in pixels.
     */
    void afterUnmarshal( Unmarshaller u, Object parent ) {
        setSampleSize();
    }

	public ConfidenceIntervalLayer() {
		super(PlotType.SUBSAMPLE);
		setSampleSize();
	}

	public ConfidenceIntervalLayer(String lowerFieldName, String upperFieldName,
			Integer glyphSize, ColourScheme colourScheme) {
		super(PlotType.SUBSAMPLE);
		
		this.lowerFieldName = lowerFieldName;
		this.upperFieldName = upperFieldName;
		this.glyphSize = glyphSize;
		this.colourScheme = colourScheme;
		
		setSampleSize();
	}
	
	private void setSampleSize() {
	    if(glyphSize < 1 || glyphSize == null) {
	        throw new IllegalArgumentException("Glyph size must be non-null and > 0");
	    }        
	    setXSampleSize(glyphSize);
	    setYSampleSize(glyphSize);
	}

	@Override
	protected void drawIntoImage(BufferedImage image, DataReader dataReader) {
		Graphics2D g = image.createGraphics();
 
		// Plot lower confidence interval triangles
		List<PlottingDatum> lowerData = dataReader.getDataForLayerName(lowerFieldName);
        for(PlottingDatum datum : lowerData) {
        	Number value = datum.getValue();
        	
            int i = datum.getGridCoords().getXIndex();
            int j = datum.getGridCoords().getYIndex();
            if (value != null && !Float.isNaN(value.floatValue())) {
            	Color color = colourScheme.getColor(value);
        		int[] xPoints = {i - glyphSize / 2, i + glyphSize / 2 - 1, i + glyphSize / 2 - 1};
        		int[] yPoints = {j + glyphSize / 2 - 1, j + glyphSize / 2 - 1, j - glyphSize / 2};
        		g.setColor(color);
            	g.fillPolygon(xPoints, yPoints, 3);	
            }
        }
        
        // Plot upper confidence interval triangles
        List<PlottingDatum> upperData = dataReader.getDataForLayerName(upperFieldName);
        for(PlottingDatum datum : upperData) {
        	Number value = datum.getValue();
        	
            int i = datum.getGridCoords().getXIndex();
            int j = datum.getGridCoords().getYIndex();
            
            if (value != null && !Float.isNaN(value.floatValue())) {
            	Color color = colourScheme.getColor(value);
            	int[] xPoints = {i - glyphSize / 2, i - glyphSize / 2, i + glyphSize / 2 - 1};
        		int[] yPoints = {j + glyphSize / 2 - 1, j - glyphSize / 2, j - glyphSize / 2};
        		g.setColor(color);
            	g.fillPolygon(xPoints, yPoints, 3);	
            }
        }
	}

	@Override
	protected Set<NameAndRange> getFieldsWithScales() {
        Set<NameAndRange> ret = new HashSet<Drawable.NameAndRange>();
        ret.add(new NameAndRange(lowerFieldName, Extents.newExtent(
                colourScheme.getScaleMin(), colourScheme.getScaleMax())));
        ret.add(new NameAndRange(upperFieldName, Extents.newExtent(
                colourScheme.getScaleMin(), colourScheme.getScaleMax())));
        return ret;
	}

}
