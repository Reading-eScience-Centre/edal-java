package uk.ac.rdg.resc.edal.graphics.style.sld;

import java.util.List;

public abstract class AbstractSLDSymbolizer2D extends AbstractSLDSymbolizer {

	protected String xVarName;
	protected String yVarName;
	
	@Override
	protected void setVarNames(List<String> varNames) throws SLDException {
		if(varNames.size() != 2) {
			throw new SLDException("Found " + varNames.size() + " variable names, " +
					"expected two.");
		}
		this.xVarName = varNames.get(0);
		this.yVarName = varNames.get(1);
	}

}
