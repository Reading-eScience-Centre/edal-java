/**
 * Copyright (c) 2013 The University of Reading
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
 */

package uk.ac.rdg.resc.edal.graphics;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Ellipse2D;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.title.Title;
import org.jfree.data.Range;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.AbstractXYZDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.XYZDataset;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.TextAnchor;
import org.joda.time.Chronology;
import org.joda.time.DateTime;

import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.exceptions.MismatchedCrsException;
import uk.ac.rdg.resc.edal.feature.Feature;
import uk.ac.rdg.resc.edal.feature.PointCollectionFeature;
import uk.ac.rdg.resc.edal.feature.PointSeriesFeature;
import uk.ac.rdg.resc.edal.feature.ProfileFeature;
import uk.ac.rdg.resc.edal.feature.TrajectoryFeature;
import uk.ac.rdg.resc.edal.geometry.LineString;
import uk.ac.rdg.resc.edal.graphics.style.ColourScheme;
import uk.ac.rdg.resc.edal.grid.VerticalAxis;
import uk.ac.rdg.resc.edal.metadata.Parameter;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.VerticalCrs;
import uk.ac.rdg.resc.edal.position.VerticalPosition;
import uk.ac.rdg.resc.edal.util.Array1D;
import uk.ac.rdg.resc.edal.util.GISUtils;
import uk.ac.rdg.resc.edal.util.TimeUtils;

/**
 * Code to produce various types of chart.
 * 
 * @author Guy Griffiths
 * @author Jon Blower
 * @author Kevin X. Yang
 */
final public class Charting {
    private static final Color TRANSPARENT = new Color(0, 0, 0, 0);
    private static final Locale US_LOCALE = new Locale("us", "US");
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance();
    static {
        NUMBER_FORMAT.setMinimumFractionDigits(0);
        NUMBER_FORMAT.setMaximumFractionDigits(4);
    }

    private static String getTitle(String type, Collection<String> varList,
            HorizontalPosition hPos) {
        StringBuilder title = new StringBuilder();

        String posString = null;
        if (hPos != null) {
            StringBuilder posSB = new StringBuilder();
            if (!GISUtils.isWgs84LonLat(hPos.getCoordinateReferenceSystem())) {
                hPos = GISUtils.transformPosition(hPos, GISUtils.defaultGeographicCRS());
            }
            if (hPos.getY() < 0) {
                posSB.append(NUMBER_FORMAT.format(-hPos.getY()));
                posSB.append("°S");
            } else {
                posSB.append(NUMBER_FORMAT.format(hPos.getY()));
                posSB.append("°N");
            }
            posSB.append(", ");
            if (hPos.getX() < 0) {
                posSB.append(NUMBER_FORMAT.format(-hPos.getX()));
                posSB.append("°W");
            } else {
                posSB.append(NUMBER_FORMAT.format(hPos.getX()));
                posSB.append("°E");
            }
            posString = posSB.toString();
        }

        if (varList.size() > 0) {
            StringBuilder varsSB = new StringBuilder();
            for (String varId : varList) {
                varsSB.append(varId);
                varsSB.append(", ");
            }
            varsSB.delete(varsSB.length() - 2, varsSB.length() - 1);
            title.append(type);
            title.append(" of ");
            if (varList.size() > 1) {
                title.append("variables: ");
            }
            title.append(varsSB.toString());

            if (posString != null) {
                title.append(" at ");
                title.append(posString);
            }
        } else {
            title.append("No data to plot ");
            if (posString != null) {
                title.append("at ");
                title.append(posString);
            }
        }
        return title.toString();
    }

