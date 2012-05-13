/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
 * <p>A {@link GridCoverage2D} that wraps a {@link BufferedImage}, which is
 * georeferenced through the addition of a {@link BoundingBox}.</p>
 * <p>The coverage contains members for each of the four RGBA components of the
 * image (with integer values) and a Composite member, which returns a {@link Color}
 * for each pixel.</p>
 * @author Jon
 */
public class ImageCoverage extends AbstractGridCoverage2D
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
    public ScalarMetadata getRangeMetadata(String memberName) {
        this.checkMemberName(memberName);
        // All the value types are integers except for the Composite member
        return new ScalarMetadataImpl(memberName, memberName,
                Phenomenon.getPhenomenon("none"), Unit.getUnit("none"), getValueType(memberName));
    }
    
    private static Class<?> getValueType(String memberName)
    {
        if (COMPOSITE.equals(memberName)) return Color.class;
        else return Integer.class;
    }

    @Override
    protected DataReadingStrategy getDataReadingStrategy() {
        // This is an in-memory GVM and so this strategy is most efficient
        return DataReadingStrategy.PIXEL_BY_PIXEL;
    }

    @Override
    public GridValuesMatrix<?> getGridValues(final String memberName)
    {
        this.checkMemberName(memberName);
        
        return new InMemoryGridValuesMatrix<Object>()
        {
            @Override public GridAxis getXAxis() {
                return new GridAxisImpl("x", im.getWidth());
            }

            @Override public GridAxis getYAxis() {
                return new GridAxisImpl("y", im.getHeight());
            }

            @Override
            public Object readPoint(int i, int j) {
                Color color = new Color(im.getRGB(i, j));
                if (COMPOSITE.equals(memberName)) return color;
                if (RED.equals(memberName)) return color.getRed();
                if (GREEN.equals(memberName)) return color.getGreen();
                if (BLUE.equals(memberName)) return color.getBlue();
                if (ALPHA.equals(memberName)) return color.getAlpha();
                throw new IllegalStateException("Member name " + memberName + " not recognized");
            }

            @Override
            public Class<Object> getValueType() {
                return (Class<Object>)(Class)ImageCoverage.getValueType(memberName);
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
        String memberName = BLUE;
        GridValuesMatrix<?> gvm = cov.getGridValues(memberName);
        System.out.println(gvm.getValueType());
        int xSize = gvm.getXAxis().size();
        int ySize = gvm.getYAxis().size();
        
        BufferedImage im2 = new BufferedImage(xSize, ySize, BufferedImage.TYPE_INT_ARGB);
        for (int j = 0; j < ySize; j++)
        {
            for (int i = 0; i < xSize; i++)
            {
                int comp = (Integer)gvm.readPoint(i, j);
                Color col = new Color(comp, comp, comp);
                im2.setRGB(i, j, col.getRGB());
            }
        }
        ImageIO.write(im2, "png", new File("c:\\Users\\Jon\\Desktop\\" + memberName + ".png"));
    }
    
}
