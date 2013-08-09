package uk.ac.rdg.resc.edal.graphics.style.util;

import net.sf.json.JSON;
import net.sf.json.JSONException;
import net.sf.json.JSONSerializer;
import net.sf.json.xml.XMLSerializer;

public class StyleJSONParser {

    /*
     * Convert a JSON string to an XML string for use by JAXB.
     */
    public static String JSONtoXMLString(String jsonString) throws JSONException {
    	XMLSerializer serializer = new XMLSerializer();
		
    	JSON json = JSONSerializer.toJSON(jsonString);
		
		// Do not add type hints for conversion from xml back to JSON
		serializer.setTypeHintsEnabled(false);
		// Set namespace property for Image element
		serializer.setNamespace("resc", "http://www.resc.reading.ac.uk", "Image");
		
		String xmlString = serializer.write(json);
		
		// Add resc namespace prefix to image element
		xmlString = xmlString.replaceAll("Image", "resc:Image");
		// Strip out all superfluous object names and array element names
		// added by the XML serializer
		xmlString = xmlString.replaceAll("<o>", "");
		xmlString = xmlString.replaceAll("</o>", "");
		xmlString = xmlString.replaceAll("<e>", "");
		xmlString = xmlString.replaceAll("</e>", "");
				
		return xmlString;
    }
    
}
