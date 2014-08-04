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

import uk.ac.rdg.resc.edal.dataset.Dataset;
import uk.ac.rdg.resc.edal.feature.Feature;

/**
 * Describes what is being measured by a {@link Feature} or {@link Dataset}.
 * 
 * @author Jon
 * @author Guy
 */
public class Parameter {

    private String id;
    private String title;
    private String description;
    /* TODO: This will probably end up as something more complex than a string */
    private String units;
    private String standardName;

    /**
     * @param id
     *            The ID of the parameter
     * @param title
     *            A human-readable title for the quantity being measured
     * @param description
     *            A human-readable description of the quantity
     * @param units
     *            The units of the measured quantity
     */
    public Parameter(String id, String title, String description, String units, String standardName) {
        super();
        this.id = id;
        this.title = title;
        this.description = description;
        this.units = units == null ? "" : units;
        this.standardName = standardName;
    }

    /**
     * @return An identifier that is unique within the context ({@literal e.g.} within
     * the Feature or Dataset).
     */
    public String getId() {
        return id;
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
     * TODO This should allow multiple standard names and have scope for different vocabularies
     */
    public String getStandardName() {
        return standardName;
    }

    @Override
    public String toString() {
        return id + ": " + title + " (" + description + ")" + " Units: " + units;
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
