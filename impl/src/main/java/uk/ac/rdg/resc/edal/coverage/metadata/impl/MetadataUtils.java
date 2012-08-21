package uk.ac.rdg.resc.edal.coverage.metadata.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uk.ac.rdg.resc.edal.coverage.metadata.RangeMetadata;
import uk.ac.rdg.resc.edal.coverage.metadata.ScalarMetadata;
import uk.ac.rdg.resc.edal.feature.Feature;

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

    public static List<RangeMetadata> getAllTreeMembers(RangeMetadata topMetadata) {
        List<RangeMetadata> ret = new ArrayList<RangeMetadata>();
        for (String memberName : topMetadata.getMemberNames()) {
            RangeMetadata memberMetadata = topMetadata.getMemberMetadata(memberName);
            ret.add(memberMetadata);

            Set<String> memberNames = memberMetadata.getMemberNames();
            if (memberName != null && memberNames.size() > 0) {
                ret.addAll(getAllTreeMembers(memberMetadata));
            }
        }
        return ret;
    }

    public static RangeMetadata getMetadataForFeatureMember(Feature feature, String memberName) {
        return getDescendentMetadata(feature.getCoverage().getRangeMetadata(), memberName);
    }

    /**
     * This removes all members of the metadata which do not appear in members
     * 
     * @param metadata
     *            the {@link RangeMetadata} object
     * @param members
     *            the members to keep
     */
    public static RangeMetadata getCopyOfMetadataContaining(RangeMetadata metadata,
            Set<String> members) {
        RangeMetadata newMetadata = null;
        try {
            newMetadata = metadata.clone();

            Set<String> memberNames = new HashSet<String>(newMetadata.getMemberNames());
            for (String memberName : memberNames) {
                RangeMetadata memberMetadata = newMetadata.getMemberMetadata(memberName);
                if (memberMetadata instanceof ScalarMetadata) {
                    if (!members.contains(memberName)) {
                        newMetadata.removeMember(memberName);
                    }
                } else {
                    memberMetadata = getCopyOfMetadataContaining(memberMetadata, members);
                    if (memberMetadata.getMemberNames().size() == 0) {
                        newMetadata.removeMember(memberName);
                    }
                }
            }
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return newMetadata;
    }

    public static List<ScalarMetadata> getAllScalarDescendents(RangeMetadata topMetadata) {
        List<ScalarMetadata> ret = new ArrayList<ScalarMetadata>();
        for (String memberName : topMetadata.getMemberNames()) {
            RangeMetadata memberMetadata = topMetadata.getMemberMetadata(memberName);
            if (memberMetadata instanceof ScalarMetadata) {
                ret.add((ScalarMetadata) memberMetadata);
            } else {
                ret.addAll(getAllScalarDescendents(memberMetadata));
            }
        }
        return ret;
    }

    public static List<RangeMetadata> getPlottableLayers(Feature feature) {
        List<RangeMetadata> plottableMembers = new ArrayList<RangeMetadata>();
        List<RangeMetadata> allTreeMembers = getAllTreeMembers(feature.getCoverage()
                .getRangeMetadata());
        for (RangeMetadata testMetadata : allTreeMembers) {
            if (testMetadata instanceof ScalarMetadata
                    || (testMetadata.getRepresentativeChildren() != null && testMetadata
                            .getRepresentativeChildren().size() > 0)) {
                plottableMembers.add(testMetadata);
            }
        }
        return plottableMembers;
    }
    
    public static String getUnitsString(Feature feature, String memberName){
        RangeMetadata rangeMetadata = getDescendentMetadata(feature.getCoverage()
                .getRangeMetadata(), memberName);
        if(rangeMetadata instanceof ScalarMetadata){
            ScalarMetadata scalarMetadata = (ScalarMetadata) rangeMetadata;
            return scalarMetadata.getUnits().getUnitString();
        } else  {
            List<ScalarMetadata> representativeChildren = rangeMetadata.getRepresentativeChildren();
            if(representativeChildren == null || representativeChildren.size()==0){
                return "";
            } else {
                return representativeChildren.get(0).getUnits().getUnitString();
            }
        }
    }
    
    public static String getScalarMemberName(Feature feature, String memberName){
        RangeMetadata rangeMetadata = getDescendentMetadata(feature.getCoverage().getRangeMetadata(), memberName);
        if(rangeMetadata instanceof ScalarMetadata){
            return rangeMetadata.getName();
        } else {
            List<ScalarMetadata> representativeChildren = rangeMetadata.getRepresentativeChildren();
            if(representativeChildren != null && representativeChildren.size() > 0){
                return representativeChildren.get(0).getName();
            }
        }
        throw new IllegalArgumentException("Cannot get a scalar member corresponding to the name "+memberName);
    }
    
    public static boolean isPlottable(RangeMetadata metadata){
        return (metadata instanceof ScalarMetadata)
                || (metadata.getRepresentativeChildren() != null && metadata
                        .getRepresentativeChildren().size() > 0);
    }
}
