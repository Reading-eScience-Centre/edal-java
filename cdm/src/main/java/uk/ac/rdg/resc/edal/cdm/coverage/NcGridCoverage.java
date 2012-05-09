/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.rdg.resc.edal.cdm.coverage;

import java.io.IOException;
import java.util.Set;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dt.GridDatatype;
import uk.ac.rdg.resc.edal.Phenomenon;
import uk.ac.rdg.resc.edal.Unit;
import uk.ac.rdg.resc.edal.cdm.util.CdmUtils;
import uk.ac.rdg.resc.edal.coverage.GridCoverage2D;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.coverage.impl.AbstractDiskBackedGridCoverage2D;
import uk.ac.rdg.resc.edal.coverage.impl.GridDataSource;
import uk.ac.rdg.resc.edal.util.CollectionUtils;

/**
 * A {@link GridCoverage2D} that is backed by a disk- (or network-)based NetCDF dataset.
 * Currently all datatypes will appear as Floats - TODO: relax this limitation.
 * @author Jon
 */
public class NcGridCoverage extends AbstractDiskBackedGridCoverage2D
{
    private final String location;
    private final int zIndex;
    private final int tIndex;
    
    private final Set<String> memberNames;
    private final HorizontalGrid horizGrid;
    private final String description;
    private final Unit units;
    private final Phenomenon phenom;
    private final Class<?> clazz;
    private final String fileType;
    
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
            // TODO this returns the unit in an unknown vocab: check for UDUNITS
            this.units = Unit.getUnit(grid.getUnitsString());
            this.phenom = CdmUtils.getPhenomenon(grid.getVariable());
            
            // TODO Deal with different data types.  This is not totally trivial as we
            // have to think about how to unpack data values (using scale/offset/missing).
            this.clazz = Float.class; //CdmUtils.getClass(grid.getDataType());
            
            this.fileType = nc.getFileTypeId();
            
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
    protected Class<?> getValueType(String memberName) { return this.clazz; }

    @Override
    protected String getDescription(String memberName) { return this.description; }

    @Override
    protected Unit getUnits(String memberName) { return this.units; }

    @Override
    protected Phenomenon getParameter(String memberName) { return this.phenom; }

    @Override
    public String getDescription() { return this.description; }

    @Override
    public Set<String> getMemberNames() { return this.memberNames; }

    @Override
    public HorizontalGrid getDomain() { return this.horizGrid; }
    
    
    /////////  DATA READING  //////////////

    @Override
    protected GridDataSource<?> openDataSource(String memberName)
    {
        checkMemberName(memberName);
        return new NcGridDataSource(this.location, memberName, this.zIndex, this.tIndex, fileType);
    }
    
}
