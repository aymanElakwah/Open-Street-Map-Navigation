package global_planning.astar;

import global_planning.graph.Node;
import global_planning.graph.Way;
import global_planning.graph.WayToNode;

import java.util.*;

class AStar {
    private HashMap<Long, AStarNode> nodes = new HashMap<>();
    private HashSet<Long> settledNodes = new HashSet<>();
    private PriorityQueue<AStarNode> queue = new PriorityQueue<>();
    private AStarNode dstNode;


    AStar(Collection<Node> intersectionNodes) {
        for (Node node : intersectionNodes)
            this.nodes.put(node.getId(), new AStarNode(node));
    }

    void setDestination(AStarNode dst) {
        dstNode = dst;
        dstNode.setActualDistanceFromSource(Double.POSITIVE_INFINITY);
        this.nodes.put(dstNode.getId(), dstNode);
        for (AStarNode node : nodes.values()) {
            node.setDestination(dstNode.getGraphNode());
            node.setActualDistanceFromSource(Double.POSITIVE_INFINITY);
        }
    }

    ArrayList<Node> run(AStarNode srcNode) {
        long timeMs = System.currentTimeMillis();
        srcNode.setActualDistanceFromSource(0);
        AStarNode nodeToVisit = srcNode;
        while (nodeToVisit != null && nodeToVisit != dstNode) {
            visit(nodeToVisit);
            nodeToVisit = queue.poll();
        }
        queue.clear();
        settledNodes.clear();
        if (nodeToVisit == null) {
            System.err.println("No path found between source and destination");
            return null;
        }
        System.out.println("Distance: " + dstNode.getActualDistanceFromSource());
        System.out.println("Time(ms): " + (System.currentTimeMillis() - timeMs));
        return getCompletePath(srcNode);
    }

    private ArrayList<Node> getPath(AStarNode srcNode) {
        ArrayList<Node> path = new ArrayList<>();
        AStarNode node = dstNode;
        while (node != srcNode) {
            path.add(node.getGraphNode());
            node = node.getParent();
        }
        path.add(srcNode.getGraphNode());
        Collections.reverse(path);
        return path;
    }

    private ArrayList<Node> getCompletePath(AStarNode srcNode) {
        ArrayList<Node> simplePath = getPath(srcNode);
        ArrayList<Node> completePath = new ArrayList<>();
        completePath.add(simplePath.get(0));
        for (int i = 0; i < simplePath.size() - 1; i++) {
            completePath.addAll(simplePath.get(i).getNodesTo(simplePath.get(i + 1)));
        }
        completePath.add(simplePath.get(simplePath.size() - 1));
        return completePath;
    }

    private void visit(AStarNode node) {
        Way currentWay = node.getCurrentWay();
        settledNodes.add(node.getId());
        for (Map.Entry<Node, WayToNode> entry : node.getGraphNode().getConnectedNodes().entrySet()) {
            Node connectedNode = entry.getKey();
            if (settledNodes.contains(connectedNode.getId())) continue;
            double distance = node.getActualDistanceFromSource() + entry.getValue().getDistance();
            distance += currentWay != null && currentWay != entry.getValue().getWay() ? 20 : 0;
            AStarNode aStarConnectedNode = nodes.get(connectedNode.getId());
            if (distance < aStarConnectedNode.getActualDistanceFromSource()) {
                aStarConnectedNode.setActualDistanceFromSource(distance);
                aStarConnectedNode.setParent(node);
                queue.add(aStarConnectedNode);
            }
        }
    }

}
