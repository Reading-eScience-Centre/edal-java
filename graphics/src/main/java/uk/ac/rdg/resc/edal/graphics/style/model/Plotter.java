package uk.ac.rdg.resc.edal.graphics.style.model;

import java.awt.image.BufferedImage;
import java.util.List;

import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import uk.ac.rdg.resc.edal.graphics.style.PlottingDatum;

/**
 * A plotter class. Each plotter should deal with transforming a number (usually
 * one) of input fields into an output image. This should perform a single
 * conceptual task, such as "plot contours"
 * 
 * @author guy
 * 
 */
@XmlType(namespace=Image.NAMESPACE)
@XmlSeeAlso({RasterPlotter.class, ArrowPlotter.class})
public abstract class Plotter {
    @XmlType(namespace=Image.NAMESPACE)
    public enum PlotType {
        RASTER, SUBSAMPLE, GLYPH, TRAJECTORY
    }
    private int n = 1;
    private PlotType plotType;
    
    /*
     * For subsample type
     */
    private int xSampleSize = 8;
    private int ySampleSize = 8;
    private SubsampleType subsampleType = SubsampleType.CLOSEST;
    @XmlType(namespace=Image.NAMESPACE)
    public enum SubsampleType {
        MEAN, CLOSEST
    }

    protected Plotter(){}
    
    public Plotter(int n, PlotType plotType) {
        this.n = n;
        this.plotType = plotType;
    }

    public void drawImage(BufferedImage image, final List<PlottingDatum>[] data) {
        if (data.length != n) {
            throw new IllegalArgumentException("Wrong number of args");
        }
        
        drawIntoImage(image, data);
    }
    
    public PlotType getPlotType() {
        return plotType;
    }
    
    public void setXSampleSize(int xSampleSize) {
        this.xSampleSize = xSampleSize;
    }
    
    @XmlTransient
    public int getXSampleSize(){
        return xSampleSize;
    }
    
    public void setYSampleSize(int ySampleSize) {
        this.ySampleSize = ySampleSize;
    }
    
    @XmlTransient
    public int getYSampleSize(){
        return ySampleSize;
    }
    
    public void setSubsampleType(SubsampleType subsampleType) {
        this.subsampleType = subsampleType;
    }
    
    @XmlTransient
    public SubsampleType getSubsampleType() {
        return subsampleType;
    }
    
    protected abstract void drawIntoImage(BufferedImage image, List<PlottingDatum>[] data);
}
