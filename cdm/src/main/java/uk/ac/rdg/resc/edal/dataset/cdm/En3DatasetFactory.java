/*******************************************************************************
 * Copyright (c) 2013 The University of Reading
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
 ******************************************************************************/

package uk.ac.rdg.resc.edal.dataset.cdm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;
import uk.ac.rdg.resc.edal.dataset.AbstractContinuousDomainDataset;
import uk.ac.rdg.resc.edal.dataset.Dataset;
import uk.ac.rdg.resc.edal.dataset.DatasetFactory;
import uk.ac.rdg.resc.edal.dataset.DiscreteFeatureReader;
import uk.ac.rdg.resc.edal.dataset.FeatureIndexer;
import uk.ac.rdg.resc.edal.dataset.FeatureIndexer.FeatureBounds;
import uk.ac.rdg.resc.edal.dataset.PRTreeFeatureIndexer;
import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.domain.SimpleHorizontalDomain;
import uk.ac.rdg.resc.edal.domain.SimpleTemporalDomain;
import uk.ac.rdg.resc.edal.domain.SimpleVerticalDomain;
import uk.ac.rdg.resc.edal.exceptions.DataReadingException;
import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.feature.ProfileFeature;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.grid.VerticalAxisImpl;
import uk.ac.rdg.resc.edal.metadata.Parameter;
import uk.ac.rdg.resc.edal.metadata.VariableMetadata;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.VerticalCrs;
import uk.ac.rdg.resc.edal.position.VerticalCrsImpl;
import uk.ac.rdg.resc.edal.util.Array1D;
import uk.ac.rdg.resc.edal.util.CollectionUtils;
import uk.ac.rdg.resc.edal.util.Extents;
import uk.ac.rdg.resc.edal.util.TimeUtils;
import uk.ac.rdg.resc.edal.util.ValuesArray1D;
import uk.ac.rdg.resc.edal.util.cdm.CdmUtils;

/**
 * {@link DatasetFactory} that creates {@link Dataset}s representing profile
 * data from the EN3 database read through the Unidata Common Data Model.
 * 
 * @author Guy Griffiths
 */
public final class En3DatasetFactory extends DatasetFactory {
    private static final Logger log = LoggerFactory.getLogger(En3DatasetFactory.class);
    /*
     * Static definitions of parameters found in the EN3 database
     */
    private static final Parameter TEMP_PARAMETER = new Parameter("TEMP", "Sea Water Temperature",
            "The measured temperature, in degrees celcius, of the sea water", "degrees_C",
            "sea_water_temperature");
    private static final Parameter POT_TEMP_PARAMETER = new Parameter("POTM_CORRECTED",
            "Sea Water Potential Temperature",
            "The potential temperature, in degrees celcius, of the sea water", "degrees_C",
            "sea_water_potential_temperature");
    private static final Parameter PSAL_PARAMETER = new Parameter("PSAL_CORRECTED",
            "Sea Water Salinity",
            "The measured salinity, in practical salinity units (psu) of the sea water ", "psu",
            "sea_water_salinity");

    private static final Map<String, Parameter> ALL_PARAMETERS = new HashMap<String, Parameter>();

    static {
        ALL_PARAMETERS.put(TEMP_PARAMETER.getId(), TEMP_PARAMETER);
        ALL_PARAMETERS.put(POT_TEMP_PARAMETER.getId(), POT_TEMP_PARAMETER);
        ALL_PARAMETERS.put(PSAL_PARAMETER.getId(), PSAL_PARAMETER);
    }

    /*
     * Converting between ID and file/profile number could be done (and was
     * previously done) using a map of internal ID to file and profile number.
     * 
     * By using a simple string encoding (number:filepath) we use less memory,
     * which can be a concern when querying the entire EN3 database
     */
    private static class FileAndProfileNumber {
        private File file;
        private int profileNumber;

        public FileAndProfileNumber(File file, int profileNumber) {
            this.file = file;
            this.profileNumber = profileNumber;
        }
    }

