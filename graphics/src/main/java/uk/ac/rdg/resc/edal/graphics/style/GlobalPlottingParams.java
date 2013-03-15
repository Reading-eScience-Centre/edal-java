package uk.ac.rdg.resc.edal.graphics.style;

import uk.ac.rdg.resc.edal.Extent;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.position.TimePosition;
import uk.ac.rdg.resc.edal.util.Extents;

public class GlobalPlottingParams {
    private int width = 256;
    private int height = 256;

    private BoundingBox bbox;
    private Extent<Double> zExtent;
    private Extent<TimePosition> tExtent;
    private Double targetZ;
    private TimePosition targetT;

    public GlobalPlottingParams(int width, int height, BoundingBox bbox, Extent<Double> zExtent,
            Extent<TimePosition> tExtent, Double targetZ, TimePosition targetT) {
        super();
        this.width = width;
        this.height = height;
        this.bbox = bbox;
        this.zExtent = zExtent;
        this.tExtent = tExtent;
        this.targetZ = targetZ;
        this.targetT = targetT;
        
        if(zExtent == null && targetZ != null) {
            zExtent = Extents.newExtent(targetZ, targetZ);
        }
        
        if(tExtent == null && targetT != null) {
            tExtent = Extents.newExtent(targetT, targetT);
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public BoundingBox getBbox() {
        return bbox;
    }

    public Extent<Double> getZExtent() {
        return zExtent;
    }

    public Extent<TimePosition> getTExtent() {
        return tExtent;
    }

    public Double getTargetZ() {
        return targetZ;
    }

    public TimePosition getTargetT() {
        return targetT;
    }
}
