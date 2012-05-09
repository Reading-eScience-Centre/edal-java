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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import uk.ac.rdg.resc.edal.feature.FeatureCollection;
import uk.ac.rdg.resc.edal.feature.GridSeriesFeature;

public abstract class GridSeriesFeatureCollectionFactory {
    
    public abstract FeatureCollection<GridSeriesFeature> read(String location, String id, String name) throws IOException;

    /**
     * Maps class names to GridSeriesFeatureCollectionFactory objects.  Only one DataReader object of
     * each class will ever be created.
     */
    private static Map<String, GridSeriesFeatureCollectionFactory> readers = new HashMap<String, GridSeriesFeatureCollectionFactory>();
    
    /**
     * Gets a DataReader of the given class.  Only one instance of each class
     * will be returned, hence subclasses of DataReader must be thread-safe.
     * @param dataReaderClassName The name of the subclass of DataReader
     * @throws Exception if there was an error creating the DataReader
     * @throws ClassCastException if {@code dataReaderClassName} isn't the name
     * of a valid DataReader subclass
     */
    public static GridSeriesFeatureCollectionFactory forName(String factoryClassName)
            throws Exception
    {
        String clazz = DefaultGridSeriesFeatureCollectionFactory.class.getName();
        if (factoryClassName != null && !factoryClassName.trim().equals(""))
        {
            clazz = factoryClassName;
        }
        // TODO make this thread safe.  Can this be done without explicit locking?
        // See Bloch, Effective Java.
        if (!readers.containsKey(clazz))
        {
            // Create the DataReader object
            Object factoryObj = Class.forName(clazz).newInstance();
            // this will throw a ClassCastException if drObj is not a DataReader
            readers.put(clazz, (GridSeriesFeatureCollectionFactory) factoryObj);
        }
        return readers.get(clazz);
    }
}
