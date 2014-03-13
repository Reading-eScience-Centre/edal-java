package uk.ac.rdg.resc.edal.graphics.style;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.naming.OperationNotSupportedException;

public class ThresholdDensityMap implements DensityMap {

    /*
     * These hold lists of colours and the value which marks the lower boundary
     * of their threshold, IN REVERSE ORDER OF VALUES. It is supplied in
     * ascending order (because this is the logical way to think of things), but
     * is reversed in the setter.
     */
    private List<Float> thresholds;
    private List<Float> densities;
    
    private float noDataDensity = 0F;
    
    public ThresholdDensityMap(List<Float> thresholds, List<Float> densities, float noDataDensity) {
        super();
        this.noDataDensity = noDataDensity;
        this.thresholds = thresholds;
        this.densities = densities;
        initialiseDensities();
    }

	private void initialiseDensities() {
        if(thresholds == null || thresholds.size() < 1) {
            throw new IllegalArgumentException("Threshold values must not be null and must have at least one value");
        }
        /*
         * Check that there are the correct number of colours.
         */
        if (densities == null || densities.size() != (thresholds.size() + 1)) {
        	throw new IllegalArgumentException("Densities must not be null and must be in the correct number to match the thresholds.");
        }
        /*
         * Check that thresholds are correctly ordered.  Then reverse lists. 
         */
        Float value = -Float.MAX_VALUE;
        for(Float band : thresholds) {
            if(band < value) {
                throw new IllegalArgumentException(
                        "Threshold bands must be in ascending order of value");
            }
            value = band;
        }
        Collections.reverse(thresholds);
        
        Collections.reverse(densities);
	}

	@Override
	public float getDensity(Number value) throws OperationNotSupportedException {
        if(value == null || Float.isNaN(value.floatValue())) {
            return noDataDensity;
        }
        /*
         * Remember: THIS LIST IS IN REVERSE ORDER.
         */
        Iterator<Float> densityIterator = densities.iterator();
		float density = densityIterator.next();
        for(Float band : thresholds) {
            if(value.floatValue() > band) {
                return density;
            }
            density = densityIterator.next();
        }
        return density;
	}

	@Override
	public Float getMinValue() {
        return thresholds.get(thresholds.size() - 1);
	}

	@Override
	public Float getMaxValue() {
        return thresholds.get(0);
	}

}
