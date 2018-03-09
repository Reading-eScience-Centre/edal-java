/*******************************************************************************
 * Copyright (c) 2016 The University of Reading
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

package uk.ac.rdg.resc.edal.examples;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import uk.ac.rdg.resc.edal.dataset.Dataset;
import uk.ac.rdg.resc.edal.dataset.GriddedDataset;
import uk.ac.rdg.resc.edal.dataset.cdm.CdmGridDatasetFactory;
import uk.ac.rdg.resc.edal.dataset.cdm.En3DatasetFactory;
import uk.ac.rdg.resc.edal.dataset.plugins.VectorPlugin;
import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.geometry.BoundingBoxImpl;
import uk.ac.rdg.resc.edal.graphics.style.ArrowLayer;
import uk.ac.rdg.resc.edal.graphics.style.ArrowLayer.ArrowStyle;
import uk.ac.rdg.resc.edal.graphics.style.ColourScheme;
import uk.ac.rdg.resc.edal.graphics.style.ColouredGlyphLayer;
import uk.ac.rdg.resc.edal.graphics.style.MapImage;
import uk.ac.rdg.resc.edal.graphics.style.RasterLayer;
import uk.ac.rdg.resc.edal.graphics.style.ScaleRange;
import uk.ac.rdg.resc.edal.graphics.style.SegmentColourScheme;
import uk.ac.rdg.resc.edal.graphics.utils.GraphicsUtils;
import uk.ac.rdg.resc.edal.graphics.utils.PlottingDomainParams;
import uk.ac.rdg.resc.edal.graphics.utils.SimpleFeatureCatalogue;

/**
 * This program was created to generate some high resolution images for a
 * poster. Although it is not documented, or a particularly illustrative
 * example, it may as well live in the examples code. If it's not useful (and it
 * probably isn't) then ignore it.
 *
 * @author Guy Griffiths
 */
public class CreateHiRes {
    public static void main(String[] args) throws EdalException, IOException {
        en4();
        uncertTemp();
        winds();
    }

    public static void en4() throws EdalException, IOException {
        En3DatasetFactory df = new En3DatasetFactory();
        Dataset dataset = df.createDataset("en4",
                "/home/guy/Data/EN4/EN.4.1.1.f.profiles.g10.20151[01].nc");
        for (String varId : dataset.getVariableIds()) {
            System.out.println(varId);
        }
        MapImage imageGenerator = new MapImage();
        String tempName = "POTM_CORRECTED";
        ColourScheme colourScheme = new SegmentColourScheme(new ScaleRange(0f, 35f, false), null,
                null, null, "div-BuRd2", 250);
        ColouredGlyphLayer glyphs = new ColouredGlyphLayer(tempName, "bigcircle", colourScheme);
        imageGenerator.getLayers().add(glyphs);

        BufferedImage image = imageGenerator.drawImage(PlottingDomainParams
                .paramsForGriddedDataset(10000, 5000, BoundingBoxImpl.global(), null, null),
                new SimpleFeatureCatalogue<Dataset>(dataset, true));
        ImageIO.write(image, "png", new File("en4.png"));
    }

    public static void uncertTemp() throws EdalException, IOException {
        CdmGridDatasetFactory df = new CdmGridDatasetFactory();
        GriddedDataset dataset = (GriddedDataset) df.createDataset("sst",
                "/home/guy/Data/GeoViQua/ostia/ostia.ncml");
        for (String varId : dataset.getVariableIds()) {
            System.out.println(varId);
        }
        MapImage imageGenerator = new MapImage();
        String tempName = "analysed_sst";
        String errorName = "analysis_error";
        ColourScheme colourScheme = new SegmentColourScheme(new ScaleRange(270f, 310f, false),
                null, null, null, "seq-cubeYF", 250);
        RasterLayer raster = new RasterLayer(tempName, colourScheme);
        imageGenerator.getLayers().add(raster);

        System.out.println(GraphicsUtils.estimateValueRange(dataset, errorName));
        ColourScheme uncertCS = new SegmentColourScheme(new ScaleRange(0f, 3.5f, false), null,
                null, null, "#00000000,#ff000000", 250);
        RasterLayer uncert = new RasterLayer(errorName, uncertCS);
        imageGenerator.getLayers().add(uncert);

        BufferedImage image = imageGenerator.drawImage(PlottingDomainParams
                .paramsForGriddedDataset(10000, 5000, BoundingBoxImpl.global(), null, null),
                new SimpleFeatureCatalogue<GriddedDataset>(dataset, true));
        ImageIO.write(image, "png", new File("uncert.png"));
    }

    public static void winds() throws EdalException, IOException {
        CdmGridDatasetFactory df = new CdmGridDatasetFactory();
        GriddedDataset dataset = (GriddedDataset) df.createDataset("wind",
                "/home/guy/Data/vector/vector.nc");
        dataset.addVariablePlugin(new VectorPlugin("sozowind", "somewind", "wind", true));
        MapImage imageGenerator = new MapImage();
        String magName = "sozowind:somewind-mag";
        String dirName = "sozowind:somewind-dir";
        ColourScheme colourScheme = new SegmentColourScheme(new ScaleRange(0.0f, 0.2f, false),
                null, null, null, "psu-magma", 250);
        RasterLayer raster = new RasterLayer(magName, colourScheme);
        imageGenerator.getLayers().add(raster);

        ArrowLayer arrow = new ArrowLayer(dirName, 60, Color.black, new Color(0, true),
                ArrowStyle.FAT_ARROW);
        imageGenerator.getLayers().add(arrow);

        BufferedImage image = imageGenerator.drawImage(PlottingDomainParams
                .paramsForGriddedDataset(10000, 5000, BoundingBoxImpl.global(), null, null),
                new SimpleFeatureCatalogue<GriddedDataset>(dataset, true));
        ImageIO.write(image, "png", new File("winds.png"));
    }
}