    /**
     * Converts an ID to a {@link FileAndProfileNumber}
     * 
     * @param id
     *            The ID
     * @return The corresponding {@link FileAndProfileNumber}
     */
    private static FileAndProfileNumber deserialiseId(String id) {
        /*
         * The ID is of the form:
         * 
         * 123:/path/to/file
         */
        String[] split = id.split(":");
        int profileNumber = Integer.parseInt(split[0]);

        if (split.length > 2) {
            /*
             * The file contains ":", so we need to concatenate all of the
             * elements of split
             */
            StringBuilder filePath = new StringBuilder();
            for (int i = 1; i < split.length; i++) {
                filePath.append(split[i]);
                if (i != split.length - 1) {
                    filePath.append(":");
                }
            }

            return new FileAndProfileNumber(new File(filePath.toString()), profileNumber);
        } else {
            return new FileAndProfileNumber(new File(split[1]), profileNumber);
        }
    }

    /**
     * Converts a {@link File} and profile number into a unique ID
     * 
     * @param file
     *            The file containing the profile
     * @param profileNumber
     *            The profile number within the file
     * @return A unique ID
     */
    private static String serialiseId(File file, int profileNumber) {
        /*
         * We just use a very simple format here - easy and quick to go back and
         * forth.
         */
        return profileNumber + ":" + file.getAbsolutePath();
    }

    /*
     * The VerticalCrs for the EN3 database. In EN3, depth is measured as a
     * positive number of metres downwards.
     */
    private static final VerticalCrsImpl EN3_VERTICAL_CRS = new VerticalCrsImpl("m", false, false,
            false);

    /*
     * Encapsulates the format used for datetimes in the EN3 database
     */
    private static final DateTimeFormatter EN3_DATE_TIME_FORMATTER = (new DateTimeFormatterBuilder())
            .appendYear(4, 4).appendLiteral("-").appendMonthOfYear(1).appendLiteral("-")
            .appendDayOfMonth(1).appendLiteral(" ").appendHourOfDay(1).appendLiteral(":")
            .appendMinuteOfHour(1).appendLiteral(":").appendSecondOfMinute(1).appendLiteral(" utc")
            .toFormatter().withZoneUTC().withChronology(ISOChronology.getInstanceUTC());

