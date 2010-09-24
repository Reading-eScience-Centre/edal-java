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
import uk.ac.rdg.resc.edal.coverage.domain.ProfileDomain;

/**
 * <p>A Coverage that contains values for a vertical profile of data</p>
 * @author Jon
 */
public interface ProfileCoverage extends DiscretePointCoverage<Double>
{
    @Override
    public ProfileDomain getDomain();

    /**
     * Returns the value of the coverage at the given vertical coordinate.
     * @param position The vertical position at which the coverage is to be evaluated,
     * which must be expressed in this coverage's {@link #getCoordinateReferenceSystem()
     * vertical coordinate reference system}.
     * @param fieldNames The fields for which the coverage is to be evaluated.
     * If {@code fieldNames} is null, this method returns a Record containing
     * values for all fields.  If {@code fieldNames} is non-null but empty, this
     * method returns an empty record.
     * @return a {@link Record} containing the values of the given fields at the
     * given position, or null if this coverage is not defined at the given
     * position.
     * @todo check the behaviour if the coverage is not defined at the position.
     */
    @Override
    public Record evaluate(Double position, Collection<String> fieldNames);

}
