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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.channels.FileChannel;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.rdg.resc.edal.dataset.Dataset;
import uk.ac.rdg.resc.edal.graphics.utils.DatasetCatalogue;

/**
 * Deals purely with the (de)serialisation of an XML config file for a
 * {@link DatasetCatalogue}.
 * 
 * @author Guy Griffiths
 */
@XmlType(name = "config", propOrder = { "datasets", "cacheInfo" })
@XmlRootElement(name = "config")
public class CatalogueConfig {
    private static final Logger log = LoggerFactory.getLogger(CatalogueConfig.class);

    /* Included in XML - see setDatasets for details */
    private Map<String, DatasetConfig> datasets = new LinkedHashMap<>();
    @XmlElement(name = "cache")
    private CacheInfo cacheInfo = new CacheInfo();
    @XmlTransient
    private DatasetStorage datasetStorage = null;
    @XmlTransient
    protected File configFile;
    @XmlTransient
    private File configBackup;

    /** The scheduler that will handle the background (re)loading of datasets */
    private static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    /**
     * Contains handles to background threads that can be used to cancel
     * reloading of datasets. Maps dataset IDs to Future objects
     */
    private static Map<String, ScheduledFuture<?>> futures = new HashMap<String, ScheduledFuture<?>>();

    /*
     * Used for JAX-B
     */
    protected CatalogueConfig() {
    }

    /**
     * Creates a NEW config with an empty file
     * 
     * @param configFile
     * @throws IOException
     * @throws JAXBException
     */
    public CatalogueConfig(File configFile) throws IOException, JAXBException {
        datasets = new LinkedHashMap<>();
        this.configFile = configFile;
    }

    public CatalogueConfig(DatasetConfig[] datasets, CacheInfo cacheInfo) {
        super();
        setDatasets(datasets);
        this.cacheInfo = cacheInfo;
    }

    public void setDatasetLoadedHandler(DatasetStorage datasetStorage) {
        this.datasetStorage = datasetStorage;
    }

    public void loadDatasets() {
        /*
         * Loop through all DatasetConfigs and load Datasets from each.
         * 
         * Do this in a manner which means that they are "reloaded" every second
         * (and checked as to whether they need to actually have anything done
         * to them)
         * 
         * Also during the load, return EnhancedVariableMetadata (these are just
         * the VariableConfigs...)
         */
        for (final DatasetConfig dataset : datasets.values()) {
            scheduleReload(dataset);
        }
    }

    private void scheduleReload(final DatasetConfig dataset) {
        if (datasetStorage == null) {
            throw new IllegalStateException(
                    "You need to set something to handle loaded datasets before loading them.");
        }
        Runnable reloader = new Runnable() {
            @Override
            public void run() {
                /*
                 * This will check to see if the metadata need reloading, then
                 * go ahead if so.
                 */
                dataset.refresh(datasetStorage);
            }
        };
        /*
         * Run the task immediately, and then redo it every 1s
         */
        ScheduledFuture<?> future = scheduler.scheduleWithFixedDelay(reloader, 0, 1,
                TimeUnit.SECONDS);
        /* We need to keep a handle to the Future object so we can cancel it */
        futures.put(dataset.getId(), future);
    }

    public CacheInfo getCacheSettings() {
        return cacheInfo;
    }

    public DatasetConfig getDatasetInfo(String datasetId) {
        return datasets.get(datasetId);
    }

    /*
     * By making getDatasets() and setDatasets() both deal with arrays of
     * DatasetConfig, JAXB is able to instantiate them. If we used Collections
     * instead this would not work.
     */
    public DatasetConfig[] getDatasets() {
        return datasets.values().toArray(new DatasetConfig[0]);
    }

    @XmlElementWrapper(name = "datasets")
    @XmlElement(name = "dataset")
    private void setDatasets(DatasetConfig[] datasets) {
        this.datasets = new LinkedHashMap<>();
        for (DatasetConfig dataset : datasets) {
            this.datasets.put(dataset.getId(), dataset);
        }
    }

    public synchronized void addDataset(DatasetConfig dataset) {
        datasets.put(dataset.getId(), dataset);
        scheduleReload(dataset);
    }

