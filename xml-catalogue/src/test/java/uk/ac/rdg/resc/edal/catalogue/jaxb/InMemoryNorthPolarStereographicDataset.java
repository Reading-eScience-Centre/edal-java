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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import uk.ac.rdg.resc.edal.catalogue.jaxb.DatasetConfig;
import uk.ac.rdg.resc.edal.catalogue.jaxb.VariableConfig;
import uk.ac.rdg.resc.edal.dataset.DataReadingStrategy;
import uk.ac.rdg.resc.edal.dataset.GridDataSource;
import uk.ac.rdg.resc.edal.dataset.GriddedDataset;
import uk.ac.rdg.resc.edal.dataset.plugins.VectorPlugin;
import uk.ac.rdg.resc.edal.exceptions.DataReadingException;
import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.exceptions.InvalidCrsException;
import uk.ac.rdg.resc.edal.feature.GridFeature;
import uk.ac.rdg.resc.edal.graphics.utils.ColourPalette;
import uk.ac.rdg.resc.edal.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.grid.RegularGridImpl;
import uk.ac.rdg.resc.edal.metadata.GridVariableMetadata;
import uk.ac.rdg.resc.edal.metadata.Parameter;
import uk.ac.rdg.resc.edal.util.Array4D;
import uk.ac.rdg.resc.edal.util.Extents;
import uk.ac.rdg.resc.edal.util.GISUtils;

/**
 * This is an in-memory dataset which provides x- and y- vector variables for a
 * dataset on a native north polar stereographic grid. It can be used for
 * testing of vector transformations. It also exposes the method
 * {@link InMemoryNorthPolarStereographicDataset#getWmsVariables()} for
 * convenience, allowing this to be easily inserted into an
 * {@link NcwmsCatalogue}.
 * 
 * @author Guy Griffiths
 */
public class InMemoryNorthPolarStereographicDataset extends GriddedDataset {
    private static final long serialVersionUID = 1L;

    public InMemoryNorthPolarStereographicDataset() throws EdalException {
        super("northPole", getGridVariables());
        addVariablePlugin(new VectorPlugin("allx_u", "allx_v", "All X", false));
        addVariablePlugin(new VectorPlugin("ally_u", "ally_v", "All Y", false));
    }

    private static Collection<GridVariableMetadata> getGridVariables() {
        HorizontalGrid hDomain;
        try {
            hDomain = new RegularGridImpl(-4350000, -4350000, 8350000, 8350000,
                    GISUtils.getCrs("EPSG:32661"), 100, 100);
        } catch (InvalidCrsException e) {
            e.printStackTrace();
            hDomain = null;
        }
        GridVariableMetadata xumetadata = new GridVariableMetadata(new Parameter("allx_u",
                "All X,  u-component", "...", "none", null), hDomain, null, null, true);
        GridVariableMetadata xvmetadata = new GridVariableMetadata(new Parameter("allx_v",
                "All X,  v-component", "...", "none", null), hDomain, null, null, true);
        GridVariableMetadata yumetadata = new GridVariableMetadata(new Parameter("ally_u",
                "All Y,  u-component", "...", "none", null), hDomain, null, null, true);
        GridVariableMetadata yvmetadata = new GridVariableMetadata(new Parameter("ally_v",
                "All Y,  v-component", "...", "none", null), hDomain, null, null, true);
        List<GridVariableMetadata> metadataList = new ArrayList<GridVariableMetadata>();
        metadataList.add(xumetadata);
        metadataList.add(xvmetadata);
        metadataList.add(yumetadata);
        metadataList.add(yvmetadata);
        return metadataList;
    }

