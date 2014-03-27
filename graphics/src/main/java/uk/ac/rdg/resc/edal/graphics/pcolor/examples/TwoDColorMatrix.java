package uk.ac.rdg.resc.edal.graphics.pcolor.examples;


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
 * Creates a 2D matrix of varying Colorfulness C and Hue H given the lightness J as
 * command line argument using the perceptually uniform CIECAM02 colour model. The
 * error delta E in encoding the CIECAM02 colour into sRGB is calculated. A value
 * less than 1 for this is good. The results are output as a HTML table which is saved
 * in the working directory of the project. NB. There are loops controlling the
 * Colorfulness in two places, once for generating the table headers and once for
 * generating the colours. 
 * 
 * The filename of the HTML table and the lightness are expected as command line
 * arguments.
 */

/**
 * Example that outputs an HTML page that shows a matrix of colors of identical J
 * but varying hue and colorfulness.
 * @author Simon Thum
 */
public class TwoDColorMatrix {
	
	public static void main(String[] args) throws Throwable {
		if (args.length < 2) {
			System.err.println("Please specify a file and then J (0-100)");
			return;
		}
		new TwoDColorMatrix().emitTable(
				new OutputStreamWriter(new FileOutputStream(Paths.get(args[0]).toFile())),
				Integer.parseInt(args[1]));
	}

	public void emitTable(OutputStreamWriter out, int theJ) throws Throwable {
		CAMLch start_col = new CAMLch(new float[] {theJ, 0, 0}, 1, CS_CAMLch.defaultJChInstance);
		
		out.write("<!DOCTYPE html>\n");
		out.write("<html>\n");
		out.write("<body style='background-color: #757575; text-color:#bbb'>\r\n");
		out.write("<h3>Colors of equal Lightness (J = " + start_col.get(CAMLch.L) + ")</h3>\r\n");
		out.write("<p>Colorfulness (C) and Hue (h) spread uniformly according to CIECAM02; darker colors are outside of the sRGB gamut.</p>");
		out.write("<table width = \"90%\">\r\n");
		
		final int numColors = 16;
		// table header - same as inner loop plus start column
		out.write("<th>Start color</th>");
		for (float C = 100; C >= 0; C -= 10) {
			out.write("<th>(C = " + Float.toString(C) + ")</th>\r\n");
		}
		// color table
		for (int i = 0; i < numColors; i++) {
			CAMLch col = ColorTools.setChannel(start_col, CAMLch.h, (float)(i * (360.0 / numColors)));
			out.write("<tr>\n");
			String colStr = String.format("J %.0f C %.0f h %.0f", col.get(CAMLch.L), col.get(CAMLch.c), col.get(CAMLch.h));
			out.write(String.format("  <td>%d (" + colStr + ")</td>\n", i));			
			for (float C = 100; C >= 0; C -= 10) {
				col = ColorTools.setChannel(col, CAMLch.c, C);
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
				// tone down cells with noticeable error
				error = (float) Math.sqrt(error);
				if (error > 1) {
					emitCol = ColorTools.setChannel(col, CAMLch.L, theJ / 2);
				}
				
				// finally, emit the color as a table cell
				out.write(String.format("    <td bgcolor=%s>delta E: %2.1f</td>\n", ColorTools.toHtml(emitCol, false), error));
			}
			out.write("</tr>\n");
		}
		out.write("</table>");	
		out.write("<p>2013 Simon Thum (Fraunhofer IGD)</p>");
		out.write("</body>");
		out.write("</html>");
		out.close();
	}

}
