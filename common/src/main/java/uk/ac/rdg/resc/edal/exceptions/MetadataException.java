package uk.ac.rdg.resc.edal.exceptions;

/**
 * Exception that is thrown when there is a problem with metadata.
 * 
 * @author Guy
 */
public class MetadataException extends EdalException {
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance of a MetadataException
     */
    public MetadataException(String message) {
        super(message);
    }
    
    public MetadataException(String message, Throwable cause) {
        super(message, cause);
    }
}
