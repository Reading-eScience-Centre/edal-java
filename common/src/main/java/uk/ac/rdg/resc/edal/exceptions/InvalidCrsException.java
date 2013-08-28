package uk.ac.rdg.resc.edal.exceptions;

/**
 * Exception that is thrown when a user requests an unsupported coordinate
 * reference system
 * 
 * @author Jon Blower
 */
public class InvalidCrsException extends EdalException {
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance of InvalidCrsException
     * 
     * @param crsCode
     *            The code of the unsupported CRS
     */
    public InvalidCrsException(String crsCode) {
        super("The CRS " + crsCode + " is not supported by this server", "InvalidCRS");
    }

}
