package xmu.mocom.dijkstra;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;

import org.jgrapht.Graph;

import xmu.mocom.roadNet.Path;
import xmu.mocom.roadNet.PathSegment;
import xmu.mocom.roadNet.RoadNode;
import xmu.mocom.roadNet.RoadSegmentEdge;
import xmu.mocom.simulator.SimClock;

/*
 * Dijkstra Algorithm
 */
public class Dijkstra implements Serializable {
	
	public static long CURRENT_ID = 0;	//the flag that uniquely sign the Dijkstra search
	
	/*
	 * Bidirectional Dijkstra Algorithm,return the BidirectionalDijkstraData when the set S and T have intersection or priority queues are empty
	 * 
	 * @param g the road net
	 * @param start the start node on the road net
	 * @param target the target node on the road net
	 * @param clock
	 * @return BidirectionalDijkstraData
	 */
	public static BidirectionalDijkstraData bidirectionalDijkstra(Graph<RoadNode, RoadSegmentEdge> g, RoadNode start, RoadNode target, SimClock clock) {
		//init
		long current_id = CURRENT_ID ++;
		BidirectionalDijkstraData dijkstraData = new BidirectionalDijkstraData(current_id);
		init(g, dijkstraData, start, target, clock);
		while( (!dijkstraData.isForwardPriorityQueueEmpty()) || (!dijkstraData.isBackwardPriorityQueueEmpty()) ) {
			if(!dijkstraData.isForwardPriorityQueueEmpty()) {
				RoadNode rnode = dijkstraData.pollForwardPriorityQueue();
				dijkstraData.putIntoS(rnode);
				if (rnode == target) {
					dijkstraData.setFinish();
					return dijkstraData;
				}
				if(rnode.getDijkstraNode().isOnS() && rnode.getDijkstraNode().isOnT()) {
					dijkstraData.setIntersection();
					//updateForwardPriorityQueue(g, dijkstraData, rnode, clock);
					return dijkstraData;
				}
				if (!rnode.isCore()) {
					updateForwardPriorityQueue(g, dijkstraData, rnode, clock);
				}
			}
			if(!dijkstraData.isBackwardPriorityQueueEmpty()) {
				RoadNode rnode = dijkstraData.pollBackwardPriorityQueue();
				dijkstraData.putIntoT(rnode);
				if(rnode.getDijkstraNode().isOnS() && rnode.getDijkstraNode().isOnT()) {
					dijkstraData.setIntersection();
					return dijkstraData;
				}
				if (!rnode.isCore()) {
					updateBackwardPriorityQueue(g, dijkstraData, rnode, clock);
				}
			}
		}
		return dijkstraData;
	}
	
	/*
	 * relaxing the outgoing arcs of start and the incoming arcs of target
	 * 
	 * @param g the road net
	 * @param dijkstraData the data structure that store the process record of Bidirectional Dijkstra
	 * @param start
	 * @param target
	 * @param clock
	 */
	public static void init(Graph<RoadNode, RoadSegmentEdge> g, BidirectionalDijkstraData dijkstraData, RoadNode start, RoadNode target, SimClock clock) {
		start.setDijkstraNode(new DijkstraNode(dijkstraData.getCurrentId()));
		start.getDijkstraNode().setDistance(0);
		start.getDijkstraNode().setParentNode(null);
		start.getDijkstraNode().setDistanceFromParent(0);
		start.getDijkstraNode().setArrivalTime(clock.getNow());
		dijkstraData.putIntoS(start);
		updateForwardPriorityQueue(g, dijkstraData, start, clock);
		
		target.setDijkstraNode(new DijkstraNode(dijkstraData.getCurrentId()));
		target.getDijkstraNode().setBackwardDistance(0);
		target.getDijkstraNode().setBackwardParentNode(null);
		target.getDijkstraNode().setBackwardDistanceFromParent(0);
		target.getDijkstraNode().setBackwardArrivalTime(clock.getNow());
		dijkstraData.putIntoT(target);
		updateBackwardPriorityQueue(g, dijkstraData, target, clock);
	}

