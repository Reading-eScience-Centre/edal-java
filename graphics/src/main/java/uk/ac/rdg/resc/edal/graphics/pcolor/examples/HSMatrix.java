package uk.ac.rdg.resc.edal.graphics.pcolor.examples;


import java.awt.Color;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Paths;

/**
 * Creates a 2D matrix of varying Hue H and Saturation S given the Brightness B as
 * command line argument using the HSB colour model for comparison with CIECAM02. The
 * results are output as a HTML table which is saved in the working directory of the
 * project. NB. There are loops controlling the Saturation in two places, once for
 * generating the table headers and once for generating the colours. 
 * 
 * The filename of the HTML table and the brightness are expected as command line
 * arguments.
 */
	public class HSMatrix {
	
		public static void main(String[] args) throws Throwable {
		if (args.length < 2) {
			System.err.println("Please specify a file and then B (0.0-1.0)");
			return;
		}
		new HSMatrix().emitTable(
				new OutputStreamWriter(new FileOutputStream(Paths.get(args[0]).toFile())),
				Float.parseFloat(args[1]));
	}

	public void emitTable(OutputStreamWriter out, float theB) throws Throwable {
		out.write("<!DOCTYPE html>\n");
		out.write("<html>\n");
		out.write("<body style='background-color: #757575; text-color:#bbb'>\r\n");
		out.write(String.format("<h3>Colors of equal Brightness (B = %.1f)</h3>\r\n", theB));
		out.write("<p>Saturation (S) and Hue (H) spread uniformly according to HSB.</p>");
		out.write("<table width = \"90%\">\r\n");
		
		// table header - same as inner loop plus start column
		out.write("<th>Start color</th>");
		for (int i = 10; i >= 0; i--) {
			float S = i/10.0F;
			out.write(String.format("<th>(S = %.1f)</th>\r\n", S));
		}
		// color table
		for (int i = 10; i >= 0; i--) {
			float H = i/10.0F;
			out.write("<tr>\n");
			out.write(String.format("<td>(H = %.1f)</td>\n", H));			
			for (int j = 10; j >= 0; j--) {
				float S = j/10.0F;
				Color col = Color.getHSBColor(H, S, theB);
				String hex = String.format("#%02x%02x%02x", col.getRed(), col.getGreen(), col.getBlue());
				out.write(String.format("    <td bgcolor=%s>%s</td>\n", hex, hex));
			}
			out.write("</tr>\n");
		}
		out.write("</table>");	
		out.write("</body>");
		out.write("</html>");
		out.close();
	}

}