    public Collection<VariableConfig> getWmsVariables() {
        DatasetConfig ds = new DatasetConfig();
        ds.setId("northPole");
        ds.setTitle("North Polar Stereographic");
        ds.setLocation("inmemory");

        VariableConfig xuVar = new VariableConfig("allx_u", "All X u-comp", "", Extents.newExtent(0f,
                100f), ColourPalette.DEFAULT_PALETTE_NAME, Color.black, Color.black, new Color(0,
                true), "linear", 250);
        VariableConfig xvVar = new VariableConfig("allx_v", "All X v-comp", "", Extents.newExtent(0f,
                100f), ColourPalette.DEFAULT_PALETTE_NAME, Color.black, Color.black, new Color(0,
                true), "linear", 250);
        VariableConfig yuVar = new VariableConfig("ally_u", "All Y u-comp", "", Extents.newExtent(0f,
                100f), ColourPalette.DEFAULT_PALETTE_NAME, Color.black, Color.black, new Color(0,
                true), "linear", 250);
        VariableConfig yvVar = new VariableConfig("ally_v", "All Y v-comp", "", Extents.newExtent(0f,
                100f), ColourPalette.DEFAULT_PALETTE_NAME, Color.black, Color.black, new Color(0,
                true), "linear", 250);

        VariableConfig xmagVar = new VariableConfig("allx_uallx_v-mag", "All X magnitude", "",
                Extents.newExtent(0f, 100f), ColourPalette.DEFAULT_PALETTE_NAME, Color.black,
                Color.black, new Color(0, true), "linear", 250);
        VariableConfig xdirVar = new VariableConfig("allx_uallx_v-dir", "All X direction", "",
                Extents.newExtent(0f, 100f), ColourPalette.DEFAULT_PALETTE_NAME, Color.black,
                Color.black, new Color(0, true), "linear", 250);

        VariableConfig ymagVar = new VariableConfig("ally_ually_v-mag", "All Y magnitude", "",
                Extents.newExtent(0f, 100f), ColourPalette.DEFAULT_PALETTE_NAME, Color.black,
                Color.black, new Color(0, true), "linear", 250);
        VariableConfig ydirVar = new VariableConfig("ally_ually_v-dir", "All Y direction", "",
                Extents.newExtent(0f, 100f), ColourPalette.DEFAULT_PALETTE_NAME, Color.black,
                Color.black, new Color(0, true), "linear", 250);

        VariableConfig xgroupVar = new VariableConfig("allx_uallx_v-group", "All X", "",
                Extents.newExtent(0f, 100f), ColourPalette.DEFAULT_PALETTE_NAME, Color.black,
                Color.black, new Color(0, true), "linear", 250);
        VariableConfig ygroupVar = new VariableConfig("ally_ually_v-group", "All Y", "",
                Extents.newExtent(0f, 100f), ColourPalette.DEFAULT_PALETTE_NAME, Color.black,
                Color.black, new Color(0, true), "linear", 250);
        List<VariableConfig> vars = new ArrayList<VariableConfig>();
        xuVar.setParentDataset(ds);
        vars.add(xuVar);
        xvVar.setParentDataset(ds);
        vars.add(xvVar);
        yuVar.setParentDataset(ds);
        vars.add(yuVar);
        yvVar.setParentDataset(ds);
        vars.add(yvVar);
        xmagVar.setParentDataset(ds);
        vars.add(xmagVar);
        xdirVar.setParentDataset(ds);
        vars.add(xdirVar);
        ymagVar.setParentDataset(ds);
        vars.add(ymagVar);
        ydirVar.setParentDataset(ds);
        vars.add(ydirVar);
        xgroupVar.setParentDataset(ds);
        vars.add(xgroupVar);
        ygroupVar.setParentDataset(ds);
        vars.add(ygroupVar);

        return vars;
    }

    @Override
    public GridFeature readFeature(String featureId) throws DataReadingException {
        throw new UnsupportedOperationException("Not implemented - this is a test dataset");
    }

    @Override
    protected GridDataSource openDataSource() throws DataReadingException {
        return new GridDataSource() {
            @Override
            public Array4D<Number> read(final String variableId, int tmin, int tmax, int zmin,
                    int zmax, int ymin, int ymax, int xmin, int xmax) throws IOException {
                return new Array4D<Number>((tmax - tmin + 1), (zmax - zmin + 1), (ymax - ymin + 1),
                        (xmax - xmin + 1)) {
                    @Override
                    public Number get(int... coords) {
                        if (variableId.equalsIgnoreCase("allx_u")) {
                            return 10;
                        } else if (variableId.equalsIgnoreCase("allx_v")) {
                            return 0;
                        } else if (variableId.equalsIgnoreCase("ally_u")) {
                            return 0;
                        } else if (variableId.equalsIgnoreCase("ally_v")) {
                            return 10;
                        }
                        return null;
                    }

                    @Override
                    public void set(Number value, int... coords) {
                    }
                };
            }

            @Override
            public void close() throws DataReadingException {
            }
        };
    }

    @Override
    protected DataReadingStrategy getDataReadingStrategy() {
        return DataReadingStrategy.BOUNDING_BOX;
    }
}