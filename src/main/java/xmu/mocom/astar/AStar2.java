package xmu.mocom.astar;

import org.dom4j.DocumentException;
import org.jgrapht.Graph;
import xmu.mocom.coreRouting.CoreEdge;
import xmu.mocom.coreRouting.CoreNode;
import xmu.mocom.dijkstra.Dijkstra;
import xmu.mocom.experiment.LoadMap;
import xmu.mocom.roadNet.Path;
import xmu.mocom.roadNet.PathSegment;
import xmu.mocom.roadNet.RoadNode;
import xmu.mocom.roadNet.RoadSegmentEdge;

import java.io.IOException;
import java.util.*;

import static xmu.mocom.astar.AStar.GRAPH_FILE;

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
    private static String Path4="experimentData/path4.txt";

    public static void main(String[] args)throws IOException, DocumentException, java.lang.Exception{

        System.out.println("graphing...");
        Graph<RoadNode, RoadSegmentEdge> graph = LoadMap.getMap(GRAPH_FILE);

        RoadNode n1=GraphUtil.findRoadNodeById(graph,"2791910141");
        RoadNode n2=GraphUtil.findRoadNodeById(graph,"1408154683");

        AStar2 aStar2=new AStar2();
        ClockSimulator clock=new ClockSimulator(1000000);

        System.out.println("start: "+n1.getLon()+", "+n1.getLat());
        System.out.println("target: "+n2.getLon()+", "+n2.getLat());

        //aStar2.astarExpand(graph,n1,n2,clock);
        Path path=aStar2.astarPath(graph,n1,n2,clock,false);
        FileUtil.record(path,Path4,false);

    }
    /*
     * 找到当前节点通往的所有节点，并为这些节点赋予对应时间、距离等信息,最后进行A*优先级排序
     * @param graph
     * @param start
     * @return java.util.List<xmu.mocom.roadNet.RoadNode>
     */
    public void findNextRoadNodes
    (Graph<RoadNode, RoadSegmentEdge> graph, RoadNode start, RoadNode target, ClockSimulator clock){
        for(RoadSegmentEdge edge:start.getAllNextEdge(graph)){  //对于当前起点连接的所有边（双向边）
            if(graph.getEdgeSource(edge).equals(start)){  //选择以当前节点为起点的边（单向边）
                RoadNode edgeTarget=graph.getEdgeTarget(edge);
                System.out.println("探索到了节点："+edgeTarget.getOsmId());

                if(allNodes.containsKey(edgeTarget.getOsmId())){
                   if(allNodes.get(edgeTarget.getOsmId()).isSteped()){
                       System.out.println("已经涉足，直接跳过");
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
        sortList(nextRoadNodes);
    }

    /*
     * 将下一步可达节点按照Astar优先级排列
     * @param list
     * @return void
     */
    private void sortList(List<SearchNode> list){
        Collections.sort(list, new Comparator<SearchNode>() {
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
    * 功能描述
    * @param graph
    * @param start
    * @param target
    * @param clock
    * @param isToCore   可选择拓展到core/target
    * @return xmu.mocom.roadNet.Path
    */
    public Path astarPath
    (Graph<RoadNode, RoadSegmentEdge> graph,RoadNode start,RoadNode target,ClockSimulator clock,boolean isToCore){
        SearchNode newStart=new SearchNode(start);
        allNodes.put(start.getOsmId(),newStart);
        newStart.setDistance(0);

        while (!newStart.getRoadNode().equals(target)){
            if(isToCore&&newStart.getRoadNode().isCore()){
                break;
            }
            System.out.println("到达节点："+newStart.getRoadNode().getOsmId());
            newStart.setSteped(true);
            clock.setNow(newStart.getDistance());//时钟重置时间
            findNextRoadNodes(graph,newStart.getRoadNode(),target,clock);//找到新起点的所有下一个节点
            newStart=getTopFromNextRoadNodes();
        }

        Path path=findPathFromTail(newStart);

        if(newStart.getRoadNode().equals(target)){
            System.out.println("直接拓展到终点");
        }
        else {
            System.out.println("拓展到core节点");
            System.out.println("startCore: "+newStart.getRoadNode().getLon()+", "+newStart.getRoadNode().getLat());
            addPath(path,findCorePath(newStart.getRoadNode().getCoreNode(),target.getBelongTo().getCoreNode(),clock));
            addPath(path,astarPath(graph,target.getBelongTo(),target,clock,false));
        }
        return path;
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
    private Path findCorePath(CoreNode coreStart, CoreNode coreTarget, ClockSimulator clock){
        CoreEdge coreEdge=null;
        for(CoreEdge c:coreStart.getEdgeSet()){
            if(c.isStartNode(coreStart)&&c.isTargetNode(coreTarget)){
                coreEdge=c;
                break;
            }
        }
        if(coreEdge==null){
            System.out.println("未找到对应CoreEdge");
            return  null;
        }

        Path path=coreEdge.getTimeDependentPathList().get(clock.getHour());
        clock.addmsec(path.getDistance());
        return path;
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
    private void coreToCore(Graph<RoadNode, RoadSegmentEdge> graph,RoadNode core,RoadNode target,ClockSimulator clock){
        RoadNode targetCore=target.getBelongTo();
        System.out.println("startCore: "+core.getLon()+", "+core.getLat());
        System.out.println("targetCore: "+targetCore.getLon()+", "+targetCore.getLat());
        findCorePath(core.getCoreNode(),targetCore.getCoreNode(),clock);
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

    /*
     * 将两条path合并
     * @param p1
     * @param p2
     * @return void
     */
    public void addPath(Path p1,Path p2){
        while (!p2.isEmpty()){
            PathSegment pathSegment=p2.pollPathSegment();
            p1.addPathSegmentLast(pathSegment);
        }
    }
}
