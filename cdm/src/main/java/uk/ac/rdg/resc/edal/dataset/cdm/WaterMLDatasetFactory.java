/*******************************************************************************
 * Copyright (c) 2015 The University of Reading
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

package uk.ac.rdg.resc.edal.dataset.cdm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import uk.ac.rdg.resc.edal.dataset.PointDataset;
import uk.ac.rdg.resc.edal.dataset.Dataset;
import uk.ac.rdg.resc.edal.dataset.DatasetFactory;
import uk.ac.rdg.resc.edal.dataset.DiscreteFeatureReader;
import uk.ac.rdg.resc.edal.dataset.FeatureIndexer;
import uk.ac.rdg.resc.edal.dataset.FeatureIndexer.FeatureBounds;
import uk.ac.rdg.resc.edal.dataset.PRTreeFeatureIndexer;
import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.domain.HorizontalDomain;
import uk.ac.rdg.resc.edal.domain.SimpleHorizontalDomain;
import uk.ac.rdg.resc.edal.domain.TemporalDomain;
import uk.ac.rdg.resc.edal.exceptions.DataReadingException;
import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.feature.DiscreteFeature;
import uk.ac.rdg.resc.edal.feature.PointFeature;
import uk.ac.rdg.resc.edal.feature.PointSeriesFeature;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.grid.TimeAxisImpl;
import uk.ac.rdg.resc.edal.metadata.Parameter;
import uk.ac.rdg.resc.edal.metadata.VariableMetadata;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.util.Array1D;
import uk.ac.rdg.resc.edal.util.GISUtils;
import uk.ac.rdg.resc.edal.util.TimeUtils;
import uk.ac.rdg.resc.edal.util.ValuesArray1D;

public class WaterMLDatasetFactory extends DatasetFactory {

    @Override
    public Dataset createDataset(String id, String location, boolean forceRefresh)
            throws IOException, EdalException {
        try {
            /*
             * First we read the GetSiteInfoFile.xml which maps site codes to
             * physical locations.
             */
            Map<String, HorizontalPosition> sites = new HashMap<>();
            File wmlSites = new File(location + "/" + "GetSiteInfoFile.xml");
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(wmlSites);
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            /*
             * Find all elements within timeSeriesResponse/timeSeries - this is
             * a list of nodes each of which contain an individual site
             * definition
             */
            NodeList sitesList = (NodeList) xpath.compile("timeSeriesResponse/timeSeries")
                    .evaluate(doc, XPathConstants.NODESET);
            /*
             * Precompile some XPath expressions for convenience
             */
            XPathExpression siteCodeExpr = xpath.compile("sourceInfo/siteCode");
            XPathExpression latitudeExpr = xpath
                    .compile("sourceInfo/geoLocation/geogLocation/latitude");
            XPathExpression longitudeExpr = xpath
                    .compile("sourceInfo/geoLocation/geogLocation/longitude");
            for (int i = 0; i < sitesList.getLength(); i++) {
                Node site = sitesList.item(i);
                String siteCode = siteCodeExpr.evaluate(site);
                Double latitude = (Double) latitudeExpr.evaluate(site, XPathConstants.NUMBER);
                Double longitude = (Double) longitudeExpr.evaluate(site, XPathConstants.NUMBER);
                sites.put(siteCode, new HorizontalPosition(longitude, latitude,
                        DefaultGeographicCRS.WGS84));
            }
            /*
             * We now have a Map of site code to physical location.
             * 
             * Now read the ExportValues file which contains the actual
             * timeseries
             */

            File wmlData = new File(location + "/" + "ExportValues.xml");
            builder = factory.newDocumentBuilder();
            doc = builder.parse(wmlData);
            NodeList timeseriesList = (NodeList) xpath.compile("timeSeriesResponse/timeSeries")
                    .evaluate(doc, XPathConstants.NODESET);

            Collection<PointSeriesFeature> features = new ArrayList<>();
            XPathExpression varIdExpr = xpath.compile("variable/variableCode");
            XPathExpression typeExpr = xpath.compile("variable/valueType");
            XPathExpression valuesExpr = xpath.compile("values/value");
            XPathExpression unitsExpr = xpath.compile("variable/units");
            for (int i = 0; i < timeseriesList.getLength(); i++) {
                Node timeseries = timeseriesList.item(i);
                String siteCode = siteCodeExpr.evaluate(timeseries);
                String varId = varIdExpr.evaluate(timeseries);
                String featureId = siteCode + ":" + varId;
                NodeList valuesList = (NodeList) valuesExpr.evaluate(timeseries,
                        XPathConstants.NODESET);
                List<DateTime> axisValues = new ArrayList<>();
                Array1D<Number> dataValues = new ValuesArray1D(valuesList.getLength());
                for (int j = 0; j < valuesList.getLength(); j++) {
                    Node valueNode = valuesList.item(j);
                    DateTime time = TimeUtils.iso8601ToDateTime(valueNode.getAttributes()
                            .getNamedItem("dateTime").getNodeValue(), ISOChronology.getInstance());
                    axisValues.add(time);
                    Double value = Double.parseDouble(valueNode.getTextContent());
                    if (value != null && value == -9999) {
                        value = null;
                    }
                    dataValues.set(value, j);
                }
                Map<String, Array1D<Number>> valuesMap = new HashMap<>();
                valuesMap.put(varId, dataValues);
                Node evaluate = (Node) unitsExpr.evaluate(timeseries, XPathConstants.NODE);
                Map<String, Parameter> parameters = new HashMap<>();
                parameters.put(varId, new Parameter(varId, varId, null, evaluate.getAttributes()
                        .getNamedItem("unitsCode").getNodeValue(), typeExpr.evaluate(timeseries)));
                features.add(new PointSeriesFeature(featureId, varId + " at " + siteCode,
                        "Timeseries feature of " + varId + " at site " + siteCode,
                        new TimeAxisImpl("Time axis for " + varId, axisValues),
                        sites.get(siteCode), null, parameters, valuesMap));
            }
            Collection<VariableMetadata> metadata = new ArrayList<>();
            /*
             * We can't use Parameter as the key to the bounds object, because
             * Parameters can be considered equal even if they have different
             * IDs
             */
            Map<Parameter, List<HorizontalDomain>> hDomains = new HashMap<>();
            Map<Parameter, List<TemporalDomain>> tDomains = new HashMap<>();
            for (PointSeriesFeature feature : features) {
                for (String parameterId : feature.getParameterIds()) {
                    Parameter parameter = feature.getParameter(parameterId);
                    if (!hDomains.containsKey(parameter)) {
                        hDomains.put(parameter, new ArrayList<HorizontalDomain>());
                        tDomains.put(parameter, new ArrayList<TemporalDomain>());
                    }
                    HorizontalPosition pos = feature.getHorizontalPosition();
                    hDomains.get(parameter).add(
                            new SimpleHorizontalDomain(pos.getX(), pos.getY(), pos.getX(), pos
                                    .getY(), pos.getCoordinateReferenceSystem()));
                    tDomains.get(parameter).add(feature.getDomain());
                }
            }
            for (Parameter p : hDomains.keySet()) {
                metadata.add(new VariableMetadata(p, GISUtils
                        .getIntersectionOfHorizontalDomains(hDomains.get(p).toArray(
                                new HorizontalDomain[0])), null, GISUtils
                        .getIntersectionOfTemporalDomains(tDomains.get(p).toArray(
                                new TemporalDomain[0]))));
            }
            FeatureIndexer featureIndexer = new PRTreeFeatureIndexer();
            List<FeatureBounds> featureBounds = new ArrayList<>();
            for (PointSeriesFeature feature : features) {
                featureBounds.add(FeatureBounds.fromPointSeriesFeature(feature));
            }
            featureIndexer.addFeatures(featureBounds);
            return new WaterMLDataset(id, metadata, featureIndexer, features);
        } catch (ParserConfigurationException | SAXException | XPathExpressionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    class WaterMLDataset extends PointDataset<PointSeriesFeature> {
        private WaterMLFeatureReader featureReader;

        public WaterMLDataset(String id, Collection<VariableMetadata> vars,
                FeatureIndexer featureIndexer, Collection<PointSeriesFeature> features) {
            super(id, vars, featureIndexer);
            featureReader = new WaterMLFeatureReader(features);
        }

        @Override
        public Class<? extends DiscreteFeature<?, ?>> getFeatureType(String variableId) {
            return PointSeriesFeature.class;
        }

        @Override
        public boolean supportsProfileFeatureExtraction(String varId) {
            return false;
        }

        @Override
        public boolean supportsTimeseriesExtraction(String varId) {
            return true;
        }

        @Override
        protected PointFeature convertFeature(PointSeriesFeature feature, BoundingBox hExtent,
                Extent<Double> zExtent, Extent<DateTime> tExtent, Double targetZ, DateTime targetT) {
            return convertPointSeriesFeature(feature, targetT);
        }

        @Override
        public DiscreteFeatureReader<PointSeriesFeature> getFeatureReader() {
            return featureReader;
        }
    }

    private final class WaterMLFeatureReader implements DiscreteFeatureReader<PointSeriesFeature> {
        private Map<String, PointSeriesFeature> features;

        public WaterMLFeatureReader(Collection<PointSeriesFeature> features) {
            this.features = new HashMap<>();
            for (PointSeriesFeature feature : features) {
                this.features.put(feature.getId(), feature);
            }
        }

        @Override
        public PointSeriesFeature readFeature(String id, Set<String> variableIds)
                throws DataReadingException {
            return features.get(id);
        }

        @Override
        public Collection<PointSeriesFeature> readFeatures(Collection<String> ids,
                Set<String> variableIds) throws DataReadingException {
            Collection<PointSeriesFeature> ret = new ArrayList<>();
            for (String id : ids) {
                ret.add(readFeature(id, variableIds));
            }
            return ret;
        }
    }

    public static void main(String[] args) throws EdalException, IOException {
        WaterMLDatasetFactory factory = new WaterMLDatasetFactory();
        factory.createDataset("testwml", "/home/guy/Data/wml/");
    }
}
