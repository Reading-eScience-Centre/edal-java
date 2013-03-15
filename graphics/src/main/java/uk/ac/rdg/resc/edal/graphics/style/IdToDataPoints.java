package uk.ac.rdg.resc.edal.graphics.style;

import java.util.List;

import uk.ac.rdg.resc.edal.geometry.BoundingBox;

public interface IdToDataPoints {
    public List<PlottingDatum> getDataPoints(String textualIdentifier, BoundingBox bbox);
}
