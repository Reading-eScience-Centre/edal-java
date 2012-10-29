package uk.ac.rdg.resc.godiva.client.handlers;

public interface AviExportHandler {
    public String getAviUrl(String times, String fps);
    public void animationStarted(String times, String fps);
    public void animationStopped();
}
