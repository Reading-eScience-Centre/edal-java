package uk.ac.rdg.resc.edal.exceptions;

import uk.ac.rdg.resc.edal.geometry.impl.LineString;

/**
 * Exception that is thrown when a {@link LineString} is constructed with an
 * invalid line string specification
 * 
 * @author Jon
 */
public class InvalidLineStringException extends Exception {
    private static final long serialVersionUID = 1L;

    public InvalidLineStringException(String message) {
        super(message);
    }
}
