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
import uk.ac.rdg.resc.edal.metadata.VariableMetadata;
import uk.ac.rdg.resc.edal.position.VerticalCrs;
import uk.ac.rdg.resc.edal.util.PlottingDomainParams;

/**
 * Provides access to data and metadata held in underlying storage,
 * {@literal e.g.} on disk, in a database or on a remote server.
 * 
 * @author Guy
 * @author Jon
 */
public interface Dataset {

    /**
     * @return The ID which identifies this dataset.
     */
    public String getId();

    /**
     * Returns the IDs of features which are present in this Dataset
     */
    public Set<String> getFeatureIds();

    /**
     * Reads an entire feature from underlying storage
     */
    public Feature<?> readFeature(String featureId) throws DataReadingException;

    /**
     * Returns the IDs of variables in this {@link Dataset}. Generally the term
     * "variable" refers to a measured physical quantity
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
     * Returns the variables at the top level of the hierarchy.
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
     *            <code>null</code> then all variable IDs will be plotted
     * @param params
     *            The {@link PlottingDomainParams} object describing the domain
     *            to be plotted. The exact manner these are interpreted may
     *            depend on the type of {@link DiscreteFeature} returned.
     * @return A {@link Collection} of {@link DiscreteFeature}s which can be
     *         plotted
     * @throws DataReadingException
     *             If there is a problem reading the underlying data
     */
    public Collection<? extends DiscreteFeature<?, ?>> extractMapFeatures(Set<String> varIds,
            PlottingDomainParams params) throws DataReadingException;

    /**
     * Extracts {@link ProfileFeature}(s) from the {@link Dataset}
     * 
     * @param varIds
     *            The variable IDs to extract
     * @param params
     *            The {@link PlottingDomainParams} describing the domain to be
     *            extracted:
     * 
     *            <li>{@link PlottingDomainParams#getTargetZ()} is ignored.
     * 
     *            <li>If {@link PlottingDomainParams#getZExtent()} is non-
     *            <code>null</code>, all of the domains of the
     *            {@link ProfileFeature}s will fully contain the vertical extent
     *            - i.e. the domain may extend one discrete point either side of
     *            the supplied extent, but no more.
     * 
     *            <li>If {@link PlottingDomainParams#getZExtent()} is
     *            <code>null</code>, the profile features will have the maximum
     *            recorded vertical extent.
     * 
     *            <li>If {@link PlottingDomainParams#getTExtent()} is non-
     *            <code>null</code>, all measurements which fall entirely within
     *            the extent given by {@link PlottingDomainParams#getTExtent()}
     *            will be extracted
     * 
     *            <li>If {@link PlottingDomainParams#getTExtent()} is
     *            <code>null</code> only profiles exactly matching the time
     *            specified by {@link PlottingDomainParams#getTargetT()} will be
     *            extracted
     * 
     *            <li>If {@link PlottingDomainParams#getTargetT()} and
     *            {@link PlottingDomainParams#getTExtent()} are both
     *            <code>null</code>, {@link ProfileFeature}s for all available
     *            time values will be extracted.
     * 
     *            <li>If {@link PlottingDomainParams#getBbox()} is non-
     *            <code>null</code>, all measurements falling within
     *            {@link PlottingDomainParams#getBbox()} will be returned.
     * 
     *            <li>If {@link PlottingDomainParams#getBbox()} is
     *            <code>null</code>, only measurements which match the
     *            horizontal position given by
     *            {@link PlottingDomainParams#getTargetHorizontalPosition()}
     *            will be extracted. In the case of a gridded feature, this will
     *            be the profile of the grid cell which the position falls into,
     *            but for a dataset with a continuous {@link HorizontalDomain}
     *            only exact matches will be extracted
     * 
     *            <li>If both
     *            {@link PlottingDomainParams#getTargetHorizontalPosition()} and
     *            {@link PlottingDomainParams#getBbox()} are <code>null</code>
     *            no constraint is placed on horizontal positions - e.g. for a
     *            gridded field a profile will be returned for every horizontal
     *            grid point.
     * 
     *            <li>If a
     *            {@link PlottingDomainParams#getTargetHorizontalPosition()} is
     *            non-<code>null</code>, the resulting {@link Collection} will
     *            be sorted according to distance (in co-ordinate units) from
     *            the target position
     * 
     * @return A {@link Collection} of {@link ProfileFeature}s
     * @throws DataReadingException
     *             if there is a problem reading the underlying data
     */
    public Collection<? extends ProfileFeature> extractProfileFeatures(Set<String> varIds,
            PlottingDomainParams params) throws DataReadingException;

