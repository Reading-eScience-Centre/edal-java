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

package uk.ac.rdg.resc.edal.dataset.plugins;

import java.util.Arrays;

import uk.ac.rdg.resc.edal.domain.HorizontalDomain;
import uk.ac.rdg.resc.edal.domain.TemporalDomain;
import uk.ac.rdg.resc.edal.domain.VerticalDomain;
import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.grid.TimeAxis;
import uk.ac.rdg.resc.edal.grid.VerticalAxis;
import uk.ac.rdg.resc.edal.metadata.GridVariableMetadata;
import uk.ac.rdg.resc.edal.metadata.Parameter;
import uk.ac.rdg.resc.edal.metadata.VariableMetadata;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.util.Array1D;
import uk.ac.rdg.resc.edal.util.Array2D;
import uk.ac.rdg.resc.edal.util.GISUtils;

/**
 * This class specifies a way of generating new variables on-the-fly from
 * existing ones.
 * 
 * It works by supplying to the constructor a list of variable IDs which the
 * plugin uses, and a list of suffixes which it provides. The full variable IDs
 * which the plugin provides are obtained by combining the input IDs and adding
 * each of the suffixes.
 * 
 * The plugin must then override the two abstract methods
 * {@link VariablePlugin#doProcessVariableMetadata(VariableMetadata...)} and
 * {@link VariablePlugin#generateValue(String, HorizontalPosition, Number...)}
 * to generate appropriate metadata and values respectively.
 * 
 * For an example of usage, see {@link VectorPlugin}, which groups vector
 * components and generates magnitude and direction variables.
 * 
 * @author Guy Griffiths
 */
public abstract class VariablePlugin {

    protected String[] uses;
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
        if (usesVariables.length == 0) {
            throw new IllegalArgumentException(
                    "A plugin must use at least 1 variable.  This is a practical issue, rather than an ideological one - you are quite free to ignore it when generating values.");
        }
        uses = usesVariables;
        provides = new String[providesSuffixes.length];
        combinedName = combineIds(usesVariables);
        prefixLength = combinedName.length() + 1;
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
     * Convenience method for generating an {@link Array1D} from source
     * 
     * @param varId
     *            The ID of the variable to generate
     * @param positions
     *            An {@link Array1D} of the positions of each value
     * @param sourceArrays
     *            An {@link Array1D} containing the source values
     * @return An {@link Array1D} containing the generated values
     */
    public Array1D<Number> generateArray1D(final String varId,
            final Array1D<HorizontalPosition> positions, final Array1D<Number>... sourceArrays) {
        if (sourceArrays.length != uses.length) {
            throw new IllegalArgumentException("This plugin needs " + uses.length
                    + " data sources, but you have supplied " + sourceArrays.length);
        }
        return new Array1D<Number>(sourceArrays[0].getShape()[0]) {
            @Override
            public void set(Number value, int... coords) {
                throw new IllegalArgumentException("This Array is immutable");
            }

            @Override
            public Number get(int... coords) {
                Number[] sourceValues = new Number[sourceArrays.length];
                for (int i = 0; i < sourceValues.length; i++) {
                    sourceValues[i] = sourceArrays[i].get(coords);
                    if (sourceValues[i] == null) {
                        return null;
                    }
                }
                return generateValue(varId.substring(prefixLength), positions.get(coords),
                        sourceValues);
            }
        };
    }

