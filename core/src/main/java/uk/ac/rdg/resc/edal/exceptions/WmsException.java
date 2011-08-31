package uk.ac.rdg.resc.edal.exceptions;

/**
 * Exception that will cause a ServiceException document to be returned to the
 * client. See WEB-INF/jsp/displayWmsException.jsp and WMS-servlet.xml.
 * 
 * @author Jon Blower $Revision$ $Date$ $Log$
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
