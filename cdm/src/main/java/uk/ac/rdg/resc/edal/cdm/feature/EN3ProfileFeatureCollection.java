package uk.ac.rdg.resc.edal.cdm.feature;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.geotoolkit.referencing.crs.DefaultGeographicCRS;

import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;
import uk.ac.rdg.resc.edal.Extent;
import uk.ac.rdg.resc.edal.Phenomenon;
import uk.ac.rdg.resc.edal.PhenomenonVocabulary;
import uk.ac.rdg.resc.edal.Unit;
import uk.ac.rdg.resc.edal.UnitVocabulary;
import uk.ac.rdg.resc.edal.cdm.util.CdmUtils;
import uk.ac.rdg.resc.edal.coverage.domain.impl.ProfileDomainImpl;
import uk.ac.rdg.resc.edal.coverage.impl.ProfileCoverageImpl;
import uk.ac.rdg.resc.edal.feature.ProfileFeature;
import uk.ac.rdg.resc.edal.feature.impl.FeatureCollectionImpl;
import uk.ac.rdg.resc.edal.feature.impl.ProfileFeatureImpl;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.geometry.impl.BoundingBoxImpl;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.TimePeriod;
import uk.ac.rdg.resc.edal.position.TimePosition;
import uk.ac.rdg.resc.edal.position.VerticalCrs;
import uk.ac.rdg.resc.edal.position.VerticalCrs.PositiveDirection;
import uk.ac.rdg.resc.edal.position.VerticalPosition;
import uk.ac.rdg.resc.edal.position.impl.HorizontalPositionImpl;
import uk.ac.rdg.resc.edal.position.impl.TimePeriodImpl;
import uk.ac.rdg.resc.edal.position.impl.TimePositionJoda;
import uk.ac.rdg.resc.edal.position.impl.VerticalCrsImpl;
import uk.ac.rdg.resc.edal.position.impl.VerticalPositionImpl;
import uk.ac.rdg.resc.edal.util.BigList;
import uk.ac.rdg.resc.edal.util.Extents;
import uk.ac.rdg.resc.edal.util.LittleBigList;

public class EN3ProfileFeatureCollection extends FeatureCollectionImpl<ProfileFeature> {

    private BoundingBox bbox;
    private Extent<VerticalPosition> vExtent;
    private Extent<TimePosition> tExtent;
    
