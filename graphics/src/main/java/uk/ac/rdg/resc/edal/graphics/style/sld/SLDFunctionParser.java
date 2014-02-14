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
			NodeList interpolateFunctions = (NodeList) xPath.evaluate(
					"./se:Interpolate", node, XPathConstants.NODESET);
			NodeList categorizeFunctions = (NodeList) xPath.evaluate(
					"./se:Categorize", node, XPathConstants.NODESET);
			NodeList segmentFunctions = (NodeList) xPath.evaluate(
					"./resc:Segment", node, XPathConstants.NODESET);
			
			// check that there is exactly one function and parse it if so
			if (interpolateFunctions.getLength() == 1
					&& categorizeFunctions.getLength() == 0
					&& segmentFunctions.getLength() == 0) {
				return new ColorSLDInterpolateFunction(xPath, interpolateFunctions.item(0));
			} else if (categorizeFunctions.getLength() == 1
					&& interpolateFunctions.getLength() == 0
					&& segmentFunctions.getLength() == 0) {
				return new ColorSLDCategorizeFunction(xPath, categorizeFunctions.item(0));
			} else if (segmentFunctions.getLength() == 1
					&& interpolateFunctions.getLength() == 0
					&& categorizeFunctions.getLength() == 0) {
				return new ColorSLDSegmentFunction(xPath, segmentFunctions.item(0));
			} else {
				throw new SLDException("A single function (Categorize, Interpolate or Segment) was expected.");
			}
		} catch (Exception e) {
			throw new SLDException(e);
		}
	}

	public static SLDFunction<Float> parseFloatSLDFunction(XPath xPath, Node node)
			throws SLDException {
		try {
			// Get lists of the functions
			NodeList interpolateFunctions = (NodeList) xPath.evaluate(
					"./se:Interpolate", node, XPathConstants.NODESET);
			NodeList categorizeFunctions = (NodeList) xPath.evaluate(
					"./se:Categorize", node, XPathConstants.NODESET);
			NodeList segmentFunctions = (NodeList) xPath.evaluate(
					"./resc:Segment", node, XPathConstants.NODESET);
			
			// check that there is exactly one function and parse it if so
			if (interpolateFunctions.getLength() == 1
					&& categorizeFunctions.getLength() == 0
					&& segmentFunctions.getLength() == 0) {
				return new FloatSLDInterpolateFunction(xPath, interpolateFunctions.item(0));
			} else if (categorizeFunctions.getLength() == 1
					&& interpolateFunctions.getLength() == 0
					&& segmentFunctions.getLength() == 0) {
				return new FloatSLDCategorizeFunction(xPath, categorizeFunctions.item(0));
			} else if (segmentFunctions.getLength() == 1
					&& interpolateFunctions.getLength() == 0
					&& categorizeFunctions.getLength() == 0) {
				return new FloatSLDSegmentFunction(xPath, segmentFunctions.item(0));
			} else {
				throw new SLDException("A single function (Categorize, Interpolate or Segment) was expected.");
			}
		} catch (Exception e) {
			throw new SLDException(e);
		}
	}

}
