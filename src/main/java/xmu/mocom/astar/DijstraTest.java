package xmu.mocom.astar;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import xmu.mocom.dijkstra.Dijkstra;
import xmu.mocom.experiment.LoadMap;
import xmu.mocom.roadNet.Path;
import xmu.mocom.roadNet.RoadNode;
import xmu.mocom.roadNet.RoadSegmentEdge;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;

import static xmu.mocom.astar.AStar.GRAPH_FILE;

/**
 * @Author: Chengyu Sun
 * @Description:
 * @Date: Created in 2019/7/5 8:52
 */
public class DijstraTest {

    public static String disPath="experimentData/disPath.txt";
    public static String disPath2="experimentData/disPath2.txt";
    public static String astarPath="experimentData/astarPath.txt";
    public static void bettter(Graph<RoadNode, RoadSegmentEdge> graph,RoadNode start, RoadNode target){

        DijkstraShortestPath<RoadNode, DefaultWeightedEdge> dijkstraAlg = new DijkstraShortestPath(graph);
        GraphPath<RoadNode,DefaultWeightedEdge> thepath = dijkstraAlg.getPath(start, target);
        //System.out.println("path:="+thepath);
        Iterator it = thepath.getVertexList().iterator();
        FileUtil.cleanFile(disPath);
        while(it.hasNext()) {
            RoadNode roadNode = (RoadNode) it.next();
            FileUtil.record(roadNode,disPath,true);
        }
    }

    public static void dis(Graph<RoadNode, RoadSegmentEdge> graph,RoadNode start, RoadNode target, ClockSimulator clock){
        Path path= Dijkstra.singlePath(graph,start,target,clock);
        FileUtil.record(path,disPath2,false);
    }

    public static void astar(Graph<RoadNode, RoadSegmentEdge> graph,RoadNode start, RoadNode target, ClockSimulator clock){
        Path path= new AStar().AstarRegular(graph,start,target,clock);
        FileUtil.record(path,astarPath,false);
    }

    public static void main(String[] args)throws Exception{
        System.out.println("graphing...");
        Instant i=Instant.now();
        Graph<RoadNode, RoadSegmentEdge> graph = LoadMap.getMap(GRAPH_FILE);
        Instant ii=Instant.now();
        System.out.println("反序列化文件的时间为："+ Duration.between(i,ii).toMillis());

        RoadNode start=GraphUtil.findRoadNodeById(graph,"4012919279");
        RoadNode target=GraphUtil.findRoadNodeById(graph,"2774722514");

        System.out.println("start: "+start.getLon()+", "+start.getLat());
        System.out.println("target: "+target.getLon()+", "+target.getLat());

        Instant i1=Instant.now();
        Graph<RoadNode, RoadSegmentEdge> g=GraphUtil.graphClone(graph);
        Instant i2=Instant.now();
        bettter(g,start,target);
        Instant i3=Instant.now();

        g=GraphUtil.graphClone(graph);
        Instant i4=Instant.now();
        dis(g,start,target,new ClockSimulator(5000000));
        Instant i5=Instant.now();

        g=GraphUtil.graphClone(graph);
        Instant i6=Instant.now();
        astar(g,start,target,new ClockSimulator(5000000));
        Instant i7=Instant.now();

        System.out.println("反序列化文件的时间为："+ Duration.between(i,ii).toMillis());
        System.out.println("复制图的时间为： "+Duration.between(i1,i2).toMillis());
        System.out.println("工具包自带dis时间为： "+Duration.between(i2,i3).toMillis());
        System.out.println("自己dis时间为： "+Duration.between(i4,i5).toMillis());
        System.out.println("Astar时间为： "+Duration.between(i6,i7).toMillis());
    }

}
