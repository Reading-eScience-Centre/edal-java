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

package uk.ac.rdg.resc.godiva.client.handlers;

import uk.ac.rdg.resc.godiva.client.state.GodivaStateInfo;

public interface AviExportHandler {
    /**
     * Returns the URL to fetch an AVI file. This depends on the timesteps and
     * frame rate required (all other data can be obtained from the current
     * state ({@link GodivaStateInfo})
     * 
     * @param times
     *            A string representing the desired times
     * @param fps
     *            The frame rate, in frames-per-second
     * @return A URL which will lead to an AVI file containing an animation
     */
    public String getAviUrl(String times, String fps);

    /**
     * This is called when an animation is started. We provide the times and FPS
     * so that the implementation can call getAviUrl if required (for example to
     * set a link to the AVI when an animation is started as an overlay)
     * 
     * @param times
     * @param fps
     */
    public void animationStarted(String times, String fps);

    /**
     * Called when the animation is stopped.
     */
    public void animationStopped();
}
