package uk.ac.rdg.resc.edal.graphics.style.sld;

import uk.ac.rdg.resc.edal.exceptions.EdalException;

public class SLDException extends EdalException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5465038077417251267L;

	public SLDException() {
	    super("Problem with SLD document");
	}

	public SLDException(String arg0) {
		super(arg0);
	}
	
	public SLDException(Throwable arg0) {
	    super("Problem with SLD document", arg0);
	}

	public SLDException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}
}
