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

package uk.ac.rdg.resc.edal.coverage.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.rdg.resc.edal.Phenomenon;
import uk.ac.rdg.resc.edal.Unit;
import uk.ac.rdg.resc.edal.coverage.Coverage;
import uk.ac.rdg.resc.edal.coverage.DiscreteCoverage;
import uk.ac.rdg.resc.edal.coverage.domain.DiscreteDomain;
import uk.ac.rdg.resc.edal.coverage.metadata.RangeMetadata;
import uk.ac.rdg.resc.edal.coverage.metadata.ScalarMetadata;
import uk.ac.rdg.resc.edal.coverage.metadata.impl.RangeMetadataImpl;
import uk.ac.rdg.resc.edal.coverage.metadata.impl.ScalarMetadataImpl;
import uk.ac.rdg.resc.edal.coverage.plugins.Plugin;
import uk.ac.rdg.resc.edal.util.BigList;

/**
 * A partial implementation of a discrete {@link Coverage} which contains
 * multiple members. This provides a base method members to the metadata
 * 
 * @author Guy Griffiths
 * 
 * @param <P>
 *            The type of object used to identify positions within the
 *            coverage's domain. This may be a spatial, temporal, or combined
 *            spatiotemporal position.
 * @param <DO>
 *            The type of domain object
 * @param <GD>
 *            The type of domain which members must be on
 */
