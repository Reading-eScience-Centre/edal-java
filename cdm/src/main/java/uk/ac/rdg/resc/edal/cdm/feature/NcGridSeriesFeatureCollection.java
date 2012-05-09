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
package uk.ac.rdg.resc.edal.cdm.feature;


import uk.ac.rdg.resc.edal.feature.FeatureCollection;
import uk.ac.rdg.resc.edal.feature.GridSeriesFeature;

/**
 * An implementation of {@link FeatureCollection} which contains
 * {@link GridSeriesFeature} objects which hold {@link Float} data.
 * 
 * NEEDS TO BE REWORKED INTO NEW STRUCTURE
 * @author Guy Griffiths
 * 
 */
public class NcGridSeriesFeatureCollection

//extends AbstractFeatureCollection<GridSeriesFeature> implements FeatureCollection<GridSeriesFeature>

{

//    /**
//     * Instantiates a collection of features from one or more NetCDF files.
//     * 
//     * @param collectionId
//     *            The ID of the collection (comparable to the Dataset ID in old
//     *            ncWMS)
//     * @param collectionName
//     *            The name of the collection (comparable to the Dataset name in
//     *            old ncWMS)
//     * @param location
//     *            The location of the NetCDF file(s)
//     * @throws IOException
//     *             If there is a problem reading the file
//     */
//    public NcGridSeriesFeatureCollection(String collectionId, String collectionName, String location)
//            throws IOException {
//        super(collectionId, collectionName);
//
//        class CompoundData{
//            NcGridSeriesCoverage xCoverage;
//            NcGridSeriesCoverage yCoverage;
//            String xVarId;
//            String yVarId;
//            DataReadingStrategy dataReadingStrategy;
//        }
//        Map<String, CompoundData> compoundsCoverageComponents = new HashMap<String, CompoundData>();
//
//        List<File> files = FileUtils.expandGlobExpression(location);
//        if(files.size() == 0){
//            throw new FileNotFoundException("No files found at " + location);
//        }
//        for (File file : files) {
//            String filename = file.getPath();
//            NetcdfDataset ncDataset = CdmUtils.openDataset(filename);
//
//            GridDataset gridDS = CdmUtils.getGridDataset(ncDataset);
//            DataReadingStrategy dataReadingStrategy = CdmUtils.getOptimumDataReadingStrategy(ncDataset);
//
//            for (Gridset gridset : gridDS.getGridsets()) {
//                /*
//                 * Get everything from the GridCoordSystem that is needed for
//                 * making an NcGridSeriesFeature, and keep locally until...
//                 */
//                GridCoordSystem coordSys = gridset.getGeoCoordSystem();
//                HorizontalGrid hGrid = CdmUtils.createHorizontalGrid(coordSys);
//                if(hGrid instanceof LookUpTableGrid){
//                    dataReadingStrategy = DataReadingStrategy.BOUNDING_BOX;
//                }
//                VerticalAxis vAxis = CdmUtils.createVerticalAxis(coordSys);
//                TimeAxis tAxis = null;
//                if (coordSys.hasTimeAxis1D()) {
//                    tAxis = CdmUtils.createTimeAxis(coordSys);
//                }
//
//                List<GridDatatype> grids = gridset.getGrids();
//                for (GridDatatype gridDT : grids) {
//                    /*
//                     * ...here, where we can get each variable and construct the
//                     * NcGridSeriesFeature add add it to the collection
//                     */
//                    VariableDS var = gridDT.getVariable();
//                    String name = CdmUtils.getVariableTitle(var);
//                    String varId = var.getName();
//                    String description = var.getDescription();
//
//                    if (id2Feature.containsKey(varId)) {
//                        ((NcGridSeriesCoverage) id2Feature.get(varId).getCoverage())
//                                .addToCoverage(filename, varId, tAxis);
//                    } else {
//                        NcGridSeriesCoverage coverage = new NcGridSeriesCoverage(filename, varId,
//                                hGrid, vAxis, tAxis, description, var.getUnitsString());
//                        GridSeriesFeature feature = new GridSeriesFeatureImpl(name,
//                                varId, this, coverage, dataReadingStrategy);
//                        id2Feature.put(varId, feature);
//                    }
//                    
//                    /*
//                     * Now deal with elements which may be part of a compound coverage
//                     */
//                    if (name.contains("eastward")) {
//                        String compoundName = name.replaceFirst("eastward_", "");
//                        CompoundData cData;
//                        if (!compoundsCoverageComponents.containsKey(compoundName)) {
//                            cData = new CompoundData();
//                            compoundsCoverageComponents.put(compoundName, cData);
//                        }
//                        cData = compoundsCoverageComponents.get(compoundName);
//                        /*
//                         * By doing this, we will end up with the merged coverage
//                         */
//                        cData.xCoverage = (NcGridSeriesCoverage) id2Feature.get(varId).getCoverage();
//                        cData.xVarId = varId;
//                        cData.dataReadingStrategy = dataReadingStrategy;
//                    } else if (name.contains("northward")) {
//                        String compoundName = name.replaceFirst("northward_", "");
//                        CompoundData cData;
//                        if (!compoundsCoverageComponents.containsKey(compoundName)) {
//                            cData = new CompoundData();
//                            compoundsCoverageComponents.put(compoundName, cData);
//                        }
//                        cData = compoundsCoverageComponents.get(compoundName);
//                        /*
//                         * By doing this, we will end up with the merged coverage
//                         */
//                        cData.yCoverage = (NcGridSeriesCoverage) id2Feature.get(varId).getCoverage();
//                        cData.yVarId = varId;
//                    }
//                }
//            }
//            CdmUtils.closeDataset(ncDataset);
//        }
//        for (String compoundVar : compoundsCoverageComponents.keySet()) {
//            CompoundData cData = compoundsCoverageComponents.get(compoundVar);
//            String id = cData.xVarId+cData.yVarId;
//            if (!id2Feature.containsKey(id)) {
//                try {
//                    GridSeriesCoverage<Vector2D<Float>> coverage = new NcVectorGridSeriesCoverage(
//                            cData.xCoverage, cData.yCoverage);
//                    GridSeriesFeature<Vector2D<Float>> feature = new GridSeriesFeatureImpl<Vector2D<Float>>(
//                            compoundVar, id, this, coverage,
//                            cData.dataReadingStrategy);
//                    id2Feature.put(id, feature);
//                } catch (InstantiationException e) {
//                    /*
//                     * If we get this error, it means that the components do not
//                     * match properly, and can't make a Vector coverage.
//                     */
//                    // TODO log the error
//                    System.out.println("Cannot merge data from different grids");
//                    continue;
//                }
//            } else {
//                System.out.println("Already have a feature with this ID.  THIS IS A PROBLEM");
//                assert false;
//            }
//        }
//    }

}
