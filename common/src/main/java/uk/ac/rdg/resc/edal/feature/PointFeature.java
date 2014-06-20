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

package uk.ac.rdg.resc.edal.feature;

import java.util.Map;

import uk.ac.rdg.resc.edal.domain.PointDomain;
import uk.ac.rdg.resc.edal.metadata.Parameter;
import uk.ac.rdg.resc.edal.position.GeoPosition;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.util.Array1D;

/**
 * A measurement at a single 4D point
 * 
 * @author Guy Griffiths
 */
public class PointFeature extends AbstractDiscreteFeature<GeoPosition, GeoPosition> {
    public PointFeature(String id, String name, String description, GeoPosition pos4d,
            Map<String, Parameter> parameters, Map<String, Array1D<Number>> values) {
        super(id, name, description, new PointDomain(pos4d), parameters, values);
    }

    @Override
    public PointDomain getDomain() {
        return (PointDomain) super.getDomain();
    }

    /**
     * Convenience method for returning the horizontal position. This is
     * equivalent to calling {@link PointFeature()#getGeoPosition()} and
     * extracting the horizontal part of the position
     * 
     * @return The {@link HorizontalPosition} of this feature
     */
    public HorizontalPosition getHorizontalPosition() {
        return getGeoPosition().getHorizontalPosition();
    }

    /**
     * Convenience method for returning the 4d position of this feature. This is
     * equivalent to calling {@link PointDomain#getDomainObjects()} and
     * extracting the first (and only) position.
     * 
     * @return The {@link GeoPosition} of this feature
     */
    public GeoPosition getGeoPosition() {
        return getDomain().getDomainObjects().get(0);
    }

    @Override
    public Array1D<Number> getValues(String paramId) {
        return (Array1D<Number>) super.getValues(paramId);
    }

    /**
     * Convenience method for extracting the single value associated with a
     * particular parameter.  Equivalent to calling getValues().get(0)
     * 
     * @param varId The variable ID to read
     * @return The value
     */
    public Number getValue(String varId) {
        return getValues(varId).get(0);
    }
}