	/*
	 * get the dijkstra shortest path from start to target, running upon the result of bidirectionalDijkstra
	 */
	public static Path unidirectionalDijkstra(Graph<RoadNode, RoadSegmentEdge> g, BidirectionalDijkstraData dijkstraData, RoadNode start, RoadNode target, SimClock clock) {
		Path path =null;

		Set<RoadNode> coreNodeOnS = dijkstraData.getCoreNodeOnS();
		Iterator<RoadNode> it = coreNodeOnS.iterator();
		while(it.hasNext()) {
			RoadNode rNode = it.next();
			updateForwardPriorityQueue(g, dijkstraData, rNode, clock);
		}

		while(!dijkstraData.isForwardPriorityQueueEmpty()) {
			RoadNode rnode = dijkstraData.pollForwardPriorityQueue();
			dijkstraData.putIntoS(rnode);
			if (rnode == target) {
				path = outShortestPath(start, target);
				return path;
			}
			//updateUnidirectionPriorityQueue(g, dijkstraData, rnode, clock);
			updateForwardPriorityQueue(g, dijkstraData, rnode, clock);
		}
		return path;
	}

	/*
	 *put the node that adjacents to roadNode into forward priority queue
	 * 
	 * @param g
	 * @param roadNode 
	 * @param clock
	 */
	public static void updateForwardPriorityQueue(Graph<RoadNode, RoadSegmentEdge> g, BidirectionalDijkstraData dijkstraData, RoadNode roadNode, SimClock clock) {
		//get the all connected edge of roadNode
		Set<RoadSegmentEdge> edgeSet = g.outgoingEdgesOf(roadNode);
		Iterator<RoadSegmentEdge> it = edgeSet.iterator();
		while(it.hasNext()) {
			RoadSegmentEdge nextEdge = it.next();
			long edge_distance = 0;
			RoadNode nextNode = null;
			nextNode = g.getEdgeTarget(nextEdge);
			edge_distance = nextEdge.getDistanceList().get(clock.getMinuteId(roadNode.getDijkstraNode().getArrivalTime()));
			if (nextNode.getDijkstraNode() !=null) {
				if (nextNode.getDijkstraNode().getCurrentId() == dijkstraData.getCurrentId()) {
					if (nextNode.getDijkstraNode().isOnS()) {
						continue;
					}
				}
			}
			if(nextNode.getDijkstraNode() == null) {
				nextNode.setDijkstraNode(new DijkstraNode(dijkstraData.getCurrentId()));
			}
			if(nextNode.getDijkstraNode().getCurrentId() != dijkstraData.getCurrentId()) {
				nextNode.setDijkstraNode(new DijkstraNode(dijkstraData.getCurrentId()));
			}
			long temp_distance = roadNode.getDijkstraNode().getDistance() + edge_distance;
			if( temp_distance < nextNode.getDijkstraNode().getDistance() ) {
				nextNode.getDijkstraNode().setParentNode(roadNode);
				nextNode.getDijkstraNode().setArrivalTime(roadNode.getDijkstraNode().getArrivalTime()+edge_distance);
				nextNode.getDijkstraNode().setDistance(temp_distance);
				nextNode.getDijkstraNode().setDistanceFromParent(edge_distance);
				dijkstraData.resortForwardPriorityQueue(nextNode);
			}
		}//while(it.hasNext()) 
	}
	
