package xmu.mocom.test;

import org.dom4j.DocumentException;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import xmu.mocom.roadNet.Path;
import xmu.mocom.roadNet.PathSegment;
import xmu.mocom.roadNet.RoadSegmentEdge;
import xmu.mocom.coreRouting.CoreRouting;
import xmu.mocom.dijkstra.Dijkstra;
import xmu.mocom.roadNet.RoadNode;
import xmu.mocom.simulator.SimClock;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import xmu.mocom.experiment.LoadMap;

public class testCoreRouting {
    //private static String OSM_PATH = "experimentData/map_highway.osm";
    private static String GRAPH_FILE = "experimentData/core_choose_nums=4000_core_nums=50_graph(2).ser";
    //private static String REQUEST_FILE = "experimentData/1h_request_3s_per_req_osm_small10.txt";
    private static String REQUEST_FILE = "experimentData/1h_request_3s_per_req_osm(1).txt";
    //private static String REQUEST_FILE = "experimentData/1h_request_3s_per_req_osm_small1198.txt";

    public static void main(String[] args) throws IOException, DocumentException, java.lang.Exception {

        SimClock simClock = new SimClock(1553951724,1);
        //create the road net
        //Graph<RoadNode, RoadSegmentEdge> g = RoadNetCreator.CreateRoadNetByOsm(OSM_PATH);
        Graph<RoadNode, RoadSegmentEdge> g = LoadMap.getMap(GRAPH_FILE);
        System.out.println("graph ok");
//    	RoadNode e1 = g.vertexSet().stream().filter(elemen -> elemen.getOsmId().equals("1881181356")).findAny().get();
//    	RoadNode e2 = g.vertexSet().stream().filter(elemen -> elemen.getOsmId().equals("2592412682")).findAny().get();

      RoadNode e1 = g.vertexSet().stream().filter(elemen -> elemen.getOsmId().equals("1840796394")).findAny().get();
      RoadNode e2 = g.vertexSet().stream().filter(elemen -> elemen.getOsmId().equals("1422693455")).findAny().get();

        List<Request> requestList = ReadRequest.readRequest(REQUEST_FILE);
        Iterator<Request> it = requestList.iterator();
        Instant inst1 = Instant.now();
        while(it.hasNext()){
            Request request = it.next();
            RoadNode rn1 = g.vertexSet().stream().filter(elemen -> elemen.getOsmId().equals(String.valueOf(request.getStartId()))).findAny().get();
            RoadNode rn2 = g.vertexSet().stream().filter(elemen -> elemen.getOsmId().equals(String.valueOf(request.getTargetId()))).findAny().get();
		    testCoreRouting(g,rn1,rn2,simClock);
            //testShortestPath(g,rn1,rn2);
        }
        Instant inst2 = Instant.now();
        System.out.println("T="+Duration.between(inst1, inst2).toMillis());


//      Set<RoadSegmentEdge> edgeSet = g.edgesOf(e1);
//		RoadSegmentEdge edge = null;
//		Iterator itEdge = edgeSet.iterator();
//		while(itEdge.hasNext()) {
//			edge =  (RoadSegmentEdge) itEdge.next();
//			break;
//		}
//		List<Long> list = edge.getDistanceList();
//		Iterator it = list.iterator();
//		while(it.hasNext()) {
//			long speed = (long) it.next();
//			System.out.println(speed);
//		}
//        Instant inst1 = Instant.now();
//        testShortestPath(g,e1,e2);
//        Instant inst2 = Instant.now();
//        testSinglePath(g,e1,e2,simClock);
//        Instant inst3 = Instant.now();
//        testCoreRouting(g,e1,e2,simClock);
//        Instant inst4 = Instant.now();
//        System.out.println("T1="+ Duration.between(inst1, inst2).toMillis()+",T2="+Duration.between(inst2, inst3).toMillis()+",T3="+Duration.between(inst3,inst4).toMillis());
//        System.out.println("输出完成！");



//		Instant inst1 = Instant.now();
//		testCoreRouting(g,e1,e2,simClock);
//		Instant inst2 = Instant.now();
//		System.out.println("T="+Duration.between(inst1, inst2).toMillis());

    }

    public static void testShortestPath(Graph<RoadNode, RoadSegmentEdge> g,RoadNode e1,RoadNode e2){
        DijkstraShortestPath<RoadNode, DefaultWeightedEdge> dijkstraAlg = new DijkstraShortestPath(g);
        GraphPath<RoadNode,DefaultWeightedEdge> thepath = dijkstraAlg.getPath(e1, e2);
        //System.out.println("path:="+thepath);
        System.out.println("jgrapht shortestPath");
        Iterator it = thepath.getVertexList().iterator();
        while(it.hasNext()) {
            RoadNode rnode = (RoadNode) it.next();
            System.out.println("**"+rnode.getOsmId()+"**");
        }
    }

    public static void testSinglePath(Graph<RoadNode, RoadSegmentEdge> g,RoadNode e1,RoadNode e2,SimClock clock){
        Path thepath = Dijkstra.singlePath(g, e1, e2, clock);
        //System.out.println("path:="+thepath);
        while(!thepath.isEmpty()) {
            PathSegment pathSegment = thepath.pollPathSegment();
            RoadNode rnode = pathSegment.getEndNode();
            System.out.println("--"+rnode.getOsmId()+"--");
        }
    }

    public static void testCoreRouting(Graph<RoadNode, RoadSegmentEdge> g,RoadNode e1,RoadNode e2,SimClock clock) {
        Path thepath = CoreRouting.shortestPath(g, e1, e2, clock);
        //System.out.println("path:="+thepath);
        while(!thepath.isEmpty()) {
            PathSegment pathSegment = thepath.pollPathSegment();
            RoadNode rnode = pathSegment.getEndNode();
            System.out.println("++"+rnode.getOsmId()+"++");
        }
    }

}
