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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates;
import uk.ac.rdg.resc.edal.coverage.grid.RectilinearGrid;
import uk.ac.rdg.resc.edal.coverage.grid.ReferenceableAxis;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.geometry.HorizontalPosition;
import uk.ac.rdg.resc.edal.geometry.HorizontalPositionList;
import uk.ac.rdg.resc.edal.geometry.impl.BoundingBoxImpl;
import uk.ac.rdg.resc.edal.geometry.impl.HorizontalPositionImpl;
import uk.ac.rdg.resc.edal.util.CollectionUtils;
import uk.ac.rdg.resc.edal.util.Utils;

/**
 * Abstract superclass that partially implements a two-dimensional
 * {@link RectilinearGrid}.
 * @author Jon
 */
public abstract class AbstractRectilinearGrid extends AbstractHorizontalGrid implements RectilinearGrid
{

    protected AbstractRectilinearGrid(CoordinateReferenceSystem crs)
    {
        super(crs);
    }

    @Override
    public ReferenceableAxis getAxis(int index) {
        if (index == 0) return this.getXAxis();
        if (index == 1) return this.getYAxis();
        throw new IndexOutOfBoundsException();
    }

    @Override
    public final BoundingBox getExtent() {
        return new BoundingBoxImpl(
            this.getXAxis().getExtent(),
            this.getYAxis().getExtent(),
            this.getCoordinateReferenceSystem()
        );
    }

    @Override
    public GridEnvelopeImpl getGridExtent() {
        return new GridEnvelopeImpl(
            this.getXAxis().getSize() - 1,
            this.getYAxis().getSize() - 1
        );
    }

    @Override
    protected final HorizontalPosition transformCoordinatesNoBoundsCheck(int i, int j) {
        double x = this.getXAxis().getCoordinateValue(i);
        double y = this.getYAxis().getCoordinateValue(j);
        return new HorizontalPositionImpl(x, y, this.getCoordinateReferenceSystem());
    }

    @Override
    public GridCoordinates inverseTransformCoordinates(HorizontalPosition pos) {
        pos = Utils.transformPosition(pos, this.getCoordinateReferenceSystem());
        int i = this.getXAxis().getCoordinateIndex(pos.getX());
        int j = this.getYAxis().getCoordinateIndex(pos.getY());
        if (i < 0 || j < 0) return null;
        // [i,j] order corresponds with [x,y] as specified in the contract of
        // RectilinearGrid
        return new GridCoordinatesImpl(i, j);
    }

    @Override
    public GridCoordinates findNearestGridPoint(HorizontalPosition pos) {
        pos = Utils.transformPosition(pos, this.getCoordinateReferenceSystem());
        return this.findNearestGridPoint(pos.getX(), pos.getY());
    }

    @Override
    public List<GridCoordinates> findNearestGridPoints(HorizontalPositionList posList) {
        List<HorizontalPosition> newPosList = Utils.transformPositions(posList, this.getCoordinateReferenceSystem());
        List<GridCoordinates> gridCoords = CollectionUtils.newArrayList();
        for (HorizontalPosition pos : newPosList) {
            gridCoords.add(this.findNearestGridPoint(pos.getX(), pos.getY()));
        }
        return Collections.unmodifiableList(gridCoords);
    }

    private GridCoordinates findNearestGridPoint(double x, double y) {
        int i = this.getXAxis().getNearestCoordinateIndex(x);
        int j = this.getYAxis().getNearestCoordinateIndex(y);
        if (i < 0 || j < 0) return null;
        // [i,j] order corresponds with [x,y] as specified in the contract of
        // RectilinearGrid
        return new GridCoordinatesImpl(i, j);
    }

    /** Returns an unmodifiable list of axis names in x,y order */
    @Override
    public final List<String> getAxisNames() {
        return Collections.unmodifiableList(
            Arrays.asList(this.getXAxis().getName(), this.getYAxis().getName())
        );
    }

}
