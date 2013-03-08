package uk.ac.rdg.resc.edal.graphics.style.datamodel.impl;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import uk.ac.rdg.resc.edal.graphics.style.DataReadingTypes.PlotType;
import uk.ac.rdg.resc.edal.graphics.style.PlottingDatum;
import uk.ac.rdg.resc.edal.util.Extents;

@XmlType(namespace = Image.NAMESPACE, name = "RasterLayerType")
public class RasterLayer extends ImageLayer {
    
    @XmlElement(name = "DataFieldName", required = true)
    private String dataFieldName;
    @XmlElement(name = "ColourScheme")
    private ColourScheme colourScheme = new ColourScheme();
    
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
         * We can directly access data[0], because the data array is guaranteed
         * to be of size 1 (because it is specified in the constructor, and
         * checks occur in the superclass)
         */
        for (PlottingDatum datum : dataReader.getDataForLayerName(dataFieldName)) {
            image.setRGB(datum.getGridCoords().getXIndex(), datum.getGridCoords().getYIndex(),
                    colourScheme.getColor(datum.getValue()).getRGB());
        }
    }
    
    @Override
    protected Set<NameAndRange> getFieldsWithScales() {
        Set<NameAndRange> ret = new HashSet<Drawable.NameAndRange>();
        ret.add(new NameAndRange(dataFieldName, Extents.newExtent(
                colourScheme.scaleRange.getScaleMin(), colourScheme.scaleRange.getScaleMax())));
        return ret;
    }
}
