package uk.ac.rdg.resc.edal.cdm.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import ucar.nc2.Attribute;
import ucar.nc2.Variable;
import ucar.nc2.constants.AxisType;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dataset.CoordinateAxis1DTime;
import ucar.nc2.dataset.CoordinateAxis2D;
import ucar.nc2.dt.GridCoordSystem;
import uk.ac.rdg.resc.edal.Unit;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.coverage.grid.RectilinearGrid;
import uk.ac.rdg.resc.edal.coverage.grid.ReferenceableAxis;
import uk.ac.rdg.resc.edal.coverage.grid.RegularAxis;
import uk.ac.rdg.resc.edal.coverage.grid.RegularGrid;
import uk.ac.rdg.resc.edal.coverage.grid.TimeAxis;
import uk.ac.rdg.resc.edal.coverage.grid.VerticalAxis;
import uk.ac.rdg.resc.edal.coverage.grid.impl.RectilinearGridImpl;
import uk.ac.rdg.resc.edal.coverage.grid.impl.ReferenceableAxisImpl;
import uk.ac.rdg.resc.edal.coverage.grid.impl.RegularAxisImpl;
import uk.ac.rdg.resc.edal.coverage.grid.impl.RegularGridImpl;
import uk.ac.rdg.resc.edal.coverage.grid.impl.TimeAxisImpl;
import uk.ac.rdg.resc.edal.coverage.grid.impl.VerticalAxisImpl;
import uk.ac.rdg.resc.edal.position.CalendarSystem;
import uk.ac.rdg.resc.edal.position.TimePosition;
import uk.ac.rdg.resc.edal.position.VerticalCrs;
import uk.ac.rdg.resc.edal.position.VerticalCrs.PositiveDirection;
import uk.ac.rdg.resc.edal.position.impl.TimePositionImpl;
import uk.ac.rdg.resc.edal.position.impl.VerticalCrsImpl;
import uk.ac.rdg.resc.edal.util.CollectionUtils;

/**
 * Contains static helper methods for reading data and metadata from NetCDF
 * files, OPeNDAP servers and other data sources using the Unidata Common Data
 * Model.
 * 
 * @author Jon
 * @author Guy Griffiths
 */
public final class CdmUtils {
    /**
     * @return the value of the standard_name attribute of the variable, or the
     *         long_name if it does not exist, or the unique id if neither of
     *         these attributes exist.
     */
    public static String getVariableTitle(Variable var) {
        Attribute stdNameAtt = var.findAttributeIgnoreCase("standard_name");
        if (stdNameAtt == null || stdNameAtt.getStringValue().trim().equals("")) {
            Attribute longNameAtt = var.findAttributeIgnoreCase("long_name");
            if (longNameAtt == null || longNameAtt.getStringValue().trim().equals("")) {
                return var.getName();
            } else {
                return longNameAtt.getStringValue();
            }
        } else {
            return stdNameAtt.getStringValue();
        }
    }

    /**
     * Creates a two-dimensional referenceable grid from the given grid
     * coordinate system. Will return more specific subclasses (
     * {@link RectilinearGrid} or {@link RegularGrid}) if appropriate for the
     * passed-in coordinate system. The grid's coordinate system will be a WGS84
     * longitude-latitude system.
     * 
     * @todo May want to be careful about datum shifts - model data is often in
     *       spherical coordinates, not strict WGS84
     */
    public static HorizontalGrid createHorizontalGrid(GridCoordSystem coordSys) {
        CoordinateAxis xAxis = coordSys.getXHorizAxis();
        CoordinateAxis yAxis = coordSys.getYHorizAxis();
        boolean isLatLon = xAxis.getAxisType() == AxisType.Lon && yAxis.getAxisType() == AxisType.Lat;

        if (xAxis instanceof CoordinateAxis1D && yAxis instanceof CoordinateAxis1D) {
            ReferenceableAxis<Double> xRefAxis = createReferenceableAxis((CoordinateAxis1D) xAxis);
            ReferenceableAxis<Double> yRefAxis = createReferenceableAxis((CoordinateAxis1D) yAxis);
            if (isLatLon) {
                CoordinateReferenceSystem crs84 = DefaultGeographicCRS.WGS84;
                // We can create a RectilinearGrid in lat-lon space
                if (xRefAxis instanceof RegularAxis && yRefAxis instanceof RegularAxis) {
                    // We can create a regular grid
                    return new RegularGridImpl((RegularAxis) xRefAxis, (RegularAxis) yRefAxis, crs84);
                } else {
                    // Axes are not both regular
                    return new RectilinearGridImpl(xRefAxis, yRefAxis, crs84);
                }
            } else {
                // Axes are not latitude and longitude so we need to create a
                // ReferenceableGrid that uses the coordinate system's
                // Projection object to convert from x and y to lat and lon
                throw new UnsupportedOperationException("Axes not lat lon - not yet supported");
                // TODO IMPLEMENT PROJECTEDGRID
                // return new ProjectedGrid(coordSys);
            }
        } else if (xAxis instanceof CoordinateAxis2D && yAxis instanceof CoordinateAxis2D) {
            // The axis must be 2D so we have to create look-up tables
            if (!isLatLon) {
                throw new UnsupportedOperationException("Can't create a HorizontalGrid"
                        + " from 2D coordinate axes that are not longitude and latitude.");
            }
            // TODO IMPLEMENT LOOKUPTABLEGRID
            throw new UnsupportedOperationException("Need a lookup grid - not yet supported");
            // return LookUpTableGrid.generate(coordSys);
        } else {
            // Shouldn't get here
            throw new IllegalStateException("Inconsistent axis types");
        }
    }

