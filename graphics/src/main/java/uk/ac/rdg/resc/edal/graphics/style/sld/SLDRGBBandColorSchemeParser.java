package uk.ac.rdg.resc.edal.graphics.style.sld;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Node;

import uk.ac.rdg.resc.edal.graphics.style.RGBBandColourScheme;
import uk.ac.rdg.resc.edal.graphics.style.ScaleRange;

public class SLDRGBBandColorSchemeParser {

    /**
     * Parses a ColorMap element within the node passed to it.
     * 
     * @param xPath
     * @param node
     * @return ColourScheme
     * @throws SLDException
     */
    public static RGBBandColourScheme parseColorScheme(XPath xPath, Node node) throws SLDException {
        try {
            // get the ColorMap
            Node colorMap = (Node) xPath.evaluate("./se:ColorMap", node, XPathConstants.NODE);
            if (colorMap == null || colorMap.getNodeType() != Node.ELEMENT_NODE) {
                throw new SLDException("ColorMap element expected.");
            }
            
            Node redBand = (Node) xPath.evaluate("./se:ColorMap/resc:RedBand", node, XPathConstants.NODE);
            ScaleRange redRange = SLDRange.parseRange(xPath, redBand).getScaleRange();
            
            Node greenBand = (Node) xPath.evaluate("./se:ColorMap/resc:GreenBand", node, XPathConstants.NODE);
            ScaleRange greenRange = SLDRange.parseRange(xPath, greenBand).getScaleRange();
            
            Node blueBand = (Node) xPath.evaluate("./se:ColorMap/resc:BlueBand", node, XPathConstants.NODE);
            ScaleRange blueRange = SLDRange.parseRange(xPath, blueBand).getScaleRange();
            
            return new RGBBandColourScheme(redRange, greenRange, blueRange);
        } catch (Exception e) {
            throw new SLDException(e);
        }
    }

}
