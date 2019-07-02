package xmu.mocom.coreRouting;

import org.jgrapht.Graph;

import xmu.mocom.roadNet.Path;
import xmu.mocom.roadNet.RoadSegmentEdge;
import xmu.mocom.dijkstra.BidirectionalDijkstraData;
import xmu.mocom.dijkstra.Dijkstra;
import xmu.mocom.roadNet.RoadNode;
import xmu.mocom.simulator.SimClock;

public class CoreRouting {

	public static Path shortestPath(Graph<RoadNode, RoadSegmentEdge> g, RoadNode start, RoadNode target, SimClock clock) {
		BidirectionalDijkstraData dijkstraData = initializationPhase(g, start, target, clock);
		if(dijkstraData.isFinish()){
			return Dijkstra.outShortestPath(start, target);
		}
		return mainPhase(g, dijkstraData, start, target, clock);
	}
	
	/*
	 * initialization phase of Core Routing
	 * 
	 * @param g the road net
	 * @param start the start node on the road net
	 * @param target the target node on the road net
	 * @param clock simulator clock
	 * @return BidirectionalDijkstraData
	 */
	public static BidirectionalDijkstraData initializationPhase
	(Graph<RoadNode, RoadSegmentEdge> g, RoadNode start, RoadNode target, SimClock clock) {
		BidirectionalDijkstraData dijkstraData = Dijkstra.bidirectionalDijkstra(g, start, target, clock);
		return dijkstraData;
	}
	
	/*
	 * main phase of Core Routing, search the single shortest path  
	 * 
	 * @param g the road net
	 * @param dijkstraData the data structure that store the process record of Bidirectional Dijkstra
	 * @param start the start node on the road net
	 * @param target the target node on the road net
	 * @param clock simulator clock
	 * @return Path the shortest path from start to target
	 */
	public static Path mainPhase(Graph<RoadNode, RoadSegmentEdge> g, BidirectionalDijkstraData dijkstraData, RoadNode start, RoadNode target, SimClock clock) {
		if (dijkstraData.isIntersection()) {
			//System.out.println("unidirectional dijkstra");
			return Dijkstra.unidirectionalDijkstra(g, dijkstraData, start, target, clock);
		}else {
			//System.out.println("TDALT");
			return TDALT.path(g, dijkstraData, start, target, clock);
		}
	}
}

