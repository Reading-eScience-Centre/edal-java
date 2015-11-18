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

import java.awt.Color;
import java.util.Map;

import uk.ac.rdg.resc.edal.dataset.Dataset;
import uk.ac.rdg.resc.edal.feature.Feature;

/**
 * Describes what is being measured by a {@link Feature} or {@link Dataset}.
 * 
 * @author Jon
 * @author Guy Griffiths
 */
public class Parameter {

    public static class Category {
        private final String label;
        private final Color colour;
        private final String description;

        public Category(String label, Color colour, String description) {
            this.label = label;
            this.colour = colour;
            this.description = description;
        }

        public String getLabel() {
            return label;
        }

        public Color getColour() {
            return colour;
        }

        public String getDescription() {
            return description;
        }
    }

    private String varId;
    private String title;
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

    /**
     * A colour set for generating categorical palettes. This is a rainbow
     * colour set, so picking values as spread out as possible from this will
     * generate a reasonable categorical map.
     * 
     * It is preferable to specify the colours manually for categorical data,
     * but this is here for those occasions where that is not possible.
     */
    public static final Color[] CATEGORICAL_COLOUR_SET = new Color[] { new Color(140, 0, 0),
            new Color(158, 0, 0), new Color(175, 0, 0), new Color(193, 0, 0), new Color(211, 0, 0),
            new Color(228, 0, 0), new Color(246, 0, 0), new Color(255, 7, 0),
            new Color(255, 23, 0), new Color(255, 39, 0), new Color(255, 55, 0),
            new Color(255, 71, 0), new Color(255, 87, 0), new Color(255, 103, 0),
            new Color(255, 119, 0), new Color(255, 135, 0), new Color(255, 151, 0),
            new Color(255, 167, 0), new Color(255, 183, 0), new Color(255, 199, 0),
            new Color(255, 215, 0), new Color(255, 231, 0), new Color(255, 247, 0),
            new Color(247, 255, 7), new Color(231, 255, 23), new Color(215, 255, 39),
            new Color(199, 255, 55), new Color(183, 255, 71), new Color(167, 255, 87),
            new Color(151, 255, 103), new Color(135, 255, 119), new Color(119, 255, 135),
            new Color(103, 255, 151), new Color(87, 255, 167), new Color(71, 255, 183),
            new Color(55, 255, 199), new Color(39, 255, 215), new Color(23, 255, 231),
            new Color(7, 255, 247), new Color(0, 251, 255), new Color(0, 235, 255),
            new Color(0, 219, 255), new Color(0, 203, 255), new Color(0, 187, 255),
            new Color(0, 171, 255), new Color(0, 155, 255), new Color(0, 139, 255),
            new Color(0, 123, 255), new Color(0, 107, 255), new Color(0, 91, 255),
            new Color(0, 75, 255), new Color(0, 59, 255), new Color(0, 43, 255),
            new Color(0, 27, 255), new Color(0, 11, 255), new Color(0, 0, 255),
            new Color(0, 0, 239), new Color(0, 0, 223), new Color(0, 0, 207), new Color(0, 0, 191),
            new Color(0, 0, 175), new Color(0, 0, 159), new Color(0, 0, 143) };
}
