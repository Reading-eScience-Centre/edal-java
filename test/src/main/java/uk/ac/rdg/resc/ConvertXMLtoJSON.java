package uk.ac.rdg.resc;

import java.io.InputStream;

import net.sf.json.JSON;
import net.sf.json.xml.XMLSerializer;

import org.apache.commons.io.IOUtils;

public class ConvertXMLtoJSON {

	public static void main(String[] args) throws Exception {

		InputStream is = ConvertJSONtoXML.class.getResourceAsStream("/xml/subsampled_glyph.xml");
		String xmlData = IOUtils.toString(is);
		
		XMLSerializer serializer = new XMLSerializer();
		JSON json = serializer.read(xmlData);
		
		System.out.println(json.toString(2));
		
	}

}
