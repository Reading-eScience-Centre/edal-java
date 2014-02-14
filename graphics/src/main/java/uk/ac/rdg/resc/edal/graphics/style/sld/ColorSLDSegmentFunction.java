package uk.ac.rdg.resc.edal.graphics.style.sld;

import static uk.ac.rdg.resc.edal.graphics.style.sld.AbstractSLDSymbolizer.decodeColour;

import java.awt.Color;
import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
					valueList.add(decodeColour(values.item(i).getTextContent()));
				}
			}
			
			// get the below minimum and above maximum colours
			String belowMinColourText = (String) xPath.evaluate(
					"./resc:BelowMinValue", function, XPathConstants.STRING);
			belowMinValue = decodeColour(belowMinColourText);
			String aboveMaxColourText = (String) xPath.evaluate(
					"./resc:AboveMaxValue", function, XPathConstants.STRING);
			aboveMaxValue = decodeColour(aboveMaxColourText);
			
			// get the number of segments
			parseNumberOfSegments();
			
			// create the range
			parseRange();
		} catch (Exception e) {
			throw new SLDException(e);
		}
	}

}
