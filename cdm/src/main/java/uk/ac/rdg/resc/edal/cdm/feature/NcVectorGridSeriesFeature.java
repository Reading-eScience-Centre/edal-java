package uk.ac.rdg.resc.edal.cdm.feature;

import java.io.IOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ucar.nc2.dt.GridDatatype;
import uk.ac.rdg.resc.edal.cdm.DataReadingStrategy;
import uk.ac.rdg.resc.edal.cdm.PixelMap;
import uk.ac.rdg.resc.edal.coverage.GridCoverage2D;
import uk.ac.rdg.resc.edal.coverage.GridSeriesCoverage;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.coverage.impl.GridCoverage2DImpl;
import uk.ac.rdg.resc.edal.feature.Feature;
import uk.ac.rdg.resc.edal.feature.FeatureCollection;
import uk.ac.rdg.resc.edal.feature.impl.AbstractGridSeriesFeature;
import uk.ac.rdg.resc.edal.position.Vector2D;
import uk.ac.rdg.resc.edal.position.impl.Vector2DFloat;

public class NcVectorGridSeriesFeature extends
		AbstractGridSeriesFeature<Vector2D<Float>> {

	private DataReadingStrategy dataReadingStrategy;
	private GridDatatype xGrid;
	private GridDatatype yGrid;

	public NcVectorGridSeriesFeature(String name, String id,
			String description,
			FeatureCollection<? extends Feature> parentCollection,
			GridSeriesCoverage<Vector2D<Float>> coverage,
			DataReadingStrategy dataReadingStrategy, GridDatatype xGrid,
			GridDatatype yGrid) {
		super(name, id, description, parentCollection, coverage);
		this.dataReadingStrategy = dataReadingStrategy;
		this.xGrid = xGrid;
		this.yGrid = yGrid;
	}

	@Override
	public GridCoverage2D<Vector2D<Float>> extractHorizontalGrid(int tindex,
			int zindex, final HorizontalGrid targetDomain) {
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
			try {
				dataReadingStrategy.readData(tindex, zindex, xGrid, pixelMap, xData);
				dataReadingStrategy.readData(tindex, zindex, yGrid, pixelMap, yData);
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
