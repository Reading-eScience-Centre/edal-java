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

package uk.ac.rdg.resc.edal.graphics.style.util;


/**
 * Interface defining the enahnced metadata about a variable, including title,
 * description, copyright/more info and default values for layer plotting
 * 
 * Any of these methods may return <code>null</code> if no default is set.
 * 
 * @author Guy Griffiths
 */
public interface EnhancedVariableMetadata {
    /**
     * @return The ID of the variable this {@link EnhancedVariableMetadata} is
     *         associated with
     */
    public String getId();

    /**
     * @return The title of this layer to be displayed in the menu and the
     *         Capabilities document
     */
    public String getTitle();

    /**
     * @return A brief description of this layer to be displayed in the
     *         Capabilities document
     */
    public String getDescription();

    /**
     * @return Copyright information about this layer to be displayed be clients
     */
    public String getCopyright();

    /**
     * @return More information about this layer to be displayed be clients
     */
    public String getMoreInfo();

    /**
     * @return The default plot settings for this variable - this may not return
     *         <code>null</code>, but any of the defined methods within the
     *         returned {@link PlottingStyleParameters} object may do.
     */
    public PlottingStyleParameters getDefaultPlottingParameters();
}
