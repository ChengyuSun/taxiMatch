package xmu.mocom.dijkstra;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import xmu.mocom.roadNet.RoadNode;

/*
 * the data structure that bidirectionalDijkstra search need to use,
 * including foward search set S, backward search set T, priority queues...
 */
public class BidirectionalDijkstraData implements Serializable {
	public Set<RoadNode> S;
	public Set<RoadNode> T;
	public LinkedList<RoadNode> forward_priority_queue;
	public LinkedList<RoadNode> backward_priority_queue;
	//public LinkedList<RoadNode> priority_queue;
	public boolean intersectionFlag;
	public boolean coreIntersectionFlag;
	public boolean outOfUFlag;
	public boolean finishFlag;
	public long current_id;
	public long limitU;
	
	public BidirectionalDijkstraData(long current_id){
		this.S = new HashSet<RoadNode>();
		this.T = new HashSet<RoadNode>();
		this.forward_priority_queue = new LinkedList<RoadNode>();
		this.backward_priority_queue = new LinkedList<RoadNode>();
		//this.priority_queue = new LinkedList<RoadNode>();
		this.intersectionFlag = false;
		this.coreIntersectionFlag = false;
		this.outOfUFlag = false;
		this.finishFlag = false;
		this.current_id = current_id;
		this.limitU = 999999999;
	}
	
	public boolean isOnSandT(RoadNode node) {
		return node.getDijkstraNode().isOnS() && node.getDijkstraNode().isOnT();
	}
	
	public void putIntoS(RoadNode node) {
		S.add(node);
		node.getDijkstraNode().setOnS();
	}
	
	public void putIntoT(RoadNode node) {
		T.add(node);
		node.getDijkstraNode().setOnT();
	}
	
	/*
	 * add roadNode into forward priority queue (roadNode is not in the queue)
	 * 
	 * @param roadNode
	 */
	public void addForwardPriorityQueue(RoadNode roadNode) {
		if(!this.forward_priority_queue.isEmpty()) {
			for(int i = 0;i < this.forward_priority_queue.size(); i++) {
				RoadNode rnode = this.forward_priority_queue.get(i);
				//if(roadNode.getDijkstraNode().getDistance() < rnode.getDijkstraNode().getDistance()) {
				if(roadNode.getDijkstraNode().getEstimateF() < rnode.getDijkstraNode().getEstimateF()) {
					this.forward_priority_queue.add(i,roadNode);
					break;
				}else {
					if(i == this.forward_priority_queue.size()-1) {
						this.forward_priority_queue.add(roadNode);
						break;
					}//if(i == this.priority_queue.size()-1)
				}//else
			}//for(int i = 0;i < this.priority_queue.size(); i++) 
		}else {
			this.forward_priority_queue.add(roadNode);
		}//else
	}
	
	/*
	 * add roadNode into backward priority queue (roadNode is not in the queue)
	 * 
	 * @param roadNode
	 */
	public void addBackwardPriorityQueue(RoadNode roadNode) {
		if(!this.backward_priority_queue.isEmpty()) {
			for(int i = 0;i < this.backward_priority_queue.size(); i++) {
				RoadNode rnode = this.backward_priority_queue.get(i);
				//if(roadNode.getDijkstraNode().getDistance() < rnode.getDijkstraNode().getDistance()) {
				if(roadNode.getDijkstraNode().getBackwardEstimateF() < rnode.getDijkstraNode().getBackwardEstimateF()) {
					this.backward_priority_queue.add(i,roadNode);
					break;
				}else {
					if(i == this.backward_priority_queue.size()-1) {
						this.backward_priority_queue.add(roadNode);
						break;
					}//if(i == this.priority_queue.size()-1)
				}//else
			}//for(int i = 0;i < this.priority_queue.size(); i++) 
		}else {
			this.backward_priority_queue.add(roadNode);
		}//else
	}
	
	/*
	 * add roadNode into forward priority queue (roadNode is in the queue or not in the queue)
	 * 
	 * @param roadNode
	 */
	public void resortForwardPriorityQueue(RoadNode roadNode) {
		if(this.forward_priority_queue.contains(roadNode))
		{
			this.forward_priority_queue.remove(roadNode);
		}
		addForwardPriorityQueue(roadNode);
	}
	
	/*
	 * add roadNode into backward priority queue (roadNode is in the queue or not in the queue)
	 * 
	 * @param roadNode
	 */
	public void resortBackwardPriorityQueue(RoadNode roadNode) {
		if(this.backward_priority_queue.contains(roadNode)) {
			this.backward_priority_queue.remove(roadNode);
		}
		addBackwardPriorityQueue(roadNode);
	}
	
	/*
	 * poll the first node in forward priority queue
	 * 
	 * @return RoadNode
	 */
	public RoadNode pollForwardPriorityQueue() {
		if(this.forward_priority_queue.isEmpty()) {
			return null;
		}else {
			return this.forward_priority_queue.poll();
		}
	}
	
	/*
	 * poll the first node in backward priority queue
	 * 
	 * @return RoadNode
	 */
	public RoadNode pollBackwardPriorityQueue() {
		if(this.backward_priority_queue.isEmpty()) {
			return null;
		}else {
			return this.backward_priority_queue.poll();
		}
	}
	
	public boolean isForwardPriorityQueueEmpty() {
		return this.forward_priority_queue.isEmpty();
	}
	
	public boolean isBackwardPriorityQueueEmpty() {
		return this.backward_priority_queue.isEmpty();
	}
	
	public boolean isIntersection() {
		return this.intersectionFlag;
	}
	
	public void setIntersection() {
		this.intersectionFlag = true;
	}
	
	public boolean isCoreIntersection() {
		return this.coreIntersectionFlag;
	}
	
	public void setCoreIntersection() {
		this.coreIntersectionFlag = true;
	}
	
	public void setOutOfU() {
		this.outOfUFlag = true;
	}
	
	public boolean isOutOfU() {
		return this.outOfUFlag;
	}
	
	public void setFinish() {
		this.finishFlag = true;
	}
	
	public boolean isFinish() {
		return this.finishFlag;
	}
	
	public long getCurrentId() {
		return this.current_id;
	}
	
	
	public Set<RoadNode> getCoreNodeOnS(){
		Set<RoadNode> subSet = new HashSet<RoadNode>();
		Iterator<RoadNode> it = this.S.iterator();
		while(it.hasNext()) {
			RoadNode rnode = it.next();
			if(rnode.isCore()) {
				subSet.add(rnode);
			}
		}
		return subSet;
	}
	
	public Set<RoadNode> getCoreNodeOnT(){
		Set<RoadNode> subSet = new HashSet<RoadNode>();
		Iterator<RoadNode> it = this.T.iterator();
		while(it.hasNext()) {
			RoadNode rnode = it.next();
			if(rnode.isCore()) {
				subSet.add(rnode);
			}
		}
		return subSet;
	}
	
	public void setLimitU(long limitU) {
		this.limitU = limitU;
	}
	
	public long getLimitU() {
		return this.limitU;
	}
	
}