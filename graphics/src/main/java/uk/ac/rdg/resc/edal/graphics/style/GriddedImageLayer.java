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

package uk.ac.rdg.resc.edal.graphics.style;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.rdg.resc.edal.domain.MapDomain;
import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.feature.DiscreteFeature;
import uk.ac.rdg.resc.edal.feature.Feature;
import uk.ac.rdg.resc.edal.feature.GridFeature;
import uk.ac.rdg.resc.edal.feature.MapFeature;
import uk.ac.rdg.resc.edal.graphics.utils.FeatureCatalogue;
import uk.ac.rdg.resc.edal.graphics.utils.PlottingDomainParams;
import uk.ac.rdg.resc.edal.graphics.utils.FeatureCatalogue.FeaturesAndMemberName;
import uk.ac.rdg.resc.edal.grid.GridCell2D;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.util.Array;
import uk.ac.rdg.resc.edal.util.Array2D;

/**
 * An {@link ImageLayer} which handles a single {@link GridFeature} for every
 * layer name.
 * 
 * @author Guy
 */
public abstract class GriddedImageLayer extends ImageLayer {

    protected class MapFeatureDataReader {
        private PlottingDomainParams params;
        private FeatureCatalogue catalogue;
        private Map<String, FeaturesAndMemberName> extractedFeatures = new HashMap<String, FeaturesAndMemberName>();

        public MapFeatureDataReader(PlottingDomainParams params, FeatureCatalogue catalogue) {
            this.params = params;
            this.catalogue = catalogue;
        }

        private MapFeature getFeature(String layerId) throws EdalException {
            /*
             * This cast is OK, because extractFeature performs the check and
             * throws an exception if necessary
             */
            return (MapFeature) extractFeature(layerId).getFeatures().iterator().next();
        }

        private String getVariableName(String layerId) throws EdalException {
            return extractFeature(layerId).getMember();
        }

        private FeaturesAndMemberName extractFeature(String layerId) throws EdalException {
            if (!extractedFeatures.containsKey(layerId)) {
                FeaturesAndMemberName featureAndMemberName = catalogue.getFeaturesForLayer(layerId,
                        params);
                Collection<? extends DiscreteFeature<?, ?>> features = featureAndMemberName
                        .getFeatures();
                MapFeature mapFeature = null;
                for (DiscreteFeature<?, ?> testFeature : features) {
                    if (testFeature instanceof MapFeature) {
                        if (mapFeature != null) {
                            throw new EdalException("Expecting a single gridded feature for the layer "
                                    + layerId);
                        } else {
                            mapFeature = (MapFeature) testFeature;
                        }
                    }
                }
                if (mapFeature == null) {
                    throw new EdalException("Expecting a gridded feature for the layer " + layerId);
                }
                FeaturesAndMemberName singleMapFeature = new FeaturesAndMemberName(mapFeature,
                        featureAndMemberName.getMember());
                extractedFeatures.put(layerId, singleMapFeature);
            }
            return extractedFeatures.get(layerId);
        }

        public Array2D<Number> getDataForLayerName(String layerId) throws EdalException {
            /*
             * TODO We could add different data reading strategies here - e.g.
             * subsampling of layers so that gridded features can be plotted as
             * spaced glyphs...
             */
            MapFeature mapFeature = getFeature(layerId);
            final Array2D<Number> values = mapFeature.getValues(getVariableName(layerId));
            /*
             * Since BufferedImages have the y-axis increasing downwards, wrap
             * the returned values in an Array2D with a flipped y-axis
             */
            return new Array2D<Number>(values.getYSize(), values.getXSize()) {
                @Override
                public void set(Number value, int... coords) {
                    throw new UnsupportedOperationException("This is an immutable Array2D");
                }

                @Override
                public Number get(int... coords) {
                    return values.get(params.getHeight() - coords[0] - 1, coords[1]);
                }
            };
        };

        public Array2D<HorizontalPosition> getMapDomainObjects(String layerId) throws EdalException {
            MapFeature mapFeature = getFeature(layerId);
            final Array<GridCell2D> domainObjects = mapFeature.getDomain().getDomainObjects();
            /*
             * Since BufferedImages have the y-axis increasing downwards, wrap
             * the returned values in an Array2D with a flipped y-axis
             */
            return new Array2D<HorizontalPosition>(domainObjects.getShape()[0],
                    domainObjects.getShape()[1]) {
                @Override
                public HorizontalPosition get(int... coords) {
                    return domainObjects.get(params.getHeight() - coords[0] - 1, coords[1])
                            .getCentre();
                }

                @Override
                public void set(HorizontalPosition value, int... coords) {
                    throw new UnsupportedOperationException("This is an immutable Array2D");
                }
            };
        }
    }

    @Override
    protected void drawIntoImage(BufferedImage image, final PlottingDomainParams params,
            final FeatureCatalogue catalogue) throws EdalException {
        drawIntoImage(image, new MapFeatureDataReader(params, catalogue));
    }

    @Override
    public Collection<Class<? extends Feature<?>>> supportedFeatureTypes() {
        List<Class<? extends Feature<?>>> clazzes = new ArrayList<Class<? extends Feature<?>>>();
        clazzes.add(MapFeature.class);
        return clazzes;
    }

    /**
     * Draws the data into the supplied image.
     * 
     * @param image
     *            A {@link BufferedImage} to draw into
     * @param dataReader
     *            A {@link MapFeatureDataReader} which is used to obtain the
     *            actual data values and domain. The {@link MapDomain} returned
     *            will match the size of the image, and pixels are georeferenced
     *            to the GridCell2Ds which comprise the domain
     * @throws EdalException
     *             If there is a problem reading the data or drawing into the
     *             image
     */
    protected abstract void drawIntoImage(BufferedImage image, MapFeatureDataReader dataReader)
            throws EdalException;

}