    /**
     * Creates a {@link VerticalAxis} from a given {@link GridCoordSystem} object.
     * @param coordSys
     * @return
     */
    public static VerticalAxis createVerticalAxis(GridCoordSystem coordSys) {
        CoordinateAxis1D zAxis = coordSys.getVerticalAxis();
        boolean isPositive = coordSys.isZPositive();
        boolean isPressure = false;
        String units = "";
        List<Double> values = Collections.emptyList();

        if (zAxis != null) {
            isPressure = zAxis.getAxisType() == AxisType.Pressure;
            units = zAxis.getUnitsString();

            List<Double> zValues = CollectionUtils.newArrayList();
            for (double zVal : zAxis.getCoordValues()) {
                // Pressure axes have "positive = down" but we don't want to
                // reverse the sign of the values.
                if (isPositive || isPressure)
                    zValues.add(zVal);
                else
                    zValues.add(-zVal); // This is probably a depth axis
            }
            values = Collections.unmodifiableList(zValues);
        } else {
            return null;
        }

        VerticalCrs vCrs = new VerticalCrsImpl(Unit.getUnit(units), isPositive ? PositiveDirection.UP
                : PositiveDirection.DOWN, isPressure);
        return new VerticalAxisImpl("Vertical Axis", values, vCrs);
    }
    
    public static TimeAxis createTimeAxis(GridCoordSystem coordSys) {
        if (!coordSys.hasTimeAxis1D()) {
            return null;
        }
        CoordinateAxis1DTime timeAxis = coordSys.getTimeAxis1D();

        Attribute cal = timeAxis.findAttribute("calendar");
        String calString = cal == null ? null : cal.getStringValue().toLowerCase();

        if (calString == null || calString.equals("gregorian") || calString.equals("standard")) {
            List<TimePosition> timesteps = new ArrayList<TimePosition>();
            // Use the Java NetCDF library's built-in date parsing code
            for (Date date : timeAxis.getTimeDates()) {
                timesteps.add(new TimePositionImpl(date.getTime(), CalendarSystem.CAL_ISO_8601));
            }
            return new TimeAxisImpl("time", timesteps);
        } else {
            CalendarSystem calSys = CalendarSystem.valueOf(calString);
            if (calSys == null) {
                throw new IllegalArgumentException("The calendar system " + cal.getStringValue() + " cannot be handled");
            }
            throw new UnsupportedOperationException("Currently only standard calendar systems are supported");
        }
    }


    /**
     * Creates a {@link ReferenceableAxis} from the given
     * {@link CoordinateAxis1D}. Creates a longitude axis if axis.getAxisType()
     * == AxisType.Lon.
     */
    public static ReferenceableAxis<Double> createReferenceableAxis(CoordinateAxis1D axis) {
        return createReferenceableAxis(axis, axis.getAxisType() == AxisType.Lon);
    }

    /**
     * Creates a {@link ReferenceableAxis} from the given
     * {@link CoordinateAxis1D}.
     * 
     * @param isLongitude
     *            true if this is a longitude axis ({@literal i.e.} wraps at 360
     *            degrees).
     */
    public static ReferenceableAxis<Double> createReferenceableAxis(CoordinateAxis1D axis, boolean isLongitude) {
        if (axis == null)
            throw new NullPointerException();
        String name = axis.getName();
        // TODO: generate coordinate system axes if appropriate
        if (axis.isRegular()) {
            return new RegularAxisImpl(name, axis.getStart(), axis.getIncrement(), (int) axis.getSize(), isLongitude);
        } else {
            return new ReferenceableAxisImpl(name, axis.getCoordValues(), isLongitude);
        }
    }

}
