package uk.ac.rdg.resc;

import java.io.InputStream;

import net.sf.json.JSON;
import net.sf.json.JSONSerializer;
import net.sf.json.xml.XMLSerializer;

import org.apache.commons.io.IOUtils;

public class ConvertJSONtoXML {

	public static void main(String[] args) throws Exception {
		
		InputStream is = ConvertJSONtoXML.class.getResourceAsStream("/json/blackening.txt");
		String jsonData = IOUtils.toString(is);
		
		XMLSerializer serializer = new XMLSerializer();
		JSON json = JSONSerializer.toJSON(jsonData);
		serializer.setTypeHintsEnabled(false);
		serializer.setNamespace("resc", "http://www.resc.reading.ac.uk","Image");
		String xml = serializer.write(json);
		xml = xml.replaceAll("Image", "resc:Image");
		xml = xml.replaceAll("<o>", "");
		xml = xml.replaceAll("</o>", "");
		xml = xml.replaceAll("<e>", "");
		xml = xml.replaceAll("</e>", "");

		System.out.println(xml);
	}
	
}
