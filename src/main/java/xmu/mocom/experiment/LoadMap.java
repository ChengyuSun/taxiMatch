package xmu.mocom.experiment;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import xmu.mocom.astar.Cluster;
import xmu.mocom.coreRouting.CoreEdge;
import xmu.mocom.coreRouting.CoreNode;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;

import xmu.mocom.roadNet.Path;
import xmu.mocom.roadNet.RoadNode;
import xmu.mocom.roadNet.RoadSegmentEdge;
import xmu.mocom.dijkstra.Dijkstra;
import xmu.mocom.simulator.SimClock;

public class LoadMap {

    private static String URL="jdbc:mysql://localhost:3306/seg_speed_average_p?useUnicode=true&characterEncoding=UTF8&useSSL=false";
    private static String USER="root";
    private static String PASSWORD="Xas219988251314!";

    private static int breakPoint=24;
    private static int ut=1;
    private static int core_choose_nums=4000;
    private static int core_nums=50;

    private static String graphInformation="experimentData\\core_choose_nums="+core_choose_nums+"_core_nums="+core_nums+"_graph.ser";
	private static String corePath="experimentData\\core_choose_nums="+core_choose_nums+"_core_nums="+core_nums+".txt";

	//根据osmId找到对应的RoadNode
	public static RoadNode getRoadNodeById(List<RoadNode> RoadNodes,String id)
	{
		int length=RoadNodes.size();
		return findRoadNode(0, length-1, RoadNodes, id);
	}

	public static RoadNode findRoadNode(int left,int right,List<RoadNode> RoadNodes,String id)
	{
		if(left<=right)
		{
			int index=(left+right)/2;
			String nowId=RoadNodes.get(index).getOsmId();
			if(nowId.equals(id))return RoadNodes.get(index);
			else if(Long.parseLong(nowId)>Long.parseLong(id)) return findRoadNode(left, index-1, RoadNodes, id);
			else return findRoadNode(index+1, right, RoadNodes, id);
		}
		else return null;
	}

	//根据边的Id查找对应的TD-cost
	public static List<Long> getTdCostByRelationship(String id,Connection connection) throws SQLException
	{
		List<Long> timeList=new ArrayList<Long>();
		Long sumCost=0L;

		String sql="select * from seg_time_average_p where seg_id="+id+" order by minute_id";
		Statement statement=connection.createStatement();
		ResultSet resultSet=statement.executeQuery(sql);
		for(int i=0;i<breakPoint;i++)
		{
			resultSet.next();
			sumCost = (long)resultSet.getInt("time");
			timeList.add(sumCost);
		}
		statement.close();
		return timeList;
	}

	//加上补充的边，使图单向全联通
	public static void addSupplementaryRoadSegmentEdges(Graph<RoadNode, RoadSegmentEdge> g,List<RoadNode> RoadNodes,Connection conn) throws SQLException
	{
		String sql="select * from add_edge";
		Statement statement=conn.createStatement();
		ResultSet resultSet=statement.executeQuery(sql);
		int start_id=24000;
		while(resultSet.next())
		{
			long start=resultSet.getLong("start");
			long end=resultSet.getLong("end");
			String startT=String.valueOf(start);
			String endT=String.valueOf(end);
			RoadNode sRoadNode=getRoadNodeById(RoadNodes, startT);
			RoadNode eRoadNode=getRoadNodeById(RoadNodes, endT);

			RoadSegmentEdge roadSegmentEdge=g.addEdge(sRoadNode, eRoadNode);
			roadSegmentEdge.setOsm_id(String.valueOf(start_id));
			roadSegmentEdge.setMinDistance(1);
			List<Long> distances=new ArrayList<>();
			for(int i=0;i<breakPoint;i++){
				distances.add(1L);
			}
			roadSegmentEdge.setDistanceList(distances);
			start_id+=1;

			//双向连接
			RoadSegmentEdge roadSegmentEdge1=g.addEdge(eRoadNode, sRoadNode);
			roadSegmentEdge.setOsm_id(String.valueOf(start_id));
			roadSegmentEdge.setMinDistance(1);
			List<Long> distances1=new ArrayList<>();
			for(int i=0;i<breakPoint;i++){
				distances1.add(1L);
			}
			roadSegmentEdge1.setDistanceList(distances1);
			start_id+=1;
		}
		statement.close();
	}

	//加载core节点Id
	public static List<RoadNode> loadCoreId(Graph<RoadNode,RoadSegmentEdge> g,List<RoadNode> RoadNodes) throws Exception{
		List<RoadNode> chooseNodes=new ArrayList<RoadNode>();
		BufferedReader bufferedReader=new BufferedReader(new FileReader(corePath));
		String id="";
		while((id=bufferedReader.readLine())!=null){
			RoadNode roadNode=getRoadNodeById(RoadNodes,id);
			roadNode.setCore();
			chooseNodes.add(roadNode);
			Set<CoreEdge> edgeSet=new HashSet<CoreEdge>();//存哈希SET？
			CoreNode coreNode=new CoreNode(roadNode,edgeSet);
			roadNode.setCoreNode(coreNode);
		}
		return chooseNodes;
	}

