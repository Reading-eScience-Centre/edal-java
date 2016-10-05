/*******************************************************************************
 * Copyright (c) 2014 The University of Reading
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.joda.time.DateTime;

import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.domain.TemporalDomain;
import uk.ac.rdg.resc.edal.domain.VerticalDomain;
import uk.ac.rdg.resc.edal.exceptions.DataReadingException;
import uk.ac.rdg.resc.edal.feature.DiscreteFeature;
import uk.ac.rdg.resc.edal.feature.PointFeature;
import uk.ac.rdg.resc.edal.feature.PointSeriesFeature;
import uk.ac.rdg.resc.edal.feature.ProfileFeature;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.geometry.BoundingBoxImpl;
import uk.ac.rdg.resc.edal.metadata.VariableMetadata;
import uk.ac.rdg.resc.edal.position.GeoPosition;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.VerticalPosition;
import uk.ac.rdg.resc.edal.util.Array1D;
import uk.ac.rdg.resc.edal.util.Extents;
import uk.ac.rdg.resc.edal.util.GISUtils;
import uk.ac.rdg.resc.edal.util.ImmutableArray1D;

/**
 * An {@link AbstractContinuousDomainDataset} whose map features are
 * {@link PointFeature}s. Subclasses must provide a method to convert from their
 * underlying feature type to a {@link PointFeature}, given a set of parameters.
 * 
 * @param <F>
 *            The type of {@link DiscreteFeature} which this
 *            {@link PointDataset} reads natively (i.e. the same type of
 *            {@link DiscreteFeature} which is returned by the
 *            {@link DiscreteFeatureReader} associated with this
 *            {@link PointDataset}).
 * 
 * @author Guy Griffiths
 */
