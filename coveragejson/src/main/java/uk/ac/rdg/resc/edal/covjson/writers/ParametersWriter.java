package uk.ac.rdg.resc.edal.covjson.writers;

import java.util.Collection;

import uk.ac.rdg.resc.edal.covjson.StreamingEncoder.MapEncoder;
import uk.ac.rdg.resc.edal.domain.Domain;
import uk.ac.rdg.resc.edal.metadata.Parameter;

public class ParametersWriter <T> {

	private final MapEncoder<T> encoder;

	public ParametersWriter(MapEncoder<T> encoder) {
		this.encoder = encoder;
	}

	public void write(Collection<Parameter> parameters) {
		
		
	}

}
