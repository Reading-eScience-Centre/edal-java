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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

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
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;
import uk.ac.rdg.resc.edal.dataset.Dataset;
import uk.ac.rdg.resc.edal.dataset.DatasetFactory;
import uk.ac.rdg.resc.edal.dataset.DiscreteFeatureReader;
import uk.ac.rdg.resc.edal.dataset.FeatureIndexer;
import uk.ac.rdg.resc.edal.dataset.FeatureIndexer.FeatureBounds;
import uk.ac.rdg.resc.edal.dataset.PRTreeFeatureIndexer;
import uk.ac.rdg.resc.edal.dataset.PointDataset;
import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.domain.SimpleHorizontalDomain;
import uk.ac.rdg.resc.edal.domain.SimpleTemporalDomain;
import uk.ac.rdg.resc.edal.domain.SimpleVerticalDomain;
import uk.ac.rdg.resc.edal.exceptions.DataReadingException;
import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.feature.DiscreteFeature;
import uk.ac.rdg.resc.edal.feature.PointFeature;
import uk.ac.rdg.resc.edal.feature.ProfileFeature;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.grid.VerticalAxisImpl;
import uk.ac.rdg.resc.edal.metadata.Parameter;
import uk.ac.rdg.resc.edal.metadata.VariableMetadata;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.VerticalCrsImpl;
import uk.ac.rdg.resc.edal.util.Array1D;
import uk.ac.rdg.resc.edal.util.CollectionUtils;
import uk.ac.rdg.resc.edal.util.Extents;
import uk.ac.rdg.resc.edal.util.GISUtils;
import uk.ac.rdg.resc.edal.util.TimeUtils;
import uk.ac.rdg.resc.edal.util.ValuesArray1D;
import uk.ac.rdg.resc.edal.util.cdm.CdmUtils;

/**
 * {@link DatasetFactory} that creates {@link Dataset}s representing profile
 * data from Argo floats read through the Unidata Common Data Model.
 *
 * @author Guy Griffiths
 */
public class ArgoDatasetFactory extends DatasetFactory {
    protected static final Logger log = LoggerFactory.getLogger(ArgoDatasetFactory.class);

    /*
     * The following variables are treated like constants, and define which
     * variables and parameters to read.
     * 
     * The reason they are not constants is that EN3 and EN4 datasets use an
     * identical format, with the exception that some of the variable names and
     * the vertical CRS are changed.
     * 
     * By treating these like constants, but allowing subclasses to modify them,
     * we can implement an EN3/4 dataset factory very simply.
     */
    protected String LONGITUDE = "LONGITUDE";
    protected String LATITUDE = "LATITUDE";
    protected String TIME = "JULD";
    protected String DEPTH = "PRES_ADJUSTED";

    protected Parameter TEMP_PARAMETER = new Parameter("TEMP_ADJUSTED",
            "Sea Water Potential Temperature",
            "The potential temperature, in degrees celcius, of the sea water", "degrees_C",
            "sea_water_potential_temperature");
    protected Parameter PSAL_PARAMETER = new Parameter("PSAL_ADJUSTED", "Sea Water Salinity",
            "The measured salinity, in practical salinity units (psu) of the sea water ", "psu",
            "sea_water_salinity");

    protected String POSITION_QC = "POSITION_QC";
    protected String TEMP_QC = "TEMP_ADJUSTED_QC";
    protected String PSAL_QC = "PSAL_ADJUSTED_QC";

    protected String PLATFORM_ID = "PLATFORM_NUMBER";
    protected String N_LEVELS = "N_LEVELS";
    protected String N_PROF = "N_PROF";

    protected VerticalCrsImpl VERTICAL_CRS = new VerticalCrsImpl("decibar", true, false, false);

    /*
     * Converting between ID and file/profile number could be done (and was
     * previously done) using a map of internal ID to file and profile number.
     *
     * By using a simple string encoding (number:filepath) we use less memory,
     * which can be a concern when querying a large number of floats
     */
    private static class FileAndProfileNumber {
        private File file;
        private int profileNumber;

