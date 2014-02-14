package uk.ac.rdg.resc.edal.graphics.style.sld;

import java.awt.Color;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Node;

import uk.ac.rdg.resc.edal.graphics.style.ColourMap;
import uk.ac.rdg.resc.edal.graphics.style.ColourScale;
import uk.ac.rdg.resc.edal.graphics.style.ColourScheme;
import uk.ac.rdg.resc.edal.graphics.style.InterpolateColourScheme;
import uk.ac.rdg.resc.edal.graphics.style.PaletteColourScheme;
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
				String palette;
				if (segment.getPaletteName() != null) {
					palette = segment.getPaletteName();
				} else {
					palette = "";
					int i = 1;
					int l = segment.getValueList().size();
					for (Color c: segment.getValueList()) {
						palette = palette + String.format("#%02x%02x%02x%02x",
								c.getAlpha(), c.getRed(), c.getGreen(), c.getBlue());
						if (i < l) {
							palette = palette + ",";
						}
						i++;
					}
					
				}
				ColourMap colourMap = new ColourMap(segment.getBelowMinValue(),
						segment.getAboveMaxValue(),segment.getFallbackValue(), palette,
						segment.getNumberOfSegments());
				SLDRange range = segment.getRange();
				ColourScale colourScale = new ColourScale(range.getMinimum(),
						range.getMaximum(), range.getSpacing() == Spacing.LOGARITHMIC);
				colourScheme = new PaletteColourScheme(colourScale, colourMap);
			} else {
				throw new SLDException("Unexpected function in ColorMap.");
			}
			return colourScheme;
		} catch (Exception e) {
			throw new SLDException(e);
		}
	}

}
