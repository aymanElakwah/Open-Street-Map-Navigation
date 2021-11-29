package global_planning;

import com.graphhopper.reader.ReaderElement;
import com.graphhopper.reader.osm.OSMInputFile;
import global_planning.graph.Graph;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;

public class ReadMap {

    private static void readFile(File file, Graph graph) throws IOException, XMLStreamException {
        final OSMInputFile osmInputFile = new OSMInputFile(file).setWorkerThreads(2).open();
        ReaderElement element;
        while ((element = osmInputFile.getNext()) != null) {
            int type = element.getType();
            if (type == ReaderElement.NODE) {
                graph.insertNode(element);
            } else if (type == ReaderElement.WAY) {
                graph.insertWay(element);
            }
        }
    }

    public static Graph buildGraph(String filename) {
        Graph graph = new Graph();
        try {
            File file;
            if (Main.jarFolder == null) {
                file = new File(filename);
            } else {
                file = new File(Main.jarFolder, filename);
            }
            readFile(file, graph);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        graph.connectNodes();
        return graph;
    }
}
