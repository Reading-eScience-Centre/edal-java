package uk.ac.rdg.resc.edal.covjson.writers;

import uk.ac.rdg.resc.edal.covjson.StreamingEncoder.MapEncoder;
import uk.ac.rdg.resc.edal.feature.Feature;

public class RangesWriter <T> {

	private final MapEncoder<T> encoder;

	public RangesWriter(MapEncoder<T> encoder) {
		this.encoder = encoder;
	}

	public void write(Feature<?> feature) {
		
		
	}

}
