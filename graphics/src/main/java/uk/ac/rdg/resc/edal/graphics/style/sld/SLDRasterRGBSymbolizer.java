package uk.ac.rdg.resc.edal.graphics.style.sld;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import uk.ac.rdg.resc.edal.graphics.style.ImageLayer;
import uk.ac.rdg.resc.edal.graphics.style.RGBBandColourScheme;
import uk.ac.rdg.resc.edal.graphics.style.RasterRGBLayer;

public class SLDRasterRGBSymbolizer extends AbstractSLDSymbolizer1D {

    /*
     * Parse symbolizer using XPath
     */
    @Override
    protected ImageLayer parseSymbolizer() throws XPathExpressionException, NumberFormatException,
            SLDException {
        String redBandName = (String) xPath.evaluate("./se:ColorMap/resc:RedBand/resc:BandName",
                symbolizerNode, XPathConstants.STRING);
        String greenBandName = (String) xPath
                .evaluate("./se:ColorMap/resc:GreenBand/resc:BandName", symbolizerNode,
                        XPathConstants.STRING);
        String blueBandName = (String) xPath.evaluate("./se:ColorMap/resc:BlueBand/resc:BandName",
                symbolizerNode, XPathConstants.STRING);

        RGBBandColourScheme colourScheme = SLDRGBBandColorSchemeParser.parseColorScheme(xPath,
                symbolizerNode);

        RasterRGBLayer rgbLayer = new RasterRGBLayer(redBandName, greenBandName, blueBandName,
                colourScheme);
        return rgbLayer;
    }

}
