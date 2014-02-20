package uk.ac.rdg.resc.edal.graphics.style;

import java.util.ArrayList;
import java.util.List;

import javax.naming.OperationNotSupportedException;

import uk.ac.rdg.resc.edal.graphics.style.sld.SLDException;

public class InterpolateDensityMap implements DensityMap {

	private float minimum;
	private float maximum;
	private float minDensity;
	private float maxDensity;
	private float noDataValue;
	
	public InterpolateDensityMap(List<InterpolationPoint<Float>> points,
			float noDataValue) throws SLDException {
		if (points.size() != 2) {
			throw new SLDException("There must be exactly two interpolation" +
					"points in Interpolate for opacities and patterns.");
		} else if (points.get(0).getData() < points.get(1).getData()) {
			this.minimum = points.get(0).getData();
			this.minDensity = points.get(0).getValue();
			this.maximum = points.get(1).getData();
			this.maxDensity = points.get(1).getValue();
		} else {
			this.minimum = points.get(1).getData();
			this.minDensity = points.get(1).getValue();
			this.maximum = points.get(0).getData();
			this.maxDensity = points.get(0).getValue();
		}
		this.noDataValue = noDataValue;
	}
	
	@Override
	public float getDensity(Number value) throws OperationNotSupportedException {
		if (value == null || Float.isNaN(value.floatValue())) {
			return noDataValue;
		} else if (value.floatValue() < minimum) {
			return minDensity;
		} else if (value.floatValue() > maximum) {
			return maxDensity;
		} else {
			float frac = (value.floatValue() - minimum)/(maximum - minimum);
			if (minDensity < maxDensity) {
				return minDensity + frac*(maxDensity - minDensity);
			} else {
				return minDensity - frac*(minDensity - maxDensity);
			}
		}
		
	}

	@Override
	public Float getMinValue() {
		return minimum;
	}

	@Override
	public Float getMaxValue() {
		return maximum;
	}

	public static void main(String[] args) throws Exception {
		List<InterpolationPoint<Float>> points = new ArrayList<>();
		points.add(new InterpolationPoint<Float>(0f, 0.75f));
		points.add(new InterpolationPoint<Float>(100f, 0.25f));
		InterpolateDensityMap interpolate = new InterpolateDensityMap(points, 1f);
		for (float f = -5f; f < 110f; f += 10f) {
			System.out.println(f + ", " + interpolate.getDensity(f));
		}
		System.out.print(interpolate.getDensity(null));
	}
}
