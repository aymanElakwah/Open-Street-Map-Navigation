package global_planning.graph;

import java.util.ArrayList;

public class Way {
    private long id;
    private boolean isOneWay;

    private ArrayList<Node> nodes = new ArrayList<>();

    public Way(long id, boolean isOneWay) {
        this.id = id;
        this.isOneWay = isOneWay;
    }

    public long getId() {
        return id;
    }

    public boolean isOneWay() {
        return isOneWay;
    }


    public void addNode(Node node) {
        nodes.add(node);
    }

    public ArrayList<Node> getNodes() {
        return nodes;
    }

    public Node getNode(int index) {
        return nodes.get(index);
    }

}
