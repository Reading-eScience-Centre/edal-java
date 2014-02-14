package uk.ac.rdg.resc.edal.graphics.style.sld;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;

public class AbstractSLDFunction2D<T> extends AbstractSLDFunction<T> {

	protected SLDVariables2D variables;
	
	public AbstractSLDFunction2D(XPath xPath, Node function) throws SLDException {
		super(xPath, function);
		try {
			parseVariables();
		} catch (Exception e) {
			throw new SLDException(e);
		}
	}
	
	public SLDVariables2D getVariables() {
		return variables;
	}
	
	private void parseVariables() throws XPathExpressionException, SLDException {
		String[] variables = parseLookupValue();
		this.variables = new SLDVariables2D(variables);
	}

	public void copyVariables(SLDVariables2D variables) {
		variables.setXVariable(this.variables.getXVariable());
		variables.setYVariable(this.variables.getYVariable());
	}
	
}
