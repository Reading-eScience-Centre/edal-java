package uk.ac.rdg.resc.edal.coverage.metadata.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
    
    public static List<RangeMetadata> getAllTreeMembers(RangeMetadata topMetadata){
        List<RangeMetadata> ret = new ArrayList<RangeMetadata>();
        for(String memberName : topMetadata.getMemberNames()){
            RangeMetadata memberMetadata = topMetadata.getMemberMetadata(memberName);
            ret.add(memberMetadata);
            
            Set<String> memberNames = memberMetadata.getMemberNames();
            if(memberName != null && memberNames.size() > 0){
                ret.addAll(getAllTreeMembers(memberMetadata));
            }
        }
        return ret;
    }
}
