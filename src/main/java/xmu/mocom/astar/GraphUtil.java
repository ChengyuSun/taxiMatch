package xmu.mocom.astar;

import org.jgrapht.Graph;
import xmu.mocom.roadNet.RoadNode;
import xmu.mocom.roadNet.RoadSegmentEdge;

import java.util.Set;

/**
 * @Author: Chengyu Sun
 * @Description:
 * @Date: Created in 2019/6/28 11:25
 */
public class GraphUtil {
    public static RoadNode findRoadNodeById(Graph<RoadNode, RoadSegmentEdge> graph,String id){
        for(RoadNode roadNode:graph.vertexSet()){
            if(roadNode.getOsmId().equals(id)){
                return roadNode;
            }
        }
        return null;
    }
}
