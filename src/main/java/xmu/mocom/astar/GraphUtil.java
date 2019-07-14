package xmu.mocom.astar;

import org.jgrapht.Graph;
import xmu.mocom.roadNet.RoadNode;
import xmu.mocom.roadNet.RoadSegmentEdge;

import java.io.*;
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

    public static Graph<RoadNode, RoadSegmentEdge> graphClone(Graph<RoadNode, RoadSegmentEdge> graph){
        Graph<RoadNode, RoadSegmentEdge> result=null;
        try {
            System.out.println("开始序列化对象");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(graph);

            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            result = (Graph<RoadNode, RoadSegmentEdge>) ois.readObject();
            System.out.println("结果读取完成");

            oos.close();
            ois.close();

        } catch (Exception e) {
            System.out.println("出现错误"+e.getMessage());
            e.printStackTrace();
        }
        return result;
    }
}
