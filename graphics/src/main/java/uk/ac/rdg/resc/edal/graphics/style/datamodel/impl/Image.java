package uk.ac.rdg.resc.edal.graphics.style.datamodel.impl;

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

@XmlType(namespace=Image.NAMESPACE, name="ImageType")
@XmlRootElement(namespace=Image.NAMESPACE, name="Image")
public class Image extends Drawable {
    /*
     * This is the namespace for the XML.
     * 
     * IF YOU CHANGE IT, YOU NEED TO MODIFY pom.xml AS WELL
     */
    public static final String NAMESPACE="http://www.resc.reading.ac.uk";
    
    @XmlElements({
        @XmlElement(name="Image", type = Image.class),
        @XmlElement(name="ArrowLayer", type = ArrowLayer.class),
        @XmlElement(name="RasterLayer", type = RasterLayer.class),
        @XmlElement(name="StippleLayer", type = StippleLayer.class),
        @XmlElement(name="ContourLayer", type = ContourLayer.class),
        @XmlElement(name="SubsampledGlyphLayer", type = SubsampledGlyphLayer.class)
    })
    private List<Drawable> layers = new ArrayList<Drawable>();

    public List<Drawable> getLayers() {
        return layers;
    }
    
    @Override
    public BufferedImage drawImage(GlobalPlottingParams params, Id2FeatureAndMember id2Feature) {
        BufferedImage finalImage = new BufferedImage(params.getWidth(), params.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = finalImage.createGraphics();
        
        for(Drawable drawable : layers) {
            if(drawable != null) {
                BufferedImage drawnImage = drawable.drawImage(params, id2Feature);
                OpacityTransform opacityTransform = drawable.getOpacityTransform();
                if(opacityTransform != null) {
                    opacityTransform.drawIntoImage(drawnImage, params, id2Feature);
                }
                graphics.drawImage(drawnImage, 0, 0, null);
            }
        }
        return finalImage;
    }
}
