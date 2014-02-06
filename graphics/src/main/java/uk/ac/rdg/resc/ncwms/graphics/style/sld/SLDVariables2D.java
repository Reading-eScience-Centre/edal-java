package uk.ac.rdg.resc.ncwms.graphics.style.sld;

public class SLDVariables2D {

	private String xVariable;
	private String yVariable;

	public SLDVariables2D() {}
	
	public SLDVariables2D(String[] variables) throws SLDException {
		if (variables.length != 2) {
			throw new SLDException ("The must be exactly two variables for a bivariate function.");
		}
		this.xVariable = variables[0];
		this.yVariable = variables[1];
	}

	public String getXVariable() {
		return xVariable;
	}

	public void setXVariable(String xVariable) {
		this.xVariable = xVariable;
	}

	public String getYVariable() {
		return yVariable;
	}

	public void setYVariable(String yVariable) {
		this.yVariable = yVariable;
	}
	
}