        public FileAndProfileNumber(File file, int profileNumber) {
            this.file = file;
            this.profileNumber = profileNumber;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((file == null) ? 0 : file.hashCode());
            result = prime * result + profileNumber;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            FileAndProfileNumber other = (FileAndProfileNumber) obj;
            if (file == null) {
                if (other.file != null)
                    return false;
            } else if (!file.equals(other.file))
                return false;
            if (profileNumber != other.profileNumber)
                return false;
            return true;
        }
    }

    /*
     * Encapsulates the format used for datetimes
     */
    private static final DateTimeFormatter DATE_TIME_FORMATTER = (new DateTimeFormatterBuilder())
            .appendYear(4, 4).appendLiteral("-").appendMonthOfYear(1).appendLiteral("-")
            .appendDayOfMonth(1).appendLiteral(" ").appendHourOfDay(1).appendLiteral(":")
            .appendMinuteOfHour(1).appendLiteral(":").appendSecondOfMinute(1).appendLiteral(" utc")
            .toFormatter().withZoneUTC().withChronology(ISOChronology.getInstanceUTC());

    /* This is because we deserialise a HashMap which is a generic. */
    @SuppressWarnings("unchecked")
    @Override
    public ArgoDataset createDataset(String id, String location, boolean forceRefresh)
            throws IOException, EdalException {
        log.debug("IN createDataset Entering createDataset");
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
         * Check to see if we have indexed this set of files in the past. If so,
         * read the spatial index from disk, otherwise generate a new one and
         * write that.
         */
        File spatialIndexFile = new File(workingDir, id + ".index.ser");
        boolean readExistingSpatialIndex = false;
        ObjectInputStream in = null;
        FileInputStream fileIn = null;

        if (spatialIndexFile.exists() && !forceRefresh) {
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
                List<File> existingFilesList = (List<File>) in.readObject();
                if (files.equals(existingFilesList)) {
                    /*
                     * The current list of files is the same as the one used in
                     * the previous indexing.
                     */
                    readExistingSpatialIndex = true;
                }
            } catch (ClassNotFoundException | IOException | ClassCastException e) {
                /*
                 * Log this error, but otherwise ignore it - we will just
                 * recreate the spatial index, so it's not a big problem.
                 */
                log.warn("Problem reading serialisation index", e);
            }
        }

        /*
         * The domain of this dataset
         */
        SimpleHorizontalDomain hDomain = null;
        SimpleVerticalDomain zDomain = null;
        SimpleTemporalDomain tDomain = null;
        /*
         * The map of IDs to file locations
         */
        Map<Integer, File> id2File = new HashMap<>();
        /*
         * The spatial indexer to use
         */
        FeatureIndexer indexer = null;

        if (readExistingSpatialIndex) {
            try {
                hDomain = (SimpleHorizontalDomain) in.readObject();
                zDomain = (SimpleVerticalDomain) in.readObject();
                tDomain = (SimpleTemporalDomain) in.readObject();
                id2File = (HashMap<Integer, File>) in.readObject();
                indexer = (PRTreeFeatureIndexer) in.readObject();

                log.debug("Successfully read spatial index from file");
            } catch (ClassNotFoundException | IOException | ClassCastException e) {
                /*
                 * Problem reading spatial index/domain from file. Set the flag
                 * to regenerate it.
                 */
                log.warn("Problem reading domain/spatial index", e);
                readExistingSpatialIndex = false;
            }
        }

