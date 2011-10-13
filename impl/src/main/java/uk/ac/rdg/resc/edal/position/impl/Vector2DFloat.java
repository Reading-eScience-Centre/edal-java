package uk.ac.rdg.resc.edal.position.impl;

import uk.ac.rdg.resc.edal.position.Vector2D;

public class Vector2DFloat implements Vector2D<Float> {

	private float magnitude;
	private float direction;

	/**
	 * Initialise the vector with x and y components
	 * 
	 * @param x
	 *            the x-component of the vector
	 * @param y
	 *            the y-component of the vector
	 */
	public Vector2DFloat(float x, float y) {
		magnitude = (float) Math.sqrt(x*x+y*y);
		direction = (float) Math.atan2(y, x);
	}

	@Override
	public Float getX() {
		return (float) (magnitude * Math.cos(direction));
	}

	@Override
	public Float getY() {
		return (float) (magnitude * Math.sin(direction));
	}

	@Override
	public Float getMagnitude() {
		return magnitude;
	}

	@Override
	public float getDirection() {
		return direction;
	}

}
