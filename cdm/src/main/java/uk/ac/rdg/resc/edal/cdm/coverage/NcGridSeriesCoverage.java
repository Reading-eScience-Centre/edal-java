/*******************************************************************************
 * Copyright (c) 2011 The University of Reading
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the University of Reading, nor the names of the
 *    authors or contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package uk.ac.rdg.resc.edal.cdm.coverage;

import java.io.IOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;
import uk.ac.rdg.resc.edal.Extent;
import uk.ac.rdg.resc.edal.Phenomenon;
import uk.ac.rdg.resc.edal.Unit;
import uk.ac.rdg.resc.edal.cdm.FilenameAndTimeIndex;
import uk.ac.rdg.resc.edal.cdm.util.CdmUtils;
import uk.ac.rdg.resc.edal.coverage.Coverage;
import uk.ac.rdg.resc.edal.coverage.GridSeriesCoverage;
import uk.ac.rdg.resc.edal.coverage.Record;
import uk.ac.rdg.resc.edal.coverage.domain.GridSeriesDomain;
import uk.ac.rdg.resc.edal.coverage.domain.impl.GridSeriesDomainImpl;
import uk.ac.rdg.resc.edal.coverage.grid.GridCell4D;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.coverage.grid.TimeAxis;
import uk.ac.rdg.resc.edal.coverage.grid.VerticalAxis;
import uk.ac.rdg.resc.edal.coverage.grid.impl.TimeAxisImpl;
import uk.ac.rdg.resc.edal.coverage.impl.AbstractDiscreteCoverage;
import uk.ac.rdg.resc.edal.coverage.metadata.RangeMetadata;
import uk.ac.rdg.resc.edal.coverage.metadata.ScalarMetadata;
import uk.ac.rdg.resc.edal.coverage.metadata.impl.ScalarMetadataImpl;
import uk.ac.rdg.resc.edal.position.GeoPosition;
import uk.ac.rdg.resc.edal.position.TimePosition;
import uk.ac.rdg.resc.edal.util.AbstractBigList;
import uk.ac.rdg.resc.edal.util.BigList;

/**
 * A class representing a {@link Coverage} backed by a NetCDF file NEEDS TO BE
 * REWORKED INTO NEW STRUCTURE
 * 
 * @author Guy Griffiths
 * 
 */