	/*
	 *put the node that adjacents to roadNode into backward priority queue
	 * 
	 * @param g
	 * @param roadNode 
	 * @param clock
	 */
	public static void updateBackwardPriorityQueue(Graph<RoadNode, RoadSegmentEdge> g, BidirectionalDijkstraData dijkstraData, RoadNode roadNode, SimClock clock) {
		//get the all connected edge of roadNode
		Set<RoadSegmentEdge> edgeSet = g.incomingEdgesOf(roadNode);
		Iterator<RoadSegmentEdge> it = edgeSet.iterator();
		while(it.hasNext()) {
			RoadSegmentEdge nextEdge = it.next();
			long edge_distance = 0;
			RoadNode nextNode = null;
			nextNode = g.getEdgeSource(nextEdge);
			edge_distance = nextEdge.getMinDistance();
			if (nextNode.getDijkstraNode() !=null) {
				if (nextNode.getDijkstraNode().getCurrentId() == dijkstraData.getCurrentId()) {
					if (nextNode.getDijkstraNode().isOnT()) {
						continue;
					}
				}
			}
			if(nextNode.getDijkstraNode() == null) {
				nextNode.setDijkstraNode(new DijkstraNode(dijkstraData.getCurrentId()));
			}
			if(nextNode.getDijkstraNode().getCurrentId() != dijkstraData.getCurrentId()) {
				nextNode.setDijkstraNode(new DijkstraNode(dijkstraData.getCurrentId()));
			}
			long temp_distance = roadNode.getDijkstraNode().getBackwardDistance() + edge_distance;
			if( temp_distance < nextNode.getDijkstraNode().getBackwardDistance() ) {
				nextNode.getDijkstraNode().setBackwardParentNode(roadNode);
				nextNode.getDijkstraNode().setBackwardArrivalTime(roadNode.getDijkstraNode().getBackwardArrivalTime()+edge_distance);
				nextNode.getDijkstraNode().setBackwardDistance(temp_distance);
				nextNode.getDijkstraNode().setBackwardDistanceFromParent(edge_distance);
				dijkstraData.resortBackwardPriorityQueue(nextNode);
			}
		}//while(it.hasNext()) 
	}

	/*
	 * get the dijkstra shortest path from start to target
	 */
	public static Path singlePath(Graph<RoadNode, RoadSegmentEdge> g, RoadNode start, RoadNode target, SimClock clock) {
		long current_id = CURRENT_ID ++;
		BidirectionalDijkstraData dijkstraData = new BidirectionalDijkstraData(current_id);
		return dijkstraSinglePath(g, dijkstraData, start, target, clock);
	}

	/*
	 * get the dijkstra shortest path from start to target
	 */
	public static Path dijkstraSinglePath
	(Graph<RoadNode, RoadSegmentEdge> g, BidirectionalDijkstraData dijkstraData, RoadNode start, RoadNode target, SimClock clock) {
		start.setDijkstraNode(new DijkstraNode(dijkstraData.getCurrentId()));
		start.getDijkstraNode().setDistance(0);
		start.getDijkstraNode().setParentNode(null);
		start.getDijkstraNode().setDistanceFromParent(0);
		start.getDijkstraNode().setArrivalTime(clock.getNow());
		dijkstraData.putIntoS(start);
		dijkstraData.addForwardPriorityQueue(start);
		Path path =null;
		while(!dijkstraData.isForwardPriorityQueueEmpty()) {
			RoadNode rnode = dijkstraData.pollForwardPriorityQueue();
			//rnode.getDijkstraNode().setOnS();
			dijkstraData.putIntoS(rnode);
			if (rnode == target) {
				path = outShortestPath(start, target);
				return path;
			}
			//updateUnidirectionPriorityQueue(g, dijkstraData, rnode, clock);
			updateForwardPriorityQueue(g, dijkstraData, rnode, clock);
		}
		return path;
	}

	public static Path outShortestPath(RoadNode start, RoadNode target) {
		RoadNode rnode = target;
		long count = 0;
		Path path = new Path();
		while (rnode != start) {
			RoadNode parentNode = rnode.getDijkstraNode().getParentNode();
			PathSegment pathSegment = new PathSegment(parentNode, rnode, rnode.getDijkstraNode().getDistanceFromParent());
			path.addPathSegmentFirst(pathSegment);
			count ++;
			if (count > 9999999) {
				path = null;
				return path;
			}
			rnode = parentNode;
		}
		return path;
	}

}//class


