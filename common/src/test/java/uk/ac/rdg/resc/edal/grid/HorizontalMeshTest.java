/*******************************************************************************
 * Copyright (c) 2015 The University of Reading
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

package uk.ac.rdg.resc.edal.grid;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import uk.ac.rdg.resc.edal.position.HorizontalPosition;

/**
 * Test class for {@link HorizontalMesh}.
 * 
 * @author Guy Griffiths
 * 
 */
public class HorizontalMeshTest {
    private HorizontalMesh mesh;

    @Before
    public void setUp() {
        /*
         * Test of NestedBoundaries
         */
        List<HorizontalPosition> positions = new ArrayList<>();
        positions.add(new HorizontalPosition(0, 0));
        positions.add(new HorizontalPosition(0, 8));
        positions.add(new HorizontalPosition(9, 8));
        positions.add(new HorizontalPosition(9, 0));

        positions.add(new HorizontalPosition(1, 1));
        positions.add(new HorizontalPosition(1, 3));
        positions.add(new HorizontalPosition(3, 3));
        positions.add(new HorizontalPosition(3, 1));

        positions.add(new HorizontalPosition(1, 4));
        positions.add(new HorizontalPosition(1, 7));
        positions.add(new HorizontalPosition(4, 7));
        positions.add(new HorizontalPosition(4, 4));

        positions.add(new HorizontalPosition(2, 5));
        positions.add(new HorizontalPosition(2, 6));
        positions.add(new HorizontalPosition(3, 6));
        positions.add(new HorizontalPosition(3, 5));

        positions.add(new HorizontalPosition(5, 4));
        positions.add(new HorizontalPosition(5, 7));
        positions.add(new HorizontalPosition(8, 7));
        positions.add(new HorizontalPosition(8, 4));

        positions.add(new HorizontalPosition(10, 2));
        positions.add(new HorizontalPosition(10, 7));
        positions.add(new HorizontalPosition(15, 7));
        positions.add(new HorizontalPosition(15, 2));

        positions.add(new HorizontalPosition(11, 3));
        positions.add(new HorizontalPosition(11, 6));
        positions.add(new HorizontalPosition(14, 6));
        positions.add(new HorizontalPosition(14, 3));

        positions.add(new HorizontalPosition(12, 4));
        positions.add(new HorizontalPosition(12, 5));
        positions.add(new HorizontalPosition(13, 5));
        positions.add(new HorizontalPosition(13, 4));

        positions.add(new HorizontalPosition(2, 4.25));
        positions.add(new HorizontalPosition(2, 4.75));
        positions.add(new HorizontalPosition(3, 4.75));
        positions.add(new HorizontalPosition(3, 4.25));

        positions.add(new HorizontalPosition(2.2, 5.2));
        positions.add(new HorizontalPosition(2.8, 5.2));
        positions.add(new HorizontalPosition(2.8, 5.8));
        positions.add(new HorizontalPosition(2.2, 5.8));

        List<int[]> connections = new ArrayList<>();
        connections.add(new int[] { 0, 1, 2, 3 });
        connections.add(new int[] { 4, 5, 6, 7 });
        connections.add(new int[] { 8, 9, 10, 11 });
        connections.add(new int[] { 12, 13, 14, 15 });
        connections.add(new int[] { 16, 17, 18, 19 });
        connections.add(new int[] { 20, 21, 22, 23 });
        connections.add(new int[] { 24, 25, 26, 27 });
        connections.add(new int[] { 28, 29, 30, 31 });
        connections.add(new int[] { 32, 33, 34, 35 });
        connections.add(new int[] { 36, 37, 38, 39 });

        mesh = HorizontalMesh.fromConnections(positions, connections, 0);

        /*
         * The code below will draw an image with green pixels where the mesh
         * contains the position and white where it doesn't. This gives a nice
         * overview of how nested boundaries work. If the test below fails, a
         * first test would be to uncomment the code below and run it - it
         * should hopefully clarify the situation.
         */
//        int width = 1000;
//        int height = 1000;
//        RegularGridImpl imageGrid = new RegularGridImpl(-1., -1., 16., 9.,
//                DefaultGeographicCRS.WGS84, width, height);
//        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
//        Graphics2D g = image.createGraphics();
//        g.setColor(Color.white);
//        g.fillRect(0, 0, width, height);
//        for (GridCell2D cell : imageGrid.getDomainObjects()) {
//            HorizontalPosition centre = cell.getCentre();
//            if (mesh.contains(centre)) {
//                image.setRGB(cell.getGridCoordinates().getX(), cell.getGridCoordinates().getY(),
//                        Color.green.getRGB());
//            }
//        }
//        ImageIO.write(image, "png", new File("nests.png"));
    }

    /**
     * Tests the {@link HorizontalMesh#contains(HorizontalPosition)} method,
     * using nested boundaries
     */
    @Test
    public void testNestedContains() {
        /*
         * Contained in a top-level polygon
         */
        assertTrue(mesh.contains(new HorizontalPosition(0.5, 0.5)));
        assertTrue(mesh.contains(new HorizontalPosition(4.5, 2)));
        assertTrue(mesh.contains(new HorizontalPosition(10.5, 4.5)));
        assertTrue(mesh.contains(new HorizontalPosition(14.5, 2.5)));

        /*
         * Contained in a 2nd-level polygon
         */
        assertFalse(mesh.contains(new HorizontalPosition(2, 2)));
        assertFalse(mesh.contains(new HorizontalPosition(11.5, 3.5)));
        assertFalse(mesh.contains(new HorizontalPosition(6.5, 5.5)));
        assertFalse(mesh.contains(new HorizontalPosition(1.5, 5.5)));

        /*
         * Contained in a 3rd-level polygon
         */
        assertTrue(mesh.contains(new HorizontalPosition(12.5, 4.5)));
        assertTrue(mesh.contains(new HorizontalPosition(2.5, 4.5)));
        assertTrue(mesh.contains(new HorizontalPosition(2.1, 5.1)));
        assertTrue(mesh.contains(new HorizontalPosition(2.9, 5.9)));

        /*
         * Contained in a 4th-level polygon
         */
        assertFalse(mesh.contains(new HorizontalPosition(2.5, 5.5)));
    }
}
