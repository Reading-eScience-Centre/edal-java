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

public class NcGridSeriesFeature extends AbstractGridSeriesFeature {

    private DataReadingStrategy dataReadingStrategy;
    private GridDatatype grid;
    
    public NcGridSeriesFeature(String name, String id, String description,
            FeatureCollection<? extends Feature> parentCollection, GridSeriesCoverage<Float> coverage,
            DataReadingStrategy dataReadingStrategy, GridDatatype grid) {
        super(name, id, description, parentCollection, coverage);
        this.dataReadingStrategy = dataReadingStrategy;
        this.grid = grid;
    }

    @Override
    public GridCoverage2D<Float> extractHorizontalGrid(int tindex, int zindex, final HorizontalGrid targetDomain) {
        HorizontalGrid sourceGrid = getCoverage().getDomain().getHorizontalGrid();
        PixelMap pixelMap = new PixelMap(sourceGrid, targetDomain);
        
        List<Float> dataList;
        if (pixelMap.isEmpty()) {
            // There is no overlap between the source data grid and the target
            // domain. Return a list of null values.
            // It's very unlikely that the target domain will be bigger than
            // Integer.MAX_VALUE
            dataList = new AbstractList<Float>() {
                @Override
                public Float get(int index) {
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
            float[] data = new float[(int) targetDomain.size()];
            Arrays.fill(data, Float.NaN);
            try {
                dataReadingStrategy.readData(tindex, zindex, grid, pixelMap, data);
            } catch (IOException e) {
                e.printStackTrace();
            }
            dataList = new ArrayList<Float>();
            for (float d : data) {
                if (Float.isNaN(d)) {
                    dataList.add(null);
                } else {
                    dataList.add(d);
                }
            }
        }
        return new GridCoverage2DImpl(getCoverage(), targetDomain, dataList);
    }

}
