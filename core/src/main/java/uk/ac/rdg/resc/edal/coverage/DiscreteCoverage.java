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
import java.util.List;
import java.util.Map;

/**
 * <p>A {@link Coverage} whose domain consists of a finite number of domain
 * objects, each of which is associated with a single record of measurement
 * values.</p>
 * @param <DO> The type of domain object
 * @author Jon
 */
public interface DiscreteCoverage<DO> extends Coverage {

    /**
     * Gets the list of objects that comprise this coverage's domain
     * @return the list of objects that comprise this coverage's domain
     */
    public List<DO> getDomain();

    /**
     * Gets the list of objects that comprise this coverage's range.  There
     * will be one entry in the list for each domain object, in the same order.
     * Each list entry is a Map of {@link #getMemberNames() member name} to
     * data values.
     * @return
     */
    public List<Map<String, ?>> getRange();

    /**
     * Gets the value of this coverage associated with the given domain object.
     * @todo Check that there are no conflicts in the case that the domain object
     * is a subclass of DirectPosition.
     */
    public Map<String, ?> evaluate(DO domainObject, Collection<String> memberNames);

    public Object evaluate(DO domainObject, String memberName);

}
