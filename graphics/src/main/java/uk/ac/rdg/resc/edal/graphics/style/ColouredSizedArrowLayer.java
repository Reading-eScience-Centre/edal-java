package uk.ac.rdg.resc.edal.graphics.style;

import java.awt.*;

/**
 * User: JRI <julien.ripault@atos.net>
 * Date: 02/08/2016
 */
public class ColouredSizedArrowLayer extends SizedArrowLayer {

    private ColourScheme colourScheme;

    public ColouredSizedArrowLayer(String directionFieldName, String magnitudeFieldName, Integer minArrowSize, Integer maxArrowSize, ScaleRange magnitudeScaleRange, Color arrowColour, ArrowLayer.ArrowStyle arrowStyle) {
        super(directionFieldName, magnitudeFieldName, minArrowSize, maxArrowSize, magnitudeScaleRange, arrowColour, arrowStyle);
    }

    public ColouredSizedArrowLayer(String layerName, String arrowSizeField, Integer arrowMinSize, Integer arrowMaxSize, ScaleRange scale, ColourScheme arrowColourScheme, ArrowLayer.ArrowStyle arrowStyle) {
        super(layerName, arrowSizeField, arrowMinSize, arrowMaxSize, scale, null, arrowStyle);
        this.colourScheme = arrowColourScheme;
    }

    @Override
    protected Color getArrowColour(Number arrowSize) {
        return colourScheme.getColor(arrowSize);
    }
}
