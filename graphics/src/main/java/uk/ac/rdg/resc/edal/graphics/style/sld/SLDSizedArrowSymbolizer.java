package uk.ac.rdg.resc.edal.graphics.style.sld;

import java.awt.Color;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import uk.ac.rdg.resc.edal.exceptions.EdalParseException;
import uk.ac.rdg.resc.edal.graphics.style.ArrowLayer.ArrowStyle;
import uk.ac.rdg.resc.edal.graphics.style.ImageLayer;
import uk.ac.rdg.resc.edal.graphics.style.ScaleRange;
import uk.ac.rdg.resc.edal.graphics.style.SizedArrowLayer;
import uk.ac.rdg.resc.edal.graphics.style.sld.SLDRange.Spacing;
import uk.ac.rdg.resc.edal.graphics.style.util.GraphicsUtils;

public class SLDSizedArrowSymbolizer extends AbstractSLDSymbolizer1D {

    /*
     * Parse symbolizer using XPath
     */
    @Override
    protected ImageLayer parseSymbolizer() throws NumberFormatException, XPathExpressionException,
            SLDException, EdalParseException {
        // get the arrow properties
        String arrowMinSizeText = (String) xPath.evaluate("./resc:ArrowMinSize", symbolizerNode,
                XPathConstants.STRING);
        Integer arrowMinSize = 4;
        if (!(arrowMinSizeText == null) && !(arrowMinSizeText.equals(""))) {
            arrowMinSize = Integer.parseInt(arrowMinSizeText);
        }
        String arrowMaxSizeText = (String) xPath.evaluate("./resc:ArrowMaxSize", symbolizerNode,
                XPathConstants.STRING);
        Integer arrowMaxSize = 12;
        if (!(arrowMaxSizeText == null) && !(arrowMaxSizeText.equals(""))) {
            arrowMaxSize = Integer.parseInt(arrowMaxSizeText);
        }
        String arrowSizeField = (String) xPath.evaluate("./resc:ArrowSizeField", symbolizerNode,
                XPathConstants.STRING);
        if (arrowSizeField == null || arrowSizeField.isEmpty()) {
            throw new SLDException("Must have an ArrowSizeField element for a sized arrow layer");
        }
        String arrowColourText = (String) xPath.evaluate("./resc:ArrowColour", symbolizerNode,
                XPathConstants.STRING);
        Color arrowColour = Color.BLACK;
        if (arrowColourText != null && !(arrowColourText.equals(""))) {
            arrowColour = GraphicsUtils.parseColour(arrowColourText);
        }

        ArrowStyle arrowStyle = ArrowStyle.THIN_ARROW;
        String arrowStyleText = (String) xPath.evaluate("./resc:ArrowStyle", symbolizerNode,
                XPathConstants.STRING);
        if (arrowStyleText != null && !(arrowStyleText.equals(""))) {
            arrowStyle = ArrowStyle.valueOf(arrowStyleText);
        }

        SLDRange range = SLDRange.parseRange(xPath, symbolizerNode);
        ScaleRange scale = new ScaleRange(range.getMinimum(), range.getMaximum(),
                range.getSpacing() == Spacing.LOGARITHMIC);

        // instantiate a new arrow layer and add it to the image
        SizedArrowLayer arrowLayer = new SizedArrowLayer(layerName, arrowSizeField, arrowMinSize,
                arrowMaxSize, scale, arrowColour, arrowStyle);
        return arrowLayer;
    }
}
