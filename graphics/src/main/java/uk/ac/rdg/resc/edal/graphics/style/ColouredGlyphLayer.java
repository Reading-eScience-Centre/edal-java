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

package uk.ac.rdg.resc.edal.graphics.style;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.xml.bind.Unmarshaller;

import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.feature.DiscreteFeature;
import uk.ac.rdg.resc.edal.feature.Feature;
import uk.ac.rdg.resc.edal.feature.PointFeature;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.graphics.utils.ColourableIcon;
import uk.ac.rdg.resc.edal.graphics.utils.FeatureCatalogue;
import uk.ac.rdg.resc.edal.graphics.utils.FeatureCatalogue.FeaturesAndMemberName;
import uk.ac.rdg.resc.edal.graphics.utils.PlottingDomainParams;
import uk.ac.rdg.resc.edal.grid.RegularAxis;
import uk.ac.rdg.resc.edal.grid.RegularGrid;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.util.Extents;
import uk.ac.rdg.resc.edal.util.GISUtils;

public class ColouredGlyphLayer extends ImageLayer {
    protected String dataFieldName;
    protected String glyphIconName = "circle";
    protected ColourScheme colourScheme;

    protected Map<String, ColourableIcon> icons;
    protected ColourableIcon icon;

    public ColouredGlyphLayer(String dataFieldName, String glyphIconName, ColourScheme colourScheme) {
        this.dataFieldName = dataFieldName;
        this.glyphIconName = glyphIconName;
        this.colourScheme = colourScheme;
        /*
         * Read the icon files before the object is created.
         */
        readInIcons();
        /*
         * Now set the icon to the default
         */
        icon = getIcon(glyphIconName);
    }

    void afterUnmarshal(Unmarshaller u, Object parent) {
        /*
         * Once we have unmarshalled from XML, we may need to change the icon
         * used
         */
        icon = getIcon(glyphIconName);
    }

    public String getGlyphIconName() {
        return glyphIconName;
    }

    protected void readInIcons() {
        icons = new HashMap<String, ColourableIcon>();

        URL iconUrl;
        BufferedImage iconImage;

        /*
         * This will work when the files are packaged as a JAR. For running
         * within an IDE, you may need to add the root directory of the project
         * to the classpath
         */
        try {
            iconUrl = this.getClass().getResource("/img/circle.png");
            iconImage = ImageIO.read(iconUrl);
            icons.put("circle", new ColourableIcon(iconImage));

            iconUrl = this.getClass().getResource("/img/square.png");
            iconImage = ImageIO.read(iconUrl);
            icons.put("square", new ColourableIcon(iconImage));

            iconUrl = this.getClass().getResource("/img/dot.png");
            iconImage = ImageIO.read(iconUrl);
            icons.put("dot", new ColourableIcon(iconImage));
            
            iconUrl = this.getClass().getResource("/img/bigcircle.png");
            iconImage = ImageIO.read(iconUrl);
            icons.put("bigcircle", new ColourableIcon(iconImage));
        } catch (IOException e) {
            throw new EdalException(
                    "Cannot read required icons.  Ensure that JAR is packaged correctly, or that your project is set up correctly in your IDE");
        }
    }

    protected ColourableIcon getIcon(String name) {
        ColourableIcon ret = null;
        if (name == null) {
            ret = icons.get("circle");
        } else {
            ret = icons.get(name.toLowerCase());
        }
        if (ret != null) {
            return ret;
        } else {
            return icons.get("circle");
        }
    }

    @Override
    protected void drawIntoImage(BufferedImage image, PlottingDomainParams params,
            FeatureCatalogue catalogue) throws EdalException {
        /*
         * Get a RegularGrid from the parameters
         */
        RegularGrid imageGrid = params.getImageGrid();

        /*
         * Get all of the features which need to be drawn in this image. Because
         * we want to also draw glyphs which are outside the image (so that
         * their edges are visible), we extend the bounding box
         */
        BoundingBox bbox = params.getBbox();
        BoundingBox largeBoundingBox = GISUtils.getLargeBoundingBox(bbox, 10);
        PlottingDomainParams extractParams = new PlottingDomainParams(
                (int) (params.getWidth() * 1.1), (int) (params.getHeight() * 1.1),
                largeBoundingBox, params.getZExtent(), params.getTExtent(),
                params.getTargetHorizontalPosition(), params.getTargetZ(), params.getTargetT());

        FeaturesAndMemberName featuresForLayer = catalogue.getFeaturesForLayer(dataFieldName,
                extractParams);
        Collection<? extends DiscreteFeature<?, ?>> features = featuresForLayer.getFeatures();

        /*
         * Get the RegularAxis objects so that we can find the unconstrained
         * index of the position (i.e. the index even if it is beyond the axis
         * bounds)
         */
        RegularAxis xAxis = imageGrid.getXAxis();
        RegularAxis yAxis = imageGrid.getYAxis();

        /*
         * The graphics object for drawing
         */
        Graphics2D g = image.createGraphics();
        
        g.setColor(colourScheme.getColor(null));
        g.fillRect(0, 0, image.getWidth(), image.getHeight());

        for (DiscreteFeature<?, ?> feature : features) {
            /*
             * We only support plotting of PointFeatures
             */
            if (feature instanceof PointFeature) {
                PointFeature pointFeature = (PointFeature) feature;

                /*
                 * Find the co-ordinates to draw the icon at
                 */
                HorizontalPosition position = pointFeature.getHorizontalPosition();
                if (!GISUtils.crsMatch(position.getCoordinateReferenceSystem(), params.getBbox()
                        .getCoordinateReferenceSystem())) {
                    position = GISUtils.transformPosition(position, params.getBbox()
                            .getCoordinateReferenceSystem());
                }

                int i = xAxis.findIndexOfUnconstrained(position.getX());
                int j = params.getHeight() - 1 - yAxis.findIndexOfUnconstrained(position.getY());

                Number value = pointFeature.getValue(featuresForLayer.getMember());
                if (value != null) {
                    /*
                     * Draw the icon
                     */
                    Color color = colourScheme.getColor(value);
                    g.drawImage(icon.getColouredIcon(color), i - icon.getWidth() / 2,
                            j - icon.getHeight() / 2, null);
                }
            }
        }
    }

    @Override
    public Collection<Class<? extends Feature<?>>> supportedFeatureTypes() {
        List<Class<? extends Feature<?>>> clazzes = new ArrayList<Class<? extends Feature<?>>>();
        clazzes.add(PointFeature.class);
        return clazzes;
    }

    @Override
    public Set<NameAndRange> getFieldsWithScales() {
        Set<NameAndRange> ret = new HashSet<Drawable.NameAndRange>();
        ret.add(new NameAndRange(dataFieldName, Extents.newExtent(colourScheme.getScaleMin(),
                colourScheme.getScaleMax())));
        return ret;
    }
}
