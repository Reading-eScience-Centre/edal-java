package uk.ac.rdg.resc;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.data.xy.AbstractXYZDataset;
import org.jfree.data.xy.XYZDataset;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;

import uk.ac.rdg.resc.edal.Extent;
import uk.ac.rdg.resc.edal.cdm.feature.NcGridSeriesFeatureCollection;
import uk.ac.rdg.resc.edal.coverage.domain.GridSeriesDomain;
import uk.ac.rdg.resc.edal.coverage.domain.impl.HorizontalDomain;
import uk.ac.rdg.resc.edal.exceptions.InvalidCrsException;
import uk.ac.rdg.resc.edal.exceptions.InvalidLineStringException;
import uk.ac.rdg.resc.edal.feature.GridSeriesFeature;
import uk.ac.rdg.resc.edal.geometry.impl.LineString;
import uk.ac.rdg.resc.edal.graphics.Charting;
import uk.ac.rdg.resc.edal.graphics.ColorPalette;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.TimePosition;
import uk.ac.rdg.resc.edal.util.CollectionUtils;
import uk.ac.rdg.resc.edal.util.Extents;

public class HovmollerTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException, InvalidCrsException, InvalidLineStringException{
		// TODO Auto-generated method stub
		final Color TRANSPARENT = new Color(0, 0, 0, 0);
		NcGridSeriesFeatureCollection featureCollection = new NcGridSeriesFeatureCollection(
                "testcollection", "Test Collection", "/home/guy/PythonCourse/polcoms.nc");
		GridSeriesFeature feature = featureCollection.getFeatureById("testcollection2");
		GridSeriesDomain domain = feature.getCoverage().getDomain();
		
		String memberName = "sst";
		Extent<TimePosition> time_coverage =domain.getTimeAxis().getCoordinateExtent();
		TimePosition t_begin =time_coverage.getLow();
		TimePosition t_end =time_coverage.getHigh();
		
		List<List<Float>> values =new ArrayList<List<Float>> ();
		float min_v =15.0F;
		float max_v =1.0F;
		
		LineString horizPath = new LineString("-6.425 51.9,-5.0 52.8,-4.9 54.0", DefaultGeographicCRS.WGS84, "1.3.0");
		
		HorizontalDomain optimalTransectDomain = Charting.getOptimalTransectDomain(feature.getCoverage()
                .getDomain().getHorizontalGrid(), horizPath);
		

		List<HorizontalPosition> controlPoints = optimalTransectDomain.getDomainObjects();
		
        for (HorizontalPosition pos : controlPoints) {
            /*
             * This cast is OK, because we have already thrown an exception if
             * this doesn't return a number
             */
            @SuppressWarnings("unchecked")
            List<Float> temp = (List<Float>) feature
            		.extractPointSeriesFeature(pos, null, Extents.newExtent(t_begin, t_end), CollectionUtils.setOf(memberName))
                    .getCoverage().getValues(memberName);
            values.add(temp);
            float temp_min =Collections.min(temp);
			float temp_max=Collections.max(temp);
			if(temp_min <min_v)
				min_v =temp_min;
			if(temp_max >max_v)
				max_v =temp_max;
        }
        

        int x_path =values.size();

	
		DateAxis t_axis =new DateAxis();
		SimpleDateFormat sdf =new SimpleDateFormat("yyyy-MM-dd HH:mm");
		t_axis.setRange(new Date(t_begin.getValue()), new Date(t_end.getValue()));
		t_axis.setDateFormatOverride(sdf);
		
		long t_step =60*60*1000;
		XYZDataset dataset = new HovmollerDataset(values, values.size(), 73, t_begin.getValue(), t_step);
		
		NumberAxis lat_axis = new NumberAxis();
		lat_axis.setTickMarksVisible(false);
		lat_axis.setTickLabelsVisible(false);

		lat_axis.setRange(0, x_path);
		t_axis.setInverted(true);

		
		ColorPalette.loadPalettes(new File("/home/guy/Workspace/edal-java/ncwms/src/main/webapp/WEB-INF/conf/palettes/"));
        ColorPalette palette =ColorPalette.get("rainbow");
        Extent<Float> colourScaleRange = Extents.newExtent(min_v, max_v);
        int numColourBands =100;
        boolean logarithmic=false;
        PaintScale scale = Charting.createPaintScale(palette, colourScaleRange, numColourBands, logarithmic);       
        org.jfree.data.Range colorBarRange = new org.jfree.data.Range(colourScaleRange.getLow(),
                colourScaleRange.getHigh());        
        NumberAxis scale_axis = new NumberAxis();
        scale_axis.setRange(colorBarRange);
        PaintScaleLegend paintScaleLegend = new PaintScaleLegend(scale, scale_axis);
        paintScaleLegend.setPosition(RectangleEdge.BOTTOM);
        
        XYBlockRenderer xyblockrenderer = new XYBlockRenderer();

        double y_range =xyblockrenderer.findRangeBounds(dataset).getLength();

