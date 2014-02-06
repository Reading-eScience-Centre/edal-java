package uk.ac.rdg.resc.ncwms.graphics.style.sld;

import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import uk.ac.rdg.resc.edal.graphics.style.InterpolationPoint;

public abstract class AbstractSLDInterpolateFunction<T> extends AbstractSLDFunction<T> {

	protected List<InterpolationPoint<T>> interpolationPoints;
	
	public AbstractSLDInterpolateFunction(XPath xPath, Node function) {
		super(xPath, function);
	}

	public List<InterpolationPoint<T>> getInterpolationPoints() {
		return this.interpolationPoints;
	}
	
	protected NodeList parseInterpolationPoints() throws XPathExpressionException, SLDException {
		// get list of data points
		NodeList pointNodes = (NodeList) xPath.evaluate(
				"./se:InterpolationPoint", function, XPathConstants.NODESET);
		if (pointNodes == null) {
			throw new SLDException("The interpolate function must contain a list of interpolation points.");
		}
		return pointNodes;
	}

}
