package uk.ac.rdg.resc.edal.graphics.style.sld;

import java.awt.Color;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SLDFunctionParser {

    public static SLDFunction<Color> parseColorSLDFunction(XPath xPath, Node node)
            throws SLDException {
        try {
            // Get lists of the functions
            NodeList interpolateFunctions = (NodeList) xPath.evaluate("./se:Interpolate", node,
                    XPathConstants.NODESET);
            NodeList categorizeFunctions = (NodeList) xPath.evaluate("./se:Categorize", node,
                    XPathConstants.NODESET);
            NodeList segmentFunctions = (NodeList) xPath.evaluate("./resc:Segment", node,
                    XPathConstants.NODESET);
            NodeList mapFunctions = (NodeList) xPath.evaluate("./resc:Map", node,
                    XPathConstants.NODESET);

            int nFuncs = interpolateFunctions.getLength() + categorizeFunctions.getLength()
                    + segmentFunctions.getLength() + mapFunctions.getLength();
            if (nFuncs != 1) {
                throw new SLDException(
                        "Exactly one function (Categorize, Interpolate, Segment, or Map) was expected.");
            }

            // check that there is exactly one function and parse it if so
            if (interpolateFunctions.getLength() == 1) {
                return new ColorSLDInterpolateFunction(xPath, interpolateFunctions.item(0));
            } else if (categorizeFunctions.getLength() == 1) {
                return new ColorSLDCategorizeFunction(xPath, categorizeFunctions.item(0));
            } else if (segmentFunctions.getLength() == 1) {
                return new ColorSLDSegmentFunction(xPath, segmentFunctions.item(0));
            } else if (mapFunctions.getLength() == 1) {
                return new ColorSLDMapFunction(xPath, mapFunctions.item(0));
            } else {
                /*
                 * This can never be reached. The length of the total number of
                 * functions must be 1, so one of the NodeLists must contain
                 * exactly 1 item.
                 * 
                 * If we reach this point, it's a coding error.
                 */
                throw new RuntimeException(
                        "Reached theoretically unreachable piece of code.  SLDFunctionParser needs fixing...");
            }
        } catch (Exception e) {
            throw new SLDException(e);
        }
    }

    public static SLDFunction<Float> parseFloatSLDFunction(XPath xPath, Node node)
            throws SLDException {
        try {
            // Get lists of the functions
            NodeList interpolateFunctions = (NodeList) xPath.evaluate("./se:Interpolate", node,
                    XPathConstants.NODESET);
            NodeList categorizeFunctions = (NodeList) xPath.evaluate("./se:Categorize", node,
                    XPathConstants.NODESET);
            NodeList segmentFunctions = (NodeList) xPath.evaluate("./resc:Segment", node,
                    XPathConstants.NODESET);

            // check that there is exactly one function and parse it if so
            if (interpolateFunctions.getLength() == 1 && categorizeFunctions.getLength() == 0
                    && segmentFunctions.getLength() == 0) {
                return new FloatSLDInterpolateFunction(xPath, interpolateFunctions.item(0));
            } else if (categorizeFunctions.getLength() == 1
                    && interpolateFunctions.getLength() == 0 && segmentFunctions.getLength() == 0) {
                return new FloatSLDCategorizeFunction(xPath, categorizeFunctions.item(0));
            } else if (segmentFunctions.getLength() == 1 && interpolateFunctions.getLength() == 0
                    && categorizeFunctions.getLength() == 0) {
                return new FloatSLDSegmentFunction(xPath, segmentFunctions.item(0));
            } else {
                throw new SLDException(
                        "A single function (Categorize, Interpolate or Segment) was expected.");
            }
        } catch (Exception e) {
            throw new SLDException(e);
        }
    }

}
