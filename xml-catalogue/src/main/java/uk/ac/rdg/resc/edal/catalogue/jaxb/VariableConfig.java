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

package uk.ac.rdg.resc.edal.catalogue.jaxb;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import uk.ac.rdg.resc.edal.dataset.Dataset;
import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.graphics.utils.ColourPalette;
import uk.ac.rdg.resc.edal.graphics.utils.EnhancedVariableMetadata;
import uk.ac.rdg.resc.edal.graphics.utils.GraphicsUtils.ColorAdapter;
import uk.ac.rdg.resc.edal.graphics.utils.PlottingStyleParameters;
import uk.ac.rdg.resc.edal.util.Extents;

/**
 * A class representing a variable in the XML config. This contains all of the
 * information needed to define the {@link EnhancedVariableMetadata} which
 * contains default plotting options etc.
 * 
 * @author Guy Griffiths
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class VariableConfig implements EnhancedVariableMetadata {
    @XmlAttribute(name = "id", required = true)
    private String id;

    @XmlAttribute(name = "title")
    private String title = null;

    @XmlAttribute(name = "description")
    private String description = null;

    /*
     * This is handled by the setter, to check for conflict with log scaling
     */
    @XmlTransient
    private Extent<Float> colorScaleRange = null;

    @XmlAttribute(name = "palette")
    private String paletteName = ColourPalette.DEFAULT_PALETTE_NAME;

    @XmlAttribute(name = "belowMinColor")
    @XmlJavaTypeAdapter(ColorAdapter.class)
    private Color belowMinColour = Color.black;

    @XmlAttribute(name = "aboveMaxColor")
    @XmlJavaTypeAdapter(ColorAdapter.class)
    private Color aboveMaxColour = Color.black;

    @XmlAttribute(name = "noDataColor")
    @XmlJavaTypeAdapter(ColorAdapter.class)
    private Color noDataColour = new Color(0, true);

    /*
     * This is handled by the setter, to check for conflict with scale range
     */
    @XmlTransient
    private String scaling = "linear";

    @XmlAttribute(name = "numColorBands")
    private int numColorBands = ColourPalette.MAX_NUM_COLOURS;

    @XmlAttribute(name = "disabled")
    private Boolean disabled = null;

    /* The dataset to which this variable belongs */
    @XmlTransient
    private DatasetConfig dataset;

    VariableConfig() {
    }

    public VariableConfig(String id, String title, String description,
            Extent<Float> colorScaleRange, String paletteName, Color belowMinColour,
            Color aboveMaxColour, Color noDataColour, String scaling, int numColorBands) {
        super();
        this.id = id;
        this.title = title;
        this.description = description;
        this.colorScaleRange = colorScaleRange;
        this.paletteName = paletteName;
        this.belowMinColour = belowMinColour;
        this.aboveMaxColour = aboveMaxColour;
        if (noDataColour == null) {
            this.noDataColour = new Color(0, true);
        } else {
            this.noDataColour = noDataColour;
        }
        this.scaling = scaling;
        this.numColorBands = numColorBands;
    }

    /**
     * @return The ID of the configured variable within its {@link Dataset}
     */
    public String getId() {
        return id;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public PlottingStyleParameters getDefaultPlottingParameters() {
        if (!ColourPalette.getPredefinedPalettes().contains(paletteName)) {
            this.paletteName = ColourPalette.DEFAULT_PALETTE_NAME;
        }
        boolean logScale;
        if (scaling == null) {
            logScale = false;
        } else {
            logScale = scaling.equalsIgnoreCase("log") || scaling.equalsIgnoreCase("logarithmic");
        }
        /*
         * TODO make opacity a configurable property? Or just controllable from
         * WMS URL parameters (currently the case)
         * 
         * TODO Support configuration of multiple default scale ranges
         */
        List<Extent<Float>> scaleRanges = new ArrayList<>();
        scaleRanges.add(colorScaleRange);
        return new PlottingStyleParameters(scaleRanges, paletteName, aboveMaxColour,
                belowMinColour, noDataColour, logScale, numColorBands, 1f);
    }

    public boolean isQueryable() {
        return dataset.isQueryable();
    }

    public boolean isDownloadable() {
        return dataset.isDownloadable();
    }

    public boolean isDisabled() {
        return disabled == null ? false : disabled;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getCopyright() {
        return dataset.getCopyrightStatement();
    }

    @Override
    public String getMoreInfo() {
        return dataset.getMoreInfo();
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @XmlAttribute(name = "colorScaleRange")
    @XmlJavaTypeAdapter(ScaleRangeAdapter.class)
    public void setColorScaleRange(Extent<Float> colorScaleRange) {
        if ((!scaling.equalsIgnoreCase("log") && !scaling.equalsIgnoreCase("logarithmic"))
                || (colorScaleRange != null && colorScaleRange.getLow() > 0.0 && colorScaleRange
                        .getHigh() > 0.0)) {
            /*
             * We don't set either limit to <= zero if the scaling to
             * logarithmic
             */
            this.colorScaleRange = colorScaleRange;
        }
    }

    /*
     * Required for JAX-B marshalling, so not unused
     */
    @SuppressWarnings("unused")
    private Extent<Float> getColorScaleRange() {
        return colorScaleRange;
    }

    public void setPaletteName(String paletteName) {
        this.paletteName = paletteName;
    }

    @XmlAttribute(name = "scaling")
    public void setScaling(String scaling) {
        if ((!scaling.equalsIgnoreCase("log") && !scaling.equalsIgnoreCase("logarithmic"))
                || (colorScaleRange != null && colorScaleRange.getLow() > 0.0 && colorScaleRange
                        .getHigh() > 0.0)) {
            /*
             * We don't set the scaling to logarithmic if either scale bound is
             * less than zero
             */
            this.scaling = scaling;
        }
    }

    /*
     * Required for JAX-B marshalling, so not unused
     */
    @SuppressWarnings("unused")
    private String getScaling() {
        return scaling;
    }

    public void setNumColorBands(int numColorBands) {
        if (numColorBands > ColourPalette.MAX_NUM_COLOURS) {
            this.numColorBands = ColourPalette.MAX_NUM_COLOURS;
        } else if (numColorBands < 2) {
            this.numColorBands = 2;
        } else {
            this.numColorBands = numColorBands;
        }
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    private static class ScaleRangeAdapter extends XmlAdapter<String, Extent<Float>> {
        private ScaleRangeAdapter() {
        }

        @Override
        public Extent<Float> unmarshal(String scaleRangeStr) throws Exception {
            String[] split = scaleRangeStr.split(" ");
            return Extents.newExtent(Float.parseFloat(split[0]), Float.parseFloat(split[1]));
        }

        @Override
        public String marshal(Extent<Float> scaleRange) throws Exception {
            return scaleRange.getLow() + " " + scaleRange.getHigh();
        }

        private static ScaleRangeAdapter adapter = new ScaleRangeAdapter();

        @SuppressWarnings("unused")
        public static ScaleRangeAdapter getInstance() {
            return adapter;
        }
    }

    void setParentDataset(DatasetConfig dataset) {
        this.dataset = dataset;
    }

    public DatasetConfig getParentDataset() {
        return dataset;
    }
}
