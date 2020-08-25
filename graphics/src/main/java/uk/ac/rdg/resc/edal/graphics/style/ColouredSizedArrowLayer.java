package uk.ac.rdg.resc.edal.graphics.style;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Set;

import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.graphics.style.ArrowLayer.ArrowDirectionConvention;
import uk.ac.rdg.resc.edal.util.Array2D;
import uk.ac.rdg.resc.edal.util.Extents;

/**
 * A {@link GriddedImageLayer} which draws arrows which can be scaled and
 * coloured according to two (potentially different) data fields
 * 
 * User: JRI <julien.ripault@atos.net> Date: 02/08/2016
 */
public class ColouredSizedArrowLayer extends SizedArrowLayer {
    private ColourScheme colourScheme;
    private String arrowColourField;

    /**
     * Instantiates a new {@link ColouredSizedArrowLayer}
     * 
     * @param directionField           The name of the data layer used to determine
     *                                 direction
     * @param arrowSizeField           The name of the data layer used to determine
     *                                 arrow size
     * @param arrowColourField         The name of the data layer used to determine
     *                                 arrow colour
     * @param arrowMinSize             The minimum arrow size to plot, in pixels
     * @param arrowMaxSize             The maximum arrow size to plot, in pixels
     * @param scale                    The {@link ScaleRange} used to scale the
     *                                 arrow size
     * @param arrowColourScheme        The {@link ColourScheme} used to determine
     *                                 arrow colour
     * @param arrowStyle               The style of arrow to plot
     * @param arrowDirectionConvention The direction convention of arrow to plot
     */
    public ColouredSizedArrowLayer(String directionField, String arrowSizeField, String arrowColourField,
            Integer arrowMinSize, Integer arrowMaxSize, ScaleRange scale, ColourScheme arrowColourScheme,
            ArrowLayer.ArrowStyle arrowStyle, ArrowDirectionConvention arrowDirectionConvention) {
        super(directionField, arrowSizeField, arrowMinSize, arrowMaxSize, scale, null, arrowStyle,
                arrowDirectionConvention);
        this.arrowColourField = arrowColourField;
        this.colourScheme = arrowColourScheme;
    }

    @Override
    protected Color getArrowColour(Number arrowSize) {
        return colourScheme.getColor(arrowSize);
    }

    @Override
    protected void drawIntoImage(BufferedImage image, MapFeatureDataReader dataReader) throws EdalException {
        Array2D<Number> directions = dataReader.getDataForLayerName(directionFieldName);
        Array2D<Number> sizes = dataReader.getDataForLayerName(arrowSizeFieldName);
        Array2D<Number> colours = dataReader.getDataForLayerName(arrowColourField);
        drawArrows(image, dataReader, directions, sizes, colours);
    }

    @Override
    public Set<NameAndRange> getFieldsWithScales() {
        Set<NameAndRange> fieldsWithScales = super.getFieldsWithScales();
        fieldsWithScales.add(new NameAndRange(arrowColourField,
                Extents.newExtent(colourScheme.getScaleMin(), colourScheme.getScaleMax())));
        return fieldsWithScales;
    }
}
