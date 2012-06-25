package uk.ac.rdg.resc.edal.coverage.metadata.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uk.ac.rdg.resc.edal.coverage.metadata.RangeMetadata;

/**
 * An implementation of a general {@link RangeMetadata}
 * 
 * @author Guy Griffiths
 * 
 */
public class RangeMetadataImpl implements RangeMetadata {

    private final RangeMetadata parent;
    private final Map<String, RangeMetadata> members;
    private final String name;
    private final String description;

    public RangeMetadataImpl(RangeMetadata parent, String name, String description) {
        this.parent = parent;
        this.name = name;
        this.description = description;

        this.members = new HashMap<String, RangeMetadata>();
    }

    @Override
    public void addMember(RangeMetadata metadata) {
        if (metadata == null)
            throw new IllegalArgumentException("Null metadata is not allowed");
        members.put(metadata.getName(), metadata);
    }

    @Override
    public RangeMetadata removeMember(String name) {
        /*
         * This should remove a member however deeply it is nested.
         */
        RangeMetadata removed = members.remove(name);
        if (removed == null) {
            throw new IllegalArgumentException(name + " is not a child of this metadata");
        } else {
            return removed;
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Set<String> getMemberNames() {
        return members.keySet();
    }

    @Override
    public RangeMetadata getMemberMetadata(String name) {
        return members.get(name);
    }

    @Override
    public RangeMetadata getParent() {
        return parent;
    }

    public Set<String> getFlattenedMemberNames() {
        return getChildrenOf(this);
    }

    private Set<String> getChildrenOf(RangeMetadata metadata) {
        Set<String> children = new HashSet<String>();

        Set<String> set = metadata.getMemberNames();
        for (String member : set) {
            RangeMetadata memberMetadata = metadata.getMemberMetadata(member);
            if (!memberMetadata.getMemberNames().isEmpty()) {
                children.addAll(getChildrenOf(memberMetadata));
            } else {
                children.add(member);
            }
        }
        return children;
    }
}
