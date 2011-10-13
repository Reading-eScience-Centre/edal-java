package uk.ac.rdg.resc.edal.cdm.coverage;

import java.util.ArrayList;
import java.util.List;

import uk.ac.rdg.resc.edal.Extent;
import uk.ac.rdg.resc.edal.Phenomenon;
import uk.ac.rdg.resc.edal.PhenomenonVocabulary;
import uk.ac.rdg.resc.edal.Unit;
import uk.ac.rdg.resc.edal.UnitVocabulary;
import uk.ac.rdg.resc.edal.coverage.RangeMetadata;
import uk.ac.rdg.resc.edal.coverage.RangeMetadataImpl;
import uk.ac.rdg.resc.edal.coverage.VectorGridSeriesCoverage;
import uk.ac.rdg.resc.edal.coverage.domain.GridSeriesDomain;
import uk.ac.rdg.resc.edal.coverage.grid.GridCell4D;
import uk.ac.rdg.resc.edal.coverage.impl.AbstractDiscreteSimpleCoverage;
import uk.ac.rdg.resc.edal.position.GeoPosition;
import uk.ac.rdg.resc.edal.position.Vector2D;
import uk.ac.rdg.resc.edal.position.impl.Vector2DFloat;

public class NcVectorGridSeriesCoverage
		extends
		AbstractDiscreteSimpleCoverage<GeoPosition, GridCell4D, Vector2D<Float>>
		implements VectorGridSeriesCoverage<Float> {

	private NcGridSeriesCoverage xCoverage;
	private NcGridSeriesCoverage yCoverage;
	
	public NcVectorGridSeriesCoverage(NcGridSeriesCoverage xCoverage, NcGridSeriesCoverage yCoverage) throws InstantiationException {
		if(xCoverage.size() != yCoverage.size()){
			throw new InstantiationException("Component coverages must be the same size as each other");
		}
		if(!xCoverage.getVariable().getUnitsString().equalsIgnoreCase(yCoverage.getVariable().getUnitsString())){
			throw new InstantiationException("x- and y-components must have the same units");
		}
		this.xCoverage = xCoverage;
		this.yCoverage = yCoverage;
	}
	
	@Override
	public GridSeriesDomain getDomain() {
		return xCoverage.getDomain();
	}

	@Override
	public Vector2D<Float> evaluate(int tindex, int zindex, int yindex,
			int xindex) {
		float xVal = xCoverage.evaluate(tindex, zindex, yindex, xindex);
		float yVal = yCoverage.evaluate(tindex, zindex, yindex, xindex);
		return new Vector2DFloat(xVal, yVal);
	}

	@Override
	public List<Vector2D<Float>> evaluate(Extent<Integer> tindexExtent,
			Extent<Integer> zindexExtent, Extent<Integer> yindexExtent,
			Extent<Integer> xindexExtent) {
		List<Float> xVals = xCoverage.evaluate(tindexExtent, zindexExtent, yindexExtent, xindexExtent);
		List<Float> yVals = yCoverage.evaluate(tindexExtent, zindexExtent, yindexExtent, xindexExtent);
		
		if(xVals.size() != yVals.size()){
			// TODO throw an exception here
		}
		
		List<Vector2D<Float>> ret = new ArrayList<Vector2D<Float>>();
		for(int i=0; i<xVals.size(); i++){
			ret.add(new Vector2DFloat(xVals.get(i), yVals.get(i)));
		}
		return ret;
	}

	@Override
	public List<Vector2D<Float>> getValues() {
		List<Float> xVals = xCoverage.getValues();
		List<Float> yVals = yCoverage.getValues();
		
		if(xVals.size() != yVals.size()){
			// TODO throw an exception here
		}
		
		List<Vector2D<Float>> ret = new ArrayList<Vector2D<Float>>();
		for(int i=0; i<xVals.size(); i++){
			ret.add(new Vector2DFloat(xVals.get(i), yVals.get(i)));
		}
		return ret;
	}

	@Override
	public String getDescription() {
		String xDesc = xCoverage.getDescription();
		int xIndex = xDesc.indexOf("-component of");
		String description = xDesc.substring(xIndex + 14);
		return description;
	}
	
	@Override
	protected RangeMetadata getRangeMetadata() {
		String xName = xCoverage.getVariable().getName();
		String yName = yCoverage.getVariable().getName();
        RangeMetadata metadata = new RangeMetadataImpl(getDescription(),
                Phenomenon.getPhenomenon(xName+"+"+yName, PhenomenonVocabulary.CLIMATE_AND_FORECAST),
                Unit.getUnit(xCoverage.getVariable().getUnitsString(), UnitVocabulary.UDUNITS));
        return metadata;
	}
}