    /**
     * Extracts {@link PointSeriesFeature}(s) from the {@link Dataset}
     * 
     * @param varIds
     *            The variable IDs to extract
     * @param params
     *            The {@link PlottingDomainParams} describing the domain to be
     *            extracted:
     * 
     *            <li>{@link PlottingDomainParams#getTargetT()} is ignored.
     * 
     *            <li>If {@link PlottingDomainParams#getTExtent()} is non-
     *            <code>null</code>, all of the domains of the
     *            {@link PointSeriesFeature}s will fully contain the time extent
     *            - i.e. the domain may extend one discrete point either side of
     *            the supplied extent, but no more.
     * 
     *            <li>If {@link PlottingDomainParams#getTExtent()} is
     *            <code>null</code>, all {@link PointSeriesFeature}s will
     *            contain the full range of times.
     * 
     *            <li>If {@link PlottingDomainParams#getZExtent()} is non-
     *            <code>null</code> , all measurements which fall entirely
     *            within the extent given by
     *            {@link PlottingDomainParams#getZExtent()} will be extracted
     * 
     *            <li>If {@link PlottingDomainParams#getZExtent()} is
     *            <code>null</code> only profiles exactly matching the depth
     *            specified by {@link PlottingDomainParams#getTargetZ()} will be
     *            extracted
     * 
     *            <li>
     *            If {@link PlottingDomainParams#getTargetZ()} and
     *            {@link PlottingDomainParams#getZExtent()} are both
     *            <code>null</code>, {@link PointSeriesFeature}s for all
     *            available elevations will be extracted.
     * 
     *            <li>If {@link PlottingDomainParams#getBbox()} is non-
     *            <code>null</code>, all measurements falling within
     *            {@link PlottingDomainParams#getBbox()} will be returned.
     * 
     *            <li>If {@link PlottingDomainParams#getBbox()} is
     *            <code>null</code>, only measurements which match the
     *            horizontal position given by
     *            {@link PlottingDomainParams#getTargetHorizontalPosition()}
     *            will be extracted. In the case of a gridded feature, this will
     *            be the timeseries of the grid cell which the position falls
     *            into, but for a dataset with a continuous
     *            {@link HorizontalDomain} only exact matches will be extracted
     * 
     *            <li>If both
     *            {@link PlottingDomainParams#getTargetHorizontalPosition()} and
     *            {@link PlottingDomainParams#getBbox()} are <code>null</code>
     *            no constraint is placed on horizontal positions - e.g. for a
     *            gridded field a timeseries will be returned for every
     *            horizontal grid point.
     * 
     *            <li>If a
     *            {@link PlottingDomainParams#getTargetHorizontalPosition()} is
     *            non-<code>null</code>, the resulting {@link Collection} will
     *            be sorted according to distance (in co-ordinate units) from
     *            the target position
     * 
     * @return A {@link Collection} of {@link PointSeriesFeature}s
     * @throws DataReadingException
     *             if there is a problem reading the underlying data
     */
    public Collection<? extends PointSeriesFeature> extractTimeseriesFeatures(Set<String> varIds,
            PlottingDomainParams params) throws DataReadingException;
}
