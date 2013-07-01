package uk.ac.rdg.resc.edal.graphics.style.datamodel.impl;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import uk.ac.rdg.resc.edal.graphics.style.DataReadingTypes.PlotType;
import uk.ac.rdg.resc.edal.graphics.style.PlottingDatum;
import uk.ac.rdg.resc.edal.util.Extents;

@XmlType(namespace = Image.NAMESPACE, name = "Raster2DLayerType")
public class Raster2DLayer extends ImageLayer {

	@XmlElement(name = "XDataFieldName", required = true)
    private String xDataFieldName;
	@XmlElement(name = "YDataFieldName", required = true)
    private String yDataFieldName;
    @XmlElement(name = "ThresholdColourScheme2D", type = ThresholdColourScheme2D.class)
    private ColourScheme2D colourScheme = new ThresholdColourScheme2D();
    
	public Raster2DLayer() {
		super(PlotType.RASTER);
	}

	public Raster2DLayer(String xDataFieldName, String yDataFieldName, ColourScheme2D colourScheme) {
		super(PlotType.RASTER);
		
		this.xDataFieldName = xDataFieldName;
		this.yDataFieldName = yDataFieldName;
		this.colourScheme = colourScheme;
	}

	@Override
	protected void drawIntoImage(BufferedImage image, DataReader dataReader) {
		int height = image.getHeight();
		int width = image.getWidth();
		// Read fields into arrays
		Number[][] xData = new Number[height][width];
		for(PlottingDatum datum: dataReader.getDataForLayerName(xDataFieldName)) {
			int x = datum.getGridCoords().getXIndex();
			int y = datum.getGridCoords().getYIndex();
			if (y >= 0 && y < height && x >= 0 && x < width) {
				xData[y][x] = datum.getValue();
			}
		}
		Number[][] yData = new Number[height][width];
		for(PlottingDatum datum: dataReader.getDataForLayerName(yDataFieldName)) {
			int x = datum.getGridCoords().getXIndex();
			int y = datum.getGridCoords().getYIndex();
			if (y >= 0 && y < height && x >= 0 && x < width) {
				yData[y][x] = datum.getValue();
			}
		}
		// Transform the values to colours and transfer to the image
		int[] pixels = new int[width*height];
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				pixels[x + y*width] = colourScheme.getColor(xData[y][x], yData[y][x]).getRGB();
			}
		}
		image.setRGB(0, 0, width, height, pixels, 0, width);
	}

	@Override
	protected Set<NameAndRange> getFieldsWithScales() {
        Set<NameAndRange> ret = new HashSet<Drawable.NameAndRange>();
        ret.add(new NameAndRange(xDataFieldName, Extents.newExtent(
                colourScheme.getScaleMin(1), colourScheme.getScaleMax(1))));
        ret.add(new NameAndRange(yDataFieldName, Extents.newExtent(
                colourScheme.getScaleMin(2), colourScheme.getScaleMax(2))));
        return ret;
	}

}
