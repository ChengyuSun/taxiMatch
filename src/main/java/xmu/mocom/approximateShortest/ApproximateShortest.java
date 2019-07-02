package xmu.mocom.approximateShortest;

import org.jgrapht.Graph;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

import xmu.mocom.coreRouting.CoreEdge;
import xmu.mocom.dijkstra.Dijkstra;
import xmu.mocom.roadNet.Path;
import xmu.mocom.roadNet.PathSegment;
import xmu.mocom.roadNet.RoadNode;
import xmu.mocom.roadNet.RoadSegmentEdge;
import xmu.mocom.simulator.SimClock;
import xmu.mocom.coreRouting.CoreNode;


public class ApproximateShortest {

    private static double EARTH_RADIUS = 6378.137;

    public static Path approximateShortestPath(Graph<RoadNode, RoadSegmentEdge> g, List<RoadNode> coreNodeList, RoadNode start, RoadNode target, SimClock clock){
        RoadNode sourceCoreNode = getNearbyCoreNode(g,coreNodeList,start,target);
        RoadNode targetCoreNode = getNearbyCoreNode(g,coreNodeList,target,start);
        Path path1 = Dijkstra.singlePath(g,start,sourceCoreNode,clock);
        Path path2 = getCorePath(sourceCoreNode.getCoreNode(), targetCoreNode.getCoreNode(), clock);
        Path path3 = Dijkstra.singlePath(g,targetCoreNode,target,clock);
        return concatPath(path1, path2, path3);
    }

    public static RoadNode getNearbyCoreNode(Graph<RoadNode, RoadSegmentEdge> g, List<RoadNode> coreNodeList,  RoadNode node1, RoadNode node2){
        double lat = node1.getLat();
        double lon = node1.getLon();
        List<RoadNode> candidateNodeList = new ArrayList<RoadNode>();
        Iterator<RoadNode> it = coreNodeList.iterator();
        while(it.hasNext()){
            RoadNode roadNode = it.next();
            double lat2 = roadNode.getLat();
            double lon2 = roadNode.getLon();
            if(Math.abs(lat-lat2)<0.0015 && Math.abs(lon-lon2)<0.0015){
                candidateNodeList.add(roadNode);
            }
        }
        it = candidateNodeList.iterator();
        RoadNode matchNode = null;
        double minDistance = 999999999;
        while(it.hasNext()){
            RoadNode roadNode = it.next();
            double distance = getDistance(node1,roadNode);
            if(distance < minDistance){
                matchNode = roadNode;
                minDistance = distance;
            }
        }
        return matchNode;
    }

    public static Path getCorePath(CoreNode start, CoreNode target, SimClock clock){
        Set<CoreEdge> edgeSet = start.getEdgeSet();
        Iterator<CoreEdge> it = edgeSet.iterator();
        CoreEdge matchEdge = null;
        while(it.hasNext()){
            CoreEdge coreEdge = it.next();
            if(coreEdge.isTargetNode(target)){
                matchEdge = coreEdge;
                break;
            }
        }
        return matchEdge.getTimeDependentPathList().get(clock.getMinuteId());
    }

    public static Path concatPath(Path path1, Path path2, Path path3){
        Path path = new Path();
        List<PathSegment> segmentList = path.getSegmentList();
        segmentList.addAll(path1.getSegmentList());
        segmentList.addAll(path2.getSegmentList());
        segmentList.addAll(path3.getSegmentList());
        System.out.println(path1.getDistance());
        System.out.println(path2.getDistance());
        System.out.println(path3.getDistance());
        path.addDistance(path1.getDistance());
        path.addDistance(path2.getDistance());
        path.addDistance(path3.getDistance());
        System.out.println(path.getDistance());
        return path;
    }

    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }

    /*
     * 通过经纬度获取距离(单位：米)
     *
     * @param lat1
     * @param lng1
     * @param lat2
     * @param lng2
     * @return 距离
     */
    public static double getDistance(double lat1, double lng1, double lat2,
                                     double lng2) {
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lng1) - rad(lng2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                + Math.cos(radLat1) * Math.cos(radLat2)
                * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000d) / 10000d;
        s = s * 1000;
        return s;
    }

    public static long getDistance(RoadNode node1, RoadNode node2) {
        double lat1 = node1.getLat();
        double lon1 = node1.getLon();
        double lat2 = node2.getLat();
        double lon2 = node2.getLon();
        return (long) getDistance(lat1,lon1,lat2,lon2);
    }
}
