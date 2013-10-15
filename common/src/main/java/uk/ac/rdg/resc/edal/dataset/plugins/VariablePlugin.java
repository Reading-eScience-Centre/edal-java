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

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.domain.HorizontalDomain;
import uk.ac.rdg.resc.edal.domain.SimpleHorizontalDomain;
import uk.ac.rdg.resc.edal.domain.SimpleTemporalDomain;
import uk.ac.rdg.resc.edal.domain.SimpleVerticalDomain;
import uk.ac.rdg.resc.edal.domain.TemporalDomain;
import uk.ac.rdg.resc.edal.domain.VerticalDomain;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.metadata.VariableMetadata;
import uk.ac.rdg.resc.edal.position.VerticalCrs;
import uk.ac.rdg.resc.edal.util.Array1D;
import uk.ac.rdg.resc.edal.util.Array2D;

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
 * {@link VariablePlugin#generateValue(String, Number...)} to generate
 * appropriate metadata and values respectively.
 * 
 * For an example of usage, see {@link VectorPlugin}, which groups vector
 * components and generates magnitude and direction variables.
 * 
 * @author Guy Griffiths
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
     */
    public Array1D generateArray1D(final String varId, final Array1D... sourceArrays) {
        if (sourceArrays.length != uses.length) {
            throw new IllegalArgumentException("This plugin needs " + uses.length
                    + " data sources, but you have supplied " + sourceArrays.length);
        }
        return new Array1D(sourceArrays[0].getShape().length) {
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
                return generateValue(varId.substring(prefixLength), sourceValues);
            }
        };
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
                    if (sourceValues[i] == null) {
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
     * calling the {@link VariableMetadata#setParent(VariableMetadata)} methods.
     * 
     * Note that the IDs in the newly-created {@link VariableMetadata} objects
     * should be generated by calling {@link VariablePlugin#getFullId(String)}
     * with the variable's suffix.
     * 
     * This is guaranteed to only be called once.
     * 
     * @param metadata
     *            An array of {@link VariableMetadata} of the source variables
     *            in the order they were supplied to the constructor
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
     *            An array of {@link Number}s representing the source values in
     *            the order they were supplied to the constructor
     * @return The derived value
     */
    protected abstract Number generateValue(String varSuffix, Number... sourceValues);

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
     * Gets the union of a number of {@link HorizontalDomain}s
     * 
     * @param domains
     *            The {@link HorizontalDomain}s to find a union of
     * @return A new {@link HorizontalDomain} whose {@link BoundingBox}
     *         represents the area where valid values can be found in all the
     *         supplied {@link HorizontalDomain}s. The
     *         {@link CoordinateReferenceSystem} of the returned
     *         {@link HorizontalDomain} will be WGS84
     */
    protected HorizontalDomain getUnionOfHorizontalDomains(HorizontalDomain... domains) {
        if (domains.length == 0) {
            throw new IllegalArgumentException("Must provide multiple domains to get a union");
        }
        double minLat = Double.MAX_VALUE;
        double maxLat = -Double.MAX_VALUE;
        double minLon = Double.MAX_VALUE;
        double maxLon = -Double.MAX_VALUE;
        for (HorizontalDomain domain : domains) {
            /*
             * If one of the domains is null, their union is null
             */
            if(domain == null) {
                return null;
            }
            GeographicBoundingBox gbbox = domain.getGeographicBoundingBox();
            if (gbbox.getEastBoundLongitude() > maxLon) {
                maxLon = gbbox.getEastBoundLongitude();
            }
            if (gbbox.getWestBoundLongitude() < minLon) {
                minLon = gbbox.getWestBoundLongitude();
            }
            if (gbbox.getNorthBoundLatitude() > maxLat) {
                maxLat = gbbox.getNorthBoundLatitude();
            }
            if (gbbox.getSouthBoundLatitude() < minLat) {
                minLat = gbbox.getSouthBoundLatitude();
            }
        }
        return new SimpleHorizontalDomain(minLon, minLat, maxLon, maxLat);
    }

    /**
     * Gets the union of a number of {@link VerticalDomain}s
     * 
     * @param domains
     *            The {@link VerticalDomain}s to find a union of. They must all
     *            share the same {@link VerticalCrs}
     * @return A new {@link VerticalDomain} whose extent represents the range
     *         where valid values can be found in all the supplied
     *         {@link VerticalDomain}s
     */
    protected VerticalDomain getUnionOfVerticalDomains(VerticalDomain... domains) {
        if (domains.length == 0) {
            throw new IllegalArgumentException("Must provide multiple domains to get a union");
        }
        if(domains[0] == null) {
            return null;
        }
        VerticalCrs verticalCrs = domains[0].getVerticalCrs();
        Double min = -Double.MAX_VALUE;
        Double max = Double.MAX_VALUE;
        for (VerticalDomain domain : domains) {
            /*
             * If one of the domains is null, their union is null
             */
            if(domain == null) {
                return null;
            }
            if ((domain.getVerticalCrs() == null && verticalCrs != null)
                    || !domain.getVerticalCrs().equals(verticalCrs)) {
                throw new IllegalArgumentException(
                        "Vertical domain CRSs must match to calculate their union");
            }
            if (domain.getExtent().getLow() > min) {
                min = domain.getExtent().getLow();
            }
            if (domain.getExtent().getHigh() < max) {
                max = domain.getExtent().getHigh();
            }
        }
        return new SimpleVerticalDomain(min, max, verticalCrs);
    }

    /**
     * Gets the union of a number of {@link TemporalDomain}s
     * 
     * @param domains
     *            The {@link TemporalDomain}s to find a union of
     * @return A new {@link TemporalDomain} whose extent represents the range
     *         where valid values can be found in all the supplied
     *         {@link TemporalDomain}s
     */
    protected TemporalDomain getUnionOfTemporalDomains(TemporalDomain... domains) {
        if (domains.length == 0) {
            throw new IllegalArgumentException("Must provide multiple domains to get a union");
        }
        if(domains[0] == null) {
            return null;
        }
        Chronology chronology = domains[0].getChronology();
        DateTime min = new DateTime(0L, chronology);
        DateTime max = new DateTime(Long.MAX_VALUE, chronology);
        for (TemporalDomain domain : domains) {
            /*
             * If one of the domains is null, their union is null
             */
            if(domain == null) {
                return null;
            }
            if (domain.getExtent().getLow().isAfter(min)) {
                min = domain.getExtent().getLow();
            }
            if (domain.getExtent().getHigh().isBefore(max)) {
                max = domain.getExtent().getHigh();
            }
        }
        return new SimpleTemporalDomain(min, max, chronology);
    }
}
