package uk.ac.rdg.resc.edal.graphics.pcolor.examples;


import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Paths;

/**
 * Creates a 2D matrix of varying Saturation S and Brightness B given the hue H as a
 * command line argument using the HSB colour model for comparison with CIECAM02. The
 * results are output as a HTML table which is saved in the working directory of the
 * project and a list of values in an XML file for thresholding which are saved in the
 * working directory of the project. NB. There are loops controlling the Saturation in
 * two places, once for generating the table headers and once for generating the colours. 
 * 
 * The filename of the HTML table and the hue are expected as command line arguments.
 */
public class SBMatrix {
	
	public static void main(String[] args) throws Throwable {
		if (args.length < 2) {
			System.err.println("Please specify a file and then H (0.0-1.0)");
			return;
		}
		new SBMatrix().emitTable(
				new OutputStreamWriter(new FileOutputStream(Paths.get(args[0]).toFile())),
				Float.parseFloat(args[1]));
	}

	public void emitTable(OutputStreamWriter out, float theH) throws Throwable {
		out.write("<!DOCTYPE html>\n");
		out.write("<html>\n");
		out.write("<body style='background-color: #757575; text-color:#bbb'>\r\n");
		out.write(String.format("<h3>Colors of equal Hue (H = %.1f)</h3>\r\n", theH));
		out.write("<p>Saturation (S) and Brightness (B) spread uniformly according to HSB.</p>");
		out.write("<table width = \"90%\">\r\n");
		
		// create XML output file
		OutputStreamWriter xmlOut = new OutputStreamWriter(
				new FileOutputStream(new File("SBMatrix.xml")));
		xmlOut.write(String.format("                <!-- Colors of equal Hue (H = %.1f) -->\r\n", theH));
		xmlOut.write("                <!-- saturation (S) and Brightness (B) varied according to HSB. -->\r\n");
		
		// table header - same as inner loop plus start column
		out.write("<th>Start color</th>");
		for (int i = 5; i >= 1; i -= 1) {
			float S = i/5.0F;
			out.write(String.format("<th>(S = %.1f)</th>\r\n", S));
		}
		// color table
		for (int i = 100; i >= 1; i--) {
			float B = i/100.0F;
			out.write("<tr>\n");
			out.write(String.format("<td>(B = %.2f)</td>\n", B));
			xmlOut.write(String.format("                <!-- B = %.2f -->\r\n", B));
			for (int j = 5; j >= 1; j -= 1) {
				float S = j/5.0F;
				Color col = Color.getHSBColor(theH, S, B);
				String hex = String.format("#%02x%02x%02x", col.getRed(), col.getGreen(), col.getBlue());
				out.write(String.format("    <td bgcolor=%s>%s</td>\n", hex, hex));
				xmlOut.write("                  <se:Value>" + hex.toUpperCase() + "</se:Value>\r\n");

			}
			out.write("</tr>\n");
		}
		out.write("</table>");	
		out.write("</body>");
		out.write("</html>");
		out.close();
		xmlOut.close();
	}

}
