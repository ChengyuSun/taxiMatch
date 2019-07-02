package xmu.mocom.object;

public class BgEdge {

    private int capacity;
    private int residualFlow;
    private double costWeight;
    private BgNode fromNode;
    private BgNode toNode;
    private BgEdge reverseEdge;
    private boolean forwardFlag;

    private BgEdge(BgNode fromNode, BgNode toNode){
        this.fromNode = fromNode;
        this.toNode = toNode;
    }

    public BgEdge(BgNode fromNode, BgNode toNode, int capacity, int residualFlow, double costWeight){
        this.fromNode = fromNode;
        this.toNode = toNode;
        this.capacity = capacity;
        this.residualFlow = residualFlow;
        this.costWeight = costWeight;
        this.setForward();
        fromNode.addEdge(this);
        toNode.addEdge(this);

        BgEdge reverseEdge = new BgEdge(toNode, fromNode);
        reverseEdge.setCapacity(capacity);
        reverseEdge.setResidualFlow(capacity-residualFlow);
        reverseEdge.setCostWeight(-costWeight);
        reverseEdge.setBackward();
        fromNode.addEdge(reverseEdge);
        toNode.addEdge(reverseEdge);

        this.setReverseEdge(reverseEdge);
        reverseEdge.setReverseEdge(this);
    }

    public int getCapacity() {
        return this.capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getResidualFlow() {
        return this.residualFlow;
    }

    public void setResidualFlow(int residualFlow) {
        this.residualFlow = residualFlow;
    }

    public double getCostWeight() {
        return this.costWeight;
    }

    public void setCostWeight(double costWeight) {
        this.costWeight = costWeight;
    }

    public BgNode getFromNode() {
        return this.fromNode;
    }

    public void setFromNode(BgNode fromNode) {
        this.fromNode = fromNode;
    }

    public BgNode getToNode() {
        return this.toNode;
    }

    public void setToNode(BgNode toNode) {
        this.toNode = toNode;
    }

    public BgEdge getReverseEdge() {
        return this.reverseEdge;
    }

    public void setReverseEdge(BgEdge reverseEdge) {
        this.reverseEdge = reverseEdge;
    }

    public boolean isForward() {
        return forwardFlag;
    }

    public void setForward() {
        this.forwardFlag = true;
    }

    public void setBackward(){
        this.forwardFlag = false;
    }
}
