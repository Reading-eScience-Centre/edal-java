/*******************************************************************************
 * Copyright (c) 2012 The University of Reading
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

package uk.ac.rdg.resc.edal.coverage.impl;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Set;

import javax.imageio.ImageIO;

import org.geotoolkit.referencing.crs.DefaultGeographicCRS;

import uk.ac.rdg.resc.edal.Phenomenon;
import uk.ac.rdg.resc.edal.Unit;
import uk.ac.rdg.resc.edal.coverage.GridCoverage2D;
import uk.ac.rdg.resc.edal.coverage.grid.GridAxis;
import uk.ac.rdg.resc.edal.coverage.grid.GridValuesMatrix;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.coverage.grid.impl.GridAxisImpl;
import uk.ac.rdg.resc.edal.coverage.grid.impl.InMemoryGridValuesMatrix;
import uk.ac.rdg.resc.edal.coverage.grid.impl.RegularGridImpl;
import uk.ac.rdg.resc.edal.coverage.metadata.ScalarMetadata;
import uk.ac.rdg.resc.edal.coverage.metadata.impl.ScalarMetadataImpl;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.geometry.impl.BoundingBoxImpl;
import uk.ac.rdg.resc.edal.util.CollectionUtils;

/**
 * <p>
 * A {@link GridCoverage2D} that wraps a {@link BufferedImage}, which is
 * georeferenced through the addition of a {@link BoundingBox}.
 * </p>
 * <p>
 * The coverage contains members for each of the four RGBA components of the
 * image (with integer values) and a Composite member, which returns a
 * {@link Color} for each pixel.
 * </p>
 * 
 * @author Jon
 * @author Guy Griffiths
 */
public class ImageCoverage extends GridCoverage2DImpl
{
    private static final String RED   = "red";
    private static final String GREEN = "green";
    private static final String BLUE  = "blue";
    private static final String ALPHA = "alpha";
    private static final String COMPOSITE = "composite";
    
    private final BufferedImage im;
    
    private static final Set<String> MEMBER_NAMES =
            CollectionUtils.setOf(RED, GREEN, BLUE, ALPHA, COMPOSITE);
    
    public ImageCoverage(BufferedImage im, BoundingBox bbox)
    {
        super("2D Grid Coverage generated from a BufferedImage", new RegularGridImpl(bbox,
                im.getWidth(), im.getHeight()), DataReadingStrategy.PIXEL_BY_PIXEL);
        this.im = im;
    }

    @Override
    public String getDescription() {
        return "2D Grid Coverage generated from a BufferedImage";
    }

    @Override
    public Set<String> getScalarMemberNames() {
        return MEMBER_NAMES;
    }
    
    /**
     * Returns the wrapped BufferedImage.
     */
    public BufferedImage getImage() {
        return this.im;
    }

    @Override
    public ScalarMetadata getScalarMetadata(String memberName) {
        this.checkMemberName(memberName);
        return new ScalarMetadataImpl(this.getRangeMetadata(), memberName, memberName,
            Phenomenon.getPhenomenon("none"), Unit.getUnit("none"), getValueType(memberName));
    }
    
    private static Class<?> getValueType(String memberName)
    {
        // All the value types are integers except for the Composite member
        if (COMPOSITE.equals(memberName)) return Color.class;
        else return Integer.class;
    }

    @Override
    public GridValuesMatrix<Object> getGridValues(final String memberName)
    {
        this.checkMemberName(memberName);
        
        return new InMemoryGridValuesMatrix<Object>() {
            @Override
            public Class<Object> getValueType() {
                return (Class<Object>) ImageCoverage.getValueType(memberName);
            }

            @Override
            public GridAxis doGetAxis(int n) {
                switch (n) {
                case 0:
                    return new GridAxisImpl("x", im.getWidth());
                case 1:
                    return new GridAxisImpl("y", im.getHeight());
                default:
                    /*
                     * We should never reach this code, because getAxis will already have checked the bounds
                     */
                    throw new IllegalStateException("Axis index out of bounds");
                }
            }

            @Override
            public int getNDim() {
                return 2;
            }

            @Override
            protected Object doReadPoint(int[] coords) {
                coords[1] = getAxis(1).size() - coords[1] - 1;
                Color color = new Color(im.getRGB(coords[0], coords[1]));
                if (COMPOSITE.equals(memberName)) return color;
                if (RED.equals(memberName)) return color.getRed();
                if (GREEN.equals(memberName)) return color.getGreen();
                if (BLUE.equals(memberName)) return color.getBlue();
                if (ALPHA.equals(memberName)) return color.getAlpha();
                throw new IllegalStateException("Member name " + memberName + " not recognized");
            }
        };
    }
    
    // Simple test routine: copying an image
    // TODO perform a spatial subset, not just a straight copy.
    public static void main(String[] args) throws Exception
    {
        BufferedImage im = ImageIO.read(new File("C:\\Users\\Jon\\Desktop\\bluemarble.world.200410.3x5400x2700.jpg"));
        GridCoverage2D cov = new ImageCoverage(im,
             new BoundingBoxImpl(-180, -90, 180, 90, DefaultGeographicCRS.WGS84));
        
        // HorizontalGrid targetGrid = new RegularGridImpl(-180, -90, 180, 90, DefaultGeographicCRS.WGS84, 512, 512);
        HorizontalGrid targetGrid = new RegularGridImpl(-10, 50, 5, 60, DefaultGeographicCRS.WGS84, 512, 512);
        GridCoverage2D subset = cov.extractGridCoverage(targetGrid, CollectionUtils.setOf(RED, BLUE, COMPOSITE));
        
        String memberName = COMPOSITE;
        GridValuesMatrix<?> gvm = subset.getGridValues(memberName);
        System.out.println(gvm.getValueType());
        int xSize = gvm.getAxis(0).size();
        int ySize = gvm.getAxis(1).size();
        System.out.printf("%d, %d%n", xSize, ySize);
        
        BufferedImage im2 = new BufferedImage(xSize, ySize, BufferedImage.TYPE_INT_ARGB);
        for (int j = 0; j < ySize; j++)
        {
            for (int i = 0; i < xSize; i++)
            {
                //int comp = (Integer)gvm.readPoint(i, j);
                //Color col = new Color(comp, comp, comp);
                // We have to reverse the y-axis because coordinates in image
                // space run from top to bottom
                Color col = (Color) gvm.readPoint(new int[] { i, j });
                im2.setRGB(i, ySize - j - 1, col.getRGB());
            }
        }
        ImageIO.write(im2, "png", new File("c:\\Users\\Jon\\Desktop\\" + memberName + ".png"));
    }
    
}
