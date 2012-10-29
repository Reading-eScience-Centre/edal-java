package uk.ac.rdg.resc.edal.feature;

public interface UniqueMembersFeatureCollection<F extends Feature> extends FeatureCollection<F> {
    public F getFeatureContainingMember(String memberName);
}
