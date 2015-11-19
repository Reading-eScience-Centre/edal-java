package uk.ac.rdg.resc.edal.graphics.style.sld;

import java.awt.Color;
import java.util.ArrayList;

import javax.xml.xpath.XPath;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import uk.ac.rdg.resc.edal.util.GraphicsUtils;

public class ColorSLDCategorizeFunction extends
		AbstractSLDCategorizeFunction<Color> {

	public ColorSLDCategorizeFunction(XPath xPath, Node function) throws SLDException {
		super(xPath, function);
		try {
			// get the fallback value
			this.fallbackValue = parseColorFallbackValue();
			
			// get list of colours
			NodeList colourNodes = parseValues();
			
			// transform to list of Color objects
			values = new ArrayList<Color>();
			for (int j = 0; j < colourNodes.getLength(); j++) {
				Node colourNode = colourNodes.item(j);
				values.add(GraphicsUtils.parseColour(colourNode.getTextContent()));
			}
			
			//get list of thresholds
			parseThresholds();
		} catch(Exception e) {
			throw new SLDException(e);
		}
	}

}
