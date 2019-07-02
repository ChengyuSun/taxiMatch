package xmu.mocom.test;

import org.dom4j.DocumentException;
import org.jgrapht.Graph;
import xmu.mocom.approximateShortest.ApproximateShortest;
import xmu.mocom.dijkstra.Dijkstra;
import xmu.mocom.experiment.LoadMap;
import xmu.mocom.roadNet.Path;
import xmu.mocom.roadNet.PathSegment;
import xmu.mocom.roadNet.RoadNode;
import xmu.mocom.roadNet.RoadSegmentEdge;
import xmu.mocom.simulator.SimClock;

import java.io.IOException;
import java.util.Set;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;


public class TestApproximateShortest {

    private static String GRAPH_FILE = "experimentData/core_choose_nums=4000_core_nums=50_graph(2).ser";
    public static void main(String[] args) throws IOException, DocumentException, java.lang.Exception {

        SimClock simClock = new SimClock(1553951724,1);
        //create the road net
        //Graph<RoadNode, RoadSegmentEdge> g = RoadNetCreator.CreateRoadNetByOsm(OSM_PATH);
        Graph<RoadNode, RoadSegmentEdge> g = LoadMap.getMap(GRAPH_FILE);
        System.out.println("graph ok");

        List<RoadNode> coreNodeList = new ArrayList<RoadNode>();

        Set<RoadNode> roadNodeSet = g.vertexSet();
        Iterator<RoadNode> it = roadNodeSet.iterator();
        while(it.hasNext()){
            RoadNode roadNode = it.next();
            if(roadNode.isCore()) {
                coreNodeList.add(roadNode);
                System.out.println(roadNode.getOsmId());
            }
        }


        RoadNode e1 = g.vertexSet().stream().filter(elemen -> elemen.getOsmId().equals("2852465281")).findAny().get();
        RoadNode e2 = g.vertexSet().stream().filter(elemen -> elemen.getOsmId().equals("1425910444")).findAny().get();


        testApproximatePath(g,coreNodeList, e1, e2, simClock);






    }


    public static void testApproximatePath(Graph<RoadNode, RoadSegmentEdge> g,List<RoadNode> coreNodeList,RoadNode e1,RoadNode e2,SimClock clock){
        Path thepath = ApproximateShortest.approximateShortestPath(g, coreNodeList, e1, e2, clock);
        //System.out.println("path:="+thepath);
        System.out.println("length = "+thepath.getDistance());
        while(!thepath.isEmpty()) {
            PathSegment pathSegment = thepath.pollPathSegment();
            RoadNode rnode = pathSegment.getEndNode();
            System.out.println("--"+rnode.getOsmId()+"--");
        }
    }

}
