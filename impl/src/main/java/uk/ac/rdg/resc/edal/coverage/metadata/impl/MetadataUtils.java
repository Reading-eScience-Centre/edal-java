package uk.ac.rdg.resc.edal.coverage.metadata.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uk.ac.rdg.resc.edal.coverage.metadata.RangeMetadata;
import uk.ac.rdg.resc.edal.coverage.metadata.ScalarMetadata;
import uk.ac.rdg.resc.edal.feature.Feature;

public class MetadataUtils {

    /**
     * Returns all sub-members of the metadata tree contained under the top
     * metadata object
     * 
     * @param topMetadata
     *            the top metadata object
     * @return A list containing all descendant metadata
     */
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

    /**
     * Gets the metadata with the given name from the supplied feature
     * 
     * @param feature
     *            the {@link Feature} containing the metadata
     * @param memberName
     *            the ID of the metadata
     * @return Either the desired metadata, or <code>null</code> if it doesn't
     *         exist
     */
    public static RangeMetadata getMetadataForFeatureMember(Feature feature, String memberName) {
        return getDescendantMetadata(feature.getCoverage().getRangeMetadata(), memberName);
    }

    /**
     * Gets a descendant of the supplied {@link RangeMetadata} with the given
     * name
     * 
     * @param topMetadata
     *            The top-level metadata object to search
     * @param memberName
     *            The name of the desired member
     * @return Either the desired metadata, or <code>null</code> if it doesn't
     *         exist
     */
    public static RangeMetadata getDescendantMetadata(RangeMetadata topMetadata, String memberName) {
        if (topMetadata.getMemberNames().contains(memberName)) {
            return topMetadata.getMemberMetadata(memberName);
        } else {
            for (String childMember : topMetadata.getMemberNames()) {
                RangeMetadata memberMetadata = topMetadata.getMemberMetadata(childMember);
                if (!(memberMetadata instanceof ScalarMetadata)) {
                    return getDescendantMetadata(memberMetadata, memberName);
                }
            }
        }
        return null;
    }

    /**
     * This returns a copy of the supplied {@link RangeMetadata} object,
     * containing only those members with the names supplied in
     * <code>members</code>, and their ancestors
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

    /**
     * Returns all metadata objects belonging to the supplied feature which are
     * plottable (i.e. are scalar or have representative children)
     * 
     * @param feature
     *            The desired {@link Feature}
     * @return A {@link List} containing all plottable members
     */
    public static List<RangeMetadata> getPlottableLayers(Feature feature) {
        List<RangeMetadata> plottableMembers = new ArrayList<RangeMetadata>();
        List<RangeMetadata> allTreeMembers = getAllTreeMembers(feature.getCoverage()
                .getRangeMetadata());
        for (RangeMetadata testMetadata : allTreeMembers) {
            if (isPlottable(testMetadata)) {
                plottableMembers.add(testMetadata);
            }
        }
        return plottableMembers;
    }

    /**
     * Gets the string representation of the units for the given feature and
     * member name
     * 
     * @return Either the units string, or "" if there is no member
     */
    public static String getUnitsString(Feature feature, String memberName) {
        RangeMetadata rangeMetadata = getDescendantMetadata(feature.getCoverage()
                .getRangeMetadata(), memberName);
        if (rangeMetadata instanceof ScalarMetadata) {
            ScalarMetadata scalarMetadata = (ScalarMetadata) rangeMetadata;
            return scalarMetadata.getUnits().getUnitString();
        } else {
            List<ScalarMetadata> representativeChildren = rangeMetadata.getRepresentativeChildren();
            if (representativeChildren == null || representativeChildren.size() == 0) {
                return "";
            } else {
                return representativeChildren.get(0).getUnits().getUnitString();
            }
        }
    }

    /**
     * Gets the name of the {@link ScalarMetadata} descendant represented by
     * <code>memberName</code>. This will either be the <code>memberName</code>,
     * or the representative child metadata name, if <code>memberName</code> is
     * a parent metadata object
     * 
     * @param feature
     *            The {@link Feature}
     * @param memberName
     *            The member name
     * @return The corresponding scalar metadata name
     * @throws IllegalArgumentException
     *             if <code>memberName</code> doesn't represent either a scalar
     *             member or a parent layer which has a representative child
     *             member
     */
    public static String getScalarMemberName(Feature feature, String memberName) {
        RangeMetadata rangeMetadata = getDescendantMetadata(feature.getCoverage()
                .getRangeMetadata(), memberName);
        if (rangeMetadata instanceof ScalarMetadata) {
            return rangeMetadata.getName();
        } else {
            List<ScalarMetadata> representativeChildren = rangeMetadata.getRepresentativeChildren();
            if (representativeChildren != null && representativeChildren.size() > 0) {
                return representativeChildren.get(0).getName();
            }
        }
        ScalarMetadata scalarMetadata = getScalarMetadata(feature, memberName);
        if (scalarMetadata == null) {
            throw new IllegalArgumentException(
                    "Cannot get a scalar member corresponding to the name " + memberName);
        } else {
            return scalarMetadata.getName();
        }
    }

    /**
     * Tests whether a particular {@link RangeMetadata} object represents
     * plottable data
     */
    public static boolean isPlottable(RangeMetadata metadata) {
        return (metadata instanceof ScalarMetadata)
                || (metadata.getRepresentativeChildren() != null && metadata
                        .getRepresentativeChildren().size() > 0);
    }

    /**
     * Gets the {@link ScalarMetadata} descendant represented by
     * <code>memberName</code>. This will either be the <code>memberName</code>
     * metadata, or the representative child metadata, if
     * <code>memberName</code> represents a parent metadata object
     * 
     * @param feature
     *            The {@link Feature}
     * @param memberName
     *            The member name
     * @return The corresponding {@link ScalarMetadata}, or null if none exists
     */
    public static ScalarMetadata getScalarMetadata(Feature feature, String memberName) {
        RangeMetadata rangeMetadata = getDescendantMetadata(feature.getCoverage()
                .getRangeMetadata(), memberName);
        if (rangeMetadata instanceof ScalarMetadata) {
            return (ScalarMetadata) rangeMetadata;
        } else {
            List<ScalarMetadata> representativeChildren = rangeMetadata.getRepresentativeChildren();
            if (representativeChildren != null && representativeChildren.size() > 0) {
                return representativeChildren.get(0);
            }
        }
        return null;
    }
}
