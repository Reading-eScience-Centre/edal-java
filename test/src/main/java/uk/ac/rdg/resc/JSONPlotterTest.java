package uk.ac.rdg.resc;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.xml.bind.JAXBException;

import org.geotoolkit.referencing.crs.DefaultGeographicCRS;

import uk.ac.rdg.resc.edal.cdm.feature.NcGridSeriesFeatureCollection;
import uk.ac.rdg.resc.edal.coverage.domain.GridSeriesDomain;
import uk.ac.rdg.resc.edal.feature.GridSeriesFeature;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.geometry.impl.BoundingBoxImpl;
import uk.ac.rdg.resc.edal.graphics.style.FeatureCollectionAndMemberName;
import uk.ac.rdg.resc.edal.graphics.style.GlobalPlottingParams;
import uk.ac.rdg.resc.edal.graphics.style.Id2FeatureAndMember;
import uk.ac.rdg.resc.edal.graphics.style.StyleJSONParser;
import uk.ac.rdg.resc.edal.graphics.style.StyleXMLParser;
import uk.ac.rdg.resc.edal.graphics.style.datamodel.impl.Image;
import uk.ac.rdg.resc.edal.position.TimePosition;

public class JSONPlotterTest {
    public static void main(String[] args) throws IOException, InstantiationException,
            JAXBException {
        /*
         * First, get the dataset, and find some valid values for elevation and
         * time
         */
        final NcGridSeriesFeatureCollection featureCollection = new NcGridSeriesFeatureCollection(
                "testcollection", "Test Collection", "/home/bx904529/Data/FOAM_ONE/FOAM_one.ncml");

        final GridSeriesFeature feature = featureCollection.getFeatureById("testcollection1");
        GridSeriesDomain domain = feature.getCoverage().getDomain();

        Double vPos = null;
        try {
            vPos = domain.getVerticalAxis().getCoordinateValue(0);
        } catch (NullPointerException e) {
        }
        TimePosition tPos = null;
        try {
            tPos = domain.getTimeAxis().getCoordinateValue(0);
        } catch (NullPointerException e) {
        }

        /*
         * Now set the parameters for drawing the map.
         */
        int width = 800;
        int height = 400;

        BoundingBox bbox = domain.getHorizontalGrid().getCoordinateExtent();
//        BoundingBox bbox = new BoundingBoxImpl(150, 15, 180, 45, DefaultGeographicCRS.WGS84);
        GlobalPlottingParams params = new GlobalPlottingParams(width, height, bbox, null, null,
                vPos, tPos);

        /*
         * A simple interface mapping strings to FeatureCollection + member
         * name. This just uses the member name and accesses our only
         * FeatureCollection.
         * 
         * In edal-ncwms, this would be map "dataset/variable" to the
         * appropriate FeatureCollection
         */
        Id2FeatureAndMember id2Feature = new Id2FeatureAndMember() {
            @Override
            public FeatureCollectionAndMemberName getFeatureAndMemberName(String id) {
                return new FeatureCollectionAndMemberName(featureCollection, id);
            }
        };

        /*
         * Now get all files in the "xml" directory
         */
        File files = new File(ClassLoader.getSystemResource("json").getFile());
        for (File file : files.listFiles()) {
//        File file = new File(ClassLoader.getSystemResource("json/subsampled_glyph.txt").getFile());{
            try {
                /*
                 * Read each file, deserialise to an image, and then render and save
                 * the image
                 */
                String jsonString = readFile(file);
                String xmlString = StyleJSONParser.JSONtoXMLString(jsonString);
                Image image = StyleXMLParser.deserialise(xmlString);
    
                File outputFile = new File("/home/bx904529/jsonOutput/", file.getName().replaceAll("txt$",
                        "png"));
                long t1 = System.nanoTime();
                BufferedImage drawnImage = image.drawImage(params, id2Feature);
                long t2 = System.nanoTime();
                System.out.println((t2-t1)/1000000);
//                BufferedImage legend = image.getLegend(100);
//                if(legend != null) {
//                    drawnImage.createGraphics().drawImage(legend, 0, 0, null);
//                    ImageIO.write(legend, "png", outputFile);
//                }
                ImageIO.write(drawnImage, "png", outputFile);
            } catch (Exception e) {
                System.out.println("Problem with "+file.getAbsolutePath()+": "+e.getMessage());
                e.printStackTrace();
            }
        }

    }

    private static String readFile(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = null;
        StringBuilder stringBuilder = new StringBuilder();
        String ls = System.getProperty("line.separator");

        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
            stringBuilder.append(ls);
        }
        reader.close();

        return stringBuilder.toString();
    }
}