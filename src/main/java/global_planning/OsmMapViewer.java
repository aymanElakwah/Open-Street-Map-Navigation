package global_planning;

import global_planning.astar.AStarDriver;
import global_planning.graph.Graph;
import global_planning.graph.Node;
import org.openstreetmap.gui.jmapviewer.*;
import org.openstreetmap.gui.jmapviewer.events.JMVCommandEvent;
import org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate;
import org.openstreetmap.gui.jmapviewer.interfaces.JMapViewerEventListener;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;


/**
 * A class to start a Swing application which shows a map and is able to interact with imported OSM data.
 */
public class OsmMapViewer extends JFrame implements JMapViewerEventListener {

    private static final long serialVersionUID = 1L;

    private JMapViewerTree treeMap;
    private JLabel zoomValue;
    private JLabel metersPerPixelsValue;
    private AStarDriver aStar;
    private boolean selectingSrc = true;

    /**
     * Constructs the {@code Viewer}.
     */
    public OsmMapViewer() {
        super("JMapViewer Demo");
        treeMap = new JMapViewerTree("Zones", false);
        setupJFrame();
        setupBasicPanels();

        // Listen to the map viewer for user operations so components will
        // receive events and updates
        map().addJMVListener(this);

        // Set some options, e.g. tile source and that markers are visible
        map().setTileSource(new OsmTileSource.Mapnik());
        OsmTileLoader loader = new OsmTileLoader(map());
        loader.headers.put("User-Agent", "Mozilla/5.0 (platform; rv:geckoversion) Gecko/geckotrail Firefox/firefoxversion");
        map().setTileLoader(loader);
        map().setMapMarkerVisible(true);
        map().setZoomContolsVisible(true);

        // activate map
        add(treeMap, BorderLayout.CENTER);

        map().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ICoordinate position = map().getPosition(e.getPoint());
                if (selectingSrc) {
                    map().removeAllMapPolygons();
                    map().removeAllMapMarkers();
                    Node srcNode = aStar.selectSource(position.getLat(), position.getLon());
                    if (srcNode != null) {
                        drawPoint(position.getLat(), position.getLon());
                        System.out.println("Source selected at " + position.getLat() + ", " + position.getLon());
                        selectingSrc = false;
                    }
                } else {
                    Node dstNode = aStar.selectDestination(position.getLat(), position.getLon());
                    if (dstNode == null) return;
                    System.out.println("Destination selected at " + position.getLat() + ", " + position.getLon());
                    drawPoint(position.getLat(), position.getLon());
                    selectingSrc = true;
                    ArrayList<Node> path = aStar.findPath();
                    if (path != null) {
                        drawPath(path);
                        System.out.println(Main.pathOutput("", path)[0]);
                    }
                }
            }
        });

        Graph graph = ReadMap.buildGraph("october.pbf");
        aStar = new AStarDriver(graph);
        System.out.println(graph.getIntersectionNodes().size());
        System.out.println(graph.getEdgeCount());
        map().setZoom(17);
        map().setCenter(new Point(19659141, 13849292));
    }

    private void setupJFrame() {
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    private void setupBasicPanels() {
        JPanel panel = new JPanel();
        add(panel, BorderLayout.PAGE_START);

        JPanel panelTop = new JPanel();
        JLabel metersPerPixelsLabel = new JLabel("Meters/Pixels: ");
        metersPerPixelsValue = new JLabel(String.format("%s", map().getMeterPerPixel()));
        JLabel zoomLabel = new JLabel("Zoom: ");
        zoomValue = new JLabel(String.format("%s", map().getZoom()));
        panelTop.add(zoomLabel);
        panelTop.add(zoomValue);
        panelTop.add(metersPerPixelsLabel);
        panelTop.add(metersPerPixelsValue);
        panel.add(panelTop, BorderLayout.CENTER);

        JPanel helpPanel = new JPanel();
        JLabel helpLabel = new JLabel("Use right mouse button to move,\n "
                + "left double click or mouse wheel to zoom.");
        helpPanel.add(helpLabel);
        add(helpPanel, BorderLayout.PAGE_END);
    }

    private JMapViewer map() {
        return treeMap.getViewer();
    }

    private void updateZoomParameters() {
        if (metersPerPixelsValue != null)
            metersPerPixelsValue.setText(String.format("%s", map().getMeterPerPixel()));
        if (zoomValue != null)
            zoomValue.setText(String.format("%s", map().getZoom()));
    }

    private ICoordinate c(double lat, double lon) {
        return new Coordinate(lat, lon);
    }

    private ICoordinate c(Node node) {
        return new Coordinate(node.getLat(), node.getLon());
    }

    public void drawPoint(double lat, double lon) {
        map().addMapMarker(new MapMarkerCircle(lat, lon, 0.00001));
    }

    public void drawPoint(Node node) {
        drawPoint(node.getLat(), node.getLon());
    }


    public void drawLine(Node first, Node second) {
        ICoordinate c1 = c(first);
        ICoordinate c2 = c(second);
        map().addMapPolygon(new MapPolygonImpl(c1, c2, c2));
    }

    public void drawPath(List<Node> path) {
        for (int i = 0; i < path.size() - 1; i++) {
            drawLine(path.get(i), path.get(i + 1));
            drawPoint(path.get(i));
        }
        drawPoint(path.get(path.size() - 1));
    }

    @Override
    public void processCommand(JMVCommandEvent command) {
        if (command.getCommand().equals(JMVCommandEvent.COMMAND.ZOOM) ||
                command.getCommand().equals(JMVCommandEvent.COMMAND.MOVE)) {
            updateZoomParameters();
        }
    }

    public static void main(String[] args) throws Exception {
        OsmMapViewer osmMapViewer = new OsmMapViewer();
        osmMapViewer.setVisible(true);
    }

}
