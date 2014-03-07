package uk.ac.rdg.resc.edal.graphics.style.sld;

import java.awt.Color;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Node;

import uk.ac.rdg.resc.edal.graphics.style.ColourScale;
import uk.ac.rdg.resc.edal.graphics.style.ColourScheme;
import uk.ac.rdg.resc.edal.graphics.style.InterpolateColourScheme;
import uk.ac.rdg.resc.edal.graphics.style.SegmentColourScheme;
import uk.ac.rdg.resc.edal.graphics.style.ThresholdColourScheme;
import uk.ac.rdg.resc.edal.graphics.style.sld.SLDRange.Spacing;

public class SLDColorMapParser {

	/**
	 * Parses a ColorMap element within the node passed to it.
	 * 
	 * @param xPath
	 * @param node
	 * @return ColourScheme
	 * @throws SLDException
	 */
	public static ColourScheme parseColorMap(XPath xPath, Node node)
			throws SLDException {
		try {
			// get the ColorMap
			Node colorMap = (Node) xPath.evaluate(
					"./se:ColorMap", node, XPathConstants.NODE);
			if (colorMap == null || colorMap.getNodeType() != Node.ELEMENT_NODE) {
				throw new SLDException("ColorMap element expected.");
			}

			// get the function defining the colour map
			SLDFunction<Color> function = SLDFunctionParser.parseColorSLDFunction(xPath, colorMap);
			
			// create the colour scheme
			ColourScheme colourScheme;
			if (function instanceof ColorSLDCategorizeFunction) {
				ColorSLDCategorizeFunction categorize = (ColorSLDCategorizeFunction) function;
				colourScheme = new ThresholdColourScheme(categorize.getThresholds(),
						categorize.getValues(), categorize.getFallbackValue());
			} else if (function instanceof ColorSLDInterpolateFunction) {
				ColorSLDInterpolateFunction interpolate = (ColorSLDInterpolateFunction) function;
				colourScheme = new InterpolateColourScheme(interpolate.getInterpolationPoints(),
						interpolate.getFallbackValue());
			} else if (function instanceof ColorSLDSegmentFunction) {
				ColorSLDSegmentFunction segment = (ColorSLDSegmentFunction) function;
				SLDRange range = segment.getRange();
				ColourScale colourScale = new ColourScale(range.getMinimum(),
						range.getMaximum(), range.getSpacing() == Spacing.LOGARITHMIC);
				if (segment.getPaletteName() != null) {
					colourScheme = new SegmentColourScheme(colourScale,
							segment.getBelowMinValue(), segment.getAboveMaxValue(),
							segment.getFallbackValue(), segment.getPaletteName(),
							segment.getNumberOfSegments());
				} else {
					List<Color> colours = segment.getValueList();
					colourScheme = new SegmentColourScheme(colourScale,
							segment.getBelowMinValue(), segment.getAboveMaxValue(),
							segment.getFallbackValue(), colours.toArray(new Color[colours.size()]),
							segment.getNumberOfSegments());
				}
			} else {
				throw new SLDException("Unexpected function in ColorMap.");
			}
			return colourScheme;
		} catch (Exception e) {
			throw new SLDException(e);
		}
	}

}
