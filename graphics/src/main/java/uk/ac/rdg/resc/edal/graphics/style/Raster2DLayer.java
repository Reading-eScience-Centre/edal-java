package uk.ac.rdg.resc.edal.graphics.style;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import uk.ac.rdg.resc.edal.graphics.style.util.DataReadingTypes.PlotType;
import uk.ac.rdg.resc.edal.util.Array2D;
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
        /*
         * Read fields into arrays
         */
        Array2D xValues = dataReader.getDataForLayerName(xDataFieldName);
        Array2D yValues = dataReader.getDataForLayerName(yDataFieldName);
        
        Iterator<Number> xIterator = xValues.iterator();
        Iterator<Number> yIterator = yValues.iterator();
        int index = 0;
        int[] pixels = new int[image.getWidth() * image.getHeight()];
        /*
         * Could have done check on either iterator - they should be the same size
         * 
         * Get the colours from the 2 values and set the pixel colour
         */
        while(xIterator.hasNext()) {
            pixels[index++] = colourScheme.getColor(xIterator.next(), yIterator.next()).getRGB();
        }
        image.setRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());
    }

    @Override
    protected Set<NameAndRange> getFieldsWithScales() {
        Set<NameAndRange> ret = new HashSet<Drawable.NameAndRange>();
        ret.add(new NameAndRange(xDataFieldName, Extents.newExtent(colourScheme.getScaleMin(1),
                colourScheme.getScaleMax(1))));
        ret.add(new NameAndRange(yDataFieldName, Extents.newExtent(colourScheme.getScaleMin(2),
                colourScheme.getScaleMax(2))));
        return ret;
    }

}
