package global_planning.graph;

import java.util.ArrayList;
import java.util.List;

public class WayToNode {
    private Way way;
    private int startNode;
    private int targetNode;
    private double distance;

    public WayToNode(Way way, int startNodeIndex, int targetNodeIndex) {
        init(way, startNodeIndex, targetNodeIndex);
        this.distance = way.getNode(startNodeIndex).calculateDistance(getNode());
    }

    public WayToNode(Way way, int startNodeIndex, int targetNodeIndex, double distance) {
        init(way, startNodeIndex, targetNodeIndex);
        this.distance = distance;
    }

    private void init(Way way, int startNodeIndex, int targetNodeIndex) {
        this.way = way;
        this.startNode = startNodeIndex;
        this.targetNode = targetNodeIndex;
    }

    public List<Node> getWayNodes() {
        if (startNode > targetNode) {
            List<Node> nodes = way.getNodes().subList(targetNode + 1, startNode + 1);
            List<Node> reverseOrder = new ArrayList<>(nodes.size());
            for (int i = 0; i < nodes.size(); i++) {
                reverseOrder.add(nodes.get(nodes.size() - i - 1));
            }
            return reverseOrder;
        }
        return way.getNodes().subList(startNode, targetNode);
    }

    public Node getNode() {
        return way.getNode(targetNode);
    }

    public Way getWay() {
        return way;
    }

    public double getDistance() {
        return distance;
    }

}
