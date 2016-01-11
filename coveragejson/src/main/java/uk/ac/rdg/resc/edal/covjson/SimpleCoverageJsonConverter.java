package uk.ac.rdg.resc.edal.covjson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;

import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.feature.Feature;

public class SimpleCoverageJsonConverter implements CoverageJsonConverter {

	@Override
	public String convertFeatureToJson(Feature<?> feature) {
		// TODO this is too much overhead, just output to stream and not string
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		StreamingEncoder encoder;
		try {
			encoder = new JsonStreamingEncoder(os);
		} catch (IOException e) {
			throw new EdalException("Error writing CoverageJSON", e);
		}
		CoverageJsonWriter writer = new CoverageJsonWriter(encoder);
		writer.write(feature);
		String covjson = os.toString();
		return covjson;
	}

	@Override
	public String convertFeaturesToJson(Collection<Feature<?>> features) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		StreamingEncoder encoder;
		try {
			encoder = new JsonStreamingEncoder(os);
		} catch (IOException e) {
			throw new EdalException("Error writing CoverageJSON", e);
		}
		CoverageJsonWriter writer = new CoverageJsonWriter(encoder);
		writer.write(features);
		String covjson = os.toString();
		return covjson;
	}

}
