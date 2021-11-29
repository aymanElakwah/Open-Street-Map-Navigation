package global_planning;

public class GazeboConversion {
    private static final double osm2WorldScaleFactor = 3.469653620064874E7;
    private static final double osm2WorldOriginX = 2.035619274593575E7;
    private static final double osm2WorldOriginY = 2.038461064403214E7;
    private static final double originX = 647;
    private static final double originY = -140;
    private static final double worldScale = 4;


    public static Point2D gazeboXY(double lat, double lon) {
        double x = worldScale * (Utils.lonToX(lon) * osm2WorldScaleFactor - osm2WorldOriginX) - originX;
        double y = worldScale * (Utils.latToY(lat) * osm2WorldScaleFactor - osm2WorldOriginY) - originY;
        return new Point2D(x, y);
    }

    public static Point2D gpsLocation(double gazeboX, double gazeboY) {
        double lon = Utils.xToLon(((gazeboX + originX) / worldScale + osm2WorldOriginX) / osm2WorldScaleFactor);
        double lat = Utils.yToLat(((gazeboY + originY) / worldScale + osm2WorldOriginY) / osm2WorldScaleFactor);
        return new Point2D(lat, lon);
    }
}
