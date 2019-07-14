package xmu.mocom.astar;

import org.dom4j.DocumentException;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import xmu.mocom.coreRouting.CoreEdge;
import xmu.mocom.coreRouting.CoreNode;
import xmu.mocom.dijkstra.Dijkstra;
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
    private static String ThirdPath="experimentData/leftPath.txt";
    private static String FirstPath="experimentData/testPath.txt";
    private static String CorePath="experimentData/corePath.txt";
    private Set<String> S=new HashSet<String>();
    private List<RoadNode> nextRoadNodes=new ArrayList<>();
    private static double speed=0.0065;

    public static void main(String[] args)throws IOException, DocumentException, java.lang.Exception{

        System.out.println("graphing...");
        Graph<RoadNode, RoadSegmentEdge> graph = LoadMap.getMap(GRAPH_FILE);

        /*  test 1  效果奇差
         * # 起点 (118.1496317, 24.4898824,    4012919283
            # 终点 (118.07227, 24.4578902,     1223212190
            # startCore  118.0881711,24.4828203
            # TargetCore 118.0717192, 24.4625586
         */

        /*test2  效果较好
        start: 118.192591, 24.4865512  1422597009
        target: 118.126015, 24.5356673  1422693845

        startCore: 118.1917327,24.4847002
        targetCore: 118.1298394,24.5329152
         */

        /*test3  结果奇差
        start: 118.1494332, 24.4901656  4012919276
        target: 118.1361622, 24.4721757  2815371666
        startCore: 118.1464654,24.4914873
        targetCore: 118.1439876,24.4794368
         */

        /* test4  结果很好
        start: 118.0888667, 24.4423123
        target: 118.1603893, 24.4883155
        startCore: 118.0886226,24.4396882
        targetCore: 118.1572804,24.4853667
         */

        RoadNode n1=GraphUtil.findRoadNodeById(graph,"4012919283");
        RoadNode n2=GraphUtil.findRoadNodeById(graph,"1223212190");

        AStar aStar=new AStar();
        ClockSimulator clock=new ClockSimulator(1000000);

        System.out.println("start: "+n1.getLon()+", "+n1.getLat());
        System.out.println("target: "+n2.getLon()+", "+n2.getLat());

        aStar.astarExpand(graph,n1,n2,clock);
        //aStar.findLeftPath(graph,n1,n2,clock);

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
            if(graph.getEdgeSource(edge).getOsmId().equals(start.getOsmId())){  //选择以当前节点为起点的边
                RoadNode edgeTarget=graph.getEdgeTarget(edge);//当前边的终点
                if(S.contains(edgeTarget.getOsmId())){
                    continue;
                }
                long distance=start.getDijkstraNode().getDistance()+edge.getDistanceList().get(clock.getHour());
                //预计到达时间
                if(nextRoadNodes.contains(edgeTarget)){
                    if(distance< edgeTarget.getDijkstraNode().getDistance()){
                        nextRoadNodes.remove(edgeTarget);//删去旧的
                    }
                    else {
                        continue;
                    }
                }
                edgeTarget.getDijkstraNode().setParentNode(start);//保存父节点信息
                edgeTarget.getDijkstraNode().setDistance(distance);
                AstarEstimate(target,edgeTarget);
                nextRoadNodes.add(edgeTarget);
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
            (RoadNode target,RoadNode currentNode){
        RoadNode targetCore=target.getBelongTo();  //找到目的地所属core节点

        currentNode.getDijkstraNode().setEstimateH(
                (long)(MillerCoordinate.distance(currentNode,targetCore)/speed)
        );
    }


    /*
     * 从起点出发，按照Astar算法进行探索
     * @param null
     * @return
     */
    public void astarExpand(Graph<RoadNode, RoadSegmentEdge> graph,RoadNode start,RoadNode target,ClockSimulator clock){
        Path path=new Path();
        start.getDijkstraNode().setParentNode(null);//把起点初始化
        start.getDijkstraNode().setDistance(0);

        //起点就是终点
        if(start.getOsmId()==target.getOsmId()){
            return ;
        }
        //起点就是core节点
        if(start.isCore()){
            coreToTarget(graph,start,target,clock);
            return ;
        }

        findNextRoadNodes(graph,start,target,clock);
        String newStartId=getTopFromNextRoadNodes().getOsmId();
        S.add(start.getOsmId());
        RoadNode newStart=GraphUtil.findRoadNodeById(graph,newStartId);

        while (!newStart.isCore()&&!newStartId.equals(target.getOsmId())){
            //将节点存储于文件中
            S.add(newStartId);//避免之后重复该节点
            clock.setNow(newStart.getDijkstraNode().getDistance());//时钟重置时间
            findNextRoadNodes(graph,newStart,target,clock);//找到新起点的所有下一个节点
            newStartId=getTopFromNextRoadNodes().getOsmId();
            newStart=GraphUtil.findRoadNodeById(graph,newStartId);
        }

        path=findPathFromTail(newStart);
        FileUtil.record(path,FirstPath,false);

        if(newStart.isCore()){
            coreToTarget(graph,newStart,target,clock);
            return;
        }
        System.out.println("直接拓展到终点");
    }

    /*
     * 选择下一个节点
     * @param
     * @return xmu.mocom.roadNet.RoadNode
     */
    private RoadNode getTopFromNextRoadNodes(){
        return nextRoadNodes.remove(0);
    }

    /*
     * 找到core节点路径并保存在原来的文件中
     * @param coreStart
     * @param coreTarget
     * @return void
     */
    private void findCorePath(CoreNode coreStart,CoreNode coreTarget,ClockSimulator clock){
        CoreEdge coreEdge=null;
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

        Path path=coreEdge.getTimeDependentPathList().get(clock.getHour());
        FileUtil.record(path,CorePath,false);
        clock.addmsec(path.getDistance());
    }



    /*
     * 将剩余的路径添加到文件中
     * @param graph
     * @param start
     * @param target
     * @param clock
     * @return void
     */
    private void findLeftPath(Graph<RoadNode, RoadSegmentEdge> graph,RoadNode start,RoadNode target,ClockSimulator clock){
        Path path= Dijkstra.singlePath(graph,start,target,clock);
        System.out.println("leftStart: "+start.getLon()+", "+start.getLat());
        System.out.println("leftTarget: "+target.getLon()+", "+target.getLat());

        FileUtil.record(path,ThirdPath,false);
        clock.addmsec(path.getDistance());
    }

    /*
     * 后两个步骤
     * @param graph
     * @param core
     * @param target
     * @param clock
     * @return void
     */
    private void coreToTarget(Graph<RoadNode, RoadSegmentEdge> graph,RoadNode core,RoadNode target,ClockSimulator clock){
        RoadNode targetCore=target.getBelongTo();
        System.out.println("startCore: "+core.getLon()+", "+core.getLat());
        System.out.println("targetCore: "+targetCore.getLon()+", "+targetCore.getLat());
        findCorePath(core.getCoreNode(),targetCore.getCoreNode(),clock);
        findLeftPath(graph,targetCore,target,clock);
    }


    /*
     * 通用Astar
     * @param graph
     * @param start
     * @param target
     * @param clock
     * @return xmu.mocom.roadNet.Path
     */
    public Path AstarRegular(Graph<RoadNode, RoadSegmentEdge> graph,RoadNode start,RoadNode target,ClockSimulator clock){
        Path path=new Path();
        if(start.getOsmId().equals(target.getOsmId())){
            path.addPathSegmentFirst(new PathSegment(start,target,0));
            return path;
        }

        start.getDijkstraNode().setParentNode(null);//把起点初始化
        start.getDijkstraNode().setDistance(0);
        S.add(start.getOsmId());

        findNextRoadNodes(graph,start,target,clock);
        String newStartId=getTopFromNextRoadNodes().getOsmId();
        RoadNode newStart=GraphUtil.findRoadNodeById(graph,newStartId);

        while (!newStartId.equals(target.getOsmId())){
            S.add(newStartId);//避免之后重复该节点
            clock.setNow(newStart.getDijkstraNode().getDistance());//时钟重置时间
            findNextRoadNodes(graph,newStart,target,clock);//找到新起点的所有下一个节点
            newStartId=getTopFromNextRoadNodes().getOsmId();
            newStart=GraphUtil.findRoadNodeById(graph,newStartId);
        }
        path=findPathFromTail(target);
        return path;
    }

    /*
     * 从尾部还原路径
     * @param tail
     * @return xmu.mocom.roadNet.Path
     */
    public Path findPathFromTail(RoadNode tail){
        Path path=new Path();
        while (tail.getDijkstraNode().getParentNode()!=null){
            RoadNode parent=tail.getDijkstraNode().getParentNode();
            path.addPathSegmentFirst(new PathSegment(
                    tail,parent, tail.getDijkstraNode().getDistance()-parent.getDijkstraNode().getDistance()
            ));
            tail=parent;
        }
        return path;
    }


}
