package uk.ac.rdg.resc.edal.graphics.pcolor.examples;


import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Paths;

/**
 * Example that outputs an HTML page that shows a matrix of colors of identical
 * hue but varying saturation and brightness.
 */
public class BVector {
	
	public static void main(String[] args) throws Throwable {
		if (args.length < 3) {
			System.err.println("Please specify a file and then H (0.0-1.0) and S (0.0-1.0)");
			return;
		}
		new BVector().emitTable(
				new OutputStreamWriter(new FileOutputStream(Paths.get(args[0]).toFile())),
				Float.parseFloat(args[1]), Float.parseFloat(args[2]));
	}

	public void emitTable(OutputStreamWriter out, float theH, float theS) throws Throwable {
		out.write("<!DOCTYPE html>\n");
		out.write("<html>\n");
		out.write("<body style='background-color: #757575; text-color:#bbb'>\r\n");
		out.write(String.format("<h3>Colors of equal Hue (H = %.1f)</h3>\r\n", theH));
		out.write(String.format("<p>and Saturation (S = %.1f), Brightness (B) spread uniformly according to HSB.</p>", theS));
		out.write("<table width = \"90%\">\r\n");
		
		// create XML output file
		OutputStreamWriter xmlOut = new OutputStreamWriter(
				new FileOutputStream(new File("BVector.xml")));
		xmlOut.write(String.format("                <!-- Colors of equal Hue (H = %.1f) -->\r\n", theH));
		xmlOut.write(String.format("                <!-- and Saturation (S = %.1f), Brightness (B) varied according to HSB. -->\r\n", theS));
		
		// table header - same as inner loop plus start column
		out.write("<th>Start color</th>");
		out.write(String.format("<th>(S = %.1f)</th>\r\n", theS));

		// color table
		out.write("<tr>\n");
		for (int i = 40; i >= 1; i--) {
			float B = (float)i/40.0F;
			out.write(String.format("<td>(B = %.3f)</td>\n", B));
			Color col = Color.getHSBColor(theH, theS, B);
			String hex = String.format("#%02x%02x%02x", col.getRed(), col.getGreen(), col.getBlue());
			out.write(String.format("    <td bgcolor=%s>%s</td>\n", hex, hex));
			xmlOut.write("                  <se:Value>" + hex.toUpperCase() + "</se:Value>\r\n");
			if (i > 1) {
				float t = 312.0F - (float)i;
				xmlOut.write(String.format("                  <se:Threshold>%.1f</se:Threshold>\r\n", t));
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