        //73 is a magic number, as i know the dataset about every hour of 3-day
        xyblockrenderer.setBlockHeight(y_range / 73);
        
        double x_range =(max_v -min_v) /x_path;
       
        xyblockrenderer.setBlockWidth( x_range );
        xyblockrenderer.setPaintScale(scale);
        
        XYPlot plot = new XYPlot(dataset, lat_axis, t_axis, xyblockrenderer);
        //plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinesVisible(false);
        
        Double prevCtrlPointDistance = null;
        for (int i = 0; i < horizPath.getControlPoints().size(); i++) {
            double ctrlPointDistance = horizPath.getFractionalControlPointDistance(i);
            if (prevCtrlPointDistance != null) {
                // determine start end end value for marker based on index of
                // ctrl point
                IntervalMarker target = new IntervalMarker(x_path * prevCtrlPointDistance,
                        x_path * ctrlPointDistance);
                target.setPaint(TRANSPARENT);
                String first_half ="["
                        + printTwoDecimals(horizPath.getControlPoints().get(i - 1).getY())
                        + ","
                        + printTwoDecimals(horizPath.getControlPoints().get(i - 1).getX())
                        + "]";
                String second_half ="["
                        + printTwoDecimals(horizPath.getControlPoints().get(i).getY())
                        + ","
                        + printTwoDecimals(horizPath.getControlPoints().get(i).getX())
                        + "]";
                String label = first_half+ " to "+second_half;
                target.setLabel(label);
                target.setLabelFont(new Font("SansSerif", Font.ITALIC, 11));
                if (i % 2 == 0) {
                    //target.setPaint(new Color(222, 222, 255, 128));
                    target.setLabelAnchor(RectangleAnchor.TOP_LEFT);
                    target.setLabelTextAnchor(TextAnchor.TOP_LEFT);
                } else {
                    //target.setPaint(new Color(233, 225, 146, 128));
                    target.setLabelAnchor(RectangleAnchor.BOTTOM_LEFT);
                    target.setLabelTextAnchor(TextAnchor.BOTTOM_LEFT);
                }
                // add marker to plot
                plot.addDomainMarker(target);
            }
            prevCtrlPointDistance = horizPath.getFractionalControlPointDistance(i);
        }
        

        JFreeChart jfreechart = new JFreeChart("HovMolloerDemo", plot);
        jfreechart.removeLegend();
        jfreechart.addSubtitle(paintScaleLegend);
        jfreechart.setBackgroundPaint(Color.white);
        ChartUtilities.writeChartAsPNG(new FileOutputStream(new File("/home/guy/000test_final.png")), jfreechart, 500, 400);
        System.out.println("end.");
	}
	
	private static class HovmollerDataset extends AbstractXYZDataset {
        private static final long serialVersionUID = 1L;
        private final int x_path;
        private final List<List<Float>> data;
        private final int y_path;  //time axis 
        private final long t_begin;
        private final long y_step;

        public HovmollerDataset(List<List<Float>> data,
                int x_path, int y_path, long t_begin, long y_step) {
            /*
             * TODO Test that this is the right way round
             */
            this.data = data;
            this.x_path =x_path;
            this.y_path =y_path;
            this.t_begin =t_begin;
            this.y_step =y_step;
        }

        @Override
        public int getSeriesCount() {
            return 1;
        }

        @Override
        public String getSeriesKey(int series) {
            checkSeries(series);
            return "Time section";
        }

        @Override
        public int getItemCount(int series) {
            checkSeries(series);
            return this.x_path * this.y_path;
        }

        @Override
        public Number getX(int series, int item) {
            checkSeries(series);
            // The x coordinate is just the integer index of the point along
            // the horizontal path
            int k =item % this.x_path;
            return k;
        }

        /**
         * Gets the value of elevation, assuming linear variation between min
         * and max.
         */
        @Override
        public Number getY(int series, int item) {
            checkSeries(series);
            int yIndex = item / this.x_path;
            return yIndex *y_step +t_begin ;
        }

        /**
         * Gets the data value corresponding with the given item, interpolating
         * between the recorded data values using nearest-neighbour
         * interpolation
         */
        @Override
        public Float getZ(int series, int item) {
            checkSeries(series);
            int xIndex = item % x_path;
            int yIndex =item / this.x_path;

            return data.get(xIndex).get(yIndex);
        }

        /**
         * @throws IllegalArgumentException
         *             if the argument is not zero.
         */
        private static void checkSeries(int series) {
            if (series != 0)
                throw new IllegalArgumentException("Series must be zero");
        }
    }
	
	private static String printTwoDecimals(double d) {
		final Locale US_LOCALE = new Locale("us", "US");
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        // We need to set the Locale properly, otherwise the DecimalFormat
        // doesn't
        // work in locales that use commas instead of points.
        // Thanks to Justino Martinez for this fix!
        DecimalFormatSymbols decSym = DecimalFormatSymbols.getInstance(US_LOCALE);
        twoDForm.setDecimalFormatSymbols(decSym);
        return twoDForm.format(d);
    }
}