    @Override
    public Dataset createDataset(String id, String location) throws IOException, EdalException {
        long t1 = System.currentTimeMillis();
        
        /*
         * Use these to calculate the spatial extent of the entire dataset
         */
        double minLat = Double.MAX_VALUE;
        double maxLat = -Double.MAX_VALUE;

        double minLon = Double.MAX_VALUE;
        double maxLon = -Double.MAX_VALUE;

        double minZ = Double.MAX_VALUE;
        double maxZ = -Double.MAX_VALUE;

        DateTime minT = new DateTime(Long.MAX_VALUE);
        DateTime maxT = new DateTime(-Long.MAX_VALUE);

        /*
         * Expand the glob expression and then loop over each individual file
         */
        List<File> files = CdmUtils.expandGlobExpression(location);

        /*
         * TODO Read the id.files file and check if the list of files matches.
         * 
         * If so, read the id.index file and deserialise as the spatial index
         * and domain (or something)
         * 
         * If it's different, delete the spatial index, and create a new one,
         * serialising it to disk afterwards (as long as the working directory
         * is non-null).
         */
        File spatialIndexFile = new File(workingDir, id + ".index.ser");
        boolean readExistingSpatialIndex = false;
        ObjectInputStream in = null;
        FileInputStream fileIn = null;

        if (spatialIndexFile.exists()) {
            /*-
             * We have an existing spatial index for this ID.
             * 
             * This file contains simple serialisations of (in order):
             * 
             * The list of files used in the index.
             * The horizontal domain
             * The vertical domain
             * The time domain
             * The spatial indexer
             */
            try {
                fileIn = new FileInputStream(spatialIndexFile);
                in = new ObjectInputStream(fileIn);
                @SuppressWarnings("unchecked")
                List<File> existingFilesList = (List<File>) in.readObject();
                if (files.equals(existingFilesList)) {
                    /*
                     * The current list of files is the same as the one used in
                     * the previous indexing.
                     */
                    readExistingSpatialIndex = true;
                }
            } catch (ClassNotFoundException | IOException e) {
                /*
                 * Log this error, but otherwise ignore it - we will just
                 * recreate the spatial index, so it's not a big problem.
                 */
                log.warn("Problem reading EN3 serialisation index", e);
            }
        }

        /*
         * The domain of this EN3 dataset
         */
        SimpleHorizontalDomain hDomain = null;
        SimpleVerticalDomain zDomain = null;
        SimpleTemporalDomain tDomain = null;
        /*
         * The spatial indexer to use
         */
        FeatureIndexer indexer = null;

        if (readExistingSpatialIndex) {
            try {
                hDomain = (SimpleHorizontalDomain) in.readObject();
                zDomain = (SimpleVerticalDomain) in.readObject();
                tDomain = (SimpleTemporalDomain) in.readObject();
                indexer = (PRTreeFeatureIndexer) in.readObject();
                
                log.debug("Successfully read spatial index from file");
            } catch (ClassNotFoundException | IOException e) {
                /*
                 * Problem reading spatial index/domain from file. Set the flag
                 * to regenerate it.
                 */
                log.warn("Problem reading EN3 domain/spatial index", e);
                readExistingSpatialIndex = false;
            }
        }

        if (!readExistingSpatialIndex) {
            /*
             * We don't want to keep reading from the file, because it either
             * doesn't exist, or it doesn't match the file set we are now
             * reading (e.g. a new file has been added, or this ID was last used
             * for a different EN3 dataset)
             */
            if (in != null) {
                in.close();
            }
            if (fileIn != null) {
                fileIn.close();
            }
            if (spatialIndexFile.exists()) {
                /*
                 * We have a spatial index file but it doesn't match the files
                 * list. Delete it to create a new one.
                 */
                spatialIndexFile.delete();
            }

            /*
             * Now loop through all files, read the profile domains and IDs and
             * create the spatial index.
             */
            indexer = new PRTreeFeatureIndexer();
            List<PRTreeFeatureIndexer.FeatureBounds> featureBounds = new ArrayList<>();

            int totalProfiles = 0;
            
            for (File file : files) {
                NetcdfDataset nc = CdmUtils.openDataset(file.getAbsolutePath());

                Dimension nProfiles = nc.findDimension("N_PROF");
                Dimension nLevels = nc.findDimension("N_LEVELS");

                Variable latitudeVar = nc.findVariable("LATITUDE");
                Variable longitudeVar = nc.findVariable("LONGITUDE");
                Variable timeVar = nc.findVariable("JULD");
                Variable depthVar = nc.findVariable("DEPH_CORRECTED");

                Attribute timeUnits = timeVar.findAttribute("units");
                String timeUnitsStr = timeUnits.getStringValue();
                String[] timeUnitsParts = timeUnitsStr.split(" since ");

                if (timeUnitsParts.length != 2) {
                    log.error("Expected time units of the form xxxs since yyyy-dd-mm hh:mm:ss utc");
                    continue;
                }

                /*
                 * Find the length of a unit, in seconds (we don't use
                 * milliseconds because the DateTime.plusMillis takes an integer
                 * argument and there is a very good chance of integer overflow
                 * for recent values)
                 */
                int unitLength = TimeUtils.getUnitLengthSeconds(timeUnitsParts[0]);
                DateTime refTime = EN3_DATE_TIME_FORMATTER.parseDateTime(timeUnitsParts[1]);

                Array latValues = latitudeVar.read();
                Array lonValues = longitudeVar.read();
                Array timeValues = timeVar.read();
                Array depthValues = depthVar.read();

                /*
                 * Loop over all profiles
                 */
                for (int i = 0; i < nProfiles.getLength(); i++) {
                    /*
                     * Get the horizontal position of the current profile
                     */
                    double lat = latValues.getDouble(i);
                    double lon = lonValues.getDouble(i);
                    /*
                     * All positions are in WGS84
                     */
                    HorizontalPosition horizontalPosition = new HorizontalPosition(lon, lat,
                            DefaultGeographicCRS.WGS84);
                    /*
                     * Find the time of the current profile measurement
                     */
                    double seconds = (timeValues.getDouble(i) * unitLength);
                    DateTime time = refTime.plusSeconds((int) seconds);
                    Extent<DateTime> tExtent = Extents.newExtent(time, time);

                    /*
                     * Find the vertical extent of the current profile
                     */
                    double minProfDepth = Double.MAX_VALUE;
                    double maxProfDepth = -Double.MAX_VALUE;
                    for (int j = 0; j < nLevels.getLength(); j++) {
                        double depth = depthValues.getDouble(i * nLevels.getLength() + j);
                        if (!Double.isNaN(depth)) {
                            minProfDepth = Math.min(depth, minProfDepth);
                            maxProfDepth = Math.max(depth, maxProfDepth);
                        }
                    }
                    if (maxProfDepth < minProfDepth) {
                        /*
                         * We have the situation where all values of depth
                         * returned NaN.
                         * 
                         * This profile cannot be indexed
                         */
                        continue;
                    }
                    Extent<Double> zExtent = Extents.newExtent(minProfDepth, maxProfDepth);

                    /*
                     * Create a unique ID
                     */
                    String profileId = serialiseId(file, i);

                    /*
                     * Store the bounds of this feature to load into the spatial
                     * indexer
                     */
                    featureBounds.add(new FeatureBounds(profileId, horizontalPosition, zExtent,
                            tExtent, CollectionUtils.setOf(TEMP_PARAMETER.getId(),
                                    POT_TEMP_PARAMETER.getId(), PSAL_PARAMETER.getId())));

                    /*
                     * Update entire dataset extents
                     */
                    minLat = Math.min(minLat, lat);
                    maxLat = Math.max(maxLat, lat);

                    minLon = Math.min(minLon, lon);
                    maxLon = Math.max(maxLon, lon);

                    if (!Double.isNaN(minProfDepth)) {
                        minZ = Math.min(minZ, minProfDepth);
                    }
                    if (!Double.isNaN(maxProfDepth)) {
                        maxZ = Math.max(maxZ, maxProfDepth);
                    }

                    if (minT.isAfter(time)) {
                        minT = time;
                    }
                    if (maxT.isBefore(time)) {
                        maxT = time;
                    }
                }

                CdmUtils.closeDataset(nc);

                log.debug("Read " + nProfiles.getLength() + " profiles from file: "
                        + file.getAbsolutePath());
                log.debug("Allocated memory " + (Runtime.getRuntime().totalMemory() / 1_000_000L)
                        + "/" + (Runtime.getRuntime().maxMemory() / 1_000_000L));
                totalProfiles += nProfiles.getLength();
            }
            log.debug("Read "+totalProfiles+" features.  Starting indexing...");
            /*
             * The domain of this dataset. Since all variables are valid for the
             * entire dataset, their domain must include the domains of all
             * points within it.
             */
            hDomain = new SimpleHorizontalDomain(minLon, minLat, maxLon, maxLat);
            zDomain = new SimpleVerticalDomain(minZ, maxZ, EN3_VERTICAL_CRS);
            tDomain = new SimpleTemporalDomain(minT, maxT);

            /*
             * Now add all features to the spatial indexer
             */
            indexer.addFeatures(featureBounds);
            log.debug("Indexed "+totalProfiles+" features.");
            log.debug("Allocated memory " + (Runtime.getRuntime().totalMemory() / 1_000_000L) + "/"
                    + (Runtime.getRuntime().maxMemory() / 1_000_000L));

            /*
             * Now serialise the file list, domains, and indexer to file
             */
            try {
                FileOutputStream fileOut = new FileOutputStream(spatialIndexFile);
                ObjectOutputStream out = new ObjectOutputStream(fileOut);
                out.writeObject(files);
                out.writeObject(hDomain);
                out.writeObject(zDomain);
                out.writeObject(tDomain);
                out.writeObject(indexer);
                out.close();
                fileOut.close();
                log.debug("Serialised spatial index to file");
            } catch (IOException e) {
                log.warn("Unable to serialise spatial index to file", e);
            }
        }

        if (hDomain == null || zDomain == null || tDomain == null || indexer == null) {
            /*
             * The above is equivalent to an if-else block, but is implemented
             * as 2 opposite ifs so that the second block can be entered if the
             * first fails.
             * 
             * The upshot is that none of the above variables can be null,
             * unless the logic is seriously wrong.
             */
            assert false;
        }

        /*
         * Create a list of VariableMetadata objects for this domain. These can
         * be hardcoded, because these are the variables which the EN3 dataset
         * measures, and this reader is only for EN3 datasets...
         */
        List<VariableMetadata> metadata = new ArrayList<VariableMetadata>();
        metadata.add(new VariableMetadata(TEMP_PARAMETER.getId(), TEMP_PARAMETER, hDomain, zDomain,
                tDomain));
        metadata.add(new VariableMetadata(POT_TEMP_PARAMETER.getId(), POT_TEMP_PARAMETER, hDomain,
                zDomain, tDomain));
        metadata.add(new VariableMetadata(PSAL_PARAMETER.getId(), PSAL_PARAMETER, hDomain, zDomain,
                tDomain));

        long t2 = System.currentTimeMillis();
        log.debug("Time to create EN3 dataset: "+((t2-t1)/1000.0)+"s");

        return new En3Dataset(id, metadata, indexer, hDomain.getBoundingBox(), zDomain.getExtent(),
                tDomain.getExtent());
    }

