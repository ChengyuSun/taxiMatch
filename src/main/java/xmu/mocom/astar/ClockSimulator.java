package xmu.mocom.astar;

import xmu.mocom.simulator.SimClock;

/**
 * @Author: Chengyu Sun
 * @Description:
 * @Date: Created in 2019/6/27 9:12
 */
public class ClockSimulator extends SimClock {


    private long origin;
    private long now;

//    private long hour;
//    private long min;
//    private long sec;
//    private long msec;
//
//    public ClockSimulator(long hour, long min, long sec, long msec) {
//        this.hour = hour;
//        this.min = min;
//        this.sec = sec;
//        this.msec = msec;
//    }

    public ClockSimulator(long x){
        super(x);
        now=origin=x;
    }

    public void addmsec(long cost){
        now+=cost;
        now=now%86400000;
    }

    public void setNow(long distance){
        now=origin+distance;
    }


//    public void flush(){
//        sec+=(msec/1000);
//        msec=msec%1000;
//
//        min+=sec/60;
//        sec=sec%60;
//
//        hour+=min/60;
//        min=min%60;
//
//        hour=hour%24;
//    }


    public int getHour(){
        return (int)(now%86400000)/3600000;
    }

//    public String toString(){
//        return "hour:"+hour+",minute:"+min+",second:"+sec+",msec:"+msec;
//    }

    public long getNow(){
        return now;
    }

    public String toString(){
        long hour=now/3600000;
        long min=(now%3600000)/60000;
        long sec=(now%60000)/1000;
        long msec=now%1000;
        return "hour:"+hour+" min:"+min+" sec:"+sec+" msec:"+msec;
    }

}
