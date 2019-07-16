package xmu.mocom.roadNet;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.jgrapht.Graph;

import xmu.mocom.astar.GraphUtil;
import xmu.mocom.coreRouting.CoreNode;
import xmu.mocom.dijkstra.DijkstraNode;

public class RoadNode implements Serializable {

    private String osm_id;    //osm id
    private double lon;        //经度
    private double lat;        //纬度
    private boolean coreFlag;
   // private DijkstraNode dijkstraNode;
    private CoreNode coreNode;
    private RoadNode belongTo;//从属于哪个core节点

    public RoadNode() {
        coreFlag = false;
        //dijkstraNode = null;
        coreNode = null;
    }

    public RoadNode(Element e) {
        List<Attribute> list = e.attributes();
        for (Attribute attribute : list) {
            if (attribute.getName().equals("id"))
                this.osm_id = attribute.getValue();
            if (attribute.getName().equals("lon"))
                this.lon = Double.parseDouble(attribute.getValue());
            if (attribute.getName().equals("lat"))
                this.lat = Double.parseDouble(attribute.getValue());
        }
        coreFlag = false;
        //dijkstraNode = null;
        coreNode = null;
    }

    public String getOsmId() {
        return osm_id;
    }


    public void setOsmId(String osm_id) {
        this.osm_id = osm_id;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLon() {
        return this.lon;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLat() {
        return this.lat;
    }

    public void setCore() {
        this.coreFlag = true;
    }

    public boolean isCore() {
        return this.coreFlag;
    }

//    public void setDijkstraNode(DijkstraNode node) {
//        this.dijkstraNode = node;
//    }
//
//    public DijkstraNode getDijkstraNode() {
//        return this.dijkstraNode;
//    }

    public Set<RoadSegmentEdge> getAllNextEdge(Graph<RoadNode, RoadSegmentEdge> g) {
        Set<RoadSegmentEdge> edgeSet = g.edgesOf(this);
        Iterator it = edgeSet.iterator();
        Set<RoadSegmentEdge> nextEdgeSet = new HashSet<RoadSegmentEdge>();
        while (it.hasNext()) {
            RoadSegmentEdge nextEdge = (RoadSegmentEdge) it.next();
            if (g.getEdgeSource(nextEdge) == this) {
                nextEdgeSet.add(nextEdge);
            }
        }
        return nextEdgeSet;
    }

    public boolean setCoreNode(CoreNode coreNode) {
        if (this.coreFlag) {
            this.coreNode = coreNode;
            return true;
        } else {
            return false;
        }
    }

    public CoreNode getCoreNode() {
        return this.coreNode;
    }

    public void setBelongTo(Graph<RoadNode, RoadSegmentEdge> g,String coreId){
        RoadNode core= GraphUtil.findRoadNodeById(g,coreId);
        this.belongTo=core;
    }

    public void setBelongTo(RoadNode coreNode){
        this.belongTo=coreNode;
    }

    public RoadNode getBelongTo(){
        return belongTo;
    }

}

