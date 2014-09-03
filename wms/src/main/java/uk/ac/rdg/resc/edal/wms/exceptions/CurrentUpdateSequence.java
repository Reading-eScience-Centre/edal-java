/**
 * Copyright (c) 2009 The University of Reading
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

package uk.ac.rdg.resc.edal.wms.exceptions;

import uk.ac.rdg.resc.edal.exceptions.EdalException;

/**
 * Exception that is thrown when a user requests an updatesequence that is equal
 * to the current updatesequence
 * 
 * @author Jon Blower
 */
public class CurrentUpdateSequence extends EdalException {
    private static final long serialVersionUID = 1L;
    private static final String CURRENT_UPDATE_SEQUENCE = "CurrentUpdateSequence";

    /**
     * Creates a new instance of CurrentUpdateSequence
     * 
     * @param updateSequence
     *            The updatesequence requested by the client
     */
    public CurrentUpdateSequence(String updateSequence) {
        super("The updatesequence value " + updateSequence + " is equal to the current value",
                CURRENT_UPDATE_SEQUENCE);
    }

    public CurrentUpdateSequence(String message, Throwable cause) {
        super(message, CURRENT_UPDATE_SEQUENCE, cause);
    }
}
