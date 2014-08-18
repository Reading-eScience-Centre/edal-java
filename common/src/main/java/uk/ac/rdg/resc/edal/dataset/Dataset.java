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

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.joda.time.Chronology;

import uk.ac.rdg.resc.edal.dataset.plugins.VariablePlugin;
import uk.ac.rdg.resc.edal.domain.HorizontalDomain;
import uk.ac.rdg.resc.edal.exceptions.DataReadingException;
import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.feature.DiscreteFeature;
import uk.ac.rdg.resc.edal.feature.Feature;
import uk.ac.rdg.resc.edal.feature.PointSeriesFeature;
import uk.ac.rdg.resc.edal.feature.ProfileFeature;
import uk.ac.rdg.resc.edal.grid.TimeAxis;
import uk.ac.rdg.resc.edal.grid.VerticalAxis;
import uk.ac.rdg.resc.edal.metadata.VariableMetadata;
import uk.ac.rdg.resc.edal.position.VerticalCrs;
import uk.ac.rdg.resc.edal.util.PlottingDomainParams;

/**
 * Provides access to data and metadata held in underlying storage,
 * {@literal e.g.} on disk, in a database or on a remote server.
 * 
 * @author Guy Griffiths
 * @author Jon Blower
 */
public interface Dataset {

    /**
     * @return The ID which identifies this dataset.
     */
    public String getId();

    /**
     * @return the IDs of features which are present in this Dataset
     */
    public Set<String> getFeatureIds();

    /**
     * Determines the type of feature returned by the
     * {@link Dataset#readFeature(String)} method for a particular variable
     * (feature ID)
     * 
     * @param variableId
     *            The ID of the variable
     * @return The class of the {@link Feature}s returned when calling the
     *         {@link Dataset#readFeature} method for the same variable ID
     */
    public Class<? extends DiscreteFeature<?, ?>> getFeatureType(String variableId);

    /**
     * Reads an entire feature from underlying storage
     * 
     * @param featureId
     *            The ID of the feature to read
     * @return The resulting {@link Feature}
     */
    public Feature<?> readFeature(String featureId) throws DataReadingException;

    /**
     * @return the IDs of variables in this {@link Dataset}. Generally the term
     *         "variable" refers to a measured physical quantity
     */
    public Set<String> getVariableIds();

    /**
     * Returns the {@link VariableMetadata} associated with a particular
     * variable ID
     * 
     * @param variableId
     *            The variable ID to search for
     * @return The desired {@link VariableMetadata}
     */
    public VariableMetadata getVariableMetadata(String variableId);

    /**
     * @return the variables at the top level of the hierarchy.
     */
    public Set<VariableMetadata> getTopLevelVariables();

    /**
     * Adds a {@link VariablePlugin} to this dataset to generate derived
     * variables from existing ones in the {@link Dataset}
     * 
     * @param plugin
     *            The {@link VariablePlugin} to add
     * @throws EdalException
     *             If there is a problem adding the plugin
     */
    public void addVariablePlugin(VariablePlugin plugin) throws EdalException;

    /**
     * @return The {@link Chronology} used for any times in this dataset. Can be
     *         <code>null</code> if this dataset contains no features with time
     *         information
     */
    public Chronology getDatasetChronology();

    /**
     * @return The {@link VerticalCrs} used for any vertical positions in this
     *         dataset. Can be <code>null</code> if this dataset contains no
     *         features with vertical information
     */
    public VerticalCrs getDatasetVerticalCrs();

    /**
     * Determines the type of feature returned by the
     * {@link Dataset#extractMapFeatures(Set, PlottingDomainParams)} method for
     * a particular variable
     * 
     * @param variableId
     *            The ID of the variable
     * @return The class of the {@link Feature}s returned when calling the
     *         {@link Dataset#extractMapFeatures} method for the given variable
     *         ID
     */
    public Class<? extends DiscreteFeature<?, ?>> getMapFeatureType(String variableId);

    /**
     * Extracts features to be plotted on a map.
     * 
     * @param varIds
     *            The IDs of the variables to be extracted. If this is
     *            <code>null</code> then all variable IDs will be plotted. Any
     *            non-scalar parent variables will have all of their child
     *            variables extracted.
     * @param params
     *            The {@link PlottingDomainParams} object describing the domain
     *            to be plotted. The exact manner these are interpreted may
     *            depend on the type of {@link DiscreteFeature} returned.
     * @return A {@link Collection} of {@link DiscreteFeature}s which can be
     *         plotted
     * @throws DataReadingException
     *             If there is a problem reading the underlying data
     */
    public List<? extends DiscreteFeature<?, ?>> extractMapFeatures(Set<String> varIds,
            PlottingDomainParams params) throws DataReadingException;

