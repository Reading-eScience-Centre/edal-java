package uk.ac.rdg.resc.edal.graphics.style.sld;

import java.awt.Color;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import uk.ac.rdg.resc.edal.exceptions.EdalParseException;
import uk.ac.rdg.resc.edal.graphics.style.ScaleRange;
import uk.ac.rdg.resc.edal.graphics.style.ContourLayer;
import uk.ac.rdg.resc.edal.graphics.style.ContourLayer.ContourLineStyle;
import uk.ac.rdg.resc.edal.graphics.style.sld.SLDRange.Spacing;
import uk.ac.rdg.resc.edal.graphics.style.ImageLayer;
import uk.ac.rdg.resc.edal.graphics.utils.GraphicsUtils;

public class SLDContourSymbolizer extends AbstractSLDSymbolizer1D {

    /*
     * Parse symbolizer using XPath
     */
    @Override
    protected ImageLayer parseSymbolizer() throws NumberFormatException, XPathExpressionException,
            SLDException, EdalParseException {
        // create the scale
        String autoscaleEnabledText = (String) xPath.evaluate("./resc:AutoscaleEnabled",
                symbolizerNode, XPathConstants.STRING);
        Boolean autoscaleEnabled;
        if (autoscaleEnabledText == null) {
            autoscaleEnabled = false;
        } else {
            autoscaleEnabled = Boolean.parseBoolean(autoscaleEnabledText);
        }
        ScaleRange scale;
        if (autoscaleEnabled.equals(false)) {
            SLDRange range = SLDRange.parseRange(xPath, symbolizerNode);
            scale = new ScaleRange(range.getMinimum(), range.getMaximum(),
                    range.getSpacing() == Spacing.LOGARITHMIC);
        } else {
            scale = null;
        }

        // get the contour properties
        String numberOfContoursText = (String) xPath.evaluate("./resc:NumberOfContours",
                symbolizerNode, XPathConstants.STRING);
        Integer numberOfContours = 10;
        if (!(numberOfContoursText == null)) {
            numberOfContours = Integer.parseInt(numberOfContoursText.trim());
        }
        String contourLineColourText = (String) xPath.evaluate("./resc:ContourLineColour",
                symbolizerNode, XPathConstants.STRING);
        Color contourLineColour = Color.BLACK;
        if (!(contourLineColourText == null) && !(contourLineColourText.equals(""))) {
            contourLineColour = GraphicsUtils.parseColour(contourLineColourText);
            if (contourLineColour == null) {
                throw new SLDException("Contour line colour incorrectly formatted.");
            }
        }
        String contourLineWidthText = (String) xPath.evaluate("./resc:ContourLineWidth",
                symbolizerNode, XPathConstants.STRING);
        Integer contourLineWidth = 1;
        if (!(contourLineWidthText == null) && !(contourLineWidthText.equals(""))) {
            contourLineWidth = Integer.parseInt(contourLineWidthText.trim());
        }
        String contourLineStyleText = (String) xPath.evaluate("./resc:ContourLineStyle",
                symbolizerNode, XPathConstants.STRING);
        ContourLineStyle contourLineStyle = ContourLineStyle.DASHED;
        if (!(contourLineStyleText == null) && !(contourLineStyleText.trim().equals(""))) {
            contourLineStyleText = contourLineStyleText.trim();
            if (contourLineStyleText.equalsIgnoreCase("DASHED")) {
                contourLineStyle = ContourLineStyle.DASHED;
            } else if (contourLineStyleText.equalsIgnoreCase("HEAVY")) {
                contourLineStyle = ContourLineStyle.HEAVY;
            } else if (contourLineStyleText.equalsIgnoreCase("HIGHLIGHT")) {
                contourLineStyle = ContourLineStyle.HIGHLIGHT;
            } else if (contourLineStyleText.equalsIgnoreCase("MARK")) {
                contourLineStyle = ContourLineStyle.MARK;
            } else if (contourLineStyleText.equalsIgnoreCase("MARK_LINE")) {
                contourLineStyle = ContourLineStyle.MARK_LINE;
            } else if (contourLineStyleText.equalsIgnoreCase("SOLID")) {
                contourLineStyle = ContourLineStyle.SOLID;
            } else if (contourLineStyleText.equalsIgnoreCase("STROKE")) {
                contourLineStyle = ContourLineStyle.STROKE;
            } else {
                throw new SLDException("Contour line style not recognized.");
            }
        }
        String labelEnabledText = (String) xPath.evaluate("./resc:Scale/resc:LabelEnabled",
                symbolizerNode, XPathConstants.STRING);
        boolean labelEnabled = true;
        if (!(labelEnabledText == null) && !(labelEnabledText.equals(""))) {
            labelEnabled = Boolean.parseBoolean(labelEnabledText);
        }

        // instantiate a new contour layer and add it to the image
        ContourLayer contourLayer = new ContourLayer(layerName, scale, autoscaleEnabled,
                numberOfContours, contourLineColour, contourLineWidth, contourLineStyle,
                labelEnabled);
        return contourLayer;
    }
}