public abstract class PointDataset<F extends DiscreteFeature<?, ?>> extends
        AbstractContinuousDomainDataset {
    private BoundingBox bbox;
    private Extent<Double> zExtent;
    private Extent<DateTime> tExtent;

    public PointDataset(String id, Collection<? extends VariableMetadata> vars,
            FeatureIndexer featureIndexer, BoundingBox bbox, Extent<Double> zExtent,
            Extent<DateTime> tExtent) {
        super(id, vars, featureIndexer);
        this.bbox = bbox;
        this.zExtent = zExtent;
        this.tExtent = tExtent;
    }

    public PointDataset(String id, Collection<? extends VariableMetadata> vars,
            FeatureIndexer featureIndexer) {
        super(id, vars, featureIndexer);
        double minX = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;

        double minY = Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;

        double minZ = Double.MAX_VALUE;
        double maxZ = -Double.MAX_VALUE;

        long minT = Long.MAX_VALUE;
        long maxT = -Long.MAX_VALUE;

        for (VariableMetadata metadata : vars) {
            minX = Math.min(minX, metadata.getHorizontalDomain().getBoundingBox().getMinX());
            maxX = Math.max(maxX, metadata.getHorizontalDomain().getBoundingBox().getMaxX());
            minY = Math.min(minY, metadata.getHorizontalDomain().getBoundingBox().getMinY());
            maxY = Math.max(maxY, metadata.getHorizontalDomain().getBoundingBox().getMaxY());
            VerticalDomain zDomain = metadata.getVerticalDomain();
            if (zDomain != null) {
                minZ = Math.min(minZ, zDomain.getExtent().getLow());
                maxZ = Math.max(maxZ, zDomain.getExtent().getHigh());
            }
            TemporalDomain tDomain = metadata.getTemporalDomain();
            if (tDomain != null) {
                minT = Math.min(minT, tDomain.getExtent().getLow().getMillis());
                maxT = Math.max(maxT, tDomain.getExtent().getHigh().getMillis());
            }
        }

        this.bbox = new BoundingBoxImpl(minX, minY, maxX, maxY, DefaultGeographicCRS.WGS84);
        if (minZ != Double.MAX_VALUE) {
            this.zExtent = Extents.newExtent(minZ, maxZ);
        } else {
            this.zExtent = null;
        }
        if (minT != Long.MAX_VALUE) {
            this.tExtent = Extents.newExtent(new DateTime(minT), new DateTime(maxT));
        } else {
            this.tExtent = null;
        }
    }

    public List<? extends DiscreteFeature<?, ?>> extractMapFeatures(Set<String> varIds,
            BoundingBox hExtent, Extent<Double> zExtent, Extent<DateTime> tExtent, Double targetZ,
            DateTime targetT) throws DataReadingException {
        List<? extends DiscreteFeature<?, ?>> extractedMapFeatures = super.extractMapFeatures(
                varIds, hExtent, zExtent, tExtent);
        List<PointFeature> pointFeatures = new ArrayList<>();
        for (DiscreteFeature<?, ?> feature : extractedMapFeatures) {
            /*
             * This conversion is safe because:
             * 
             * AbstractContinuousDomainDataset reads all features with
             * getFeatureReader().readFeatures()
             * 
             * This class overrides getFeatureReader() to ensure that it returns
             * features of type F
             */
            @SuppressWarnings("unchecked")
            PointFeature pointFeature = convertFeature((F) feature, hExtent, zExtent, tExtent,
                    targetZ, targetT);
            if (pointFeature != null) {
                pointFeatures.add(pointFeature);
            }
        }
        return pointFeatures;
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Always returns a PointFeature - this is the point of this class. For
     * mixed feature types, extend directly from
     * AbstractContinuousDomainDataset.
     */
    @Override
    public final Class<PointFeature> getMapFeatureType(String variableId) {
        return PointFeature.class;
    }

    @Override
    protected BoundingBox getDatasetBoundingBox() {
        return bbox;
    }

    @Override
    protected Extent<Double> getDatasetVerticalExtent() {
        return zExtent;
    }

    @Override
    protected Extent<DateTime> getDatasetTimeExtent() {
        return tExtent;
    }

    public abstract DiscreteFeatureReader<F> getFeatureReader();

    /**
     * Convert a {@link DiscreteFeature} of type F to a {@link PointFeature}
     * 
     * @param feature
     *            The feature to convert
     * @param hExtent
     *            The {@link BoundingBox} containing the feature
     * @param zExtent
     *            The vertical {@link Extent} containing the feature
     * @param tExtent
     *            The time {@link Extent} containing the feature
     * @param targetZ
     *            The target depth if multiple features are present in the
     *            vertical extent
     * @param targetT
     *            The target time if multiple features are present in the time
     *            extent
     * @return A {@link PointFeature} ready for plotting, or <code>null</code>
     *         if the supplied parameters specify a location where no
     *         {@link PointFeature} is present.
     */
    protected abstract PointFeature convertFeature(F feature, BoundingBox hExtent,
            Extent<Double> zExtent, Extent<DateTime> tExtent, Double targetZ, DateTime targetT);

    /**
     * Convenience method to convert a {@link ProfileFeature} to a
     * {@link PointFeature}. Can be used by subclasses which only handle
     * {@link ProfileFeature}s to implement
     * {@link PointDataset#convertFeature(DiscreteFeature, BoundingBox, Extent, Extent, Double, DateTime)}
     * 
     * @param feature
     *            The {@link ProfileFeature} to convert.
     * @param targetZ
     *            The target elevation to extract a {@link PointFeature} at
     * @return A {@link PointFeature} ready for plotting, or <code>null</code>
     *         if the supplied elevation specifies a location where no
     *         {@link PointFeature} is present.
     */
    protected PointFeature convertProfileFeature(ProfileFeature feature, Double targetZ) {
        HorizontalPosition position = feature.getHorizontalPosition();

        /*
         * Get the z-index of the target depth within the vertical domain
         */
        int zIndex;
        if (targetZ == null) {
            /*
             * If no target z is provided, pick the value closest to the surface
             */
            zIndex = feature.getDomain().findIndexOf(
                    GISUtils.getClosestElevationToSurface(feature.getDomain()));
        } else {
            zIndex = GISUtils.getIndexOfClosestElevationTo(targetZ, feature.getDomain());
        }
        if (zIndex < 0) {
            return null;
        }

        Double zValue = feature.getDomain().getCoordinateValue(zIndex);

        GeoPosition pos4d = new GeoPosition(position, new VerticalPosition(zValue, feature
                .getDomain().getVerticalCrs()), feature.getTime());

        Map<String, Array1D<Number>> values = new HashMap<>();
        for (String paramId : feature.getParameterIds()) {
            values.put(paramId, new ImmutableArray1D<>(new Number[] { feature.getValues(paramId)
                    .get(zIndex) }));
        }

        PointFeature ret = new PointFeature(feature.getId() + ":" + zValue, "Measurement from "
                + feature.getName(), "Value extracted at depth " + zValue + " from "
                + feature.getDescription(), pos4d, feature.getParameterMap(), values);
        ret.getFeatureProperties().putAll(feature.getFeatureProperties());
        return ret;
    }

    /**
     * Convenience method to convert a {@link PointSeriesFeature} to a
     * {@link PointFeature}. Can be used by subclasses which only handle
     * {@link PointSeriesFeature}s to implement
     * {@link PointDataset#convertFeature(DiscreteFeature, BoundingBox, Extent, Extent, Double, DateTime)}
     * 
     * @param feature
     *            The {@link PointSeriesFeature} to convert.
     * @param targetT
     *            The target {@link DateTime} to extract a {@link PointFeature}
     *            at
     * @return A {@link PointFeature} ready for plotting, or <code>null</code>
     *         if the supplied {@link DateTime} specifies a time at which no
     *         {@link PointFeature} is present.
     */
    protected PointFeature convertPointSeriesFeature(PointSeriesFeature feature, DateTime targetT) {
        HorizontalPosition position = feature.getHorizontalPosition();

        /*
         * Get the t-index of the target depth within the vertical domain
         */
        int tIndex;
        if (targetT == null) {
            /*
             * If no target time is provided, pick the time closest to now
             */
            tIndex = feature.getDomain().findIndexOf(
                    GISUtils.getClosestToCurrentTime(feature.getDomain()));
        } else {
            tIndex = GISUtils.getIndexOfClosestTimeTo(targetT, feature.getDomain());
        }
        if (tIndex < 0) {
            return null;
        }

        DateTime time = feature.getDomain().getCoordinateValue(tIndex);

        GeoPosition pos4d = new GeoPosition(position, feature.getVerticalPosition(), time);

        Map<String, Array1D<Number>> values = new HashMap<>();
        for (String paramId : feature.getParameterIds()) {
            values.put(paramId, new ImmutableArray1D<>(new Number[] { feature.getValues(paramId)
                    .get(tIndex) }));
        }

        PointFeature ret = new PointFeature(feature.getId() + ":" + time, "Measurement from "
                + feature.getName(), "Value extracted at time " + time + " from "
                + feature.getDescription(), pos4d, feature.getParameterMap(), values);
        ret.getFeatureProperties().putAll(feature.getFeatureProperties());
        return ret;
    }
}
