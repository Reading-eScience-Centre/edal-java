package uk.ac.rdg.resc.edal.cdm.coverage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.nc2.Variable;
import uk.ac.rdg.resc.edal.Extent;
import uk.ac.rdg.resc.edal.Phenomenon;
import uk.ac.rdg.resc.edal.PhenomenonVocabulary;
import uk.ac.rdg.resc.edal.Unit;
import uk.ac.rdg.resc.edal.UnitVocabulary;
import uk.ac.rdg.resc.edal.coverage.GridSeriesCoverage;
import uk.ac.rdg.resc.edal.coverage.RangeMetadata;
import uk.ac.rdg.resc.edal.coverage.RangeMetadataImpl;
import uk.ac.rdg.resc.edal.coverage.domain.GridSeriesDomain;
import uk.ac.rdg.resc.edal.coverage.domain.impl.GridSeriesDomainImpl;
import uk.ac.rdg.resc.edal.coverage.grid.GridCell4D;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.coverage.grid.TimeAxis;
import uk.ac.rdg.resc.edal.coverage.grid.VerticalAxis;
import uk.ac.rdg.resc.edal.coverage.grid.impl.TimeAxisImpl;
import uk.ac.rdg.resc.edal.coverage.impl.AbstractDiscreteSimpleCoverage;
import uk.ac.rdg.resc.edal.position.GeoPosition;
import uk.ac.rdg.resc.edal.position.TimePosition;

public class NcGridSeriesCoverage extends AbstractDiscreteSimpleCoverage<GeoPosition, GridCell4D, Float> implements
        GridSeriesCoverage<Float> {

    private HorizontalGrid hGrid;
    private VerticalAxis vAxis;
    private TimeAxis tAxis;
    private RangeMetadata metadata;
    
    private Map<TimePosition, VariableAndTIndex> tPosToVariable;

    public NcGridSeriesCoverage(Variable variable, HorizontalGrid hGrid, VerticalAxis vAxis, TimeAxis tAxis) {
        this.hGrid = hGrid;
        this.vAxis = vAxis;
        this.tAxis = tAxis;
        tPosToVariable = new HashMap<TimePosition, VariableAndTIndex>();
        if(tAxis != null){
            int tindex = 0;
            for(TimePosition t : tAxis.getCoordinateValues()){
                tPosToVariable.put(t, new VariableAndTIndex(variable, tindex));
                tindex++;
            }
        } else {
            tPosToVariable.put(null, new VariableAndTIndex(variable, -1));
        }
        metadata = new RangeMetadataImpl(variable.getDescription(),
                Phenomenon.getPhenomenon(variable.getName(), PhenomenonVocabulary.CLIMATE_AND_FORECAST),
                Unit.getUnit(variable.getUnitsString(), UnitVocabulary.UDUNITS), Float.class);
    }
    
    public void addToCoverage(Variable variable, TimeAxis tAxis){
        List<TimePosition> values = this.tAxis.getCoordinateValues();
        int tindex = 0;
        for(TimePosition t : tAxis.getCoordinateValues()){
            if(!values.contains(t)){
                tPosToVariable.put(t, new VariableAndTIndex(variable, tindex));
                values.add(t);
            }
            tindex++;
        }
        String name = tAxis.getName();
        Collections.sort(values);
        this.tAxis = new TimeAxisImpl(name, values);
    }

    @Override
    protected RangeMetadata getRangeMetadata() {
        return metadata;
    }

    @Override
    public Float evaluate(int tindex, int zindex, int yindex, int xindex) {
        TimePosition tPos = null;
        VariableAndTIndex variableAndTIndex = null;
        Variable variable = null;
        if(tAxis != null){
            tPos = tAxis.getCoordinateValue(tindex);
        }
        variableAndTIndex = tPosToVariable.get(tPos);
        variable = variableAndTIndex.getVariable();
        
        List<Range> ranges = new ArrayList<Range>();
        Float ret = null;
        try {
            if (tAxis != null) {
                ranges.add(new Range(variableAndTIndex.getTIndex(), variableAndTIndex.getTIndex()));
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
        
        List<Variable> variablesToRead = new ArrayList<Variable>();
        List<Range> rangesToRead = new ArrayList<Range>();
        
        try {
            if (tAxis != null) {
                Integer startI = null;
                Integer endI = null;
                for (int i = tindexExtent.getLow(); i <= tindexExtent.getHigh(); i++) {
                    TimePosition time = tAxis.getCoordinateValue(i);
                    Variable varOfCurrentI = tPosToVariable.get(time).getVariable();
                    if (variablesToRead.size() > 0
                            && variablesToRead.get(variablesToRead.size() - 1) == varOfCurrentI) {
                        endI = tPosToVariable.get(time).getTIndex();
                    } else {
                        if (startI != null) {
                            if (endI == null)
                                endI = startI;
                            rangesToRead.add(new Range(startI, endI));
                        }
                        variablesToRead.add(varOfCurrentI);
                        startI = tPosToVariable.get(time).getTIndex();
                    }
                }
                rangesToRead.add(new Range(startI, endI));
            } else {
                variablesToRead.add(tPosToVariable.get(null).getVariable());
                rangesToRead.add(new Range(tindexExtent.getLow(), tindexExtent.getHigh()));
            }
        
            for (int i = 0; i < variablesToRead.size(); i++) {
                ranges = new ArrayList<Range>();
                if (tAxis != null) {
                    ranges.add(rangesToRead.get(i));
                }
                if (vAxis != null) {
                    ranges.add(new Range(zindexExtent.getLow(), zindexExtent.getHigh()));
                }
                if (hGrid != null) {
                    ranges.add(new Range(yindexExtent.getLow(), yindexExtent.getHigh()));
                    ranges.add(new Range(xindexExtent.getLow(), xindexExtent.getHigh()));
                }
                Array a = variablesToRead.get(i).read(ranges);
                while (a.hasNext()) {
                    ret.add(a.nextFloat());
                }
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
    public GridSeriesDomain getDomain() {
        return new GridSeriesDomainImpl(hGrid, vAxis, tAxis);
    }

    private List<Float> values = null;
    
    @Override
    public List<Float> getValues() {
        if(values == null){
//            try {
//                Array arr = variable.read();
//                float[] vals = (float[]) arr.copyTo1DJavaArray();
                values = new ArrayList<Float>();
//                for(float f : vals){
//                    values.add(f);
//                }
                return values;
//            } catch (IOException e) {
//                return null;
//            }
        } else {
            return values;
        }
    }

    @Override
    public String getDescription() {
        return metadata.getDescription();
    }
    
    private class VariableAndTIndex {
        private final Variable variable;
        private final int tIndex;
        
        public VariableAndTIndex(Variable variable, int tIndex) {
            super();
            this.variable = variable;
            this.tIndex = tIndex;
        }

        public Variable getVariable() {
            return variable;
        }

        public int getTIndex() {
            return tIndex;
        }
    }
}
