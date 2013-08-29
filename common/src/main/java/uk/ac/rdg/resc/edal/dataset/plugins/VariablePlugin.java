package uk.ac.rdg.resc.edal.dataset.plugins;

import java.util.Arrays;

import uk.ac.rdg.resc.edal.metadata.VariableMetadata;
import uk.ac.rdg.resc.edal.util.Array2D;

/**
 * This class specifies a way of generating new variables on-the-fly from
 * existing ones.
 * 
 * @author Guy
 */
public abstract class VariablePlugin {

    private String[] uses;
    private String[] provides;
    private int prefixLength;

    /**
     * Instantiate a plugin
     * 
     * @param usesVariables
     *            The IDs of the variables used to generate new values
     * @param providesSuffixes
     *            The suffixes of the generated variables. These will not form
     *            the actual variable IDs.
     */
    public VariablePlugin(String[] usesVariables, String[] providesSuffixes) {
        uses = usesVariables;
        provides = new String[providesSuffixes.length];
        combineIds(usesVariables);
        prefixLength = combinedName.length();
        for (int i = 0; i < providesSuffixes.length; i++) {
            provides[i] = getFullId(providesSuffixes[i]);
        }
    }

    /**
     * @return The IDs of the variables which this plugin uses,
     *         <em>in the order it needs them</em>
     */
    public String[] usesVariables() {
        return uses;
    }

    /**
     * @return The IDs of the variables which this plugin provides
     */
    public String[] providesVariables() {
        return provides;
    }

    /**
     * Convenience method for generating an {@link Array2D} from source
     */
    public Array2D generateArray2D(final String varId, final Array2D... sourceArrays) {
        if (sourceArrays.length != uses.length) {
            throw new IllegalArgumentException("This plugin needs " + uses.length
                    + " data sources, but you have supplied " + sourceArrays.length);
        }
        return new Array2D(sourceArrays[0].getYSize(), sourceArrays[0].getXSize()) {
            @Override
            public void set(Number value, int... coords) {
                throw new IllegalArgumentException("This Array is immutable");
            }

            @Override
            public Number get(int... coords) {
                Number[] sourceValues = new Number[sourceArrays.length];
                for (int i = 0; i < sourceValues.length; i++) {
                    sourceValues[i] = sourceArrays[i].get(coords);
                    if(sourceValues[i] == null) {
                        return null;
                    }
                }
                return generateValue(varId.substring(prefixLength), sourceValues);
            }
        };
    }

    private boolean metadataProcessed = false;

    /**
     * Modifies the current {@link VariableMetadata} tree to reflect the changes
     * this plugin implements.
     * 
     * @param metadata
     *            An array of {@link VariableMetadata} of the source variables
     * @return An array of any new {@link VariableMetadata} objects inserted
     *         into the tree
     */
    public VariableMetadata[] processVariableMetadata(VariableMetadata... metadata) {
        if (metadataProcessed) {
            throw new IllegalStateException("Metadata has already been processed for this plugin");
        }
        if (metadata.length != uses.length) {
            throw new IllegalArgumentException("This plugin needs " + uses.length
                    + " metadata sources, but you have supplied " + metadata.length);
        }
        return doProcessVariableMetadata(metadata);
    }

    /**
     * Generates a value for the desired ID
     * 
     * @param varId
     *            The ID of the variable to generate a value for
     * @param values
     *            An array of {@link Number}s representing the source values
     * @return The derived value
     */
    public Number getValue(String varId, Number... values) {
        if (!Arrays.asList(provides).contains(varId)) {
            throw new IllegalArgumentException("This plugin does not provide the variable " + varId);
        }
        if (values.length != uses.length) {
            throw new IllegalArgumentException("This plugin needs " + uses.length
                    + " metadata sources, but you have supplied " + values.length);
        }
        if (values[0] == null || values[1] == null) {
            return null;
        }
        return generateValue(varId.substring(prefixLength), values);
    }

    /**
     * Subclasses should override this method to modify the
     * {@link VariableMetadata} tree, and return any new objects added to it.
     * This allows subclasses to arbitrarily restructure the metadata tree by
     * calling the {@link VariableMetadata#setParent(VariableMetadata)} methods
     * 
     * This is guaranteed to only be called once.
     * 
     * The {@link VariableMetadata} generated must share the domain type of
     * their parents.
     * 
     * TODO remove this restriction somehow
     * 
     * @param metadata
     *            An array of {@link VariableMetadata} of the source variables
     * @return The derived {@link VariableMetadata}
     */
    protected abstract VariableMetadata[] doProcessVariableMetadata(VariableMetadata... metadata);

    /**
     * Subclasses should override this method to generate values based on source
     * variable values
     * 
     * @param varSuffix
     *            The suffix ID of the variable to generate
     *            {@link VariableMetadata} for. This will be one of the provided
     *            suffixes in the constructor, but not the actual variable ID
     *            (which subclasses do not need to worry about)
     * @param values
     *            An array of {@link Number}s representing the source values
     * @return The derived value
     */
    protected abstract Number generateValue(String varSuffix, Number... sourceValues);

    /**
     * Provides a convenience method for mangling several IDs into one new one.
     * 
     * @param partsToUse
     *            The IDs to base this name on
     */
    private String combinedName = null;

    private String combineIds(String... partsToUse) {
        if (combinedName == null) {
            /*
             * Just concatenate them.
             */
            StringBuilder ret = new StringBuilder();
            for (int i = 0; i < partsToUse.length; i++) {
                ret.append(partsToUse[i]);
            }
            combinedName = ret.toString();
        }
        return combinedName;
    }
    
    protected String getFullId(String suffix) {
        return combinedName + suffix;
    }
}