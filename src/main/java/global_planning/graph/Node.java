package global_planning.graph;

import global_planning.Utils;

import java.util.HashMap;
import java.util.List;

public class Node {
    private long id;
    private double lat, lon;
    private double X;
    private double Y;
    private int count;
    private HashMap<Node, WayToNode> connectedNodes = new HashMap<>();

    public Node(long id, double lat, double lon) {
        this.id = id;
        this.lat = lat;
        this.lon = lon;
        this.X = Utils.lonToX(lon);
        this.Y = Utils.latToY(lat);
    }

    public Node(double lat, double lon) {
        this(0, lat, lon);
    }

    public boolean isIntersection() {
        return count > 1 || !connectedNodes.isEmpty();
    }

    public void increment() {
        count++;
    }

    public void connect(WayToNode wayToTarget) {
        Node target = wayToTarget.getNode();
        if (!connectedNodes.containsKey(target)) {
            connectedNodes.put(target, wayToTarget);
            return;
        }
        if (wayToTarget.getDistance() < connectedNodes.get(target).getDistance())
            connectedNodes.put(target, wayToTarget);
    }

    public void disconnect(Node node) {
        connectedNodes.remove(node);
    }

    public HashMap<Node, WayToNode> getConnectedNodes() {
        return connectedNodes;
    }

    public List<Node> getNodesTo(Node node) {
        return connectedNodes.get(node).getWayNodes();
    }

    public double calculateDistance(Node node) {
//        return Math.sqrt(Math.pow(getX() - node.getX(), 2) + Math.pow(getY() - node.getY(), 2));
        return Utils.distance(getLat(), getLon(), node.getLat(), node.getLon());

    }

    public double calculateDistance(double lat, double lon) {
//        return Math.sqrt(Math.pow(getX() - global_planning.Utils.lonToX(lon), 2) + Math.pow(getY() - global_planning.Utils.latToY(lat), 2));
        return Utils.distance(getLat(), getLon(), lat, lon);
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public double getLatRad() {
        return Math.toRadians(lat);
    }

    public double getLonRad() {
        return Math.toRadians(lon);
    }

    public double getX() {
        return X;
    }

    public double getY() {
        return Y;
    }

    public void setId(long id) {
        if (this.id == 0)
            this.id = id;
    }

    public long getId() {
        return id;
    }

    public int getCount() {
        return count;
    }

}