    public EN3ProfileFeatureCollection(String collectionId, String collectionName, String location)
            throws IOException, InvalidRangeException {
        super(collectionId, collectionName);

        double minx = Double.MAX_VALUE;
        double maxx = -Double.MAX_VALUE;
        double miny = Double.MAX_VALUE;
        double maxy = -Double.MAX_VALUE;
        long mint = Long.MAX_VALUE;
        long maxt = -Long.MAX_VALUE;
        double minz = Double.MAX_VALUE;
        double maxz = -Double.MAX_VALUE;
        
        VerticalCrs vCrs = null;
        List<File> files = CdmUtils.expandGlobExpression(location);
        /*
         * The ID number of the profile within the collection
         */
        int idN = 0;
        for(File file : files){
            String filename = file.getPath();
            NetcdfDataset ncDataset = CdmUtils.openDataset(filename);
    
            List<Dimension> dimensions = ncDataset.getDimensions();
            int nLevels = 0;
            int nProf = 0;
            for (Dimension dim : dimensions) {
                if (dim.getName().equals("N_LEVELS")) {
                    nLevels = dim.getLength();
                }
                if (dim.getName().equals("N_PROF")) {
                    nProf = dim.getLength();
                }
            }
    
            List<Variable> variables = ncDataset.getVariables();
            Variable depthVar = null;
            Variable latVar = null;
            Variable lonVar = null;
            Variable timeVar = null;
            Variable psalVar = null;
            Variable tempVar = null;
            Variable refTimeVar = null;
    
            for (Variable var : variables) {
                if (var.getName().equals("DEPH_CORRECTED")) {
                    depthVar = var;
                } else if (var.getName().equals("LATITUDE")) {
                    latVar = var;
                } else if (var.getName().equals("LONGITUDE")) {
                    lonVar = var;
                } else if (var.getName().equals("PSAL_CORRECTED")) {
                    psalVar = var;
                } else if (var.getName().equals("TEMP")) {
                    tempVar = var;
                } else if (var.getName().equals("JULD")) {
                    timeVar = var;
                } else if (var.getName().equals("REFERENCE_DATE_TIME")) {
                    refTimeVar = var;
                }
            }
    
            if(refTimeVar == null){
                continue;
            }
            
            /*
             * First get the reference time.
             */
            String refTimeStr = new String((char[]) refTimeVar.read().copyTo1DJavaArray());
            int year = Integer.parseInt(refTimeStr.substring(0, 4));
            int month = Integer.parseInt(refTimeStr.substring(4, 6));
            int day = Integer.parseInt(refTimeStr.substring(6, 8));
            int hour = Integer.parseInt(refTimeStr.substring(8, 10));
            int minute = Integer.parseInt(refTimeStr.substring(10, 12));
            int second = Integer.parseInt(refTimeStr.substring(12, 14));
            Calendar refCal = Calendar.getInstance();
            refCal.set(Calendar.YEAR, year);
            refCal.set(Calendar.MONTH, month - 1);
            refCal.set(Calendar.DAY_OF_MONTH, day);
            refCal.set(Calendar.HOUR_OF_DAY, hour);
            refCal.set(Calendar.MINUTE, minute);
            refCal.set(Calendar.SECOND, second);
            refCal.set(Calendar.MILLISECOND, 0);
            TimePosition refTime = new TimePositionJoda(refCal.getTimeInMillis());
    
            /*
             * Now get the vertical units
             */
            List<Attribute> attributes = depthVar.getAttributes();
            String depthUnitsString = null;
            for (Attribute attr : attributes) {
                if (attr.getName().equals("units")) {
                    depthUnitsString = attr.getStringValue();
                }
            }
            // TODO UNKNOWN? or UDUNITS?
            Unit depthUnits = Unit.getUnit(depthUnitsString, UnitVocabulary.UNKNOWN);
            vCrs = new VerticalCrsImpl(depthUnits, PositiveDirection.DOWN, false);
    
            Array depthArr = depthVar.read();
            Index depthIndex = depthArr.getIndex();
    
            Array tempArr = tempVar.read();
            Index tempIndex = tempArr.getIndex();
    
            Array psalArr = psalVar.read();
            Index psalIndex = psalArr.getIndex();
    
            Array latArr = latVar.read();
            Index latIndex = latArr.getIndex();
            Array lonArr = lonVar.read();
            Index lonIndex = lonArr.getIndex();
            Array timeArr = timeVar.read();
            Index timeIndex = timeArr.getIndex();
    
            /*
             * For each profile
             */
            for (int prof = 0; prof < nProf; prof++) {
                /*
                 * Extract the domain.
                 */
                List<Double> depths = new ArrayList<Double>();
                BigList<Float> temps = new LittleBigList<Float>();
                BigList<Float> psals = new LittleBigList<Float>();
    
                depthIndex.setDim(0, prof);
                tempIndex.setDim(0, prof);
                psalIndex.setDim(0, prof);
                
                double lastDepth = -Double.MAX_VALUE;
                for (int lev = 0; lev < nLevels; lev++) {
                    depthIndex.setDim(1, lev);
                    double depth = depthArr.getDouble(depthIndex);
                    if (Double.isNaN(depth) || (lastDepth >= depth)) {
                        break;
                    } else {
                        depths.add(depth);
                        lastDepth = depth;
                    }
                    tempIndex.setDim(1, lev);
                    psalIndex.setDim(1, lev);
                    float temp = tempArr.getFloat(tempIndex);
                    float psal = psalArr.getFloat(psalIndex);
                    temps.add(temp);
                    psals.add(psal);
                }
                if (depths.size() == 0) {
                    /*
                     * We have no levels for this profile
                     */
                    continue;
                }
                ProfileDomainImpl domain;
                try {
                    domain = new ProfileDomainImpl(depths, vCrs);
                } catch (IllegalArgumentException iae) {
                    /*
                     * If we can't create a domain (e.g. non-monatonic values),
                     * we'll just ignore the profile for now...
                     * 
                     * TODO can we salvage something?
                     */
                    continue;
                }
    
                /*
                 * Extract the time of the observation
                 */
                latIndex.setDim(0, prof);
                double latitude = latArr.getDouble(latIndex);
                lonIndex.setDim(0, prof);
                double longitude = lonArr.getDouble(lonIndex);
                timeIndex.setDim(0, prof);
                double timeDiff = timeArr.getDouble(timeIndex);
                int days = (int) Math.floor(timeDiff);
                double hoursDiff = 24 * (timeDiff - days);
                int hours = (int) Math.floor(hoursDiff);
                double minsDiff = 60 * (hoursDiff - hours);
                int mins = (int) Math.floor(minsDiff);
                double secsDiff = 60 * (minsDiff - mins);
                int secs = (int) Math.floor(secsDiff);
                TimePeriod timeOffset = new TimePeriodImpl().withDays(days).withHours(hours)
                        .withMinutes(mins).withSeconds(secs);
                TimePosition time = refTime.plus(timeOffset);
    
                /*
                 * Extract the horizontal position
                 */
                HorizontalPosition hPos = new HorizontalPositionImpl(longitude, latitude,
                        DefaultGeographicCRS.WGS84);
                if(longitude < minx) {
                    minx = longitude;
                }
                if(longitude > maxx) {
                    maxx = longitude;
                }
                if(latitude < miny) {
                    miny = latitude;
                }
                if(latitude > maxy) {
                    maxy = latitude;
                }
                if(time.getValue() < mint){
                    mint = time.getValue();
                }
                if(time.getValue() > maxt){
                    maxt = time.getValue();
                }
                if(Collections.min(depths) < minz){
                    minz= Collections.min(depths);
                }
                if(Collections.max(depths) > maxz){
                    maxz= Collections.min(depths);
                }
    
                /*
                 * Create the coverage
                 */
                ProfileCoverageImpl coverage = new ProfileCoverageImpl("Coverage", domain);
    
                /*
                 * Add the temperature and salinity members
                 */
                coverage.addMember("TEMP", domain, "Temperature", Phenomenon.getPhenomenon(
                        "sea_water_temperature", PhenomenonVocabulary.CLIMATE_AND_FORECAST), Unit
                        .getUnit("degrees_celcius", UnitVocabulary.UDUNITS), temps, Float.class);
                coverage.addMember("PSAL", domain, "Practical Salinity", Phenomenon.getPhenomenon(
                        "sea_water_salinity", PhenomenonVocabulary.CLIMATE_AND_FORECAST), Unit.getUnit(
                        "psu", UnitVocabulary.UDUNITS), psals, Float.class);
                ProfileFeatureImpl feature = new ProfileFeatureImpl("Profile feature " + prof, "prof"
                        + (idN++), "Profile data for an EN3 buoy", coverage, hPos, time, this);
                addFeature(feature);
            }
        }
        
        if(maxx < minx && maxy < miny) {
            /*
             * We haven't found any profiles...
             */
            minx = 0.0;
            maxx = 0.0;
            miny = 0.0;
            maxy = 0.0;
            vExtent = Extents.emptyExtent(VerticalPosition.class);
            tExtent = Extents.emptyExtent(TimePosition.class);
        } else {
            vExtent = Extents.newExtent((VerticalPosition) new VerticalPositionImpl(minz, vCrs),
                    (VerticalPosition) new VerticalPositionImpl(maxz, vCrs));
            tExtent = Extents.newExtent((TimePosition) new TimePositionJoda(mint),
                    (TimePosition) new TimePositionJoda(maxt));
        }
        bbox = new BoundingBoxImpl(minx, miny, maxx, maxy, DefaultGeographicCRS.WGS84);
    }

    @Override
    public BoundingBox getCollectionBoundingBox() {
        return bbox;
    }

    @Override
    public Extent<VerticalPosition> getCollectionVerticalExtent() {
        return vExtent;
    }

    @Override
    public Extent<TimePosition> getCollectionTimeExtent() {
        return tExtent;
    }
}