        if (!readExistingSpatialIndex) {
            /*
             * We don't want to keep reading from the file, because it either
             * doesn't exist, or it doesn't match the file set we are now
             * reading (e.g. a new file has been added, or this ID was last used
             * for a different dataset)
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

            /*
             * We want to be able to easily convert a feature ID to a file and
             * profile number. We could:
             *
             * Create unique IDs and store a map of ID -> File/ProfileNumber
             *
             * Encode the full path/profile number in the ID
             *
             * Store the common prefix+suffix of all file paths in the dataset,
             * and encode the non-unique path/profile number in the ID
             *
             * But to get around having awkward characters in the ID and not use
             * too much memory, we use a hybrid solution, where we store a Map
             * of IDs to Files, and encode the file ID and the profile number in
             * the feature ID
             */

            int fileId = 0;

            for (File file : files) {
                id2File.put(fileId, file);
                NetcdfDataset nc = NetcdfDatasetAggregator.getDataset(file.getAbsolutePath());

                Dimension nProfiles = nc.findDimension(N_PROF);
                Dimension nLevels = nc.findDimension(N_LEVELS);

                Variable latitudeVar = nc.findVariable(LATITUDE);
                Variable longitudeVar = nc.findVariable(LONGITUDE);
                Variable timeVar = nc.findVariable(TIME);
                Variable depthVar = nc.findVariable(DEPTH);

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
                DateTime refTime = DATE_TIME_FORMATTER.parseDateTime(timeUnitsParts[1]);

                Array latValues = latitudeVar.read();
                Array lonValues = longitudeVar.read();
                Array timeValues = timeVar.read();
                Array depthValues = depthVar.read();

                /*
                 * Loop over all profiles
                 */
                for (int profileNum = 0; profileNum < nProfiles.getLength(); profileNum++) {
                    /*
                     * Get the horizontal position of the current profile
                     */
                    double lat = latValues.getDouble(profileNum);
                    double lon = lonValues.getDouble(profileNum);

                    if (Double.isNaN(lat) || Double.isNaN(lon)) {
                        /*
                         * We have bad data for the position. This reading must
                         * be ignored.
                         */
                        continue;
                    }

                    /*
                     * All positions are in WGS84
                     */
                    HorizontalPosition horizontalPosition = new HorizontalPosition(lon, lat,
                            GISUtils.defaultGeographicCRS());
                    /*
                     * Find the time of the current profile measurement
                     */
                    double seconds = (timeValues.getDouble(profileNum) * unitLength);
                    if (Double.isNaN(seconds)) {
                        continue;
                    }

                    DateTime time = refTime.plusSeconds((int) seconds);
                    Extent<DateTime> tExtent = Extents.newExtent(time, time);

                    /*
                     * Find the vertical extent of the current profile
                     */
                    List<Double> depths = new ArrayList<>();
                    for (int j = 0; j < nLevels.getLength(); j++) {
                        double depth = depthValues.getDouble(profileNum * nLevels.getLength() + j);
                        if (!Double.isNaN(depth) && depth != 99999.0) {
                            depths.add(depth);
                        } else {
                            break;
                        }
                    }

                    if (depths.size() == 0) {
                        /*
                         * We have the situation where all values of depth
                         * returned NaN
                         *
                         * This profile cannot be indexed
                         */
                        continue;
                    }
                    boolean nonMonotonic = false;
                    for (int k = 0; k < depths.size() - 1; k++) {
                        double depth = depths.get(k);
                        double nextDepth = depths.get(k + 1);
                        if (nextDepth <= depth) {
                            /*
                             * We have a non-monotonic value. This usually
                             * follows a very large value in the middle of a
                             * normally increasing depth axis.
                             */
                            nonMonotonic = true;
                            break;
                        }
                    }
                    if (nonMonotonic) {
                        /*
                         * Ignore profiles with non-monotonic axes
                         */
                        continue;
                    }

                    Extent<Double> zExtent = Extents.newExtent(Collections.min(depths),
                            Collections.max(depths));

                    /*
                     * Create a unique ID
                     */
                    String profileId = fileId + ":" + profileNum;

                    /*
                     * Store the bounds of this feature to load into the spatial
                     * indexer
                     */
                    featureBounds.add(new FeatureBounds(profileId, horizontalPosition, zExtent,
                            tExtent, CollectionUtils.setOf(TEMP_PARAMETER.getVariableId(),
                                    PSAL_PARAMETER.getVariableId())));

                    /*
                     * Update entire dataset extents
                     */
                    minLat = Math.min(minLat, lat);
                    maxLat = Math.max(maxLat, lat);

                    minLon = Math.min(minLon, lon);
                    maxLon = Math.max(maxLon, lon);

                    minZ = Math.min(minZ, zExtent.getLow());
                    maxZ = Math.max(maxZ, zExtent.getHigh());

                    if (minT.isAfter(time)) {
                        minT = time;
                    }
                    if (maxT.isBefore(time)) {
                        maxT = time;
                    }
                }

                log.debug("Read " + nProfiles.getLength() + " profiles from file: "
                        + file.getAbsolutePath());
                log.debug("Allocated memory " + (Runtime.getRuntime().totalMemory() / 1_000_000L)
                        + "/" + (Runtime.getRuntime().maxMemory() / 1_000_000L));
                totalProfiles += nProfiles.getLength();
                fileId++;
                NetcdfDatasetAggregator.releaseDataset(nc);
            }
            if (totalProfiles == 0) {
                throw new DataReadingException(
                        "There are no profiles in the location: " + location);
            }

            log.debug("Read " + totalProfiles + " features.  Starting indexing...");
            /*
             * The domain of this dataset. Since all variables are valid for the
             * entire dataset, their domain must include the domains of all
             * points within it.
             */
            hDomain = new SimpleHorizontalDomain(minLon, minLat, maxLon, maxLat);
            zDomain = new SimpleVerticalDomain(minZ, maxZ, VERTICAL_CRS);
            tDomain = new SimpleTemporalDomain(minT, maxT);

            /*
             * Now add all features to the spatial indexer
             */
            indexer.addFeatures(featureBounds);
            log.debug("Indexed " + totalProfiles + " features.");
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
                out.writeObject(id2File);
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
        metadata.add(new VariableMetadata(TEMP_PARAMETER, hDomain, zDomain, tDomain));
        metadata.add(new VariableMetadata(PSAL_PARAMETER, hDomain, zDomain, tDomain));

        long t2 = System.currentTimeMillis();
        log.debug("Time to create dataset: " + ((t2 - t1) / 1000.0) + "s");

        log.debug("OUT createDataset Returning from createDataset");
        return new ArgoDataset(id, metadata, indexer, hDomain.getBoundingBox(), zDomain.getExtent(),
                tDomain.getExtent(), id2File);
    }

