package uk.ac.rdg.resc.edal.graphics.style.sld;
//package uk.ac.rdg.resc.ncwms.graphics.style.sld;
//
//import static uk.ac.rdg.resc.ncwms.graphics.style.sld.SLDColorMapParser.parseColorMap;
//
//import javax.xml.xpath.XPathConstants;
//import javax.xml.xpath.XPathExpressionException;
//
//import uk.ac.rdg.resc.edal.graphics.style.ColourScheme;
//import uk.ac.rdg.resc.edal.graphics.style.ImageLayer;
//
//public class SLDSubsampledIconSymbolizer extends AbstractSLDSymbolizer {
//
//	/*
//	 * Parse symbolizer using XPath
//	 */
//	@Override
//	protected ImageLayer parseSymbolizer() throws XPathExpressionException,
//			NumberFormatException, SLDException, InstantiationException {
//		// get the glyph icon properties
//		String iconName = (String) xPath.evaluate(
//				"./resc:IconName", symbolizerNode, XPathConstants.STRING);
//		String iconSpacingText = (String) xPath.evaluate(
//				"./resc:IconSpacing", symbolizerNode, XPathConstants.STRING);
//		Float iconSpacing = 1.5F;
//		if (!(iconSpacingText == null) && ! (iconSpacingText.equals(""))) {
//			iconSpacing = Float.parseFloat(iconSpacingText);
//		}
//		
//		ColourScheme colourScheme = parseColorMap(xPath, symbolizerNode);
//		
//		// instantiate a new subsampled glyph layer and add it to the image
//		SubsampledIconLayer subsampledIconLayer = new SubsampledIconLayer(layerName, iconName, iconSpacing, colourScheme);
//		return subsampledIconLayer;
//	}
//
//}
