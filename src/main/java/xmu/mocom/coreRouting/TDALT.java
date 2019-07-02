package xmu.mocom.coreRouting;

import java.util.Iterator;
import java.util.Set;

import org.jgrapht.Graph;

import xmu.mocom.roadNet.Path;
import xmu.mocom.roadNet.PathSegment;
import xmu.mocom.roadNet.RoadSegmentEdge;
import xmu.mocom.dijkstra.BidirectionalDijkstraData;
import xmu.mocom.dijkstra.DijkstraNode;
import xmu.mocom.roadNet.RoadNode;
import xmu.mocom.simulator.SimClock;

/*
 * 
 * Time-Dependent Bidirectional A∗ with the potential function
 * 
 */
public class TDALT {

	private static double EARTH_RADIUS = 6378.137;
	
	public static Path path(Graph<RoadNode, RoadSegmentEdge> g, BidirectionalDijkstraData dijkstraData, RoadNode start, RoadNode target, SimClock clock) {
		initializationS(dijkstraData, target, clock);
		initializationT(dijkstraData, start, clock);
		while(!dijkstraData.isFinish()) {
			forwardSearch(g, dijkstraData, target, clock);
			if(!dijkstraData.isOutOfU()) {
				backwardSearch(g, dijkstraData, start, clock);
			}
		}
		return outShortestPaht(start, target);
	}
	
	/*
	 * AStar forward Search
	 * 
	 * @param g
	 * @param dijkstraData
	 * @param target
	 * @param clock
	 */
	public static void forwardSearch
	(Graph<RoadNode, RoadSegmentEdge> g, BidirectionalDijkstraData dijkstraData, RoadNode target, SimClock clock) {
		if(!dijkstraData.isForwardPriorityQueueEmpty()) {
			RoadNode node = dijkstraData.pollForwardPriorityQueue();
			dijkstraData.putIntoS(node);
			//if is target
			if(node == target) {
				dijkstraData.setFinish();
				return;
			}else {
				if(!dijkstraData.isCoreIntersection()) {
					if(node.getDijkstraNode().isOnS() && node.getDijkstraNode().isOnT()) {
						dijkstraData.setCoreIntersection();
						dijkstraData.setLimitU(node.getDijkstraNode().getEstimateF());
					}
				}
				updateAStarForwardPriorityQueue(g, dijkstraData, node, target, clock);
			}
		}else {
			//the queue is empty,didn't find the target, just return
			return;
		}
	}
	
	/*
	 * AStar backward Search
	 * 
	 * @param g
	 * @param dijkstraData
	 * @param start
	 * @param clock
	 */
	public static void backwardSearch
    (Graph<RoadNode, RoadSegmentEdge> g, BidirectionalDijkstraData dijkstraData, RoadNode start, SimClock clock) {
		if(!dijkstraData.isBackwardPriorityQueueEmpty()) {
			RoadNode node = dijkstraData.pollBackwardPriorityQueue();
			dijkstraData.putIntoT(node);
			if(dijkstraData.isCoreIntersection()) {
				if(node.getDijkstraNode().getEstimateF() > dijkstraData.getLimitU()) {
					dijkstraData.setOutOfU();
				}
			}else {
				if(node.getDijkstraNode().isOnS() && node.getDijkstraNode().isOnT()) {
					dijkstraData.setCoreIntersection();
					dijkstraData.setLimitU(node.getDijkstraNode().getEstimateF());
				}
			}
			updateAStarBackwardPriorityQueue(g, dijkstraData, node, start, clock);
		}else {
			//the queue is empty,didn't find the target, just return
			return;
		}
	}
	
