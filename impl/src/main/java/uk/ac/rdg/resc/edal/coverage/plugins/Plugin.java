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

package uk.ac.rdg.resc.edal.coverage.plugins;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uk.ac.rdg.resc.edal.coverage.grid.GridValuesMatrix;
import uk.ac.rdg.resc.edal.coverage.impl.AbstractMultimemberDiscreteCoverage;
import uk.ac.rdg.resc.edal.coverage.metadata.RangeMetadata;
import uk.ac.rdg.resc.edal.coverage.metadata.ScalarMetadata;
import uk.ac.rdg.resc.edal.coverage.metadata.impl.MetadataUtils;

/**
 * A generic class defining a plugin for coverages. This is used with
 * {@link AbstractMultimemberDiscreteCoverage}s to provide new members by using
 * others, or to simply pass through values whilst defining a new metadata
 * structure (e.g. a complex tree) Plugins can be implemented by subclassing
 * this class and defining the required methods:
 * 
 * {@link Plugin#generateMetadata(String, List, RangeMetadata)}
 * {@link Plugin#generateValue(String, List)}
 * 
 * @author Guy Griffiths
 * 
 */
public abstract class Plugin {
    private final List<String> uses;
    private final List<RangeMetadata> usesMetadata;
    private final String baseName;
    private Set<String> provides;

    /**
     * Construct a new {@link Plugin}. This will set the metadata of the fields
     * used and the description. The Plugin is not fully initialised until
     * {@link Plugin#init()} is called.
     * 
     * @param usesMetadata
     *            The {@link RangeMetadata} used by this plugin.
     */
    public Plugin(List<RangeMetadata> usesMetadata) {
        this.usesMetadata = usesMetadata;

        uses = new ArrayList<String>();
        for (RangeMetadata usesMetadatum : usesMetadata) {
            uses.add(usesMetadatum.getName());
        }

        StringBuilder build = new StringBuilder();
        for (String component : uses) {
            build.append(component);
        }
        baseName = build.toString();
    }

    /**
     * Initialises the plugin. This generates the new metadata to see what the
     * plugin provides.
     */
    public void init() {
        RangeMetadata rangeMetadata = getRangeMetadata();

        provides = new HashSet<String>();
        List<RangeMetadata> providedMetadata = MetadataUtils.getAllTreeMembers(rangeMetadata);
        for (RangeMetadata providedMetadatum : providedMetadata) {
            provides.add(providedMetadatum.getName());
        }
    }

    /**
     * A mangled version of the IDs of the metadata used in this plugin. Child
     * classes can use this as part of their provided variable IDs if they like
     * 
     * @return
     */
    protected String getParentName() {
        return baseName;
    }

    /**
     * Returns a list of the IDs needed by this plugin
     * 
     * @return
     */
    public List<String> uses() {
        return uses;
    }

    /**
     * Returns a {@link Set} of the IDs this plugin provides
     * 
     * @return
     */
    public Set<String> provides() {
        return provides;
    }

    private void checkValidRequest(String memberName, List<?> supplied) {
        if (memberName != null && !provides.contains(memberName)) {
            throw new IllegalArgumentException("This Plugin does not provide the field "
                    + memberName);
        }
        if (supplied == null) {
            if ((uses != null && uses.size() > 0)) {
                throw new IllegalArgumentException(
                        "This Plugin needs some data, but you have supplied none");
            }
        } else {
            if (uses == null && supplied.size() > 0) {
                throw new IllegalArgumentException(
                        "This Plugin needs no data, but you have supplied some");
            } else if (uses != null && supplied.size() != uses.size()) {
                throw new IllegalArgumentException("This Plugin needs " + uses.size()
                        + " fields, but you have provided " + supplied.size());
            }
        }
    }

    /**
     * Gets a value from the plugin. Normally clients will use
     * {@link Plugin#getProcessedValues(String, List)} and get a
     * {@link GridValuesMatrix}
     * 
     * @param memberName
     *            The desired member ID
     * @param values
     *            The values which the plugin uses, in the same order as they
     *            are in {@link Plugin#uses()}
     * @return The value generated by the plugin
     */
    public Object getProcessedValue(String memberName, List<Object> values) {
        checkValidRequest(memberName, values);
        return generateValue(memberName, values);
    }

    /**
     * Gets a {@link GridValuesMatrix} where values are generated by the
     * {@link Plugin}
     * 
     * @param memberName
     *            The desired member ID
     * @param gvmInputs
     *            The {@link GridValuesMatrix}s for the members which this
     *            plugin uses, in the same order as they are in
     *            {@link Plugin#uses()}
     * @return A {@link GridValuesMatrix} containing values generated by the
     *         plugin
     */
    public GridValuesMatrix<?> getProcessedValues(String memberName,
            final List<? extends GridValuesMatrix<?>> gvmInputs) {
        checkValidRequest(memberName, gvmInputs);

        return new PluginWrappedGridValuesMatrix(this, gvmInputs, memberName);
    }

    private boolean metadataGenerated = false;

    /**
     * Gets the top-level {@link RangeMetadata} for the members generated by the
     * plugin
     * 
     * @return
     */
    public RangeMetadata getRangeMetadata() {
        metadataGenerated = true;
        return generateRangeMetadata(usesMetadata);
    }

    /**
     * Gets the metadata for a scalar member
     * 
     * @param memberName
     * @return
     */
    public ScalarMetadata getMemberMetadata(String memberName) {
        if (!metadataGenerated) {
            throw new IllegalStateException(
                    "Call generateMetadataTree first to generate the metadata");
        }
        return getScalarMetadata(memberName);
    }

    /**
     * Generate the whole metadata tree from a list of component metadata. This
     * list will be the same one passed to the constructor.
     * 
     * Note that the names of the {@link ScalarMetadata} objects in this tree
     * are the names which will be referred to in
     * {@link Plugin#generateValue(String, List)},
     * {@link Plugin#generateValueType(String, List)}, and
     * {@link Plugin#getScalarMetadata(String)}
     * 
     * @param metadataList
     *            The list of {@link RangeMetadata} objects for the fields which
     *            the {@link Plugin} uses
     * @return
     */
    protected abstract RangeMetadata generateRangeMetadata(List<RangeMetadata> metadataList);

    /**
     * Returns the {@link ScalarMetadata} for a particular (scalar) member.
     * These can safely be generated at the call to
     * {@link Plugin#generateRangeMetadata(List)} and cached, since it is
     * guaranteed that this method will be first called after
     * generateRangeMetadata
     * 
     * @param memberName
     * @return
     */
    protected abstract ScalarMetadata getScalarMetadata(String memberName);

    /**
     * Returns the value type of the desired member
     * 
     * @param memberName
     *            The desired member name
     * @param classes
     *            A list of value types of the used objects
     * @return
     */
    protected abstract Class<?> generateValueType(String memberName, List<Class<?>> classes);

    /**
     * Generates the value
     * 
     * @param memberName
     *            The member to generate a value for
     * @param values
     *            A list of input values, in the order specified in the
     *            constructor (and returned in {@link Plugin#uses()}
     * @return
     */
    protected abstract Object generateValue(String memberName, List<Object> values);

}
