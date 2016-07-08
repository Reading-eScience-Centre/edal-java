package uk.ac.rdg.resc.edal.graphics.style.sld;

import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Node;

import uk.ac.rdg.resc.edal.graphics.style.InterpolateDensityMap;
import uk.ac.rdg.resc.edal.graphics.style.DensityMap;
import uk.ac.rdg.resc.edal.graphics.style.SegmentDensityMap;
import uk.ac.rdg.resc.edal.graphics.style.ThresholdDensityMap;

public class SLDDensityMapParser {

	/**
	 * Parse the functions within a node for a pattern symbolizer or opacity transform
	 * 
	 * @param xPath
	 * @param node
	 * @return PatternScale
	 * @throws SLDException
	 */
	public static DensityMap parseDensityMap(XPath xPath, Node node)
			throws SLDException{
		try {
			// get the function defining the pattern
			SLDFunction<Float> function = SLDFunctionParser.parseFloatSLDFunction(xPath, node);
			
			// create the pattern scale
			DensityMap opacityMap;
			if (function instanceof FloatSLDCategorizeFunction) {
				FloatSLDCategorizeFunction categorize = (FloatSLDCategorizeFunction) function;
				opacityMap = new ThresholdDensityMap(categorize.getThresholds(), categorize.getValues(), categorize.getFallbackValue());
			} else if (function instanceof FloatSLDInterpolateFunction) {
				FloatSLDInterpolateFunction interpolate = (FloatSLDInterpolateFunction) function;
				opacityMap = new InterpolateDensityMap(interpolate.getInterpolationPoints(), interpolate.getFallbackValue());
			} else if (function instanceof FloatSLDSegmentFunction) {
				FloatSLDSegmentFunction segment = (FloatSLDSegmentFunction) function;
				List<Float> values = segment.getValueList();
				if (values.size() != 2) {
					throw new SLDException("The Segment function within an opacity transform or pattern must contain exactly two Values");
				}
				float minOpacity = values.get(0);
				float maxOpacity = values.get(1);
				opacityMap = new SegmentDensityMap(segment.getNumberOfSegments(),
						segment.getRange(), minOpacity, maxOpacity,
						segment.getBelowMinValue(), segment.getAboveMaxValue(),
						segment.getFallbackValue());
			} else {
				throw new SLDException("Unexpected function in ColorMap.");
			}
			return opacityMap;
		} catch (Exception e) {
			throw new SLDException(e);
		}
	}

	/**
	 * Parse the lookup value from within a function for a pattern or opacity transform
	 * 
	 * @param xPath
	 * @param node
	 * @return data field name
	 * @throws SLDException
	 */
	public static String parseLookupValue(XPath xPath, Node node) throws SLDException {
		try {
			// parse Categorize function
			String lookupValue = (String) xPath.evaluate(
					"./se:Categorize/se:LookupValue", node, XPathConstants.STRING);
			if (!(lookupValue == null) && !(lookupValue.equals(""))) {
				return lookupValue.trim();
			}			

			// parse Interpolate function
			lookupValue = (String) xPath.evaluate(
					"./se:Interpolate/se:LookupValue", node, XPathConstants.STRING);
			if (!(lookupValue == null) && !(lookupValue.equals(""))) {
				return lookupValue.trim();
			}

			// parse Segment function
			lookupValue = (String) xPath.evaluate(
					"./resc:Segment/se:LookupValue", node, XPathConstants.STRING);
			if (!(lookupValue == null) && !(lookupValue.equals(""))) {
				return lookupValue.trim();
			}
			
			throw new SLDException("A lookup value must be specified within a " +
					"valid function within an opacity transform.");
		} catch (Exception e) {
			throw new SLDException(e);
		}
	}

}