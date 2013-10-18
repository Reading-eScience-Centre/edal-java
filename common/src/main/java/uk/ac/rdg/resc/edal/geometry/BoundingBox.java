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

package uk.ac.rdg.resc.edal.geometry;

import uk.ac.rdg.resc.edal.position.HorizontalPosition;

/**
 * A rectangular bounding box in the horizontal plane.
 * 
 * @author Jon Blower
 */
public interface BoundingBox extends Polygon {
    /**
     * Gets the minimum ordinate along the first axis, equivalent to
     * {@code getMinimum(0)}.
     */
    public double getMinX();

    /**
     * Gets the maximum ordinate along the first axis, equivalent to
     * {@code getMaximum(0)}.
     */
    public double getMaxX();

    /**
     * Gets the minimum ordinate along the second axis, equivalent to
     * {@code getMinimum(1)}.
     */
    public double getMinY();

    /**
     * Gets the maximum ordinate along the second axis, equivalent to
     * {@code getMaximum(1)}.
     */
    public double getMaxY();

    /**
     * Gets the width of the bounding box, i.e. {@code getMaxX() - getMinX()}.
     */
    public double getWidth();

    /**
     * Gets the height of the bounding box, i.e. {@code getMaxY() - getMinY()}.
     */
    public double getHeight();

    /**
     * Gets the position (getMinX(), getMinY())
     */
    public HorizontalPosition getLowerCorner();

    /**
     * Gets the position (getMaxX(), getMaxY())
     */
    public HorizontalPosition getUpperCorner();
    
    @Override
    public boolean equals(Object obj);
    
    @Override
    public int hashCode();
}
