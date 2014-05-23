package uk.ac.rdg.resc.edal.grid;

import static org.junit.Assert.*;

import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.junit.Test;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.geometry.BoundingBoxImpl;

public class RegularGridImplTest {
    
    private CoordinateReferenceSystem crs =DefaultGeographicCRS.WGS84;
    private RegularGrid grid;

    @Test
    public void regularGridImplTest() {
        BoundingBox bbox =new BoundingBoxImpl(100.0, 20.0, 120.0, 50.0, crs);
        grid = new RegularGridImpl(bbox, 20, 30);

        RegularAxis longside;
        RegularAxis latside;
        double resolutionX = (bbox.getMaxX() -bbox.getMinX())/ grid.getXSize();
        double resolutionY = (bbox.getMaxY() -bbox.getMinY())/ grid.getYSize();
        longside = new RegularAxisImpl("Geodetic longitude", 100.5, resolutionX, grid.getXSize(), true);
        latside = new RegularAxisImpl("Geodetic latitude", 20.5, resolutionY, grid.getYSize(), false);
        
        assertTrue(grid.getXAxis().equals(longside));
        assertTrue(grid.getYAxis().equals(latside));
    }

}
