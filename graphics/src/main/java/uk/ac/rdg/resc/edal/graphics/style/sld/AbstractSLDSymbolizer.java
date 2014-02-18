package uk.ac.rdg.resc.edal.graphics.style.sld;

import static uk.ac.rdg.resc.edal.graphics.style.sld.SLDOpacityMapParser.parseLookupValue;
import static uk.ac.rdg.resc.edal.graphics.style.sld.SLDOpacityMapParser.parseOpacityMap;

import java.awt.Color;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;

import uk.ac.rdg.resc.edal.graphics.style.FlatOpacity;
import uk.ac.rdg.resc.edal.graphics.style.ImageLayer;
import uk.ac.rdg.resc.edal.graphics.style.LinearOpacity;
import uk.ac.rdg.resc.edal.graphics.style.PatternScale;

public abstract class AbstractSLDSymbolizer implements SLDSymbolizer{
	
	protected List<String> varNames;
	protected Node symbolizerNode;
	protected ImageLayer imageLayer;
	protected XPath xPath;
	
	/**
	 * This method must be implemented by any symbolizer classes extending this
	 * class. New subclasses should use the xPath field to parse the symbolizerNode
	 * field, which will contain the symbolizer node of the SLD XML document on
	 * execution. The SLD layer name is also provided in the layerName field. The
	 * resulting image layer should be returned. New symbolizers must also be
	 * registered using the registerSymbolizer method of the StyleSLDParser class
	 * at runtime, either in the static initializer of that class or elsewhere before
	 * the createImage method of StyleSLDParser is executed.
	 * 
	 * @return ImageLayer
	 * @throws Exception
	 */
	protected abstract ImageLayer parseSymbolizer() throws Exception;

	/**
	 * Parse the node of the SLD XML document when given as a parameter as well as
	 * the name of the SLD layer and return an image layer object.
	 * @param layerName
	 * @param symbolizerNode
	 * @return ImageLayer
	 * @throws SLDException
	 */
	@Override
	public ImageLayer getImageLayer(List<String> varNames, Node symbolizerNode) throws SLDException {
		if (imageLayer == null) {
			try {
				setVarNames(varNames);
				setSymbolizerNode(symbolizerNode);
				xPath = XPathFactory.newInstance().newXPath();
				xPath.setNamespaceContext(new SLDNamespaceResolver());
				imageLayer = parseSymbolizer();
				addOpacity();
			} catch (Exception e) {
				throw new SLDException(e);
			}	
		}
		return imageLayer;
	}
	
	protected abstract void setVarNames(List<String> varNames) throws SLDException;

	private void setSymbolizerNode(Node symbolizerNode) throws SLDException {
		// make sure layer is not null and an element node
		if (symbolizerNode == null || symbolizerNode.getNodeType() != Node.ELEMENT_NODE) {
			throw new SLDException("The symbolizer node cannot be null and must be an element node.");
		}
		this.symbolizerNode = symbolizerNode;
	}
	
	/**
	 * Adds an opacity transform parsed from the symbolizer node to the image layer.
	 * @throws XPathExpressionException
	 * @throws NumberFormatException
	 * @throws SLDException
	 */
	private void addOpacity() throws XPathExpressionException,
			NumberFormatException, SLDException {
		
		Node opacityNode = (Node) xPath.evaluate(
				"./se:Opacity", symbolizerNode, XPathConstants.NODE);
		Node transformNode = (Node) xPath.evaluate(
				"./resc:OpacityTransform", symbolizerNode, XPathConstants.NODE);
		
		if (opacityNode != null) {
			if (transformNode != null) {
				throw new SLDException("A symbolizer can only contain one opacity transform");
			}
			String opacity = opacityNode.getTextContent();
			imageLayer.setOpacityTransform(new FlatOpacity(Float.parseFloat(opacity)));
			return;
		}
		
		if (transformNode != null) {
			String dataFieldName = parseLookupValue(xPath, transformNode);
			PatternScale patternScale =  parseOpacityMap(xPath, transformNode);
			if (patternScale.isLogarithmic() == true) {
				throw new SLDException("Currently logarithmic opacity transforms " +
						"are not supported.");
			}
			imageLayer.setOpacityTransform(new LinearOpacity(dataFieldName,
					patternScale.getOpaqueValue(),
					patternScale.getTransparentValue()));
			return;
		}
		
	}
	
	/**
	 * Decode a string representing a colour in both the case when there is an opacity or not
	 * @param s
	 * @return
	 */
	public static Color decodeColour(String s) {
        if (s.length() == 7) {
            return Color.decode(s);
        } else if (s.length() == 9) {
            Color color = Color.decode("#"+s.substring(3));
            int alpha = Integer.parseInt(s.substring(1,3), 16);
            return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
        } else {
            return null;
        }
    }

}
