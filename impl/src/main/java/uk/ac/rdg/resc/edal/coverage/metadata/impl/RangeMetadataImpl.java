/*******************************************************************************
 * Copyright (c) 2012 The University of Reading
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the University of Reading, nor the names of the
 *    authors or contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/

package uk.ac.rdg.resc.edal.coverage.metadata.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.rdg.resc.edal.coverage.metadata.RangeMetadata;
import uk.ac.rdg.resc.edal.coverage.metadata.ScalarMetadata;

/**
 * An implementation of a general {@link RangeMetadata}
 * 
 * @author Guy Griffiths
 * 
 */
public class RangeMetadataImpl implements RangeMetadata {

    private RangeMetadata parent = null;
    protected final Map<String, RangeMetadata> members;
    private final String name;
    private String title;
    private final String description;

    public RangeMetadataImpl(String name, String description) {
        this.name = name;
        this.title = description;
        this.description = description;

        this.members = new LinkedHashMap<String, RangeMetadata>();
    }

    @Override
    public void addMember(RangeMetadata metadata) {
        if (metadata == null)
            throw new IllegalArgumentException("Null metadata is not allowed");
        if (getChildrenOf(getTopParentOf(this)).contains(metadata.getName())) {
            throw new IllegalArgumentException("This metadata already contains a member named "
                    + metadata.getName());
        }
        metadata.setParentMetadata(this);
        members.put(metadata.getName(), metadata);
    }

    /*
     * Finds the top level parent of this metadata tree
     */
    private static RangeMetadata getTopParentOf(RangeMetadata metadata) {
        if (metadata.getParent() == null) {
            return metadata;
        } else {
            return getTopParentOf(metadata.getParent());
        }
    }

    /*
     * Finds all members below the specified metadata
     */
    private static Set<String> getChildrenOf(RangeMetadata metadata) {
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

    @Override
    public RangeMetadata removeMember(String name) {
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
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
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

    @Override
    public void setParentMetadata(RangeMetadata parent) {
        this.parent = parent;
    }

    @Override
    public RangeMetadata clone() throws CloneNotSupportedException {
        RangeMetadataImpl rangeMetadata = new RangeMetadataImpl(name, description);
        for (RangeMetadata member : members.values()) {
            rangeMetadata.addMember(member.clone());
        }
        return rangeMetadata;
    }

    private List<ScalarMetadata> plottableChildren = null;

    public void setChildrenToPlot(List<String> plottableChildrenIds) {
        plottableChildren = new ArrayList<ScalarMetadata>();
        plottableChildren.addAll(getScalarRepresentatives(this, plottableChildrenIds));
    }

    private List<ScalarMetadata> getScalarRepresentatives(RangeMetadata parentMetadata,
            List<String> representativeChildren) {
        List<ScalarMetadata> ret = new ArrayList<ScalarMetadata>();

        for (String representativeChild : representativeChildren) {
            RangeMetadata memberMetadata = parentMetadata.getMemberMetadata(representativeChild);
            if (memberMetadata == null) {
                continue;
            } else if (memberMetadata instanceof ScalarMetadata) {
                ret.add((ScalarMetadata) memberMetadata);
            } else {
                ret.addAll(getScalarRepresentatives(memberMetadata, representativeChildren));
            }
        }
        return ret;
    }

    @Override
    public List<ScalarMetadata> getRepresentativeChildren() {
        return plottableChildren;
    }
}
