package uk.ac.rdg.resc.edal.graphics.style.util;

/**
 * This class contains the enum types needed for specifying how data should be
 * extracted. These were previously part of the model.ImageLayer class, but if
 * they are part of the model package, they get serialized to the XML schema,
 * even though they're not used. This is a bug in JAXB2, but this satisfactorily
 * works around it.
 * 
 * @author guy
 * 
 */
public class DataReadingTypes {
    public enum PlotType {
        RASTER, SMOOTHED, SUBSAMPLE, GLYPH, TRAJECTORY
    }

    public enum SubsampleType {
        MEAN, CLOSEST
    }
}
