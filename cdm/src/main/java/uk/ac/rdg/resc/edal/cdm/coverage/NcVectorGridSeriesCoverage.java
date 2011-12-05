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
package uk.ac.rdg.resc.edal.cdm.coverage;

import java.util.ArrayList;
import java.util.List;

import ucar.nc2.Variable;
import uk.ac.rdg.resc.edal.Extent;
import uk.ac.rdg.resc.edal.Phenomenon;
import uk.ac.rdg.resc.edal.PhenomenonVocabulary;
import uk.ac.rdg.resc.edal.coverage.Coverage;
import uk.ac.rdg.resc.edal.coverage.RangeMetadata;
import uk.ac.rdg.resc.edal.coverage.RangeMetadataImpl;
import uk.ac.rdg.resc.edal.coverage.VectorGridSeriesCoverage;
import uk.ac.rdg.resc.edal.coverage.domain.GridSeriesDomain;
import uk.ac.rdg.resc.edal.coverage.grid.GridCell4D;
import uk.ac.rdg.resc.edal.coverage.grid.TimeAxis;
import uk.ac.rdg.resc.edal.coverage.impl.AbstractDiscreteSimpleCoverage;
import uk.ac.rdg.resc.edal.position.GeoPosition;
import uk.ac.rdg.resc.edal.position.Vector2D;
import uk.ac.rdg.resc.edal.position.impl.Vector2DFloat;

/**
 * A {@link Coverage}, backed by (a) NetCDF file(s) which holds Vector data
 * 
 * @author Guy Griffiths
 * 
 */
public class NcVectorGridSeriesCoverage extends
        AbstractDiscreteSimpleCoverage<GeoPosition, GridCell4D, Vector2D<Float>> implements
        VectorGridSeriesCoverage<Float> {

    private NcGridSeriesCoverage xCoverage;
    private NcGridSeriesCoverage yCoverage;

    /**
     * Instantiate the coverage from two existing coverages containing scalar
     * data
     * 
     * @param xCoverage
     *            the coverage containing the x-component of the data
     * @param yCoverage
     *            the coverage containing the y-component of the data
     * @throws InstantiationException
     *             if the two coverages are not of the same size, with the same
     *             units
     */
    public NcVectorGridSeriesCoverage(NcGridSeriesCoverage xCoverage, NcGridSeriesCoverage yCoverage)
            throws InstantiationException {
        if (xCoverage.size() != yCoverage.size()) {
            throw new InstantiationException(
                    "Component coverages must be the same size as each other");
        }
        if (!xCoverage.getRangeMetadata().getUnits()
                .equals(yCoverage.getRangeMetadata().getUnits())) {
            throw new InstantiationException("x- and y-components must have the same units");
        }
        this.xCoverage = xCoverage;
        this.yCoverage = yCoverage;
    }

    /**
     * Merge additional variables into the coverage
     * 
     * @param xVar
     *            the {@link Variable} containing the additional x-component
     *            data
     * @param yVar
     *            the {@link Variable} containing the additional y-component
     *            data
     * @param tAxis
     *            the {@link TimeAxis} of the additional coverages
     */
    public void addToCoverage(Variable xVar, Variable yVar, TimeAxis tAxis) {
        xCoverage.addToCoverage(xVar, tAxis);
        yCoverage.addToCoverage(yVar, tAxis);
    }

    @Override
    public GridSeriesDomain getDomain() {
        return xCoverage.getDomain();
    }

    @Override
    public Vector2D<Float> evaluate(int tindex, int zindex, int yindex, int xindex) {
        float xVal = xCoverage.evaluate(tindex, zindex, yindex, xindex);
        float yVal = yCoverage.evaluate(tindex, zindex, yindex, xindex);
        return new Vector2DFloat(xVal, yVal);
    }

    @Override
    public List<Vector2D<Float>> evaluate(Extent<Integer> tindexExtent,
            Extent<Integer> zindexExtent, Extent<Integer> yindexExtent, Extent<Integer> xindexExtent) {
        List<Float> xVals = xCoverage.evaluate(tindexExtent, zindexExtent, yindexExtent,
                xindexExtent);
        List<Float> yVals = yCoverage.evaluate(tindexExtent, zindexExtent, yindexExtent,
                xindexExtent);

        if (xVals.size() != yVals.size()) {
            throw new UnsupportedOperationException(
                    "Cannot evaluate a vector containing two different coverages");
        }

        List<Vector2D<Float>> ret = new ArrayList<Vector2D<Float>>();
        for (int i = 0; i < xVals.size(); i++) {
            ret.add(new Vector2DFloat(xVals.get(i), yVals.get(i)));
        }
        return ret;
    }

    @Override
    public List<Vector2D<Float>> getValues() {
        List<Float> xVals = xCoverage.getValues();
        List<Float> yVals = yCoverage.getValues();

        if (xVals.size() != yVals.size()) {
            throw new UnsupportedOperationException(
                    "Cannot evaluate a vector containing two different coverages");
        }

        List<Vector2D<Float>> ret = new ArrayList<Vector2D<Float>>();
        for (int i = 0; i < xVals.size(); i++) {
            ret.add(new Vector2DFloat(xVals.get(i), yVals.get(i)));
        }
        return ret;
    }

    @Override
    public String getDescription() {
        String xDesc = xCoverage.getDescription();
        int xIndex = xDesc.indexOf("-component of");
        String description = xDesc.substring(xIndex + 14);
        return description;
    }

    @Override
    protected RangeMetadata getRangeMetadata() {
        String xName = xCoverage.getRangeMetadata().getParameter().getStandardName();
        String yName = yCoverage.getRangeMetadata().getParameter().getStandardName();
        RangeMetadata metadata = new RangeMetadataImpl(getDescription(), Phenomenon.getPhenomenon(
                xName + "+" + yName, PhenomenonVocabulary.CLIMATE_AND_FORECAST), xCoverage
                .getRangeMetadata().getUnits(), Vector2D.class);
        return metadata;
    }
}
