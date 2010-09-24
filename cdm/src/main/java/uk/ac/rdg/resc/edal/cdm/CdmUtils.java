/*
 * Copyright (c) 2010 The University of Reading
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
 */

package uk.ac.rdg.resc.edal.cdm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.geotoolkit.metadata.iso.extent.DefaultGeographicBoundingBox;
import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.chrono.JulianChronology;
import org.opengis.coverage.grid.GridCoordinates;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;
import ucar.nc2.constants.AxisType;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dataset.CoordinateAxis1DTime;
import ucar.nc2.dataset.CoordinateAxis2D;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.VariableDS;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDataset;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.TypedDatasetFactory;
import ucar.unidata.geoloc.LatLonPoint;
import ucar.unidata.geoloc.LatLonRect;
import uk.ac.rdg.resc.edal.coverage.domain.Domain;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.coverage.grid.RectilinearGrid;
import uk.ac.rdg.resc.edal.coverage.grid.ReferenceableAxis;
import uk.ac.rdg.resc.edal.coverage.grid.RegularAxis;
import uk.ac.rdg.resc.edal.coverage.grid.RegularGrid;
import uk.ac.rdg.resc.edal.coverage.grid.impl.RectilinearGridImpl;
import uk.ac.rdg.resc.edal.coverage.grid.impl.ReferenceableAxisImpl;
import uk.ac.rdg.resc.edal.coverage.grid.impl.RegularAxisImpl;
import uk.ac.rdg.resc.edal.coverage.grid.impl.RegularGridImpl;
import uk.ac.rdg.resc.edal.geometry.HorizontalPosition;
import uk.ac.rdg.resc.edal.time.AllLeapChronology;
import uk.ac.rdg.resc.edal.time.NoLeapChronology;
import uk.ac.rdg.resc.edal.time.ThreeSixtyDayChronology;
import uk.ac.rdg.resc.edal.time.TimeUtils;
import uk.ac.rdg.resc.edal.util.CollectionUtils;

/**
 * Contains static helper methods for reading data and metadata from NetCDF files,
 * OPeNDAP servers and other data sources using the Unidata Common Data Model.
 * @author Jon
 */
public final class CdmUtils
{
    private static final Logger logger = LoggerFactory.getLogger(CdmUtils.class);

    /** Map of CF identifiers for calendar systems to joda-time Chronologies */
    private static final Map<String, Chronology> CHRONOLOGIES = CollectionUtils.newHashMap();

    static
    {
        CHRONOLOGIES.put("julian", JulianChronology.getInstanceUTC());
        CHRONOLOGIES.put("360_day", ThreeSixtyDayChronology.getInstanceUTC());
        CHRONOLOGIES.put("all_leap", AllLeapChronology.getInstanceUTC());
        CHRONOLOGIES.put("366_day", AllLeapChronology.getInstanceUTC());
        CHRONOLOGIES.put("noleap", NoLeapChronology.getInstanceUTC());
        CHRONOLOGIES.put("365_day", NoLeapChronology.getInstanceUTC());
    }

    /** Enforce non-instantiability */
    private CdmUtils() { throw new AssertionError(); }

    /**
     * Creates a {@link ReferenceableAxis} from the given {@link CoordinateAxis1D}.
     */
    public static ReferenceableAxis createReferenceableAxis(CoordinateAxis1D axis)
    {
        if (axis == null) throw new NullPointerException();
        boolean isLongitude = axis.getAxisType() == AxisType.Lon;
        String name = axis.getName();
        // TODO: generate coordinate system axes if appropriate
        if (axis.isRegular())
        {
            return new RegularAxisImpl(name, axis.getStart(),
                    axis.getIncrement(), (int)axis.getSize(), isLongitude);
        }
        else
        {
            return new ReferenceableAxisImpl(name, axis.getCoordValues(),
                    isLongitude);
        }
    }