    public static JFreeChart createVerticalProfilePlot(
            Collection<? extends ProfileFeature> features, String plottedQuantity,
            HorizontalPosition hPos, String copyrightStatement) throws MismatchedCrsException {

        Map<Parameter, XYDataset> param2SeriesCollection = new HashMap<>();
        Set<String> varList = new HashSet<String>();
        VerticalCrs vCrs = null;
        boolean invertYAxis = false;

        for (ProfileFeature feature : features) {
            if (vCrs == null) {
                vCrs = feature.getDomain().getVerticalCrs();
                if (vCrs != null) {
                    invertYAxis = !vCrs.isPositiveUpwards();
                }
            } else {
                if (!vCrs.equals(feature.getDomain().getVerticalCrs())) {
                    throw new MismatchedCrsException(
                            "All vertical CRSs must match to plot multiple profile plots");
                }
            }
            for (String varId : feature.getVariableIds()) {
                Parameter parameter = feature.getParameter(varId);
                XYSeriesCollection collection;

                if (param2SeriesCollection.containsKey(parameter)) {
                    collection = (XYSeriesCollection) param2SeriesCollection.get(parameter);
                } else {
                    collection = new XYSeriesCollection();
                    param2SeriesCollection.put(parameter, collection);
                }

                List<Double> elevationValues = feature.getDomain().getCoordinateValues();

                /*
                 * This is the label used for the legend.
                 */
                String legend = varId + " from feature " + feature.getId() + " at ("
                        + NUMBER_FORMAT.format(feature.getHorizontalPosition().getX()) + ","
                        + NUMBER_FORMAT.format(feature.getHorizontalPosition().getY()) + ")";
                if (feature.getTime() != null) {
                    legend += "- " + TimeUtils.formatUtcHumanReadableDateTime(feature.getTime());
                }
                XYSeries series = new XYSeries(legend, true);

                series.setDescription(feature.getParameter(varId).getDescription());

                boolean hasValues = false;
                for (int i = 0; i < elevationValues.size(); i++) {
                    if (feature.getValues(varId) == null) {
                        continue;
                    }
                    Number val = feature.getValues(varId).get(i);
                    if (val == null || Double.isNaN(val.doubleValue())) {
                        /*
                         * Don't add NaNs to the series
                         */
                        continue;
                    }
                    series.addOrUpdate(elevationValues.get(i), val);
                    hasValues = true;
                }

                if (hasValues) {
                    collection.addSeries(series);
                    varList.add(varId);
                }
            }
        }

        return getChart("Vertical profile", param2SeriesCollection, varList, hPos,
                copyrightStatement, "Depth", PlotOrientation.HORIZONTAL, invertYAxis);
    }

    /**
     * Creates a vertical axis for plotting the given elevation values from the
     * given layer
     */
    private static NumberAxis getZAxis(VerticalCrs vCrs) {
        /*
         * We can deal with three types of vertical axis: Height, Depth and
         * Pressure. The code for this is very messy in ncWMS, sorry about
         * that... We should improve this but there are possible knock-on
         * effects, so it's not a very easy job.
         */
        final String zAxisLabel;
        final boolean invertYAxis;
        if (vCrs != null && vCrs.isPositiveUpwards()) {
            zAxisLabel = "Height";
            invertYAxis = false;
        } else if (vCrs != null && vCrs.isPressure()) {
            zAxisLabel = "Pressure";
            invertYAxis = true;
        } else {
            zAxisLabel = "Depth";
            invertYAxis = true;
        }

        String units = "";
        if (vCrs != null) {
            units = " (" + vCrs.getUnits() + ")";
        }
        NumberAxis zAxis = new NumberAxis(zAxisLabel + units);
        zAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        zAxis.setInverted(invertYAxis);

        return zAxis;
    }

