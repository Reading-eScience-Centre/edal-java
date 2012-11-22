package uk.ac.rdg.resc.godiva.client.requests;

public interface ErrorHandler {
    /**
     * Handles a general error
     * 
     * @param e
     */
    public void handleError(Throwable e);
}
