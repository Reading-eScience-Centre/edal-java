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
import java.util.Map;

import uk.ac.rdg.resc.edal.dataset.Dataset;
import uk.ac.rdg.resc.edal.feature.Feature;

/**
 * Describes what is being measured by a variable within a {@link Feature} or a
 * {@link Dataset}.
 * 
 * @author Jon
 * @author Guy Griffiths
 */
public class Parameter implements Serializable {
    private static final long serialVersionUID = 1L;

    public static class Category {
        private final String id;
        private final String label;
        private final String colour;
        private final String description;
        private final Map<String, String> foreignLabels;

        public Category(String id, String label, String colour, String description) {
            if (id == null) {
                throw new IllegalArgumentException("The ID of a Category may not be null");
            }
            this.id = id;
            if (label != null) {
                this.label = label;
            } else {
                this.label = "Category for " + id;
            }
            this.colour = colour;
            this.description = description;
            this.foreignLabels = new HashMap<>();
        }
        
        public Category(String id, String label, String colour, String description, Map<String, String> foreignLabels) {
            if (id == null) {
                throw new IllegalArgumentException("The ID of a Category may not be null");
            }
            this.id = id;
            if (label != null) {
                this.label = label;
            } else {
                this.label = "Category for " + id;
            }
            this.colour = colour;
            this.description = description;
            this.foreignLabels = foreignLabels;
        }

        /**
         * @return The ID of this {@link Category}. Will never return
         *         <code>null</code>
         */
        public String getId() {
            return id;
        }

        /**
         * @return The label associated with this {@link Category}. Will never
         *         return <code>null</code>
         */
        public String getLabel() {
            return label;
        }
        
        /**
         * @return A {@link Map} of language code to the category label in that language 
         */
        public Map<String, String> getForeignLabels() {
            return foreignLabels;
        }

        /**
         * @return A {@link String} representing the preferred colour to be used
         *         for this {@link Category}. May return <code>null</code>
         */
        public String getColour() {
            return colour;
        }

        /**
         * @return A description of this {@link Category}. May return
         *         <code>null</code>
         */
        public String getDescription() {
            return description;
        }
    }

    private String varId;
    private String title;
    private Map<String, String> foreignTitles = new HashMap<>();
    private String description;
    /* TODO: This will probably end up as something more complex than a string */
    private String units;
    private String standardName;
    private Map<Integer, Category> categories = null;

    /**
     * @param varId
     *            The ID of the variable which this parameter describes
     * @param title
     *            A human-readable title for the quantity being measured
     * @param description
     *            A human-readable description of the quantity
     * @param units
     *            The units of the measured quantity
     * @param standardName
     *            The standard of the measured quantity
     */
    public Parameter(String varId, String title, String description, String units,
            String standardName) {
        super();
        if (varId == null || varId.matches("^.*[^a-zA-Z0-9_\\-:.].*$")) {
            throw new IllegalArgumentException(
                    "A Parameter must have an ID consisting of only alphanumeric characters, underscores, hyphens, periods, and colons");
        }
        this.varId = varId;
        this.title = title;
        this.description = description;
        this.units = units == null ? "" : units;
        this.standardName = standardName;
    }

    /**
     * @param varId
     *            The ID of the variable which this parameter describes
     * @param title
     *            A human-readable title for the quantity being measured
     * @param description
     *            A human-readable description of the quantity
     * @param units
     *            The units of the measured quantity
     * @param standardName
     *            The standard of the measured quantity
     * @param categories
     *            The {@link Map} of integers to the categories they represent
     *            (for categorical data only)
     */
    public Parameter(String varId, String title, String description, String units,
            String standardName, Map<Integer, Category> categories) {
        super();
        this.varId = varId;
        this.title = title;
        this.description = description;
        this.units = units == null ? "" : units;
        this.standardName = standardName;
        this.categories = categories;
    }

    /**
     * @return The identifier of the variable this {@link Parameter} describes.
     *         This is unique within the context ({@literal e.g.} within the
     *         {@link Feature} or {@link Dataset}).
     */
    public String getVariableId() {
        return varId;
    }

    /**
     * @return Human-readable, fairly short title for the parameter.
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * @return A Map of langauge code to title in that language
     */
    public Map<String, String> getForeignTitles() {
        return foreignTitles;
    }

    /**
     * @return Human-readable, perhaps-lengthy description of the parameter.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return The units of this parameter
     */
    public String getUnits() {
        return units;
    }

    /**
     * @return The standard name of the phenomena which this represents
     * 
     *         TODO This should allow multiple standard names and have scope for
     *         different vocabularies
     */
    public String getStandardName() {
        return standardName;
    }

    /**
     * @return A {@link Map} of integer values to the {@link Category} they
     *         represent. Returns <code>null</code> for non-categorical data.
     */
    public Map<Integer, Category> getCategories() {
        return categories;
    }

    @Override
    public String toString() {
        return varId + ": " + title + " (" + description + ")" + " Units: " + units;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((standardName == null) ? 0 : standardName.hashCode());
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
        Parameter other = (Parameter) obj;
        if (standardName == null) {
            if (other.standardName != null)
                return false;
        } else if (!standardName.equals(other.standardName))
            return false;
        if (units == null) {
            if (other.units != null)
                return false;
        } else if (!units.equals(other.units))
            return false;
        return true;
    }
}
