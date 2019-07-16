package xmu.mocom.roadNet;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
/*
 * path of the road net
 */
public class Path implements Serializable {

	private static final long serialVersionUID = 7780355356501363789L;

	private LinkedList<PathSegment> segmentList;
	private long distance;
	
	public Path() {
		segmentList = new LinkedList<PathSegment>();
		this.distance = 0;
	}
	
	public void addPathSegmentFirst(PathSegment pathSegment) {
		segmentList.addFirst(pathSegment);
		this.distance += pathSegment.getDistance();
	}
	
	public PathSegment pollPathSegment() {
		PathSegment pathSegment = segmentList.poll();
		this.distance -= pathSegment.getDistance();
		return pathSegment;
	}
	
	public boolean isEmpty() {
		return this.segmentList.isEmpty();
	}
	
	public long getDistance() {
		return this.distance;
	}

	public List<PathSegment> getSegmentList(){
		return this.segmentList;
	}

	public void addDistance(long distance){
		this.distance += distance;
	}

	public void addPathSegmentLast(PathSegment pathSegment){
		segmentList.addLast(pathSegment);
		this.distance+=pathSegment.getDistance();
	}
}


