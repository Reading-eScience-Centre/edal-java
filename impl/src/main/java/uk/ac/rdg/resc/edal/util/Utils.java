package uk.ac.rdg.resc.edal.util;


public class Utils {
    /**
     * Returns the smallest longitude value that is equivalent to the target
     * value and greater than the reference value. Therefore if {@code reference
     * == 10.0} and {@code target == 5.0} this method will return 365.0.
     */
    public static double getNextEquivalentLongitude(double reference, double target) {
        // Find the clockwise distance from the first value on this axis
        // to the target value. This will be a positive number from 0 to
        // 360 degrees
        double clockDiff = Utils.constrainLongitude360(target - reference);
        return reference + clockDiff;
    }

    /**
     * Returns a longitude value in degrees that is equal to the given value but
     * in the range (-180:180]. In this scheme the anti-meridian is represented
     * as +180, not -180.
     */
    public static double constrainLongitude180(double value) {
        double val = constrainLongitude360(value);
        return val > 180.0 ? val - 360.0 : val;
    }

    /**
     * Returns a longitude value in degrees that is equal to the given value but
     * in the range [0:360]
     */
    public static double constrainLongitude360(double value) {
        double val = value % 360.0;
        return val < 0.0 ? val + 360.0 : val;
    }
}
