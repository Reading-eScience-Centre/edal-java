package uk.ac.rdg.resc.edal.graphics.style.sld;

import java.awt.Color;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;

import uk.ac.rdg.resc.edal.exceptions.EdalParseException;
import uk.ac.rdg.resc.edal.graphics.style.util.GraphicsUtils;

public abstract class AbstractSLDFunction<T> implements SLDFunction<T> {

	XPath xPath;
	Node function;
	T fallbackValue;
	
	public AbstractSLDFunction(XPath xPath, Node function) {
		this.xPath = xPath;
		this.function = function;
	}

	@Override
	public T getFallbackValue() {
		return this.fallbackValue;
	}

	protected Color parseColorFallbackValue() throws XPathExpressionException, EdalParseException {
		// get fall back value
		String fallbackValue = (String) xPath.evaluate(
				"./@fallbackValue", function, XPathConstants.STRING);
		Color noDataColour = null;
		if (!(fallbackValue == null) && !(fallbackValue.equals(""))) {
			noDataColour = GraphicsUtils.parseColour(fallbackValue);
		}
		return noDataColour;
	}
	
	protected Float parseFloatFallbackValue() throws XPathExpressionException, NumberFormatException {
		// get fall back value
		String fallbackValue = (String) xPath.evaluate(
				"./@fallbackValue", function, XPathConstants.STRING);
		Float noDataValue = null;
		if (!(fallbackValue == null) && !(fallbackValue.equals(""))) {
			noDataValue = Float.parseFloat(fallbackValue);
		}
		return noDataValue;
	}
	
	/**
	 * Returns a string array of the variables specified within the se:LookupValue tag.
	 * 
	 * @param xPath
	 * @param node
	 * @return data field names
	 * @throws XPathExpressionException 
	 * @throws SLDException 
	 */
	protected String[] parseLookupValue() throws XPathExpressionException, SLDException {
		// get the contents of LookupValue and check it is not empty
		String lookupValue;
		lookupValue = (String) xPath.evaluate(
				"./se:LookupValue", function, XPathConstants.STRING);
		if (lookupValue == null || lookupValue.equals("")) {
			throw new SLDException("The names of the data fields must be specified in the se:LookupValue tag.");
		}
		
		// split the names of the variables up
		String[] variableNames = lookupValue.split(",");
		
		// tidy the names up
		for(int i = 0; i < variableNames.length; i++) {
			variableNames[i] = variableNames[i].trim();
			if (variableNames[i].equals("")) {
				throw new SLDException("The name for variable " + (i + 1) +	" cannot be empty.");
			}
		}

		// return the names
		return variableNames;
	}

}
