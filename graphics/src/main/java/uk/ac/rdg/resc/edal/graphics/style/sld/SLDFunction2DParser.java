package uk.ac.rdg.resc.edal.graphics.style.sld;

import java.awt.Color;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SLDFunction2DParser {

	public static SLDFunction<Color> parseColorSLDFunction2D(XPath xPath, Node node)
			throws SLDException {
		try {
			// Get lists of the functions
			NodeList categorize2DFunctions = (NodeList) xPath.evaluate(
					"./resc:Categorize2D", node, XPathConstants.NODESET);
			
			// check that there is exactly one function and parse it if so
			if (categorize2DFunctions.getLength() == 1) {
				return new ColorSLDCategorize2DFunction(xPath, categorize2DFunctions.item(0));
			} else {
				throw new SLDException("A single function (Categorize2D) was expected.");
			}
		} catch (Exception e) {
			throw new SLDException(e);
		}
	}

}