    /**
     * Creates a two-dimensional referenceable grid from the given grid
     * coordinate system.  Will return more specific subclasses
     * ({@link RectilinearGrid} or {@link RegularGrid}) if appropriate for the
     * passed-in coordinate system.  The grid's coordinate system will be a
     * WGS84 longitude-latitude system.
     * @todo May want to be careful about datum shifts - model data is often
     * in spherical coordinates, not strict WGS84
     */
    public static HorizontalGrid createHorizontalGrid(GridCoordSystem coordSys)
    {
        CoordinateAxis xAxis = coordSys.getXHorizAxis();
        CoordinateAxis yAxis = coordSys.getYHorizAxis();
        boolean isLatLon = xAxis.getAxisType() == AxisType.Lon &&
                           yAxis.getAxisType() == AxisType.Lat;

        if (xAxis instanceof CoordinateAxis1D && yAxis instanceof CoordinateAxis1D)
        {
            ReferenceableAxis xRefAxis = createReferenceableAxis((CoordinateAxis1D)xAxis);
            ReferenceableAxis yRefAxis = createReferenceableAxis((CoordinateAxis1D)yAxis);
            if (isLatLon)
            {
                CoordinateReferenceSystem crs84 = DefaultGeographicCRS.WGS84;
                // We can create a RectilinearGrid in lat-lon space
                if (xRefAxis instanceof RegularAxis && yRefAxis instanceof RegularAxis)
                {
                    // We can create a regular grid
                    return new RegularGridImpl(
                        (RegularAxis)xRefAxis,
                        (RegularAxis)yRefAxis,
                        crs84
                    );
                }
                else
                {
                    // Axes are not both regular
                    return new RectilinearGridImpl(xRefAxis, yRefAxis, crs84);
                }
            }
            else
            {
                // Axes are not latitude and longitude so we need to create a
                // ReferenceableGrid that uses the coordinate system's
                // Projection object to convert from x and y to lat and lon
                return new ProjectedGrid(coordSys);
            }
        }
        else if (xAxis instanceof CoordinateAxis2D && yAxis instanceof CoordinateAxis2D)
        {
            // The axis must be 2D so we have to create look-up tables
            if (!isLatLon)
            {
                throw new UnsupportedOperationException("Can't create a HorizontalGrid" +
                    " from 2D coordinate axes that are not longitude and latitude.");
            }
            return LookUpTableGrid.generate(coordSys);
        }
        else
        {
            // Shouldn't get here
            throw new IllegalStateException("Inconsistent axis types");
        }
    }

    /** Gets a GridDataset from the given NetcdfDataset */
    public static GridDataset getGridDataset(NetcdfDataset nc) throws IOException
    {
        return (GridDataset)TypedDatasetFactory.open(FeatureType.GRID,
            nc, null, null);
    }

    /**
     * Estimates the optimum {@link DataReadingStrategy} from the given
     * NetcdfDataset.  Essentially, if the data are remote (e.g. OPeNDAP) or
     * compressed, this will return {@link DataReadingStrategy#BOUNDING_BOX},
     * which makes a single i/o call, minimizing the overhead.  If the data
     * are local and uncompressed this will return {@link DataReadingStrategy#PIXEL_BY_PIXEL},
     * which minimize the amount of data read
     * @param nc The NetcdfDataset from which data will be read.
     * @return an optimum DataReadingStrategy for reading from the dataset
     */
    public static DataReadingStrategy getOptimumDataReadingStrategy(NetcdfDataset nc)
    {
        String fileType = nc.getFileTypeId();
        return "netCDF".equals(fileType) || "HDF4".equals(fileType)
            ? DataReadingStrategy.PIXEL_BY_PIXEL
            : DataReadingStrategy.BOUNDING_BOX;
    }

