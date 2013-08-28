package uk.ac.rdg.resc.edal.exceptions;

/**
 * Exception specific to WMS
 * 
 * @author Jon Blower
 */
public class EdalException extends Exception {
    private static final long serialVersionUID = 1L;
    private String code = null;

    public EdalException(String message) {
        super(message);
    }

    public EdalException(String message, String code) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
