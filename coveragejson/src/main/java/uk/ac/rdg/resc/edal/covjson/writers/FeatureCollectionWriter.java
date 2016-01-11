package uk.ac.rdg.resc.edal.covjson.writers;

import java.util.Collection;

import uk.ac.rdg.resc.edal.covjson.StreamingEncoder.MapEncoder;
import uk.ac.rdg.resc.edal.feature.Feature;

public class FeatureCollectionWriter <T> {

	private final MapEncoder<T> map;
	private final boolean root;

	/**
	 * 
	 * @param encoder
	 * @param root If true, then the feature is the root element in the document
	 * 	that is written.
	 */
	public FeatureCollectionWriter(MapEncoder<T> encoder, boolean root) {
		this.map = encoder;
		this.root = root;
	}

	public void write(Collection<Feature<?>> features) {
		// TODO Auto-generated method stub
		
	}

}
