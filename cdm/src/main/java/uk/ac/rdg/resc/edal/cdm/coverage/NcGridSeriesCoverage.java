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


import uk.ac.rdg.resc.edal.coverage.Coverage;

/**
 * A class representing a {@link Coverage} backed by a NetCDF file
 * NEEDS TO BE REWORKED INTO NEW STRUCTURE
 * @author Guy Griffiths
 * 
 */
public class NcGridSeriesCoverage 
//extends
//        AbstractDiscreteSimpleCoverage<GeoPosition, GridCell4D, Float> implements
//        GridSeriesCoverage<Float> 
{

//    private HorizontalGrid hGrid;
//    private VerticalAxis vAxis;
//    private TimeAxis tAxis;
//    private RangeMetadata metadata;
//
//    private Map<TimePosition, FilenameVarIdTimeIndex> tPosToVariable;
//
//    private List<Float> values = null;
//    private GridSeriesDomain domain;
//
//    public NcGridSeriesCoverage(String filename, String varId, HorizontalGrid hGrid, VerticalAxis vAxis,
//            TimeAxis tAxis, String description, String units) {
//        this.hGrid = hGrid;
//        this.vAxis = vAxis;
//        this.tAxis = tAxis;
//        tPosToVariable = new HashMap<TimePosition, FilenameVarIdTimeIndex>();
//        if (tAxis != null) {
//            int tindex = 0;
//            for (TimePosition t : tAxis.getCoordinateValues()) {
//                tPosToVariable.put(t, new FilenameVarIdTimeIndex(filename, varId, tindex));
//                tindex++;
//            }
//        } else {
//            tPosToVariable.put(null, new FilenameVarIdTimeIndex(filename, varId, -1));
//        }
//        /*
//         * TODO
//         * Is varId OK here, or should we use something else?
//         * It came from var.getName()
//         */
//        metadata = new RangeMetadataImpl(description, Phenomenon.getPhenomenon(
//                varId, PhenomenonVocabulary.CLIMATE_AND_FORECAST), Unit.getUnit(
//                units, UnitVocabulary.UDUNITS), Float.class);
//    }
//
//    /**
//     * Merge a new {@link Variable} into this {@link Coverage}
//     * 
//     * @param variable
//     *            the {@link Variable} containing the data to be merged
//     * @param tAxis
//     *            the {@link TimeAxis} of the new data
//     */
//    public void addToCoverage(String filename, String varId, TimeAxis tAxis) {
//        List<TimePosition> values = this.tAxis.getCoordinateValues();
//        int tindex = 0;
//        if (tAxis == null) {
//            throw new UnsupportedOperationException(
//                    "Cannot merge new time data into a Coverage with no time axis");
//        }
//        for (TimePosition t : tAxis.getCoordinateValues()) {
//            /*
//             * Add the new time to the map
//             */
//            if (!values.contains(t)) {
//                tPosToVariable.put(t, new FilenameVarIdTimeIndex(filename, varId, tindex));
//                values.add(t);
//            }
//            tindex++;
//        }
//        /*
//         * Create the new time axis
//         */
//        String name = tAxis.getName();
//        Collections.sort(values);
//        this.tAxis = new TimeAxisImpl(name, values);
//        // Update the domain
//        domain = new GridSeriesDomainImpl(hGrid, vAxis, this.tAxis);
//    }
//
//    @Override
//    protected RangeMetadata getRangeMetadata() {
//        return metadata;
//    }
//
//    @Override
//    public Float evaluate(int tindex, int zindex, int yindex, int xindex) {
//        TimePosition tPos = null;
//        FilenameVarIdTimeIndex fileVarTimeIndex = null;
//        Variable variable = null;
//        Float returnVal = null;
//        if (tAxis != null) {
//            tPos = tAxis.getCoordinateValue(tindex);
//        }
//        fileVarTimeIndex = tPosToVariable.get(tPos);
//        try {
//            variable = getVariable(fileVarTimeIndex);
//    
//            List<Range> ranges = new ArrayList<Range>();
//            
//            if (tAxis != null) {
//                ranges.add(new Range(fileVarTimeIndex.getTIndex(), fileVarTimeIndex.getTIndex()));
//            }
//            if (vAxis != null) {
//                ranges.add(new Range(zindex, zindex));
//            }
//            if (hGrid != null) {
//                ranges.add(new Range(yindex, yindex));
//                ranges.add(new Range(xindex, xindex));
//            }
//            Array a = variable.read(ranges);
//            if (a.getSize() == 1) {
//                returnVal = a.getFloat(0);
//            } else {
//                throw new InvalidRangeException();
//            }
//        } catch (InvalidRangeException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        return returnVal;
//    }
//
//    @Override
//    public List<Float> evaluate(Extent<Integer> tindexExtent, Extent<Integer> zindexExtent,
//            Extent<Integer> yindexExtent, Extent<Integer> xindexExtent) {
//        List<Range> ranges = new ArrayList<Range>();
//        List<Float> ret = new ArrayList<Float>();
//
//        List<FilenameVarIdTimeIndex> filesToRead = new ArrayList<FilenameVarIdTimeIndex>();
//        List<Range> rangesToRead = new ArrayList<Range>();
//
//        try {
//            if (tAxis != null) {
//                /*
//                 * If we have a time axis, populate the lists with the variables
//                 * and ranges, IN TIME ORDER, which we need to read
//                 */
//                Integer startI = null;
//                Integer endI = null;
//                /*
//                 * Loop through all time indices we want data for
//                 */
//                for (int i = tindexExtent.getLow(); i <= tindexExtent.getHigh(); i++) {
//                    /*
//                     * Get the variable at the current time index
//                     */
//                    TimePosition time = tAxis.getCoordinateValue(i);
//                    FilenameVarIdTimeIndex varOfCurrentI = tPosToVariable.get(time);
//                    if (filesToRead.size() > 0
//                            && filesToRead.get(filesToRead.size() - 1).getFilename().equals(varOfCurrentI.getFilename()) 
//                            && filesToRead.get(filesToRead.size() - 1).getVarId().equals(varOfCurrentI.getVarId())) {
//                        /*
//                         * If we are still scanning through the same variable,
//                         * update the final time index for this variable
//                         */
//                        endI = tPosToVariable.get(time).getTIndex();
//                    } else {
//                        /*
//                         * If we have a new variable, first create the Range
//                         */
//                        if (startI != null) {
//                            if (endI == null)
//                                endI = startI;
//                            rangesToRead.add(new Range(startI, endI));
//                        }
//                        /*
//                         * Then add the new variable to the list, and set the
//                         * start index in that variable
//                         */
//                        filesToRead.add(varOfCurrentI);
//                        startI = tPosToVariable.get(time).getTIndex();
//                    }
//                }
//                if (endI == null)
//                    endI = startI;
//                /*
//                 * Once we have finished, we still need to add the final Range
//                 */
//                rangesToRead.add(new Range(startI, endI));
//            } else {
//                /*
//                 * If we have no time axis, we only have one variable, whose
//                 * axis indices will be equivalent to the entire coverage's axis
//                 * indices
//                 */
//                filesToRead.add(tPosToVariable.get(null));
//                rangesToRead.add(new Range(tindexExtent.getLow(), tindexExtent.getHigh()));
//            }
//
//            /*
//             * Now loop through all of the variables we have to read, setting
//             * the appropriate Ranges and read the results, adding to a
//             * returnable list
//             */
//            for (int i = 0; i < filesToRead.size(); i++) {
//                ranges = new ArrayList<Range>();
//                if (tAxis != null) {
//                    ranges.add(rangesToRead.get(i));
//                }
//                if (vAxis != null) {
//                    try{
//                    ranges.add(new Range(zindexExtent.getLow(), zindexExtent.getHigh()));
//                    }catch(Exception e){
//                        e.printStackTrace();
//                        System.out.println(zindexExtent);
//                    }
//                }
//                if (hGrid != null) {
//                    ranges.add(new Range(yindexExtent.getLow(), yindexExtent.getHigh()));
//                    ranges.add(new Range(xindexExtent.getLow(), xindexExtent.getHigh()));
//                }
//                
//                Variable variable = getVariable(filesToRead.get(i));
//                Array a = variable.read(ranges);
//                while (a.hasNext()) {
//                    ret.add(a.nextFloat());
//                }
//            }
//        } catch (InvalidRangeException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        return ret;
//    }
//
//    /*
//     * We define state variables, such that we keep a Variable object in memory
//     * until a new one is used. This prevents closing and re-opening the dataset
//     * for every call to evaluate (which seems to be extremely slow for e.g.
//     * curvilinear grids).
//     */
//    private String currentFilename = null;
//    private NetcdfDataset nc = null;
//    private Variable currentVariable = null;
//
//    private synchronized Variable getVariable(FilenameVarIdTimeIndex fileVarTimeIndex) throws IOException{
//        Variable variable = null;
//        if(currentFilename != null && currentFilename.equals(fileVarTimeIndex.getFilename())){
//            variable = currentVariable;
//        } else {
//            if(currentFilename != null){
//                CdmUtils.closeDataset(nc);
//            }
//            nc = CdmUtils.openDataset(fileVarTimeIndex.getFilename());
//            variable = CdmUtils.getGridDatatype(nc, fileVarTimeIndex.getVarId()).getVariable();
//            currentVariable = variable;
//            currentFilename = fileVarTimeIndex.getFilename();
//        }
//        return variable;
//    }
//    
//    @Override
//    public GridSeriesDomain getDomain() {
//        if (domain == null)
//            domain = new GridSeriesDomainImpl(hGrid, vAxis, tAxis);
//        return domain;
//    }
//
//    @Override
//    public List<Float> getValues() {
//        if (values == null) {
//            values = new AbstractList<Float>() {
//                @Override
//                public Float get(int index) {
//                    GridCoordinates4D gC = getDomain().getComponentsOf(index);
//                    return evaluate(gC.getTIndex(), gC.getZIndex(), gC.getYIndex(), gC.getXIndex());
//                }
//
//                @Override
//                public int size() {
//                    return (int) getDomain().size();
//                }
//            };
//        }
//        return values;
//        /*
//         * Note: The method below works. It is slow on the first access, and
//         * then fast on subsequent ones. However, it uses a lot of memory, and
//         * is overkill if we only want to extract a few values.
//         * 
//         * The method above is slower, but uses very little memory, and will
//         * take the same amount of time for each individual value extracted
//         */
////        if (values == null) {
////            Extent<Integer> xExtent = null;
////            Extent<Integer> yExtent = null;
////            Extent<Integer> zExtent = null;
////            Extent<Integer> tExtent = null;
////            if(hGrid != null){
////                xExtent = hGrid.getXAxis().getIndexExtent();
////                yExtent = hGrid.getYAxis().getIndexExtent();
////            }
////            if(vAxis != null){
////                zExtent = vAxis.getIndexExtent();
////            }
////            if(tAxis != null){
////                tExtent = tAxis.getIndexExtent();
////            }
////            values = evaluate(tExtent, zExtent, yExtent, xExtent);
////        }
////        return values;
//    }
//    
//
//
//    @Override
//    public String getDescription() {
//        return metadata.getDescription();
//    }
}
