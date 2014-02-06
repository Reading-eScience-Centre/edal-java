package uk.ac.rdg.resc.ncwms.graphics.style.sld;

import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Node;

import uk.ac.rdg.resc.edal.graphics.style.InterpolationPoint;
import uk.ac.rdg.resc.edal.graphics.style.PatternScale;
import uk.ac.rdg.resc.ncwms.graphics.style.sld.SLDRange.Spacing;

public class SLDOpacityMapParser {

	/**
	 * Parse the functions within a node for a pattern symbolizer or opacity transform
	 * 
	 * @param xPath
	 * @param node
	 * @return PatternScale
	 * @throws SLDException
	 */
	public static PatternScale parseOpacityMap(XPath xPath, Node node)
			throws SLDException{
		try {
			// get the function defining the pattern
			SLDFunction<Float> function = SLDFunctionParser.parseFloatSLDFunction(xPath, node);
			
			// create the pattern scale
			PatternScale patternScale;
			if (function instanceof FloatSLDCategorizeFunction) {
				// TODO support Categorize function
				throw new SLDException("The categorize function is not currently supported as a method of specifying patterns or opacities.");
			} else if (function instanceof FloatSLDInterpolateFunction) {
				FloatSLDInterpolateFunction interpolate = (FloatSLDInterpolateFunction) function;
				Float transparentValue;
				Float opaqueValue;
				List<InterpolationPoint<Float>> points = interpolate.getInterpolationPoints();
				if (points == null || points.size() != 2) {
					throw new SLDException("Exactly two points must be explicitly specified in the Interpolate function for patterns or opacities.");
				} else if (Math.round(points.get(0).getValue()) == 0 && Math.round(points.get(1).getValue()) == 1) {
					transparentValue = points.get(0).getData();
					opaqueValue = points.get(1).getData();
				} else if (Math.round(points.get(0).getValue()) == 1 && Math.round(points.get(1).getValue()) == 0) {
					opaqueValue = points.get(0).getData();
					transparentValue = points.get(1).getData();
				} else {
					throw new SLDException("Currently the opacity must vary between 0 and 1 or 1 and 0.");
				}
				patternScale = new PatternScale(254, transparentValue, opaqueValue, false);
			} else if (function instanceof FloatSLDSegmentFunction) {
				FloatSLDSegmentFunction segment = (FloatSLDSegmentFunction) function;
				Float transparentValue;
				Float opaqueValue;
				if (segment.getValueList() == null || segment.getValueList().size() != 2) {
					throw new SLDException("Exactly two values must be explicitly specified in the Segment function for patterns or opacities.");
				} else if (Math.round(segment.getValueList().get(0)) == 0 && Math.round(segment.getValueList().get(1)) == 1) {
					transparentValue = segment.getRange().getMinimum();
					opaqueValue = segment.getRange().getMaximum();
				} else if (Math.round(segment.getValueList().get(0)) == 1 && Math.round(segment.getValueList().get(1)) == 0) {
					opaqueValue = segment.getRange().getMinimum();
					transparentValue = segment.getRange().getMaximum();
				} else {
					throw new SLDException("Currently the opacity must vary between 0 and 1 or 1 and 0.");
				}
				patternScale = new PatternScale(segment.getNumberOfSegments(), transparentValue, opaqueValue, segment.getRange().getSpacing() == Spacing.LOGARITHMIC);
			} else {
				throw new SLDException("Unexpected function in ColorMap.");
			}
			return patternScale;
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
			// TODO support Categorize function

			// parse Interpolate function
			String lookupValue = (String) xPath.evaluate(
					"./se:Interpolate/se:LookupValue", node, XPathConstants.STRING);
			if (!(lookupValue == null) && !(lookupValue.equals(""))) {
				return lookupValue;
			}

			// parse Segment function
			lookupValue = (String) xPath.evaluate(
					"./se:Segment/se:LookupValue", node, XPathConstants.STRING);
			if (!(lookupValue == null) && !(lookupValue.equals(""))) {
				return lookupValue;
			}
			
			throw new SLDException("A lookup value must be specified within a " +
					"valid function within an opacity transform.");
		} catch (Exception e) {
			throw new SLDException(e);
		}
	}

}