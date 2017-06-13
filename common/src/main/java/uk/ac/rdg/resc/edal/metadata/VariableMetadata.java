/*******************************************************************************
 * Copyright (c) 2013 The University of Reading
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

package uk.ac.rdg.resc.edal.metadata;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import uk.ac.rdg.resc.edal.dataset.Dataset;
import uk.ac.rdg.resc.edal.domain.HorizontalDomain;
import uk.ac.rdg.resc.edal.domain.TemporalDomain;
import uk.ac.rdg.resc.edal.domain.VerticalDomain;

/**
 * Describes a variable held within a {@link Dataset}. Variables can be
 * hierarchically nested.
 * 
 * @author Guy Griffiths
 * @author Jon Blower
 */
public class VariableMetadata implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private Dataset dataset;
    private Parameter parameter;
    private HorizontalDomain hDomain;
    private VerticalDomain zDomain;
    private TemporalDomain tDomain;
    private VariableMetadata parent;
    private Set<VariableMetadata> children;
    private boolean scalar;

    private Map<String, Object> variableProperties = new HashMap<>();

    /*
     * A Map containing arbitrary roles of child VariableMetadata objects in
     * relation to this one. This can be used to specify e.g. a certain child
     * represents magnitude of a vector group
     */
    private Map<String, VariableMetadata> childrenWithRoles = new LinkedHashMap<String, VariableMetadata>();

    /**
     * Constructs a {@link VariableMetadata} object holding metadata about a
     * plottable variable.
     * <p>
     * Note that the this {@link VariableMetadata} object will not have a
     * {@link Dataset} associated with it, since these objects are usually
     * created before being attached to a {@link Dataset}. It is expected that
     * when {@link Dataset} is created it will set itself using the
     * {@link VariableMetadata#setDataset(Dataset)} method
     * </p>
     * 
     * @param parameter
     *            The {@link Parameter} which the variable is measuring - the ID
     *            is used as the variable ID and must be unique amongst its
     *            siblings
     * @param hDomain
     *            The {@link HorizontalDomain} on which the variable is measured
     * @param zDomain
     *            The {@link VerticalDomain} on which the variable is measured
     * @param tDomain
     *            The {@link TemporalDomain} on which the variable is measured
     */
    public VariableMetadata(Parameter parameter, HorizontalDomain hDomain, VerticalDomain zDomain,
            TemporalDomain tDomain) {
        this(parameter, hDomain, zDomain, tDomain, true);
    }

    /**
     * Constructs a {@link VariableMetadata} object holding metadata about a
     * variable.
     * <p>
     * Note that the this {@link VariableMetadata} object will not have a
     * {@link Dataset} associated with it, since these objects are usually
     * created before being attached to a {@link Dataset}. It is expected that
     * when {@link Dataset} is created it will set itself using the
     * {@link VariableMetadata#setDataset(Dataset)} method
     * </p>
     * 
     * @param parameter
     *            The {@link Parameter} which the variable is measuring - the ID
     *            is used as the variable ID and must be unique amongst its
     *            siblings
     * @param hDomain
     *            The {@link HorizontalDomain} on which the variable is measured
     * @param zDomain
     *            The {@link VerticalDomain} on which the variable is measured
     * @param tDomain
     *            The {@link TemporalDomain} on which the variable is measured
     * @param scalar
     *            Whether or not this {@link VariableMetadata} can be read as a
     *            scalar quantity
     */
    public VariableMetadata(Parameter parameter, HorizontalDomain hDomain, VerticalDomain zDomain,
            TemporalDomain tDomain, boolean scalar) {
        if (parameter == null) {
            throw new NullPointerException("Parameter cannot be null");
        }
        this.id = parameter.getVariableId();
        this.parameter = parameter;
        this.hDomain = hDomain;
        this.zDomain = zDomain;
        this.tDomain = tDomain;
        parent = null;
        children = new LinkedHashSet<VariableMetadata>();
        this.scalar = scalar;
    }

    /**
     * Recursively set the dataset for this {@link VariableMetadata}
     * 
     * @param dataset
     */
    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
        for (VariableMetadata child : children) {
            child.setDataset(dataset);
        }
    }

    /**
     * @return The identifier of the variable.
     */
    public String getId() {
        return id;
    }

    /**
     * The dataset to which this variable belongs
     */
    public Dataset getDataset() {
        return dataset;
    }

    /**
     * Describes what is being measured by the values of this variable.
     */
    public Parameter getParameter() {
        return parameter;
    }

    /**
     * Returns the horizontal domain of the variable.
     */
    public HorizontalDomain getHorizontalDomain() {
        return hDomain;
    }

    /**
     * Returns the vertical domain of the variable
     */
    public VerticalDomain getVerticalDomain() {
        return zDomain;
    }

    /**
     * Returns the temporal domain of the variable
     */
    public TemporalDomain getTemporalDomain() {
        return tDomain;
    }

    /**
     * Returns the {@link VariableMetadata} of the parent object, or
     * <code>null</code> if this {@link VariableMetadata} has no parent
     */
    public VariableMetadata getParent() {
        return parent;
    }

    /**
     * Returns a {@link Set} containing the children of this
     * {@link VariableMetadata}, or an empty {@link Set} if there are none. This
     * method may not return <code>null</code>
     */
    public Set<VariableMetadata> getChildren() {
        return children;
    }

    /**
     * Sets the parent-child relationship of this {@link VariableMetadata} and
     * another one.
     * 
     * @param parent
     *            The parent {@link VariableMetadata} object
     * @param relationshipToParent
     *            A {@link String} representing the relationship this
     *            {@link VariableMetadata} has with its parent. This is an
     *            arbitrary value which can be used to access
     *            {@link VariableMetadata#getChildWithRole(String)}
     */
    public void setParent(VariableMetadata parent, String relationshipToParent) {
        /*
         * If we're setting a named relationship, we need to check that the
         * parent doesn't already have such a child
         */
        if (relationshipToParent != null && parent != null) {
            if (parent.childrenWithRoles.containsKey(relationshipToParent)) {
                throw new IllegalArgumentException(
                        "The parent already has a child with the relationship: "
                                + relationshipToParent);
            }
        }

        VariableMetadata currentMetadata = parent;
        while (currentMetadata != null) {
            if (currentMetadata.equals(this)) {
                /*
                 * We have a circular metadata tree.
                 */
                throw new IllegalArgumentException(
                        "Setting this as a parent metadata creates a circular tree");
            }
            currentMetadata = currentMetadata.getParent();
        }

        if (this.parent != null && !this.parent.equals(parent)) {
            /*
             * We are changing to a new parent. Therefore, the old one will not
             * have this as a child any more.
             */
            this.parent.children.remove(this);

            Iterator<Entry<String, VariableMetadata>> iterator = this.parent.childrenWithRoles
                    .entrySet().iterator();
            while (iterator.hasNext()) {
                Entry<String, VariableMetadata> children = iterator.next();
                if (children.getValue().equals(this)) {
                    iterator.remove();
                }
            }
        }
        this.parent = parent;
        if (this.parent != null) {
            /*
             * We only add this as a child if it is not the tree root
             */
            this.parent.children.add(this);
            if (relationshipToParent != null) {
                this.parent.childrenWithRoles.put(relationshipToParent.trim(), this);
            }
            dataset = parent.getDataset();
        }
    }

    /**
     * Returns all child {@link VariableMetadata} with the designated role
     * 
     * @param role
     *            The role to search for
     * @return The child {@link VariableMetadata} which has this role, or
     *         <code>null</code> if there are none
     */
    public VariableMetadata getChildWithRole(String role) {
        if (role == null) {
            return null;
        }
        return childrenWithRoles.get(role.trim());
    }

    /**
     * @return Whether this {@link VariableMetadata} represents a scalar
     *         quantity. {@link VariableMetadata} which doesn't is generally
     *         used for grouping other {@link VariableMetadata}.
     */
    public boolean isScalar() {
        return scalar;
    }

    /**
     * @return A {@link Map} of arbitrary properties which this variable has
     *         associated with it. This can be used to record any additional
     *         required information about the variable.
     */
    public Map<String, Object> getVariableProperties() {
        return variableProperties;
    }
}
