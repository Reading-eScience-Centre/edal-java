package uk.ac.rdg.resc.edal.covjson;

import java.io.IOException;
import java.util.Collection;

import uk.ac.rdg.resc.edal.covjson.StreamingEncoder.MapEncoder;
import uk.ac.rdg.resc.edal.covjson.writers.FeatureCollectionWriter;
import uk.ac.rdg.resc.edal.covjson.writers.FeatureWriter;
import uk.ac.rdg.resc.edal.feature.Feature;

public class CoverageJsonWriter {
	private final StreamingEncoder encoder;

	public CoverageJsonWriter(StreamingEncoder encoder) {
		this.encoder = encoder;
	}
	
	void write(Feature<?> feature) throws IOException {
		MapEncoder<StreamingEncoder> map = encoder.startMap();
		new FeatureWriter<>(map, true).write(feature);
		map.end();
		encoder.end();
	}
	
	void write(Collection<Feature<?>> features) throws IOException {
		MapEncoder<StreamingEncoder> map = encoder.startMap();
		new FeatureCollectionWriter<>(map, true).write(features);
		map.end();
		encoder.end();
	}
}
