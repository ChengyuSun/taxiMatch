package xmu.mocom.astar;

import org.dom4j.DocumentException;
import org.jgrapht.Graph;
import xmu.mocom.coreRouting.CoreEdge;
import xmu.mocom.coreRouting.CoreNode;
import xmu.mocom.experiment.LoadMap;
import xmu.mocom.roadNet.Path;
import xmu.mocom.roadNet.PathSegment;
import xmu.mocom.roadNet.RoadNode;
import xmu.mocom.roadNet.RoadSegmentEdge;

import java.io.*;
import java.util.*;

/**
 * @Author: Chengyu Sun
 * @Description:
 * @Date: Created in 2019/6/24 11:21
 */
public class AStar {

    public static String GRAPH_FILE = "experimentData/core_choose_nums=4000_core_nums=50_graph.ser";
    private Set<String> S=new HashSet<String>();
    private List<RoadNode> nextRoadNodes=new ArrayList<>();
    private static double speed=0.0065;

    public static void main(String[] args)throws IOException, DocumentException, java.lang.Exception{

        System.out.println("graphing...");
        Graph<RoadNode, RoadSegmentEdge> graph = LoadMap.getMap(GRAPH_FILE);

        RoadNode n1=GraphUtil.findRoadNodeById(graph,"4012919283");
        RoadNode n2=GraphUtil.findRoadNodeById(graph,"1223212190");

        AStar aStar=new AStar();
        ClockSimulator clock=new ClockSimulator(100000);

        RoadNode end=aStar.expandToCore(graph,n1,n2,clock);

    }

    /*
     * 找到当前节点通往的所有节点，并为这些节点赋予对应时间、距离等信息,最后进行A*优先级排序
     * @param graph
     * @param start
     * @return java.util.List<xmu.mocom.roadNet.RoadNode>
     */
    public void findNextRoadNodes
    (Graph<RoadNode, RoadSegmentEdge> graph, RoadNode start,RoadNode target,ClockSimulator clock){
        for(RoadSegmentEdge edge:start.getAllNextEdge(graph)){  //对于当前节点连接的所有边
            if(graph.getEdgeSource(edge).getOsmId()==start.getOsmId()){  //选择以当前节点为起点的边

                graph.getEdgeTarget(edge).getDijkstraNode().setDistance(
                        graph.getEdgeSource(edge).getDijkstraNode().getDistance()+edge.getDistanceList().get(clock.getHour())
                );
                graph.getEdgeTarget(edge).getDijkstraNode().setParentNode(start);//保存父节点信息
                AstarEstimate(graph,target,graph.getEdgeTarget(edge));
                nextRoadNodes.add(graph.getEdgeTarget(edge));
            }
        }
        Collections.sort(nextRoadNodes, new Comparator<RoadNode>() {
            @Override
            public int compare(RoadNode o1, RoadNode o2) {
                if(o1.getDijkstraNode().getEstimateF()<o2.getDijkstraNode().getEstimateF()){
                    return -1;
                }
                else if(o1.getDijkstraNode().getEstimateF()==o2.getDijkstraNode().getEstimateF()){
                    return 0;
                }
                return 1;
            }
        });

    }


    /*
     * 估计当前节点到目标节点所属core节点的时间，作为其estimateH值
     * @param graph
     * @param target
     * @param currentNode
     * @return void
     */
    public void AstarEstimate
            (Graph<RoadNode, RoadSegmentEdge> graph,RoadNode target,RoadNode currentNode){
        RoadNode targetCore=new Cluster().findNearCore(target.getOsmId(),graph);  //找到目的地所属core节点

        currentNode.getDijkstraNode().setEstimateH(
                (long)(MillerCoordinate.distance(currentNode,targetCore)/speed)
        );
    }


    /*
     * 从起点出发，按照Astar算法进行探索
     * @param null
     * @return
     */

    public RoadNode expandToCore(Graph<RoadNode, RoadSegmentEdge> graph,RoadNode start,RoadNode target,ClockSimulator clock){

        start.getDijkstraNode().setParentNode(null);
        start.getDijkstraNode().setDistance(0);

        if(start.getOsmId()==target.getOsmId()||start.isCore()){
            return start;
        }

        findNextRoadNodes(graph,start,target,clock);

        RoadNode oldStart=start;
        RoadNode newStart=getTopFromNextRoadNodes();

        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("experimentData/testPath.txt")));
            while (!newStart.isCore()&&!newStart.getOsmId().equals(target.getOsmId())){
                //将节点存储于文件中
                out.write(newStart.getOsmId()+":"+newStart.getLon()+":"+newStart.getLat()+"\n");
                System.out.println("到达节点:"+newStart.getOsmId());
                S.add(newStart.getOsmId());//避免之后重复该节点
                RoadSegmentEdge choosenEdge=graph.getEdge(oldStart,newStart);//找到对应边


                clock.setNow(newStart.getDijkstraNode().getDistance());//时钟重置时间
                System.out.println("现在时间为: "+clock);

                findNextRoadNodes(graph,newStart,target,clock);//找到新起点的所有下一个节点

                oldStart=newStart;
                newStart=getTopFromNextRoadNodes();
            }
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(newStart.isCore()){
            System.out.println("找到core节点");
            RoadNode targetCore=new Cluster().findNearCore(target.getOsmId(),graph);
            System.out.println("目标附近core节点："+targetCore.isCore()+"  "+targetCore.getLon()+","+targetCore.getLat());
            findCorePath(newStart.getCoreNode(),targetCore.getCoreNode(),clock);
        }
        else{
            System.out.println("找到终点");
        }

        return newStart;
    }


    /*
     * 选择下一个节点
     * @param
     * @return xmu.mocom.roadNet.RoadNode
     */
    public RoadNode getTopFromNextRoadNodes(){
        for(RoadNode roadNode:nextRoadNodes){
            if(!S.contains(roadNode.getOsmId())){
                return roadNode;
            }
        }
        return null;
    }


    /*
     * 找到core节点路径并保存在原来的文件中
     * @param coreStart
     * @param coreTarget
     * @return void
     */
    public void findCorePath(CoreNode coreStart,CoreNode coreTarget,ClockSimulator clock){
        CoreEdge coreEdge=null;
        System.out.println(coreStart.getEdgeSet().size());
        System.out.println(coreTarget.getEdgeSet().size());
        for(CoreEdge c:coreStart.getEdgeSet()){
            if(c.isStartNode(coreStart)&&c.isTargetNode(coreTarget)){
                coreEdge=c;
                break;
            }
        }
        if(coreEdge==null){
            System.out.println("未找到对应CoreEdge");
            return;
        }
        FileWriter fw=null;
        try {
            fw=new FileWriter("experimentData/testPath.txt",true);
            Path path=coreEdge.getTimeDependentPathList().get(clock.getHour());
            while (path!=null){
                PathSegment pathSegment=path.pollPathSegment();
                RoadNode roadNode=pathSegment.getEndNode();
                fw.write(roadNode.getOsmId()+":"+roadNode.getLon()+":"+roadNode.getLat()+"\n");
            }

            //fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void findLeftPath(Graph<RoadNode, RoadSegmentEdge> graph){

    }


}
