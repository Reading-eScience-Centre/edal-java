package uk.ac.rdg.resc.edal.graphics.style;

import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

public class SLDNamespaceResolver implements NamespaceContext {

	/**
	 * This method returns the namespace URI for all prefixes needed
	 * to parse an SLD document.
	 * 
	 * @param prefix
	 * @return uri
	 */
	public static final String SLD_NAMESPACE =
			"http://www.opengis.net/sld";
	public static final String SE_NAMESPACE =
			"http://www.opengis.net/se";

	@Override
	public String getNamespaceURI(String prefix) {
		if (prefix == null) {
			throw new IllegalArgumentException("No prefix provided!");
		} else if (prefix.equals("sld")) {
			return SLD_NAMESPACE;
		} else if (prefix.equals("se")) {
			return SE_NAMESPACE;
		} else {
			return XMLConstants.NULL_NS_URI;
		}
	}

	@Override
	public String getPrefix(String namespaceURI) {
		// Not needed in this context.
		return null;
	}

	@Override
	public Iterator getPrefixes(String namespaceURI) {
		// Not needed in this context.
		return null;
	}

}
