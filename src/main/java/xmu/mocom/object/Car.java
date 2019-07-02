package xmu.mocom.object;

import xmu.mocom.roadNet.Path;
import xmu.mocom.roadNet.RoadNode;

public class Car {

    private RoadNode location;
    private RoadNode nextLocation;
    private Path path;
    private long residualDistance;  //the residual distance from next location
    private int carStatus;

    public Car(){

    }

    public Car(RoadNode location){
        this.location = location;
        this.nextLocation = location;
        this.path = null;
        this.residualDistance = 0;
        this.carStatus = 0;
    }

    public RoadNode getLocation(){
        return this.location;
    }

    public void setLocation(RoadNode location){
        this.location = location;
    }

    public RoadNode getNextLocation(){
        return nextLocation;
    }

    public void setNextLocation(RoadNode nextLocation){
        this.nextLocation = nextLocation;
    }

    public Path getPath(){
        return this.path;
    }

    public void setPath(Path path){
        this.path = path;
    }

    public long getResidualDistance(){
        return this.residualDistance;
    }

    public void setResidualDistance(long residualDistance){
        this.residualDistance = residualDistance;
    }

    public int getCarStatus(){
        return this.carStatus;
    }

    public void setCarStatus(int carStatus){
        this.carStatus = carStatus;
    }
}