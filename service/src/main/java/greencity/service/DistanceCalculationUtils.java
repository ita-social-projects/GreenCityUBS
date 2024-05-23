package greencity.service;

public final class DistanceCalculationUtils {
    private static final double EARTH_RADIUS_IN_KM = 6371.0;

    private DistanceCalculationUtils() {
    }

    /**
     * Calculates the distance between two points on the Earth using the Haversine
     * formula.
     *
     * @param point1Lat  Latitude of the first point.
     * @param point1Long Longitude of the first point.
     * @param point2Lat  Latitude of the second point.
     * @param point2Long Longitude of the second point.
     * @return The distance between the two points in kilometers.
     */
    public static double calculateDistanceInKmByHaversineFormula(double point1Lat, double point1Long,
        double point2Lat, double point2Long) {
        double differenceLatInRad = Math.toRadians(point2Lat - point1Lat);
        double differenceLongInRad = Math.toRadians(point2Long - point1Long);

        double point1LatInRad = Math.toRadians(point1Lat);
        double point2LatInRad = Math.toRadians(point2Lat);

        double angle = haversine(differenceLatInRad) + Math.cos(point1LatInRad)
            * Math.cos(point2LatInRad) * haversine(differenceLongInRad);
        return EARTH_RADIUS_IN_KM * 2 * Math.atan2(Math.sqrt(angle), Math.sqrt(1 - angle));
    }

    /**
     * Helper method to calculate the haversine of a distance.
     *
     * @param distance The distance in radians.
     * @return The haversine of the distance.
     */
    private static double haversine(double distance) {
        return Math.pow(Math.sin(distance / 2), 2);
    }
}