    private final class ArgoDataset extends PointDataset<ProfileFeature> {
        private static final long serialVersionUID = 1L;
        private ArgoDatabaseReader reader = new ArgoDatabaseReader(this);

        private Map<Integer, File> fileMap;

        public ArgoDataset(String id, Collection<? extends VariableMetadata> vars,
                FeatureIndexer featureIndexer, BoundingBox bbox, Extent<Double> zExtent,
                Extent<DateTime> tExtent, Map<Integer, File> fileMap) {
            super(id, vars, featureIndexer, bbox, zExtent, tExtent);
            this.fileMap = fileMap;
        }

        @Override
        public Class<? extends DiscreteFeature<?, ?>> getFeatureType(String variableId) {
            /*
             * All variables return a ProfileFeature
             */
            return ProfileFeature.class;
        }

        @Override
        public DiscreteFeatureReader<ProfileFeature> getFeatureReader() {
            log.debug("Returning FeatureReader");
            return reader;
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

        private File getFileFromId(int fileId) {
            return fileMap.get(fileId);
        }

        @Override
        protected PointFeature convertFeature(ProfileFeature feature, BoundingBox hExtent,
                Extent<Double> zExtent, Extent<DateTime> tExtent, Double targetZ,
                DateTime targetT) {
            log.debug("Converting ProfileFeature to PointFeature");
            return convertProfileFeature(feature, targetZ);
        }

        @Override
        public List<PointFeature> extractMapFeatures(Set<String> varIds, BoundingBox hExtent,
                Extent<Double> zExtent, Double targetZ, Extent<DateTime> tExtent, DateTime targetT)
                throws DataReadingException {
            List<PointFeature> mapFeatures = super.extractMapFeatures(varIds, hExtent, zExtent,
                    targetZ, tExtent, targetT);
            /*
             * Ensure that we only include each unique profile once. The profile
             * ID is stored in the feature name.
             */
            Map<String, FeatureAndDeltaT> nearestFeatures = new HashMap<>();
            for (PointFeature feature : mapFeatures) {
                String profId = feature.getName();
                if (!nearestFeatures.containsKey(profId)) {
                    nearestFeatures.put(profId, new FeatureAndDeltaT(feature, Math.abs(
                            feature.getGeoPosition().getTime().getMillis() - targetT.getMillis())));
                } else {
                    long deltaT = Math.abs(
                            feature.getGeoPosition().getTime().getMillis() - targetT.getMillis());
                    if (deltaT < nearestFeatures.get(profId).deltaT) {
                        nearestFeatures.put(profId, new FeatureAndDeltaT(feature, deltaT));
                    }
                }
            }
            List<PointFeature> features = new ArrayList<>();
            for (FeatureAndDeltaT f : nearestFeatures.values()) {
                features.add(f.feature);
            }
            return features;
        }
    }

