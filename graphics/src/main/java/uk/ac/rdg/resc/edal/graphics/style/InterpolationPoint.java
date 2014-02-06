package uk.ac.rdg.resc.edal.graphics.style;

public class InterpolationPoint<T> {
	private Float data;
	private T value;
	
	public InterpolationPoint(Float data, T value) {
		this.data = data;
		this.value = value;
	}
	
	public Float getData() {
		return data;
	}
	
	public T getValue() {
		return value;
	}
}