	/*
	 * initializing the priority queue with all leaves of Core Nodes that are on S 
	 * 
	 * @param dijkstraData
	 * @param target
	 * @param clock
	 */
	public static void initializationS(BidirectionalDijkstraData dijkstraData, RoadNode target, SimClock clock) {
		Set<RoadNode> coreNodeOnS = dijkstraData.getCoreNodeOnS();
		Iterator<RoadNode> it = coreNodeOnS.iterator();
		while(it.hasNext()) {
			RoadNode rNode = it.next();
			CoreNode coreNode = rNode.getCoreNode();
			Set<CoreEdge> edgeSet = coreNode.getEdgeSet();
			Iterator<CoreEdge> edgeIt = edgeSet.iterator();
			while(edgeIt.hasNext()) {
				CoreEdge coreEdge = edgeIt.next();
				long edge_distance = coreEdge.getTimeDependentPathList().get(clock.getMinuteId(rNode.getDijkstraNode().getArrivalTime())).getDistance();
				if(coreEdge.isStartNode(coreNode)){
					//add the target node into priority queue
					CoreNode targetNode = coreEdge.getTargetNode();
					RoadNode targetRoadNode = targetNode.getRoadNode();
					if (targetRoadNode.getDijkstraNode() !=null) {
						if (targetRoadNode.getDijkstraNode().getCurrentId() == dijkstraData.getCurrentId()) {
							if (targetRoadNode.getDijkstraNode().isOnS()) {
								continue;
							}
						}
					}
					if(targetRoadNode.getDijkstraNode() == null) {
						targetRoadNode.setDijkstraNode(new DijkstraNode(dijkstraData.getCurrentId()));
					}
					if(targetRoadNode.getDijkstraNode().getCurrentId() != dijkstraData.getCurrentId()) {
						targetRoadNode.setDijkstraNode(new DijkstraNode(dijkstraData.getCurrentId()));
					}
					long temp_distance = rNode.getDijkstraNode().getDistance() + edge_distance;
					if( temp_distance < targetRoadNode.getDijkstraNode().getDistance() ) {
						targetRoadNode.getDijkstraNode().setParentNode(rNode);
						targetRoadNode.getDijkstraNode().setArrivalTime(rNode.getDijkstraNode().getArrivalTime()+edge_distance);
						targetRoadNode.getDijkstraNode().setDistance(temp_distance);
						targetRoadNode.getDijkstraNode().setDistanceFromParent(edge_distance);
						targetRoadNode.getDijkstraNode().setEstimateH(estimateH(targetRoadNode,target));
						dijkstraData.resortForwardPriorityQueue(targetRoadNode);
					}
				}
			}
		}
	}
	
	/*
	 * initializing the priority queue with all leaves of Core Nodes that are on T 
	 * 
	 * @param dijkstraData
	 * @param start
	 * @param clock
	 */
	public static void initializationT(BidirectionalDijkstraData dijkstraData, RoadNode start, SimClock clock) {
		Set<RoadNode> coreNodeOnT = dijkstraData.getCoreNodeOnT();
		Iterator<RoadNode> it = coreNodeOnT.iterator();
		while(it.hasNext()) {
			RoadNode rNode = it.next();
			CoreNode coreNode = rNode.getCoreNode();
			Set<CoreEdge> edgeSet = coreNode.getEdgeSet();
			Iterator<CoreEdge> edgeIt = edgeSet.iterator();
			while(edgeIt.hasNext()) {
				CoreEdge coreEdge = edgeIt.next();
				long edge_distance = coreEdge.getMinDistance();
				if(coreEdge.isTargetNode(coreNode)){
					//add the target node into priority queue
					CoreNode startNode = coreEdge.getStartNode();
					RoadNode startRoadNode = startNode.getRoadNode();
					if (startRoadNode.getDijkstraNode() !=null) {
						if (startRoadNode.getDijkstraNode().getCurrentId() == dijkstraData.getCurrentId()) {
							if (startRoadNode.getDijkstraNode().isOnT()) {
								continue;
							}
						}
					}
					if(startRoadNode.getDijkstraNode() == null) {
						startRoadNode.setDijkstraNode(new DijkstraNode(dijkstraData.getCurrentId()));
					}
					if(startRoadNode.getDijkstraNode().getCurrentId() != dijkstraData.getCurrentId()) {
						startRoadNode.setDijkstraNode(new DijkstraNode(dijkstraData.getCurrentId()));
					}
					long temp_distance = rNode.getDijkstraNode().getBackwardDistance() + edge_distance;
					if( temp_distance < startRoadNode.getDijkstraNode().getBackwardDistance() ) {
						startRoadNode.getDijkstraNode().setBackwardParentNode(rNode);
						startRoadNode.getDijkstraNode().setBackwardArrivalTime(rNode.getDijkstraNode().getBackwardArrivalTime()+edge_distance);
						startRoadNode.getDijkstraNode().setBackwardDistance(temp_distance);
						startRoadNode.getDijkstraNode().setBackwardDistanceFromParent(edge_distance);
						startRoadNode.getDijkstraNode().setBackwardEstimateH(estimateH(startRoadNode,start));
						dijkstraData.resortBackwardPriorityQueue(startRoadNode);
					}
				}
			}
		}
	}
	
