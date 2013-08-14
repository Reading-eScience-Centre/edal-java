package uk.ac.rdg.resc.edal.graphics.style.datamodel.impl;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;

import uk.ac.rdg.resc.edal.graphics.ColourableIcon;
import uk.ac.rdg.resc.edal.graphics.style.DataReadingTypes.PlotType;
import uk.ac.rdg.resc.edal.graphics.style.PlottingDatum;
import uk.ac.rdg.resc.edal.util.Extents;

@XmlType(namespace = Image.NAMESPACE, name = "GlyphLayerType")
public abstract class GlyphLayer extends ImageLayer {

    @XmlElement(name = "DataFieldName", required = true)
    protected String dataFieldName;
    @XmlElement(name = "GlyphIconName")
    protected String glyphIconName = "circle";
    @XmlElements({@XmlElement(name = "PaletteColourScheme", type = PaletteColourScheme.class),
        @XmlElement(name = "ThresholdColourScheme", type = ThresholdColourScheme.class)})
    protected ColourScheme colourScheme = new PaletteColourScheme();
    
    protected Map<String, ColourableIcon> icons;
    protected ColourableIcon icon;

	private GlyphLayer() throws InstantiationException {
		super(PlotType.SUBSAMPLE);
		
		readInIcons();
	}
	
	public GlyphLayer(PlotType plotType) throws InstantiationException {
		super(plotType);
		
		readInIcons();
	}
	
	public String getGlyphIconName() {
		return glyphIconName;
	}
	
	protected void readInIcons() throws InstantiationException {
		icons = new HashMap<String, ColourableIcon>();

        URL iconUrl;
        BufferedImage iconImage;
        
        /*
         * This will work when the files are packaged as a JAR. For running
         * within an IDE, you may need to add the root directory of the project
         * to the classpath
         */
        try {
            iconUrl = this.getClass().getResource("/img/circle.png");
            iconImage = ImageIO.read(iconUrl);
            icons.put("circle", new ColourableIcon(iconImage));
            
            iconUrl = this.getClass().getResource("/img/square.png");
            iconImage = ImageIO.read(iconUrl);
            icons.put("square", new ColourableIcon(iconImage));
        } catch (IOException e) {
            throw new InstantiationException(
                    "Cannot read required icons.  Ensure that JAR is packaged correctly, or that your project is set up correctly in your IDE");
        }
		
	}
	
    protected ColourableIcon getIcon(String name){
        ColourableIcon ret = null;
        if(name == null){
            ret = icons.get("circle");
        } else {
            ret = icons.get(name.toLowerCase());
        }
        if(ret != null){
            return ret;
        } else {
            return icons.get("circle");
        }
    }

    @Override
	protected void drawIntoImage(BufferedImage image, DataReader dataReader) {
		List<PlottingDatum> plotData = dataReader.getDataForLayerName(dataFieldName);

		Graphics2D g = image.createGraphics();
        
        for(PlottingDatum datum : plotData) {
        	Number value = datum.getValue();
        	
            int i = datum.getGridCoords().getXIndex();
            int j = datum.getGridCoords().getYIndex();
            if (value != null && !Float.isNaN(value.floatValue())) {
            	Color color = colourScheme.getColor(value);
            	g.drawImage(icon.getColouredIcon(color), i - icon.getWidth()/2,
            			j - icon.getHeight()/2, null);	
            }
        }
	}

    @Override
    protected Set<NameAndRange> getFieldsWithScales() {
        Set<NameAndRange> ret = new HashSet<Drawable.NameAndRange>();
        ret.add(new NameAndRange(dataFieldName, Extents.newExtent(
                colourScheme.getScaleMin(), colourScheme.getScaleMax())));
        return ret;
    }
}