	//导入core节点的信息
	public static void addCoreInformation(Graph<RoadNode,RoadSegmentEdge> g,List<RoadNode> RoadNodes) throws Exception{
		List<RoadNode> chooseNodes=loadCoreId(g,RoadNodes);
		for(RoadNode m:chooseNodes){
			for(RoadNode n:chooseNodes){
				if(m!=n){
					LinkedList<Path> pathLinkedList=new LinkedList<Path>();
					long minDistance=0;
					for(int minute_id=0;minute_id<breakPoint;minute_id++){
						SimClock simClock=new SimClock(minute_id*(24*3600*1000)/breakPoint,ut);
						Path path=Dijkstra.singlePath(g,m,n,simClock);
						pathLinkedList.add(path);
						long distance=path.getDistance();
						if(minDistance==0){
							minDistance=distance;
						}
						if(distance<minDistance){
							minDistance=distance;
						}
					}
					CoreEdge coreEdge=new CoreEdge(m.getCoreNode(),n.getCoreNode(),pathLinkedList,minDistance);
					m.getCoreNode().addEdge(coreEdge);
				}
			}
		}
	}

	//构建地图
	public static Graph<RoadNode, RoadSegmentEdge> initMap() throws Exception
	{
		//如果之前存了信息，直接读
		File file=new File(graphInformation);
		if(file.exists()){
			FileInputStream fileIn = new FileInputStream(graphInformation);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			Graph<RoadNode, RoadSegmentEdge> g = (Graph<RoadNode, RoadSegmentEdge>) in.readObject();
			in.close();
			fileIn.close();

			System.out.println("文件读取结束，开始修改内存");
			loadBelongFile(g);
			System.out.println("内存修改完毕，重写文件");
			//将修改后的graph重新存入
			FileOutputStream fileOut =
					new FileOutputStream(graphInformation);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(g);
			out.close();
			fileOut.close();

			System.out.println("文件重写完毕");
			return g;
		}
		else{
			file.createNewFile();
		}
		//DataService.input(n4jMap,breakPoint);

		Graph<RoadNode, RoadSegmentEdge> g = new DefaultDirectedGraph<RoadNode, RoadSegmentEdge>(RoadSegmentEdge.class);

		List<RoadNode> RoadNodes=new ArrayList();

		//将原始图所有的点插入
		Connection connection=DriverManager.getConnection(URL,USER,PASSWORD);
		String sql="select * from node order by id";
		Statement statement=connection.createStatement();
		ResultSet resultSet=statement.executeQuery(sql);
		while(resultSet.next())
		{
			RoadNode RoadNode=new RoadNode();
			long id=resultSet.getLong("id");
			double lon=resultSet.getDouble("lon");
			double lat=resultSet.getDouble("lat");
			RoadNode.setOsmId(String.valueOf(id));
			RoadNode.setLon(lon);
			RoadNode.setLat(lat);

			RoadNodes.add(RoadNode);
			g.addVertex(RoadNode);
		}
		statement.close();

		//加载节点所属信息
		loadBelongFile(g);
		System.out.println("Add RoadNodes finished!");

		//将原始图所有的边插入
		sql="select * from seg_node";
		statement=connection.createStatement();
		resultSet=statement.executeQuery(sql);
		while(resultSet.next())
		{
			long id=resultSet.getLong("seg_id");
			long start=resultSet.getLong("start_id");
			long end=resultSet.getLong("end_id");
			String idT=String.valueOf(id);
			String startT=String.valueOf(start);
			String endT=String.valueOf(end);
			RoadSegmentEdge RoadSegmentEdge=g.addEdge(getRoadNodeById(RoadNodes,startT), getRoadNodeById(RoadNodes, endT));
			RoadSegmentEdge.setOsm_id(idT);
			RoadSegmentEdge.setDistanceList(getTdCostByRelationship(RoadSegmentEdge.getOsm_id(), connection));
		}
		statement.close();

		System.out.println("Add RoadSegmentEdges finished!");

		//将补充的边插入，以满足图的全联通性
		addSupplementaryRoadSegmentEdges(g, RoadNodes, connection);

		System.out.println("Add supplementary RoadSegmentEdges finished!");

		connection.close();

		//将coreNode和coreEdge的信息导入
		addCoreInformation(g,RoadNodes);

		System.out.println("Add core information finished!");

		//将地图信息序列化存入文件中
		FileOutputStream fileOut =
				new FileOutputStream(graphInformation);
		ObjectOutputStream out = new ObjectOutputStream(fileOut);
		out.writeObject(g);
		out.close();
		fileOut.close();

		return g;
	}

	//直接根据序列化文件读取地图
	public static Graph<RoadNode, RoadSegmentEdge> getMap(String fileName) throws Exception{
		File file=new File(fileName);
		if(file.exists()){
			FileInputStream fileIn = new FileInputStream(fileName);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			Graph<RoadNode, RoadSegmentEdge> g = (Graph<RoadNode, RoadSegmentEdge>) in.readObject();
			in.close();
			fileIn.close();
			return g;
		}
		else{
			throw new Exception("can not find serializable file!");
		}
	}


	/*
	 * 将所属core信息加载进入节点中，没有的则选取最近core冒充
	 * @param g
	 * @return void
	 */
	public static void loadBelongFile(Graph<RoadNode, RoadSegmentEdge> g)throws Exception{
        String line="";
        Cluster cluster=new Cluster();

        for(RoadNode roadNode:g.vertexSet()){
			RoadNode core=cluster.findNearCore(roadNode.getOsmId(),g);
			roadNode.setBelongTo(core);
        }
    }

	public static void main(String[] args) throws Exception{
//		long timeStart=System.currentTimeMillis();
//		Graph<RoadNode, RoadSegmentEdge> graph=initMap();
//		long timeEnd=System.currentTimeMillis();
//		long cost=timeEnd-timeStart;
//		System.out.println("init cost:"+cost+"ms");
		LoadMap.initMap();
	}

}
