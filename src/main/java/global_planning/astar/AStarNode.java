package global_planning.astar;

import global_planning.graph.Node;
import global_planning.graph.Way;

public class AStarNode implements Comparable<AStarNode> {
    private Node graphNode;
    private AStarNode parent;
    private double actualDistanceFromSource;
    private double distanceToDestinationHeuristic;

    AStarNode(Node graphNode) {
        this.graphNode = graphNode;
    }

    public AStarNode(Node graphNode, Node dst) {
        this(graphNode);
        setDestination(dst);
    }

    long getId() {
        return graphNode.getId();
    }

    Node getGraphNode() {
        return graphNode;
    }

    AStarNode getParent() {
        return parent;
    }

    double getActualDistanceFromSource() {
        return actualDistanceFromSource;
    }

    void setActualDistanceFromSource(double actualDistanceFromSource) {
        this.actualDistanceFromSource = actualDistanceFromSource;
    }

    void setParent(AStarNode parent) {
        this.parent = parent;
    }

    void setDestination(Node dst) {
        distanceToDestinationHeuristic = graphNode.calculateDistance(dst);
    }

    private double getCost() {
        return actualDistanceFromSource + distanceToDestinationHeuristic;
    }

    public Way getCurrentWay() {
        if (getParent() == null)
            return null;
        return getParent().getGraphNode().getConnectedNodes().get(getGraphNode()).getWay();
    }

    @Override
    public int compareTo(AStarNode o) {
        return Double.compare(getCost(), o.getCost());
    }
}