    private final class En3Dataset extends AbstractContinuousDomainDataset {
        private En3DatabaseReader reader = new En3DatabaseReader(this);
        private BoundingBox bbox;
        private Extent<Double> zExtent;
        private Extent<DateTime> tExtent;

        public En3Dataset(String id, Collection<? extends VariableMetadata> vars,
                FeatureIndexer featureIndexer, BoundingBox bbox, Extent<Double> zExtent,
                Extent<DateTime> tExtent) {
            super(id, vars, ProfileFeature.class, featureIndexer);
            this.bbox = bbox;
            this.zExtent = zExtent;
            this.tExtent = tExtent;
        }

        @Override
        public DiscreteFeatureReader<ProfileFeature> getFeatureReader() {
            return reader;
        }

        @Override
        protected BoundingBox getDatasetBoundingBox() {
            return bbox;
        }

        @Override
        protected Extent<Double> getDatasetVerticalExtent() {
            return zExtent;
        }

        @Override
        protected Extent<DateTime> getDatasetTimeExtent() {
            return tExtent;
        }

        @Override
        public Chronology getDatasetChronology() {
            return ISOChronology.getInstance();
        }

        @Override
        public VerticalCrs getDatasetVerticalCrs() {
            return EN3_VERTICAL_CRS;
        }

