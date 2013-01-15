package uk.ac.rdg.resc.edal.graphics.style;

import uk.ac.rdg.resc.edal.feature.Feature;
import uk.ac.rdg.resc.edal.feature.FeatureCollection;

public class FeatureCollectionAndMemberName {
    private FeatureCollection<? extends Feature> feature;
    private String memberName;

    public FeatureCollectionAndMemberName(FeatureCollection<? extends Feature> feature, String memberName) {
        super();
        this.feature = feature;
        this.memberName = memberName;
    }

    public FeatureCollection<? extends Feature> getFeatureCollection() {
        return feature;
    }

    public String getMemberName() {
        return memberName;
    }
}
