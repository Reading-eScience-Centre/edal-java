package uk.ac.rdg.resc.edal.graphics.style.model;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import uk.ac.rdg.resc.edal.graphics.style.GlobalPlottingParams;
import uk.ac.rdg.resc.edal.graphics.style.Id2FeatureAndMember;

@XmlType(namespace=Image.NAMESPACE)
@XmlRootElement(namespace=Image.NAMESPACE)
public class Image extends DrawableLayer {
    
    /*
     * This is the namespace for the XML.
     * 
     * IF YOU CHANGE IT, YOU NEED TO MODIFY pom.xml AS WELL
     */
    public static final String NAMESPACE="http://www.resc.reading.ac.uk";
    
    @XmlElements({
        @XmlElement(name="image", type = Image.class),
        @XmlElement(name="arrowPlotter", type = ArrowPlotter.class),
        @XmlElement(name="rasterPlotter", type = RasterPlotter.class)
    })
    private List<DrawableLayer> layers = new ArrayList<DrawableLayer>();

    public void addLayer(DrawableLayer layer) {
        layers.add(layer);
    }

    @Override
    public BufferedImage drawImage(GlobalPlottingParams params, Id2FeatureAndMember id2Feature) {
        BufferedImage finalImage = new BufferedImage(params.getWidth(), params.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = finalImage.createGraphics();
        for (DrawableLayer layer : layers) {
            graphics.drawImage(layer.drawImage(params, id2Feature), 0, 0, null);
        }
        return finalImage;
    }
}
