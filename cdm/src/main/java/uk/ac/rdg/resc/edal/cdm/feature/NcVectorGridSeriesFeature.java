package uk.ac.rdg.resc.edal.cdm.feature;

import java.io.IOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ucar.nc2.dt.GridDatatype;
import uk.ac.rdg.resc.edal.cdm.DataReadingStrategy;
import uk.ac.rdg.resc.edal.cdm.PixelMap;
import uk.ac.rdg.resc.edal.cdm.coverage.NcVectorGridSeriesCoverage;
import uk.ac.rdg.resc.edal.coverage.GridCoverage2D;
import uk.ac.rdg.resc.edal.coverage.GridSeriesCoverage;
import uk.ac.rdg.resc.edal.coverage.domain.GridSeriesDomain;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.coverage.grid.TimeAxis;
import uk.ac.rdg.resc.edal.coverage.grid.VerticalAxis;
import uk.ac.rdg.resc.edal.coverage.impl.GridCoverage2DImpl;
import uk.ac.rdg.resc.edal.feature.Feature;
import uk.ac.rdg.resc.edal.feature.FeatureCollection;
import uk.ac.rdg.resc.edal.feature.impl.AbstractGridSeriesFeature;
import uk.ac.rdg.resc.edal.position.TimePosition;
import uk.ac.rdg.resc.edal.position.Vector2D;
import uk.ac.rdg.resc.edal.position.impl.Vector2DFloat;

public class NcVectorGridSeriesFeature extends
		AbstractGridSeriesFeature<Vector2D<Float>> {

	private DataReadingStrategy dataReadingStrategy;
    private Map<TimePosition, GridDatatypePair> tPosToGridMap = null;
    private boolean noTimeAxis = false;
    
    private class GridDatatypePair{
        private final GridDatatype xGrid;
        private final GridDatatype yGrid;
        private final int tindex;
        public GridDatatypePair(GridDatatype xGrid, GridDatatype yGrid, int tindex) {
            super();
            this.xGrid = xGrid;
            this.yGrid = yGrid;
            this.tindex = tindex;
        }
        public GridDatatype getXGrid() {
            return xGrid;
        }
        public GridDatatype getYGrid() {
            return yGrid;
        }
        public int getTIndex(){
            return tindex;
        }
    }

	public NcVectorGridSeriesFeature(String name, String id,
			String description,
			FeatureCollection<? extends Feature> parentCollection,
			GridSeriesCoverage<Vector2D<Float>> coverage,
			DataReadingStrategy dataReadingStrategy, GridDatatype xGrid,
			GridDatatype yGrid) {
		super(name, id, description, parentCollection, coverage);
		this.dataReadingStrategy = dataReadingStrategy;
		TimeAxis tAxis = coverage.getDomain().getTimeAxis();
		if(tAxis != null){
		    tPosToGridMap = new HashMap<TimePosition, GridDatatypePair>();
		    int tindex = 0;
		    for(TimePosition t : tAxis.getCoordinateValues()){
		        GridDatatypePair gridPair = new GridDatatypePair(xGrid, yGrid, tindex);
		        tPosToGridMap.put(t, gridPair);
		        tindex++;
		    }
        } else {
            tPosToGridMap.put(null, new GridDatatypePair(xGrid, yGrid, -1));
            noTimeAxis = true;
        }
	}
	

    public void mergeGrids(GridDatatype gridX, GridDatatype gridY, HorizontalGrid hGrid,
            VerticalAxis vAxis, TimeAxis tAxis) {
        if(tPosToGridMap == null){
            throw new UnsupportedOperationException("The existing feature has no time axis to merge with");
        }
        GridSeriesDomain domain = getCoverage().getDomain();
        if(!domain.getHorizontalGrid().equals(hGrid) || (domain.getVerticalAxis()!= null && !domain.getVerticalAxis().equals(vAxis))){
            throw new UnsupportedOperationException("You cannot merge features with different spatial axes");
        }
        NcVectorGridSeriesCoverage coverage = (NcVectorGridSeriesCoverage) getCoverage();
        coverage.addToCoverage(gridX.getVariable(), gridY.getVariable(), tAxis);
        int tindex = 0;
        for(TimePosition t : tAxis.getCoordinateValues()){
            GridDatatypePair gridPair = new GridDatatypePair(gridX, gridY, tindex);
            tPosToGridMap.put(t, gridPair);
            tindex++;
        }
    }

    @Override
	public GridCoverage2D<Vector2D<Float>> extractHorizontalGrid(int tindex,
			int zindex, final HorizontalGrid targetDomain) {
        TimePosition tPos = null;
        if(!noTimeAxis){
            tPos = getCoverage().getDomain().getTimeAxis().getCoordinateValue(tindex);
        }
		HorizontalGrid sourceGrid = getCoverage().getDomain()
				.getHorizontalGrid();
		PixelMap pixelMap = new PixelMap(sourceGrid, targetDomain);

		List<Vector2D<Float>> dataList;
		if (pixelMap.isEmpty()) {
            /*
             * There is no overlap between the source data grid and the target
             * domain. Return a list of null values. It's very unlikely that the
             * target domain will be bigger than Integer.MAX_VALUE
             */
			dataList = new AbstractList<Vector2D<Float>>() {
				@Override
				public Vector2D<Float> get(int index) {
					if (index < 0 || index >= (int) targetDomain.size())
						throw new IndexOutOfBoundsException();
					return null;
				}

				@Override
				public int size() {
					return (int) targetDomain.size();
				}
			};
		} else {
			float[] xData = new float[(int) targetDomain.size()];
			float[] yData = new float[(int) targetDomain.size()];
			Arrays.fill(xData, Float.NaN);
			Arrays.fill(yData, Float.NaN);
			GridDatatypePair grid;
			try {
			    grid = tPosToGridMap.get(tPos);
			    if(!noTimeAxis){
			        tindex = tPosToGridMap.get(tPos).getTIndex();
			    }
				dataReadingStrategy.readData(tindex, zindex, grid.getXGrid(), pixelMap, xData);
				dataReadingStrategy.readData(tindex, zindex, grid.getYGrid(), pixelMap, yData);
			} catch (IOException e) {
				// TODO deal with this better
				e.printStackTrace();
			}
			dataList = new ArrayList<Vector2D<Float>>();
			for(int i=0; i<xData.length; i++){
				if(Float.isNaN(xData[i]) || Float.isNaN(yData[i])){
					dataList.add(null);
				} else {
					dataList.add(new Vector2DFloat(xData[i], yData[i]));
				}
			}
		}
		return new GridCoverage2DImpl<Vector2D<Float>>(getCoverage(), targetDomain, dataList);
	}
}
