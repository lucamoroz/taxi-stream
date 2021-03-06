package utils;

public class CoordinateHelper {
    private static final int EARTH_RADIUS = 6371000;

    /**
     * @return distance in meters
     */
    public static double calculateDistance(TaxiLog t1, TaxiLog t2) {
        double phi1 = (t1.getLatitude() / 360) * 2 * Math.PI;
        double phi2 = (t2.getLatitude() / 360) * 2 * Math.PI;
        double lambda1 = (t1.getLongitude() / 360) * 2 * Math.PI;
        double lambda2 = (t2.getLongitude() / 360) * 2 * Math.PI;

        return 2 * EARTH_RADIUS * Math.asin(
                Math.sqrt(Math.pow(Math.sin((Math.abs(phi2 - phi1))/2), 2) +
                        Math.cos(phi1) * Math.cos(phi2)*Math.pow(Math.sin((Math.abs(lambda2 - lambda1))/2),2)));
    }
}