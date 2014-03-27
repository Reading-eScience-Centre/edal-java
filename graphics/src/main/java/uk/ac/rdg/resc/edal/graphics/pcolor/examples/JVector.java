package uk.ac.rdg.resc.edal.graphics.pcolor.examples;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Paths;

import de.fhg.igd.pcolor.CAMLab;
import de.fhg.igd.pcolor.CAMLch;
import de.fhg.igd.pcolor.PColor;
import de.fhg.igd.pcolor.sRGB;
import de.fhg.igd.pcolor.colorspace.CS_CAMLab;
import de.fhg.igd.pcolor.colorspace.CS_CAMLch;
import de.fhg.igd.pcolor.util.ColorTools;
import de.fhg.igd.pcolor.util.MathTools;

/**
 * Creates a vector of varying Lightness J given Hue H and Colorfulness C as
 * command line arguments using the perceptually uniform CIECAM02 colour
 * model. The error delta E in encoding the CIECAM02 colour into sRGB is
 * calculated. A value less than 1 for this is good. The results are output
 * as a HTML table and a list of XML values for thresholding called JVector.xml
 * which are saved in the working directory of the project.
 * 
 * The filename of the HTML table, the hue and the colorfulness are expected
 * as command line arguments.
 */
public class JVector {

	public static void main(String[] args) throws Throwable {
		if (args.length < 3) {
			System.err.println("Please specify a file and then h (0-360) and C (0-100)");
			return;
		}
		new JVector().emitTable(
				new OutputStreamWriter(new FileOutputStream(Paths.get(args[0]).toFile())),
				Integer.parseInt(args[1]), Integer.parseInt(args[2]));
	}

	public void emitTable(OutputStreamWriter out, int theH, int theC) throws Throwable {
		CAMLch start_col = new CAMLch(new float[] {0, 0, theH}, 1, CS_CAMLch.defaultJChInstance);
		
		out.write("<!DOCTYPE html>\n");
		out.write("<html>\n");
		out.write("<body style='background-color: #757575; text-color:#bbb'>\r\n");
		out.write("<h3>Colors of equal Hue (h = " + start_col.get(CAMLch.h) + ")</h3>\r\n");
		out.write("<p>and Colorfulness (C = " + theC + ").</p>");
		out.write("<p>Lightness (J) varied according to CIECAM02.</p>");
		out.write("<table width = \"90%\">\r\n");
		
		// table header - same as inner loop plus start column
		out.write("<th>Start color</th>");
		out.write("<th>(C = " + Float.toString(theC) + ")</th>\r\n");

		// create XML output file
		OutputStreamWriter xmlOut = new OutputStreamWriter(
				new FileOutputStream(new File("JVector.xml")));
		xmlOut.write("                <!-- Colors of equal Hue (h = " + start_col.get(CAMLch.h) + ") -->\r\n");
		xmlOut.write("                <!-- and Colorfulness (C = " + theC + "). -->\r\n");
		xmlOut.write("                <!-- Lightness (J) varied according to CIECAM02. -->\r\n");
		
		// color table
		float d = 274.0f;
		for (float J = 85; J >= 15; J -= 2) {
			CAMLch col = ColorTools.setChannel(start_col, CAMLch.L, J);
			out.write("<tr>\n");
//				xmlOut.write("                <!-- J = " + J + " -->\r\n");
			String colStr = String.format("J %.0f C %.0f h %.0f", col.get(CAMLch.L), col.get(CAMLch.c), col.get(CAMLch.h));
			out.write(String.format("  <td>(J = %.0f) (" + colStr + ")</td>\n", J));
			col = ColorTools.setChannel(col, CAMLch.c, theC);
			float[] rgb_f = col.getColorSpace().toRGB(col.getComponents());
			int[] rgb = new int[3];
			for (int j = 0; j < 3; j++)
				rgb[j] = MathTools.saturate((int)(rgb_f[j] * 255.0), 0, 255);
			
			CAMLab col_ref = (CAMLab) PColor.convert(col, CS_CAMLab.defaultJaMbMInstance);
			CAMLab col_back = (CAMLab) PColor.convert(new sRGB(rgb[0] / 255.0f, rgb[1] / 255.0f, rgb[2] / 255.0f), CS_CAMLab.defaultInstance);
			float error = 0;
			for (int j = 0; j < 3; j++) {
				float diff = col_ref.get(j) - col_back.get(j);
				error += diff * diff;
			}
			
			CAMLch emitCol = col;
			error = (float) Math.sqrt(error);
			
			// finally, emit the color as a table cell
			out.write(String.format("    <td bgcolor=%s>delta E: %2.1f</td>\n", ColorTools.toHtml(emitCol, false), error));
			
			// write the color code to an XML file
			int argb = emitCol.getARGB();
			xmlOut.write("                <se:Value>#" + 
					Integer.toHexString(argb).toUpperCase() + "</se:Value>\r\n");
			if (d <= 308.0f) {
				xmlOut.write("                <se:Threshold>" + d + "</se:Threshold>\r\n");	
			}
			d += 1.0f;
		}
		out.write("</tr>\n");
		out.write("</table>");	
		out.write("</body>");
		out.write("</html>");
		out.close();
		xmlOut.close();
	}

}
