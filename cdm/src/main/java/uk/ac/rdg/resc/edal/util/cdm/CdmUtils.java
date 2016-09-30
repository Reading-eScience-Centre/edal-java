/**
 * Copyright (c) 2010 The University of Reading
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the University of Reading, nor the names of the
 * authors or contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
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

package uk.ac.rdg.resc.edal.util.cdm;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.oro.io.GlobFilenameFilter;
import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.chrono.GregorianChronology;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.chrono.JulianChronology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.constants.AxisType;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dataset.CoordinateAxis1DTime;
import ucar.nc2.dataset.CoordinateAxis2D;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.grid.GridDataset;
import ucar.nc2.time.CalendarDate;
import uk.ac.rdg.resc.edal.dataset.DataReadingStrategy;
import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.exceptions.DataReadingException;
import uk.ac.rdg.resc.edal.grid.DefinedBoundsAxis;
import uk.ac.rdg.resc.edal.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.grid.LookUpTableGrid;
import uk.ac.rdg.resc.edal.grid.RectilinearGrid;
import uk.ac.rdg.resc.edal.grid.RectilinearGridImpl;
import uk.ac.rdg.resc.edal.grid.ReferenceableAxis;
import uk.ac.rdg.resc.edal.grid.ReferenceableAxisImpl;
import uk.ac.rdg.resc.edal.grid.RegularAxis;
import uk.ac.rdg.resc.edal.grid.RegularAxisImpl;
import uk.ac.rdg.resc.edal.grid.RegularGrid;
import uk.ac.rdg.resc.edal.grid.RegularGridImpl;
import uk.ac.rdg.resc.edal.grid.TimeAxis;
import uk.ac.rdg.resc.edal.grid.TimeAxisImpl;
import uk.ac.rdg.resc.edal.grid.VerticalAxis;
import uk.ac.rdg.resc.edal.grid.VerticalAxisImpl;
import uk.ac.rdg.resc.edal.grid.cdm.CdmTransformedGrid;
import uk.ac.rdg.resc.edal.position.VerticalCrs;
import uk.ac.rdg.resc.edal.position.VerticalCrsImpl;
import uk.ac.rdg.resc.edal.util.Array2D;
import uk.ac.rdg.resc.edal.util.CollectionUtils;
import uk.ac.rdg.resc.edal.util.Extents;
import uk.ac.rdg.resc.edal.util.chronologies.AllLeapChronology;
import uk.ac.rdg.resc.edal.util.chronologies.NoLeapChronology;
import uk.ac.rdg.resc.edal.util.chronologies.ThreeSixtyDayChronology;

/**
 * Contains static helper methods for reading data and metadata from NetCDF
 * files, OPeNDAP servers and other data sources using the Unidata Common Data
 * Model.
 * 
 * @author Jon Blower
 * @author Guy Griffiths
 * @author Mike Grant, Plymouth Marine Labs
 */
public final class CdmUtils {
    private static final Logger log = LoggerFactory.getLogger(CdmUtils.class);

    /*
     * Enforce non-instantiability
     */
    private CdmUtils() {
        throw new AssertionError();
    }

