package uk.ac.rdg.resc.edal.graphics.style.util;

import uk.ac.rdg.resc.edal.feature.MapFeature;

public interface FeatureCatalogue {
    public class MapFeatureAndMember {
        private MapFeature feature;
        private String member;

        public MapFeatureAndMember(MapFeature feature, String member) {
            super();
            this.feature = feature;
            this.member = member;
        }

        public MapFeature getMapFeature() {
            return feature;
        }

        public String getMember() {
            return member;
        }
    }

    public MapFeatureAndMember getFeatureAndMemberName(String id, GlobalPlottingParams params);
}
