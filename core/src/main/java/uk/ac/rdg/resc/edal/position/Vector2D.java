package uk.ac.rdg.resc.edal.position;

/**
 * A class representing a 2-dimensional vector
 * 
 * @author Guy Griffiths
 *
 * @param <R>
 */
public interface Vector2D<R> {
	/**
	 * Gets the x-component of the vector
	 * @return
	 */
	public R getX();
	
	/**
	 * Gets the y-component of the vector
	 * @return
	 */
	public R getY();
	
	/**
	 * Gets the magnitude
	 * @return
	 */
	public R getMagnitude();
	
	/**
	 * Gets the direction in radians
	 * @return
	 */
	public float getDirection();
}
