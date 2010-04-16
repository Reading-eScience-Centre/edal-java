/*
 * Copyright (c) 2008 The University of Reading
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
 */

package uk.ac.rdg.resc.edal.coverage;

import java.util.Set;
import javax.measure.unit.Unit;
import org.opengis.util.TypeName;
import uk.ac.rdg.resc.edal.phenomenon.Phenomenon;

/**
 * Describes the content of a Coverage's {@link Record}s.  A Record consists of
 * one or more members, each of which has a runtime class, a Phenomenon object
 * (which defines the member more precisely) and its units of measure.
 * @author Jon
 */
public interface RecordType {
    
    /**
     * Returns a Set of unique identifiers for all the members of this coverage
     * ({@literal e.g.} {"temperature", "salinity", "chlorophyll"}.  These
     * identifiers are not usually revealed externally, but are used as internal
     * keys for properties and data values.  Must not return null (return the
     * empty set if this record type really has no members).
     * @return
     */
    public Set<String> getMemberNames();
    
    /**
     * Gets the runtime class of a member.
     * Here {@link Class} replaces GeoAPI's use of {@link TypeName}.
     * @param memberName
     * @return The runtime class of the member
     * @throws NullPointerException if {@code memberName == null}
     * @throws IllegalArgumentException if the memberName is not present in
     * {@link #getMemberNames()}.
     */
    public Class<?> getClass(String memberName);

    /**
     * Returns the Phenomenon object corresponding to the given member name.
     * @param memberName The name of the record member.
     * @return the Phenomenon object corresponding to the given member name.
     * @throws NullPointerException if {@code memberName == null}
     * @throws IllegalArgumentException if the memberName is not present in
     * {@link #getMemberNames()}.
     */
    public Phenomenon getParameter(String memberName);

    /**
     * Returns the units of measure of the given record member as a String.
     * @param memberName The name of the record member.
     * @return the units of measure of the give record member
     * @todo Should we specify that the String must be parseable by Udunits?
     * @todo What if the member inherently has no units (e.g. a quality flag)?
     * Should this return null or the empty string?
     * @todo What if the member has no units, but the numeric value is still
     * relevant (e.g. a ratio)?  Should this return an empty string, "1" or
     * "kg/kg" or similar?
     * @throws NullPointerException if {@code memberName == null}
     * @throws IllegalArgumentException if the memberName is not present in
     * {@link #getMemberNames()}.
     */
    public Unit getUnitsOfMeasure(String memberName);

}