    public static JFreeChart createTimeSeriesPlot(Collection<? extends PointSeriesFeature> features,
            HorizontalPosition hPos, String copyrightStatement) throws MismatchedCrsException {
        Chronology chronology = null;
        Set<String> varList = new HashSet<>();
        Map<Parameter, XYDataset> param2SeriesCollection = new HashMap<>();

        for (PointSeriesFeature feature : features) {
            if (chronology == null) {
                chronology = feature.getDomain().getChronology();
            } else {
                if (!chronology.equals(feature.getDomain().getChronology())) {
                    throw new MismatchedCrsException(
                            "All chronologies must match to plot multiple time series plots");
                }
            }

            for (String varId : feature.getVariableIds()) {
                Parameter parameter = feature.getParameter(varId);
                TimeSeriesCollection collection;

                if (param2SeriesCollection.containsKey(parameter)) {
                    collection = (TimeSeriesCollection) param2SeriesCollection.get(parameter);
                } else {
                    collection = new TimeSeriesCollection();
                    param2SeriesCollection.put(parameter, collection);
                }

                List<DateTime> timeValues = feature.getDomain().getCoordinateValues();

                /*
                 * This is the label used for the legend.
                 */
                VerticalPosition zPos = feature.getVerticalPosition();
                String legend = varId + " from feature " + feature.getId() + " at ("
                        + NUMBER_FORMAT.format(feature.getHorizontalPosition().getY()) + ", "
                        + NUMBER_FORMAT.format(feature.getHorizontalPosition().getX()) + ")";
                if (zPos != null) {
                    legend += ", " + zPos.getZ();
                    if (zPos.getCoordinateReferenceSystem() != null) {
                        legend += zPos.getCoordinateReferenceSystem().getUnits();
                    }
                }
                TimeSeries series = new TimeSeries(legend);

                series.setDescription(feature.getParameter(varId).getDescription());

                boolean hasValues = false;
                for (int i = 0; i < timeValues.size(); i++) {
                    if (feature.getValues(varId) == null) {
                        continue;
                    }
                    Number val = feature.getValues(varId).get(i);
                    if (val == null || Double.isNaN(val.doubleValue())) {
                        /*
                         * Don't add NaNs to the series
                         */
                        continue;
                    }
                    series.addOrUpdate(new Millisecond(new Date(timeValues.get(i).getMillis())),
                            val);
                    hasValues = true;
                }

                if (hasValues) {
                    collection.addSeries(series);
                    varList.add(varId);
                }
            }
        }

        return getChart("Timeseries", param2SeriesCollection, varList, hPos, copyrightStatement,
                "Time", PlotOrientation.VERTICAL, false);
    }

    private static JFreeChart getChart(String type,
            Map<Parameter, XYDataset> parameter2SeriesCollection, Collection<String> varList,
            HorizontalPosition hPos, String copyrightStatement, String domainLabel,
            PlotOrientation orientation, boolean invertDomainAxis) {

        String title = getTitle(type, varList, hPos);

        XYPlot plot = new XYPlot();

        ValueAxis domainAxis;
        if (type.equalsIgnoreCase("Timeseries")) {
            DateAxis dateAxis = new DateAxis(domainLabel);
            dateAxis.setInverted(invertDomainAxis);
            domainAxis = dateAxis;
        } else {
            NumberAxis numberAxis = new NumberAxis(domainLabel);
            numberAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
            numberAxis.setAutoRangeIncludesZero(false);
            numberAxis.setInverted(invertDomainAxis);
            domainAxis = numberAxis;
        }
        plot.setDomainAxis(domainAxis);

        int i = 0;
        boolean legendNeeded = false;
        for (Entry<Parameter, XYDataset> entry : parameter2SeriesCollection.entrySet()) {
            if (i > 0) {
                legendNeeded = true;
            }
            XYDataset coll = entry.getValue();

            if (coll.getSeriesCount() > 0) {
                NumberAxis valueAxis = new NumberAxis();
                valueAxis.setAutoRangeIncludesZero(false);
                valueAxis.setAutoRange(true);
                valueAxis.setNumberFormatOverride(NUMBER_FORMAT);
                Parameter parameter = entry.getKey();
                valueAxis.setLabel(parameter.getStandardName() + " (" + parameter.getUnits() + ")");
                plot.setDataset(i, coll);
                plot.setRangeAxis(i, valueAxis);
                XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
                for (int j = 0; j < coll.getSeriesCount(); j++) {
                    renderer.setSeriesShape(j, new Ellipse2D.Double(-1.0, -1.0, 2.0, 2.0));
                    renderer.setSeriesShapesVisible(j, true);
                    if (j > 0) {
                        legendNeeded = true;
                    }
                }
                plot.setRenderer(i, renderer);
                plot.mapDatasetToRangeAxis(i, i);
                i++;
            }
        }

        plot.setNoDataMessage("There is no data for your choice");
        plot.setNoDataMessageFont(new Font("sansserif", Font.BOLD, 20));
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinePaint(Color.white);
        plot.setOrientation(orientation);

        /*
         * Use default font and create a legend if there are multiple lines
         */
        JFreeChart chart = new JFreeChart(title.toString(), null, plot, legendNeeded);

        if (copyrightStatement != null) {
            final TextTitle textTitle = new TextTitle(copyrightStatement);
            textTitle.setFont(new Font("SansSerif", Font.PLAIN, 10));
            textTitle.setPosition(RectangleEdge.BOTTOM);
            textTitle.setHorizontalAlignment(HorizontalAlignment.RIGHT);
            chart.addSubtitle(textTitle);
        }

        return chart;
    }

