package uk.ac.rdg.resc.edal.graphics.style.datamodel.impl;

import java.awt.Color;
import java.util.List;

/**
 * Takes a list of interpolation points with colours and data points. If a data
 * value is below the first point then the first colour is returned. If it is
 * above the last point then the last colour is returned. If it is in between
 * two points then the colour is interpolated. This provides the functionality
 * required by the Symbology Encoding interpolation function.
 * 
 * @author Charles Roberts
 */
public class InterpolateColourScheme extends ColourScheme {

	public static class InterpolationPoint {
		private Float data;
		private Color colour;
		
		public InterpolationPoint(Float data, Color colour) {
			this.data = data;
			this.colour = colour;
		}
		
		public Float getData() {
			return data;
		}
		public void setData(Float data) {
			this.data = data;
		}
		public Color getColour() {
			return colour;
		}
		public void setColour(Color colour) {
			this.colour = colour;
		}
	}
	
	private List<InterpolationPoint> points;
	
	private Color noDataColour = new Color(0, 0, 0, 0);
	
	public InterpolateColourScheme(List<InterpolationPoint> points, Color noDataColour) {
		super();
		if (noDataColour != null) {
			this.noDataColour = noDataColour;
		}
		this.points = points;
		initializeColours();
	}

	@Override
	public Color getColor(Number value) {
		if (value == null || Float.isNaN(value.floatValue())) {
			return noDataColour;
		}
		if (points.size() < 2) {
			return points.get(0).getColour();
		}
		if (value.floatValue() < points.get(0).getData()) {
			return points.get(0).getColour();
		}
		for (int i = 1; i < points.size(); i++) {
			if (value.floatValue() < points.get(i).getData()) {
				// determine fraction to come from lower colour
				float dist = points.get(i).getData() - value.floatValue();
				if (dist < 0F) {
					dist = 0F;
				}
				float frac = dist/(points.get(i).getData() - points.get(i - 1).getData());
				return interpolate(points.get(i - 1).getColour(), points.get(i).getColour(), frac);
			}
		}
		return points.get(points.size() - 1).getColour();
	}

	@Override
	public Float getScaleMin() {
		return points.get(0).getData();
	}

	@Override
	public Float getScaleMax() {
		return points.get(points.size() - 1).getData();
	}
	
	private void initializeColours() {
		if(points == null || points.size() < 2) {
            throw new IllegalArgumentException("Interpolation points must not be null and must have at least one value");
        }
		/*
         * Check that thresholds are correctly ordered. 
         */
        Float value = -Float.MAX_VALUE;
        for(InterpolationPoint point : points) {
            if(point.getData() == null || point.getColour() == null) {
            	throw new IllegalArgumentException(
            			"Both data points and colours must not be null.");
            } else if(point.getData() < value) {
                throw new IllegalArgumentException(
                        "Data points must be in ascending order of value");
            }
            value = point.getData();
        }
	}

    /**
     * Linearly interpolates between two RGB colours. Taken from the class
     * ColourPalette in the uk.ac.rdg.resc.edal.graphics.style package.
     * 
     * @param c1
     *            the first colour
     * @param c2
     *            the second colour
     * @param fracFromC1
     *            the fraction of the final colour that will come from c1
     * @return the interpolated Color
     */
    private static Color interpolate(Color c1, Color c2, float fracFromC1) {
        float fracFromC2 = 1.0f - fracFromC1;
        return new Color(Math.round(fracFromC1 * c1.getRed() + fracFromC2 * c2.getRed()),
                Math.round(fracFromC1 * c1.getGreen() + fracFromC2 * c2.getGreen()),
                Math.round(fracFromC1 * c1.getBlue() + fracFromC2 * c2.getBlue()),
                Math.round(fracFromC1 * c1.getAlpha() + fracFromC2 * c2.getAlpha()));
    }

}
