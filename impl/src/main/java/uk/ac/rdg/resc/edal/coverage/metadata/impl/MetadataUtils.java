package uk.ac.rdg.resc.edal.coverage.metadata.impl;

import uk.ac.rdg.resc.edal.coverage.metadata.RangeMetadata;
import uk.ac.rdg.resc.edal.coverage.metadata.ScalarMetadata;

public class MetadataUtils {

    public static RangeMetadata getDescendentMetadata(RangeMetadata topMetadata, String memberName) {
        if (topMetadata.getMemberNames().contains(memberName)) {
            return topMetadata.getMemberMetadata(memberName);
        } else {
            for (String childMember : topMetadata.getMemberNames()) {
                RangeMetadata memberMetadata = topMetadata.getMemberMetadata(childMember);
                if (!(memberMetadata instanceof ScalarMetadata)) {
                    return getDescendentMetadata(memberMetadata, memberName);
                }
            }
        }
        return null;
    }
}
