package uk.ac.rdg.resc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import uk.ac.rdg.resc.edal.graphics.style.StyleSLDParser;

public class SLDParserTest {

	public static void main(String[] args) {
        File file = new File(ClassLoader.getSystemResource("xml/se_threshold_1.xml").getFile());
		try {
			String xml = StyleSLDParser.SLDtoXMLString(file);
			System.out.println(xml);
		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (SAXException se) {
			se.printStackTrace();
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (XPathExpressionException xee) {
			xee.printStackTrace();
		}
	}
}
