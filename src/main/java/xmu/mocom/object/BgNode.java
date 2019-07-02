package xmu.mocom.object;

import java.util.ArrayList;
import java.util.List;

public class BgNode {

    private List<BgEdge> bgEdgeList;

    public BgNode(){
        bgEdgeList = new ArrayList<BgEdge>();
    }

    public void addEdge(BgEdge bgEdge){
        this.bgEdgeList.add(bgEdge);
    }
}
