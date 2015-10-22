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
import java.util.Map.Entry;
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

import uk.ac.rdg.resc.edal.dataset.AbstractPointDataset;
import uk.ac.rdg.resc.edal.dataset.Dataset;
import uk.ac.rdg.resc.edal.dataset.DatasetFactory;
import uk.ac.rdg.resc.edal.dataset.DiscreteFeatureReader;
import uk.ac.rdg.resc.edal.dataset.FeatureIndexer;
import uk.ac.rdg.resc.edal.dataset.PRTreeFeatureIndexer;
import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.domain.SimpleHorizontalDomain;
import uk.ac.rdg.resc.edal.domain.TemporalDomain;
import uk.ac.rdg.resc.edal.exceptions.DataReadingException;
import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.feature.DiscreteFeature;
import uk.ac.rdg.resc.edal.feature.PointFeature;
import uk.ac.rdg.resc.edal.feature.PointSeriesFeature;
import uk.ac.rdg.resc.edal.feature.ProfileFeature;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.geometry.BoundingBoxImpl;
import uk.ac.rdg.resc.edal.grid.TimeAxis;
import uk.ac.rdg.resc.edal.grid.TimeAxisImpl;
import uk.ac.rdg.resc.edal.metadata.Parameter;
import uk.ac.rdg.resc.edal.metadata.VariableMetadata;
import uk.ac.rdg.resc.edal.position.GeoPosition;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.util.Array1D;
import uk.ac.rdg.resc.edal.util.ImmutableArray1D;
import uk.ac.rdg.resc.edal.util.PlottingDomainParams;
import uk.ac.rdg.resc.edal.util.TimeUtils;
import uk.ac.rdg.resc.edal.util.ValuesArray1D;

public class WaterMLDatasetFactory extends DatasetFactory {

