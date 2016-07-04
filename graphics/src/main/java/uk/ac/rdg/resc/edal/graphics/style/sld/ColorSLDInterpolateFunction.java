package uk.ac.rdg.resc.edal.graphics.style.sld;

import java.awt.Color;
import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import uk.ac.rdg.resc.edal.graphics.style.InterpolationPoint;
import uk.ac.rdg.resc.edal.graphics.utils.GraphicsUtils;

public class ColorSLDInterpolateFunction extends
		AbstractSLDInterpolateFunction<Color> {

	public ColorSLDInterpolateFunction(XPath xPath, Node function) throws SLDException {
		super(xPath, function);
		try {
			// get the fallback value
			this.fallbackValue = parseColorFallbackValue();
			
			// get list of data points
			NodeList pointNodes = parseInterpolationPoints();
			
			// parse into an ArrayList
			interpolationPoints = new ArrayList<InterpolationPoint<Color>>();
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
				interpolationPoints.add(new InterpolationPoint<Color>(
						Float.parseFloat(dataNode.getTextContent()),
						GraphicsUtils.parseColour(valueNode.getTextContent())));
			}
		} catch(Exception e) {
			throw new SLDException(e);
		}
	}

}
