package uk.ac.rdg.resc.edal.graphics.style.datamodel.impl;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import uk.ac.rdg.resc.edal.graphics.ColourableIcon;
import uk.ac.rdg.resc.edal.graphics.style.PlottingDatum;

@XmlType(namespace = Image.NAMESPACE, name = "SimpleGlyphLayerType")
public class SimpleGlyphLayer extends GlyphLayer {

    @XmlElement(name = "DataFieldName", required = true)
    private String dataFieldName;
    @XmlElement(name = "IconName")
	private String iconName = "circle";
    @XmlElement(name = "ColourScheme")
    private ColourScheme colourScheme = new ColourScheme();
	
    private Map<String, ColourableIcon> icons;
    private ColourableIcon pointIcon;
    private int xSpan = 11;
    private int ySpan = 11;

	public SimpleGlyphLayer() throws InstantiationException {
		super();
        
		readInIcons();
		
		pointIcon = getIcon(this.iconName);
	}

	public SimpleGlyphLayer(String dataFieldName, String iconName, ColourScheme colourScheme) throws InstantiationException {
		super();
        
		readInIcons();
		
		this.dataFieldName = dataFieldName;
		this.iconName = iconName;
		this.colourScheme = colourScheme;
		
		pointIcon = getIcon(this.iconName);
		xSpan = pointIcon.getWidth() + 2;
		ySpan = pointIcon.getHeight() + 2;
	}

	private void readInIcons() throws InstantiationException {
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
	
    private ColourableIcon getIcon(String name){
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
		int width = image.getWidth();
		int height = image.getHeight();
		Number[][] data = new Number[width][height];

		// Initialise all data to null values
		for (int i = 0; i < width; i++) {
			for (int j = 0; j< height; j++) {
				data[i][j] = null;
			}
		}
		
		// Read in data into array
		for (PlottingDatum datum : dataReader.getDataForLayerName(dataFieldName)) {
			int i = datum.getGridCoords().getXIndex();
			int j = datum.getGridCoords().getYIndex();
			if (i >= 0 && i < width && j >= 0 && j < height) {
				data[i][j] = datum.getValue();
			}
		}
		
		Graphics2D graphics = image.createGraphics();

		// Plot glyph for every glyphSpan points
		for (int i = 0; i < width; i += xSpan) {
            for (int j = 0; j < height; j += ySpan) {
                double[][] glyphData = new double[xSpan][ySpan];
                for (int iGlyph = 0; iGlyph < xSpan; iGlyph++) {
                    for (int jGlyph = 0; jGlyph < ySpan; jGlyph++) {
                        try {
                        	Number value = data[i + iGlyph][j + jGlyph];
                        	if (value == null) {
                        		glyphData[iGlyph][jGlyph] = Double.NaN;
                        	} else {
                                glyphData[iGlyph][jGlyph] = value.doubleValue();                        		
                        	}
                        } catch (ArrayIndexOutOfBoundsException e) {
                            glyphData[iGlyph][jGlyph] = Double.NaN;
                        }
                    }
                }
                BufferedImage glyphImage = getGlyphImage(glyphData);
                graphics.drawImage(glyphImage, i, height - (j + ySpan) - 1, null);
            }
        }
	}
	
	private BufferedImage getGlyphImage(double[][] glyphData) {
		// Average glyph data
		double mean = 0.0;
		int count = 0;
		for (int i = 0; i < xSpan; i++) {
			for (int j = 0; j < ySpan; j++) {
				if (!Double.isNaN(glyphData[i][j])) {
					mean += glyphData[i][j];
					count += 1;
				}
			}
		}
		mean /= count;
		
		BufferedImage glyph = new BufferedImage(xSpan, ySpan, BufferedImage.TYPE_INT_ARGB);
		if (!Double.isNaN(mean)) {
			Graphics2D graphics = glyph.createGraphics();
			Color color = colourScheme.getColor(mean);
			graphics.drawImage(pointIcon.getColouredIcon(color), 2, 2, null);
		}
		
		return glyph;
	}

}
