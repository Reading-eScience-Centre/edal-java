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

package uk.ac.rdg.resc.edal.dataset;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import uk.ac.rdg.resc.edal.exceptions.EdalException;

/**
 * A factory for {@link Dataset} objects. The intention is that one factory
 * object will be created for each type of data source (e.g. one factory object
 * per file format). These objects can be stateless (hence thread-safe)
 * singletons and shared between datasets.
 * 
 * @author Guy Griffiths
 * @author Jon
 */
public abstract class DatasetFactory {
    /**
     * Maps class names to {@link DatasetFactory} objects. Only one
     * {@link DatasetFactory} object of each class will ever be created.
     */
    private static Map<String, DatasetFactory> readers = new HashMap<String, DatasetFactory>();

    private static String defaultDatasetFactoryName = null;

    public static void setDefaultDatasetFactoryClass(Class<?> clazz) {
        defaultDatasetFactoryName = clazz.getName();
    }

    public synchronized static DatasetFactory forName(String clazz) throws InstantiationException,
            IllegalAccessException, ClassNotFoundException {
        if ((clazz == null || clazz.trim().equals(""))) {
            if (defaultDatasetFactoryName == null) {
                throw new ClassNotFoundException(
                        "No default data reader class defined.  You should set one using DatasetFactory.setDefaultDatasetFactoryClass");
            } else {
                clazz = defaultDatasetFactoryName;
            }
        }
        if (!readers.containsKey(clazz)) {
            /* Create the DatasetFactory object */
            Object dfObj = Class.forName(clazz).newInstance();
            /*
             * This will throw a ClassCastException if dfObj is not a
             * DatasetFactory
             */
            readers.put(clazz, (DatasetFactory) dfObj);
        }
        return readers.get(clazz);

    }

    /**
     * Returns a Dataset object representing the data at the given location.
     * 
     * @param id
     *            The ID to assign to this dataset
     * @param location
     *            The location of the source data: this may be a file, database
     *            connection string or a remote server address.
     * @throws EdalException
     *             If there is a problem creating the dataset
     */
    public abstract Dataset<?> createDataset(String id, String location) throws IOException,
            EdalException;
}
