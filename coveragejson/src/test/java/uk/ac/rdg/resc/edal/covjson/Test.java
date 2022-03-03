package uk.ac.rdg.resc.edal.covjson;

import static org.joda.time.DateTime.now;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.sis.referencing.CommonCRS;

import uk.ac.rdg.resc.edal.feature.PointFeature;
import uk.ac.rdg.resc.edal.metadata.Parameter;
import uk.ac.rdg.resc.edal.position.GeoPosition;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.VerticalCrsImpl;
import uk.ac.rdg.resc.edal.position.VerticalPosition;
import uk.ac.rdg.resc.edal.util.Array1D;
import uk.ac.rdg.resc.edal.util.ValuesArray1D;

public class Test {
    public static void main(String[] args) {

        Parameter temperature_parameter = new Parameter(
                "1",
                "temperature",
                "definition of temperature",
                "degreesC",
                "temperature"
        );
        Parameter humidity_parameter = new Parameter(
                "2",
                "humidity",
                "definition of humidity",
                "percent",
                "humidity"
        );

        Array1D<Number> temperature_values = new ValuesArray1D(1);
        temperature_values.set(19, 0);
        
        Array1D<Number> humidity_values = new ValuesArray1D(1);
        humidity_values.set(80, 0);

        Map<String, Parameter> parameterMap = new HashMap<>();
        parameterMap.put("1", temperature_parameter);
        parameterMap.put("2", humidity_parameter);

        Map<String, Array1D<Number>> valuesMap = new HashMap<>();
        valuesMap.put("1", temperature_values);
        valuesMap.put("2", humidity_values);

        PointFeature feature = new PointFeature(
                "1",
                "point feature",
                "a test feature",
                new GeoPosition(
                        new HorizontalPosition(-1, 60, CommonCRS.defaultGeographic()),
                        new VerticalPosition(0, new VerticalCrsImpl("m", false, false, true)),
                        now()
                ),
                parameterMap,
                valuesMap
        );
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CoverageJsonConverter converter = new CoverageJsonConverterImpl();
        converter.checkFeatureSupported(feature);
        converter.convertFeatureToJson(out, feature);
        System.out.println(out);
    }
    
//    Array2D<Number> temperature_values = new ValuesArray2D(1, 1);
//    temperature_values.set(19, 0, 0);
//
//    Array2D<Number> humidity_values = new ValuesArray2D(1, 1);
//    humidity_values.set(80, 0, 0);
//
//    Map<String, Parameter> parameterMap = new HashMap<>();
//    parameterMap.put("temperature", temperature_parameter);
//    parameterMap.put("humidity", humidity_parameter);
//
//    Map<String, Array2D<Number>> valuesMap = new HashMap<>();
//    valuesMap.put("temperature", temperature_values);
//    valuesMap.put("humidity", humidity_values);
//
//    Feature<HorizontalPosition> feature = new MapFeature("1", "point feature", "a test feature",
//            new MapDomain(new RegularGridImpl(new BoundingBoxImpl(-1, 60, 0, 61), 1, 1), 0.0,
//                    new VerticalCrsImpl("m", false, false, true), now()),
//            parameterMap, valuesMap);
}