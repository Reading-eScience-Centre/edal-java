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
