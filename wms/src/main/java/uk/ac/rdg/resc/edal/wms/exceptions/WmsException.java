package uk.ac.rdg.resc.edal.wms.exceptions;

/**
 * Exception specific to WMS
 * 
 * @author Jon Blower
 */
public class WmsException extends Exception {
    private static final long serialVersionUID = 1L;
    private String code = null;

    public WmsException(String message) {
        super(message);
    }

    public WmsException(String message, String code) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
