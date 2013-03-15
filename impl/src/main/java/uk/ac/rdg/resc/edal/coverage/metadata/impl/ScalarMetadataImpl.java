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

import java.util.Collections;
import java.util.List;
import java.util.Set;

import uk.ac.rdg.resc.edal.Phenomenon;
import uk.ac.rdg.resc.edal.Unit;
import uk.ac.rdg.resc.edal.coverage.metadata.RangeMetadata;
import uk.ac.rdg.resc.edal.coverage.metadata.ScalarMetadata;

/**
 * An implementation of {@link ScalarMetadata}
 * 
 * @author Jon
 * @author Guy Griffiths
 * 
 */
public class ScalarMetadataImpl implements ScalarMetadata {

    private final String name;
    private String title;
    private final String description;
    private final Phenomenon parameter;
    private final Unit units;
    private final Class<?> clazz;
    private RangeMetadata parent = null;

    /**
     * Instantiates the {@link ScalarMetadata} object
     * 
     * @param name
     *            The unique identifier of the field
     * @param description
     *            A human-readable description of the field
     * @param parameter
     *            The {@link Phenomenon} this field represents
     * @param units
     *            The {@link Unit}s of the data represented by this field
     * @param clazz
     *            The {@link Class} returned by calls to evaluate on this field
     * @param allowedPlotStyles
     *            The available plotting styles for this field, with the first
     *            being the default. Must contain at least one style
     */
    public ScalarMetadataImpl(String name, String description, Phenomenon parameter, Unit units,
            Class<?> clazz) {
        this.name = name;
        if(description != null && !description.equals("")) {
            this.title = description.substring(0, 1).toUpperCase() + description.substring(1);
        } else {
            this.title = description;
        }
        this.description = description;
        this.parameter = parameter;
        this.units = units;
        this.clazz = clazz;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Phenomenon getParameter() {
        return parameter;
    }

    @Override
    public Unit getUnits() {
        return units;
    }

    @Override
    public RangeMetadata getMemberMetadata(String memberName) {
        return null;
    }

    @Override
    public String getName() {
        return this.name;
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
    public Class<?> getValueType() {
        return this.clazz;
    }

    @Override
    public Set<String> getMemberNames() {
        return Collections.emptySet();
    }

    @Override
    public RangeMetadata getParent() {
        return this.parent;
    }

    @Override
    public void setParentMetadata(RangeMetadata parent) {
        this.parent = parent;
    }

    @Override
    public RangeMetadata removeMember(String memberName) {
        throw new UnsupportedOperationException(
                "This is scalar metadata, and cannot have child members.  Therefore removing members is unsupported");
    }

    @Override
    public void addMember(RangeMetadata metadata) {
        throw new UnsupportedOperationException(
                "This is scalar metadata, and cannot have child members.  Therefore adding members is unsupported");
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((parameter == null) ? 0 : parameter.hashCode());
        result = prime * result + ((parent == null) ? 0 : parent.hashCode());
        result = prime * result + ((units == null) ? 0 : units.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ScalarMetadataImpl other = (ScalarMetadataImpl) obj;
        if (clazz == null) {
            if (other.clazz != null)
                return false;
        } else if (!clazz.equals(other.clazz))
            return false;
        if (description == null) {
            if (other.description != null)
                return false;
        } else if (!description.equals(other.description))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (parameter == null) {
            if (other.parameter != null)
                return false;
        } else if (!parameter.equals(other.parameter))
            return false;
        if (parent == null) {
            if (other.parent != null)
                return false;
        } else if (!parent.equals(other.parent))
            return false;
        if (units == null) {
            if (other.units != null)
                return false;
        } else if (!units.equals(other.units))
            return false;
        return true;
    }

    @Override
    public ScalarMetadata clone() throws CloneNotSupportedException {
        return new ScalarMetadataImpl(name, description, parameter, units, clazz);
    }

    @Override
    public List<ScalarMetadata> getRepresentativeChildren() {
        /*
         * This is scalar, so it never has any children to plot
         */
        return null;
    }
}
