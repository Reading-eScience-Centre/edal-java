package uk.ac.rdg.resc.edal.graphics.exceptions;

import uk.ac.rdg.resc.edal.exceptions.WmsException;

/**
 * Exception that is thrown when a user requests an unsupported image format
 * 
 * @author Jon Blower $Revision$ $Date$ $Log$
 */
public class InvalidFormatException extends WmsException {
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance of InvalidFormatException
     * 
     * @param message
     *            The message to display to the client
     */
    public InvalidFormatException(String message) {
        super(message, "InvalidFormat");
    }

}
