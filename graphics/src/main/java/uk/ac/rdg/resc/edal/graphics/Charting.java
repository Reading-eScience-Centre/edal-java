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
import java.text.NumberFormat;
import java.util.Date;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.joda.time.Chronology;
import org.joda.time.DateTime;

import uk.ac.rdg.resc.edal.exceptions.MismatchedCrsException;
import uk.ac.rdg.resc.edal.feature.Feature;
import uk.ac.rdg.resc.edal.feature.PointSeriesFeature;
import uk.ac.rdg.resc.edal.feature.ProfileFeature;
import uk.ac.rdg.resc.edal.metadata.Parameter;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.VerticalCrs;

/**
 * Code to produce various types of chart.
 * 
 * @author Guy Griffiths
 * @author Jon Blower
 * @author Kevin X. Yang
 */
final public class Charting {
//    private static final Color TRANSPARENT = new Color(0, 0, 0, 0);
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance();
    static {
        NUMBER_FORMAT.setMinimumFractionDigits(0);
        NUMBER_FORMAT.setMaximumFractionDigits(4);
    }

    public static JFreeChart createVerticalProfilePlot(List<ProfileFeature> features,
            HorizontalPosition hPos) throws MismatchedCrsException {
        XYSeriesCollection xySeriesColl = new XYSeriesCollection();

        StringBuilder varList = new StringBuilder();
        String xAxisLabel = "";
        VerticalCrs vCrs = null;
        boolean invertYAxis = false;
        for (ProfileFeature feature : features) {
            if (vCrs == null) {
                vCrs = feature.getDomain().getVerticalCrs();
            } else {
                if (vCrs.equals(feature.getDomain().getVerticalCrs())) {
                    throw new MismatchedCrsException(
                            "All vertical CRSs must match to plot multiple profile plots");
                }
            }
            for (String varId : feature.getParameterIds()) {
                varList.append(varId);
                varList.append(", ");
                List<Double> elevationValues = feature.getDomain().getCoordinateValues();

                /*
                 * This is the label used for the legend.
                 */
                String location = feature.getHorizontalPosition().toString();
                XYSeries series = new XYSeries(location, true);
                series.setDescription(feature.getParameter(varId).getDescription());
                for (int i = 0; i < elevationValues.size(); i++) {
                    Number val = feature.getValues(varId).get(i);
                    if (val == null || Double.isNaN(val.doubleValue())) {
                        /*
                         * Don't add NaNs to the series
                         */
                        continue;
                    }
                    series.add(elevationValues.get(i), val);
                }

                xAxisLabel = getAxisLabel(feature, varId);

                xySeriesColl.addSeries(series);
            }
        }

        NumberAxis elevationAxis = getZAxis(vCrs);
        elevationAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        if (invertYAxis) {
            elevationAxis.setInverted(true);
        }
        elevationAxis.setAutoRangeIncludesZero(false);
        elevationAxis.setNumberFormatOverride(NUMBER_FORMAT);

        NumberAxis valueAxis = new NumberAxis(xAxisLabel);
        valueAxis.setAutoRangeIncludesZero(false);
        valueAxis.setNumberFormatOverride(NUMBER_FORMAT);

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        for (int i = 0; i < features.size(); i++) {
            renderer.setSeriesShape(i, new Ellipse2D.Double(-1.0, -1.0, 2.0, 2.0));
            renderer.setSeriesShapesVisible(i, true);
        }

        XYPlot plot = new XYPlot(xySeriesColl, elevationAxis, valueAxis, renderer);
        plot.setNoDataMessage("There is no data for your choice");
        plot.setNoDataMessageFont(new Font("sansserif", Font.BOLD, 20));
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinePaint(Color.white);
        plot.setOrientation(PlotOrientation.HORIZONTAL);

        String title;
        if (varList.length() > 0) {
            varList.delete(varList.length() - 2, varList.length() - 1);
            title = "Vertical profile of variables: " + varList.toString() + " at "
                    + hPos.toString();
        } else {
            title = "No data to plot at " + hPos.toString();
        }

        /*
         * Use default font and create a legend if there are multiple lines
         */
        return new JFreeChart(title, null, plot, xySeriesColl.getSeriesCount() > 1);
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
        if (vCrs.isPositiveUpwards()) {
            zAxisLabel = "Height";
            invertYAxis = false;
        } else if (vCrs.isPressure()) {
            zAxisLabel = "Pressure";
            invertYAxis = true;
        } else {
            zAxisLabel = "Depth";
            invertYAxis = true;
        }

        NumberAxis zAxis = new NumberAxis(zAxisLabel + " (" + vCrs.getUnits() + ")");
        zAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        zAxis.setInverted(invertYAxis);

        return zAxis;
    }

