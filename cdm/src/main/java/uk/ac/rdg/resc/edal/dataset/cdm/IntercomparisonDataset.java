/*******************************************************************************
 * Copyright (c) 2014 The University of Reading
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
 ******************************************************************************/

package uk.ac.rdg.resc.edal.dataset.cdm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.Chronology;

import uk.ac.rdg.resc.edal.dataset.Dataset;
import uk.ac.rdg.resc.edal.dataset.DatasetFactory;
import uk.ac.rdg.resc.edal.dataset.plugins.VariablePlugin;
import uk.ac.rdg.resc.edal.domain.HorizontalDomain;
import uk.ac.rdg.resc.edal.domain.TemporalDomain;
import uk.ac.rdg.resc.edal.domain.VerticalDomain;
import uk.ac.rdg.resc.edal.exceptions.DataReadingException;
import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.exceptions.MismatchedCrsException;
import uk.ac.rdg.resc.edal.feature.DiscreteFeature;
import uk.ac.rdg.resc.edal.feature.Feature;
import uk.ac.rdg.resc.edal.feature.GridFeature;
import uk.ac.rdg.resc.edal.feature.PointSeriesFeature;
import uk.ac.rdg.resc.edal.feature.ProfileFeature;
import uk.ac.rdg.resc.edal.metadata.Parameter;
import uk.ac.rdg.resc.edal.metadata.VariableMetadata;
import uk.ac.rdg.resc.edal.position.VerticalCrs;
import uk.ac.rdg.resc.edal.util.CollectionUtils;
import uk.ac.rdg.resc.edal.util.GISUtils;
import uk.ac.rdg.resc.edal.util.PlottingDomainParams;

public class IntercomparisonDataset implements Dataset {

    private String id;
    private Dataset inSituDataset;
    private Dataset gridDataset;

    private Map<String, Boolean> featureIdsInGrid = new HashMap<String, Boolean>();
    private Map<String, VariableMetadata> ownMetadata = new HashMap<String, VariableMetadata>();

    public static void main(String[] args) throws InstantiationException, IllegalAccessException,
            ClassNotFoundException, EdalException, IOException {
        /*
         * TODO Move this to edal-common once done. It's only in this package to
         * aid in testing
         */
        Dataset dataset = (new IntercomparisonDatasetFactory()).createDataset("test", "");
//        IntercomparisonDataset dataset = new IntercomparisonDataset("test",
//                DatasetFactory.forName("uk.ac.rdg.resc.edal.dataset.cdm.En3DatasetFactory"),
//                "/home/guy/Data/EN3/EN3_v2a_Profiles_2011*.nc",
//                DatasetFactory.forName("uk.ac.rdg.resc.edal.dataset.cdm.CdmGridDatasetFactory"),
//                "/home/guy/Data/FOAM_ONE/FOAM_one.ncml");
    }

    private class DatasetAndId {
        private Dataset dataset;
        private String variableId;

        public DatasetAndId(Dataset dataset, String variableId) {
            super();
            this.dataset = dataset;
            this.variableId = variableId;
        }
    }

    private Map<String, DatasetAndId> varId2DatasetAndId = new HashMap<String, IntercomparisonDataset.DatasetAndId>();

    public IntercomparisonDataset(String id, DatasetFactory inSituFactory, String inSituLocation,
            DatasetFactory gridFactory, String gridLocation) throws EdalException {
        this.id = id;
        try {
            inSituDataset = inSituFactory.createDataset(id + "-insitu", inSituLocation);
            gridDataset = gridFactory.createDataset(id + "-grid", gridLocation);
        } catch (IOException e) {
            throw new EdalException("Problem creating intercomparison dataset", e);
        }

        /*
         * Check we don't have duplicate feature IDs and store which IDs are in
         * which dataset
         */
        for (String featureId : inSituDataset.getFeatureIds()) {
            if (!featureIdsInGrid.containsKey(featureId)) {
                featureIdsInGrid.put(featureId, false);
            } else {
                throw new EdalException("Duplicate feature ID (in-situ):" + featureId);
            }
        }
        for (String featureId : gridDataset.getFeatureIds()) {
            if (!featureIdsInGrid.containsKey(featureId)) {
                featureIdsInGrid.put(featureId, true);
            } else {
                throw new EdalException("Duplicate feature ID (gridded):" + featureId);
            }
        }

        if (inSituDataset.getDatasetChronology() != null) {
            if (!inSituDataset.getDatasetChronology().equals(gridDataset.getDatasetChronology())) {
                throw new MismatchedCrsException(
                        "Dataset chronologies must match for intercomparison");
            }
        } else if (gridDataset.getDatasetChronology() != null) {
            throw new MismatchedCrsException("Dataset chronologies must match for intercomparison");
        }

        if (inSituDataset.getDatasetVerticalCrs() != null) {
            if (!inSituDataset.getDatasetVerticalCrs().equals(gridDataset.getDatasetVerticalCrs())) {
                throw new MismatchedCrsException(
                        "Dataset vertical CRSs must match for intercomparison");
            }
        } else if (gridDataset.getDatasetVerticalCrs() != null) {
            throw new MismatchedCrsException("Dataset vertical CRSs must match for intercomparison");
        }
        calculateVariableRelationships();
    }