    private static class FeatureAndDeltaT {
        PointFeature feature;
        long deltaT;

        public FeatureAndDeltaT(PointFeature feature, long deltaT) {
            this.feature = feature;
            this.deltaT = deltaT;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (int) (deltaT ^ (deltaT >>> 32));
            result = prime * result + ((feature == null) ? 0 : feature.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            FeatureAndDeltaT other = (FeatureAndDeltaT) obj;
            if (deltaT != other.deltaT)
                return false;
            if (feature == null) {
                if (other.feature != null)
                    return false;
            } else if (!feature.equals(other.feature))
                return false;
            return true;
        }
    }

    private final class ArgoDatabaseReader implements DiscreteFeatureReader<ProfileFeature> {

        private ArgoDataset dataset;

        public ArgoDatabaseReader(ArgoDataset dataset) {
            this.dataset = dataset;
        }

        /**
         * Converts an ID to a {@link FileAndProfileNumber}
         *
         * @param id
         *            The ID
         * @return The corresponding {@link FileAndProfileNumber}
         */
        private FileAndProfileNumber deserialiseId(String id) {
            String[] split = id.split(":");
            int fileId = Integer.parseInt(split[0]);
            int profileNumber = Integer.parseInt(split[1]);
            File file = dataset.getFileFromId(fileId);
            return new FileAndProfileNumber(file, profileNumber);
        }

