/*
 * Copyright (c) 2010 The University of Reading
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
 */

package uk.ac.rdg.resc.edal.feature;

import uk.ac.rdg.resc.edal.CalendarSystem;
import uk.ac.rdg.resc.edal.coverage.Coverage;

/**
 * <p>Superclass for all CSML FeatureTypes.</p>
 * @author Jon
 * @todo How best to represent metadata?
 */
public interface Feature 
{
    
    /**
     * Gets the {@link FeatureCollection} to which this feature belongs.  If this
     * feature does not belong to a collection, this will return null.
     * @return
     */
    public FeatureCollection<? extends Feature> getFeatureCollection();
    
    /**
     * Gets an identifier that is unique within the {@link #getFeatureCollection() 
     * feature collection to which this feature belongs}.  Must never be null.
     */
    public String getId();
    
    /**
     * Gets a human-readable short string that identifies this feature.
     * Not enforced to be unique.
     */
    public String getName();
    
    /**
     * Gets a (perhaps lengthy) human-readable description of this feature.
     * @return
     */
    public String getDescription();

    /**
     * Gets the measurement values
     */
    public Coverage getCoverage();

    /**
     * Gets the calendar system used to interpret dates and times relating to
     * this feature.
     * @return
     */
    public CalendarSystem getCalendarSystem();
}
