package uk.ac.rdg.resc.edal.cdm.coverage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.nc2.Variable;
import uk.ac.rdg.resc.edal.Extent;
import uk.ac.rdg.resc.edal.Phenomenon;
import uk.ac.rdg.resc.edal.PhenomenonVocabulary;
import uk.ac.rdg.resc.edal.Unit;
import uk.ac.rdg.resc.edal.UnitVocabulary;
import uk.ac.rdg.resc.edal.coverage.AbstractDiscreteSimpleCoverage;
import uk.ac.rdg.resc.edal.coverage.GridSeriesCoverage;
import uk.ac.rdg.resc.edal.coverage.RangeMetadata;
import uk.ac.rdg.resc.edal.coverage.RangeMetadataImpl;
import uk.ac.rdg.resc.edal.coverage.domain.GridSeriesDomain;
import uk.ac.rdg.resc.edal.coverage.domain.impl.GridSeriesDomainImpl;
import uk.ac.rdg.resc.edal.coverage.grid.GridCell4D;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.coverage.grid.TimeAxis;
import uk.ac.rdg.resc.edal.coverage.grid.VerticalAxis;
import uk.ac.rdg.resc.edal.position.GeoPosition;
import uk.ac.rdg.resc.edal.util.Extents;

public class NcGridSeriesCoverage extends AbstractDiscreteSimpleCoverage<GeoPosition, GridCell4D, Float> implements
        GridSeriesCoverage<Float> {

    private Variable variable;
    private HorizontalGrid hGrid;
    private VerticalAxis vAxis;
    private TimeAxis tAxis;

    public NcGridSeriesCoverage(Variable variable, HorizontalGrid hGrid, VerticalAxis vAxis, TimeAxis tAxis) {
        this.variable = variable;
        this.hGrid = hGrid;
        this.vAxis = vAxis;
        this.tAxis = tAxis;
    }

    @Override
    protected RangeMetadata getRangeMetadata() {
        RangeMetadata metadata = new RangeMetadataImpl(variable.getDescription(),
                                       Phenomenon.getPhenomenon(variable.getName(), PhenomenonVocabulary.CLIMATE_AND_FORECAST),
                                       Unit.getUnit(variable.getUnitsString(), UnitVocabulary.UDUNITS));
        return metadata;
    }

    @Override
    public Float evaluate(int tindex, int zindex, int yindex, int xindex) {
        List<Range> ranges = new ArrayList<Range>();
        Float ret = null;
        try {
            if (tAxis != null) {
                ranges.add(new Range(tindex, tindex));
            }
            if (vAxis != null) {
                ranges.add(new Range(zindex, zindex));
            }
            if (hGrid != null) {
                ranges.add(new Range(yindex, yindex));
                ranges.add(new Range(xindex, xindex));
            }
            Array a = variable.read(ranges);
            if (a.getSize() == 1) {
                ret = a.getFloat(0);
            } else {
                throw new InvalidRangeException();
            }
        } catch (InvalidRangeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ret;
    }

    @Override
    public List<Float> evaluate(Extent<Integer> tindexExtent, Extent<Integer> zindexExtent,
            Extent<Integer> yindexExtent, Extent<Integer> xindexExtent) {
        List<Range> ranges = new ArrayList<Range>();
        List<Float> ret = new ArrayList<Float>();
        try {
            if (tAxis != null) {
                ranges.add(new Range(tindexExtent.getLow(), tindexExtent.getHigh()));
            }
            if (vAxis != null) {
                ranges.add(new Range(zindexExtent.getLow(), zindexExtent.getHigh()));
            }
            if (hGrid != null) {
                ranges.add(new Range(yindexExtent.getLow(), yindexExtent.getHigh()));
                ranges.add(new Range(xindexExtent.getLow(), xindexExtent.getHigh()));
            }
            Array a = variable.read(ranges);
            while(a.hasNext()){
                ret.add(a.nextFloat());
            }
        } catch (InvalidRangeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ret;
    }

    @Override
    public List<Float> extractHorizontalLayer(int tindex, int zindex) {
        Extent<Integer> xExtent = Extents.newExtent(hGrid.getGridExtent().getLow(0),hGrid.getGridExtent().getHigh(0));
        Extent<Integer> yExtent = Extents.newExtent(hGrid.getGridExtent().getLow(1),hGrid.getGridExtent().getHigh(1));
        return evaluate(Extents.newExtent(tindex, tindex), Extents.newExtent(zindex, zindex), yExtent, xExtent);
    }

    @Override
    public GridSeriesDomain getDomain() {
        return new GridSeriesDomainImpl(hGrid, vAxis, tAxis);
    }

    @Override
    public List<Float> getValues() {
        try {
            Array arr = variable.read();
            float[] vals = (float[]) arr.copyTo1DJavaArray();
            List<Float> ret = new ArrayList<Float>();
            for(float f : vals){
                ret.add(f);
            }
            return ret;
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public String getDescription() {
        return variable.getDescription();
    }
}
