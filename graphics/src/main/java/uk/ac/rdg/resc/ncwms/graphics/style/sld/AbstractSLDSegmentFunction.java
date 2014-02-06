package uk.ac.rdg.resc.ncwms.graphics.style.sld;

import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class AbstractSLDSegmentFunction<T> extends AbstractSLDFunction<T> {

	protected String paletteName;
	protected List<T> valueList;
	protected T aboveMaxValue;
	protected T belowMinValue;
	protected Integer numberOfSegments;
	protected SLDRange range;
	
	public AbstractSLDSegmentFunction(XPath xPath, Node function) {
		super(xPath, function);
	}

	public String getPaletteName() {
		return paletteName;
	}

	public List<T> getValueList() {
		return valueList;
	}

	public T getAboveMaxValue() {
		return aboveMaxValue;
	}

	public T getBelowMinValue() {
		return belowMinValue;
	}

	public Integer getNumberOfSegments() {
		return numberOfSegments;
	}

	public SLDRange getRange() {
		return range;
	}

	/**
	 * Parse the value list. If a name is present put it in paletteName else get the
	 * list of values or return an error if not present.
	 * 
	 * @return NodeList of values
	 * @throws XPathExpressionException
	 * @throws SLDException
	 */
	protected NodeList parseValueList()	throws XPathExpressionException, SLDException {
		Node valueList = (Node) xPath.evaluate(
				"./resc:ValueList", function, XPathConstants.NODE);
		String paletteName = (String) xPath.evaluate(
				"./se:Name", valueList, XPathConstants.STRING);
		if (paletteName == null || paletteName.equals("")){
			NodeList values = (NodeList) xPath.evaluate(
					"./se:Value", valueList, XPathConstants.NODESET);
			if (!(values.getLength() > 0)) {
				throw new SLDException("Either a name or a list of values must be specified.");
			}
			return values;
		} else {
			this.paletteName = paletteName;
			return null;
		}
	}

	/**
	 * Parse in the number of segments.
	 * 
	 * @throws XPathExpressionException
	 */
	protected void parseNumberOfSegments()
			throws XPathExpressionException {
		String segmentsText = (String) xPath.evaluate(
				"./resc:NumberOfSegments", function, XPathConstants.STRING);
		if (segmentsText == null || segmentsText.equals("")) {
			numberOfSegments = 254;
		} else {
			numberOfSegments = Integer.parseInt(segmentsText);
		}
	}

	/**
	 * Parse in the range (minimum, maximum and spacing).
	 * 
	 * @throws XPathExpressionException
	 * @throws SLDException
	 */
	protected void parseRange() throws SLDException {
		range = SLDRange.parseRange(xPath, function);
	}

}
