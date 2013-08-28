package uk.ac.rdg.resc.edal.wms;

import java.text.ParseException;

import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;

import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.graphics.formats.ImageFormat;
import uk.ac.rdg.resc.edal.graphics.formats.InvalidFormatException;
import uk.ac.rdg.resc.edal.graphics.style.util.GlobalPlottingParams;
import uk.ac.rdg.resc.edal.util.Extents;
import uk.ac.rdg.resc.edal.util.TimeUtils;
import uk.ac.rdg.resc.edal.wms.exceptions.InvalidCrsException;
import uk.ac.rdg.resc.edal.wms.exceptions.WmsException;
import uk.ac.rdg.resc.edal.wms.util.WmsUtils;

/**
 * Object representing a request to the GetMap operation. This simply parses the
 * request and only does very basic sanity checking on the parameters (e.g.
 * checking for valid integers).
 * 
 * @author Guy Griffiths
 */
public class GetMapParameters {
    private String wmsVersion;
    private ImageFormat imageFormat;
    private boolean animation;
    
    private GlobalPlottingParams globalPlottingParams;
    private GetMapStyleParams styleParameters;

    /**
     * Creates a new instance of GetMapParameter from the given RequestParams
     * @param map 
     * 
     * @throws WmsException
     *             if the request is invalid
     */
    public GetMapParameters(RequestParams params) throws WmsException {
        this.wmsVersion = params.getMandatoryWmsVersion();
        if (!WmsUtils.SUPPORTED_VERSIONS.contains(this.wmsVersion)) {
            throw new WmsException("VERSION " + this.wmsVersion + " not supported");
        }
        try {
            imageFormat = ImageFormat.get(params.getMandatoryString("format"));
        } catch (InvalidFormatException e) {
            throw new WmsException("Unsupported image format: "+params.getMandatoryString("format"));
        }
        animation = params.getBoolean("animation", false);
        globalPlottingParams = parsePlottingParams(params);
        styleParameters = new GetMapStyleParams(params);
    }

    public GlobalPlottingParams getPlottingParameters() {
        return globalPlottingParams;
    }

    public GetMapStyleParams getStyleParameters() {
        return styleParameters;
    }

    public String getWmsVersion() {
        return wmsVersion;
    }
    
    public boolean isAnimation() {
        return animation;
    }
    
    public ImageFormat getImageFormat() {
        return imageFormat;
    }
    
    private GlobalPlottingParams parsePlottingParams(RequestParams params) throws WmsException {
        Extent<DateTime> tExtent = null;
        String timeString = params.getString("time");
        if(timeString != null && !timeString.trim().equals("")) {
            String[] timeStrings = timeString.split("/");
            if(timeStrings.length == 1) {
                try {
                    DateTime time = TimeUtils.iso8601ToDateTime(timeStrings[0], ISOChronology.getInstance());
                    tExtent = Extents.newExtent(time, time);
                } catch (ParseException e) {
                    throw new IllegalArgumentException("Time format is wrong: "+timeStrings[0]);
                }
            } else if(timeStrings.length == 2) {
                try {
                    tExtent = Extents.newExtent(
                            TimeUtils.iso8601ToDateTime(timeStrings[0], ISOChronology.getInstance()),
                            TimeUtils.iso8601ToDateTime(timeStrings[1], ISOChronology.getInstance()));
                } catch (ParseException e) {
                    throw new IllegalArgumentException("Time format is wrong: "+timeString);
                }
            } else {
                throw new IllegalArgumentException("Time can either be a single value or a range");
            }
        }
            
        DateTime targetTime = null;
        String targetTimeString = params.getString("colorby/time");
        if(targetTimeString != null) {
            try {
                targetTime = TimeUtils.iso8601ToDateTime(targetTimeString, ISOChronology.getInstance());
            } catch (ParseException e) {
                throw new IllegalArgumentException("colorby/time format is wrong: "+targetTimeString);
            }
        }
        if(targetTime == null && tExtent != null) {
            targetTime = tExtent.getHigh();
        }
        
        Extent<Double> zExtent = null;
        String depthString = params.getString("elevation");
        if(depthString != null && !depthString.trim().equals("")) {
            String[] depthStrings = depthString.split("/");
            if(depthStrings.length == 1) {
                try {
                    Double depth = Double.parseDouble(depthStrings[0]);
                    zExtent = Extents.newExtent(depth, depth);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Depth format is wrong: "+depthStrings[0]);
                }
            } else if(depthStrings.length == 2) {
                try {
                    zExtent = Extents.newExtent(
                            Double.parseDouble(depthStrings[0]),
                            Double.parseDouble(depthStrings[1]));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Depth format is wrong: "+depthString);
                }
            } else {
                throw new IllegalArgumentException("Depth can either be a single value or a range");
            }
        }
        
        Double targetDepth = null;
        String targetDepthString = params.getString("colorby/depth");
        if(targetDepthString != null) {
            try {
                targetDepth = Double.parseDouble(targetDepthString);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("colorby/depth format is wrong: "+targetDepthString);
            }
        }
        if(targetDepth == null && zExtent != null) {
            targetDepth = zExtent.getHigh();
        }
        
        String crsCode;
        BoundingBox bbox;
        if(wmsVersion.equals("1.3.0")) {
            crsCode = params.getMandatoryString("CRS");
            if (crsCode.equalsIgnoreCase("EPSG:4326")) {
                crsCode = "CRS:84";
                bbox = WmsUtils.parseBbox(params.getMandatoryString("bbox"), false, crsCode);
            } else {
                bbox = WmsUtils.parseBbox(params.getMandatoryString("bbox"), true, crsCode);
            }
        } else {
            crsCode = params.getMandatoryString("SRS");
            if (crsCode.equalsIgnoreCase("EPSG:4326")) {
                crsCode = "CRS:84";
            }
            bbox = WmsUtils.parseBbox(params.getMandatoryString("bbox"), true, crsCode);
        }
        
        try {
            return new GlobalPlottingParams(
                    params.getMandatoryPositiveInt("width"),
                    params.getMandatoryPositiveInt("height"),
                    bbox, zExtent, tExtent, targetDepth, targetTime);
        } catch (InvalidCrsException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Something's wrong with your parameters");
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Something's wrong with your parameters");
        } catch (WmsException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Something's wrong with your parameters");
        }
    }

}
