package uk.ac.rdg.resc.edal.wms;

import uk.ac.rdg.resc.edal.position.HorizontalPosition;

public class FeatureInfoPoint {
    private String layerName;
    private HorizontalPosition position;
    private Number value;

    public FeatureInfoPoint(String layerName, HorizontalPosition position, Number value) {
        this.layerName = layerName;
        this.position = position;
        this.value = value;
    }

    public String getLayerName() {
        return layerName;
    }

    public HorizontalPosition getPosition() {
        return position;
    }

    public Number getValue() {
        return value;
    }

}
