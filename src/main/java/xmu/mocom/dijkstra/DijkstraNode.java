package xmu.mocom.dijkstra;

import xmu.mocom.roadNet.RoadNode;

import java.io.Serializable;

/*
 * the data structure dijkstraNode have some more attributes can be used in Dijkstra search, which roadNode do not have 
 */
public class DijkstraNode implements Serializable {

	private static final long serialVersionUID = 8344333366928446575L;

	private long current_id;
	//RoadNode roadNode;
	private RoadNode parentNode;
	private long distance;				//the distance from start node
	private long distanceFromParent;	//the distance from parent node
	private long arrivalTime;			//the time when arrive at this node
	//private boolean is_setted;			//if the node have been explored
	private boolean onSFlag;				//if the node on S set
	private boolean onTFlag;				//if the node on T set
	private long estimateH;				//A* estimateH

	private RoadNode backwardParentNode;
	private long backwardDistance;				//the distance from start node
	private long backwardDistanceFromParent;	//the distance from parent node
	private long backwardArrivalTime;			//the time when arrive at this node
	private long backwardEstimateH;				//A* estimateH
	
	public DijkstraNode(long current_id) {
		this.current_id = current_id;
		//this.roadNode = roadNode;
		parentNode = null;
		distance = 999999999;
		distanceFromParent = 999999999;
		//is_setted = false;
		onSFlag = false;
		onTFlag = false;
		this.estimateH = 0;

		backwardParentNode = null;
		backwardDistance = 999999999;
		backwardDistanceFromParent = 999999999;
		backwardEstimateH = 0;
	}
	

	public long getCurrentId() {
		return this.current_id;
	}
	
	public void setParentNode(RoadNode parentNode) {
		this.parentNode = parentNode;
	}
	
	public RoadNode getParentNode() {
		return this.parentNode;
	}
	
	public void setDistance(long cost_time) {
		this.distance = cost_time;
	}
	
	public long getDistance() {
		return this.distance;
	}
	
	public void setDistanceFromParent(long cost_time) {
		this.distanceFromParent = cost_time;
	}
	
	public long getDistanceFromParent() {
		return this.distanceFromParent;
	}
	
	public void setArrivalTime(long time) {
		this.arrivalTime = time;
	}
	
	public long getArrivalTime() {
		return this.arrivalTime;
	}
	
	public void setOnS() {
		this.onSFlag = true;
	}
	
	public void setOutOfS() {
		this.onSFlag = false;
	}
	
	public boolean isOnS() {
		return this.onSFlag;
	}
	
	public void setOnT() {
		this.onTFlag = true;
	}
	
	public void setOutOfT() {
		this.onTFlag = false;
	}
	
	public boolean isOnT() {
		return this.onTFlag;
	}
	
	public void setEstimateH(long estimateH) {
		this.estimateH = estimateH;
	}
	
	public long getEstimateH() {
		return this.estimateH;
	}
	
	public long getEstimateF() {
		return this.distance + this.estimateH;
	}


	public void setBackwardParentNode(RoadNode backwardParentNode) {
		this.backwardParentNode = backwardParentNode;
	}

	public RoadNode getBackwardParentNode() {
		return this.backwardParentNode;
	}

	public void setBackwardDistance(long cost_time) {
		this.backwardDistance = cost_time;
	}

	public long getBackwardDistance() {
		return this.backwardDistance;
	}

	public void setBackwardDistanceFromParent(long cost_time) {
		this.backwardDistanceFromParent = cost_time;
	}

	public long getBackwardDistanceFromParent() {
		return this.backwardDistanceFromParent;
	}

	public void setBackwardArrivalTime(long time) {
		this.backwardArrivalTime = time;
	}

	public long getBackwardArrivalTime() {
		return this.backwardArrivalTime;
	}

	public void setBackwardEstimateH(long backwardEstimateH) {
		this.backwardEstimateH = backwardEstimateH;
	}

	public long getBackwardEstimateH() {
		return this.backwardEstimateH;
	}

	public long getBackwardEstimateF() {
		return this.backwardDistance + this.backwardEstimateH;
	}
}