public abstract class AbstractMultimemberDiscreteCoverage<P, DO, GD extends DiscreteDomain<P, DO>>
        extends AbstractDiscreteCoverage<P, DO> implements DiscreteCoverage<P, DO> {

    private final String description;
    /*
     * A map of variable ID (i.e. record member names) to their metadata
     */
    private final Map<String, ScalarMetadata> varId2Metadata;

    /*
     * The full metadata tree for this coverage.
     * 
     * Note that this will simply be a parent object containing all of the
     * ScalarMetadata objects found above, until the point when we use plugins.
     * Then the two will not mirror each other (and the distinction stops being
     * pointless...)
     */
    private final RangeMetadata metadata;

    /*
     * A map of variable IDs to Plugins which provide those variables
     */
    protected final Map<String, Plugin> plugins;

    private final GD domain;

    public AbstractMultimemberDiscreteCoverage(String description, GD domain) {
        super();
        this.description = description;
        this.domain = domain;
        varId2Metadata = new HashMap<String, ScalarMetadata>();
        metadata = new RangeMetadataImpl("Root", description);
        plugins = new HashMap<String, Plugin>();
    }

    @Override
    public GD getDomain() {
        return domain;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Set<String> getScalarMemberNames() {
        return varId2Metadata.keySet();
    }

    /*
     * This overrides the default implementation because we allow for the
     * possibility of nested metadata in this coverage
     */
    @Override
    public RangeMetadata getRangeMetadata() {
        return metadata;
    }

    @Override
    public ScalarMetadata getScalarMetadata(String memberName) {
        if (!getScalarMemberNames().contains(memberName)) {
            throw new IllegalArgumentException("Cannot get metadata for " + memberName
                    + " - it is not present in this coverage");
        }
        return varId2Metadata.get(memberName);
    }

    protected void addMemberToMetadata(String memberName, GD domain, String description,
            Phenomenon parameter, Unit units, Class<?> valueType) {
        /*
         * Throw an exception if we already have this variable in the coverage
         */
        if (getScalarMemberNames().contains(memberName)) {
            throw new IllegalArgumentException(
                    "This coverage already contains a member with the ID " + memberName);
        } else {
            /*
             * Otherwise, check that this variable has the same grid as the
             * coverage
             */
            if (this.domain.equals(domain)) {
                /*
                 * If so, create some metadata for the variable, and add it to
                 * the parent metadata, and the flat variable->metadata map
                 */
                ScalarMetadataImpl scalarMetadata = new ScalarMetadataImpl(memberName, description,
                        parameter, units, valueType);
                metadata.addMember(scalarMetadata);
                varId2Metadata.put(memberName, scalarMetadata);
            } else {
                throw new IllegalArgumentException(
                        "The added member does not have the same domain as this coverage");
            }
        }
    }

    /**
     * Adds a plugin to this coverage. This allows certain members to be
     * replaced by others which use them
     * 
     * @param plugin
     *            the {@link Plugin} to add to this coverage
     */
    public void addPlugin(Plugin plugin) {
        /*
         * Check that we have the variables needed for this plugin. We use
         * getAllMembers() to allow plugins to use variables that are already
         * provided by other plugins. This automatically allows us to create
         * multiple layers of nesting within the RangeMetadata
         */
        List<String> oldMembers = plugin.uses();
        if (!getAllMembers().containsAll(oldMembers)) {
            throw new IllegalArgumentException(
                    "Cannot use this Plugin - it requires variables not present in this coverage");
        }

        /*
         * Determine the parent metadata of the combined field set provided by
         * the plugin. This will be the parent of all of the old members, or the
         * root metadata if they are not all the same
         */
        RangeMetadata parentMetadata;
        if (oldMembers.size() > 0) {
            parentMetadata = varId2Metadata.get(oldMembers.get(0)).getParent();
            for (String oldMember : oldMembers) {
                RangeMetadata testMetadata = varId2Metadata.get(oldMember).getParent();
                if (!testMetadata.equals(parentMetadata)) {
                    parentMetadata = metadata;
                    break;
                }
            }
        } else {
            /*
             * We have a plugin which doesn't use any existing members. This can
             * *only* provide a constant, but if someone wants to do that, they
             * are quite welcome to
             */
            parentMetadata = metadata;
        }

        List<ScalarMetadata> oldMetadata = new ArrayList<ScalarMetadata>();

        /*
         * These are the components we want to remove from our list, and our
         * main metadata tree
         */
        for (String oldMember : oldMembers) {
            RangeMetadata oldMetadatum = varId2Metadata.get(oldMember);
            if (oldMetadatum instanceof ScalarMetadata) {
                /*
                 * This can throw an IllegalArgumentException if the member is
                 * not present, but that means we have an inconsistent state,
                 * and something bigger is wrong.
                 * 
                 * i.e. if you're reading this comment because the line below is
                 * throwing an IllegalArgumentException, you have some debugging
                 * to do...
                 */
                oldMetadata.add((ScalarMetadata) removeFromTree(metadata, oldMember));
            }
            varId2Metadata.remove(oldMember);
        }

        /*
         * Generate the full metadata tree
         */
        RangeMetadata rangeMetadata = plugin.generateMetadataTree(oldMetadata);
        /*
         * Add the metadata tree to its parent
         */
        parentMetadata.addMember(rangeMetadata);
        
        /*
         * Now:
         * 
         * Add the plugin to the map of plugins
         * 
         * Get the individual member metadata and put it in the
         * variable->metadata map
         */
        for(String newMember : plugin.provides()){
            plugins.put(newMember, plugin);
            ScalarMetadata memberMetadata = plugin.getMemberMetadata(newMember);
            varId2Metadata.put(newMember, memberMetadata);
        }
    }

    private RangeMetadata removeFromTree(RangeMetadata metadata, String name) {
        if (metadata.getMemberNames().contains(name)) {
            return metadata.removeMember(name);
        } else {
            for (String child : metadata.getMemberNames()) {
                return removeFromTree(metadata.getMemberMetadata(child), name);
            }
        }
        throw new IllegalArgumentException(name + " is not present in this metadata tree");
    }

    /*
     * This gets the variables which are present in files we know about (as
     * opposed to queryable variables which may or may not be virtual variables
     * provided by plugins.
     */
    private Set<String> getAllMembers() {
        /*
         * Get the queryable members
         */
        Set<String> allMembers = new HashSet<String>(getScalarMemberNames());
        for (Plugin p : new HashSet<Plugin>(plugins.values())) {
            /*
             * Add any members previously removed by a plugin
             */
            allMembers.addAll(p.uses());
        }
        /*
         * getAllMembers gives us all the members we can use - i.e. all raw
         * member name PLUS all raw+virtual members names, but NOT any parent
         * (i.e. non-queryable members)
         */
        return allMembers;
    }

    @Override
    public final BigList<?> getValues(final String memberName) {
        if (plugins.containsKey(memberName)) {
            return new AbstractBigList2<Object>() {
                @Override
                public Object get(long index) {
                    final Plugin plugin = plugins.get(memberName);
                    final List<Object> pluginInputs = new ArrayList<Object>();
                    for (String neededId : plugin.uses()) {
                        pluginInputs.add(getValues(neededId).get(index));
                    }
                    return plugin.getProcessedValue(memberName, pluginInputs);
                }
            };
        } else {
            return getValuesList(memberName);
        }
    }

    public abstract BigList<?> getValuesList(String memberName);
}
