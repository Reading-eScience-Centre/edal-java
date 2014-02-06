package uk.ac.rdg.resc.ncwms.graphics.style.sld;

import java.util.ArrayList;

import javax.xml.xpath.XPath;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class FloatSLDCategorizeFunction extends
		AbstractSLDCategorizeFunction<Float> {

	public FloatSLDCategorizeFunction(XPath xPath, Node function) throws SLDException {
		super(xPath, function);
		try {
			// get the fallback value
			this.fallbackValue = parseFloatFallbackValue();
			
			// get list of values
			NodeList valueNodes = parseValues();
			
			// transform to list of Float objects
			values = new ArrayList<Float>();
			for (int j = 0; j < valueNodes.getLength(); j++) {
				Node valueNode = valueNodes.item(j);
				values.add(Float.parseFloat(valueNode.getTextContent()));
			}
			
			// parse list of thresholds
			parseThresholds();
		} catch(Exception e) {
			throw new SLDException(e);
		}
	}

}
