package global_planning.astar;

import global_planning.graph.Graph;
import global_planning.graph.Node;

import java.util.ArrayList;

public class AStarDriver {
    private Graph graph;
    private AStar aStar;
    private AStarNode srcNode;
    private boolean isDestinationSelected;
    private ArrayList<Node> path;

    public AStarDriver(Graph graph) {
        this.graph = graph;
        aStar = new AStar(graph.getIntersectionNodes());
    }

    public Node selectSource(double lat, double lon) {
        path = null;
        Node srcGraphNode = graph.selectSourceNode(lat, lon);
        if (srcGraphNode == null) return null;
        srcNode = new AStarNode(srcGraphNode);
        return srcGraphNode;
    }

    public Node selectDestination(double lat, double lon) {
        path = null;
        Node dstGraphNode = graph.augmentDestinationNode(lat, lon);
        if (dstGraphNode == null) {
            System.err.println("Can't augment destination node in the graph");
            return null;
        }
        aStar.setDestination(new AStarNode(dstGraphNode));
        isDestinationSelected = true;
        return dstGraphNode;
    }

    public ArrayList<Node> findPath() {
        if (path != null)
            return path;
        if (srcNode == null) {
            System.err.println("Source is not selected");
            return null;
        }
        if (!isDestinationSelected) {
            System.err.println("Destination is not selected");
            return null;
        }
        path = aStar.run(srcNode);
        return path;
    }

    public ArrayList<Node> findPath(double srcLat, double srcLon, double dstLat, double dstLon) {
        if (selectSource(srcLat, srcLon) == null) return null;
        if (selectDestination(dstLat, dstLon) == null) return null;
        path = aStar.run(srcNode);
        return path;
    }
}