    /**
     * Creates a plot of {@link TrajectoryFeature}s which have been extracted
     * along a transect.
     * 
     * All {@link TrajectoryFeature}s must have been extracted along the same
     * {@link LineString} for this graph to be correctly displayed.
     * 
     * @param pointCollectionFeatures2Labels
     *            A {@link List} of {@link TrajectoryFeature}s to plot
     * @param transectDomain
     *            The transect domain along which *all* features must have been
     *            extracted.
     * @param hasVerticalAxis
     * @param copyrightStatement
     *            A copyright notice to display under the graph
     * @return The plot
     */
    public static JFreeChart createTransectPlot(
            Map<PointCollectionFeature, String> pointCollectionFeatures2Labels,
            LineString transectDomain, boolean hasVerticalAxis, String copyrightStatement) {
        JFreeChart chart;
        XYPlot plot;

        XYSeriesCollection xySeriesColl = new XYSeriesCollection();

        StringBuilder title = new StringBuilder("Trajectory plot of ");
        StringBuilder yLabel = new StringBuilder();
        int size = 0;
        boolean multiplePlots = false;
        if (pointCollectionFeatures2Labels.size() > 1) {
            multiplePlots = true;
        }
        VerticalPosition verticalPosition = null;
        DateTime time = null;
        for (Entry<PointCollectionFeature, String> entry : pointCollectionFeatures2Labels
                .entrySet()) {
            PointCollectionFeature feature = entry.getKey();
            if (feature.getVariableIds().size() > 1) {
                multiplePlots = true;
            }
            Array1D<HorizontalPosition> hPoints = feature.getDomain().getDomainObjects();
            verticalPosition = feature.getDomain().getVerticalPosition();
            time = feature.getDomain().getTime();
            for (String paramId : feature.getVariableIds()) {
                XYSeries series = new XYSeries(feature.getName() + ":" + paramId, true);
                double distAlongPath = 0.0;
                Array1D<Number> values = feature.getValues(paramId);
                size = (int) values.size();
                for (int i = 0; i < size; i++) {
                    if (i != 0) {
                        HorizontalPosition lastPoint = hPoints.get(i - 1);
                        HorizontalPosition currentPoint = hPoints.get(i);
                        distAlongPath += GISUtils.getHaversineDistance(lastPoint.getY(),
                                lastPoint.getX(), currentPoint.getY(), currentPoint.getX());
                    }
                    series.add(distAlongPath, values.get(i));
                }

                xySeriesColl.addSeries(series);
                yLabel.append(entry.getValue() + " (" + getParameterUnits(feature, paramId) + ")");
                yLabel.append("; ");
                title.append(entry.getValue());
                title.append(", ");
            }
        }
        if (yLabel.length() > 1) {
            yLabel.deleteCharAt(yLabel.length() - 1);
            yLabel.deleteCharAt(yLabel.length() - 1);
        }
        if (title.length() > 1) {
            title.deleteCharAt(title.length() - 1);
            title.deleteCharAt(title.length() - 1);
        }
        if (verticalPosition != null) {
            title.append(" at " + verticalPosition.getZ()
                    + verticalPosition.getCoordinateReferenceSystem().getUnits());
            if (verticalPosition.getCoordinateReferenceSystem().isPositiveUpwards()) {
                title.append(" elevation");
            } else {
                title.append(" depth");
            }
        }
        if (time != null) {
            title.append("\n" + TimeUtils.formatUtcHumanReadableDateTime(time));
        }

        /*
         * If we have a layer with more than one elevation value, we create a
         * transect chart using standard XYItem Renderer to keep the plot
         * renderer consistent with that of vertical section plot
         */
        if (hasVerticalAxis) {
            final XYItemRenderer renderer1 = new StandardXYItemRenderer();
            final NumberAxis rangeAxis1 = new NumberAxis(yLabel.toString());
            plot = new XYPlot(xySeriesColl, new NumberAxis(), rangeAxis1, renderer1);
            plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
            plot.setBackgroundPaint(Color.lightGray);
            plot.setDomainGridlinesVisible(false);
            plot.setRangeGridlinePaint(Color.white);
            for (int i = 0; i < xySeriesColl.getSeriesCount(); i++) {
                plot.getRenderer().setSeriesShape(i, new Ellipse2D.Double(-1.0, -1.0, 2.0, 2.0));
            }
            plot.setOrientation(PlotOrientation.VERTICAL);
            chart = new JFreeChart(null, null, plot, multiplePlots);
        } else {
            /*
             * If we have a layer which only has one elevation value, we simply
             * create XY Line chart
             */
            chart = ChartFactory.createXYLineChart(title.toString(), "Distance along transect (km)",
                    yLabel.toString(), xySeriesColl, PlotOrientation.VERTICAL, multiplePlots, false,
                    false);
            plot = chart.getXYPlot();
            for (int i = 0; i < xySeriesColl.getSeriesCount(); i++) {
                plot.getRenderer().setSeriesShape(i, new Ellipse2D.Double(-1.0, -1.0, 2.0, 2.0));
            }
        }

        if (copyrightStatement != null) {
            final TextTitle textTitle = new TextTitle(copyrightStatement);
            textTitle.setFont(new Font("SansSerif", Font.PLAIN, 10));
            textTitle.setPosition(RectangleEdge.BOTTOM);
            textTitle.setHorizontalAlignment(HorizontalAlignment.RIGHT);
            chart.addSubtitle(textTitle);
        }
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();

        rangeAxis.setAutoRangeIncludesZero(false);
        plot.setNoDataMessage("There is no data for what you have chosen.");

        /* Iterate through control points to show segments of transect */
        Double prevCtrlPointDistance = null;
        for (int i = 0; i < transectDomain.getControlPoints().size(); i++) {
            double ctrlPointDistance = transectDomain.getControlPointDistanceKm(i);
            if (prevCtrlPointDistance != null) {
                /*
                 * Determine start end end value for marker based on index of
                 * ctrl point
                 */
                IntervalMarker target = new IntervalMarker(prevCtrlPointDistance,
                        ctrlPointDistance);
                target.setLabel("["
                        + printTwoDecimals(transectDomain.getControlPoints().get(i - 1).getY())
                        + ","
                        + printTwoDecimals(transectDomain.getControlPoints().get(i - 1).getX())
                        + "]");
                target.setLabelFont(new Font("SansSerif", Font.ITALIC, 11));
                /*
                 * Alter colour of segment and position of label based on
                 * odd/even index
                 */
                if (i % 2 == 0) {
                    target.setPaint(new Color(222, 222, 255, 128));
                    target.setLabelAnchor(RectangleAnchor.TOP_LEFT);
                    target.setLabelTextAnchor(TextAnchor.TOP_LEFT);
                } else {
                    target.setPaint(new Color(233, 225, 146, 128));
                    target.setLabelAnchor(RectangleAnchor.BOTTOM_LEFT);
                    target.setLabelTextAnchor(TextAnchor.BOTTOM_LEFT);
                }
                /* Add marker to plot */
                plot.addDomainMarker(target);
            }
            prevCtrlPointDistance = transectDomain.getControlPointDistanceKm(i);
        }

        return chart;
    }

