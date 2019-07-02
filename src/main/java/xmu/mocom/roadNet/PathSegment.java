package xmu.mocom.roadNet;

import java.io.Serializable;

/*
 * the segment of Path
 */
public class PathSegment implements Serializable {
    private RoadNode startNode;
	private RoadNode endNode;
	//private double speed;
	private long distance;


	
	public PathSegment(RoadNode startNode, RoadNode endNode, long distance) {
		this.startNode = startNode;
		this.endNode = endNode;
		this.distance = distance;
	}
	
	public void setStartNode(RoadNode startNode) {
		this.startNode = startNode;
	}
	
	public RoadNode getStartNode() {
		return this.startNode;
	}
	
	public void setEndNode(RoadNode endNode) {
		this.endNode = endNode;
	}
	
	public RoadNode getEndNode() {
		return this.endNode;
	}
	
//	public void setSpeed(double speed) {
//		this.speed = speed;
//	}
//	
//	public double getSpeed() {
//		return this.speed;
//	}
	
	public void setDistance(long distance) {
		this.distance = distance;
	}
	
	public long getDistance() {
		return this.distance;
	}
}