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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uk.ac.rdg.resc.edal.coverage.grid.GridValuesMatrix;
import uk.ac.rdg.resc.edal.coverage.impl.AbstractMultimemberDiscreteCoverage;
import uk.ac.rdg.resc.edal.coverage.metadata.RangeMetadata;
import uk.ac.rdg.resc.edal.coverage.metadata.ScalarMetadata;

/**
 * A generic class defining a plugin for coverages. This can be used with
 * {@link AbstractMultimemberDiscreteCoverage}s to provide new members through
 * others. Plugins can be implemented by subclassing this class and defining the
 * required methods:
 * 
 * {@link Plugin#generateMetadata(String, List, RangeMetadata)}
 * {@link Plugin#generateValue(String, List)}
 * 
 * TODO: Could this be implemented more efficiently using {@link Enum}s?
 * 
 * @author Guy Griffiths
 * 
 */
public abstract class Plugin {
    private final List<String> uses;
    private final Set<String> provides;
    private final String baseName;
    private final String description;

    public Plugin(List<String> uses, List<String> provides, String description) {
        if (provides == null || provides.size() == 0) {
            throw new IllegalArgumentException("A plugin must provide some data");
        }
        this.uses = uses;
        this.description = description;

        StringBuilder build = new StringBuilder();
        for (String component : uses) {
            build.append(component);
        }
        baseName = build.toString();

        this.provides = new HashSet<String>();
        for (String provided : provides) {
            this.provides.add(baseName + "_" + provided);
        }
    }

    public String getDescription() {
        return description;
    }

    public String getParentName() {
        return baseName;
    }

    public List<String> uses() {
        return uses;
    }

    public Set<String> provides() {
        return provides;
    }

    private void checkValidRequest(String memberName, int numberOfValues) {
        if (memberName != null && (!provides.contains(memberName) || !memberName.startsWith(baseName))) {
            throw new IllegalArgumentException("This Plugin does not provide the field "
                    + memberName);
        }
        if (numberOfValues != uses.size()) {
            throw new IllegalArgumentException("This Plugin needs " + uses.size()
                    + " fields, but you have provided " + numberOfValues);
        }
    }

    public Object getProcessedValue(String memberName, List<Object> values) {
        checkValidRequest(memberName, values.size());
        String reducedName = memberName.substring(baseName.length() + 1);
        return generateValue(reducedName, values);
    }

    public GridValuesMatrix<?> getProcessedValues(String memberName,
            final List<? extends GridValuesMatrix<?>> gvmInputs) {
        checkValidRequest(memberName, gvmInputs.size());

        return new PluginWrappedGridValuesMatrix(this, gvmInputs, memberName);
    }

    private boolean metadataGenerated = false;
    
    public RangeMetadata generateMetadataTree(List<ScalarMetadata> metadataList) {
        checkValidRequest(null, metadataList.size());
        metadataGenerated = true;
        return generateRangeMetadata(metadataList);
    }
    
    public ScalarMetadata getMemberMetadata(String memberName){
        if(!metadataGenerated){
            throw new IllegalStateException("Call generateMetadataTree first to generate the metadata");
        }
        String reducedName = memberName.substring(baseName.length() + 1);
        return getScalarMetadata(reducedName);
    }

    protected abstract RangeMetadata generateRangeMetadata(List<ScalarMetadata> metadataList);

    protected abstract ScalarMetadata getScalarMetadata(String memberName);
    
    protected abstract Class<?> generateValueType(String component, List<Class<?>> classes);

    protected abstract Object generateValue(String component, List<Object> values);

}
