package uk.ac.rdg.resc.edal.graphics.style;

import java.awt.image.BufferedImage;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.graphics.style.util.GlobalPlottingParams;
import uk.ac.rdg.resc.edal.graphics.style.util.Id2FeatureAndMember;

@XmlType(namespace=Image.NAMESPACE, name="Drawable")
public abstract class Drawable {
    @XmlTransient
    public class NameAndRange {
        private String fieldLabel;
        private Extent<Float> scaleRange;

        public NameAndRange(String fieldLabel, Extent<Float> scaleRange) {
            super();
            this.fieldLabel = fieldLabel;
            this.scaleRange = scaleRange;
        }

        public String getFieldLabel() {
            return fieldLabel;
        }

        public Extent<Float> getScaleRange() {
            return scaleRange;
        }
    }
    
    @XmlElements({
        @XmlElement(name="FlatOpacity", type = FlatOpacity.class),
        @XmlElement(name="LinearOpacity", type = LinearOpacity.class)
    })
    private OpacityTransform opacityTransform;
    
    public OpacityTransform getOpacityTransform() {
        return opacityTransform;
    }

    @XmlTransient
    public void setOpacityTransform(OpacityTransform opacityTransform) {
        this.opacityTransform = opacityTransform;
    }

    public abstract BufferedImage drawImage(GlobalPlottingParams params, Id2FeatureAndMember id2Feature);
    
    /**
     * This should return a list of all the fields used in this image layer, and
     * their appropriate scale ranges. If there is NO scale range there can be
     * NO data field and vice versa - i.e. a {@link NameAndRange} object must
     * have all non-null fields. If the layer doesn't depend on any data, this
     * should return an empty set.
     * 
     * It should never return <code>null</code>.
     * 
     * @return
     */
    protected abstract Set<NameAndRange> getFieldsWithScales();
}
