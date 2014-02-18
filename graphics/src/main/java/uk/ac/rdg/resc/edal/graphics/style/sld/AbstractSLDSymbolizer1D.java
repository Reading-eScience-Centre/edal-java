package uk.ac.rdg.resc.edal.graphics.style.sld;

import java.util.List;

public abstract class AbstractSLDSymbolizer1D extends AbstractSLDSymbolizer {

	protected String layerName;
	
	@Override
	protected void setVarNames(List<String> varNames) throws SLDException {
		if (varNames.size() != 1) {
			throw new SLDException("Found " + varNames.size() + " variable names, " +
					"expected one.");
		}
		this.layerName = varNames.get(0);
	}

}
