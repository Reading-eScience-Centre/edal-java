/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.rdg.resc.edal.cdm.coverage;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dt.GridDatatype;
import uk.ac.rdg.resc.edal.Unit;
import uk.ac.rdg.resc.edal.cdm.coverage.grid.NcGridValuesMatrix;
import uk.ac.rdg.resc.edal.cdm.util.CdmUtils;
import uk.ac.rdg.resc.edal.coverage.GridCoverage2D;
import uk.ac.rdg.resc.edal.coverage.Record;
import uk.ac.rdg.resc.edal.coverage.grid.GridValuesMatrix;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.coverage.grid.RegularGrid;
import uk.ac.rdg.resc.edal.coverage.grid.impl.RegularGridImpl;
import uk.ac.rdg.resc.edal.coverage.impl.AbstractGridCoverage2D;
import uk.ac.rdg.resc.edal.coverage.impl.DataReadingStrategy;
import uk.ac.rdg.resc.edal.coverage.metadata.ScalarMetadata;
import uk.ac.rdg.resc.edal.coverage.metadata.impl.ScalarMetadataImpl;
import uk.ac.rdg.resc.edal.position.impl.HorizontalPositionImpl;
import uk.ac.rdg.resc.edal.util.CollectionUtils;

/**
 * A {@link GridCoverage2D} that is backed by a disk- (or network-)based NetCDF dataset.
 * Currently all datatypes will appear as Floats - TODO: relax this limitation.
 * @author Jon
 */
public class NcGridCoverage extends AbstractGridCoverage2D
{
    private final String location;
    private final int zIndex;
    private final int tIndex;
    
    private final String description;
    private final Set<String> memberNames;
    private final HorizontalGrid horizGrid;
    private final ScalarMetadata metadata;
    private final DataReadingStrategy strategy;
    
    /**
     * Creates an NcGridCoverage that wraps the given variable in the given
     * location.  If time and elevation axes are present in a variable, only the
     * data from the first timestep and level are accessible.
     */
    public NcGridCoverage(String location, String varId)
    {
        this(location, varId, 0, 0);
    }
    
    /**
     * Creates an NcGridCoverage that wraps all the variables in the given
     * location, using the given elevation and time indices.
     */
    public NcGridCoverage(String location, int zIndex, int tIndex)
    {
        this(location, null, zIndex, tIndex);
    }
    
    /**
     * Creates an NcGridCoverage that wraps all the given variables in the given
     * location, using the given elevation and time indices
     */
    public NcGridCoverage(String location, String varId, int zIndex, int tIndex)
    {
        NetcdfDataset nc = null;
        try
        {
            // TODO: could use NetCDF dataset cache for NcML aggregations
            nc = NetcdfDataset.openDataset(location);
            
            // getGridDatatype() throws an exception if the varId is not valid
            GridDatatype grid = CdmUtils.getGridDatatype(nc, varId);
            
            this.horizGrid = CdmUtils.createHorizontalGrid(grid.getCoordinateSystem());
            this.memberNames = CollectionUtils.setOf(varId);
            this.description = grid.getDescription();
            
            this.metadata = new ScalarMetadataImpl(
                varId,
                this.description,
                CdmUtils.getPhenomenon(grid.getVariable()),
                Unit.getUnit(grid.getUnitsString()),
                // TODO Deal with different data types.  This is not totally trivial as we
                // have to think about how to unpack data values (using scale/offset/missing).
                Float.class //CdmUtils.getClass(grid.getDataType());
            );
            
            this.strategy = CdmUtils.getOptimumDataReadingStrategy(nc);
            
            this.location = location;
            this.zIndex = zIndex;
            this.tIndex = tIndex;
        }
        catch(IOException ioe)
        {
            throw new RuntimeException(ioe);
        }
        finally
        {
            CdmUtils.safelyClose(nc);
        }
    }
    
    
    /////////  METADATA PROPERTIES //////////////

    @Override
    public ScalarMetadata getRangeMetadata(String memberName) {
        return this.metadata;
    }

    @Override
    public String getDescription() { return this.description; }

    @Override
    public Set<String> getMemberNames() { return this.memberNames; }

    @Override
    public HorizontalGrid getDomain() { return this.horizGrid; }
    
    @Override
    public GridValuesMatrix<?> getGridValues(final String memberName) {
        return new NcGridValuesMatrix(horizGrid, location, memberName, zIndex, tIndex);
    }
    
    
    /////////  DATA READING  //////////////

    @Override
    protected DataReadingStrategy getDataReadingStrategy() {
        return this.strategy;
    }
    
    public static void main(String[] args)
    {
        String var = "TMP";
        GridCoverage2D ncCov = new NcGridCoverage("C:\\Godiva2_data\\FOAM_ONE\\FOAM_20100130.0.nc", "TMP");
        Record val = ncCov.evaluate(new HorizontalPositionImpl(0, 0, DefaultGeographicCRS.WGS84));
        System.out.println(val.getValue(var));
        
        List<?> vals = ncCov.getValues(var);
        
        for (int i = 0; i < vals.size(); i+= 720)
        {
            System.out.println(vals.get(i));
        }
        
        RegularGrid grid = new RegularGridImpl(-180, -90, 180, 90, DefaultGeographicCRS.WGS84, 100, 100);
        GridCoverage2D subset = ncCov.extractGridCoverage(grid, CollectionUtils.setOf(var));
        
        System.out.println(subset.getDomain().size());
    }

}
