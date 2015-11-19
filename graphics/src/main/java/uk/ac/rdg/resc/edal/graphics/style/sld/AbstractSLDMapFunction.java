package uk.ac.rdg.resc.edal.graphics.style.sld;

import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Abstract implementation of a function which maps integer values to specific
 * other values (colours / densities / etc, parameterised by <code>T</code>).
 * 
 * It's primary use is to render categorical data where a single integer maps to
 * a data type. However the <code>&lt;se:Categorize&gt;</code> was already taken
 * and applies to data which falls between thresholds, so this is called
 * <code>&lt;resc:Map&gt;</code>
 * 
 * This is adapted from {@link AbstractSLDCategorizeFunction} because it is very
 * similar.
 * 
 * @author Guy Griffiths
 *
 * @param <T>
 */
public class AbstractSLDMapFunction<T> extends AbstractSLDFunction<T> {
    /*
     * The vast majority of this SLD stuff has no decent documentation. As a
     * result this is an implementation which is a mixture of copying another
     * one and trying to figure out what the hell the class structure is all
     * about.
     * 
     * Top tip - make sure your developers actually document the code they write
     * before they leave for greener pastures...
     */
    protected Map<Integer, T> valueMap;

    public AbstractSLDMapFunction(XPath xPath, Node function) {
        super(xPath, function);
    }

    /**
     * @return A map from {@link Integer} category values to the values they
     *         should be represented by (colour / density / etc)
     */
    public Map<Integer, T> getValueMap() {
        return this.valueMap;
    }

    protected NodeList parseValues() throws XPathExpressionException, SLDException {
        // get the list of values
        NodeList valueNodes = (NodeList) xPath.evaluate("./se:Value", function,
                XPathConstants.NODESET);
        if (valueNodes == null) {
            throw new SLDException("The categorize function must contain a list of values.");
        }
        return valueNodes;
    }
}