    private void calculateVariableRelationships() {
        /*
         * Calculate the variable metadata tree here and reshape it according to
         * shared variables...
         */

        /*
         * First loop through all available variables and create new IDs for
         * them all, so they will be guaranteed unique within this dataset
         */
        for (String gridVarId : gridDataset.getVariableIds()) {
            varId2DatasetAndId.put(gridVarId, new DatasetAndId(gridDataset, gridVarId));
        }
        for (String inSituVarId : inSituDataset.getVariableIds()) {
            varId2DatasetAndId.put(inSituVarId, new DatasetAndId(inSituDataset,
                    inSituVarId));
        }

        for (String gridVarId : gridDataset.getVariableIds()) {
            VariableMetadata gridMetadata = gridDataset.getVariableMetadata(gridVarId);
            Parameter gridParameter = gridMetadata.getParameter();
            for (String inSituVarId : inSituDataset.getVariableIds()) {
                VariableMetadata inSituMetadata = inSituDataset.getVariableMetadata(inSituVarId);
                Parameter inSituParameter = inSituMetadata.getParameter();
                /*
                 * TODO Check that the standard names are the same. Then check
                 * the units and add a unit conversion plugin to one of the
                 * datasets
                 */
                if (gridParameter.equals(inSituParameter)) {
                    /*
                     * We have a pair of variables which represent the same
                     * quantity
                     */
                    System.out.println("Variables:" + gridVarId + " and " + inSituVarId
                            + " represent the same quantity");

                    /*
                     * If both have null parents, we can add them to a parent at
                     * the top level of this dataset.
                     * 
                     * Otherwise we may have to do something more complex.
                     */
                    if (gridMetadata.getParent() == null && inSituMetadata.getParent() == null) {
                        Parameter parameter = new Parameter("compare-"
                                + gridParameter.getStandardName(), gridParameter.getTitle(),
                                gridParameter.getDescription(), gridParameter.getUnits(),
                                gridParameter.getStandardName());
                        HorizontalDomain hDomain = GISUtils.getIntersectionOfHorizontalDomains(
                                gridMetadata.getHorizontalDomain(),
                                inSituMetadata.getHorizontalDomain());
                        VerticalDomain zDomain = GISUtils.getIntersectionOfVerticalDomains(
                                gridMetadata.getVerticalDomain(),
                                inSituMetadata.getVerticalDomain());
                        TemporalDomain tDomain = GISUtils.getIntersectionOfTemporalDomains(
                                gridMetadata.getTemporalDomain(),
                                inSituMetadata.getTemporalDomain());
                        System.out.println(zDomain.getExtent());
                        if (tDomain != null) {
                            System.out.println(tDomain.getExtent());
                        }

                        if (hDomain == null) {
                            /*
                             * Handle no overlap
                             */
                        }
                        if (zDomain == null) {
                            /*
                             * Handle no overlap
                             */
                        }
                        if (tDomain == null) {
                            /*
                             * Handle no overlap
                             */
                        }

                        String parentId = "COMP:" + gridMetadata.getParameter().getStandardName();
                        VariableMetadata parentMetadata = new VariableMetadata(parentId, parameter,
                                hDomain, zDomain, tDomain, false);
                        parentMetadata.setDataset(this);
                        varId2DatasetAndId.put(parentId, new DatasetAndId(this, parentId));
                        ownMetadata.put(parentId, parentMetadata);
                        gridMetadata.setParent(parentMetadata, "grid");
                        inSituMetadata.setParent(parentMetadata, "insitu");
                        gridMetadata.setDataset(gridDataset);
                        inSituMetadata.setDataset(inSituDataset);
                    } else {
                        /*
                         * Something more complex
                         */
                    }
                }
            }
        }
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Set<String> getFeatureIds() {
        return featureIdsInGrid.keySet();
    }

    @Override
    public Feature<?> readFeature(String featureId) throws DataReadingException {
        if (!featureIdsInGrid.containsKey(featureId)) {
            throw new DataReadingException("Feature " + featureId
                    + " doesn't exist in this dataset");
        }
        if (featureIdsInGrid.get(featureId)) {
            return gridDataset.readFeature(featureId);
        } else {
            return inSituDataset.readFeature(featureId);
        }
    }

    @Override
    public Set<String> getVariableIds() {
        return varId2DatasetAndId.keySet();
    }

    @Override
    public VariableMetadata getVariableMetadata(String variableId) {
        if (!varId2DatasetAndId.containsKey(variableId)) {
            throw new IllegalArgumentException(
                    "This dataset does not contain the specified variable (" + variableId + ")");
        }
        DatasetAndId datasetAndId = varId2DatasetAndId.get(variableId);
        if(datasetAndId.dataset == this) {
            return ownMetadata.get(datasetAndId.variableId);
        }
        
        return datasetAndId.dataset.getVariableMetadata(datasetAndId.variableId);
    }

    @Override
    public Set<VariableMetadata> getTopLevelVariables() {
        Set<VariableMetadata> ret = new LinkedHashSet<VariableMetadata>();
        for (String metadataId : varId2DatasetAndId.keySet()) {
            VariableMetadata metadata = getVariableMetadata(metadataId);
            if (metadata.getParent() == null) {
                ret.add(metadata);
            }
        }
        return ret;
    }

    @Override
    public void addVariablePlugin(VariablePlugin plugin) throws EdalException {
        inSituDataset.addVariablePlugin(plugin);
        gridDataset.addVariablePlugin(plugin);
        calculateVariableRelationships();
    }

    @Override
    public Chronology getDatasetChronology() {
        /*
         * We have already checked they match so we can use either
         */
        return inSituDataset.getDatasetChronology();
    }

    @Override
    public VerticalCrs getDatasetVerticalCrs() {
        /*
         * We have already checked they match so we can use either
         */
        return inSituDataset.getDatasetVerticalCrs();
    }

    @Override
    public Class<? extends DiscreteFeature<?, ?>> getMapFeatureType(String variableId) {
        DatasetAndId datasetAndId = varId2DatasetAndId.get(variableId);
        if (datasetAndId.dataset == this) {
            System.out.println("Might get a NPE here");
            return GridFeature.class;
        }
        return datasetAndId.dataset.getMapFeatureType(datasetAndId.variableId);
    }

    @Override
    public List<? extends DiscreteFeature<?, ?>> extractMapFeatures(Set<String> varIds,
            PlottingDomainParams params) throws DataReadingException {
        List<DiscreteFeature<?, ?>> ret = new ArrayList<DiscreteFeature<?, ?>>();
        List<String> variableIds = new ArrayList<String>(varIds);
        for(int i=0;i<variableIds.size();i++) {
            String varId = variableIds.get(i);
            DatasetAndId datasetAndId = varId2DatasetAndId.get(varId);
//            if(datasetAndId == null) {
//                System.out.println("Intercomparison variable "+varId+" doesn't exist");
//            } else {
//                System.out.println("reading data from dataset:"+datasetAndId.dataset.getId());
//            }
            if (!getVariableMetadata(varId).isScalar()) {
                /*
                 * Don't read map data for unplottable variables
                 */
                Set<VariableMetadata> children = getVariableMetadata(varId).getChildren();
                for(VariableMetadata childMetadata : children) {
                    if(!variableIds.contains(childMetadata.getId())) {
                        variableIds.add(childMetadata.getId());
                    }
                }
                continue;
            }
            
            if(datasetAndId.dataset == this) {
                System.out.println("extracting a map feature for a parent intercomparison layer...");
                continue;
            }
            /*
             * TODO This can be made more efficient by collecting variables
             * needing to be read from each dataset first
             */
            ret.addAll(datasetAndId.dataset.extractMapFeatures(
                    CollectionUtils.setOf(datasetAndId.variableId), params));
        }
        return ret;
    }

    @Override
    public boolean supportsProfileFeatureExtraction(String varId) {
        DatasetAndId datasetAndId = varId2DatasetAndId.get(varId);
        return datasetAndId.dataset.supportsProfileFeatureExtraction(datasetAndId.variableId);
    }
    
    @Override
    public List<? extends ProfileFeature> extractProfileFeatures(Set<String> varIds,
            PlottingDomainParams params) throws DataReadingException {
        List<ProfileFeature> ret = new ArrayList<ProfileFeature>();
        for (String varId : varIds) {
            DatasetAndId datasetAndId = varId2DatasetAndId.get(varId);
            /*
             * TODO This can be made more efficient by collecting variables
             * needing to be read from each dataset first
             */
            if(datasetAndId.dataset == this) {
                return ret;
            }
            ret.addAll(datasetAndId.dataset.extractProfileFeatures(
                    CollectionUtils.setOf(datasetAndId.variableId), params));
        }
        return ret;
    }
    
    @Override
    public boolean supportsTimeseriesExtraction(String varId) {
        DatasetAndId datasetAndId = varId2DatasetAndId.get(varId);
        return datasetAndId.dataset.supportsTimeseriesExtraction(datasetAndId.variableId);
    }

    @Override
    public List<? extends PointSeriesFeature> extractTimeseriesFeatures(Set<String> varIds,
            PlottingDomainParams params) throws DataReadingException {
        List<PointSeriesFeature> ret = new ArrayList<PointSeriesFeature>();
        for (String varId : varIds) {
            DatasetAndId datasetAndId = varId2DatasetAndId.get(varId);
            /*
             * TODO This can be made more efficient by collecting variables
             * needing to be read from each dataset first
             */
            if(datasetAndId.dataset == this) {
                return ret;
            }
            ret.addAll(datasetAndId.dataset.extractTimeseriesFeatures(
                    CollectionUtils.setOf(datasetAndId.variableId), params));
        }
        return ret;
    }

}