        @Override
        public boolean supportsProfileFeatureExtraction(String varId) {
            /*
             * All variables are supported for profile features
             */
            return true;
        }

        @Override
        public boolean supportsTimeseriesExtraction(String varId) {
            /*
             * No variables are supported for timeseries
             */
            return false;
        }
    }

    private final class En3DatabaseReader implements DiscreteFeatureReader<ProfileFeature> {

        private Dataset dataset;

        public En3DatabaseReader(Dataset dataset) {
            this.dataset = dataset;
        }

        @Override
        public ProfileFeature readFeature(String id, Set<String> variableIds)
                throws DataReadingException {
            /*
             * Find the file containing the ID, and the profile number within
             * the file
             */
            FileAndProfileNumber fileAndProfileNumber = deserialiseId(id);

            ProfileFeature profileFeature = null;
            NetcdfFile nc = null;
            try {
                /*
                 * Open the dataset, read the profile, and close the dataset
                 */
                nc = NetcdfFile.open(fileAndProfileNumber.file.getAbsolutePath());
                profileFeature = doRead(id, nc, fileAndProfileNumber.profileNumber, variableIds);
                nc.close();
            } catch (IOException e) {
                throw new DataReadingException("Problem reading EN3 profile data", e);
            } catch (InvalidRangeException e) {
                throw new DataReadingException("Problem reading EN3 profile data", e);
            } finally {
                if (nc != null) {
                    try {
                        nc.close();
                    } catch (IOException e) {
                        log.error("Cannot close NetCDF dataset");
                    }
                }
            }
            return profileFeature;
        }

        private class FeatureAndProfileId {
            private String featureId;
            private Integer profileId;

            public FeatureAndProfileId(String featureId, Integer profileId) {
                super();
                this.featureId = featureId;
                this.profileId = profileId;
            }
        }