    /**
     * Convenience method for generating an {@link Array2D} from source
     * 
     * @param varId
     *            The ID of the variable to generate
     * @param positions
     *            An {@link Array2D} of the positions of each value
     * @param sourceArrays
     *            An {@link Array2D} containing the source values
     * @return An {@link Array2D} containing the generated values
     */
    public Array2D<Number> generateArray2D(final String varId,
            final Array2D<HorizontalPosition> positions, final Array2D<Number>... sourceArrays) {
        if (sourceArrays.length != uses.length) {
            throw new IllegalArgumentException("This plugin needs " + uses.length
                    + " data sources, but you have supplied " + sourceArrays.length);
        }
        return new Array2D<Number>(sourceArrays[0].getYSize(), sourceArrays[0].getXSize()) {
            @Override
            public void set(Number value, int... coords) {
                throw new IllegalArgumentException("This Array is immutable");
            }

            @Override
            public Number get(int... coords) {
                Number[] sourceValues = new Number[sourceArrays.length];
                for (int i = 0; i < sourceValues.length; i++) {
                    sourceValues[i] = sourceArrays[i].get(coords);
                    if (sourceValues[i] == null) {
                        return null;
                    }
                }
                return generateValue(varId.substring(prefixLength), positions.get(coords),
                        sourceValues);
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
     * @throws EdalException
     *             If there is a problem processing the metadata
     */
    public VariableMetadata[] processVariableMetadata(VariableMetadata... metadata)
            throws EdalException {
        if (metadataProcessed) {
            throw new IllegalStateException("Metadata has already been processed for this plugin");
        }
        metadataProcessed = true;
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
     * @param pos
     *            The {@link HorizontalPosition} at which the data is being
     *            generated. This may be relevant to how the plugin processes
     *            the values
     * @param values
     *            An array of {@link Number}s representing the source values
     * @return The derived value
     */
    public Number getValue(String varId, HorizontalPosition pos, Number... values) {
        if (!Arrays.asList(provides).contains(varId)) {
            throw new IllegalArgumentException("This plugin does not provide the variable " + varId);
        }
        if (values.length != uses.length) {
            throw new IllegalArgumentException("This plugin needs " + uses.length
                    + " metadata sources, but you have supplied " + values.length);
        }
        return generateValue(varId.substring(prefixLength), pos, values);
    }

    /**
     * Subclasses should override this method to modify the
     * {@link VariableMetadata} tree, and return any new objects added to it.
     * This allows subclasses to arbitrarily restructure the metadata tree by
     * calling the {@link VariableMetadata#setParent(VariableMetadata, String)}
     * methods.
     * 
     * Note that the IDs in the newly-created {@link VariableMetadata} objects
     * should be generated by calling {@link VariablePlugin#getFullId(String)}
     * with the variable's suffix.
     * 
     * If the plugin is designed to group several child variables, the first
     * child will be used to automatically estimate the scale range of the
     * parent.
     * 
     * This is guaranteed to only be called once.
     * 
     * @param metadata
     *            An array of {@link VariableMetadata} of the source variables
     *            in the order they were supplied to the constructor
     * @return The derived {@link VariableMetadata}
     * @throws EdalException
     *             If there is a problem generating new metadata
     */
    protected abstract VariableMetadata[] doProcessVariableMetadata(VariableMetadata... metadata)
            throws EdalException;

    /**
     * Subclasses should override this method to generate values based on source
     * variable values
     * 
     * @param varSuffix
     *            The suffix ID of the variable to generate
     *            {@link VariableMetadata} for. This will be one of the provided
     *            suffixes in the constructor, but not the actual variable ID
     *            (which subclasses do not need to worry about)
     * @param pos
     *            The {@link HorizontalPosition} at which the value is
     *            generated. This may affect the returned value
     * @param sourceValues
     *            An array of {@link Number}s representing the source values in
     *            the order they were supplied to the constructor
     * @return The derived value
     */
    protected abstract Number generateValue(String varSuffix, HorizontalPosition pos,
            Number... sourceValues);

    private String combinedName = null;

    /**
     * Provides a convenience method for mangling several IDs into one new one.
     * This just concatenates them, but subclasses may wish to override this if
     * they require a specific format for IDs.
     * 
     * This is guaranteed to be called once upon construction.
     * 
     * @param partsToUse
     *            The IDs to base this name on
     */
    protected String combineIds(String... partsToUse) {
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

    /**
     * Returns an ID based on the combined ID of all used variables and the
     * suffix of a provided variable.
     * 
     * This should be used by subclasses to generate new
     * {@link VariableMetadata} objects in
     * {@link VariablePlugin#doProcessVariableMetadata(VariableMetadata...)} if
     * required
     * 
     * @param suffix
     *            The suffix used to identify the generated variable.
     * @return The full ID
     */
    protected String getFullId(String suffix) {
        StringBuilder sb = new StringBuilder(combinedName);
        sb.append('-');
        sb.append(suffix);
        return sb.toString();
    }

    /**
     * Generates {@link VariableMetadata} from the given arguments and metadata.
     * This method will find the most appropriate domains to use and will return
     * a {@link GridVariableMetadata} if possible.
     * 
     * This should be used by subclasses when generating
     * {@link VariableMetadata} from several other {@link VariableMetadata}
     * objects.
     * 
     * @param parameter
     *            The {@link Parameter} describing the variable
     * @param scalar
     *            Whether the resulting variable is a scalar (if not, it is a
     *            grouping variable)
     * @param metadata
     *            An array of {@link VariableMetadata} objects for the source
     *            data
     * @return A {@link VariableMetadata} object with the specified parameters,
     *         and domains which encompass the intersection of all of the
     *         supplied domains. If appropriate, this will be a
     *         {@link GridVariableMetadata} object.
     */
    protected VariableMetadata newVariableMetadataFromMetadata(Parameter parameter, boolean scalar,
            VariableMetadata... metadata) {
        HorizontalDomain[] hDomains = new HorizontalDomain[metadata.length];
        VerticalDomain[] vDomains = new VerticalDomain[metadata.length];
        TemporalDomain[] tDomains = new TemporalDomain[metadata.length];
        for (int i = 0; i < metadata.length; i++) {
            hDomains[i] = metadata[i].getHorizontalDomain();
            vDomains[i] = metadata[i].getVerticalDomain();
            tDomains[i] = metadata[i].getTemporalDomain();
        }
        return newVariableMetadataFromDomains(parameter, scalar, hDomains, vDomains, tDomains);
    }

    /**
     * Generates {@link VariableMetadata} from the given arguments and domains.
     * This method will find the most appropriate domains to use and will return
     * a {@link GridVariableMetadata} if possible.
     * 
     * This should be used by subclasses when generating
     * {@link VariableMetadata} from several other {@link VariableMetadata}
     * objects.
     * 
     * @param parameter
     *            The {@link Parameter} describing the variable
     * @param scalar
     *            Whether the resulting variable is a scalar (if not, it is a
     *            grouping variable)
     * @param hDomains
     *            An array of {@link HorizontalDomain}s for the source data
     * @param zDomains
     *            An array of {@link VerticalDomain}s for the source data
     * @param tDomains
     *            An array of {@link TemporalDomain}s for the source data
     * @return A {@link VariableMetadata} object with the specified parameters,
     *         and domains which encompass the intersection of all of the
     *         supplied domains. If appropriate, this will be a
     *         {@link GridVariableMetadata} object.
     */
    protected VariableMetadata newVariableMetadataFromDomains(Parameter parameter, boolean scalar,
            HorizontalDomain[] hDomains, VerticalDomain[] zDomains, TemporalDomain[] tDomains) {
        HorizontalDomain hDomain = GISUtils.getIntersectionOfHorizontalDomains(hDomains);
        VerticalDomain vDomain = GISUtils.getIntersectionOfVerticalDomains(zDomains);
        TemporalDomain tDomain = GISUtils.getIntersectionOfTemporalDomains(tDomains);
        if (hDomain instanceof HorizontalGrid
                && (vDomain instanceof VerticalAxis || vDomain == null)
                && (tDomain instanceof TimeAxis || tDomain == null)) {
            return new GridVariableMetadata(parameter.getId(), parameter, (HorizontalGrid) hDomain,
                    (VerticalAxis) vDomain, (TimeAxis) tDomain, scalar);
        } else {
            return new VariableMetadata(parameter.getId(), parameter, hDomain, vDomain, tDomain,
                    scalar);
        }
    }
}
