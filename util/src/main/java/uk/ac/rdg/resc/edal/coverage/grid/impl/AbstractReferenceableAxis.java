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
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR  CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package uk.ac.rdg.resc.edal.coverage.grid.impl;

import java.util.AbstractList;
import java.util.List;
import org.geotoolkit.geometry.GeneralEnvelope;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import uk.ac.rdg.resc.edal.coverage.grid.ReferenceableAxis;
import uk.ac.rdg.resc.edal.util.Utils;

/**
 * Abstract superclass for {@link ReferenceableAxis} implementations.  Handles
 * the tricky case of searching for longitude values in the axis (longitude
 * values wrap around the globe).
 * @todo automatically apply the maximum extent -90:90 for latitude axes?  Or is
 * this dangerous, given that some model grid cells are constructed with latitudes
 * outside this range?
 * @author Jon
 */
public abstract class AbstractReferenceableAxis implements ReferenceableAxis {

    private final CoordinateSystemAxis coordSysAxis;
    private final String name;
    private final boolean isLongitude;

    private final List<Double> coordValues = new AbstractList<Double>()
    {
        @Override
        public Double get(int index) {
            return AbstractReferenceableAxis.this.getCoordinateValue(index);
        }

        @Override
        public int size() {
            return AbstractReferenceableAxis.this.getSize();
        }

        @Override
        public int indexOf(Object o) {
            if (o == null) return -1;
            if (!(o instanceof Double)) return -1;
            double target = ((Double)o).doubleValue();
            return AbstractReferenceableAxis.this.doGetCoordinateIndex(target);
        }
    };

    /**
     * Creates an axis that is referenceable to the given coordinate system axis.
     * The name of the axis will be set to the name of the given axis.
     * @throws NullPointerException if coordSysAxis is null
     */
    protected AbstractReferenceableAxis(CoordinateSystemAxis coordSysAxis, boolean isLongitude)
    {
        if (coordSysAxis == null) throw new NullPointerException("coordSysAxis cannot be null");
        this.name = coordSysAxis.getName().toString();
        this.coordSysAxis = coordSysAxis;
        this.isLongitude = isLongitude;
    }

    /**
     * Creates an axis with the given name.  The {@link #getCoordinateSystemAxis()
     * coordinate system axis} will be null.
     */
    protected AbstractReferenceableAxis(String name, boolean isLongitude)
    {
        this.name = name;
        this.coordSysAxis = null;
        this.isLongitude = isLongitude;
    }

    @Override
    public final String getName() { return this.name; }

    /**
     * {@inheritDoc}
     * <p>If this is a longitude axis, this implementation will ensure that the
     * value is corrected to the smallest equivalent longitude value that is
     * greater than {@link #getMinimumValue()}, before handing off to
     * {@link #doGetCoordinateIndex(double)}.</p>
     */
    @Override
    public final int getCoordinateIndex(double value) {
        if (this.isLongitude) {
            value = Utils.getNextEquivalentLongitude(this.getMinimumValue(), value);
        }
        return this.doGetCoordinateIndex(value);
    }

    /**
     * <p>Gets the index of the given coordinate value, ignoring the possibility
     * of longitude axis wrapping.  Returns -1 if the value is not a coordinate
     * value of this axis.</p>
     * <p>Subclasses should make this implementation as efficient as possible,
     * since the implementation is reused in the {@code indexOf} method of
     * the {@link #getCoordinateValues() list of coordinate values}.</p>
     */
    protected abstract int doGetCoordinateIndex(double value);
    
    /** Gets the value of the axis at index 0 */
    private final double getFirstValue() {
        return this.getCoordinateValue(0);
    }
    
    /** Gets the value of the axis at index (size - 1) */
    private final double getLastValue() {
        return this.getCoordinateValue(this.getSize() - 1);
    }

    /**
     * Returns the minimum coordinate value of this axis.  This will be the first
     * coordinate value if the coordinate values are in ascending order, or the
     * last coordinate value if the coordinate values are in descending order.
     * @return the minimum coordinate value of this axis
     */
    private final double getMinimumValue() {
        return this.isAscending() ? this.getFirstValue() : this.getLastValue();
    }

    /**
     * Returns the maximum coordinate value of this axis.  This will be the last
     * coordinate value if the coordinate values are in ascending order, or the
     * first coordinate value if the coordinate values are in descending order.
     * @return the maximum coordinate value of this axis
     */
    private final double getMaximumValue() {
        return this.isAscending() ? this.getLastValue() : this.getFirstValue();
    }

    /** Return true if the values of the axis are in ascending numerical order */
    protected abstract boolean isAscending();

    /**
     * {@inheritDoc}
     * <p>This implementation checks that the value is within the bounds of the
     * {@link #getExtent() envelope} of the axis, then hands off to
     * {@link #doGetNearestCoordinateIndex(double)}.
     * If this is a longitude axis, this will ensure that the value is corrected
     * to the smallest equivalent longitude value that is greater than the minimum
     * value of the extent.
     */
    @Override
    public final int getNearestCoordinateIndex(double value) {
        Envelope extent = this.getExtent();
        double minValue = extent.getMinimum(0);
        if (this.isLongitude) {
            value = Utils.getNextEquivalentLongitude(minValue, value);
        }
        if (value < minValue || value > extent.getMaximum(0)) {
            return -1;
        }
        return this.doGetNearestCoordinateIndex(value);
    }

    /**
     * Finds the nearest coordinate index to the given value, which has already
     * been checked for validity (hence this method should always return a valid
     * index).
     */
    protected abstract int doGetNearestCoordinateIndex(double value);

    @Override
    public final CoordinateSystemAxis getCoordinateSystemAxis() {
        return this.coordSysAxis;
    }

    @Override
    public final Envelope getExtent()
    {
        final double min;
        final double max;
        if (this.getSize() == 1)
        {
            min = this.getMinimumValue();
            max = this.getMaximumValue();
        }
        else
        {
            double val1 = this.getFirstValue() -
                0.5 * (this.getCoordinateValue(1) - this.getFirstValue());
            double val2 = this.getLastValue() +
                0.5 * (this.getLastValue() - this.getCoordinateValue(this.getSize() - 2));
            if (this.isAscending())
            {
                min = val1;
                max = val2;
            }
            else
            {
                min = val2;
                max = val1;
            }
        }
        return new GeneralEnvelope(min, max);
    }

    /**
     * {@inheritDoc}
     * <p>The list returned by this method is unmodifiable.</p>
     */
    @Override
    public final List<Double> getCoordinateValues() {
        return this.coordValues;
    }
}