    /**
     * Plot a vertical section chart
     * 
     * @param features
     *            A {@link List} of evenly-spaced {@link ProfileFeature}s making
     *            up this vertical section. All features <i>must</i> have been
     *            extracted onto the same {@link VerticalAxis}. They must each
     *            only contain a single parameter.
     * @param horizPath
     *            The {@link LineString} along which the {@link ProfileFeature}s
     *            have been extracted
     * @param colourScheme
     *            The {@link ColourScheme} to use for the plot
     * @param zValue
     *            The elevation at which a matching transect is plotted (will be
     *            marked on the chart) - can be <code>null</code>
     * @param zExtent
     *            The range of elevations to include on the vertical section
     *            chart. If this is <code>null</code> the entire available range
     *            will be used
     * @return The resulting chart
     */
    public static JFreeChart createVerticalSectionChart(List<ProfileFeature> features,
            LineString horizPath, ColourScheme colourScheme, Double zValue,
            Extent<Double> zExtent) {
        if (features == null || features.size() == 0) {
            throw new IllegalArgumentException(
                    "You need at least one profile to plot a vertical section.");
        }

        VerticalSectionDataset dataset = new VerticalSectionDataset(features, horizPath);

        NumberAxis xAxis = new NumberAxis("Distance along path (km)");
        xAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        PaintScale scale = createPaintScale(colourScheme);

        NumberAxis colourScaleBar = new NumberAxis(dataset.units);
        Range colorBarRange = new Range(colourScheme.getScaleMin(), colourScheme.getScaleMax());
        colourScaleBar.setRange(colorBarRange);

        PaintScaleLegend paintScaleLegend = new PaintScaleLegend(scale, colourScaleBar);
        paintScaleLegend.setPosition(RectangleEdge.BOTTOM);

        XYBlockRenderer renderer = new XYBlockRenderer();
        double elevationResolution = dataset.getElevationResolution();
        renderer.setBlockHeight(elevationResolution);
        renderer.setBlockWidth(dataset.getXResolution());
        renderer.setPaintScale(scale);

        NumberAxis zAxis = getZAxis(features.get(0).getDomain().getVerticalCrs());
        if (zExtent != null) {
            zAxis.setRange(new Range(zExtent.getLow(), zExtent.getHigh()));
        }

        XYPlot plot = new XYPlot(dataset, xAxis, zAxis, renderer);
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinePaint(Color.white);

        /* Iterate through control points to show segments of transect */
        Double prevCtrlPointDistance = null;
        for (int i = 0; i < horizPath.getControlPoints().size(); i++) {
            double ctrlPointDistance = horizPath.getControlPointDistanceKm(i);
            if (prevCtrlPointDistance != null) {
                /*
                 * Determine start end end value for marker based on index of
                 * ctrl point
                 */
                IntervalMarker target = new IntervalMarker(prevCtrlPointDistance,
                        ctrlPointDistance);
                target.setPaint(TRANSPARENT);
                /* Add marker to plot */
                plot.addDomainMarker(target);
                /* Add line marker to vertical section plot */
                if (zValue != null) {
                    final Marker verticalLevel = new ValueMarker(Math.abs(zValue));
                    verticalLevel.setPaint(Color.lightGray);
                    verticalLevel.setLabel("at " + zValue + "  level ");
                    verticalLevel.setLabelAnchor(RectangleAnchor.BOTTOM_RIGHT);
                    verticalLevel.setLabelTextAnchor(TextAnchor.TOP_RIGHT);
                    plot.addRangeMarker(verticalLevel);
                }

            }
            prevCtrlPointDistance = horizPath.getControlPointDistanceKm(i);
        }

        JFreeChart chart = new JFreeChart("Along path", plot);
        chart.removeLegend();
        chart.addSubtitle(paintScaleLegend);
        chart.setBackgroundPaint(Color.white);

        return chart;
    }