    /**
     * @param ncDataset
     *            The {@link NetcdfDataset} to get a {@link GridDataset} from
     * @return A {@link GridDataset} from the given {@link NetcdfDataset}
     * @throws DataReadingException
     *             If the given {@link NetcdfDataset} doesn't contain any
     *             {@link GridDataset}s
     */
    public static GridDataset getGridDataset(NetcdfDataset ncDataset) throws DataReadingException,
            IOException {
        /*
         * TODO Convert this to return Coverage objects once netcdf-5 is more
         * stable
         */
        GridDataset gridDataset = new GridDataset(ncDataset);
        if (gridDataset.getGrids().size() == 0) {
            throw new DataReadingException("No grids found in underlying NetCDF dataset");
        }
        return gridDataset;
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
        String fileType = nc.getFileTypeId();
        if ("netCDF".equalsIgnoreCase(fileType) || "HDF4".equalsIgnoreCase(fileType)) {
            return DataReadingStrategy.SCANLINE;
        } else {
            try {
                GridDataset gridDataset = getGridDataset(nc);
                for (GridDatatype grid : gridDataset.getGrids()) {
                    HorizontalGrid hGrid = CdmUtils.createHorizontalGrid(grid.getCoordinateSystem());
                    DataType dt = grid.getDataType();
                    long totalsize = hGrid.size() * dt.getSize();
                    /*
                     * If the size of the largest grid is greater than a
                     * fraction of the maximum amount of memory, use a SCANLINE
                     * strategy.
                     * 
                     * Here, we set the multiplier for the maximum memory.
                     * Although it's relatively small, objects are actually
                     * (considerably?) bigger than the (dimension * data type
                     * size) result. Additionally, we need to run everything
                     * else...
                     * 
                     * If we get reports that this is still too large, it can be
                     * lowered, or we can make it configurable.
                     */
                    double multiplier = 0.5;
                    if (totalsize > multiplier * Runtime.getRuntime().maxMemory()) {
                        return DataReadingStrategy.SCANLINE;
                    }
                }
            } catch (DataReadingException | IOException e) {
                /*
                 * Ignore exception - it's either not a GridDataset or we can't
                 * open it. If it's not a GridDataset, we won't be reading it
                 * with a gridded strategy. If we can't open it, we're screwed
                 * either way.
                 */
            }
            return DataReadingStrategy.BOUNDING_BOX;
        }
    }

