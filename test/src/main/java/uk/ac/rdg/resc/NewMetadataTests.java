package uk.ac.rdg.resc;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.geotoolkit.referencing.crs.DefaultGeographicCRS;

import uk.ac.rdg.resc.edal.cdm.coverage.NcGridCoverage;
import uk.ac.rdg.resc.edal.coverage.GridCoverage2D;
import uk.ac.rdg.resc.edal.coverage.Record;
import uk.ac.rdg.resc.edal.coverage.grid.GridValuesMatrix;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.coverage.grid.impl.RegularGridImpl;
import uk.ac.rdg.resc.edal.graphics.ColorPalette;
import uk.ac.rdg.resc.edal.graphics.ImageGenerators;
import uk.ac.rdg.resc.edal.graphics.MapStyleDescriptor;
import uk.ac.rdg.resc.edal.position.impl.HorizontalPositionImpl;
import uk.ac.rdg.resc.edal.util.Extents;

public class NewMetadataTests {
    public static void main(String[] args) throws InstantiationException, IOException {
        GridCoverage2D ncCov = new NcGridCoverage("/home/guy/Data/FOAM_ONE/FOAM_20100130.0.nc", "TMP");
        Record val = ncCov.evaluate(new HorizontalPositionImpl(0, 0, DefaultGeographicCRS.WGS84));
        HorizontalGrid targetGrid = new RegularGridImpl(-180, -90, 180, 90, DefaultGeographicCRS.WGS84, 360, 180);
//        GridCoverage2D extractedCov = ncCov.extractGridCoverage(targetGrid, CollectionUtils.setOf("TMP"));
//        GridValuesMatrix<?> gridValues = extractedCov.getGridValues("TMP");
        GridValuesMatrix<?> gridValues = ncCov.getGridValues("TMP");
        MapStyleDescriptor style = new MapStyleDescriptor();
        style.setScaleRange(Extents.newExtent(280.0f, 300.0f));
        ColorPalette.loadPalettes(new File("/home/guy/Workspace/edal-java/ncwms/src/main/webapp/WEB-INF/conf/palettes/"));
        style.setColorPalette("redblue");
        BufferedImage image = ImageGenerators.plotGrid(gridValues, style);
        ImageIO.write(image, "png", new File("new_metadata_test.png"));
    }
}
