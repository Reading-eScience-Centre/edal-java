package uk.ac.rdg.resc.edal.graphics.style.sld;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class AbstractSLDCategorize2DFunction<T> extends AbstractSLDFunction<T> {

	protected List<T> values;
	protected List<Float> xThresholds;
	protected List<Float> yThresholds;

	public AbstractSLDCategorize2DFunction(XPath xPath, Node function) throws SLDException {
		super(xPath, function);
	}

	public List<T> getValues() {
		return values;
	}

	public List<Float> getXThresholds() {
		return xThresholds;
	}

	public List<Float> getYThresholds() {
		return yThresholds;
	}

	protected NodeList parseValues() throws XPathExpressionException, SLDException {
		// get list of values
		NodeList valueNodes = (NodeList) xPath.evaluate(
				"./se:Value", function, XPathConstants.NODESET);
		if (valueNodes == null) {
			throw new SLDException("The Categorize2D function must contain a list of values.");
		}
		return valueNodes;
	}
	
	protected void parseThresholds() throws XPathExpressionException, SLDException {
		//get list of x thresholds
		NodeList xThresholdNodes = (NodeList) xPath.evaluate(
				"./resc:XThreshold", function, XPathConstants.NODESET);
		if (xThresholdNodes == null) {
			throw new SLDException("The Categorize2D function must contain a list of x thresholds.");
		}

		// transform to list of Floats
		xThresholds = new ArrayList<Float>();
		for (int j = 0; j < xThresholdNodes.getLength(); j++) {
			Node thresholdNode = xThresholdNodes.item(j);
			xThresholds.add(Float.parseFloat(thresholdNode.getTextContent()));
		}
		
		//get list of y thresholds
		NodeList yThresholdNodes = (NodeList) xPath.evaluate(
				"./resc:YThreshold", function, XPathConstants.NODESET);
		if (yThresholdNodes == null) {
			throw new SLDException("The Categorize2D function must contain a list of y thresholds.");
		}
		// transform to list of Floats
		yThresholds = new ArrayList<Float>();
		for (int j = 0; j < yThresholdNodes.getLength(); j++) {
			Node thresholdNode = yThresholdNodes.item(j);
			yThresholds.add(Float.parseFloat(thresholdNode.getTextContent()));
		}
	}

}
