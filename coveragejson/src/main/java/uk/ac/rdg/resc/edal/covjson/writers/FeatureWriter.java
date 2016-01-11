package uk.ac.rdg.resc.edal.covjson.writers;

import java.io.IOException;

import uk.ac.rdg.resc.edal.covjson.StreamingEncoder.MapEncoder;
import uk.ac.rdg.resc.edal.feature.Feature;

public class FeatureWriter <T> {

	private final MapEncoder<T> map;
	private final boolean root;

	/**
	 * 
	 * @param encoder
	 * @param root If true, then the feature is the root element in the document
	 * 	that is written.
	 */
	public FeatureWriter(MapEncoder<T> encoder, boolean root) {
		this.map = encoder;
		this.root = root;
	}

	public void write(Feature<?> feature) throws IOException {
		if (root) {
			Util.addJsonLdContext(map);
		}
		map
		  .put("type", "Coverage")
		  .startMap("title").put("en", feature.getName()).end();
		
		MapEncoder<MapEncoder<T>> domain = map.startMap("domain");
		new DomainWriter<>(domain).write(feature);
		
		MapEncoder<MapEncoder<T>> parameters = map.startMap("parameters");
		new ParametersWriter<>(parameters).write(feature.getParameterMap().values());
		
		MapEncoder<MapEncoder<T>> ranges = map.startMap("ranges");
		new RangesWriter<>(ranges).write(feature);
	}

}
