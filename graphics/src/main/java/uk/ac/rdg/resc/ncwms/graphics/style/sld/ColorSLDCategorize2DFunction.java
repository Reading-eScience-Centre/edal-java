package uk.ac.rdg.resc.ncwms.graphics.style.sld;

import static uk.ac.rdg.resc.ncwms.graphics.style.sld.AbstractSLDSymbolizer.decodeColour;

import java.awt.Color;
import java.util.ArrayList;

import javax.xml.xpath.XPath;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ColorSLDCategorize2DFunction extends
		AbstractSLDCategorize2DFunction<Color> {

	public ColorSLDCategorize2DFunction(XPath xPath, Node function) throws SLDException {
		super(xPath, function);
		try {
			this.fallbackValue = parseColorFallbackValue();
			
			// parse the values
			NodeList colourNodes = parseValues();
			
			// transform to list of Color objects
			values = new ArrayList<Color>();
			for (int j = 0; j < colourNodes.getLength(); j++) {
				Node colourNode = colourNodes.item(j);
				values.add(decodeColour(colourNode.getTextContent()));
			}
			
			// parse the thresholds
			parseThresholds();
		} catch (Exception e) {
			throw new SLDException(e);
		}
	}

}
