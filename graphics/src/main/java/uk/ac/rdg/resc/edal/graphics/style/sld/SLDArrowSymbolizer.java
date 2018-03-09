package uk.ac.rdg.resc.edal.graphics.style.sld;

import java.awt.Color;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import uk.ac.rdg.resc.edal.exceptions.EdalParseException;
import uk.ac.rdg.resc.edal.graphics.style.ArrowLayer;
import uk.ac.rdg.resc.edal.graphics.style.ArrowLayer.ArrowDirectionConvention;
import uk.ac.rdg.resc.edal.graphics.style.ArrowLayer.ArrowStyle;
import uk.ac.rdg.resc.edal.graphics.style.ImageLayer;
import uk.ac.rdg.resc.edal.graphics.utils.GraphicsUtils;

public class SLDArrowSymbolizer extends AbstractSLDSymbolizer1D {

    /*
     * Parse symbolizer using XPath
     */
    @Override
    protected ImageLayer parseSymbolizer() throws NumberFormatException, XPathExpressionException,
            SLDException, EdalParseException {
        // get the arrow properties
        String arrowSizeText = ((String) xPath.evaluate("./resc:ArrowSize", symbolizerNode,
                XPathConstants.STRING));
        Integer arrowSize = 8;
        if (!(arrowSizeText == null) && !(arrowSizeText.trim().isEmpty())) {
            arrowSize = Integer.parseInt(arrowSizeText.trim());
        }
        String arrowColourText = (String) xPath.evaluate("./resc:ArrowColour", symbolizerNode,
                XPathConstants.STRING);
        Color arrowColour = Color.BLACK;
        if (arrowColourText != null && !(arrowColourText.equals(""))) {
            arrowColour = GraphicsUtils.parseColour(arrowColourText);
        }

        String arrowBackgroundText = (String) xPath.evaluate("./resc:ArrowBackground",
                symbolizerNode, XPathConstants.STRING);
        Color arrowBackground = new Color(0, true);
        if (arrowBackgroundText != null && !(arrowBackgroundText.equals(""))) {
            arrowBackground = GraphicsUtils.parseColour(arrowBackgroundText);
        }

        ArrowStyle arrowStyle = ArrowStyle.THIN_ARROW;
        String arrowStyleText = (String) xPath.evaluate("./resc:ArrowStyle", symbolizerNode,
                XPathConstants.STRING);
        if (arrowStyleText != null && !(arrowStyleText.equals(""))) {
            arrowStyle = ArrowStyle.valueOf(arrowStyleText);
        }

        ArrowDirectionConvention arrowDirectionConvention = ArrowDirectionConvention.DEFAULT;
        if(arrowStyle.equals(ArrowStyle.FAT_ARROW) || arrowStyle.equals(ArrowStyle.THIN_ARROW)
            || arrowStyle.equals(ArrowStyle.TRI_ARROW)) {
            String arrowDirectionConventionText = (String) xPath.evaluate("./resc:ArrowDirectionConvention",
    				symbolizerNode, XPathConstants.STRING);

            if (arrowDirectionConventionText != null && !(arrowDirectionConventionText.equals(""))) {
                arrowDirectionConvention = ArrowDirectionConvention.valueOf(arrowDirectionConventionText);
            }        	
        }

        // instantiate a new arrow layer and add it to the image
        ArrowLayer arrowLayer = new ArrowLayer(layerName, arrowSize, arrowColour, arrowBackground,
                arrowStyle, arrowDirectionConvention);
        return arrowLayer;
    }

}