    /**
     * Converts the given LatLonRect to a GeographicBoundingBox.
     * @todo Should probably be an Extent or a BoundingBox (I think Extent
     * is more accurate - see the GeoAPI spec document.  Extents do not cross
     * the anti-meridian).  Also do we need to return a more precise CRS?
     * GeographicBoundingBox is deliberately approximate so doesn't use a CRS.
     */
    public static GeographicBoundingBox getBbox(LatLonRect latLonRect)
    {
        // TODO: should take into account the cell bounds
        LatLonPoint lowerLeft = latLonRect.getLowerLeftPoint();
        LatLonPoint upperRight = latLonRect.getUpperRightPoint();
        double minLon = lowerLeft.getLongitude();
        double maxLon = upperRight.getLongitude();
        double minLat = lowerLeft.getLatitude();
        double maxLat = upperRight.getLatitude();
        // Correct the bounding box in case of mistakes or in case it
        // crosses the date line
        if (latLonRect.crossDateline() || minLon >= maxLon)
        {
            minLon = -180.0;
            maxLon = 180.0;
        }
        if (minLat >= maxLat)
        {
            minLat = -90.0;
            maxLat = 90.0;
        }
        // Sometimes the bounding boxes can be NaN, e.g. for a VerticalPerspectiveView
        // that encompasses more than the Earth's disc
        minLon = Double.isNaN(minLon) ? -180.0 : minLon;
        minLat = Double.isNaN(minLat) ?  -90.0 : minLat;
        maxLon = Double.isNaN(maxLon) ?  180.0 : maxLon;
        maxLat = Double.isNaN(maxLat) ?   90.0 : maxLat;
        return new DefaultGeographicBoundingBox(minLon, maxLon, minLat, maxLat);
    }

    /**
     * Gets List of DateTimes representing the timesteps of the given coordinate system,
     * in an appropriate {@link Chronology}.  (Chronologies represent the
     * calendar system.)
     * @param coordSys The coordinate system containing the time information
     * @return List of TimestepInfo objects, or an empty list if the coordinate
     * system has no time axis
     * @throws IllegalArgumentException if the calendar system of the time axis
     * cannot be handled.
     */
    public static List<DateTime> getTimesteps(CoordinateAxis1DTime timeAxis)
    {
        Attribute cal = timeAxis.findAttribute("calendar");
        String calString = cal == null ? null : cal.getStringValue().toLowerCase();
        if (calString == null || calString.equals("gregorian") || calString.equals("standard"))
        {
            List<DateTime> timesteps = new ArrayList<DateTime>();
            // Use the Java NetCDF library's built-in date parsing code
            for (Date date : timeAxis.getTimeDates())
            {
                timesteps.add(new DateTime(date, DateTimeZone.UTC));
            }
            return timesteps;
        }
        else
        {
            Chronology chron = CHRONOLOGIES.get(calString);
            if (chron == null)
            {
                throw new IllegalArgumentException("The calendar system "
                    + cal.getStringValue() + " cannot be handled");
            }
            return getTimestepsForChronology(timeAxis, chron);
        }
    }

    /**
     * Creates a list of DateTimes in a non-standard calendar system.  All of the
     * DateTimes will have a zero time zone offset (i.e. UTC).
     */
    private static List<DateTime> getTimestepsForChronology(CoordinateAxis1DTime timeAxis, Chronology chron)
    {
        // Get the units of the time axis, e.g. "days since 1970-1-1 0:0:0"
        String timeAxisUnits = timeAxis.getUnitsString();
        int indexOfSince = timeAxisUnits.indexOf(" since ");

        // Get the units of the time axis, e.g. "days", "months"
        String unitIncrement = timeAxisUnits.substring(0, indexOfSince);
        // Get the number of milliseconds this represents
        long unitLength = TimeUtils.getUnitLengthMillis(unitIncrement);

        // Get the base date of the axis, e.g. "1970-1-1 0:0:0"
        String baseDateTimeString = timeAxisUnits.substring(indexOfSince + " since ".length());
        DateTime baseDateTime = TimeUtils.parseUdunitsTimeString(baseDateTimeString, chron);

        // Now create and return the axis values
        List<DateTime> timesteps = new ArrayList<DateTime>();
        for (double val : timeAxis.getCoordValues())
        {
            timesteps.add(baseDateTime.plus((long)(unitLength * val)));
        }

        return timesteps;
    }