        @Override
        public Collection<ProfileFeature> readFeatures(Collection<String> ids,
                Set<String> variableIds) throws DataReadingException {
            List<ProfileFeature> ret = new ArrayList<ProfileFeature>();

            /*
             * Find the files containing each profile and map to a list of the
             * profile numbers needing to be read from each file.
             */
            Map<File, List<FeatureAndProfileId>> file2Ids = new HashMap<File, List<FeatureAndProfileId>>();
            for (String id : ids) {
                String[] split = id.split(":");
                int profileNumber = Integer.parseInt(split[0]);
                StringBuilder filePath = new StringBuilder();
                /*
                 * On the off-chance the file contains ":", we should
                 * concatenate all of the elements of split
                 */
                for (int i = 1; i < split.length; i++) {
                    filePath.append(split[i]);
                    if (i != split.length - 1) {
                        filePath.append(":");
                    }
                }

                // FileAndProfileNumber fileAndProfileNumber =
                // profileId2FileAndProfile.get(id);
                // if (profileId2FileAndProfile != null) {
                // File file = fileAndProfileNumber.file.getAbsoluteFile();
                File file = new File(filePath.toString());
                if (!file2Ids.containsKey(file)) {
                    List<FeatureAndProfileId> idsList = new ArrayList<FeatureAndProfileId>();
                    file2Ids.put(file, idsList);
                }
                file2Ids.get(file).add(new FeatureAndProfileId(id, /*
                                                                    * fileAndProfileNumber
                                                                    * .
                                                                    */
                profileNumber));
                // }
            }
            /*
             * Now open each file in turn and read the profiles from them
             */
            for (Entry<File, List<FeatureAndProfileId>> entry : file2Ids.entrySet()) {
                File file = entry.getKey();
                NetcdfDataset nc = null;
                try {
                    nc = CdmUtils.openDataset(file.getAbsolutePath());

                    List<FeatureAndProfileId> featureProfileIds = entry.getValue();
                    for (FeatureAndProfileId featureProfileId : featureProfileIds) {
                        ProfileFeature profileFeature = doRead(featureProfileId.featureId, nc,
                                featureProfileId.profileId, variableIds);
                        if (profileFeature != null) {
                            ret.add(profileFeature);
                        }
                    }

                    CdmUtils.closeDataset(nc);
                } catch (IOException e) {
                    throw new DataReadingException("Problem reading EN3 profile data", e);
                } catch (InvalidRangeException e) {
                    throw new DataReadingException("Problem reading EN3 profile data", e);
                } catch (EdalException e) {
                    throw new DataReadingException("Problem reading EN3 profile data", e);
                } finally {
                    if (nc != null) {
                        try {
                            CdmUtils.closeDataset(nc);
                        } catch (IOException e) {
                            log.error("Cannot close NetCDF dataset");
                        }
                    }
                }
            }
            return ret;
        }

