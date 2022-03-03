package uk.ac.rdg.resc.edal.covjson;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.sis.referencing.CommonCRS;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import uk.ac.rdg.resc.edal.domain.TrajectoryDomain;
import uk.ac.rdg.resc.edal.feature.Feature;
import uk.ac.rdg.resc.edal.feature.TrajectoryFeature;
import uk.ac.rdg.resc.edal.metadata.Parameter;
import uk.ac.rdg.resc.edal.position.GeoPosition;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.VerticalCrsImpl;
import uk.ac.rdg.resc.edal.position.VerticalPosition;
import uk.ac.rdg.resc.edal.util.Array1D;
import uk.ac.rdg.resc.edal.util.ValuesArray1D;

public class Test2 {
    private static final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private static final CoverageJsonConverter converter = new CoverageJsonConverterImpl();

    public static void main(String[] args) {
        Feature<?> feature = getTrajectoryFeature(10);
        converter.checkFeatureSupported(feature);
        converter.convertFeatureToJson(out, feature);
        System.out.println(out);
    }

    private static Feature<?> getTrajectoryFeature(int trajectorySize) {

        Array1D<Number> temperature_values = new ValuesArray1D(trajectorySize);
        for (int i = 0; i < trajectorySize; i++) {
            temperature_values.set(getRandomDoubleInRange(0, 30), i);
        }

        Map<String, Array1D<Number>> valuesMap = new HashMap<>();
        valuesMap.put(getTemperatureParam().getVariableId(), temperature_values);

        Map<String, Parameter> parameterMap = new HashMap<>();
        parameterMap.put(getTemperatureParam().getVariableId(), getTemperatureParam());

//        List<GeoPosition> geoPositions = getOrderedGeoPositions(trajectorySize);
        List<GeoPosition> geoPositions = getRandomGeoPositions(trajectorySize);
        return new TrajectoryFeature("some id", "some name", "some description", new TrajectoryDomain(geoPositions),
                parameterMap, valuesMap);
    }

    private static List<GeoPosition> getOrderedGeoPositions(int trajectorySize) {
        List<GeoPosition> geoPositions = new ArrayList<>();
        DateTime dt = new DateTime(2021, 7, 1, 1, 0, DateTimeZone.forID("UTC"));
        for (int i = 0; i < trajectorySize; i++) {
            geoPositions.add(createTestPoint(getRandomDoubleInRange(-175 + 10 * i, -175 + 10 * (i + 1)),
                    getRandomDoubleInRange(-85 + 10 * i, -85 + 10 * (i + 1)),
                    getRandomDoubleInRange(10 * i, 10 * (i + 1)), dt.plusHours(i)));
        }
        return geoPositions;
    }

    private static List<GeoPosition> getRandomGeoPositions(int trajectorySize) {
        List<GeoPosition> geoPositions = new ArrayList<>();
        DateTime dt = new DateTime(2021, 7, 1, 1, 0, DateTimeZone.forID("UTC"));
        for (int i = 0; i < trajectorySize; i++) {
            geoPositions.add(createTestPoint(getRandomDoubleInRange(-175, 175), getRandomDoubleInRange(-85, 85),
                    getRandomDoubleInRange(0, 100), dt.plusHours(i)));
        System.out.println(geoPositions.get(i).getHorizontalPosition());
        }
        
        return geoPositions;
    }

    private static Parameter getTemperatureParam() {
        return new Parameter("temperature", "Air Temperature", "temperature forecast at the location", "degreesC",
                "air_temperature");
    }

    static GeoPosition createTestPoint(double x, double y, double z, DateTime dt) {
        return new GeoPosition(new HorizontalPosition(x, y, CommonCRS.WGS84.geographic()),
                new VerticalPosition(z, new VerticalCrsImpl("m", false, false, true)), dt);
    }

    static double getRandomDoubleInRange(double min, double max) {
        Random r = new Random();
        return min + (max - min) * r.nextDouble();
    }
}
