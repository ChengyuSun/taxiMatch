package xmu.mocom.astar;

import xmu.mocom.roadNet.RoadNode;

/**
 * @Author: Chengyu Sun
 * @Description:
 * @Date: Created in 2019/7/9 8:57
 */
public class AstarNode {
    private AstarNode parent;
    private RoadNode roadNode;
    private boolean isNext;
    private boolean isSteped;
    private long distanceFromStart;
    private long AstarEstimateH;


    public AstarNode(RoadNode roadNode){
        this.roadNode=roadNode;
    }

    public AstarNode getParent() {
        return parent;
    }

    public void setParent(AstarNode parent) {
        this.parent = parent;
    }

    public RoadNode getRoadNode() {
        return roadNode;
    }

    public void setRoadNode(RoadNode roadNode) {
        this.roadNode = roadNode;
    }
}
