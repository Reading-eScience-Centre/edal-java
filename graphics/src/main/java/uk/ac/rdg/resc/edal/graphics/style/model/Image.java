package uk.ac.rdg.resc.edal.graphics.style.model;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import uk.ac.rdg.resc.edal.graphics.style.GlobalPlottingParams;
import uk.ac.rdg.resc.edal.graphics.style.Id2FeatureAndMember;

@XmlType(namespace=Image.NAMESPACE)
@XmlRootElement(namespace=Image.NAMESPACE)
public class Image {
    
    public static final String NAMESPACE="http://www.resc.reading.ac.uk/";
    
    @XmlElement(name="layer")
    private List<ImageLayer> layers = new ArrayList<ImageLayer>();

    public void addLayer(ImageLayer layer) {
        layers.add(layer);
    }

    public BufferedImage render(GlobalPlottingParams params, Id2FeatureAndMember id2Feature) {
        BufferedImage finalImage = new BufferedImage(params.getWidth(), params.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = finalImage.createGraphics();
        for (ImageLayer layer : layers) {
            graphics.drawImage(layer.drawLayer(params, id2Feature), 0, 0, null);
        }
        return finalImage;
    }
}