    /**
     * 
     * @param coordSys
     *            The {@link GridCoordSystem} to create a {@link HorizontalGrid}
     *            from
     * @return two-dimensional referenceable grid from the given grid coordinate
     *         system. Will return more specific subclasses (
     *         {@link RectilinearGrid} or {@link RegularGrid}) if appropriate
     *         for the passed-in coordinate system. The grid's coordinate system
     *         will be a WGS84 longitude-latitude system.
     * 
     *         TODO May want to be careful about datum shifts - model data is
     *         often in spherical coordinates, not strict WGS84
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
                /* We can create a RectilinearGrid in lat-lon space */
                if (xRefAxis instanceof RegularAxis && yRefAxis instanceof RegularAxis) {
                    /* We can create a regular grid */
                    return new RegularGridImpl((RegularAxis) xRefAxis, (RegularAxis) yRefAxis,
                            DefaultGeographicCRS.WGS84);
                } else {
                    /* Axes are not both regular */
                    return new RectilinearGridImpl(xRefAxis, yRefAxis, DefaultGeographicCRS.WGS84);
                }
            } else {
                /*
                 * Axes are not latitude and longitude so we need to create a
                 * ReferenceableGrid that uses the coordinate system's
                 * Projection object to convert from x and y to lat and lon
                 */
                return new CdmTransformedGrid(coordSys);
            }
        } else if (xAxis instanceof CoordinateAxis2D && yAxis instanceof CoordinateAxis2D) {
            /* The axis must be 2D so we have to create look-up tables */
            if (!isLatLon) {
                throw new UnsupportedOperationException("Can't create a HorizontalGrid"
                        + " from 2D coordinate axes that are not longitude and latitude.");
            }
            final CoordinateAxis2D lonAxis = (CoordinateAxis2D) xAxis;
            final CoordinateAxis2D latAxis = (CoordinateAxis2D) yAxis;

            Array2D<Number> lonVals = get2DCoordinateValues(lonAxis);
            Array2D<Number> latVals = get2DCoordinateValues(latAxis);

            return LookUpTableGrid.generate(lonVals, latVals);
        } else {
            /* Shouldn't get here */
            throw new IllegalStateException("Inconsistent axis types");
        }
    }

    public static Array2D<Number> get2DCoordinateValues(final CoordinateAxis2D axis) {
        return new Array2D<Number>(axis.getShape(0), axis.getShape(1)) {
            @Override
            public void set(Number value, int... coords) {
                throw new UnsupportedOperationException("This Array2D is immutable");
            }

            @Override
            public Number get(int... coords) {
                return axis.getCoordValue(coords[0], coords[1]);
            }
        };
    }

    /**
     * @param coordSys
     *            the {@link CoordinateAxis1D} to create a {@link VerticalAxis}
     *            from
     * @param isPositive
     *            Whether increasing values
     * @return The resulting {@link VerticalAxis}
     */
    public static VerticalAxis createVerticalAxis(CoordinateAxis1D zAxis, boolean isPositive) {
        if (zAxis == null) {
            return null;
        }
        boolean isPressure = false;
        String units = "";
        List<Double> values = Collections.emptyList();

        isPressure = zAxis.getAxisType() == AxisType.Pressure;
        units = zAxis.getUnitsString();

        List<Double> zValues = new ArrayList<Double>();
        for (double zVal : zAxis.getCoordValues()) {
            zValues.add(zVal);
        }
        values = Collections.unmodifiableList(zValues);

        /*
         * TODO: We're assuming this CRS is not dimensionless.
         */
        VerticalCrs vCrs = new VerticalCrsImpl(units, isPressure, isPositive, false);
        return new VerticalAxisImpl("Vertical Axis", values, vCrs);
    }

    /**
     * Creates a time axis from the given {@link GridCoordSystem}
     * 
     * @param coordSys
     *            the {@link CoordinateAxis1DTime} defining the axis
     * @return a new {@link TimeAxis}
     */
    public static TimeAxis createTimeAxis(CoordinateAxis1DTime timeAxis) {
        if (timeAxis == null) {
            return null;
        }
        Attribute cal = timeAxis.findAttribute("calendar");
        String calString = cal == null ? null : cal.getStringValue().toLowerCase();

        Chronology chron = getChronologyForString(calString);
        if (chron == null) {
            throw new IllegalArgumentException("The calendar system " + cal.getStringValue()
                    + " cannot be handled");
        }
        List<DateTime> timesteps = new ArrayList<DateTime>();
        for (CalendarDate date : timeAxis.getCalendarDates()) {
            timesteps.add(new DateTime(date.getMillis(), chron));
        }
        return new TimeAxisImpl("time", timesteps);
    }

    /*
     * Gets a Chronology from its CF string representation
     */
    private static Chronology getChronologyForString(String chronologyString) {
        if (chronologyString == null || "gregorian".equalsIgnoreCase(chronologyString)
                || "standard".equalsIgnoreCase(chronologyString)) {
            return ISOChronology.getInstanceUTC();
        } else if ("proleptic_gregorian".equalsIgnoreCase(chronologyString)) {
            return GregorianChronology.getInstanceUTC();
        } else if ("julian".equalsIgnoreCase(chronologyString)) {
            return JulianChronology.getInstanceUTC();
        } else if ("noleap".equalsIgnoreCase(chronologyString)
                || "365_day".equalsIgnoreCase(chronologyString)) {
            return NoLeapChronology.getInstanceUTC();
        } else if ("all_leap".equalsIgnoreCase(chronologyString)
                || "366_day".equalsIgnoreCase(chronologyString)) {
            return AllLeapChronology.getInstanceUTC();
        } else if ("360_day".equalsIgnoreCase(chronologyString)) {
            return ThreeSixtyDayChronology.getInstanceUTC();
        }
        return null;
    }

    /**
     * Creates a {@link ReferenceableAxis} from the given
     * {@link CoordinateAxis1D}. Creates a longitude axis if
     * axis.getAxisType()==AxisType.Lon.
     * 
     * @param axis
     *            The {@link CoordinateAxis1D} to convert to a
     *            {@link ReferenceableAxis}
     * @return An equivalent {@link ReferenceableAxis}
     */
    public static ReferenceableAxis<Double> createReferenceableAxis(CoordinateAxis1D axis) {
        return createReferenceableAxis(axis, axis.getAxisType() == AxisType.Lon);
    }

    /**
     * Creates a {@link ReferenceableAxis} from the given
     * {@link CoordinateAxis1D}.
     * 
     * @param axis
     *            The {@link CoordinateAxis1D} to convert to a
     *            {@link ReferenceableAxis}
     * @param isLongitude
     *            true if this is a longitude axis ({@literal i.e.} wraps at 360
     *            degrees).
     * @return The equivalent {@link ReferenceableAxis}
     */
    public static ReferenceableAxis<Double> createReferenceableAxis(CoordinateAxis1D axis,
            boolean isLongitude) {
        if (axis == null)
            throw new NullPointerException();
        String name = axis.getFullName();

        Attribute boundsAttr = axis.findAttribute("bounds");
        if (boundsAttr != null) {
            /*
             * The cell bounds are specified by another variable in the data
             * file.
             */
            List<Double> axisValues = new ArrayList<>();
            List<Extent<Double>> axisBounds = new ArrayList<>();
            for (int i = 0; i < axis.getSize(); i++) {
                double[] coordBounds = axis.getCoordBounds(i);
                if (coordBounds.length != 2) {
                    throw new IllegalArgumentException(
                            "You must specify exactly 2 boundary points for each axis point.  "
                                    + coordBounds.length + " have been supplied");
                }
                double min = coordBounds[0];
                double max = coordBounds[1];
                Extent<Double> cellBounds;
                if (min < max) {
                    cellBounds = Extents.newExtent(min, max);
                } else {
                    cellBounds = Extents.newExtent(max, min);
                }
                axisBounds.add(cellBounds);

                axisValues.add(axis.getCoordValue(i));
            }
            return new DefinedBoundsAxis(name, axisValues, axisBounds, isLongitude);
        } else if (axis.isRegular()) {
            return new RegularAxisImpl(name, axis.getStart(), axis.getIncrement(),
                    (int) axis.getSize(), isLongitude);
        } else {
            double[] primVals = axis.getCoordValues();
            List<Double> valsList = CollectionUtils.listFromDoubleArray(primVals);
            return new ReferenceableAxisImpl(name, valsList, isLongitude);
        }
    }

    /**
     * Expands a glob expression to give a List of paths to files. This method
     * recursively searches directories, allowing for glob expressions like
     * {@code "c:\\data\\200[6-7]\\*\\1*\\A*.nc"}.
     * 
     * @param globExpression
     *            The expression to expand
     * @return a {@link List} of {@link File}s matching the given glob
     *         expression
     */
    public static List<File> expandGlobExpression(String globExpression) {
        /*
         * Check whether the glob expression represents an absolute path.
         * Relative paths may cause unpredictable and platform-dependent
         * behaviour so we give a warning
         */
        File globFile = new File(globExpression);
        if (!globFile.isAbsolute()) {
            log.warn("Using relative path for a dataset.  This may cause unpredictable or platform-dependent behaviour.  The use of absolute paths is recommended");
        }

        /*
         * Break glob pattern into path components. To do this in a reliable and
         * platform-independent way we use methods of the File class, rather
         * than String.split().
         */
        List<String> pathComponents = new ArrayList<String>();
        while (globFile != null) {
            /*
             * We "pop off" the last component of the glob pattern and place it
             * in the first component of the pathComponents List. We therefore
             * ensure that the pathComponents end up in the right order.
             */
            File parent = globFile.getParentFile();
            /*
             * For a top-level directory, getName() returns an empty string,
             * hence we use getPath() in this case
             */
            String pathComponent = parent == null ? globFile.getPath() : globFile.getName();
            pathComponents.add(0, pathComponent);
            globFile = parent;
        }

        /*
         * We must have at least two path components: one directory and one
         * filename or glob expression
         */
        List<File> searchPaths = new ArrayList<File>();
        searchPaths.add(new File(pathComponents.get(0)));
        /* Index of the glob path component */
        int i = 1;

        while (i < pathComponents.size()) {
            FilenameFilter globFilter = new GlobFilenameFilter(pathComponents.get(i));
            List<File> newSearchPaths = new ArrayList<File>();
            /* Look for matches in all the current search paths */
            for (File dir : searchPaths) {
                if (dir.isDirectory()) {
                    /*
                     * Workaround for automounters that don't make filesystems
                     * appear unless they're poked do a listing on
                     * searchpath/pathcomponent whether or not it exists, then
                     * discard the results
                     */
                    new File(dir, pathComponents.get(i)).list();

                    for (File match : dir.listFiles(globFilter)) {
                        newSearchPaths.add(match);
                    }
                }
            }
            /*
             * Next time we'll search based on these new matches and will use
             * the next globComponent
             */
            searchPaths = newSearchPaths;
            i++;
        }

        /*
         * Now we've done all our searching, we'll only retain the files from
         * the list of search paths
         */
        List<File> files = new ArrayList<File>();
        for (File path : searchPaths) {
            if (path.isFile())
                files.add(path);
        }
        return files;
    }
}