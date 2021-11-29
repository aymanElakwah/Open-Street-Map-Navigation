package global_planning.graph;

import global_planning.Utils;

import java.util.ArrayList;

public class NearestNode {
    private Way way;
    private Node node;
    private double distance;
    private Node[] surroundingNodes;
    private static final double EPSILON = 4.89084589e-8;

    public NearestNode(Way way, Node node, double distance, Node[] surroundingNodes) {
        this.way = way;
        this.node = node;
        this.distance = distance;
        this.surroundingNodes = surroundingNodes;
    }

    public Way getWay() {
        return way;
    }

    public Node getNode() {
        return node;
    }

    public double getDistance() {
        return distance;
    }

    public Node[] getSurroundingNodes() {
        return surroundingNodes;
    }

    static NearestNode findNearestNode(Way way, double x, double y) {
        ArrayList<Node> nodes = way.getNodes();
        double nearestX = 0, nearestY = 0;
        double minDistance = Double.POSITIVE_INFINITY;
        Node[] surroundingNodes = new Node[2];
        for (int i = 0; i < nodes.size() - 1; i++) {
            Node node1 = nodes.get(i);
            Node node2 = nodes.get(i + 1);
            double a = node2.getY() - node1.getY();
            double b = -(node2.getX() - node1.getX());
            double c = ((node2.getX() - node1.getX()) * node1.getY() - a * node1.getX());
            double a2b2 = a * a + b * b;
            double nearestXLocal = (b * (b * x - a * y) - a * c) / a2b2;
            double nearestYLocal = (a * (-b * x + a * y) - b * c) / a2b2;
            // Check if the nearest point is not the way segment
            if (!areOrdered(node1.getX(), nearestXLocal, node2.getX())
                    || !areOrdered(node1.getY(), nearestYLocal, node2.getY())) continue;
            double distance = Math.abs(a * x + b * y + c) / Math.sqrt(a2b2);
            if (distance < minDistance) {
                minDistance = distance;
                nearestX = nearestXLocal;
                nearestY = nearestYLocal;
                surroundingNodes[0] = node1;
                surroundingNodes[1] = node2;
            }
        }
        if (minDistance == Double.POSITIVE_INFINITY)
            return null;
        Node nearestNode = new Node(Utils.yToLat(nearestY), Utils.xToLon(nearestX));
        return new NearestNode(way, nearestNode, minDistance, surroundingNodes);
    }

    private static boolean areOrdered(double a, double b, double c) {
        return (c + EPSILON >= b && b >= a - EPSILON || a + EPSILON > b && b > c - EPSILON);
    }
}
