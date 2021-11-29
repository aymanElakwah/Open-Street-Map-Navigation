package global_planning.osm_parsing;

import global_planning.Main;
import global_planning.graph.Graph;
import global_planning.graph.Node;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class OsmParser {

    public static Graph buildGraph(String filename) {
        File file;
        if (Main.jarFolder == null) {
            file = new File(filename);
        } else {
            file = new File(Main.jarFolder, filename);
        }
        BufferedReader bufferedReader = null;
        Graph graph = new Graph();
        try {
            bufferedReader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("<node"))
                    graph.insertNode(processNode(bufferedReader, line));
                else if (line.startsWith("<way"))
                    graph.insertWay(processWay(bufferedReader, line));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        graph.connectNodes();
        return graph;
    }

    private static void readCloseTag(String line, BufferedReader bufferedReader) throws IOException{
        if (line.endsWith("/>")) return;
        while ((line = bufferedReader.readLine()) != null && !line.endsWith("/>"));

    }


    private static Node processNode(BufferedReader bufferedReader, String line) throws IOException {
        int idBeginIndex = line.indexOf('"') + 1;
        long id = Long.parseLong(line.substring(idBeginIndex, line.indexOf('"', idBeginIndex)));
        int latBeginIndex = line.indexOf("lat=") + 5;
        int latEndIndex = line.indexOf('"', latBeginIndex);
        double lat = Double.parseDouble(line.substring(latBeginIndex, latEndIndex));
        int lonBeginIndex = line.indexOf('=', latEndIndex + 1) + 2;
        int lonEndIndex = line.indexOf('"', lonBeginIndex);
        double lon = Double.parseDouble(line.substring(lonBeginIndex, lonEndIndex));
        readCloseTag(line, bufferedReader);
        return new Node(id, lat, lon);

    }

    private static WayElement processWay(BufferedReader bufferedReader, String line) throws IOException {
        int idBeginIndex = line.indexOf('"') + 1;
        long id = Long.parseLong(line.substring(idBeginIndex, line.indexOf('"', idBeginIndex)));
        WayElement wayElement = new WayElement(id);
        while ((line = bufferedReader.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("<nd")) {
                int refIdBeginIndex = line.indexOf('"') + 1;
                wayElement.addNode(Long.parseLong(line.substring(refIdBeginIndex, line.indexOf('"', refIdBeginIndex))));
            } else if (line.startsWith("<tag")) {
                int keyBeginIndex = line.indexOf('=') + 2;
                int keyEndIndex = line.indexOf('"', keyBeginIndex);
                String key = line.substring(keyBeginIndex, keyEndIndex);
                int valueBeginIndex = line.indexOf('"', keyEndIndex + 1) + 1;
                String value = line.substring(valueBeginIndex, line.indexOf('"', valueBeginIndex));
                if (key.equals("highway") && !value.equals("footway"))
                    wayElement.setHighway(true);
                else if (key.equals("oneway") && value.equals("yes"))
                    wayElement.setOneWay(true);
            } else if (line.equals("</way>")) {
                break;
            }
        }
        return wayElement;
    }
}
