package uk.ac.rdg.resc.edal.graphics.style;

import javax.naming.OperationNotSupportedException;

import uk.ac.rdg.resc.edal.graphics.style.sld.SLDRange;
import uk.ac.rdg.resc.edal.graphics.style.sld.SLDRange.Spacing;

public class SegmentDensityMap implements DensityMap {

    private int nLevels;
    private SLDRange range;
    private float minDensity;
    private float maxDensity;
    private Float belowMinDensity;
    private Float aboveMaxDensity;
    private float noDataDensity;

    public SegmentDensityMap(int nLevels, SLDRange range, float minDensity,
    		float maxDensity, Float belowMinDensity, Float aboveMaxDensity,
    		float noDataDensity) throws IllegalArgumentException {
    	this.nLevels = nLevels;
    	this.range = range;
    	if (minDensity < 0.0F || minDensity > 1.0F) {
    		throw new IllegalArgumentException("Minimum density invalid.");
    	} else {
    		this.minDensity = minDensity;
    	}
    	if (maxDensity < 0.0F || maxDensity > 1.0F) {
    		throw new IllegalArgumentException("Maximum density invalid.");
    	} else {
    		this.maxDensity = maxDensity;
    	}
    	if (belowMinDensity != null && (belowMinDensity < 0.0F || belowMinDensity > 1.0F)) {
    		throw new IllegalArgumentException("Below minimum density invalid.");
    	} else {
    		this.belowMinDensity = belowMinDensity;
    	}
    	if (aboveMaxDensity != null && (aboveMaxDensity < 0.0F || aboveMaxDensity > 1.0F)) {
    		throw new IllegalArgumentException("Above maximum density invalid.");
    	} else {
    		this.aboveMaxDensity = aboveMaxDensity;
    	}
    	if (noDataDensity < 0.0F || noDataDensity > 1.0F) {
    		throw new IllegalArgumentException("No data density invalid.");
    	} else {
    		this.noDataDensity = noDataDensity;
    	}
    }
    
	@Override
	public float getDensity(Number value) throws OperationNotSupportedException {
		// handle missing data
        if(value == null || Float.isNaN(value.floatValue())) {
            return noDataDensity;
        }
        
        // handle out of range values
        if (value.floatValue() < range.getMinimum()) {
        	return belowMinDensity == null ? minDensity : belowMinDensity;
        } else if (value.floatValue() > range.getMaximum()) {
        	return aboveMaxDensity == null ? maxDensity : aboveMaxDensity;
        }
        
        // handle values within range
        int level;
        if (range.getSpacing() == Spacing.LINEAR) {
        	float interval = (range.getMaximum() - range.getMinimum())/(float)nLevels;
        	level = (int) ((value.floatValue() - range.getMinimum())/interval);
        } else if (range.getSpacing() == Spacing.LOGARITHMIC) {
        	double logInterval = (Math.log10(range.getMaximum()) - Math.log10(range.getMinimum()))/(double)nLevels;
        	level = (int) ((Math.log10(value.floatValue()) - Math.log10(range.getMinimum()))/logInterval);
        } else {
        	throw new OperationNotSupportedException("Spacing of range not recognized.");
        }
        float density;
    	if (minDensity < maxDensity) {
    		float densityInterval = (maxDensity - minDensity)/(float)(nLevels - 1);
    		density = minDensity + densityInterval*(float)level;
        	
        	// correct density for possible rounding errors
        	if (density > maxDensity) {
        		density = maxDensity;
        	} else if (density < minDensity) {
        		density = minDensity;
        	}
    	} else {
    		float densityInterval = (minDensity - maxDensity)/(float)(nLevels - 1);
    		density =  minDensity - densityInterval*(float)level;
    		
        	// correct density for possible rounding errors
        	if (density < maxDensity) {
        		density = maxDensity;
        	} else if (density > minDensity) {
        		density = minDensity;
        	}
    	}
    	return density;
	}

	@Override
	public Float getMinValue() {
		return range.getMinimum();
	}

	@Override
	public Float getMaxValue() {
		return range.getMaximum();
	}

	public static void main(String[] args) throws Exception {
		SLDRange range = new SLDRange(0.0F, 2.5F, Spacing.LINEAR);
		SegmentDensityMap map = new SegmentDensityMap(5, range, 1F, 0F, null, null, 1F);
		for (float f = -0.5F; f < 3.5F; f += 1.0F) {
			System.out.println(f + ", " + Math.log10(f) + ", " + map.getDensity(f));
		}
	}
}
