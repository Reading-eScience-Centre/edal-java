package uk.ac.rdg.resc.edal.graphics.style.sld;

import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class FloatSLDSegmentFunction extends AbstractSLDSegmentFunction<Float> {

	public FloatSLDSegmentFunction(XPath xPath, Node function) throws SLDException{
		super(xPath, function);
		try {
			// get the fallback value
			this.fallbackValue = parseFloatFallbackValue();

			// get the value list
			NodeList values = parseValueList();
			if (values != null) {
				valueList = new ArrayList<Float>();
				for (int i = 0; i < values.getLength(); i++) {
					valueList.add(Float.parseFloat(values.item(i).getTextContent()));
				}
			}
			
			// get the below minimum and above maximum values
			String belowMinValueText = (String) xPath.evaluate(
					"./resc:BelowMinValue", function, XPathConstants.STRING);
			if (belowMinValueText.equals("")) {
				belowMinValue = null;
			} else {
				belowMinValue = Float.parseFloat(belowMinValueText);
			}
			String aboveMaxValueText = (String) xPath.evaluate(
					"./resc:AboveMaxValue", function, XPathConstants.STRING);
			if (aboveMaxValueText.equals("")) {
				aboveMaxValue = null;
			} else {
				aboveMaxValue = Float.parseFloat(aboveMaxValueText);
			}
			
			// get the number of segments
			parseNumberOfSegments();
			
			// create the range
			parseRange();
		} catch (Exception e) {
			throw new SLDException(e);
		}
	}

}