    public static JFreeChart addVerticalSectionChart(JFreeChart transectChart,
            JFreeChart verticalSectionChart) {
        /*
         * Create the combined chart with both the transect and the vertical
         * section
         */
        CombinedDomainXYPlot plot = new CombinedDomainXYPlot(
                new NumberAxis("Distance along path (km)"));
        plot.setGap(20.0);
        plot.add(transectChart.getXYPlot(), 1);
        plot.add(verticalSectionChart.getXYPlot(), 1);
        plot.setOrientation(PlotOrientation.VERTICAL);
        String title = transectChart.getTitle().getText();
        String copyright = null;
        for (int i = 0; i < transectChart.getSubtitleCount(); i++) {
            Title subtitle = transectChart.getSubtitle(i);
            if (subtitle instanceof TextTitle) {
                copyright = ((TextTitle) transectChart.getSubtitle(0)).getText();
                break;
            }
        }

        JFreeChart combinedChart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, plot,
                false);

        /* Set left margin to 10 to avoid number wrap at color bar */
        RectangleInsets r = new RectangleInsets(0, 10, 0, 0);
        transectChart.setPadding(r);

        /*
         * This is not ideal. We have already added the copyright label to the
         * first chart, but then we extract the actual plot, so we need to add
         * it again here
         */
        if (copyright != null) {
            final TextTitle textTitle = new TextTitle(copyright);
            textTitle.setFont(new Font("SansSerif", Font.PLAIN, 10));
            textTitle.setPosition(RectangleEdge.BOTTOM);
            textTitle.setHorizontalAlignment(HorizontalAlignment.RIGHT);
            combinedChart.addSubtitle(textTitle);
        }