public class NcGridSeriesCoverage extends AbstractDiscreteCoverage<GeoPosition, GridCell4D>
        implements GridSeriesCoverage {

    private final HorizontalGrid hGrid;
    private final VerticalAxis vAxis;
    private TimeAxis tAxis;

    private GridSeriesDomain domain;
    private final String description;
    private final Map<String, ScalarMetadata> varId2Metadata;
//    private final Set<String> variableIds;
    private final Map<TimePosition, FilenameAndTimeIndex> tPosToVariable;

    /*
     * You've removed final modifiers from some things which (may) want them.
     * Put them back when you've finished messing about.
     */

    /**
     * Instantiates an empty NcGridSeriesCoverage. This will contain a domain
     * and a description, but nothing else/
     * 
     * @param hGrid
     * @param vAxis
     * @param tAxis
     * @param description
     */
    public NcGridSeriesCoverage(String filename, HorizontalGrid hGrid, VerticalAxis vAxis, TimeAxis tAxis,
            String description) {
        this.hGrid = hGrid;
        this.vAxis = vAxis;
        this.tAxis = tAxis;
        this.description = description;

        domain = new GridSeriesDomainImpl(hGrid, vAxis, tAxis);
        varId2Metadata = new HashMap<String, ScalarMetadata>();
//        variableIds = new HashSet<String>();
        
        tPosToVariable = new HashMap<TimePosition, FilenameAndTimeIndex>();
        if (tAxis != null) {
            int tindex = 0;
            for (TimePosition t : tAxis.getCoordinateValues()) {
                tPosToVariable.put(t, new FilenameAndTimeIndex(filename, tindex));
                tindex++;
            }
        } else {
            tPosToVariable.put(null, new FilenameAndTimeIndex(filename, -1));
        }
    }

    /**
     * Adds a variable to the coverage.
     * 
     * @param varId
     *            The name by which the variable will be referred to
     * @param varId
     *            The ID of the variable within the NetCDF file
     * @param tAxis
     *            The time axis corresponding to this variable
     */
    // TODO - add description + other stuff for RangeMetadata
    public void addVariable(String varId, TimeAxis tAxis, String description, Phenomenon parameter,
            Unit units) {
        if (getMemberNames().contains(varId)) {
            throw new IllegalArgumentException(
                    "This coverage already contains a variable with the ID " + varId);
        } else {
            if ((tAxis == null && this.tAxis == null) || tAxis.equals(this.tAxis)) {
//                variableIds.add(varId);
                this.getRangeMetadata() is asking for trouble...
                ScalarMetadataImpl metadata = new ScalarMetadataImpl(this.getRangeMetadata(),
                        varId, description, parameter, units, Float.class);
                varId2Metadata.put(varId, metadata);
            } else {
                throw new IllegalArgumentException(
                        "The added variable does not have the same time axis as this coverage");
            }
        }

        /*
         * OK, let's try again...
         * 
         * addVariable should just make the coverage aware of the variable, by
         * adding it to a set (of member names). It should also take the time
         * axis of the variable and make sure that it matches the current domain
         * time axis. if not, throw an exception
         * 
         * this set is accessed when evaluating etc...
         * 
         * there should be an "addToCoverage" method which adds to the domain,
         * and updates the filename/time index etc.
         * 
         * This must apply to all variables.
         * 
         * therefore, addToCoverage should take a list of variables with it. If
         * this doesn't exactly match the existing set (i.e. the set prior to
         * calling addToCoverage), then throw an exception
         * 
         * Let's give that a go.
         */
    }
    
    public void addToCoverage(String filename, TimeAxis tAxis, Set<String> variables) {
        if (!variables.equals(getMemberNames())) {
            Set<String> v = getMemberNames();
            throw new IllegalArgumentException(
                    "Variable set must be exactly equal to those already present in the coverage");
        }
        /*
         * We do this because this.tAxis.getCoordinateValues() may well return
         * an AbstractList, which we cannot add to.
         */
        List<TimePosition> values = new ArrayList<TimePosition>();
        for (TimePosition tPos : this.tAxis.getCoordinateValues()) {
            values.add(tPos);
        }
        int tindex = 0;
        if (tAxis == null) {
            throw new UnsupportedOperationException(
                    "Cannot merge new time data into a Coverage with no time axis");
        }
        for (TimePosition t : tAxis.getCoordinateValues()) {
            /*
             * Add the new time to the map
             */
            if (!values.contains(t)) {
                tPosToVariable.put(t, new FilenameAndTimeIndex(filename, tindex));
                values.add(t);
            }
            tindex++;
        }
        /*
         * Create the new time axis
         */
        String name = tAxis.getName();
        Collections.sort(values);
        this.tAxis = new TimeAxisImpl(name, values);
        // Update the domain
        domain = new GridSeriesDomainImpl(hGrid, vAxis, this.tAxis);
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Set<String> getMemberNames() {
//        return variableIds;
        return varId2Metadata.keySet();
    }

    @Override
    public ScalarMetadata getRangeMetadata(String memberName) {
        if (!getMemberNames().contains(memberName)) {
            throw new IllegalArgumentException("Cannot get metadata for " + memberName
                    + " - it is not present in this coverage");
        }
        return varId2Metadata.get(memberName);
    }

    @Override
    public RangeMetadata getRangeMetadata() {
        implement this
        // TODO Auto-generated method stub
        return super.getRangeMetadata();
    }
    
    public void addMerger(Merger m) {
        /*
         * These are the components we want to remove from our list
         */
        m.getComponents();

        /*
         * These are the components we now want to add to our list
         */
        m.provides();
    }

    @Override
    public BigList<?> getValues(String memberName) {
        return new AbstractBigList2<Object>() {

            @Override
            public Object get(long index) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public List<Object> getAll(List<Long> indices) {
                // TODO Auto-generated method stub
                return super.getAll(indices);
            }

            @Override
            public List<Object> getAll(long fromIndex, long toIndex) {
                // TODO Auto-generated method stub
                return super.getAll(fromIndex, toIndex);
            }
        };
    }

    @Override
    public GridSeriesDomain getDomain() {
        return domain;
    }

    @Override
    public Record evaluate(final int tindex, final int zindex, final int yindex, final int xindex) {
        final Map<String, Float> evaluatedMembers = new HashMap<String, Float>();

        return new Record() {
            @Override
            public Object getValue(String memberName) {
                if (!evaluatedMembers.containsKey(memberName)) {
                    evaluatedMembers.put(
                            memberName,
                            evaluateSingleMember(memberName, tindex, zindex, yindex, xindex));
                }
                return evaluatedMembers.get(memberName);
            }

            @Override
            public RangeMetadata getRangeMetadata(String memberName) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Set<String> getMemberNames() {
                return NcGridSeriesCoverage.this.getMemberNames();
            }
        };
    }

    @Override
    public List<Record> evaluate(final Extent<Integer> tindexExtent,
            final Extent<Integer> zindexExtent, final Extent<Integer> yindexExtent,
            final Extent<Integer> xindexExtent) {
        /*
         * Yuck. Factor this out?
         */
        return new AbstractList<Record>() {
            /*
             * Calling evaluateSingleMember opens the required file and reads
             * all the necessary data. We only want to do this once, and we only
             * want to do it when the required member of the Record (in this
             * AbstractList) is accessed.
             * 
             * So calling this evaluate method returns an AbstractList of
             * Records.
             * 
             * The first time a member of this Record is required, we go to the
             * file and read all of the data for that member and store it (in
             * evaluatedLists). Subsequently, we just access that.
             * 
             * When another member of the Record is required, we repeat the
             * process.
             */
            private final Map<String, List<Float>> evaluatedLists = new HashMap<String, List<Float>>();

            @Override
            public Record get(final int index) {
                return new Record() {
                    @Override
                    public Object getValue(String memberName) {
                        if (!evaluatedLists.containsKey(memberName)) {
                            evaluatedLists.put(
                                    memberName,
                                    evaluateSingleMember(memberName, tindexExtent, zindexExtent,
                                            yindexExtent, xindexExtent));
                        }
                        return evaluatedLists.get(memberName).get(index);
                    }

                    @Override
                    public RangeMetadata getRangeMetadata(String memberName) {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public Set<String> getMemberNames() {
                        return NcGridSeriesCoverage.this.getMemberNames();
                    }
                };
            }

            @Override
            public int size() {
                return extentSize(xindexExtent) * extentSize(yindexExtent)
                        * extentSize(zindexExtent) * extentSize(tindexExtent);
            }

            private int extentSize(Extent<Integer> extent) {
                return extent.getHigh() - extent.getLow() + 1;
            }
        };
    }


    private Float evaluateSingleMember(String memberName, int tindex, int zindex, int yindex,
            int xindex) {
        if (!getMemberNames().contains(memberName)) {
            throw new IllegalArgumentException("Trying to evaluate " + memberName
                    + ", but this has not been added to the coverage");
        }

        String varId = memberName;
        TimePosition tPos = null;
        FilenameAndTimeIndex fileAndTimeIndex = null;
        Variable variable = null;
        Float returnVal = null;
        if (tAxis != null) {
            tPos = tAxis.getCoordinateValue(tindex);
        }
        fileAndTimeIndex = tPosToVariable.get(tPos);
        try {
            variable = getVariable(fileAndTimeIndex, varId);

            List<Range> ranges = new ArrayList<Range>();

            if (tAxis != null) {
                ranges.add(new Range(fileAndTimeIndex.getTIndex(), fileAndTimeIndex.getTIndex()));
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
                returnVal = a.getFloat(0);
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
        return returnVal;
    }

    private List<Float> evaluateSingleMember(String memberName, Extent<Integer> tindexExtent,
            Extent<Integer> zindexExtent, Extent<Integer> yindexExtent, Extent<Integer> xindexExtent) {
        if (!getMemberNames().contains(memberName)) {
            throw new IllegalArgumentException("Trying to evaluate " + memberName
                    + ", but this has not been added to the coverage");
        }

        String varId = memberName;
        List<Range> ranges = new ArrayList<Range>();
        List<Float> ret = new ArrayList<Float>();

        List<FilenameAndTimeIndex> filesToRead = new ArrayList<FilenameAndTimeIndex>();
        List<Range> rangesToRead = new ArrayList<Range>();
        try {
            if (tAxis != null) {
                /*
                 * If we have a time axis, populate the lists with the variables
                 * and ranges, IN TIME ORDER, which we need to read
                 */
                Integer startI = null;
                Integer endI = null;
                /*
                 * Loop through all time indices we want data for
                 */
                for (int i = tindexExtent.getLow(); i <= tindexExtent.getHigh(); i++) {
                    /*
                     * Get the variable at the current time index
                     */
                    TimePosition time = tAxis.getCoordinateValue(i);
                    FilenameAndTimeIndex varOfCurrentI = tPosToVariable.get(time);
                    if (filesToRead.size() > 0
                            && filesToRead.get(filesToRead.size() - 1).getFilename()
                                    .equals(varOfCurrentI.getFilename())) {
                        /*
                         * If we are still scanning through the same variable,
                         * update the final time index for this variable
                         */
                        endI = tPosToVariable.get(time).getTIndex();
                    } else {
                        /*
                         * If we have a new variable, first create the Range
                         */
                        if (startI != null) {
                            if (endI == null)
                                endI = startI;
                            rangesToRead.add(new Range(startI, endI));
                        }
                        /*
                         * Then add the new variable to the list, and set the
                         * start index in that variable
                         */
                        filesToRead.add(varOfCurrentI);
                        startI = tPosToVariable.get(time).getTIndex();
                    }
                }
                if (endI == null)
                    endI = startI;
                /*
                 * Once we have finished, we still need to add the final Range
                 */
                rangesToRead.add(new Range(startI, endI));
            } else {
                /*
                 * If we have no time axis, we only have one variable, whose
                 * axis indices will be equivalent to the entire coverage's axis
                 * indices
                 */
                filesToRead.add(tPosToVariable.get(null));
                rangesToRead.add(new Range(tindexExtent.getLow(), tindexExtent.getHigh()));
            }

            /*
             * Now loop through all of the variables we have to read, setting
             * the appropriate Ranges and read the results, adding to a
             * returnable list
             */
            for (int i = 0; i < filesToRead.size(); i++) {
                ranges = new ArrayList<Range>();
                if (tAxis != null) {
                    ranges.add(rangesToRead.get(i));
                }
                if (vAxis != null) {
                    try {
                        ranges.add(new Range(zindexExtent.getLow(), zindexExtent.getHigh()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (hGrid != null) {
                    ranges.add(new Range(yindexExtent.getLow(), yindexExtent.getHigh()));
                    ranges.add(new Range(xindexExtent.getLow(), xindexExtent.getHigh()));
                }

                Variable variable = getVariable(filesToRead.get(i), varId);
                Array a = variable.read(ranges);
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

    /*
     * We define state variables, such that we keep a Variable object in memory
     * until a new one is used. This prevents closing and re-opening the dataset
     * for every call to evaluate (which seems to be extremely slow for e.g.
     * curvilinear grids).
     */
    private String currentFilename = null;
    private String currentVarId = null;
    private NetcdfDataset nc = null;
    private Variable currentVariable = null;

    private synchronized Variable getVariable(FilenameAndTimeIndex fileAndTimeIndex, String varId)
            throws IOException {
        Variable variable = null;
        if (currentFilename != null && currentFilename.equals(fileAndTimeIndex.getFilename())
                && currentVarId != null && currentVarId.equals(varId)) {
            variable = currentVariable;
        } else {
            if (currentFilename != null) {
                CdmUtils.closeDataset(nc);
            }
            nc = CdmUtils.openDataset(fileAndTimeIndex.getFilename());
            variable = CdmUtils.getGridDatatype(nc, varId).getVariable();
            currentVariable = variable;
            currentFilename = fileAndTimeIndex.getFilename();
            currentVarId = varId;
        }
        return variable;
    }
}
