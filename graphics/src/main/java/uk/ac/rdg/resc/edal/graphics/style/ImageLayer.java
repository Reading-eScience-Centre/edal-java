package uk.ac.rdg.resc.edal.graphics.style;

import java.awt.image.BufferedImage;

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import uk.ac.rdg.resc.edal.graphics.style.util.DataReadingTypes.PlotType;
import uk.ac.rdg.resc.edal.graphics.style.util.DataReadingTypes.SubsampleType;
import uk.ac.rdg.resc.edal.graphics.style.util.FeatureCatalogue;
import uk.ac.rdg.resc.edal.graphics.style.util.GlobalPlottingParams;
import uk.ac.rdg.resc.edal.graphics.style.util.MapFeatureAndMember;
import uk.ac.rdg.resc.edal.util.Array2D;

@XmlType(namespace = Image.NAMESPACE, name = "ImageLayerType")
public abstract class ImageLayer extends Drawable {

    protected interface DataReader {
        public Array2D getDataForLayerName(String layerId);
    }

    /*
     * The type of plot. This determines how data will be extracted
     */
    private PlotType plotType;

    /*
     * For when the plot type is SUBSAMPLE
     */
    private int xSampleSize = 8;
    private int ySampleSize = 8;
    private SubsampleType subsampleType = SubsampleType.CLOSEST;

    @SuppressWarnings("unused")
    private ImageLayer() {
    }

    public ImageLayer(PlotType plotType) {
        this.plotType = plotType;
    }

    @Override
    public BufferedImage drawImage(final GlobalPlottingParams params,
            final FeatureCatalogue catalogue) {
        BufferedImage image = new BufferedImage(params.getWidth(), params.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        drawIntoImage(image, params, catalogue);
        return image;
    }

    protected void drawIntoImage(BufferedImage image, final GlobalPlottingParams params,
            final FeatureCatalogue catalogue) {
        drawIntoImage(image, new DataReader() {
            @Override
            public Array2D getDataForLayerName(String layerId) {
                MapFeatureAndMember featureAndMemberName = catalogue.getFeatureAndMemberName(
                        layerId, params);
                return featureAndMemberName.getMapFeature().getValues(
                        featureAndMemberName.getMember());
            }
        });
    }

    protected abstract void drawIntoImage(BufferedImage image, DataReader dataReader);

    public PlotType getPlotType() {
        return plotType;
    }

    public void setXSampleSize(int xSampleSize) {
        this.xSampleSize = xSampleSize;
    }

    @XmlTransient
    public int getXSampleSize() {
        return xSampleSize;
    }

    public void setYSampleSize(int ySampleSize) {
        this.ySampleSize = ySampleSize;
    }

    @XmlTransient
    public int getYSampleSize() {
        return ySampleSize;
    }

    public void setSubsampleType(SubsampleType subsampleType) {
        this.subsampleType = subsampleType;
    }

    @XmlTransient
    public SubsampleType getSubsampleType() {
        return subsampleType;
    }
}