        @Override
        public ProfileFeature readFeature(String id, Set<String> variableIds)
                throws DataReadingException {
            if (variableIds == null) {
                variableIds = dataset.getVariableIds();
            }
            /*
             * Find the file containing the ID, and the profile number within
             * the file
             */
            FileAndProfileNumber fileAndProfileNumber = deserialiseId(id);

            ProfileFeature profileFeature = null;
            NetcdfDataset nc = null;
            try {
                /*
                 * Open the dataset, read the profile, and close the dataset
                 */
                nc = NetcdfDatasetAggregator
                        .getDataset(fileAndProfileNumber.file.getAbsolutePath());
                profileFeature = doRead(id, nc, fileAndProfileNumber.profileNumber, variableIds);
            } catch (IOException e) {
                log.debug("readFeature throwing exception");
                throw new DataReadingException("Problem reading profile data", e);
            } catch (InvalidRangeException e) {
                log.debug("readFeature throwing exception");
                throw new DataReadingException("Problem reading profile data", e);
            } finally {
                NetcdfDatasetAggregator.releaseDataset(nc);
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
        public List<ProfileFeature> readFeatures(Collection<String> ids, Set<String> variableIds)
                throws DataReadingException {
            log.debug("IN readFeatures Reading multiple features");
            List<ProfileFeature> ret = new ArrayList<ProfileFeature>();
            if (variableIds == null) {
                variableIds = dataset.getVariableIds();
            }

            /*
             * Find the files containing each profile and map to a list of the
             * profile numbers needing to be read from each file.
             */
            Map<File, List<FeatureAndProfileId>> file2Ids = new HashMap<File, List<FeatureAndProfileId>>();
            log.debug("readFeatures - Mapping files to IDs");
            for (String id : ids) {
                FileAndProfileNumber fileAndProfileNumber = deserialiseId(id);
                File file = fileAndProfileNumber.file.getAbsoluteFile();

                if (!file2Ids.containsKey(file)) {
                    List<FeatureAndProfileId> idsList = new ArrayList<FeatureAndProfileId>();
                    file2Ids.put(file, idsList);
                }
                file2Ids.get(file)
                        .add(new FeatureAndProfileId(id, fileAndProfileNumber.profileNumber));
            }
            /*
             * Now open each file in turn and read the profiles from them
             */
            log.debug("readFeatures - reading from all files");
            for (Entry<File, List<FeatureAndProfileId>> entry : file2Ids.entrySet()) {
                File file = entry.getKey();
                NetcdfDataset nc = null;
                try {
                    log.debug("readFeatures - acquiring dataset: " + file.getAbsolutePath());
                    nc = NetcdfDatasetAggregator.getDataset(file.getAbsolutePath());

                    List<FeatureAndProfileId> featureProfileIds = entry.getValue();
                    /*
                     * For 100 simultaneous requests
                     */
                    log.debug("readFeatures - reading all features from file "
                            + file.getAbsolutePath());
                    for (FeatureAndProfileId featureProfileId : featureProfileIds) {
                        ProfileFeature profileFeature = doRead(featureProfileId.featureId, nc,
                                featureProfileId.profileId, variableIds);
                        if (profileFeature != null) {
                            ret.add(profileFeature);
                        }
                    }
                } catch (IOException | InvalidRangeException e) {
                    log.error("Problem reading profile data", e);
                    throw new DataReadingException("Problem reading profile data", e);
                } finally {
                    NetcdfDatasetAggregator.releaseDataset(nc);
                }
            }
            log.debug("OUT readFeatures - Read collection of features.  Returning");
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
         *            The variables to read from the file - may not be
         *            <code>null</code>
         * @return The desired {@link ProfileFeature}
         * @throws IOException
         *             If there is a problem reading data from the
         *             {@link NetcdfDataset}
         * @throws InvalidRangeException
         */
        private synchronized ProfileFeature doRead(String id, NetcdfDataset nc, int profNum,
                Set<String> variableIds) throws IOException, InvalidRangeException {
            /*
             * This method is synchronized because Variable objects can be
             * shared between instances which leads to Bad Things
             */

            /*
             * This is a fixed value. We could read the "STRING8" dimension and
             * find its length, but that seems a little unnecessary, since it
             * will be 8
             */
            int platformNameLength = 8;

            /*
             * We synchronise the rest of this method on an object specific to
             * the location of the NetcdfDataset we are reading.
             *
             * This means that doRead() cannot be called simultaneously with the
             * same NetcdfDataset object (this causes Ranges to be set
             * incorrectly and leads to ArrayIndex errors), but CAN be called
             * simultaneously to read features from different files (which is
             * fine).
             */
            Dimension nLevels = nc.findDimension(N_LEVELS);

            /*
             * Find the variables necessary to determine the 4D domain of this
             * platform
             */
            Variable latitudeVar = nc.findVariable(LATITUDE);
            Variable longitudeVar = nc.findVariable(LONGITUDE);
            Variable timeVar = nc.findVariable(TIME);
            Variable depthVar = nc.findVariable(DEPTH);

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
            platformIdRangeList.add(new Range(platformNameLength));

            Variable platformIdVar = nc.findVariable(PLATFORM_ID);
            Array platformIdArr = platformIdVar.read(platformIdRangeList);

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
            DateTime refTime = DATE_TIME_FORMATTER.parseDateTime(timeUnitsParts[1]);

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
                    latValues.getDouble(0), GISUtils.defaultGeographicCRS());

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
                domain = new VerticalAxisImpl("Depth axis of profile", zValues, VERTICAL_CRS);
            } catch (IllegalArgumentException e) {
                /*
                 * This happens when the domain is non-monotonic. For now we
                 * ignore these profiles (1-2% of total) but later we may need
                 * to re-order the measurement values
                 */
                log.warn("Non monotonic domain in argo file", e);
                return null;
            }
            /*
             * Store the number of depth values before a NaN appears (this is
             * the true depth domain - once we get to NaN values there is no
             * data)
             */
            int trueNumLevels = zValues.size();

            Map<String, Array1D<Number>> values = new HashMap<String, Array1D<Number>>();
            /*
             * Read all of the actual data
             */
            Map<String, Parameter> parameters = new HashMap<String, Parameter>();

            for (String varId : variableIds) {
                Array varArray = nc.findVariable(varId).read(allDepthsOnePlatform);

                Array1D<Number> varValues = new ValuesArray1D(trueNumLevels);
                for (int i = 0; i < trueNumLevels; i++) {
                    Double val = varArray.getDouble(i);
                    if (Double.isNaN(val)) {
                        val = null;
                    }
                    varValues.set(val, i);
                }
                values.put(varId, varValues);
                parameters.put(varId, dataset.getVariableMetadata(varId).getParameter());
            }

            String platformIdStr = platformId.toString().trim();

            log.debug("Read data, creating ProfileFeature");
            /*
             * Create the ProfileFeature
             */
            ProfileFeature ret = new ProfileFeature(id, platformIdStr,
                    "Profile data from platform " + platformIdStr, domain, hPos, time, parameters,
                    values);

            /*
             * Read the quality control flags and store in the properties of the
             * profile feature
             */
            Variable qcPosVar = nc.findVariable(POSITION_QC);
            Variable qcPotmCorrectedVar = nc.findVariable(TEMP_QC);
            Variable qcPsalCorrectedVar = nc.findVariable(PSAL_QC);

            try {
                Array qcPos = qcPosVar.read();
                Array qcPotmCorrected = qcPotmCorrectedVar.read();
                Array qcPsalCorrected = qcPsalCorrectedVar.read();
                Properties props = new Properties();

                String key;
                String value;

                key = "Position QC";
                if (qcPos.getChar(profNum) == '1') {
                    value = "Accept";
                } else if (qcPos.getChar(profNum) == '4') {
                    value = "Reject";
                } else if (qcPos.getChar(profNum) == '0') {
                    value = "No QC data";
                } else {
                    value = "N/A";
                }
                props.put(key, value);

                if (variableIds.contains(TEMP_PARAMETER.getVariableId())) {
                    key = "Potential temperature QC";
                    if (qcPotmCorrected.getChar(profNum) == '1') {
                        value = "Accept";
                    } else if (qcPotmCorrected.getChar(profNum) == '4') {
                        value = "Reject";
                    } else if (qcPotmCorrected.getChar(profNum) == '0') {
                        value = "No QC data";
                    } else {
                        value = "N/A";
                    }
                    props.put(key, value);
                }

                if (variableIds.contains(PSAL_PARAMETER.getVariableId())) {
                    key = "Practical salinity QC";
                    if (qcPsalCorrected.getChar(profNum) == '1') {
                        value = "Accept";
                    } else if (qcPsalCorrected.getChar(profNum) == '4') {
                        value = "Reject";
                    } else if (qcPsalCorrected.getChar(profNum) == '0') {
                        value = "No QC data";
                    } else {
                        value = "N/A";
                    }
                    props.put(key, value);
                }
                ret.getFeatureProperties().putAll(props);
            } catch (Exception e) {
                /*
                 * Sometimes QC variables are not present.
                 */
                log.warn("Problem reading QC data", e);
            }

            return ret;
        }
    }
}