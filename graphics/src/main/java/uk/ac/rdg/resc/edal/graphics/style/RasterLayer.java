package uk.ac.rdg.resc.edal.graphics.style;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;

import uk.ac.rdg.resc.edal.graphics.style.util.DataReadingTypes.PlotType;
import uk.ac.rdg.resc.edal.util.Array2D;
import uk.ac.rdg.resc.edal.util.Extents;

@XmlType(namespace = Image.NAMESPACE, name = "RasterLayerType")
public class RasterLayer extends ImageLayer {
    
    @XmlElement(name = "DataFieldName", required = true)
    private String dataFieldName;
    @XmlElements({@XmlElement(name = "PaletteColourScheme", type = PaletteColourScheme.class),
        @XmlElement(name = "ThresholdColourScheme", type = ThresholdColourScheme.class)})
    private ColourScheme colourScheme = new PaletteColourScheme();
    
    private RasterLayer() {
        super(PlotType.RASTER);
    }
    
    public RasterLayer(String dataFieldName, ColourScheme colourScheme) {
        super(PlotType.RASTER);
        this.dataFieldName = dataFieldName;
        this.colourScheme = colourScheme;
    }

    public String getDataFieldName() {
        return dataFieldName;
    }

    public ColourScheme getColourScheme() {
        return colourScheme;
    }

    @Override
    protected void drawIntoImage(BufferedImage image, DataReader dataReader) {
        /*
         * Initialise the array to store colour values
         */
        int[] pixels = new int[image.getWidth() * image.getHeight()];
        
        /*
         * Extract the data from the catalogue
         */
        Array2D values = dataReader.getDataForLayerName(dataFieldName);

        /*
         * The iterator iterates over the x-dimension first, which is the same
         * convention as expected for the colour-values array in image.setRGB
         * below
         */
        int index = 0;
        Iterator<Number> iterator = values.iterator();
        while(iterator.hasNext()) {
            pixels[index++] = colourScheme.getColor(iterator.next()).getRGB();
        }
        image.setRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());
    }

    @Override
    protected Set<NameAndRange> getFieldsWithScales() {
        Set<NameAndRange> ret = new HashSet<Drawable.NameAndRange>();
        ret.add(new NameAndRange(dataFieldName, Extents.newExtent(
                colourScheme.getScaleMin(), colourScheme.getScaleMax())));
        return ret;
    }
}
