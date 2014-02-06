package uk.ac.rdg.resc.ncwms.graphics.style.sld;

import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import uk.ac.rdg.resc.edal.graphics.style.InterpolationPoint;

public class FloatSLDInterpolateFunction extends
		AbstractSLDInterpolateFunction<Float> {

	public FloatSLDInterpolateFunction(XPath xPath, Node function) throws SLDException {
		super(xPath, function);
		try {
			// get the fallback value
			this.fallbackValue = parseFloatFallbackValue();
			
			// get list of data points
			NodeList pointNodes = parseInterpolationPoints();
			
			// parse into an ArrayList
			interpolationPoints = new ArrayList<InterpolationPoint<Float>>();
			for (int j = 0; j < pointNodes.getLength(); j++) {
				Node pointNode = pointNodes.item(j);
				Node dataNode = (Node) xPath.evaluate(
						"./se:Data", pointNode, XPathConstants.NODE);
				if (dataNode == null) {
					throw new SLDException("Each interpolation point must contain a data element.");
				}
				Node valueNode = (Node) xPath.evaluate(
						"./se:Value", pointNode, XPathConstants.NODE);
				if (valueNode == null) {
					throw new SLDException("Each interpolation point must contain a value element.");
				}
				interpolationPoints.add(new InterpolationPoint<Float>(
						Float.parseFloat(dataNode.getTextContent()),
						Float.parseFloat(valueNode.getTextContent())));
			}
		} catch(Exception e) {
			throw new SLDException(e);
		}
	}

}
