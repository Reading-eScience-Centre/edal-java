package uk.ac.rdg.resc.edal.graphics.style.sld;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class AbstractSLDCategorizeFunction<T> extends AbstractSLDFunction<T> {

	protected List<T> values;
	protected List<Float> thresholds;
	
	public AbstractSLDCategorizeFunction(XPath xPath, Node function) {
		super(xPath, function);
	}

	public List<T> getValues() {
		return this.values;
	}
	
	public List<Float> getThresholds() {
		return this.thresholds;
	}

	protected NodeList parseValues() throws XPathExpressionException, SLDException {
		// get the list of values
		NodeList valueNodes = (NodeList) xPath.evaluate(
				"./se:Value", function, XPathConstants.NODESET);
		if (valueNodes == null) {
			throw new SLDException("The categorize function must contain a list of values.");
		}
		return valueNodes;
	}

	protected void parseThresholds() throws XPathExpressionException, SLDException {
		// get the list of thresholds
		NodeList thresholdNodes = (NodeList) xPath.evaluate(
				"./se:Threshold", function, XPathConstants.NODESET);
		if (thresholdNodes == null) {
			throw new SLDException("The categorize function must contain a list of thresholds.");
		}
		
		// transform to list of Floats
		thresholds = new ArrayList<Float>();
		for (int j = 0; j < thresholdNodes.getLength(); j++) {
			Node thresholdNode = thresholdNodes.item(j);
			thresholds.add(Float.parseFloat(thresholdNode.getTextContent().trim()));
		}
	}

}