    public static JFreeChart createTimeSeriesPlot(List<PointSeriesFeature> features,
            HorizontalPosition hPos) throws MismatchedCrsException {
        TimeSeriesCollection timeSeriesColl = new TimeSeriesCollection();

        StringBuilder varList = new StringBuilder();
        String yAxisLabel = "";
        Chronology chronology = null;
        for (PointSeriesFeature feature : features) {
            if (chronology == null) {
                chronology = feature.getDomain().getChronology();
            } else {
                if (chronology.equals(feature.getDomain().getChronology())) {
                    throw new MismatchedCrsException(
                            "All chronologies must match to plot multiple time series plots");
                }
            }
            for (String varId : feature.getParameterIds()) {
                varList.append(varId);
                varList.append(", ");
                List<DateTime> timeValues = feature.getDomain().getCoordinateValues();

                /*
                 * This is the label used for the legend.
                 */
                String location = feature.getHorizontalPosition().toString();
                TimeSeries series = new TimeSeries(location);
                series.setDescription(feature.getParameter(varId).getDescription());
                for (int i = 0; i < timeValues.size(); i++) {
                    Number val = feature.getValues(varId).get(i);
                    if (val == null || Double.isNaN(val.doubleValue())) {
                        /*
                         * Don't add NaNs to the series
                         */
                        continue;
                    }
                    series.addOrUpdate(new Millisecond(new Date(timeValues.get(i).getMillis())),
                            val);
                }

                yAxisLabel = getAxisLabel(feature, varId);

                timeSeriesColl.addSeries(series);
            }
        }

        NumberAxis timeAxis = new NumberAxis("Time");
        timeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        timeAxis.setAutoRangeIncludesZero(false);

        NumberAxis valueAxis = new NumberAxis(yAxisLabel);
        valueAxis.setAutoRangeIncludesZero(false);
        valueAxis.setNumberFormatOverride(NUMBER_FORMAT);

        StringBuilder title = new StringBuilder();
        if (varList.length() > 0) {
            varList.delete(varList.length() - 2, varList.length() - 1);
            title.append("Time series of variables: ");
            title.append(varList.toString());
        } else {
            title.append("No data to plot");
        }
        title.append(" at ");
        title.append(hPos.toString());

        JFreeChart chart = ChartFactory.createTimeSeriesChart(title.toString(), "Date / time",
                yAxisLabel, timeSeriesColl, true, false, false);

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        for (int i = 0; i < timeSeriesColl.getSeriesCount(); i++) {
            renderer.setSeriesShape(i, new Ellipse2D.Double(-1.0, -1.0, 2.0, 2.0));
            renderer.setSeriesShapesVisible(i, true);
        }
        XYPlot plot = chart.getXYPlot();

        plot.setRenderer(renderer);
        plot.setNoDataMessage("There is no data for your choice");
        plot.setNoDataMessageFont(new Font("sansserif", Font.BOLD, 20));
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinePaint(Color.white);
        plot.setOrientation(PlotOrientation.VERTICAL);

        /*
         * Use default font and create a legend if there are multiple lines
         */
        return new JFreeChart(title.toString(), null, plot, timeSeriesColl.getSeriesCount() > 1);
    }

    private static String getAxisLabel(Feature<?> feature, String memberName) {
        Parameter parameter = feature.getParameter(memberName);
        return parameter.getTitle() + " (" + parameter.getUnits() + ")";
    }
}
