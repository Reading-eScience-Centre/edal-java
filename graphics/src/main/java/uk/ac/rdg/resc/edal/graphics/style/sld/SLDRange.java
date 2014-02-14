package uk.ac.rdg.resc.edal.graphics.style.sld;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;

public class SLDRange {

	public enum Spacing {
		LINEAR,
		LOGARITHMIC
	}
	
	private Float minimum;
	private Float maximum;
	private Spacing spacing;

	public SLDRange(Float minimum, Float maximum, Spacing spacing) {
		this.minimum = minimum;
		this.maximum = maximum;
		this.spacing = spacing;
	}
	
	public Float getMinimum() {
		return minimum;
	}

	public Float getMaximum() {
		return maximum;
	}

	public Spacing getSpacing() {
		return spacing;
	}

	/**
	 * Parse range from an XML SLD document given the node of the document using
	 * XPath;
	 * 
	 * @param xPath
	 * @param function
	 * @return SLDRange
	 * @throws XPathExpressionException
	 * @throws SLDException
	 */
	public static SLDRange parseRange(XPath xPath, Node function) throws SLDException {
		try {
			String rangeMinText = (String) xPath.evaluate(
					"./resc:Range/resc:Minimum", function, XPathConstants.STRING);
			if (rangeMinText == null || rangeMinText.equals("")) {
				throw new SLDException("The minimum of the range must be specified.");
			}
			Float rangeMin = Float.parseFloat(rangeMinText);
			String rangeMaxText = (String) xPath.evaluate(
					"./resc:Range/resc:Maximum", function, XPathConstants.STRING);
			if (rangeMaxText == null || rangeMaxText.equals("")) {
				throw new SLDException("The maximum of the range must be specified.");
			}
			Float rangeMax = Float.parseFloat(rangeMaxText);
			String spacingText = (String) xPath.evaluate(
					"./resc:Range/resc:Spacing", function, XPathConstants.STRING);
			Spacing spacing;
			if (spacingText == null || spacingText.equals("")) {
				spacing = Spacing.LINEAR;
			} else if (spacingText.equals("linear")) {
				spacing = Spacing.LINEAR;
			} else if (spacingText.equals("logarithmic")) {
				spacing = Spacing.LOGARITHMIC;
			} else {
				throw new SLDException("The spacing may be specified as \"linear\" or " + 
						"\"logarithmic\" or omitted to default to linear.");
			}
			return new SLDRange(rangeMin, rangeMax, spacing);
		} catch(Exception e) {
			throw new SLDException(e);
		}
	}

}
