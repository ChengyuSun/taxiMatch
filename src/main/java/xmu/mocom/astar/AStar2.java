package xmu.mocom.astar;

import org.jgrapht.Graph;
import xmu.mocom.coreRouting.CoreEdge;
import xmu.mocom.coreRouting.CoreNode;
import xmu.mocom.dijkstra.Dijkstra;
import xmu.mocom.roadNet.Path;
import xmu.mocom.roadNet.PathSegment;
import xmu.mocom.roadNet.RoadNode;
import xmu.mocom.roadNet.RoadSegmentEdge;

import java.util.*;

/**
 * @Author: Chengyu Sun
 * @Description:
 * @Date: Created in 2019/7/15 8:40
 */
public class AStar2 {

    private List<SearchNode> nextRoadNodes=new ArrayList<>();
    private Map<String,SearchNode> allNodes=new HashMap<>(); //为了从roadNode找到searchNode
    private static double speed=0.0065;


    private static String Path1="experimentData/path1.txt";
    private static String Path2="experimentData/path2.txt";
    private static String Path3="experimentData/path3.txt";

    /*
     * 找到当前节点通往的所有节点，并为这些节点赋予对应时间、距离等信息,最后进行A*优先级排序
     * @param graph
     * @param start
     * @return java.util.List<xmu.mocom.roadNet.RoadNode>
     */
    public void findNextRoadNodes
    (Graph<RoadNode, RoadSegmentEdge> graph, RoadNode start, RoadNode target, ClockSimulator clock){
        for(RoadSegmentEdge edge:start.getAllNextEdge(graph)){  //对于当前节点连接的所有边
            if(graph.getEdgeSource(edge).getOsmId().equals(start.getOsmId())){  //选择以当前节点为起点的边
                RoadNode edgeTarget=graph.getEdgeTarget(edge);


                if(allNodes.containsKey(edgeTarget.getOsmId())){
                   if(allNodes.get(edgeTarget.getOsmId()).isSteped()){
                       continue;
                   }
                }
                else {
                    allNodes.put(edgeTarget.getOsmId(),new SearchNode(edgeTarget));
                }

                SearchNode se=allNodes.get(edgeTarget.getOsmId());
                long distance=allNodes.get(start.getOsmId()).getDistance()+edge.getDistanceList().get(clock.getHour());
                //预计到达时间
                if(nextRoadNodes.contains(se)){
                    if(distance< se.getDistance()){
                        nextRoadNodes.remove(se);//删去旧的
                    }
                    else {
                        continue;
                    }
                }
                se.setParent(allNodes.get(start.getOsmId()));//保存父节点信息
                se.setDistance(distance);
                AstarEstimate(target,se);
                nextRoadNodes.add(se);
            }
        }
        Collections.sort(nextRoadNodes, new Comparator<SearchNode>() {
            @Override
            public int compare(SearchNode o1, SearchNode o2) {
                if(o1.getEstimateF()<o2.getEstimateF()){
                    return -1;
                }
                else if(o1.getEstimateF()==o2.getEstimateF()){
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
    (RoadNode target,SearchNode currentNode){
        RoadNode targetCore=target.getBelongTo();  //找到目的地所属core节点

        currentNode.setEstimateH(
                (long)(MillerCoordinate.distance(currentNode.getRoadNode(),targetCore)/speed)
        );
    }


    /*
     * 从起点出发，按照Astar算法进行探索
     * @param null
     * @return
     */
    public void astarExpand(Graph<RoadNode, RoadSegmentEdge> graph,RoadNode start,RoadNode target,ClockSimulator clock){

        //起点就是终点
        if(start.getOsmId()==target.getOsmId()){
            return ;
        }
        //起点就是core节点
        if(start.isCore()){
            coreToTarget(graph,start,target,clock);
            return ;
        }

        SearchNode startSearchNode=new SearchNode(start);
        allNodes.put(start.getOsmId(),startSearchNode);
        startSearchNode.setSteped(true);
        findNextRoadNodes(graph,start,target,clock);
        SearchNode newStart=getTopFromNextRoadNodes();

        while (!newStart.getRoadNode().isCore()&&!newStart.getRoadNode().equals(target)){
            newStart.setSteped(true);
            clock.setNow(newStart.getDistance());//时钟重置时间
            findNextRoadNodes(graph,newStart.getRoadNode(),target,clock);//找到新起点的所有下一个节点
            newStart=getTopFromNextRoadNodes();
        }

        Path path=findPathFromTail(newStart);
        FileUtil.record(path,Path1,false);

        if(newStart.getRoadNode().isCore()){
            coreToTarget(graph,newStart.getRoadNode(),target,clock);
            return;
        }
        System.out.println("直接拓展到终点");
    }

    /*
     * 选择下一个节点
     * @param
     * @return xmu.mocom.roadNet.RoadNode
     */
    private SearchNode getTopFromNextRoadNodes(){
        return nextRoadNodes.remove(0);
    }

    /*
     * 找到core节点路径并保存在原来的文件中
     * @param coreStart
     * @param coreTarget
     * @return void
     */
    private void findCorePath(CoreNode coreStart, CoreNode coreTarget, ClockSimulator clock){
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
        FileUtil.record(path,Path2,false);
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

        FileUtil.record(path,Path3,false);
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


        SearchNode statSearchNode=new SearchNode(start);
        statSearchNode.setSteped(true);
        findNextRoadNodes(graph,start,target,clock);
        SearchNode newStart=getTopFromNextRoadNodes();

        while (!newStart.getRoadNode().equals(target)){
            newStart.setSteped(true);
            clock.setNow(newStart.getDistance());//时钟重置时间
            findNextRoadNodes(graph,newStart.getRoadNode(),target,clock);//找到新起点的所有下一个节点
            newStart=getTopFromNextRoadNodes();
        }
        path=findPathFromTail(newStart);
        return path;
    }

    /*
     * 从尾部还原路径
     * @param tail
     * @return xmu.mocom.roadNet.Path
     */
    public Path findPathFromTail(SearchNode tail){
        Path path=new Path();
        while (tail.getParent()!=null){
            SearchNode parent=tail.getParent();
            path.addPathSegmentFirst(new PathSegment(
                    tail.getRoadNode(),parent.getRoadNode(), tail.getDistance()-parent.getDistance()
            ));
            tail=parent;
        }
        return path;
    }

}
