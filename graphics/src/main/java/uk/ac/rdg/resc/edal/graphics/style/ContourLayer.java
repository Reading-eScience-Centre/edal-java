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

import gov.noaa.pmel.sgt.CartesianGraph;
import gov.noaa.pmel.sgt.CartesianRenderer;
import gov.noaa.pmel.sgt.ContourLevels;
import gov.noaa.pmel.sgt.ContourLineAttribute;
import gov.noaa.pmel.sgt.GridAttribute;
import gov.noaa.pmel.sgt.JPane;
import gov.noaa.pmel.sgt.Layer;
import gov.noaa.pmel.sgt.LineAttribute;
import gov.noaa.pmel.sgt.LinearTransform;
import gov.noaa.pmel.sgt.dm.SGTData;
import gov.noaa.pmel.sgt.dm.SGTGrid;
import gov.noaa.pmel.sgt.dm.SimpleGrid;
import gov.noaa.pmel.util.Dimension2D;
import gov.noaa.pmel.util.Range2D;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.metadata.VariableMetadata;
import uk.ac.rdg.resc.edal.util.Array2D;
import uk.ac.rdg.resc.edal.util.Extents;

/**
 * A {@link GriddedImageLayer} which plots contours
 *
 * @author Charles Roberts
 * @author Guy Griffiths
 */
public class ContourLayer extends GriddedImageLayer {
    public enum ContourLineStyle {
        HEAVY {
            @Override
            public int getLineStyleInteger() {
                return LineAttribute.HEAVY;
            }
        },

        HIGHLIGHT {
            @Override
            public int getLineStyleInteger() {
                return LineAttribute.HIGHLIGHT;
            }
        },

        STROKE {
            @Override
            public int getLineStyleInteger() {
                return LineAttribute.STROKE;
            }
        };

        public abstract int getLineStyleInteger();
    }

    private String dataFieldName;
    private ScaleRange scale;

    private Boolean autoscaleEnabled = true;
    private Double numberOfContours = 10.0;
    private Color contourLineColour = Color.BLACK;
    private SegmentColourScheme colourScheme = null;
    private Integer contourLineWidth = 1;
    private ContourLineStyle contourLineStyle = ContourLineStyle.HEAVY;
    private Boolean labelEnabled = true;

    /**
     * @param dataFieldName
     *            The data field to plot
     * @param scale
     *            The {@link ScaleRange} spanned by the contours
     * @param autoscaleEnabled
     *            Whether to auto-scale the data
     * @param numberOfContours
     *            The number of contours to plot
     * @param contourLineColour
     *            The colour to plot contours. This defaults to black, and will
     *            be ignored if a palette is specified.
     * @param contourPalette
     *            The name of the colour palette to use. This can be
     *            <code>null</code>, in which case all contours have the same
     *            colour.
     * @param contourLineWidth
     *            The width, in pixels, to draw contour lines
     * @param contourLineStyle
     *            The {@link ContourLineStyle} in which to plot contours.
     * @param labelEnabled
     *            Whether or not to add value labels to contour lines.
     */
    public ContourLayer(String dataFieldName, ScaleRange scale, boolean autoscaleEnabled,
            double numberOfContours, Color contourLineColour, String contourPalette,
            int contourLineWidth, ContourLineStyle contourLineStyle, boolean labelEnabled) {
        this.dataFieldName = dataFieldName;
        this.scale = scale;
        this.autoscaleEnabled = autoscaleEnabled;
        this.numberOfContours = numberOfContours;
        this.contourLineColour = contourLineColour;
        this.contourLineWidth = contourLineWidth;
        this.contourLineStyle = contourLineStyle;
        this.labelEnabled = labelEnabled;
        if (contourPalette != null) {
            this.colourScheme = new SegmentColourScheme(scale, null, null, new Color(0, true),
                    contourPalette, 250);
        }
    }

