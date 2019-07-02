package xmu.mocom.astar;

import org.dom4j.DocumentException;
import org.jgrapht.Graph;
import xmu.mocom.experiment.LoadMap;
import xmu.mocom.roadNet.RoadNode;
import xmu.mocom.roadNet.RoadSegmentEdge;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @Author: Chengyu Sun
 * @Description:
 * @Date: Created in 2019/6/27 11:10
 */
public class Cluster {

    private  String belongFile="experimentData/belonging_choose_nums=4000_core_nums=50.txt";
    private  String coreFile="experimentData/core_choose_nums=4000_core_nums=50.txt";
    private  Map<String,String> belongingMap;
    private  Set<String> coreSet;

    public Cluster(){
        belongingMap=new HashMap<>();
        coreSet=new HashSet<>();
        try (FileReader reader = new FileReader(belongFile); BufferedReader br = new BufferedReader(reader)) {
                 String line;
                 while ((line = br.readLine()) != null) {
                     belongingMap.put(line.split(":")[0],line.split(":")[1]);
                 }
             } catch (IOException e) {
                 e.printStackTrace();
             }

        try (FileReader reader = new FileReader(coreFile); BufferedReader br = new BufferedReader(reader)) {
            String line;
            while ((line = br.readLine()) != null) {
                coreSet.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public  RoadNode findNearCore(String id, Graph<RoadNode, RoadSegmentEdge> graph){
        if(belongingMap.get(id)!=null){
            return GraphUtil.findRoadNodeById(graph,belongingMap.get(id));
        }
        double shortestDis=99999999;
        String nearCore="";
        for(String coreId:coreSet){
            if(MillerCoordinate.distance(id,coreId,graph)<shortestDis){
                nearCore=coreId;
                shortestDis=MillerCoordinate.distance(id,coreId,graph);
            }
        }

        return GraphUtil.findRoadNodeById(graph,nearCore);
    }

    public Set<String> getCoreSet() {
        return coreSet;
    }

    public Map<String, String> getBelongingMap() {
        return belongingMap;
    }

    public static void main(String[] args)throws IOException, DocumentException, java.lang.Exception{
        System.out.println("graphing...");
        Graph<RoadNode, RoadSegmentEdge> graph = LoadMap.getMap(AStar.GRAPH_FILE);

        for(String coreId:new Cluster().getBelongingMap().values()){
            System.out.println(coreId+": "+GraphUtil.findRoadNodeById(graph,coreId).isCore());
        }

    }
}

