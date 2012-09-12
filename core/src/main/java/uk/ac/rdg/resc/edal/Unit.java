/*******************************************************************************
 * Copyright (c) 2011 The University of Reading
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
 *******************************************************************************/

package uk.ac.rdg.resc.edal;

/**
 * <p>
 * Simple immutable class consisting of a unit string and the vocabulary that
 * can be used to interpret the string. Instances of this class are created
 * through the static factory methods, which give the possibility in future to
 * cache instances of this class, saving object creation and garbage collection.
 * </p>
 * <p>
 * Instances of this class are <b>not</b> guaranteed to be valid units within
 * the vocabulary in question. External methods must be used to check validity
 * and to convert units between systems.
 * </p>
 * 
 * @author Jon Blower
 */
public final class Unit {

    private final String unitString;
    private final UnitVocabulary unitVocabulary;

    /**
     * Instantiates a new {@link Unit}. This is private, and should only be
     * accessed by the static method getUnit
     * 
     * @param unitString
     *            the {@link String} representing the {@link Unit}
     * @param unitVocabulary
     *            the {@link UnitVocabulary} of which this {@link Unit} is a
     *            part
     */
    private Unit(String unitString, UnitVocabulary unitVocabulary) {
        this.unitString = unitString;
        this.unitVocabulary = unitVocabulary;
    }

    /**
     * Gets an instance of a unit with the given string in the given vocabulary.
     */
    public static Unit getUnit(String unitString, UnitVocabulary unitVocabulary) {
        return new Unit(unitString, unitVocabulary);
    }

    /**
     * Gets an instance of a unit with the given string in an unknown
     * vocabulary. The returned Unit should not generally be considered to be
     * convertible to other unit types.
     */
    public static Unit getUnit(String unitString) {
        return getUnit(unitString, UnitVocabulary.UNKNOWN);
    }

    /**
     * @return a {@link String} representing the {@link Unit} type
     */
    public String getUnitString() {
        return unitString;
    }

    /**
     * @return the {@link UnitVocabulary} of which this {@link Unit} is a part
     *         of
     */
    public UnitVocabulary getVocabulary() {
        return unitVocabulary;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((unitString == null) ? 0 : unitString.hashCode());
        result = prime * result + ((unitVocabulary == null) ? 0 : unitVocabulary.hashCode());
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
        Unit other = (Unit) obj;
        if (unitString == null) {
            if (other.unitString != null)
                return false;
        } else if (!unitString.equals(other.unitString))
            return false;
        if (unitVocabulary != other.unitVocabulary)
            return false;
        return true;
    }
}