        /**
         * Reads a single {@link ProfileFeature} from a {@link NetcdfDataset}
         * 
         * @param id
         *            The desired ID of the returned {@link ProfileFeature}
         * @param nc
         *            The {@link NetcdfDataset} to read the
         *            {@link ProfileFeature} from. The file must have the EN3
         *            v2a format
         * @param profNum
         *            The profile number within the file
         * @param variableIds
         *            The variables to read from the file
         * @return The desired {@link ProfileFeature}
         * @throws IOException
         *             If there is a problem reading data from the
         *             {@link NetcdfDataset}
         * @throws InvalidRangeException
         */
        private ProfileFeature doRead(String id, NetcdfFile nc, int profNum, Set<String> variableIds)
                throws IOException, InvalidRangeException {
            /*
             * This is a fixed value. We could read the "STRING8" dimension and
             * find its length, but that seems a little unnecessary, since it
             * will be 8
             */
            int platformNameLength = 8;
            Dimension nLevels = nc.findDimension("N_LEVELS");

            /*
             * Find the variables necessary to determine the 4D domain of this
             * platform
             */
            Variable latitudeVar = nc.findVariable("LATITUDE");
            Variable longitudeVar = nc.findVariable("LONGITUDE");
            Variable timeVar = nc.findVariable("JULD");
            Variable depthVar = nc.findVariable("DEPH_CORRECTED");

            /*
             * Set up some ranges to only read the pertinent part of the file
             */
            Range profileNumRange = new Range(profNum, profNum);
            Range levelNumRange = new Range(nLevels.getLength());

            /*
             * Determine the platform ID
             */
            List<Range> platformIdRangeList = new ArrayList<Range>();
            platformIdRangeList.add(profileNumRange);
            platformIdRangeList.add(new Range(8));
            Array platformIdArr = nc.findVariable("PLATFORM_NUMBER").read(platformIdRangeList);
            StringBuilder platformId = new StringBuilder();
            for (int i = 0; i < platformNameLength; i++) {
                platformId.append(platformIdArr.getChar(i));
            }

            /*
             * Determine the reference time
             */
            Attribute timeUnits = timeVar.findAttribute("units");
            String timeUnitsStr = timeUnits.getStringValue();
            String[] timeUnitsParts = timeUnitsStr.split(" since ");
            if (timeUnitsParts.length != 2) {
                log.error("Expected time units of the form \"xxxs since yyyy-dd-mm hh:mm:ss utc\"");
                return null;
            }
            int unitLength = TimeUtils.getUnitLengthSeconds(timeUnitsParts[0]);
            DateTime refTime = EN3_DATE_TIME_FORMATTER.parseDateTime(timeUnitsParts[1]);

            /*
             * Read the appropriate parts of the required variables
             */
            List<Range> singleValPerPlatform = new ArrayList<Range>();
            singleValPerPlatform.add(profileNumRange);
            Array latValues = latitudeVar.read(singleValPerPlatform);
            Array lonValues = longitudeVar.read(singleValPerPlatform);
            Array timeValues = timeVar.read(singleValPerPlatform);

            List<Range> allDepthsOnePlatform = new ArrayList<Range>();
            allDepthsOnePlatform.add(profileNumRange);
            allDepthsOnePlatform.add(levelNumRange);
            Array depthValues = depthVar.read(allDepthsOnePlatform);

            /*
             * Now use the values read from file to create the domain for this
             * feature
             */
            HorizontalPosition hPos = new HorizontalPosition(lonValues.getDouble(0),
                    latValues.getDouble(0), DefaultGeographicCRS.WGS84);

            double seconds = (timeValues.getDouble(0) * unitLength);
            DateTime time = refTime.plusSeconds((int) seconds);

            /*
             * Read the depth values, stopping when we hit NaNs
             */
            List<Double> zValues = new ArrayList<Double>();
            for (int i = 0; i < nLevels.getLength(); i++) {
                double depth = depthValues.getDouble(i);
                if (!Double.isNaN(depth) && depth != 99999.0) {
                    zValues.add(depth);
                } else {
                    break;
                }
            }
            VerticalAxisImpl domain = null;
            try {
                domain = new VerticalAxisImpl("Depth axis of profile", zValues, EN3_VERTICAL_CRS);
            } catch (IllegalArgumentException e) {
                /*
                 * This happens when the domain is non-monotonic. For now we
                 * ignore these profiles (1-2% of total) but later we may need
                 * to re-order the measurement values
                 */
                // log.error("Invalid domain in EN3 file", e);
                return null;
            }
            /*
             * Store the number of depth values before a NaN appears (this is
             * the true depth domain - once we get to NaN values there is no
             * data)
             */
            int trueNumLevels = zValues.size();

            Map<String, Array1D<Number>> values = new HashMap<String, Array1D<Number>>();
            if (variableIds == null) {
                /*
                 * If no variable IDs are specified, we want to read all of them
                 */
                variableIds = dataset.getVariableIds();
            }
            /*
             * Read all of the actual data
             */
            Map<String, Parameter> parameters = new HashMap<String, Parameter>();
            for (String varId : variableIds) {
                Array varArray = nc.findVariable(varId).read(allDepthsOnePlatform);

                Array1D<Number> varValues = new ValuesArray1D(trueNumLevels);
                for (int i = 0; i < trueNumLevels; i++) {
                    double val = varArray.getDouble(i);
                    varValues.set(val, i);
                }
                values.put(varId, varValues);
                parameters.put(varId, ALL_PARAMETERS.get(varId));
            }

            /*
             * Finally create and return the ProfileFeature
             */
            return new ProfileFeature(id, "EN3 platform " + platformId,
                    "Profile data from platform " + platformId + " in the EN3 database", domain,
                    hPos, time, parameters, values);
        }

    }
}