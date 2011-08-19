package uk.ac.rdg.resc.edal.cdm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.Variable;
import ucar.nc2.VariableSimpleIF;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dt.grid.GridDataset;
import ucar.nc2.units.SimpleUnit;
import uk.ac.rdg.resc.edal.Phenomenon;
import uk.ac.rdg.resc.edal.PhenomenonVocabulary;
import uk.ac.rdg.resc.edal.Unit;
import uk.ac.rdg.resc.edal.UnitVocabulary;
import uk.ac.rdg.resc.edal.coverage.AbstractDiscreteSimpleCoverage;
import uk.ac.rdg.resc.edal.coverage.GridSeriesCoverage;
import uk.ac.rdg.resc.edal.coverage.RangeMetadata;
import uk.ac.rdg.resc.edal.coverage.RangeMetadataImpl;
import uk.ac.rdg.resc.edal.coverage.domain.GridSeriesDomain;
import uk.ac.rdg.resc.edal.coverage.domain.impl.GridSeriesDomainImpl;
import uk.ac.rdg.resc.edal.coverage.grid.GridCell4D;
import uk.ac.rdg.resc.edal.coverage.grid.VerticalAxis;
import uk.ac.rdg.resc.edal.position.GeoPosition;

public class NcGridSeriesCoverage extends AbstractDiscreteSimpleCoverage<GeoPosition, GridCell4D, Double> implements
        GridSeriesCoverage<Double> {

    private Variable variable;
    private Variable time;

    public NcGridSeriesCoverage(String location, String variable) throws IOException, InvalidRangeException {
        NetcdfDataset nc = openDataset(location);
        for (Variable v : nc.getVariables()) {
            if (v.getName().equals(variable)) {
                this.variable = v;
                for(int i=0; i<this.variable.getRank(); i++){
                    Dimension d = this.variable.getDimension(i);
                    Variable cv = nc.findVariable(d.getName());
                    if((cv != null) && cv.isCoordinateVariable()){
                        String units = cv.getUnitsString().toLowerCase();
                        if(units.equals("degrees_north") || 
                               units.equals("degree_north") || 
                               units.equals("degrees_n") || 
                               units.equals("degree_n") || 
                               units.equals("degreesn") || 
                               units.equals("degreen")){
                            initLatitudeAxis(cv.read());
                        }
                        if(units.equals("degrees_east") || 
                                units.equals("degree_east") || 
                                units.equals("degrees_e") || 
                                units.equals("degree_e") || 
                                units.equals("degreese") || 
                                units.equals("degreee")){
                            initLongitudeAxis(cv.read());
                        }
                        if(cv.findAttribute("positive") != null ||
                                units.equals("bar") ||
                                units.equals("millibar") ||
                                units.equals("decibar") ||
                                units.equals("atmosphere") ||
                                units.equals("atm") ||
                                units.equals("pascal") ||
                                units.equals("pa") ||
                                units.equals("hpa")){
                            // TODO check for more acceptable units of pressure
                            initDepthAxis(cv.read());
                        }
                        if(SimpleUnit.isTimeUnit(units)){
                            initTimeAxis(cv.read());
                        }
                        System.out.println(cv.getName()+","+cv.getUnitsString());
                        Array coordinateArray = cv.read();
                        System.out.println();
                    }
                }
                return;
            }
        }
        for (Variable v : nc.getVariables()) {
            System.out.println(v.getName());
        }
        throw new InvalidRangeException(variable+" is not a valid variable for this dataset.");
    }

    private void initLatitudeAxis(Array coordinateArray) {
        System.out.println("Latitude axis:");
        long size = coordinateArray.getSize();
        double[] axisVals = (double[]) coordinateArray.get1DJavaArray(Double.class);
        for(int j=0; j<size; j++){
            System.out.print(axisVals[j]+",");
        }
    }

    private void initLongitudeAxis(Array coordinateArray) {
        System.out.println("Longitude axis:");
        long size = coordinateArray.getSize();
        double[] axisVals = (double[]) coordinateArray.get1DJavaArray(Double.class);
        for(int j=0; j<size; j++){
            System.out.print(axisVals[j]+",");
        }
        // TODO Auto-generated method stub
        
    }

    private void initDepthAxis(Array coordinateArray) {
        System.out.println("Vertical axis:");
        long size = coordinateArray.getSize();
        double[] axisVals = (double[]) coordinateArray.get1DJavaArray(Double.class);
        for(int j=0; j<size; j++){
            System.out.print(axisVals[j]+",");
        }
        // TODO Auto-generated method stub
        
    }

    private void initTimeAxis(Array coordinateArray) {
        System.out.println("Time axis:");
        long size = coordinateArray.getSize();
        double[] axisVals = (double[]) coordinateArray.get1DJavaArray(Double.class);
        for(int j=0; j<size; j++){
            System.out.print(axisVals[j]+",");
        }
        // TODO Auto-generated method stub
        
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
    private static NetcdfDataset openDataset(String location) throws IOException {
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

    @Override
    protected RangeMetadata getRangeMetadata() {
        RangeMetadata metadata = new RangeMetadataImpl(variable.getDescription(),
                                       Phenomenon.getPhenomenon(variable.getName(), PhenomenonVocabulary.CLIMATE_AND_FORECAST),
                                       Unit.getUnit(variable.getUnitsString(), UnitVocabulary.UDUNITS));
        return metadata;
    }

    @Override
    public Double evaluate(int tindex, int zindex, int yindex, int xindex) {
        List<Range> ranges = new ArrayList<Range>();
        Double ret=null;
        try {
            for(Dimension d : variable.getDimensions()){
                // TODO check CF conventions to see if these are standard
                if(d.getName().equalsIgnoreCase("lon")){
                    ranges.add(new Range(xindex, xindex));
                }
                if(d.getName().equalsIgnoreCase("lat")){
                    ranges.add(new Range(yindex, yindex));
                }
                if(d.getName().equalsIgnoreCase("depth")){
                    ranges.add(new Range(zindex, zindex));
                }
                if(d.getName().equalsIgnoreCase("time")){
                    ranges.add(new Range(tindex, tindex));
                }
            }
            Array a = variable.read(ranges);
            if(a.hasNext()){
                ret = new Double((Float) a.next());
            }
        } catch (InvalidRangeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ret;
    }

    @Override
    public GridSeriesDomain getDomain() {
//        VerticalAxis vAxis = new VerticalAxisImpl();
//        variable.getDimension(0).
//        GridSeriesDomainImpl ret = new GridSeriesDomainImpl(hGrid, vAxis, tAxis);
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Double> getValues() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getDescription() {
        // TODO correct description?
        return variable.getDescription();
    }

    public void test() throws IOException {
        System.out.println(variable.getDescription());
        System.out.println(variable.getNameAndDimensions());
        System.out.println(variable.getSize());
        System.out.println(variable.getRank());
        for(int i=0; i<variable.getRank(); i++){
            System.out.println("\t"+variable.getShape(i));
        }
        
        /*
         * Example of where to get getValues from
         */
        Array a = variable.read();
        float[][][] data = (float[][][]) a.copyToNDJavaArray();
//        for(int i=0; i<variable.getShape(0); i++){
//            for(int j=0; j<variable.getShape(1); j++){
//                for(int k=0; k<variable.getShape(2); k++){
//                    System.out.println(data[i][j][k]+":"+evaluate(0, i, j, k));
//                }
//            }
//        }
    }

    public static void main(String[] args) throws IOException, InvalidRangeException {
//        NcGridSeriesCoverage ncReader = new NcGridSeriesCoverage("/home/guy/Data/FOAM_20061215.0.nc", "TMP");
        NcGridSeriesCoverage ncReader = new NcGridSeriesCoverage("/home/guy/Data/POLCOMS_MRCS_NOWCAST_20091115.nc", "SALTY");
        ncReader.test();
    }
}
