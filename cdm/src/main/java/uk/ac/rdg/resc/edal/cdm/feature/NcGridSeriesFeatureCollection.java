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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.VariableDS;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDataset;
import ucar.nc2.dt.GridDataset.Gridset;
import ucar.nc2.dt.GridDatatype;
import uk.ac.rdg.resc.edal.Phenomenon;
import uk.ac.rdg.resc.edal.Unit;
import uk.ac.rdg.resc.edal.UnitVocabulary;
import uk.ac.rdg.resc.edal.cdm.coverage.grid.LookUpTableGrid;
import uk.ac.rdg.resc.edal.cdm.coverage.grid.NcGridValuesMatrix4D;
import uk.ac.rdg.resc.edal.cdm.util.CdmUtils;
import uk.ac.rdg.resc.edal.coverage.GridSeriesCoverage;
import uk.ac.rdg.resc.edal.coverage.domain.GridSeriesDomain;
import uk.ac.rdg.resc.edal.coverage.domain.impl.GridSeriesDomainImpl;
import uk.ac.rdg.resc.edal.coverage.grid.GridValuesMatrix;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.coverage.grid.TimeAxis;
import uk.ac.rdg.resc.edal.coverage.grid.VerticalAxis;
import uk.ac.rdg.resc.edal.coverage.impl.DataReadingStrategy;
import uk.ac.rdg.resc.edal.coverage.impl.GridSeriesCoverageImpl;
import uk.ac.rdg.resc.edal.coverage.plugins.VectorPlugin;
import uk.ac.rdg.resc.edal.feature.Feature;
import uk.ac.rdg.resc.edal.feature.FeatureCollection;
import uk.ac.rdg.resc.edal.feature.GridSeriesFeature;
import uk.ac.rdg.resc.edal.feature.impl.AbstractFeatureCollection;
import uk.ac.rdg.resc.edal.feature.impl.GridSeriesFeatureImpl;

/**
 * An implementation of {@link FeatureCollection} which contains
 * {@link GridSeriesFeature} objects which hold {@link Float} data.
 * 
 * @author Guy Griffiths
 * 
 */
public class NcGridSeriesFeatureCollection extends AbstractFeatureCollection<Feature> {

    /**
     * Instantiates a collection of features from one or more NetCDF files.
     * 
     * @param collectionId
     *            The ID of the collection (comparable to the Dataset ID in old
     *            ncWMS)
     * @param collectionName
     *            The name of the collection (comparable to the Dataset name in
     *            old ncWMS)
     * @param location
     *            The location of the NetCDF file(s)
     * @throws IOException
     *             If there is a problem reading the file
     */
    public NcGridSeriesFeatureCollection(String collectionId, String collectionName, String location)
            throws IOException {
        super(collectionId, collectionName);

        List<GridSeriesCoverageImpl> coverages = new ArrayList<GridSeriesCoverageImpl>();

        final class XYVarIDs {
            String xVarId;
            String yVarId;
        }

        DataReadingStrategy dataReadingStrategy = null;
        File file = new File(location);

        String filename = file.getPath();
        NetcdfDataset ncDataset = CdmUtils.openDataset(filename);

        GridDataset gridDS = CdmUtils.getGridDataset(ncDataset);
        dataReadingStrategy = CdmUtils.getOptimumDataReadingStrategy(ncDataset);

        int gridNo = 0;
        for (Gridset gridset : gridDS.getGridsets()) {
            gridNo++;
            /*
             * Get everything from the GridCoordSystem that is needed for making
             * an NcGridSeriesFeature, and keep locally until...
             */
            GridCoordSystem coordSys = gridset.getGeoCoordSystem();
            HorizontalGrid hGrid = CdmUtils.createHorizontalGrid(coordSys);
            if (hGrid instanceof LookUpTableGrid) {
                dataReadingStrategy = DataReadingStrategy.BOUNDING_BOX;
            }
            VerticalAxis vAxis = CdmUtils.createVerticalAxis(coordSys);
            TimeAxis tAxis = null;
            if (coordSys.hasTimeAxis1D()) {
                tAxis = CdmUtils.createTimeAxis(coordSys);
            }

            GridSeriesDomain domain = new GridSeriesDomainImpl(hGrid, vAxis, tAxis);
            // TODO more meaningful description
            GridSeriesCoverageImpl coverage = new GridSeriesCoverageImpl(collectionId + gridNo, domain,
                    dataReadingStrategy);

            Map<String, XYVarIDs> xyComponents = new HashMap<String, XYVarIDs>();

            if (!coverages.contains(coverage)) {
                /*
                 * Coverage doesn't exist.
                 */
                List<GridDatatype> grids = gridset.getGrids();
                /*
                 * Now add all of the variables to the coverage
                 */
                for (GridDatatype gridDT : grids) {
                    VariableDS var = gridDT.getVariable();
                    Phenomenon phenomenon = CdmUtils.getPhenomenon(var);
                    Unit units = Unit.getUnit(gridDT.getUnitsString(), UnitVocabulary.UDUNITS2);
                    String name = phenomenon.getStandardName();
                    String varId = var.getName();
                    String description = var.getDescription();

                    GridValuesMatrix<Float> gridValueMatrix = new NcGridValuesMatrix4D(
                            hGrid.getXAxis(), hGrid.getYAxis(), vAxis, tAxis, filename, varId);
                    coverage.addMember(varId, domain, description, phenomenon, units,
                            gridValueMatrix);

                    /*
                     * Now deal with elements which may be part of a compound
                     * coverage
                     */
                    if (name.contains("eastward")) {
                        String compoundName = name.replaceFirst("eastward_", "");
                        XYVarIDs cData;
                        if (!xyComponents.containsKey(compoundName)) {
                            cData = new XYVarIDs();
                            xyComponents.put(compoundName, cData);
                        }
                        cData = xyComponents.get(compoundName);
                        /*
                         * By doing this, we will end up with the merged
                         * coverage
                         */
                        cData.xVarId = varId;
                    } else if (name.contains("northward")) {
                        String compoundName = name.replaceFirst("northward_", "");
                        XYVarIDs cData;
                        if (!xyComponents.containsKey(compoundName)) {
                            cData = new XYVarIDs();
                            xyComponents.put(compoundName, cData);
                        }
                        cData = xyComponents.get(compoundName);
                        /*
                         * By doing this, we will end up with the merged
                         * coverage
                         */
                        cData.yVarId = varId;
                    }
                }
                coverages.add(coverage);
            } else {
                throw new IllegalArgumentException("We already have this coverage...");
                /*
                 * This was previously where coverages could be extended, but we
                 * no longer allow this.
                 */
            }

            for (String xyVarIDs : xyComponents.keySet()) {
                XYVarIDs xyData = xyComponents.get(xyVarIDs);
                VectorPlugin vectorPlugin = new VectorPlugin(xyData.xVarId, xyData.yVarId,
                        xyVarIDs, "Vector for " + xyVarIDs);
                coverage.addPlugin(vectorPlugin);
            }
        }

        /*
         * We have now processed all of the files into coverages. Now make them
         * into features
         */
        for (GridSeriesCoverage coverage : coverages) {
            // TODO more meaningful name/ID
            GridSeriesFeature feature = new GridSeriesFeatureImpl(collectionName, coverage.getDescription(), this, coverage);
            addFeature(feature);
        }
    }
}
