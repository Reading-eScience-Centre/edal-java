package uk.ac.rdg.resc.edal.position.impl;

import uk.ac.rdg.resc.edal.position.Vector2D;

public class Vector2DFloat implements Vector2D<Float> {

	private float x;
	private float y;

	/**
	 * Initialise the vector with x and y components
	 * 
	 * @param x
	 *            the x-component of the vector
	 * @param y
	 *            the y-component of the vector
	 */
	public Vector2DFloat(float x, float y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public Float getX() {
		return x;
	}

	@Override
	public Float getY() {
		return y;
	}

	@Override
	public Float getMagnitude() {
		return (float) Math.sqrt(x*x+y*y);
	}

	@Override
	public float getDirection() {
		return (float) Math.atan2(y, x);
	}

}
