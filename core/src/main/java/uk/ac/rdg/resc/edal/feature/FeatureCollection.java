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

package uk.ac.rdg.resc.edal.feature;

import java.util.Collection;
import java.util.Set;

import uk.ac.rdg.resc.edal.Extent;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.position.TimePosition;
import uk.ac.rdg.resc.edal.position.VerticalPosition;

/**
 * FeatureCollections contain a single feature type.
 * 
 * @todo Access control?
 * @todo Dictionaries of phenomena and units?
 * @todo Define a subclass that allows features in the collection to be found
 *       using spatio-temporal searches.
 * @author Jon Blower
 */
public interface FeatureCollection<F extends Feature> extends Iterable<F> {

    /**
     * Identifier for this collection. Unique within its context (e.g. within a
     * catalogue of feature collections), but not necessarily globally unique.
     */
    public String getId();

    /**
     * Human-readable name for this collection (not necessarily unique)
     */
    public String getName();

    /**
     * Returns the set of Feature identifiers within this collection
     * 
     * @return
     */
    public Set<String> getFeatureIds();

    /**
     * Gets the feature with the given ID.
     * 
     * @param id
     *            The ID of the feature within this collection.
     * @return
     * @throws NullPointerException
     *             if {@code id == null}
     * @throws IllegalArgumentException
     *             if {@code id} is not a valid feature id
     */
    public F getFeatureById(String id);

    /**
     * Gets all features in the {@link FeatureCollection}.
     * 
     * @return A {@link Collection} of features
     */
    public Collection<F> getFeatures();
    
    /**
     * Gets the {@link Set} of all member names present in {@link Feature}s in
     * this collection
     * 
     * @return
     */
    public Set<String> getMemberIdsInCollection();

    /**
     * Gets the runtime class of the features within this collection.
     * 
     * @return
     */
    public Class<F> getFeatureType();
    
    /**
     * Returns all features meeting the specified criteria. If any criteria are
     * <code>null</code>, then all possible values for that variable will be
     * considered.
     * 
     * @param boundingBox
     *            The containing bounding box.
     * @param zRange
     *            The containing vertical extent
     * @param tRange
     *            The containing time range
     * @param memberNames
     *            The included member names
     * @return A {@link Collection} containing all features matching the
     *         criteria
     */
    public Collection<? extends F> findFeatures(BoundingBox boundingBox, Extent<Double> zRange,
            Extent<TimePosition> tRange, Set<String> memberNames);

    /**
     * @return The minimum bounding box which will contain everything within
     *         this {@link FeatureCollection}
     */
    public BoundingBox getCollectionBoundingBox();
    
    
    /**
     * @return The minimum vertical extent which will contain everything within
     *         this {@link FeatureCollection}
     */
    public Extent<VerticalPosition> getCollectionVerticalExtent();
    
    /**
     * @return The minimum time range which will contain everything within
     *         this {@link FeatureCollection}
     */
    public Extent<TimePosition> getCollectionTimeExtent();
}
