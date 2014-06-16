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

import ucar.nc2.Attribute;
import ucar.nc2.constants.AxisType;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dataset.CoordinateAxis1DTime;
import ucar.nc2.dataset.CoordinateAxis2D;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDataset;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;
import ucar.nc2.time.CalendarDate;
import uk.ac.rdg.resc.edal.dataset.DataReadingStrategy;
import uk.ac.rdg.resc.edal.exceptions.EdalException;
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
import uk.ac.rdg.resc.edal.util.chronologies.AllLeapChronology;
import uk.ac.rdg.resc.edal.util.chronologies.NoLeapChronology;
import uk.ac.rdg.resc.edal.util.chronologies.ThreeSixtyDayChronology;

/**
 * Contains static helper methods for reading data and metadata from NetCDF
 * files, OPeNDAP servers and other data sources using the Unidata Common Data
 * Model.
 * 
 * @author Jon
 * @author Guy
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
     * Gets a GridDataset from the given NetcdfDataset
     */
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

    /**
     * @return true if the given location represents an NcML aggregation.
     *         dataset. This method simply checks to see if the location string
     *         ends with ".xml" or ".ncml", following the same procedure as the
     *         Java NetCDF library.
     */
    public static boolean isNcmlAggregation(String location) {
        return location.endsWith(".xml") || location.endsWith(".ncml");
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
        /*
         * TODO: also use the size of the grids as a deciding factor: it can be
         * very slow to read large grids by the BOUNDING_BOX strategy
         */
        String fileType = nc.getFileTypeId();
        return "netCDF".equalsIgnoreCase(fileType) || "HDF4".equalsIgnoreCase(fileType) ? DataReadingStrategy.SCANLINE
                : DataReadingStrategy.BOUNDING_BOX;
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

            Array2D<Number> lonVals = new Array2D<Number>(lonAxis.getShape(0), lonAxis.getShape(1)) {
                @Override
                public void set(Number value, int... coords) {
                    throw new UnsupportedOperationException("This Array2D is immutable");
                }

                @Override
                public Number get(int... coords) {
                    return lonAxis.getCoordValue(coords[0], coords[1]);
                }
            };
            Array2D<Number> latVals = new Array2D<Number>(latAxis.getShape(0), latAxis.getShape(1)) {
                @Override
                public void set(Number value, int... coords) {
                    throw new UnsupportedOperationException("This Array2D is immutable");
                }

                @Override
                public Number get(int... coords) {
                    return latAxis.getCoordValue(coords[0], coords[1]);
                }
            };

            return LookUpTableGrid.generate(lonVals, latVals);
        } else {
            /* Shouldn't get here */
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

            List<Double> zValues = new ArrayList<Double>();
            for (double zVal : zAxis.getCoordValues()) {
                zValues.add(zVal);
            }
            values = Collections.unmodifiableList(zValues);
        } else {
            return null;
        }

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
            return ISOChronology.getInstance();
        } else if ("proleptic_gregorian".equalsIgnoreCase(chronologyString)) {
            return GregorianChronology.getInstance();
        } else if ("julian".equalsIgnoreCase(chronologyString)) {
            return JulianChronology.getInstance();
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
        String name = axis.getFullName();
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
     * 
     * @return a {@link NetcdfDataset} object for accessing the data at the
     *         given location.
     * 
     * @throws IOException
     *             if there was an error reading from the data source.
     */
    public static NetcdfDataset openDataset(String location) throws IOException, EdalException {
        NetcdfDataset nc;
        if (isNcmlAggregation(location)) {
            /*
             * We use the cache of NetcdfDatasets to read NcML aggregations as
             * they can be time-consuming to put together. If the underlying
             * data can change we rely on the server admin setting the
             * "recheckEvery" parameter in the aggregation file.
             */
            nc = NetcdfDataset.acquireDataset(location, null);
        } else {
            /*
             * For local single files and OPeNDAP datasets we don't use the
             * cache, to ensure that we are always reading the most up-to-date
             * data. There is a small possibility that the dataset cache will
             * have swallowed up all available file handles, in which case the
             * server admin will need to increase the number of available
             * handles on the server.
             */
            nc = NetcdfDataset.openDataset(location);
        }
        return nc;
    }

    /**
     * Closes the given dataset, logging any exceptions at debug level
     * 
     * @param nc
     *            The {@link NetcdfDataset} to close
     */
    public static void closeDataset(NetcdfDataset nc) throws IOException {
        if (nc == null)
            return;
        nc.close();
    }

    /**
     * Expands a glob expression to give a List of paths to files. This method
     * recursively searches directories, allowing for glob expressions like
     * {@code "c:\\data\\200[6-7]\\*\\1*\\A*.nc"}.
     * 
     * @return a {@link List} of {@link File}s matching the given glob
     *         expression
     * @author Mike Grant, Plymouth Marine Labs
     * @author Jon Blower
     * @author Guy Griffiths
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