    /**
     * Extracts {@link ProfileFeature}(s) from the {@link Dataset}
     * 
     * @param varIds
     *            The variable IDs to extract. If this is <code>null</code> then
     *            all variable IDs will be plotted. Any non-scalar parent
     *            variables will have all of their child variables extracted.
     * @param params
     *            The {@link PlottingDomainParams} describing the domain to be
     *            extracted:
     * 
     *            <ul>
     *            <li>{@link PlottingDomainParams#getTargetZ()} is ignored.</li>
     * 
     *            <li>If {@link PlottingDomainParams#getZExtent()} is non-
     *            <code>null</code>, all {@link ProfileFeature}s whose domains
     *            have any intersection with this vertical extent will be
     *            extracted. The domains of the extracted {@link ProfileFeature}
     *            s will be the entire available domains, not just the
     *            intersection.</li>
     * 
     *            <li>If {@link PlottingDomainParams#getZExtent()} is
     *            <code>null</code>, no constraint will be placed on the
     *            vertical domain of the {@link ProfileFeature}s to extract.</li>
     * 
     *            <li>If {@link PlottingDomainParams#getTExtent()} is non-
     *            <code>null</code>, all measurements which fall entirely within
     *            the extent given by {@link PlottingDomainParams#getTExtent()}
     *            will be extracted</li>
     * 
     *            <li>If {@link PlottingDomainParams#getTExtent()} is
     *            <code>null</code> only profiles matching the time specified by
     *            {@link PlottingDomainParams#getTargetT()} will be extracted.
     *            In the case of a gridded dataset a time is considered to match
     *            if the method
     *            {@link TimeAxis#contains(org.joda.time.DateTime)} on its time
     *            axis returns <code>true</code> for the target time. For a
     *            non-gridded dataset, the feature time must exactly match the
     *            target time.</li>
     * 
     *            <li>If {@link PlottingDomainParams#getTargetT()} and
     *            {@link PlottingDomainParams#getTExtent()} are both
     *            <code>null</code>, {@link ProfileFeature}s for all available
     *            time values will be extracted.</li>
     * 
     *            <li>If {@link PlottingDomainParams#getBbox()} is non-
     *            <code>null</code>, all measurements falling within
     *            {@link PlottingDomainParams#getBbox()} will be returned.</li>
     * 
     *            <li>If {@link PlottingDomainParams#getBbox()} is
     *            <code>null</code>, only measurements which match the
     *            horizontal position given by
     *            {@link PlottingDomainParams#getTargetHorizontalPosition()}
     *            will be extracted. In the case of a gridded feature, this will
     *            be the profile of the grid cell which the position falls into,
     *            but for a dataset with a continuous {@link HorizontalDomain}
     *            only exact matches will be extracted</li>
     * 
     *            <li>If both
     *            {@link PlottingDomainParams#getTargetHorizontalPosition()} and
     *            {@link PlottingDomainParams#getBbox()} are <code>null</code>
     *            no constraint is placed on horizontal positions - e.g. for a
     *            gridded field a profile will be returned for every horizontal
     *            grid point.</li>
     * 
     *            <li>If a
     *            {@link PlottingDomainParams#getTargetHorizontalPosition()} is
     *            non-<code>null</code>, the resulting {@link Collection} will
     *            be sorted according to distance (in co-ordinate units) from
     *            the target position</li>
     *            </ul>
     * 
     * @return A {@link Collection} of {@link ProfileFeature}s
     * @throws DataReadingException
     *             if there is a problem reading the underlying data
     * @throws UnsupportedOperationException
     *             if not all of the requested variables have a vertical domain
     */
    public List<? extends ProfileFeature> extractProfileFeatures(Set<String> varIds,
            PlottingDomainParams params) throws DataReadingException, UnsupportedOperationException;

    /**
     * @param varId
     *            The ID of the variable to extract
     * @return <code>true</code> if this dataset supports the extraction of the
     *         given variable to {@link ProfileFeature}s via the
     *         {@link Dataset#extractProfileFeatures(Set, PlottingDomainParams)}
     *         method
     */
    public boolean supportsProfileFeatureExtraction(String varId);

