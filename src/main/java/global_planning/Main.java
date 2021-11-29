package global_planning;

import global_planning.astar.AStarDriver;
import global_planning.graph.Node;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    public static File jarFolder;

    public static void main(String[] args) {
        System.err.close();
        jarFolder = getJarFolder();
        AStarDriver aStar = new AStarDriver(ReadMap.buildGraph("map.pbf"));
        Scanner scanner = new Scanner(System.in);
        ArrayList<Node> carToPickup;
        ArrayList<Node> pickupToDst;
        while (true) {
            double carLat = scanner.nextDouble();
            double carLon = scanner.nextDouble();
            double pickupLat = scanner.nextDouble();
            double pickupLon = scanner.nextDouble();
            carToPickup = aStar.findPath(carLat, carLon, pickupLat, pickupLon);
            double dstLat = scanner.nextDouble();
            double dstLon = scanner.nextDouble();
            pickupToDst = aStar.findPath(pickupLat, pickupLon, dstLat, dstLon);
            String[] carToPickupPathOutput = pathOutput("carToPickup", carToPickup);
            String[] pickupToDstPathOutput = pathOutput("pickupToDst", pickupToDst);
            System.out.println(String.format("{%s,%s}", carToPickupPathOutput[0], pickupToDstPathOutput[0]));
            System.out.println(String.format("%s %s", carToPickupPathOutput[1], pickupToDstPathOutput[1]));
        }
    }

    public static String[] pathOutput(String pathName, ArrayList<Node> path) {
        if (path == null)
            path = new ArrayList<>();
        String[] pathOutput = new String[2];
        StringBuilder lats = new StringBuilder();
        StringBuilder lons = new StringBuilder();
        StringBuilder xy = new StringBuilder();
        xy.append(path.size()).append(" ");
        for (int i = 0; i < path.size(); i++) {
            Node node = path.get(i);
            lats.append(String.format("%.5f", node.getLat()));
            lons.append(String.format("%.5f", node.getLon()));
            Point2D gazeboXY = GazeboConversion.gazeboXY(node.getLat(), node.getLon());
            xy.append(gazeboXY.getX()).append(" ").append(gazeboXY.getY());
            if (i != path.size() - 1) {
                lats.append(',');
                lons.append(',');
                xy.append(' ');
            }
        }
        pathOutput[0] = String.format("\"%sLat\": [%s],\"%sLon\": [%s]", pathName, lats.toString(), pathName, lons.toString());
        pathOutput[1] = xy.toString();
        return pathOutput;
    }

    private static File getJarFolder() {
        try {
            return new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

}
