package xmu.mocom.roadNet;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.jgrapht.Graph;
import org.jgrapht.graph.DirectedWeightedMultigraph;

public class RoadNetCreator implements Serializable {

	private static double EARTH_RADIUS = 6378.137;
	
	/*
	 * Create the Road Net by osm file
	 * 
	 * @param osm_path
	 * @return a plain jgrapht graph
	 */
	public static Graph<RoadNode, RoadSegmentEdge> CreateRoadNetByOsm(String osm_path) throws DocumentException{
		//read the osm file (a xml file)
		SAXReader reader = new SAXReader();
		Document document = reader.read(new File(osm_path));
		//get the root element of the xml file
		Element root = document.getRootElement();
		//Create a empty road net
		Graph<RoadNode, RoadSegmentEdge> g = new DirectedWeightedMultigraph<>(RoadSegmentEdge.class);
		//traverse all elements
		Iterator<Element> iterator = root.elementIterator();
		while(iterator.hasNext()){
		    Element e = iterator.next(); 
		    //if the element is the type of node
		    if(e.getName().equals("node")){
		    	//create a new RoadNode
		    	RoadNode rnode = new RoadNode(e);
		    	//add the road node into the road net
		    	g.addVertex(rnode);
		    //if the element is the type of way
		    }else if(e.getName().equals("way")){
		    	//the start node of a road segment
		        RoadNode fore_e = null;
		        //traverse all elements to create every road segment
		        Iterator<Element> iter = e.elementIterator();
		        while(iter.hasNext()){
		            Element element = iter.next();  
		            //get all the road node on the way
		            List<Attribute> list1 = element.attributes(); 
		            for(Attribute attribute : list1){
		                if(attribute.getName().equals("ref")) {
		                	//get the start node of a road segment
		                	if(fore_e==null) {
		                		//get the node element whose id == ref.id
		                		fore_e = g.vertexSet().stream().filter(elemen -> elemen.getOsmId().equals(attribute.getValue())).findAny().get();
		                		//System.out.println(fore_e.getOsmId());
		                	}
		                	//get the end node of a road segment
			                else {
		                		RoadNode now_e = g.vertexSet().stream().filter(ee -> ee.getOsmId().equals(attribute.getValue())).findAny().get();
		                		List<Long> speedList = new ArrayList<Long>();
		                		for(long i =0; i < 24; i++){
		                			speedList.add(1L);
		                		}
			                	//add the road segment into road net
		                		RoadSegmentEdge rsEdge = new RoadSegmentEdge(speedList,1);
		                		g.addEdge(fore_e, now_e, rsEdge);
		                		g.setEdgeWeight(rsEdge, 5.0);
		                		fore_e = now_e;
			                }//else
		                }//if(attribute.getName().equals("ref")) 
		            }//for(Attribute attribute : list1)
		        }//while(iter.hasNext())
		    }//else if(e.getName().equals("way"))
		}//while(iterator.hasNext())
		return g;
	}//public static Graph<RoadNode, RoadSegmentEdge> CreateRoadNetByOsm
	
	private static double rad(double d) {
		return d * Math.PI / 180.0;
	}
 
	/**
	 * 通过经纬度获取距离(单位：米)
	 * 
	 * @param lat1
	 * @param lng1
	 * @param lat2
	 * @param lng2
	 * @return 距离
	 */
	public static double getDistance(double lat1, double lng1, double lat2,
			double lng2) {
		double radLat1 = rad(lat1);
		double radLat2 = rad(lat2);
		double a = radLat1 - radLat2;
		double b = rad(lng1) - rad(lng2);
		double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
				+ Math.cos(radLat1) * Math.cos(radLat2)
				* Math.pow(Math.sin(b / 2), 2)));
		s = s * EARTH_RADIUS;
		s = Math.round(s * 10000d) / 10000d;
		s = s * 1000;
		return s;
	}

    public static double getDistance(Element e1, Element e2) {
    	double lat1 = Float.parseFloat(e1.attribute("lat").getValue());
    	double lon1 = Float.parseFloat(e1.attribute("lon").getValue());
    	double lat2 = Float.parseFloat(e2.attribute("lat").getValue());
    	double lon2 = Float.parseFloat(e2.attribute("lon").getValue());
    	return getDistance(lat1,lon1,lat2,lon2);
    }
}
