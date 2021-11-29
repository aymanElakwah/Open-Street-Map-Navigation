package global_planning;

public class Utils {

    public static double distance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c * 1000;
    }

    /**
     * Convert longitude to Mercator projection.
     */
    public static double lonToX(double longitude) {
        return (longitude + 180.0) / 360.0;
    }

    /**
     * Convert from Mercator projection to longitude.
     */
    public static double xToLon(double x) {
        return 360.0 * (x - 0.5);
    }

    /**
     * Convert latitude to Mercator projection.
     */
    public static double latToY(double latitude) {
        double sinLat = Math.sin(Math.toRadians(latitude));
        return (Math.log((1.0 + sinLat) / (1.0 - sinLat)) / (4.0 * Math.PI) + 0.5);
    }

    /**
     * Convert from Mercator projection (range [0..1]) to latitude.
     */
    public static double yToLat(double y) {
        return 360.0 * Math.atan(Math.exp((y - 0.5) * (2.0 * Math.PI))) / Math.PI - 90.0;
    }

}
