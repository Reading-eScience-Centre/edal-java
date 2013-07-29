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

import java.util.Set;

import uk.ac.rdg.resc.edal.dataset.Dataset;
import uk.ac.rdg.resc.edal.domain.HorizontalDomain;
import uk.ac.rdg.resc.edal.domain.TemporalDomain;
import uk.ac.rdg.resc.edal.domain.VerticalDomain;

/**
 * Describes a variable held within a {@link Dataset}. Variables can be
 * hierarchically nested.
 * 
 * @author Jon
 * @author Guy
 */
public interface VariableMetadata {
    /**
     * The identifier of the variable with the parent dataset.
     */
    public String getId();

    /**
     * The dataset to which this variable belongs
     */
    public Dataset getDataset();

    /**
     * Describes what is being measured by the values of this variable.
     */
    public Parameter getParameter();

    /**
     * Returns the horizontal domain of the variable.
     */
    public HorizontalDomain getHorizontalDomain();

    /**
     * Returns the vertical domain of the variable
     */
    public VerticalDomain getVerticalDomain();

    /**
     * Returns the temporal domain of the variable
     */
    public TemporalDomain getTemporalDomain();

    /**
     * Returns the {@link VariableMetadata} of the parent object, or
     * <code>null</code> if this {@link VariableMetadata} has no parent
     */
    public VariableMetadata getParent();

    /**
     * Returns a {@link Set} containing the children of this
     * {@link VariableMetadata}, or an empty {@link Set} if there are none. This
     * method may not return <code>null</code>
     */
    public Set<VariableMetadata> getChildren();
}