    @Override
    public Dataset createDataset(String id, String location) throws IOException, EdalException {
        try {
            Map<String, HorizontalPosition> sites = new HashMap<>();
            File wmlSites = new File(location + "/" + "GetSiteInfoFile.xml");
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder;
            builder = factory.newDocumentBuilder();
            Document doc = builder.parse(wmlSites);
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            XPathExpression expr = xpath.compile("timeSeriesResponse/timeSeries");
            NodeList list = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
            for (int i = 0; i < list.getLength(); i++) {
                Node node = list.item(i);
                String siteCode = xpath.compile("sourceInfo/siteCode").evaluate(node);
                Double latitude = (Double) xpath.compile(
                        "sourceInfo/geoLocation/geogLocation/latitude").evaluate(node,
                        XPathConstants.NUMBER);
                Double longitude = (Double) xpath.compile(
                        "sourceInfo/geoLocation/geogLocation/longitude").evaluate(node,
                        XPathConstants.NUMBER);
                sites.put(siteCode, new HorizontalPosition(longitude, latitude,
                        DefaultGeographicCRS.WGS84));
            }
            for (Entry<String, HorizontalPosition> entry : sites.entrySet()) {
                System.out.println(entry.getKey() + " --> " + entry.getValue());
            }

            File wmlData = new File(location + "/" + "ExportValues.xml");
            builder = factory.newDocumentBuilder();
            doc = builder.parse(wmlData);
            expr = xpath.compile("timeSeriesResponse/timeSeries");
            list = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
            Map<String, Map<String, Array1D<Number>>> values = new HashMap<>();
            Map<String, TimeAxis> axes = new HashMap<>();
            Map<String, Parameter> parameters = new HashMap<>();
            for (int i = 0; i < list.getLength(); i++) {
                Node node = list.item(i);
                String varId = xpath.compile("variable/variableCode").evaluate(node);
                if(varId.equals("cota_d")) {
                    continue;
                }
                String siteCode = xpath.compile("sourceInfo/siteCode").evaluate(node);
                NodeList list2 = (NodeList) xpath.compile("values/value").evaluate(node,
                        XPathConstants.NODESET);
                List<DateTime> axisValues = new ArrayList<>();
                Array1D<Number> dataValues = new ValuesArray1D(list2.getLength());
                for (int j = 0; j < list2.getLength(); j++) {
                    Node n = list2.item(j);
                    DateTime time = TimeUtils.iso8601ToDateTime(
                            n.getAttributes().getNamedItem("dateTime").getNodeValue(),
                            ISOChronology.getInstance());
                    axisValues.add(time);
                    Double value = Double.parseDouble(n.getTextContent());
                    if (value != null && value == -9999) {
                        value = null;
                    }
                    dataValues.set(value, j);
                }
                if (!values.containsKey(siteCode)) {
                    Map<String, Array1D<Number>> varValues = new HashMap<>();
                    values.put(siteCode, varValues);
                }
                
                values.get(siteCode).put(varId ,dataValues);
                System.out.println(dataValues.getShape()[0]);

                if (!axes.containsKey(siteCode)) {
                    /*
                     * Assumes that time axes are the same for all variables.
                     * Need to check this is true...
                     */
                    axes.put(siteCode, new TimeAxisImpl("time", axisValues));
                }
                /*
                 * TODO this can be got from the waterML
                 */
                parameters.put(varId, new Parameter(varId, varId, varId, "m", "water level"));
            }
            /*
             * We now have data for all PSFs
             */
            int i=0;
            Map<String, PointSeriesFeature> features = new HashMap<>();
            for (String site : values.keySet()) {
                PointSeriesFeature feature = new PointSeriesFeature(site,
                        "Timeseries feature at site " + site, "", axes.get(site), sites.get(site), null, parameters,
                        values.get(site));
                features.put("feature"+(i++), feature);
            }

            Collection<VariableMetadata> metadata = new ArrayList<>();
            for(Entry<String, Parameter> entry : parameters.entrySet()){
                /*
                 * TOTAL HACK!!!
                 * 
                 * Just use a SimpleTemporalDomain
                 * 
                 * This works but it's a quick hack, as proof of concept
                 */
                metadata.add(new VariableMetadata(entry.getValue(), new SimpleHorizontalDomain(-180, -90, 180, 90), null, axes.get("TP1741")));
            }
            return new WaterMLDataset(id, metadata, features);
        } catch (ParserConfigurationException | SAXException | XPathExpressionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    class WaterMLDataset extends AbstractPointDataset<PointSeriesFeature> {
        public WaterMLDataset(String id, Collection<VariableMetadata> vars, final Map<String, PointSeriesFeature> features) {
            super(id, vars, new FeatureIndexer(){

                @Override
                public Collection<String> findFeatureIds(BoundingBox horizontalExtent,
                        Extent<Double> verticalExtent, Extent<DateTime> timeExtent,
                        Collection<String> variableIds) {
                    Collection<String> ret = new ArrayList<>();
                    for(Entry<String, PointSeriesFeature> f : features.entrySet()) {
                        if(horizontalExtent.contains(f.getValue().getHorizontalPosition())){
                            ret.add(f.getKey());
                        }
                    }
                    return ret;
                }

                @Override
                public Set<String> getAllFeatureIds() {
                    return features.keySet();
                }

                @Override
                public void addFeatures(List<FeatureBounds> features) {
                    throw new UnsupportedOperationException();
                }});
            featureReader = new WaterMLFeatureReader(features);
            timeAxis = features.get(features.keySet().iterator().next()).getDomain();
        }

        private TimeAxis timeAxis;
        private WaterMLFeatureReader featureReader;

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
        protected PointFeature convertFeature(PointSeriesFeature feature,
                PlottingDomainParams params) {
            TimeAxis timeAxis = feature.getDomain();
            int index = timeAxis.findIndexOf(params.getTargetT());
            Map<String, Array1D<Number>> values = new HashMap<>();
            for (String paramId : feature.getParameterIds()) {
                values.put(paramId, new ImmutableArray1D<>(new Number[] { feature
                        .getValues(paramId).get(index) }));
            }

            return new PointFeature(feature.getId(), feature.getName(), feature.getDescription(),
                    new GeoPosition(feature.getHorizontalPosition(), feature.getVerticalPosition(),
                            timeAxis.getCoordinateValue(index)), feature.getParameterMap(), values);
        }

        @Override
        public DiscreteFeatureReader<PointSeriesFeature> getFeatureReader() {
            return featureReader;
        }

        @Override
        protected BoundingBox getDatasetBoundingBox() {
            return BoundingBoxImpl.global();
        }

        @Override
        protected Extent<Double> getDatasetVerticalExtent() {
            return null;
        }

        @Override
        protected Extent<DateTime> getDatasetTimeExtent() {
            return timeAxis.getCoordinateExtent();
        }

    }

    private final class WaterMLFeatureReader implements DiscreteFeatureReader<PointSeriesFeature> {
        private Map<String, PointSeriesFeature> features;

        public WaterMLFeatureReader(Map<String, PointSeriesFeature> features) {
            this.features = features;
        }

        @Override
        public PointSeriesFeature readFeature(String id, Set<String> variableIds)
                throws DataReadingException {
            System.out.println("trying to read id: "+id+","+features.get(id));
            
            return features.get(id);
        }

        @Override
        public Collection<PointSeriesFeature> readFeatures(Collection<String> ids,
                Set<String> variableIds) throws DataReadingException {
            Collection<PointSeriesFeature> ret = new ArrayList<>();
            for(String id : features.keySet()){
                System.out.println(id+" is a real id");
            }
            for(String id : ids) {
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
