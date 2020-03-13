/*******************************************************************************
 * Copyright (c) 2013 The University of Reading
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

package uk.ac.rdg.resc.edal.catalogue.jaxb;

import java.awt.Color;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.rdg.resc.edal.catalogue.jaxb.CatalogueConfig.DatasetStorage;
import uk.ac.rdg.resc.edal.dataset.Dataset;
import uk.ac.rdg.resc.edal.dataset.DatasetFactory;
import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.graphics.utils.ColourPalette;
import uk.ac.rdg.resc.edal.graphics.utils.EnhancedVariableMetadata;
import uk.ac.rdg.resc.edal.graphics.utils.GraphicsUtils;
import uk.ac.rdg.resc.edal.metadata.VariableMetadata;

/**
 * A class representing a dataset in the XML config. This contains all of the
 * information needed to define a {@link Dataset}.
 * 
 * It also contains methods for storing state information about the loading
 * process.
 * 
 * @author Guy Griffiths
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class DatasetConfig {
    private static final Logger log = LoggerFactory.getLogger(DatasetConfig.class);
    /*
     * Attributes which are define as part of the XML configuration of a dataset
     */

    /* Unique ID for this dataset */
    @XmlAttribute(name = "id", required = true)
    private String id;

    /* Title for this dataset */
    @XmlAttribute(name = "title", required = true)
    private String title = "";

    /* Location of this dataset (NcML file, OPeNDAP location etc) */
    @XmlAttribute(name = "location", required = true)
    private String location;

    /* True if we want GetFeatureInfo enabled for this dataset */
    @XmlAttribute(name = "queryable")
    private boolean queryable = true;

    /*
     * True if we want to allow CSV downloads of timeseries/profiles enabled for
     * this dataset
     */
    @XmlAttribute(name = "downloadable")
    private boolean downloadable = false;

    /*
     * We'll use a default data reader unless this is overridden in the config file
     */
    @XmlAttribute(name = "dataReaderClass")
    private String dataReaderClass = "";

    @XmlAttribute(name = "copyrightStatement")
    private String copyrightStatement = "";

    @XmlAttribute(name = "moreInfo")
    private String moreInfo = "";

    /* Set true to disable the dataset without removing it completely */
    @XmlAttribute(name = "disabled")
    private boolean disabled = false;

    /* The update interval in minutes. -1 means "never update automatically" */
    @XmlAttribute(name = "updateInterval")
    private int updateInterval = -1;

    @XmlAttribute(name = "metadataUrl")
    private String metadataUrl = null;

    @XmlAttribute(name = "metadataDesc")
    private String metadataDesc = null;

    @XmlAttribute(name = "metadataMimetype")
    private String metadataMimetype = null;

    /*
     * The VariableConfigs are part of the XML definition, but the annotations are
     * on the setter, so that we can set each one's DatasetConfig to this after
     * deserialisation, and add them to a Map with the IDs as keys
     */
    @XmlTransient
    private Map<String, VariableConfig> variables = new LinkedHashMap<>();

    /*
     * Internal state information related to loading the Dataset which this
     * represents.
     */

    /*
     * State of this dataset.
     */
    @XmlTransient
    private DatasetState state = DatasetState.NEEDS_REFRESH;
    /* Set if there is an error loading the dataset */
    @XmlTransient
    private Throwable err;
    /*
     * The number of consecutive times we've seen an error when loading a dataset
     */
    @XmlTransient
    private int numErrorsInARow = 0;
    /*
     * Used to express progress with loading the metadata for this dataset, one line
     * at a time
     */
    @XmlTransient
    private List<String> loadingProgress = new ArrayList<String>();
    /*
     * The time at which this dataset's stored Layers were last successfully
     * updated, or null if the Layers have not yet been loaded
     */
    @XmlTransient
    private DateTime lastSuccessfulUpdateTime = null;
    /*
     * The time at which we last got an error when updating the dataset's metadata,
     * or null if we've never seen an error
     */
    @XmlTransient
    private DateTime lastFailedUpdateTime = null;

    public DatasetConfig() {
    }

    public DatasetConfig(VariableConfig[] variables) {
        setVariables(variables);
    }

    /**
     * Refreshes the dataset if required.
     * 
     * @param datasetStorage The {@link DatasetStorage} object to send
     *                       {@link Dataset}s and {@link EnhancedVariableMetadata}
     *                       back to once a refresh is completed
     */
    public void refresh(DatasetStorage datasetStorage) {
        if (!needsRefresh()) {
            return;
        }
        loadingProgress = new ArrayList<String>();
        /*
         * Include the id of the dataset in the thread for debugging purposes Comment
         * this out to use the default thread names (e.g. "pool-2-thread-1")
         */
        Thread.currentThread().setName("load-metadata-" + id);

        /* Now load the layers and manage the state of the dataset */
        try {
            /*
             * if lastUpdateTime == null, this dataset has never previously been loaded.
             */
            state = lastSuccessfulUpdateTime == null ? DatasetState.LOADING : DatasetState.UPDATING;

            createDataset(datasetStorage, true);

            /*
             * Update the state of this dataset. If we've got this far there were no errors.
             */
            err = null;
            numErrorsInARow = 0;
            state = DatasetState.READY;
            lastSuccessfulUpdateTime = new DateTime();
        } catch (Throwable e) {
            state = DatasetState.ERROR;
            numErrorsInARow++;
            lastFailedUpdateTime = new DateTime();
            /*
             * Reduce logging volume by only logging the error if it's a new type of
             * exception.
             */
            if (err == null || err.getClass() != e.getClass()) {
                log.error(e.getClass().getName() + " loading metadata for dataset " + id, e);
            }
            err = e;
        }
    }

    public void createDataset(DatasetStorage datasetStorage, boolean forceRefresh)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException, EdalException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        loadingProgress.add("Starting loading");

        /*
         * Get the appropriate DatasetFactory
         */
        DatasetFactory factory = DatasetFactory.forName(dataReaderClass);

        loadingProgress.add("Using dataset factory: " + factory.getClass());

        /*
         * TODO In the old version, we dealt with OPeNDAP credentials here...
         */

        Dataset dataset = factory.createDataset(id, location, forceRefresh);

        loadingProgress.add("Dataset created");
        /*
         * Loop through existing variables and check that they are still there, removing
         * them if not
         */
        Set<String> variableIds = dataset.getVariableIds();
        List<String> variablesToRemove = new ArrayList<String>();
        for (String varId : variables.keySet()) {
            if (!variableIds.contains(varId)) {
                variablesToRemove.add(varId);
            }
        }
        for (String varToRemove : variablesToRemove) {
            variables.remove(varToRemove);
        }
        /*
         * Now create any new variable objects which are needed.
         */
        for (String varId : variableIds) {
            if (!variables.containsKey(varId)) {
                loadingProgress.add("Creating default metadata for variable: " + varId);
                /*
                 * Create a new variable object with default values.
                 */
                Extent<Float> colorScaleRange = GraphicsUtils.estimateValueRange(dataset, varId);
                VariableMetadata variableMetadata = dataset.getVariableMetadata(varId);
                VariableConfig variable = new VariableConfig(varId, variableMetadata.getParameter().getTitle(),
                        variableMetadata.getParameter().getDescription(), colorScaleRange,
                        ColourPalette.DEFAULT_PALETTE_NAME, Color.black, Color.black, new Color(0, true), "linear",
                        ColourPalette.MAX_NUM_COLOURS);
                variable.setParentDataset(this);
                variables.put(varId, variable);
            }
        }

        loadingProgress.add("Making this dataset available through the WMS catalogue");
        datasetStorage.datasetLoaded(dataset, variables.values());

        loadingProgress.add("Finished loading dataset metadata");
    }

    public boolean needsRefresh() {
        if (disabled || state == DatasetState.LOADING || state == DatasetState.UPDATING) {
            return false;
        } else if (state == DatasetState.NEEDS_REFRESH) {
            return true;
        } else if (state == DatasetState.ERROR) {
            /*
             * We implement an exponential backoff for reloading datasets that have errors,
             * which saves repeatedly hammering remote servers
             */
            double delaySeconds = Math.pow(2, numErrorsInARow);
            /* The maximum interval between refreshes is 10 minutes */
            delaySeconds = Math.min(delaySeconds, 10 * 60);
            /* lastFailedUpdateTime should never be null: this is defensive */
            boolean needsRefresh = lastFailedUpdateTime == null ? true
                    : new DateTime().isAfter(lastFailedUpdateTime.plusSeconds((int) delaySeconds));
            return needsRefresh;
        } else if (this.updateInterval < 0) {
            /* We never update this dataset */
            return false;
        } else {
            /*
             * State = READY. Check the age of the metadata Return true if we are after the
             * next scheduled update
             */
            return new DateTime().isAfter(lastSuccessfulUpdateTime.plusMinutes(updateInterval));
        }
    }

    /**
     * Forces this dataset to be refreshed the next time it has an opportunity
     */
    public void forceRefresh() {
        this.err = null;
        this.state = DatasetState.NEEDS_REFRESH;
    }

    /*
     * Bean methods
     */

    /**
     * @return The ID of the {@link Dataset} being configured
     */
    public String getId() {
        return id;
    }

    /**
     * @return The location (on disk/network/internet) of the {@link Dataset} being
     *         configured
     */
    public String getLocation() {
        return location;
    }

    public String getTitle() {
        return title;
    }

    /**
     * @return Whether the {@link Dataset} being configured may have its values
     *         queried
     */
    public boolean isQueryable() {
        return queryable;
    }

    /**
     * @return Whether the {@link Dataset} being configured may have its values
     *         downloaded
     */
    public boolean isDownloadable() {
        return downloadable;
    }

    /**
     * @return Whether the {@link Dataset} being configured is enabled
     */
    public boolean isDisabled() {
        return disabled;
    }

    /**
     * @return How often, in minutes the {@link Dataset} should be updated (i.e.
     *         have metadata re-read in case of changes)
     */
    public int getUpdateInterval() {
        return updateInterval;
    }

    /**
     * @return The class used to convert the location given in
     *         {@link DatasetConfig#getLocation()} to a {@link Dataset}
     */
    public String getDataReaderClass() {
        return dataReaderClass;
    }

    /**
     * @return A copyright statement associated with this {@link Dataset}
     */
    public String getCopyrightStatement() {
        return copyrightStatement;
    }

    /**
     * @return Arbitrary additional information associated with this {@link Dataset}
     */
    public String getMoreInfo() {
        return moreInfo;
    }

    /**
     * @return A URL containing further metadata associated with this
     *         {@link Dataset}
     */
    public String getMetadataUrl() {
        return metadataUrl;
    }

    /**
     * @return A description of further metadata associated with this
     *         {@link Dataset}
     */
    public String getMetadataDesc() {
        return metadataDesc;
    }

    /**
     * @return The mimetype of further metadata associated with this {@link Dataset}
     */
    public String getMetadataMimetype() {
        return metadataMimetype;
    }

    /*
     * By making getVariables() and setVariables() both deal with arrays of
     * VariableConfig, JAXB is able to instantiate them. If we used Collections
     * instead this would not work.
     */

    /**
     * @return An array of {@link VariableConfig} objects representing the available
     *         variables in the represented {@link Dataset}
     */
    public VariableConfig[] getVariables() {
        return variables.values().toArray(new VariableConfig[0]);
    }

    /**
     * @return The {@link VariableConfig} objects representing the variables with
     *         the given ID in the represented {@link Dataset}, or <code>null</code>
     *         if the specified variable ID doesn't exist
     */
    public VariableConfig getVariableById(String variableId) {
        return variables.get(variableId);
    }

    /**
     * @return The {@link DatasetState} which the {@link Dataset} being configured
     *         is in
     */
    public DatasetState getState() {
        return state;
    }

    /**
     * @return A {@link List} of {@link String}s representing the completed stages
     *         of the dataset loading
     */
    public List<String> getLoadingProgress() {
        return loadingProgress;
    }

    /**
     * @return <code>true</code> if this dataset is ready for use
     */
    public synchronized boolean isReady() {
        return !isDisabled() && (state == DatasetState.READY || state == DatasetState.UPDATING);
    }

    /**
     * @return <code>true</code> if this dataset is not ready because it is being
     *         loaded
     */
    public synchronized boolean isLoading() {
        return !isDisabled() && (state == DatasetState.NEEDS_REFRESH || state == DatasetState.LOADING);
    }

    /**
     * @return If there has been an error whilst loaded the associated
     *         {@link Dataset}
     */
    public boolean hasError() {
        /*
         * Note that we don't use state == ERROR here because it's possible for a
         * dataset to be loading and have an error from a previous loading attempt that
         * an admin might want to see
         */
        return err != null;
    }

    /**
     * @return Any {@link Exception} which was thrown during the loading of the
     *         associated {@link Dataset}
     */
    public Throwable getException() {
        return err;
    }

    @XmlElementWrapper(name = "variables")
    @XmlElement(name = "variable")
    private void setVariables(VariableConfig[] variables) {
        this.variables = new HashMap<>();
        for (VariableConfig variable : variables) {
            variable.setParentDataset(this);
            this.variables.put(variable.getId(), variable);
        }
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setQueryable(boolean queryable) {
        this.queryable = queryable;
    }

    public void setDownloadable(boolean downloadable) {
        this.downloadable = downloadable;
    }

    public void setDataReaderClass(String dataReaderClass) {
        this.dataReaderClass = dataReaderClass;
    }

    public void setCopyrightStatement(String copyrightStatement) {
        if (!"".equals(copyrightStatement)) {
            this.copyrightStatement = copyrightStatement;
        }
    }

    public void setMoreInfo(String moreInfo) {
        if (!"".equals(moreInfo)) {
            this.moreInfo = moreInfo;
        }
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public void setUpdateInterval(int updateInterval) {
        this.updateInterval = updateInterval;
    }

    public void setMetadataUrl(String metadataUrl) {
        this.metadataUrl = metadataUrl;
    }

    public void setMetadataDesc(String metadataDesc) {
        this.metadataDesc = metadataDesc;
    }

    public void setMetadataMimetype(String metadataMimetype) {
        this.metadataMimetype = metadataMimetype;
    }

    public void setState(DatasetState state) {
        this.state = state;
    }

    public void setErr(Exception err) {
        this.err = err;
    }

    public void setNumErrorsInARow(int numErrorsInARow) {
        this.numErrorsInARow = numErrorsInARow;
    }

    public void setLoadingProgress(List<String> loadingProgress) {
        this.loadingProgress = loadingProgress;
    }

    public DateTime getLastUpdateTime() {
        return lastSuccessfulUpdateTime;
    }

    public void setLastSuccessfulUpdateTime(DateTime lastSuccessfulUpdateTime) {
        this.lastSuccessfulUpdateTime = lastSuccessfulUpdateTime;
    }

    public void setLastFailedUpdateTime(DateTime lastFailedUpdateTime) {
        this.lastFailedUpdateTime = lastFailedUpdateTime;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Dataset: ");
        sb.append(id);
        sb.append("\nVariables: ");
        for (VariableConfig var : variables.values()) {
            sb.append(var.getId());
            sb.append(", ");
        }
        return sb.substring(0, sb.length() - 2) + "\n";

    }

    /**
     * The state of a Dataset.
     */
    public static enum DatasetState {
        /** Dataset is new or has changed and needs to be loaded */
        NEEDS_REFRESH,

        /** In the process of loading */
        LOADING,

        /** Ready for use */
        READY,

        /** Dataset is ready but is internally sychronizing its metadata */
        UPDATING,

        /** An error occurred when loading the dataset. */
        ERROR;
    }
}
