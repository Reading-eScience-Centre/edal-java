/*
 * Copyright (c) 2010 The University of Reading
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

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import uk.ac.rdg.resc.edal.phenomenon.Phenomenon;

/**
 * <p>A Coverage is a data structure that holds measurement values.  It is
 * a function that maps positions within its domain to data values.</p>
 * @author Jon
 */
public interface Coverage {

    /** @todo getExtent() */

    /**
     * The identifiers of the members of this coverage.  These are internal IDs
     * that are not usually exposed to user interfaces.  For example, if this
     * Coverage contains temperature and salinity data, the member names might
     * be "TMP" and "SAL".  These values are used as keys in other methods in
     * this class, such as {@link #getDataType(java.lang.String)}.
     * @return the identifiers of the members of this coverage
     */
    public Set<String> getMemberNames();

    public RecordType getRangeType();

    /**
     * Returns the data type of the given coverage member.
     * @param memberName The identifier of the coverage member.
     * @return the data type of the given coverage member.
     * @throws IllegalArgumentException if {@code memberName} is not a member
     * of the {@link #getMemberNames() set of member names}.
     */
    public Class<?> getDataType(String memberName);

    /**
     * Returns the phenomenon represented by the given coverage member.
     * @param memberName The identifier of the coverage member.
     * @return the phenomenon represented by the given coverage member.
     * @throws IllegalArgumentException if {@code memberName} is not a member
     * of the {@link #getMemberNames() set of member names}.
     */
    public Phenomenon getPhenomenon(String memberName);

    /**
     * Returns the {@link CoordinateReferenceSystem CRS} used to register
     * information spatially and temporally.
     * @return
     */
    public CoordinateReferenceSystem getCoordinateReferenceSystem();

    /**
     * <p>Returns a set of records of feature attribute values for the specified
     * direct position. The parameter list is a sequence of feature attribute
     * names each of which identifies a field of the range type. If list is null,
     * the operation shall return a value for every field of the range type.
     * Otherwise, it shall return a value for each field included in list.</p>
     * <p>Note that typically the returned Set will only contain a single Record.</p>
     * <p>If the direct position passed is not in the domain of the coverage, then
     * this method returns an empty set (<b>Note:</b> this is different behaviour
     * from the {@link org.opengis.coverage.Coverage#evaluate(org.opengis.geometry.DirectPosition, java.util.Collection)
     * GeoAPI version of this method}).</p>
     * @param pos The position at which the coverage is to be evaluated
     * @param memberNames the feature attribute names to return in the Records.
     * @throws IllegalArgumentException if any of the passed feature attribute
     * names do not appear in this Coverage's {@link #getRangeType() record type}.
     * @throws CannotEvaluateException if the quantity cannot be evaluated.
     * @see org.opengis.coverage.Coverage#evaluate(org.opengis.geometry.DirectPosition, java.util.Collection)
     * @see Collections#emptySet()
     * @todo what if we pass a DP that doesn't even belong in the
     * {@link #getCoordinateReferenceSystem() CRS}?  For example, one with the
     * wrong dimensionality, or that is simply outside the CRS's validity?  Do
     * we throw a runtime exception (programmer error), the empty set (point not
     * in coverage) or "fit" the point to the CRS (e.g. by ignoring dimensions
     * that aren't represented in the coverage)?
     */
    public Record evaluate(DirectPosition pos, Collection<String> memberNames);

    public Object evaluate(DirectPosition pos, String memberName);

}
