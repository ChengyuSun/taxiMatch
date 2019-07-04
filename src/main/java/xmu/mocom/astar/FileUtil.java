package xmu.mocom.astar;

import xmu.mocom.roadNet.Path;
import xmu.mocom.roadNet.PathSegment;
import xmu.mocom.roadNet.RoadNode;

import java.io.FileWriter;
import java.io.IOException;

/**
 * @Author: Chengyu Sun
 * @Description:
 * @Date: Created in 2019/7/4 9:02
 */
public class FileUtil {
    /*
     * 将节点信息记录进入文件中，可以选择追加或是重写
     * @param roadNode
     * @param filePath
     * @param isAppend
     * @return void
     */
    public static void record(RoadNode roadNode, String filePath, boolean isAppend){
        try {
            FileWriter fw=new FileWriter(filePath,isAppend);
            fw.write(roadNode.getOsmId()+":"+roadNode.getLon()+":"+roadNode.getLat()+"\n");
            //fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * 重载函数，直接记录一条路径
     * @param path
     * @param filePath
     * @param isAppend
     * @return void
     */
    public static void record(Path path,String filePath,boolean isAppend){
        try {
            FileWriter fw=new FileWriter(filePath,isAppend);
            while (!path.isEmpty()){
                PathSegment pathSegment=path.pollPathSegment();
                RoadNode roadNode=pathSegment.getEndNode();
                fw.write(roadNode.getOsmId()+":"+roadNode.getLon()+":"+roadNode.getLat()+"\n");
            }
            //fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /*
     * 清空文件
     * @param filePath
     * @return void
     */
    public static void cleanFile(String filePath){
        try {
            FileWriter fw=new FileWriter(filePath,false);
            fw.write("");
            //fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
