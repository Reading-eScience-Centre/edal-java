package uk.ac.rdg.resc.edal.graphics.style;

import javax.naming.OperationNotSupportedException;

public interface DensityMap {

	float getDensity(Number value) throws OperationNotSupportedException;

	Float getMinValue();

	Float getMaxValue();

}
