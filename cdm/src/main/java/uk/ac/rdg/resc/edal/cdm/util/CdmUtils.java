/*******************************************************************************
 * Copyright (c) 2011 The University of Reading
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
 *******************************************************************************/
package uk.ac.rdg.resc.edal.cdm.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;
import ucar.nc2.constants.AxisType;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dataset.CoordinateAxis1DTime;
import ucar.nc2.dataset.CoordinateAxis2D;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDataset;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;
import uk.ac.rdg.resc.edal.Phenomenon;
import uk.ac.rdg.resc.edal.PhenomenonVocabulary;
import uk.ac.rdg.resc.edal.Unit;
import uk.ac.rdg.resc.edal.UnitVocabulary;
import uk.ac.rdg.resc.edal.cdm.coverage.grid.LookUpTableGrid;
import uk.ac.rdg.resc.edal.cdm.coverage.grid.ProjectedGrid;
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
import uk.ac.rdg.resc.edal.coverage.impl.DataReadingStrategy;
import uk.ac.rdg.resc.edal.position.CalendarSystem;
import uk.ac.rdg.resc.edal.position.TimePosition;
import uk.ac.rdg.resc.edal.position.VerticalCrs;
import uk.ac.rdg.resc.edal.position.VerticalCrs.PositiveDirection;
import uk.ac.rdg.resc.edal.position.impl.TimePositionJoda;
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
     * Returns the Phenomenon that the given variable represents.  If the standard_name
     * attribute is present on the variable, this will return a Phenomenon
     * from the CF Standard Name vocabulary.  If not, the vocabulary will be
     * unknown.
     */
    public static Phenomenon getPhenomenon(Variable var) {
        Attribute stdNameAtt = var.findAttributeIgnoreCase("standard_name");
        if (stdNameAtt == null || stdNameAtt.getStringValue().trim().equals(""))
        {
            Attribute longNameAtt = var.findAttributeIgnoreCase("long_name");
            if (longNameAtt == null || longNameAtt.getStringValue().trim().equals(""))
            {
                return Phenomenon.getPhenomenon(var.getName());
            }
            else
            {
                return Phenomenon.getPhenomenon(longNameAtt.getStringValue());
            }
        }
        else
        {
            return Phenomenon.getPhenomenon(stdNameAtt.getStringValue(),
                    PhenomenonVocabulary.CLIMATE_AND_FORECAST);
        }
    }
    
    /**
     * Returns the runtime Class of the values that will represent the given
     * data type
     */
    public static Class<?> getClass(DataType dt)
    {
        if (dt == DataType.DOUBLE)  return Double.class;
        if (dt == DataType.FLOAT)   return Float.class;
        if (dt == DataType.BYTE)    return Byte.class;
        if (dt == DataType.SHORT)   return Short.class;
        if (dt == DataType.INT)     return Integer.class;
        if (dt == DataType.LONG)    return Long.class;
        if (dt == DataType.STRING)  return String.class;
        if (dt == DataType.BOOLEAN) return Boolean.class;
        throw new IllegalArgumentException("Can't support datatype " + dt.name());
    }

    /**
     * Closes the given NetcdfDataset, checking for null values and swallowing
     * any IOExceptions (we can't do anything about them anyway).
     */
    public static void safelyClose(NetcdfDataset nc)
    {
        if (nc != null)
        {
            try
            {
                nc.close();
            }
            catch(IOException ioe)
            {
                // Do nothing.
            }
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
        boolean isLatLon = xAxis.getAxisType() == AxisType.Lon
                && yAxis.getAxisType() == AxisType.Lat;

        if (xAxis instanceof CoordinateAxis1D && yAxis instanceof CoordinateAxis1D) {
            ReferenceableAxis<Double> xRefAxis = createReferenceableAxis((CoordinateAxis1D) xAxis);
            ReferenceableAxis<Double> yRefAxis = createReferenceableAxis((CoordinateAxis1D) yAxis);
            if (isLatLon) {
                CoordinateReferenceSystem crs84 = DefaultGeographicCRS.WGS84;
                // We can create a RectilinearGrid in lat-lon space
                if (xRefAxis instanceof RegularAxis && yRefAxis instanceof RegularAxis) {
                    // We can create a regular grid
                    return new RegularGridImpl((RegularAxis) xRefAxis, (RegularAxis) yRefAxis,
                            crs84);
                } else {
                    // Axes are not both regular
                    return new RectilinearGridImpl(xRefAxis, yRefAxis, crs84);
                }
            } else {
                // Axes are not latitude and longitude so we need to create a
                // ReferenceableGrid that uses the coordinate system's
                // Projection object to convert from x and y to lat and lon
                return new ProjectedGrid(coordSys);
            }
        } else if (xAxis instanceof CoordinateAxis2D && yAxis instanceof CoordinateAxis2D) {
            // The axis must be 2D so we have to create look-up tables
            if (!isLatLon) {
                throw new UnsupportedOperationException("Can't create a HorizontalGrid"
                        + " from 2D coordinate axes that are not longitude and latitude.");
            }
            return LookUpTableGrid.generate(coordSys);
        } else {
            // Shouldn't get here
            throw new IllegalStateException("Inconsistent axis types");
        }
    }

    /**
     * Creates a {@link VerticalAxis} from a given {@link GridCoordSystem}
     * object.
     * 
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
                zValues.add(zVal);
            }
            values = Collections.unmodifiableList(zValues);
        } else {
            return null;
        }

        VerticalCrs vCrs = new VerticalCrsImpl(Unit.getUnit(units, UnitVocabulary.UDUNITS2),
                isPositive ? PositiveDirection.UP : PositiveDirection.DOWN, isPressure);
        return new VerticalAxisImpl("Vertical Axis", values, vCrs);
    }

    /**
     * Creates a time axis from the given {@link GridCoordSystem}
     * 
     * @param coordSys
     *            the {@link GridCoordSystem} containing the axis definition
     * @return a new {@link TimeAxis}
     */
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
                timesteps.add(new TimePositionJoda(date.getTime(), CalendarSystem.CAL_ISO_8601));
            }
            return new TimeAxisImpl("time", timesteps);
        } else {
            CalendarSystem calSys = CalendarSystem.valueOf(calString);
            if (calSys == null) {
                throw new IllegalArgumentException("The calendar system " + cal.getStringValue()
                        + " cannot be handled");
            }
            throw new UnsupportedOperationException(
                    "Currently only standard calendar systems are supported");
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
    public static ReferenceableAxis<Double> createReferenceableAxis(CoordinateAxis1D axis,
            boolean isLongitude) {
        if (axis == null)
            throw new NullPointerException();
        String name = axis.getName();
        // TODO: generate coordinate system axes if appropriate
        if (axis.isRegular()) {
            return new RegularAxisImpl(name, axis.getStart(), axis.getIncrement(),
                    (int) axis.getSize(), isLongitude);
        } else {
            double[] primVals = axis.getCoordValues();
            List<Double> valsList = CollectionUtils.listFromDoubleArray(primVals);
            return new ReferenceableAxisImpl(name, valsList, isLongitude);
        }
    }

    /**
     * Estimates the optimum {@link DataReadingStrategy} from the given
     * NetcdfDataset. Essentially, if the data are remote (e.g. OPeNDAP) or
     * compressed, this will return {@link DataReadingStrategy#BOUNDING_BOX},
     * which makes a single i/o call, minimizing the overhead. If the data are
     * local and uncompressed this will return
     * {@link DataReadingStrategy#SCANLINE}, which reduces the amount of data
     * read.
     * 
     * @param nc
     *            The NetcdfDataset from which data will be read.
     * @return an optimum DataReadingStrategy for reading from the dataset
     */
    public static DataReadingStrategy getOptimumDataReadingStrategy(NetcdfDataset nc) {
        // TODO: also use the size of the grids as a deciding factor: it can
        // be very slow to read large grids by the BOUNDING_BOX strategy
        String fileType = nc.getFileTypeId();
        return "netCDF".equals(fileType) || "HDF4".equals(fileType) ? DataReadingStrategy.SCANLINE
                : DataReadingStrategy.BOUNDING_BOX;
    }
    
    /**
     * Opens the NetCDF dataset at the given location, using the dataset cache
     * if {@code location} represents an NcML aggregation. We cannot use the
     * cache for OPeNDAP or single NetCDF files because the underlying data may
     * have changed and the NetcdfDataset cache may cache a dataset forever. In
     * the case of NcML we rely on the fact that server administrators ought to
     * have set a "recheckEvery" parameter for NcML aggregations that may change
     * with time. It is desirable to use the dataset cache for NcML aggregations
     * because they can be time-consuming to assemble and we don't want to do
     * this every time a map is drawn.
     * 
     * @param location
     *            The location of the data: a local NetCDF file, an NcML
     *            aggregation file or an OPeNDAP location, {@literal i.e.}
     *            anything that can be passed to
     *            NetcdfDataset.openDataset(location).
     * @return a {@link NetcdfDataset} object for accessing the data at the
     *         given location.
     * @throws IOException
     *             if there was an error reading from the data source.
     */
    public static NetcdfDataset openDataset(String location) throws IOException {
        NetcdfDataset nc;
        if (location.endsWith(".xml") || location.endsWith(".ncml")) {
            // We use the cache of NetcdfDatasets to read NcML aggregations
            // as they can be time-consuming to put together. If the underlying
            // data can change we rely on the server admin setting the
            // "recheckEvery" parameter in the aggregation file.
            nc = NetcdfDataset.acquireDataset(location, null);
        } else {
            // For local single files and OPeNDAP datasets we don't use the
            // cache, to ensure that we are always reading the most up-to-date
            // data. There is a small possibility that the dataset cache will
            // have swallowed up all available file handles, in which case
            // the server admin will need to increase the number of available
            // handles on the server.
            nc = NetcdfDataset.openDataset(location);
        }
        return nc;
    }
    
    /** Closes the given dataset, logging any exceptions at debug level */
    public static void closeDataset(NetcdfDataset nc) {
        if (nc == null)
            return;
        try {
            nc.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("IOException closing " + nc.getLocation());
        }
    }

    public static GridDatatype getGridDatatype(NetcdfDataset ncDataset, String varId) throws IOException {
        GridDataset gd = getGridDataset(ncDataset);
        if (gd == null) {
            throw new IllegalArgumentException("Dataset does not contain gridded data");
        }
        GridDatatype grid = gd.findGridDatatype(varId);
        if (grid == null) {
            throw new IllegalArgumentException("No variable with name " + varId);
        }
        return grid;
    }

    /** Gets a GridDataset from the given NetcdfDataset */
    public static GridDataset getGridDataset(NetcdfDataset ncDataset) throws IOException {
        FeatureDataset featureDS = FeatureDatasetFactoryManager.wrap(FeatureType.GRID, ncDataset,
                null, null);
        if (featureDS == null) {
            throw new IOException("No grid datasets found in file: " + ncDataset.getLocation());
        }
        FeatureType fType = featureDS.getFeatureType();
        assert (fType == FeatureType.GRID);
        return (GridDataset) featureDS;
    }
}