    public synchronized void removeDataset(DatasetConfig dataset) {
        datasets.remove(dataset.getId());
        futures.get(dataset.getId()).cancel(true);
        futures.remove(dataset.getId());
    }

    public synchronized void changeDatasetId(DatasetConfig dataset, String newId) {
        datasets.remove(dataset.getId());
        ScheduledFuture<?> removedScheduler = futures.remove(dataset.getId());
        dataset.setId(newId);

        datasets.put(newId, dataset);
        futures.put(dataset.getId(), removedScheduler);
    }

    public synchronized void save() throws IOException {
        if (configFile == null) {
            throw new IllegalStateException("No location set for config file");
        }
        File parentDir = configFile.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }

        /* Take a backup of the existing config file */
        if (configBackup == null) {
            String backupName = configFile.getAbsolutePath() + ".backup";
            configBackup = new File(backupName);
        }
        /* Copy current config file to the backup file. */
        if (configFile.exists()) {
            /* Delete existing backup */
            configBackup.delete();
            copyFile(configFile, configBackup);
        }

        try {
            serialise(new FileWriter(configFile));
        } catch (JAXBException e) {
            throw new IOException("Could not save file due to JAXB error", e);
        }
    }

    public static void shutdown() {
        scheduler.shutdownNow();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Datasets\n");
        sb.append("--------\n");
        for (DatasetConfig dataset : datasets.values()) {
            sb.append(dataset.toString());
            sb.append("\n");
        }
        sb.append("Cache Info\n");
        sb.append("----------\n");
        sb.append(cacheInfo.toString());
        sb.append("\n");
        return sb.toString();
    }

    public void serialise(Writer writer) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(this.getClass());

        Marshaller marshaller = context.createMarshaller();

        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        marshaller.marshal(this, writer);
    }

    public void generateSchema(final String path) throws IOException, JAXBException {
        JAXBContext context = JAXBContext.newInstance(this.getClass());
        context.generateSchema(new SchemaOutputResolver() {
            @Override
            public Result createOutput(String namespaceUri, String suggestedFileName)
                    throws IOException {
                return new StreamResult(new File(path, suggestedFileName));
            }
        });
    }

    public static CatalogueConfig deserialise(Reader xmlConfig) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(CatalogueConfig.class);

        Unmarshaller unmarshaller = context.createUnmarshaller();
        CatalogueConfig config = (CatalogueConfig) unmarshaller.unmarshal(xmlConfig);

        return config;
    }

    public static CatalogueConfig readFromFile(File configFile) throws JAXBException, IOException {
        CatalogueConfig config;
        if (!configFile.exists()) {
            /*
             * If the file doesn't exist, create it with some default values
             */
            log.warn("No config file exists in the given location (" + configFile.getAbsolutePath()
                    + ").  Creating one with defaults");
            config = new CatalogueConfig(new DatasetConfig[0], new CacheInfo());
            config.configFile = configFile;
            config.save();
        } else {
            /*
             * Otherwise read the file
             */
            config = deserialise(new FileReader(configFile));
            config.configFile = configFile;
        }
        return config;
    }

    /** Copies a file */
    private static void copyFile(File sourceFile, File destFile) throws IOException {
        try (FileInputStream sourceIS = new FileInputStream(sourceFile);
                FileChannel source = sourceIS.getChannel();
                FileOutputStream destinationOS = new FileOutputStream(destFile);
                FileChannel destination = destinationOS.getChannel()) {
            destination.transferFrom(source, 0, source.size());
        }
    }

    /**
     * Defines an entry point for what to do once a {@link Dataset} has been
     * loaded (i.e. had all of its metadata read and processed)
     *
     * @author Guy Griffiths
     */
    public interface DatasetStorage {
        /**
         * Called once a {@link Dataset} is ready to be made available
         * 
         * @param dataset
         *            The {@link Dataset} which is ready
         * @param variables
         *            A {@link Collection} of {@link VariableConfig} objects
         *            representing the available variables in the given
         *            {@link Dataset}
         */
        public void datasetLoaded(Dataset dataset, Collection<VariableConfig> variables);
    }
}
