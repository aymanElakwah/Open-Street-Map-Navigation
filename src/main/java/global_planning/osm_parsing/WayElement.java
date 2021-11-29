package global_planning.osm_parsing;

import java.util.ArrayList;

public class WayElement {
    private long id;
    private ArrayList<Long> nodes = new ArrayList<>();
    private boolean highway;
    private boolean oneWay;

    public WayElement(long id) {
        this.id = id;
    }

    public void addNode(long id) {
        nodes.add(id);
    }

    public long getId() {
        return id;
    }

    public ArrayList<Long> getNodes() {
        return nodes;
    }

    public void setHighway(boolean highway) {
        this.highway = highway;
    }

    public void setOneWay(boolean oneWay) {
        this.oneWay = oneWay;
    }

    public boolean isHighway() {
        return highway;
    }

    public boolean isOneWay() {
        return oneWay;
    }
}
