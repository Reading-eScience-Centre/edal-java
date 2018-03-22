/*******************************************************************************
 * Copyright (c) 2018 The University of Reading
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

package uk.ac.rdg.resc.edal.dataset.vtk;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.zip.DataFormatException;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration;
import net.sf.ehcache.config.CacheConfiguration.TransactionalMode;
import net.sf.ehcache.config.PersistenceConfiguration.Strategy;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;
import uk.ac.rdg.resc.edal.cache.EdalCache;
import uk.ac.rdg.resc.edal.dataset.DataSource;
import uk.ac.rdg.resc.edal.dataset.vtk.HydromodelVtkDatasetFactory.TimestepInfo;
import uk.ac.rdg.resc.edal.exceptions.DataReadingException;

public class OnDemandVtkDataSource implements DataSource {
    private static Logger log = LoggerFactory.getLogger(OnDemandVtkDataSource.class);
    private XMLInputFactory xmlif;
    
    public OnDemandVtkDataSource() {
        xmlif = XMLInputFactory.newInstance();
    }
    
    protected Number[] getData1D(TimestepInfo timestepInfo, String variableId) throws FileNotFoundException, IOException {
        /*
         * Get 1D data from cache if it is present
         */
        Number[] data1d;
        DataCacheKey key = new DataCacheKey(timestepInfo.file, variableId);
        if (vtkGridDatasetCache.isKeyInCache(key)) {
            log.debug("Getting timestep data from cache");
            data1d = (Number[]) vtkGridDatasetCache.get(key).getObjectValue();
        } else {
            log.debug("Data not in cache, reading from VTK file: "
                    + timestepInfo.file.getAbsolutePath());
            try (FileInputStream fis = new FileInputStream(timestepInfo.file)) {
                XMLStreamReader xmlr = xmlif.createXMLStreamReader(fis);

                String dataStr = null;
                String dataType = null;
                String dataFormat = null;
                /*
                 * Go through the XML as a stream
                 */
                while (xmlr.hasNext()) {
                    int elType = xmlr.next();
                    if (elType == XMLStreamReader.START_ELEMENT) {
                        /*
                         * We have a start element
                         */
                        String elementType = xmlr.getLocalName();
                        if (elementType.equalsIgnoreCase("DataArray")) {
                            /*
                             * We have a <DataArray>
                             */
                            String varName = null;
                            /*
                             * Fetch the values for Name, type and
                             * format attributes if they are present
                             */
                            for (int i = 0; i < xmlr.getAttributeCount(); i++) {
                                if (xmlr.getAttributeLocalName(i)
                                        .equalsIgnoreCase("Name")) {
                                    varName = xmlr.getAttributeValue(i);
                                } else if (xmlr.getAttributeLocalName(i)
                                        .equalsIgnoreCase("type")) {
                                    dataType = xmlr.getAttributeValue(i);
                                } else if (xmlr.getAttributeLocalName(i)
                                        .equalsIgnoreCase("format")) {
                                    dataFormat = xmlr.getAttributeValue(i);
                                }
                            }

                            if (varName != null && dataType != null && dataFormat != null) {
                                /*
                                 * We have all of the required
                                 * information for a variable.
                                 * 
                                 * Check that it's the one we're after
                                 */
                                if (varName.startsWith(variableId)) {
                                    /*
                                     * We have <DataArray
                                     * Name="VARID...">
                                     */
                                    dataStr = xmlr.getElementText();
                                    break;
                                }
                            }
                        }
                    }
                }
                /*
                 * Finished parsing file
                 */

                if (dataStr != null) {
                    data1d = VtkUtils.parseDataString(dataStr, dataFormat, dataType,
                            timestepInfo.fillValues);
                    vtkGridDatasetCache.put(new Element(key, data1d));
                } else {
                    throw new DataReadingException("No data for variable " + variableId
                            + " found in file: " + timestepInfo.file);
                }

            } catch (XMLStreamException | DataFormatException e) {
                throw new DataReadingException("Problem reading data", e);
            }
        }
        return data1d;
    }
    
    @Override
    public void close() throws DataReadingException {
        /*
         * Not required - the data source opens and closes all file
         * resources when read() is called.
         */
    }
    
    /*
     * Cache management - maximum 50 cached data arrays
     */
    private static final String CACHE_NAME = "vtkDataCache";
    private static final int MAX_HEAP_ENTRIES = 50;
    private static final MemoryStoreEvictionPolicy EVICTION_POLICY = MemoryStoreEvictionPolicy.LFU;
    private static final Strategy PERSISTENCE_STRATEGY = Strategy.NONE;
    private static final TransactionalMode TRANSACTIONAL_MODE = TransactionalMode.OFF;
    private static Cache vtkGridDatasetCache = null;

    static {
        if (EdalCache.cacheManager.cacheExists(CACHE_NAME) == false) {
            /*
             * Configure cache
             */
            log.debug(
                    "Creating vtkDataCache, with maximum " + MAX_HEAP_ENTRIES + " entries");
            CacheConfiguration config = new CacheConfiguration(CACHE_NAME, MAX_HEAP_ENTRIES)
                    .eternal(true).memoryStoreEvictionPolicy(EVICTION_POLICY)
                    .persistence(new PersistenceConfiguration().strategy(PERSISTENCE_STRATEGY))
                    .transactionalMode(TRANSACTIONAL_MODE);
            vtkGridDatasetCache = new Cache(config);
            EdalCache.cacheManager.addCache(vtkGridDatasetCache);
        } else {
            log.debug("Loading existing vtkGridDatasetCache");
            vtkGridDatasetCache = EdalCache.cacheManager.getCache(CACHE_NAME);
        }
    }

    private static class DataCacheKey {
        private File file;
        private String varId;

        public DataCacheKey(File file, String varId) {
            super();
            this.file = file;
            this.varId = varId;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((file == null) ? 0 : file.hashCode());
            result = prime * result + ((varId == null) ? 0 : varId.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            DataCacheKey other = (DataCacheKey) obj;
            if (file == null) {
                if (other.file != null)
                    return false;
            } else if (!file.equals(other.file))
                return false;
            if (varId == null) {
                if (other.varId != null)
                    return false;
            } else if (!varId.equals(other.varId))
                return false;
            return true;
        }
    }

}
