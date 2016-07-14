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

import java.io.File;
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

    /**
     * @param clazz
     *            The default {@link DatasetFactory} class for reading data
     */
    public static void setDefaultDatasetFactoryClass(Class<?> clazz) {
        defaultDatasetFactoryName = clazz.getName();
    }

    protected static File workingDir = null;

    /**
     * @param workingDir
     *            A default working directory which {@link DatasetFactory}
     *            subclasses can use to store data (e.g. to write spatial
     *            indices to disk)
     * 
     */
    public static void setWorkingDirectory(File workingDir) {
        DatasetFactory.workingDir = workingDir;
    }

    /**
     * Gets a {@link DatasetFactory} from the class name
     * 
     * @param clazz
     *            The qualified name of the {@link DatasetFactory} class
     * @return Either an instance of the requested class, or of the default
     *         dataset factory class if <code>null</code> or an empty string is
     *         supplied
     * 
     * @throws InstantiationException
     *             If there is a problem instantiating the class (e.g it is an
     *             abstract class or an interface)
     * @throws IllegalAccessException
     *             If the requested class has no public no-argument constructor
     * @throws ClassNotFoundException
     *             If the class is not found on the classpath, or if the default
     *             has been requested but no default class has been set
     */
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
    public Dataset createDataset(String id, String location) throws IOException, EdalException {
        return createDataset(id, location, false);
    }

    /**
     * Returns a Dataset object representing the data at the given location.
     * 
     * @param id
     *            The ID to assign to this dataset
     * @param location
     *            The location of the source data: this may be a file, database
     *            connection string or a remote server address.
     * @param forceRefresh
     *            Set to <code>true</code> to ensure that any cached information
     *            is not used
     * @throws EdalException
     *             If there is a problem creating the dataset
     */
    public abstract Dataset createDataset(String id, String location, boolean forceRefresh)
            throws IOException, EdalException;
}
