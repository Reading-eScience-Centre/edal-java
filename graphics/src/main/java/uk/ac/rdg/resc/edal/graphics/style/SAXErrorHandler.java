package uk.ac.rdg.resc.edal.graphics.style;

import java.io.PrintWriter;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/*
 * This code for handling SAX errors thrown when parsing an XML document is
 * taken from Oracles turorial on reading XML data into a DOM, available at:
 * http://docs.oracle.com/javase/tutorial/jaxp/dom/readingXML.html
 */
public class SAXErrorHandler implements ErrorHandler {
	
	private PrintWriter out;

	public SAXErrorHandler(PrintWriter out) {
		this.out=out;
	}
	
	private String getParseExceptionInfo(SAXParseException spe) {
		String systemId = spe.getSystemId();
		if (systemId == null) {
			systemId = "null";
		}
		
		String info = "URI=" + systemId + " Line=" + spe.getLineNumber() +
				": " + spe.getMessage();
		return info;
	}

	@Override
	public void warning(SAXParseException spe) throws SAXException {
		out.println("Warning: " + getParseExceptionInfo(spe));
	}

	@Override
	public void error(SAXParseException spe) throws SAXException {
		String message = "Error: " + getParseExceptionInfo(spe);
		throw new SAXException(message);
	}

	@Override
	public void fatalError(SAXParseException spe) throws SAXException {
		String message = "Fatal Error: " + getParseExceptionInfo(spe);
		throw new SAXException(message);
	}

}
