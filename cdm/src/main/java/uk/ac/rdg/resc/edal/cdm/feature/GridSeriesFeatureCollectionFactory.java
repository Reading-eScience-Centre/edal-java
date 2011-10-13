package uk.ac.rdg.resc.edal.cdm.feature;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import uk.ac.rdg.resc.edal.feature.FeatureCollection;
import uk.ac.rdg.resc.edal.feature.GridSeriesFeature;

public abstract class GridSeriesFeatureCollectionFactory {
    
    public abstract FeatureCollection<GridSeriesFeature<?>> read(String location, String id, String name) throws IOException;

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
