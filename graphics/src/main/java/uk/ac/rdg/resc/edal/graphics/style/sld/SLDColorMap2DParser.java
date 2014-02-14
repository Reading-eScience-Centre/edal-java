package uk.ac.rdg.resc.edal.graphics.style.sld;

import java.awt.Color;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Node;

import uk.ac.rdg.resc.edal.graphics.style.ColourScheme2D;
import uk.ac.rdg.resc.edal.graphics.style.ThresholdColourScheme2D;

public class SLDColorMap2DParser {

	/**
	 * Parses a ColorMap2D element to a ColourScheme2D object, representing a
	 * bivariate colour map
	 * 
	 * @return ColourScheme2D
	 * @throws SLDException
	 */
	public static ColourScheme2D parseColorMap2D(XPath xPath, Node node, SLDVariables2D variables)
			throws SLDException {
		try {
			// check that the variable names object is not null
			if (variables == null) {
				throw new SLDException("A variables object must be passed to parseColorMap2D.");
			}
			
			// get the function defining the colour map
			Node colorMap2D = (Node) xPath.evaluate(
					"./resc:ColorMap2D", node, XPathConstants.NODE);
			if (colorMap2D == null || colorMap2D.getNodeType() != Node.ELEMENT_NODE) {
				throw new SLDException("A bivariate colour map was expected.");
			}
			
			// parse the function
			SLDFunction<Color> function = SLDFunction2DParser.parseColorSLDFunction2D(xPath, colorMap2D);
			
			// create the colour map and copy the variables accross
			ColourScheme2D colourScheme2D;
			if (function instanceof ColorSLDCategorize2DFunction) {
				ColorSLDCategorize2DFunction categorize2D = (ColorSLDCategorize2DFunction) function;
				categorize2D.copyVariables(variables);
				colourScheme2D = new ThresholdColourScheme2D(categorize2D.getXThresholds(),
						categorize2D.getYThresholds(), categorize2D.getValues(), categorize2D.getFallbackValue());
			} else {
				throw new SLDException("Unexpected function in ColorMap2D.");
			}
			return colourScheme2D;
		} catch (Exception e) {
			throw new SLDException(e);
		}
	}

}
