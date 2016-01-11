package uk.ac.rdg.resc.edal.covjson;

import java.util.Collection;

import uk.ac.rdg.resc.edal.feature.Feature;

public class CoverageJsonWriter {
	private final StreamingEncoder encoder;

	public CoverageJsonWriter(StreamingEncoder encoder) {
		this.encoder = encoder;
	}
	
	void write(Feature<?> feature) {
		
	}
	
	void write(Collection<Feature<?>> features) {
		
	}
}
