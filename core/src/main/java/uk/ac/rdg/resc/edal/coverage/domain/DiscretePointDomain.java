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

package uk.ac.rdg.resc.edal.coverage.domain;

import java.util.List;
import uk.ac.rdg.resc.edal.coverage.DiscretePointCoverage;

/**
 * <p>A {@link DiscreteDomain} that consists of a finite number of infinitesimal
 * points.  The domain objects are therefore of the same type as the object
 * used to identify positions within the domain.  This is the domain of a
 * {@link DiscretePointCoverage}.</p>
 * <p>A typical implementation might use
 * {@link #getDomainObjects()}{@link List#contains(java.lang.Object) .contains()}
 * to implement the {@link #contains(java.lang.Object)} operation, and
 * {@link #getDomainObjects()}{@link List#indexOf(java.lang.Object) .indexOf()}
 * to implement {@link #findIndexOf(java.lang.Object)}.</p>
 * @param <P> The type of the domain object and the objects used to define
 * positions within the domain
 * @author Jon
 */
public interface DiscretePointDomain<P> extends DiscreteDomain<P, P>
{
}
