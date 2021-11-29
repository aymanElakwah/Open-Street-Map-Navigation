package global_planning.graph;

import com.carrotsearch.hppc.cursors.LongCursor;
import com.graphhopper.reader.ReaderElement;
import com.graphhopper.reader.ReaderNode;
import com.graphhopper.reader.ReaderWay;
import global_planning.Utils;
import global_planning.osm_parsing.WayElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Graph {
    private HashMap<Long, Node> allNodes = new HashMap<>();
    private HashSet<Node> intersectionNodes = new HashSet<>();
    private ArrayList<Way> ways = new ArrayList<>();
    private NearestNode srcNearestNode;
    private Node dstNode;
    private Way dstAugmentedWay;
    private int edgeCount;


    private Node processNode(ReaderElement element) {
        if (element.getType() != ReaderElement.NODE)
            return null;
        ReaderNode node = (ReaderNode) element;
        return new Node(element.getId(), node.getLat(), node.getLon());
    }

    private Way processWay(ReaderElement element) {
        if (element.getType() != ReaderElement.WAY)
            return null;
        ReaderWay wayReader = (ReaderWay) element;
        if (!wayReader.hasTagWithKeyPrefix("highway")) return null;
        if (wayReader.hasTag("highway", "footway")) return null;
        Way way = new Way(wayReader.getId(), wayReader.hasTag("oneway", "yes"));
        for (LongCursor nodeId : wayReader.getNodes()) {
            Node node = allNodes.get(nodeId.value);
            node.increment();
            way.addNode(node);
        }
        return way;
    }

    private Way processWay(WayElement wayElement) {
        if (!wayElement.isHighway()) return null;
        Way way = new Way(wayElement.getId(), wayElement.isOneWay());
        for (long nodeId : wayElement.getNodes()) {
            Node node = allNodes.get(nodeId);
            node.increment();
            way.addNode(node);
        }
        return way;
    }


    public void insertNode(ReaderElement element) {
        Node node = processNode(element);
        if (node == null) return;
        allNodes.put(element.getId(), node);
    }

    public void insertNode(Node node) {
        if (node == null) return;
        allNodes.put(node.getId(), node);
    }

    public void insertWay(ReaderElement element) {
        Way way = processWay(element);
        if (way == null) return;
        ways.add(way);
    }

    public void insertWay(WayElement wayElement) {
        Way way = processWay(wayElement);
        if (way == null) return;
        ways.add(way);
    }

    public void connectNodes() {
        for (Way way : ways) {
            Node nodeToConnect = null;
            int nodeToConnectIndex = -1;
            ArrayList<Node> nodes = way.getNodes();
            double accumulatedDistance = 0;
            for (int i = 0; i < nodes.size(); i++) {
                Node node = nodes.get(i);
                if (nodeToConnect != null) {
                    accumulatedDistance += nodes.get(i - 1).calculateDistance(node);
                }
                if (node.isIntersection()) {
                    intersectionNodes.add(node);
                    if (nodeToConnect != null) {
                        edgeCount++;
                        nodeToConnect.connect(new WayToNode(way, nodeToConnectIndex, i, accumulatedDistance));
                        if (!way.isOneWay()) {
                            edgeCount++;
                            node.connect(new WayToNode(way, i, nodeToConnectIndex, accumulatedDistance));
                        }
                    }
                    nodeToConnect = node;
                    nodeToConnectIndex = i;
                    accumulatedDistance = 0;
                }
            }
        }
        allNodes.clear();
    }

    public Node selectSourceNode(double lat, double lon) {
        srcNearestNode = findNearestNode(lat, lon);
        if (srcNearestNode == null) return null;
        srcNearestNode.getNode().setId(-1);
        return srcNearestNode.getNode();
    }

    private boolean connectSourceNode() {
        if (srcNearestNode == null) {
            System.err.println("Source node is not selected");
            return false;
        }
        disconnectSourceNode();
        Way srcWay = srcNearestNode.getWay();
        if (srcWay.getId() == dstAugmentedWay.getId()) {
            // source and destination are in the same way
            if (connectSourceNodeShortcut()) return true;
        }
        Node[] surroundingNodes = srcNearestNode.getSurroundingNodes();
        if (surroundingNodes == null) return false;
        int nodeToConnectIndex = -1;
        int nodeAfterSrcIndex = -1;
        ArrayList<Node> nodes = srcWay.getNodes();
        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            if (node == surroundingNodes[1]) {
                nodeAfterSrcIndex = i;
            }
            if (node.isIntersection()) {
                if (nodeAfterSrcIndex != -1) {
                    srcNearestNode.getNode().connect(new WayToNode(srcWay, nodeAfterSrcIndex, i));
                    return true;
                }
                nodeToConnectIndex = i;
            }
            if (!srcWay.isOneWay() && node == surroundingNodes[0] && nodeToConnectIndex != -1) {
                srcNearestNode.getNode().connect(new WayToNode(srcWay, i, nodeToConnectIndex));
            }
        }
        return true;
    }

    // This method is called when source and destination are in the same way
    private boolean connectSourceNodeShortcut() {
        Node srcNode = srcNearestNode.getNode();
        NearestNode nearestNode = NearestNode.findNearestNode(dstAugmentedWay, srcNode.getX(), srcNode.getY());
        if (nearestNode == null)
            return false;
        Node[] surroundingNodes = nearestNode.getSurroundingNodes();
        ArrayList<Node> nodes = dstAugmentedWay.getNodes();
        int srcIndex = -1;
        // check if source lies before destination
        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            if (node == surroundingNodes[1])
                srcIndex = i;
            if (node == dstNode) {
                if (srcIndex == -1) {
                    if (dstAugmentedWay.isOneWay()) return false;
                    break;
                }
                srcNode.connect(new WayToNode(dstAugmentedWay, srcIndex, i));
                return true;
            }
        }
        // Source doesn't lie before destination and it is not one way
        int dstIndex = -1;
        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            if (node == dstNode)
                dstIndex = i;
            if (node == surroundingNodes[0]) {
                if (dstIndex == -1) return false;
                srcNode.connect(new WayToNode(dstAugmentedWay, i, dstIndex));
                return true;
            }
        }
        return false;
    }

    private void disconnectSourceNode() {
        if (srcNearestNode == null) return;
        srcNearestNode.getNode().getConnectedNodes().clear();
    }

    public Node augmentDestinationNode(double lat, double lon) {
        detachDestinationNode();
        NearestNode dstNearestNode = findNearestNode(lat, lon);
        if (dstNearestNode == null) return null;
        Way dstWay = dstNearestNode.getWay();
        if (dstWay == null) return null;
        Node[] surroundingNodes = dstNearestNode.getSurroundingNodes();
        if (surroundingNodes == null) return null;
        dstAugmentedWay = new Way(dstWay.getId(), dstWay.isOneWay());
        dstNode = dstNearestNode.getNode();
        dstNode.setId(-2);
        for (Node node : dstWay.getNodes()) {
            dstAugmentedWay.addNode(node);
            if (node == surroundingNodes[0])
                dstAugmentedWay.addNode(dstNode);
        }
        if (!connectSourceNode()) return null;
        connectDestinationNode();
        return dstNode;
    }

    private void connectDestinationNode() {
        int previousIntersectionNodeIndex = -1;
        int destinationNodeIndex = -1;
        ArrayList<Node> nodes = dstAugmentedWay.getNodes();
        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            if (node.isIntersection()) {
                previousIntersectionNodeIndex = i;
                if (destinationNodeIndex != -1) {
                    nodes.get(i).connect(new WayToNode(dstAugmentedWay, i, destinationNodeIndex));
                    // This is not a real connection. This is only used to disconnect the destination node after we reach it.
                    dstNode.connect(new WayToNode(dstAugmentedWay, destinationNodeIndex, i));
                    return;
                }
            } else if (node == dstNode) {
                if (previousIntersectionNodeIndex != -1) {
                    nodes.get(previousIntersectionNodeIndex).connect(new WayToNode(dstAugmentedWay, previousIntersectionNodeIndex, i));
                    // This is not a real connection. This is only used to disconnect the destination node after we reach it.
                    dstNode.connect(new WayToNode(dstAugmentedWay, i, previousIntersectionNodeIndex));
                }
                if (dstAugmentedWay.isOneWay()) return;
                destinationNodeIndex = i;
            }
        }
    }

    private void detachDestinationNode() {
        if (dstNode == null) return;
        for (Node node : dstNode.getConnectedNodes().keySet()) {
            node.disconnect(dstNode);
        }
        dstNode = null;
        dstAugmentedWay = null;
    }

    public NearestNode findNearestNode(double lat, double lon) {
        double x = Utils.lonToX(lon);
        double y = Utils.latToY(lat);
        double minDistance = Double.POSITIVE_INFINITY;
        NearestNode nearestNode = null;
        for (Way way : ways) {
            NearestNode nearestNodeInWay = NearestNode.findNearestNode(way, x, y);
            if (nearestNodeInWay == null) continue;
            if (nearestNodeInWay.getDistance() < minDistance) {
                minDistance = nearestNodeInWay.getDistance();
                nearestNode = nearestNodeInWay;
            }
        }
        return nearestNode;
    }

    public HashSet<Node> getIntersectionNodes() {
        return intersectionNodes;
    }

    public int getEdgeCount() {
        return edgeCount;
    }
}