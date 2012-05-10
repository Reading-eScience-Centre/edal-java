/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.rdg.resc.edal.coverage.impl;

import java.awt.image.BufferedImage;
import java.util.Set;
import uk.ac.rdg.resc.edal.Phenomenon;
import uk.ac.rdg.resc.edal.Unit;
import uk.ac.rdg.resc.edal.coverage.GridCoverage2D;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.coverage.grid.impl.RegularGridImpl;
import uk.ac.rdg.resc.edal.coverage.metadata.ScalarMetadata;
import uk.ac.rdg.resc.edal.coverage.metadata.impl.ScalarMetadataImpl;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.util.CollectionUtils;

/**
 * A {@link GridCoverage2D} that wraps a {@link BufferedImage}, which is
 * georeferenced through the addition of a {@link BoundingBox}.
 * @author Jon
 */
public final class ImageCoverage extends AbstractInMemoryGridCoverage2D
{
    private static final String RED   = "red";
    private static final String GREEN = "green";
    private static final String BLUE  = "blue";
    private static final String ALPHA = "alpha";
    private static final String COMPOSITE = "composite";
    
    private final BufferedImage im;
    private final HorizontalGrid domain;
    
    private static final Set<String> MEMBER_NAMES =
            CollectionUtils.setOf(RED, GREEN, BLUE, ALPHA, COMPOSITE);
    
    public ImageCoverage(BufferedImage im, BoundingBox bbox)
    {
        this.im = im;
        this.domain = new RegularGridImpl(bbox, im.getWidth(), im.getHeight());
    }

    @Override
    public String getDescription() {
        return "2D Grid Coverage generated from a BufferedImage";
    }

    @Override
    public Set<String> getMemberNames() {
        return MEMBER_NAMES;
    }

    @Override
    public HorizontalGrid getDomain() {
        return this.domain;
    }
    
    /**
     * Returns the wrapped BufferedImage.
     */
    public BufferedImage getImage() {
        return this.im;
    }

    @Override
    protected Object getValue(String memberName, int i, int j) {
        int pixel = this.im.getRGB(i, j);
        
        // TODO
        return null;
    }

    @Override
    public ScalarMetadata getRangeMetadata(String memberName) {
        checkMemberName(memberName);
        return null; // TODO
    }
    
}