	/*
	 *put the node that adjacents to roadNode into forward priority queue
	 * 
	 * @param g
	 * @param dijkstarData
	 * @param roadNode
	 * @param target
	 * @param clock
	 */
	public static void updateAStarForwardPriorityQueue(Graph<RoadNode, RoadSegmentEdge> g, BidirectionalDijkstraData dijkstraData, RoadNode roadNode, RoadNode target, SimClock clock) {
		//if the node is core and the node is not on T, then "hops on" the core routing, else do not "hops on" the core routing
		//List<RoadNode> candidateList = new ArrayList<RoadNode>();
		if(roadNode.isCore() && !roadNode.getDijkstraNode().isOnT()) {
			CoreNode coreNode = roadNode.getCoreNode();
			Set<CoreEdge> edgeSet = coreNode.getEdgeSet();
			Iterator<CoreEdge> it = edgeSet.iterator();
			while(it.hasNext()) {
				CoreEdge coreEdge = it.next();
				if(coreEdge.isStartNode(coreNode)) {
					CoreNode anotherCoreNode = coreEdge.getTargetNode();
					RoadNode anotherRoadNode = anotherCoreNode.getRoadNode();
					if(anotherRoadNode.getDijkstraNode() == null) {
						anotherRoadNode.setDijkstraNode(new DijkstraNode(dijkstraData.getCurrentId()));
					}
					if(anotherRoadNode.getDijkstraNode().getCurrentId() != dijkstraData.getCurrentId()) {
						anotherRoadNode.setDijkstraNode(new DijkstraNode(dijkstraData.getCurrentId()));
					}
					if(!anotherRoadNode.getDijkstraNode().isOnS()) {
						long edge_distance = coreEdge.getTimeDependentPathList().get(clock.getMinuteId(roadNode.getDijkstraNode().getArrivalTime())).getDistance();
						long temp_distance = roadNode.getDijkstraNode().getDistance() + edge_distance;
						if( temp_distance < anotherRoadNode.getDijkstraNode().getDistance() ) {
							anotherRoadNode.getDijkstraNode().setParentNode(roadNode);
							anotherRoadNode.getDijkstraNode().setArrivalTime(roadNode.getDijkstraNode().getArrivalTime()+edge_distance);
							anotherRoadNode.getDijkstraNode().setDistance(temp_distance);
							anotherRoadNode.getDijkstraNode().setDistanceFromParent(edge_distance);
							anotherRoadNode.getDijkstraNode().setEstimateH(estimateH(anotherRoadNode,target));
							dijkstraData.resortForwardPriorityQueue(anotherRoadNode);
						}
					}
				}
			}
		}else {
			Set<RoadSegmentEdge> edgeSet = g.outgoingEdgesOf(roadNode);
			Iterator<RoadSegmentEdge> it = edgeSet.iterator();
			while(it.hasNext()) {
				RoadSegmentEdge edge = it.next();
				RoadNode anotherRoadNode = g.getEdgeTarget(edge);
				if(anotherRoadNode.getDijkstraNode() == null) {
					anotherRoadNode.setDijkstraNode(new DijkstraNode(dijkstraData.getCurrentId()));
				}
				if(anotherRoadNode.getDijkstraNode().getCurrentId() != dijkstraData.getCurrentId()) {
					anotherRoadNode.setDijkstraNode(new DijkstraNode(dijkstraData.getCurrentId()));
				}
				if(!anotherRoadNode.getDijkstraNode().isOnS()) {
					long edge_distance = edge.getDistanceList().get(clock.getMinuteId(roadNode.getDijkstraNode().getArrivalTime()));
					long temp_distance = roadNode.getDijkstraNode().getDistance() + edge_distance;
					if( temp_distance < anotherRoadNode.getDijkstraNode().getDistance() ) {
						anotherRoadNode.getDijkstraNode().setParentNode(roadNode);
						anotherRoadNode.getDijkstraNode().setArrivalTime(roadNode.getDijkstraNode().getArrivalTime()+edge_distance);
						anotherRoadNode.getDijkstraNode().setDistance(temp_distance);
						anotherRoadNode.getDijkstraNode().setDistanceFromParent(edge_distance);
						anotherRoadNode.getDijkstraNode().setEstimateH(estimateH(anotherRoadNode,target));
						dijkstraData.resortForwardPriorityQueue(anotherRoadNode);
					}
				}
			}
		}
	}
	
