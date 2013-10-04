package uk.ac.rdg.resc.edal.wms;

import uk.ac.rdg.resc.edal.domain.Extent;

/**
 * Interface defining the metadata which provides default (server-configured)
 * values for layer plotting.
 * 
 * URL parameters take precedence, then values returned by this interface, then
 * basic defaults (which are unlikely to be ideal).
 * 
 * This means that it is legal for any of these methods to return
 * <code>null</code> - basic defaults will be used in these cases
 * 
 * @author Guy
 */
public interface WmsLayerMetadata {
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
     * @return The default scale range of this layer, or <code>null</code> if no
     *         scale range is set
     */
    public Extent<Float> getColorScaleRange();

    /**
     * @return The default palette to use for this layer. This can be an
     *         existing palette name, or a palette definition in the form
     *         #[aa]bbggrr,#[aa]bbggrr,#[aa]bbggrr...
     */
    public String getPalette();

    /**
     * @return <code>true</code> if this variable is to use logarithmic scaling
     *         by default
     */
    public Boolean isLogScaling();

    /**
     * @returns The default number of colour bands to use for this layer's
     *          palette
     */
    public Integer getNumColorBands();

    /**
     * @return Copyright information about this layer to be displayed be clients
     */
    public String getCopyright();

    /**
     * @return More information about this layer to be displayed be clients
     */
    public String getMoreInfo();

    /**
     * @return Whether or not this layer can be queried with GetFeatureInfo
     *         requests
     */
    public boolean isQueryable();
}
