package uk.ac.rdg.resc.edal.graphics.style.sld;

import java.awt.Color;
import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import uk.ac.rdg.resc.edal.graphics.utils.GraphicsUtils;

public class ColorSLDSegmentFunction extends AbstractSLDSegmentFunction<Color> {

	public ColorSLDSegmentFunction(XPath xPath, Node function) throws SLDException {
		super(xPath, function);
		try {
			// get the fallback value
			this.fallbackValue = parseColorFallbackValue();

			// get the value list
			NodeList values = parseValueList();
			if (values != null) {
				valueList = new ArrayList<Color>();
				for (int i = 0; i < values.getLength(); i++) {
					valueList.add(GraphicsUtils.parseColour(values.item(i).getTextContent()));
				}
			}
			
			// get the below minimum and above maximum colours
			String belowMinColourText = (String) xPath.evaluate(
					"./resc:BelowMinValue", function, XPathConstants.STRING);
			belowMinValue = GraphicsUtils.parseColour(belowMinColourText);
			String aboveMaxColourText = (String) xPath.evaluate(
					"./resc:AboveMaxValue", function, XPathConstants.STRING);
			aboveMaxValue = GraphicsUtils.parseColour(aboveMaxColourText);
			
			// get the number of segments
			parseNumberOfSegments();
			
			// create the range
			parseRange();
		} catch (Exception e) {
			throw new SLDException(e);
		}
	}

}
