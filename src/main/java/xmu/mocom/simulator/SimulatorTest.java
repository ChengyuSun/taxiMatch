package xmu.mocom.simulator;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.dom4j.DocumentException;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;

import xmu.mocom.roadNet.RoadNetCreator;
import xmu.mocom.roadNet.RoadSegmentEdge;
import xmu.mocom.roadNet.RoadNode;

import org.jgrapht.*;

/**
 * taxi match simulator
 *
 */
public class SimulatorTest
{

	private static String OSM_PATH = "./map_highway.osm";
	
    public static void main(String[] args) throws IOException,DocumentException {
    	
    	//create the road net
    	Graph<RoadNode, RoadSegmentEdge> g = RoadNetCreator.CreateRoadNetByOsm(OSM_PATH);

    	RoadNode e1 = g.vertexSet().stream().filter(elemen -> elemen.getOsmId().equals("1881181356")).findAny().get();
    	RoadNode e2 = g.vertexSet().stream().filter(elemen -> elemen.getOsmId().equals("2592412682")).findAny().get();
    	Set<RoadSegmentEdge> edgeSet = g.edgesOf(e1);
		RoadSegmentEdge edge = null;
		Iterator itEdge = edgeSet.iterator();
		while(itEdge.hasNext()) {
			edge =  (RoadSegmentEdge) itEdge.next();
			break;
		}
		List<Long> list = edge.getDistanceList();
		Iterator it = list.iterator();
		while(it.hasNext()) {
			long speed = (long) it.next();
			System.out.println(speed);
		}
		testShortestPath(g,e1,e2);
    	System.out.println("输出完成！");
		
    }
        
    public static void testShortestPath(Graph<RoadNode, RoadSegmentEdge> g,RoadNode e1,RoadNode e2){
    	DijkstraShortestPath<RoadNode, DefaultWeightedEdge> dijkstraAlg = new DijkstraShortestPath(g);
    	GraphPath<RoadNode,DefaultWeightedEdge>  thepath = dijkstraAlg.getPath(e1, e2);
    	//System.out.println("path:="+thepath);
    	Iterator it = thepath.getVertexList().iterator();
    	while(it.hasNext()) {
    		RoadNode rnode = (RoadNode) it.next();
    		System.out.println("**"+rnode.getOsmId()+"**");
    	}
    }

}