	/*
	 *put the node that adjacents to roadNode into backward priority queue
	 * 
	 * @param g
	 * @param dijkstarData
	 * @param roadNode
	 * @param target
	 * @param clock
	 */
	public static void updateAStarBackwardPriorityQueue(Graph<RoadNode, RoadSegmentEdge> g, BidirectionalDijkstraData dijkstraData, RoadNode roadNode, RoadNode start, SimClock clock) {
		//if the node is core and the node is not on T, then "hops on" the core routing, else do not "hops on" the core routing
		if(roadNode.isCore()) {
			CoreNode coreNode = roadNode.getCoreNode();
			Set<CoreEdge> edgeSet = coreNode.getEdgeSet();
			Iterator<CoreEdge> it = edgeSet.iterator();
			while(it.hasNext()) {
				CoreEdge coreEdge = it.next();
				if(coreEdge.isTargetNode(coreNode)) {
					CoreNode anotherCoreNode = coreEdge.getStartNode();
					RoadNode anotherRoadNode = anotherCoreNode.getRoadNode();
					if(anotherRoadNode.getDijkstraNode() == null) {
						anotherRoadNode.setDijkstraNode(new DijkstraNode(dijkstraData.getCurrentId()));
					}
					if(anotherRoadNode.getDijkstraNode().getCurrentId() != dijkstraData.getCurrentId()) {
						anotherRoadNode.setDijkstraNode(new DijkstraNode(dijkstraData.getCurrentId()));
					}
					if(!anotherRoadNode.getDijkstraNode().isOnT() && !anotherRoadNode.getDijkstraNode().isOnS()) {
						long edge_distance = coreEdge.getMinDistance();
						long temp_distance = roadNode.getDijkstraNode().getDistance() + edge_distance;
						if( temp_distance < anotherRoadNode.getDijkstraNode().getBackwardDistance() ) {
							anotherRoadNode.getDijkstraNode().setBackwardParentNode(roadNode);
							anotherRoadNode.getDijkstraNode().setBackwardArrivalTime(roadNode.getDijkstraNode().getBackwardArrivalTime()+edge_distance);
							anotherRoadNode.getDijkstraNode().setBackwardDistance(temp_distance);
							anotherRoadNode.getDijkstraNode().setBackwardDistanceFromParent(edge_distance);
							anotherRoadNode.getDijkstraNode().setBackwardEstimateH(estimateH(anotherRoadNode,start));
							dijkstraData.resortBackwardPriorityQueue(anotherRoadNode);
						}
					}
				}
			}
		}
	}
	
	public static Path outShortestPaht(RoadNode start, RoadNode target) {
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
	
	public static long estimateH(RoadNode roadNode, RoadNode target) {
		return (long)getDistance(roadNode,target)/13;
	}
	
	private static double rad(double d) {
		return d * Math.PI / 180.0;
	}
	
	/*
	 * 通过经纬度获取距离(单位：米)
	 * 
	 * @param lat1
	 * @param lng1
	 * @param lat2
	 * @param lng2
	 * @return 距离
	 */
	public static double getDistance(double lat1, double lng1, double lat2,
			double lng2) {
		double radLat1 = rad(lat1);
		double radLat2 = rad(lat2);
		double a = radLat1 - radLat2;
		double b = rad(lng1) - rad(lng2);
		double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
				+ Math.cos(radLat1) * Math.cos(radLat2)
				* Math.pow(Math.sin(b / 2), 2)));
		s = s * EARTH_RADIUS;
		s = Math.round(s * 10000d) / 10000d;
		s = s * 1000;
		return s;
	}

    public static long getDistance(RoadNode node1, RoadNode node2) {
    	double lat1 = node1.getLat();
    	double lon1 = node1.getLon();
    	double lat2 = node2.getLat();
    	double lon2 = node2.getLon();
    	return (long) getDistance(lat1,lon1,lat2,lon2);
    }
}
