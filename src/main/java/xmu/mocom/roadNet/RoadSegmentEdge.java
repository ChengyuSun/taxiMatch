package xmu.mocom.roadNet;

import java.io.Serializable;
import java.util.List;
import org.jgrapht.graph.DefaultWeightedEdge;

public class RoadSegmentEdge extends DefaultWeightedEdge implements Serializable {
	
	private static final long serialVersionUID = 5883728839235218851L;
			
	private List<Long> distanceList;
	private long minDistance;
	private String osm_id;

	public RoadSegmentEdge() {

	}
	
	public RoadSegmentEdge(List<Long> list, long minDistance){
		this.distanceList = list;
		this.minDistance = minDistance;
	}
	
	public List<Long> getDistanceList() {
		return this.distanceList;
	}
	
	public void setDistanceList(List<Long> list) {
		this.distanceList = list;
	}
	
	public long getMinDistance() {
		return minDistance;
	}
	
	public void setMinDistance(long distance) {
		this.minDistance = distance;
	}

	public String getOsm_id() {
		return osm_id;
	}

	public void setOsm_id(String osm_id) {
		this.osm_id = osm_id;
	}
	
	
}