    /**
     * Reads a set of points at a given time and elevation from the given
     * GridDatatype.  This method will internally create a {@link HorizontalGrid}
     * object with each invocation; if you expect to call this method multiple times,
     * greater efficiency may be gained by creating the HorizontalGrid once and
     * calling {@link #readHorizontalPoints(ucar.nc2.dataset.NetcdfDataset,
     * java.lang.String, uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid, int,
     * int, uk.ac.rdg.resc.edal.coverage.domain.Domain)}.
     * @param nc The (already-opened) NetcdfDataset from which we'll read data
     * @param varId The ID of the variable from which we will read data
     * @param tIndex The time index, ignored if the grid has no time axis
     * @param zIndex The elevation index, ignored if the grid has no elevation axis
     * @param targetDomain The list of horizontal points for which we need data
     * @return a List of floating point numbers, one for each point in the
     * {@code targetDomain}, in the same order.  Missing values (e.g. land pixels
     * in oceanography data} are represented as nulls.
     * @throws IllegalArgumentException if there is no variable in the dataset
     * with the id {@code varId}.
     * @throws IOException if there was an error reading data from the data source
     */
    public static List<Float> readHorizontalPoints(NetcdfDataset nc, String varId,
            int tIndex, int zIndex, Domain<HorizontalPosition> targetDomain)
            throws IOException
    {
        GridDatatype grid = getGridDatatype(nc, varId);
        HorizontalGrid sourceGrid = createHorizontalGrid(grid.getCoordinateSystem());
        DataReadingStrategy strategy = getOptimumDataReadingStrategy(nc);
        return strategy.readData(tIndex, zIndex, sourceGrid, targetDomain, grid);
    }

    /**
     * Reads a set of points at a given time and elevation from the given
     * GridDatatype.  Use this method if you already have a {@link HorizontalGrid}
     * object created for the variable in question, otherwise use
     * {@link #readHorizontalPoints(ucar.nc2.dataset.NetcdfDataset, java.lang.String,
     * int, int, uk.ac.rdg.resc.edal.coverage.domain.Domain)}.
     * @param nc The (already-opened) NetcdfDataset from which we'll read data
     * @param varId The ID of the variable from which we will read data
     * @param sourceGrid object that maps between real-world and grid coordinates
     * in the source data grid
     * @param tIndex The time index, ignored if the grid has no time axis
     * @param zIndex The elevation index, ignored if the grid has no elevation axis
     * @param targetDomain The list of horizontal points for which we need data
     * @return a List of floating point numbers, one for each point in the
     * {@code targetDomain}, in the same order.  Missing values (e.g. land pixels
     * in oceanography data} are represented as nulls.
     * @throws IllegalArgumentException if there is no variable in the dataset
     * with the id {@code varId}.
     * @throws IOException if there was an error reading data from the data source
     */
    public static List<Float> readHorizontalPoints(NetcdfDataset nc, String varId,
            HorizontalGrid sourceGrid, int tIndex, int zIndex,
            Domain<HorizontalPosition> targetDomain)
            throws IOException
    {
        GridDatatype grid = getGridDatatype(nc, varId);
        DataReadingStrategy strategy = getOptimumDataReadingStrategy(nc);
        return strategy.readData(tIndex, zIndex, sourceGrid, targetDomain, grid);
    }

    public static GridDatatype getGridDatatype(NetcdfDataset nc, String varId)
            throws IOException
    {
        GridDataset gd = getGridDataset(nc);
        if (gd == null)
        {
            throw new IllegalArgumentException("Dataset does not contain gridded data");
        }
        GridDatatype grid = gd.findGridDatatype(varId);
        if (grid == null)
        {
            throw new IllegalArgumentException("No variable with name " + varId);
        }
        return grid;
    }