    @Override
    protected void drawIntoImage(BufferedImage image, MapFeatureDataReader dataReader)
            throws EdalException {
        int width = image.getWidth();
        int height = image.getHeight();
        double[] values = new double[width * height];
        double[] xAxis = new double[width];
        double[] yAxis = new double[height];

        int count = 0;
        for (int i = 0; i < width; i++) {
            xAxis[i] = i;
            for (int j = 0; j < height; j++) {
                yAxis[j] = height - j - 1;
                values[count] = Double.NaN;
                count++;
            }
        }

        Float scaleMin = null;
        Float scaleMax = null;
        if (autoscaleEnabled) {
            scaleMin = Float.MAX_VALUE;
            scaleMax = -Float.MAX_VALUE;
        } else {
            scaleMin = scale.getScaleMin();
            scaleMax = scale.getScaleMax();
        }

        Array2D<Number> dataValues = dataReader.getDataForLayerName(dataFieldName);
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                Number value = dataValues.get(j, i);
                float val;
                if (value == null) {
                    val = Float.NaN;
                } else {
                    val = value.floatValue();
                }
                /*
                 * SGT goes against the grain somewhat by specifying that the
                 * y-axis values vary fastest.
                 */
                values[j + i * height] = val;
                if (autoscaleEnabled) {
                    if (val < scaleMin)
                        scaleMin = val;
                    if (val > scaleMax)
                        scaleMax = val;
                }
            }
        }

        SGTGrid sgtGrid = new SimpleGrid(values, xAxis, yAxis, null);

        CartesianGraph cg = getCartesianGraph(sgtGrid, width, height);

        double contourSpacing = (scaleMax - scaleMin) / numberOfContours;

        ContourLevels levels = new ContourLevels();
        for (double val = scaleMin; val <= scaleMax; val += contourSpacing) {
            ContourLineAttribute lineStyle = new ContourLineAttribute();
            if (colourScheme != null) {
                lineStyle.setColor(colourScheme.getColor(val));
            } else {
                lineStyle.setColor(contourLineColour);
            }
            if (contourLineStyle != null) {
                lineStyle.setStyle(contourLineStyle.getLineStyleInteger());
            }
            lineStyle.setWidth(contourLineWidth);
            if (!labelEnabled) {
                /*
                 * The lineStyle.setLabelEnabled() method doesn't work. It
                 * always leaves labels present. Setting this height to zero
                 * gets rid of them though.
                 */
                lineStyle.setLabelHeightP(0);
            }
            levels.addLevel(val, lineStyle);
        }

        GridAttribute attr = new GridAttribute(levels);
        attr.setStyle(GridAttribute.CONTOUR);

        CartesianRenderer renderer = CartesianRenderer.getRenderer(cg, sgtGrid, attr);

        Graphics g = image.getGraphics();
        renderer.draw(g);
    }

    private static CartesianGraph getCartesianGraph(SGTData data, int width, int height) {
        /*
         * To get fixed size labels we need to set a physical size much smaller
         * than the pixel size (since pixels can't represent physical size).
         * Since the SGT code is so heavily tied into the display mechanism, and
         * a factor of around 100 seems to produce decent results, it's almost
         * certainly measured in inches (96dpi being a fairly reasonable monitor
         * resolution).
         * 
         * Anyway, setting the physical size as a constant factor of the pixel
         * size gives good results.
         * 
         * Font size seems to be ignored.
         */
        double factor = 96;
        double physWidth = width / factor;
        double physHeight = height / factor;

        Layer layer = new Layer("", new Dimension2D(physWidth, physHeight));
        JPane pane = new JPane("id", new Dimension(width, height));
        layer.setPane(pane);
        layer.setBounds(0, 0, width, height);

        CartesianGraph graph = new CartesianGraph();
        // Create Ranges representing the size of the image
        Range2D physXRange = new Range2D(0, physWidth);
        Range2D physYRange = new Range2D(0, physHeight);
        // These transforms convert x and y coordinates to pixel indices
        LinearTransform xt = new LinearTransform(physXRange, data.getXRange());
        LinearTransform yt = new LinearTransform(physYRange, data.getYRange());
        graph.setXTransform(xt);
        graph.setYTransform(yt);
        layer.setGraph(graph);
        return graph;
    }

    @Override
    public Set<NameAndRange> getFieldsWithScales() {
        Set<NameAndRange> ret = new HashSet<Drawable.NameAndRange>();
        ret.add(new NameAndRange(dataFieldName, Extents.newExtent(scale.getScaleMin(),
                scale.getScaleMax())));
        return ret;
    }

    @Override
    public MetadataFilter getMetadataFilter() {
        return new MetadataFilter() {
            @Override
            public boolean supportsMetadata(VariableMetadata metadata) {
                if (metadata.getParameter().getUnits().equalsIgnoreCase("degrees")) {
                    /*
                     * We want to exclude directional fields - they look
                     * terrible plotted as rasters, and we already have
                     * ArrowLayers for that...
                     */
                    return false;
                }
                if (metadata.getParameter().getCategories() != null) {
                    /*
                     * We don't want to plot contours for categorical data
                     */
                    return false;
                }
                return true;
            }
        };
    }
}
