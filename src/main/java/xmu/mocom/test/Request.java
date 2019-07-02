package xmu.mocom.test;

public class Request {

    private long startId;
    private long targetId;

    public Request(long startId, long targetId){
        this.startId = startId;
        this.targetId = targetId;
    }

    public long getStartId(){
        return this.startId;
    }

    public long getTargetId(){
        return this.targetId;
    }
}