    /**
     * Reads a timeseries of points from the given GridDatatype at a given
     * elevation and xy location
     * @param nc The (already-opened) NetcdfDataset from which we'll read data
     * @param varId The ID of the variable from which we will read data
     * @param horizGrid object that maps between real-world and grid coordinates
     * in the source data grid
     * @param tIndices The list of indices along the time axis
     * @param zIndex The elevation index, ignored if the grid has no elevation axis
     * @param xy The horizontal location of the required timeseries
     * @return a list of floating-point numbers, one for each of the time indices.
     * Missing values (e.g. land pixels in oceanography data} are represented as nulls.
     * @throws IOException if there was an error reading data from the data source
     */
    public static List<Float> readTimeseries(NetcdfDataset nc, String varId,
            HorizontalGrid horizGrid, List<Integer> tIndices,
            int zIndex, HorizontalPosition xy)
            throws IOException
    {
        GridDatatype grid = getGridDatatype(nc, varId);
        GridCoordinates gridCoords = horizGrid.findNearestGridPoint(xy);
        if (gridCoords == null)
        {
            // The point is outside the domain of the coord sys, so return
            // a list of nulls
            return Collections.nCopies(tIndices.size(), null);
        }

        int i = gridCoords.getCoordinateValue(0);
        int j = gridCoords.getCoordinateValue(1);
        int firstTIndex = tIndices.get(0);
        int lastTIndex = tIndices.get(tIndices.size() - 1);

        RangesList rangesList = new RangesList(grid);
        rangesList.setTRange(firstTIndex, lastTIndex);
        rangesList.setZRange(zIndex, zIndex);
        rangesList.setYRange(j, j);
        rangesList.setXRange(i, i);

        VariableDS enhancedVar = grid.getVariable();
        Variable origVar = enhancedVar.getOriginalVariable();
        // We read data for the whole time range.  This may mean grabbing
        // data we don't need.
        // TODO: use a datareadingstrategy here to read point-by-point for
        // local files?
        Array arr = readVariable(origVar, rangesList);

        // Copy the data to the required array, discarding the points we
        // don't need
        List<Float> tsData = new ArrayList<Float>();
        Index index = arr.getIndex();
        index.set(new int[index.getRank()]);
        for (int tIndex : tIndices)
        {
            int tIndexOffset = tIndex - firstTIndex;
            if (tIndexOffset < 0) tIndexOffset = 0; // This will happen if the layer has no t axis
            index.setDim(rangesList.getTAxisIndex(), tIndexOffset);
            float val = arr.getFloat(index);
            // Apply any scale-offset-missing conversions
            val = (float)enhancedVar.convertScaleOffsetMissing(val);
            // Replace missing values with nulls
            tsData.add(Float.isNaN(val) ? null : val);
        }

        return tsData;
    }

    /**
     * Reads a chunk of data from the given variable.  This method avoids the
     * need for catching InvalidRangeExceptions.  Such an exception will not be
     * thrown by this method unless the given RangesList was constructed for a
     * different variable shape.
     * @param var The variable from which to read data
     * @param ranges The list of ranges describing the data chunk
     * @return array of data
     * @throws IOException if there was an io error reading from the variable
     * @throws IllegalArgumentException if {@code ranges} is not valid for the
     * variable.
     */
    static Array readVariable(Variable var, RangesList ranges) throws IOException
    {
        if (var == null) throw new NullPointerException("var");
        if (ranges == null) throw new NullPointerException("ranges");
        try
        {
            return var.read(ranges.getRanges());
        }
        catch(InvalidRangeException ire)
        {
            throw new IllegalArgumentException(ire);
        }
    }

    /**
     * Gets the values of the z axis of the given coordinate system.  Returns
     * an empty list of the given coordinate system has no vertical axis.
     * , with sign reversed if zPositive == false.
     * Returns an empty list if zAxis is null.
     */
    public static List<Double> getZValues(GridCoordSystem coordSys)
    {
        if (coordSys == null) throw new NullPointerException("coordSys");

        CoordinateAxis1D zAxis = coordSys.getVerticalAxis();
        if (zAxis == null) return Collections.emptyList();

        List<Double> zValues = new ArrayList<Double>();
        boolean isPositive = coordSys.isZPositive();
        boolean isPressure = zAxis.getAxisType() == AxisType.Pressure;
        for (double zVal : zAxis.getCoordValues())
        {
            // Pressure axes have "positive = down" but we don't want to
            // reverse the sign of the values.
            if (isPositive || isPressure) zValues.add(zVal);
            else zValues.add(-zVal); // This is probably a depth axis
        }
        return zValues;
    }

    /**
     * @return the value of the standard_name attribute of the variable,
     * or the long_name if it does not exist, or the unique id if neither of
     * these attributes exist.
     */
    public static String getVariableTitle(Variable var)
    {
        Attribute stdNameAtt = var.findAttributeIgnoreCase("standard_name");
        if (stdNameAtt == null || stdNameAtt.getStringValue().trim().equals(""))
        {
            Attribute longNameAtt = var.findAttributeIgnoreCase("long_name");
            if (longNameAtt == null || longNameAtt.getStringValue().trim().equals(""))
            {
                return var.getName();
            }
            else
            {
                return longNameAtt.getStringValue();
            }
        }
        else
        {
            return stdNameAtt.getStringValue();
        }
    }

}