        /* Use the legend from the vertical section chart */
        combinedChart.addSubtitle(verticalSectionChart.getSubtitle(0));

        return combinedChart;
    }

    private static String getParameterUnits(Feature<?> feature, String memberName) {
        // Parameter parameter = feature.getParameter(memberName);
        // return parameter.getTitle() + " (" + parameter.getUnits() + ")";
        return feature.getParameter(memberName).getUnits();
    }

    /**
     * Prints a double-precision number to 2 decimal places
     * 
     * @param d
     *            the double
     * @return rounded value to 2 places, as a String
     */
    private static String printTwoDecimals(double d) {
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        /*
         * We need to set the Locale properly, otherwise the DecimalFormat
         * doesn't work in locales that use commas instead of points. Thanks to
         * Justino Martinez for this fix!
         */
        DecimalFormatSymbols decSym = DecimalFormatSymbols.getInstance(US_LOCALE);
        twoDForm.setDecimalFormatSymbols(decSym);
        return twoDForm.format(d);
    }

    /**
     * An {@link XYZDataset} that is created by interpolating a set of values
     * from a discrete set of elevations.
     */
    private static class VerticalSectionDataset extends AbstractXYZDataset {
        private static final long serialVersionUID = 1L;
        private final int horizPathLength;
        private final List<ProfileFeature> features;
        private final String paramId;
        private final List<Double> elevationValues;
        private final double minElValue;
        private final double elevationResolution;
        private final int numElevations;
        private final String units;
        private final List<Double> distanceValues;
        private double xResolution;

        public VerticalSectionDataset(List<ProfileFeature> features, LineString domain) {
            this.features = features;
            this.horizPathLength = features.size();

            double minElValue = 0.0;
            double maxElValue = 1.0;
            VerticalAxis vAxis = features.get(0).getDomain();

            if (vAxis.size() > 0) {
                minElValue = vAxis.getCoordinateValue(0);
                maxElValue = vAxis.getCoordinateValue(vAxis.size() - 1);
            }

            /* Sometimes values on the axes are reversed */
            if (minElValue > maxElValue) {
                double temp = minElValue;
                minElValue = maxElValue;
                maxElValue = temp;
            }
            this.minElValue = minElValue;

            double minGap = Double.MAX_VALUE;
            for (int i = 1; i < vAxis.size(); i++) {
                minGap = Math.min(minGap,
                        Math.abs(vAxis.getCoordinateValue(i) - vAxis.getCoordinateValue(i - 1)));
            }
            this.numElevations = (int) ((maxElValue - minElValue) / minGap);
            this.elevationResolution = (maxElValue - minElValue) / numElevations;

            double maxGap = -Double.MAX_VALUE;
            this.distanceValues = new ArrayList<>();
            this.distanceValues.add(0.0);
            HorizontalPosition lastPoint = features.get(0).getHorizontalPosition();
            for (int i = 1; i < features.size(); i++) {
                HorizontalPosition currentPoint = features.get(i).getHorizontalPosition();
                double dist = GISUtils.getHaversineDistance(lastPoint.getY(), lastPoint.getX(),
                        currentPoint.getY(), currentPoint.getX());
                lastPoint = currentPoint;
                this.distanceValues.add(this.distanceValues.get(i - 1) + dist);
                maxGap = Math.max(maxGap, dist);
            }
            int numXPoints = (int) (distanceValues.get(distanceValues.size() - 1) / maxGap);
            this.xResolution = (distanceValues.get(distanceValues.size() - 1)) / numXPoints;

            this.paramId = features.get(0).getVariableIds().iterator().next();
            this.units = features.get(0).getParameter(paramId).getUnits();
            this.elevationValues = vAxis.getCoordinateValues();
        }

        public double getElevationResolution() {
            return elevationResolution;
        }

        public double getXResolution() {
            return xResolution;
        }

        @Override
        public int getSeriesCount() {
            return 1;
        }

        @Override
        public String getSeriesKey(int series) {
            checkSeries(series);
            return "Vertical section";
        }

        @Override
        public int getItemCount(int series) {
            checkSeries(series);
            return horizPathLength * numElevations;
        }

        @Override
        public Double getX(int series, int item) {
            checkSeries(series);
            /*
             * The x coordinate is just the integer index of the point along the
             * horizontal path
             */
            return distanceValues.get(item % horizPathLength);
        }

        /**
         * Gets the value of elevation, assuming linear variation between min
         * and max.
         */
        @Override
        public Double getY(int series, int item) {
            checkSeries(series);
            int yIndex = item / horizPathLength;
            return minElValue + yIndex * elevationResolution;
        }

        /**
         * Gets the data value corresponding with the given item, interpolating
         * between the recorded data values using nearest-neighbour
         * interpolation
         */
        @Override
        public Float getZ(int series, int item) {
            checkSeries(series);
            int xIndex = item % horizPathLength;
            double elevation = getY(series, item);
            /*
             * What is the index of the nearest elevation in the list of
             * elevations for which we have data?
             */
            int nearestElevationIndex = -1;
            double minDiff = Double.MAX_VALUE;
            for (int i = 0; i < elevationValues.size(); i++) {
                double el = elevationValues.get(i);
                double diff = Math.abs(el - elevation);
                if (diff < minDiff) {
                    minDiff = diff;
                    nearestElevationIndex = i;
                }
            }

            Number number = features.get(xIndex).getValues(paramId).get(nearestElevationIndex);
            if (number != null) {
                return number.floatValue();
            } else {
                return null;
            }
        }

        /**
         * @throws IllegalArgumentException
         *             if the argument is not zero.
         */
        private static void checkSeries(int series) {
            if (series != 0) {
                throw new IllegalArgumentException("Series must be zero");
            }
        }
    }

    /**
     * Creates and returns a JFreeChart {@link PaintScale} that converts data
     * values to {@link Color}s.
     */
    public static PaintScale createPaintScale(final ColourScheme colourScheme) {
        return new PaintScale() {
            @Override
            public double getLowerBound() {
                return colourScheme.getScaleMin();
            }

            @Override
            public double getUpperBound() {
                return colourScheme.getScaleMax();
            }

            @Override
            public Color getPaint(double value) {
                return colourScheme.getColor(value);
            }
        };
    }
}