    /**
     * Extracts {@link PointSeriesFeature}(s) from the {@link Dataset}
     * 
     * @param varIds
     *            The variable IDs to extract. If this is <code>null</code> then
     *            all variable IDs will be plotted. Any non-scalar parent
     *            variables will have all of their child variables extracted.
     * @param params
     *            The {@link PlottingDomainParams} describing the domain to be
     *            extracted:
     * 
     *            <ul>
     *            <li>{@link PlottingDomainParams#getTargetT()} is ignored.</li>
     * 
     *            <li>If {@link PlottingDomainParams#getTExtent()} is non-
     *            <code>null</code>, all {@link PointSeriesFeature}s whose
     *            domains have any intersection with this vertical extent will
     *            be extracted. The domains of the extracted
     *            {@link PointSeriesFeature}s will be the entire available
     *            domains, not just the intersection.</li>
     * 
     *            <li>If {@link PlottingDomainParams#getTExtent()} is
     *            <code>null</code>, no constraint will be placed on the
     *            temporal domain of the {@link PointSeriesFeature}s to extract</li>
     * 
     *            <li>If {@link PlottingDomainParams#getZExtent()} is non-
     *            <code>null</code> , all measurements which fall entirely
     *            within the extent given by
     *            {@link PlottingDomainParams#getZExtent()} will be extracted</li>
     * 
     *            <li>If {@link PlottingDomainParams#getZExtent()} is
     *            <code>null</code> only profiles matching the depth specified
     *            by {@link PlottingDomainParams#getTargetZ()} will be
     *            extracted. In the case of a gridded dataset a depth is
     *            considered to match if the method
     *            {@link VerticalAxis#contains(Double)} on its vertical axis
     *            returns <code>true</code> for the target depth. For a
     *            non-gridded dataset, the feature depth must exactly match the
     *            target depth.</li>
     * 
     *            <li>
     *            If {@link PlottingDomainParams#getTargetZ()} and
     *            {@link PlottingDomainParams#getZExtent()} are both
     *            <code>null</code>, {@link PointSeriesFeature}s for all
     *            available elevations will be extracted.</li>
     * 
     *            <li>If {@link PlottingDomainParams#getBbox()} is non-
     *            <code>null</code>, all measurements falling within
     *            {@link PlottingDomainParams#getBbox()} will be returned.</li>
     * 
     *            <li>If {@link PlottingDomainParams#getBbox()} is
     *            <code>null</code>, only measurements which match the
     *            horizontal position given by
     *            {@link PlottingDomainParams#getTargetHorizontalPosition()}
     *            will be extracted. In the case of a gridded feature, this will
     *            be the timeseries of the grid cell which the position falls
     *            into, but for a dataset with a continuous
     *            {@link HorizontalDomain} only exact matches will be extracted</li>
     * 
     *            <li>If both
     *            {@link PlottingDomainParams#getTargetHorizontalPosition()} and
     *            {@link PlottingDomainParams#getBbox()} are <code>null</code>
     *            no constraint is placed on horizontal positions - e.g. for a
     *            gridded field a timeseries will be returned for every
     *            horizontal grid point.</li>
     * 
     *            <li>If a
     *            {@link PlottingDomainParams#getTargetHorizontalPosition()} is
     *            non-<code>null</code>, the resulting {@link Collection} will
     *            be sorted according to distance (in co-ordinate units) from
     *            the target position</li>
     *            </ul>
     * 
     * @return A {@link Collection} of {@link PointSeriesFeature}s
     * @throws DataReadingException
     *             if there is a problem reading the underlying data
     * @throws UnsupportedOperationException
     *             if not all of the requested variables have a time domain
     */
    public List<? extends PointSeriesFeature> extractTimeseriesFeatures(Set<String> varIds,
            PlottingDomainParams params) throws DataReadingException, UnsupportedOperationException;

    /**
     * @param varId
     *            The ID of the variable to extract
     * @return <code>true</code> if this dataset supports the extraction of the
     *         given variable to {@link PointSeriesFeature}s via the
     *         {@link Dataset#extractTimeseriesFeatures(Set, PlottingDomainParams)}
     *         method
     */
    public boolean supportsTimeseriesExtraction(String varId);
}
