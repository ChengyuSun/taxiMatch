package xmu.mocom.object;

import xmu.mocom.roadNet.RoadNode;

public class Request {

    private RoadNode start;
    private RoadNode target;
    private long arrivalTime;
    private long expectedT1;
    private long expectedT2;
    private boolean matchedFlag;



    public Request(RoadNode start, RoadNode target, long arrivalTime, long expectedT1, long expectedT2){
        this.start = start;
        this.target = target;
        this.arrivalTime = arrivalTime;
        this.expectedT1 = expectedT1;
        this.expectedT2 = expectedT2;
        this.matchedFlag = false;
    }

    public RoadNode getStart(){
        return this.start;
    }

    public void setStart(RoadNode start){
        this.start = start;
    }

    public RoadNode getTarget(){
        return this.target;
    }

    public void setTarget(RoadNode target){
        this.target = target;
    }

    public long getArrivalTime() {
        return this.arrivalTime;
    }

    public void setArrivalTime(long arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public long getExpectedT1() {
        return this.expectedT1;
    }

    public void setExpectedT1(long expectedT1) {
        this.expectedT1 = expectedT1;
    }

    public long getExpectedT2() {
        return this.expectedT2;
    }

    public void setExpectedT2(long expectedT2) {
        this.expectedT2 = expectedT2;
    }

    public boolean isMatched() {
        return this.matchedFlag;
    }

    public void setMatched() {
        this.matchedFlag = true;
    }

    public void setUnmatched(){
        this.matchedFlag = false;
    }
}
