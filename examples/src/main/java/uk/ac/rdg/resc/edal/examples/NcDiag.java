/*******************************************************************************
 * Copyright (c) 2016 The University of Reading
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the University of Reading, nor the names of the
 *    authors or contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/

package uk.ac.rdg.resc.edal.examples;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;

import org.opengis.metadata.extent.GeographicBoundingBox;

import uk.ac.rdg.resc.edal.dataset.DataSource;
import uk.ac.rdg.resc.edal.dataset.Dataset;
import uk.ac.rdg.resc.edal.dataset.DiscreteLayeredDataset;
import uk.ac.rdg.resc.edal.dataset.cdm.CdmGridDatasetFactory;
import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.graphics.utils.GraphicsUtils;
import uk.ac.rdg.resc.edal.grid.TimeAxis;
import uk.ac.rdg.resc.edal.grid.VerticalAxis;
import uk.ac.rdg.resc.edal.metadata.DiscreteLayeredVariableMetadata;
import uk.ac.rdg.resc.edal.metadata.VariableMetadata;

/**
 * <p>
 * Diagnostic tool for testing CDM datasets without loading them into ncWMS. If
 * there are any problems reading data using ncWMS, use this tool to find out
 * more information.
 * </p>
 * 
 * @author Jon Blower
 * @author Guy Griffiths
 */
public final class NcDiag {
    private static int id = 0;

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Usage: NcDiag <input filename/directory> <output directory>");
            System.exit(-1);
        }
        File inFile = new File(args[0]);
        List<String> filesToAnalyse = new ArrayList<>();
        if (inFile.isDirectory()) {
            recursivelyAddFilesToList(inFile, filesToAnalyse);
        } else {
            filesToAnalyse.add(inFile.getAbsolutePath());
        }
        File outFile = new File(args[1]);
        if (!outFile.isDirectory()) {
            System.err.println("Usage: NcDiag <input filename/directory> <output directory>");
            System.err.println("\t\tThe second argument must be an existing directory");
            System.exit(-1);
        }

        PrintStream ps = new PrintStream(new File(outFile, "ncdiag.html"));
        printHeader(ps);
        for (String filename : filesToAnalyse) {
            diagnoseDataset(filename, ps, outFile.getAbsolutePath());
        }
        printFooter(ps);

        ps.close();
    }

    private static void recursivelyAddFilesToList(File directory, List<String> list) {
        List<File> directories = new ArrayList<>();
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    directories.add(file);
                } else {
                    list.add(file.getAbsolutePath());
                }
            }
        }
        for (File dir : directories) {
            recursivelyAddFilesToList(dir, list);
        }
    }

    private static void diagnoseDataset(String filename, PrintStream ps, String imageDir) {
        ps.printf("<h1>Report from %s</h1>%n", filename);
        CdmGridDatasetFactory df = new CdmGridDatasetFactory();
        DiscreteLayeredDataset<? extends DataSource, ? extends DiscreteLayeredVariableMetadata> dataset;
        try {
            dataset = df.createDataset("dataset" + (id++), filename);
        } catch (Exception e) {
            ps.println("<h2>File could not be read as a dataset:</h2>");
            ps.println("<h2>" + filename + "</h2>");
            ps.println("<h2>Stack trace follows:</h2>\n");
            e.printStackTrace(ps);
            return;
        }
        Set<String> variableIds = dataset.getVariableIds();
        for (String variableId : variableIds) {
            VariableMetadata variableMetadata = dataset.getVariableMetadata(variableId);
            try {
                printInfo(ps, variableMetadata, dataset, filename, imageDir);
            } catch (Exception e) {
                ps.println("<h2>Variable could not be read:</h2>");
                ps.println("<h2>" + variableId + "</h2>");
                ps.println("<h2>Stack trace follows:</h2>\n");
                e.printStackTrace(ps);
            }
        }
    }

    /**
     * Prints the HTTP header to the given PrintStream
     */
    private static void printHeader(PrintStream ps) {
        ps.println("<html>");
        ps.printf("<head><title>Diagnosis of datasets</title></head>%n");
        ps.println("<body>");
    }

    /**
     * Prints information about the given variable.
     */
    private static void printInfo(PrintStream ps, VariableMetadata variableMetadata,
            Dataset dataset, String filename, String imageDir) throws IOException {
        ps.println("<hr />");
        ps.printf("<h2>Variable: %s</h2>%n", variableMetadata.getId());
        ps.println("<table>");
        ps.println("<tbody>");
        printTableLine(ps, "Title", variableMetadata.getParameter().getTitle());
        printTableLine(ps, "Units", variableMetadata.getParameter().getUnits());
        printTableLine(ps, "Description", variableMetadata.getParameter().getDescription());
        GeographicBoundingBox bbox = variableMetadata.getHorizontalDomain()
                .getGeographicBoundingBox();
        printTableLine(
                ps,
                "Geographic Bounding box",
                String.format("%f,%f,%f,%f", bbox.getWestBoundLongitude(),
                        bbox.getSouthBoundLatitude(), bbox.getEastBoundLongitude(),
                        bbox.getNorthBoundLatitude()));
        if (variableMetadata.getVerticalDomain() != null) {
            if (variableMetadata.getVerticalDomain() instanceof VerticalAxis) {
                printTableLine(
                        ps,
                        "Elevation axis",
                        String.format("%d values",
                                ((VerticalAxis) variableMetadata.getVerticalDomain()).size()));
            }
        }
        if (variableMetadata.getTemporalDomain() != null) {
            if (variableMetadata.getTemporalDomain() instanceof TimeAxis) {
                printTableLine(ps, "Time axis ("
                        + variableMetadata.getTemporalDomain().getChronology() + ")",
                        String.format("%d values",
                                ((TimeAxis) variableMetadata.getTemporalDomain()).size()));
            }
        }
        ps.println("</tbody>");
        ps.println("</table>");

        if (variableMetadata.isScalar()) {
            int width = 256;
            int height = 256;
            BufferedImage im = GraphicsUtils.plotDefaultImage(dataset, variableMetadata.getId(),
                    width, height);
            Extent<Float> dataRange = GraphicsUtils.estimateValueRange(dataset,
                    variableMetadata.getId());
            String imageFilename = imageDir + "/" + dataset.getId() + "-"
                    + variableMetadata.getId() + ".png";
            ImageIO.write(im, "png", new File(imageFilename));
            ps.printf("<p>Data min: %f, max: %f<br />", dataRange.getLow(), dataRange.getHigh());
            ps.printf("<img src=\"%s\" width=\"%d\" height=\"%d\" /></p>%n", imageFilename, width,
                    height);
        } else {
            ps.println("Not a scalar field - plotting is more complex, but there is no reason to think this won't work in ncWMS2");
        }
    }

    private static void printTableLine(PrintStream ps, String title, String value) {
        ps.printf("<tr><td><b>%s:</b></td><td>%s</td></tr>%n", title, value);
    }

    /**
     * Prints the HTTP footer to the given PrintStream
     */
    private static void printFooter(PrintStream ps) {
        ps.println("</body>");
        ps.println("</html>");
    }
}
