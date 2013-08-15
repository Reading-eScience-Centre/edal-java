package uk.ac.rdg.resc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import uk.ac.rdg.resc.edal.cdm.feature.NcGridSeriesFeatureCollection;
import uk.ac.rdg.resc.edal.coverage.domain.GridSeriesDomain;
import uk.ac.rdg.resc.edal.feature.GridSeriesFeature;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.graphics.style.FeatureCollectionAndMemberName;
import uk.ac.rdg.resc.edal.graphics.style.GlobalPlottingParams;
import uk.ac.rdg.resc.edal.graphics.style.Id2FeatureAndMember;
import uk.ac.rdg.resc.edal.graphics.style.StyleSLDParser;
import uk.ac.rdg.resc.edal.position.TimePosition;

public class SLDParserTest {

	public static void main(String[] args) throws IOException {
        /*
         * First, get the dataset, and find some valid values for elevation and time
         */
//        final NcGridSeriesFeatureCollection featureCollection = new NcGridSeriesFeatureCollection(
//                "testcollection", "Test Collection", "N:/Data/FOAM_ONE/FOAM_one.ncml");
        final NcGridSeriesFeatureCollection featureCollection = new NcGridSeriesFeatureCollection(
                "testcollection", "Test Collection", "N:/Data/ostia.nc");

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

        File xmlFile = new File(ClassLoader.getSystemResource("xml/se_interpolate.xml").getFile());
        File imageFile = new File("N:/xmlOutput/se_interpolate.png");
		try {
			StyleSLDParser.SLDtoXMLString(xmlFile, imageFile, params, id2Feature);
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
		} catch (IllegalArgumentException iae) {
			iae.printStackTrace();
		}
	}
}
