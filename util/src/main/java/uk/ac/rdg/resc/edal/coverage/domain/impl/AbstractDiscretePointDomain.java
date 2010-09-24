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

package uk.ac.rdg.resc.edal.coverage.domain.impl;

import uk.ac.rdg.resc.edal.coverage.domain.DiscretePointDomain;

/**
 * Skeletal implementation of a {@link DiscretePointDomain}.  This should only
 * be used with position types (P) that correctly implement the equals() method,
 * otherwise the implementation of {@link #findIndexOf(java.lang.Object)} will
 * not work.
 * @param <P> The type of the domain object and the objects used to define
 * positions within the domain
 * @author Jon
 * @todo
 */
public abstract class AbstractDiscretePointDomain<P> extends AbstractDiscreteDomain<P, P> implements DiscretePointDomain<P>
{

    /**
     * {@inheritDoc}
     * <p>This implementation delegates to {@link #getDomainObjects()}.indexOf(position).
     * Subclasses should override if a more efficient or accurate implementation
     * is possible. Note that List.indexOf(P) depends on correct operation of the
     * P.equals() method.</p>
     */
    @Override
    public int findIndexOf(P position) {
        return this.getDomainObjects().indexOf(position);
    }

}
