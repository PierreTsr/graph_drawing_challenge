
import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import Jcg.geometry.Point_3;
import Jcg.mesh.MeshLoader;
import Jcg.polyhedron.Halfedge;
import Jcg.polyhedron.Polyhedron_3;
import jdg.graph.AdjacencyListGraph;
import jdg.graph.Node;
import processing.data.JSONArray;
import processing.data.JSONObject;
import tc.TC;

/**
 * This class provides methods for dealing with input/output for JSON files storing planar graphs
 * 
 * @author Luca Castelli Aleardi (Ecole Polytechnique, nov 2020)
 */
public class IO {
	
	/**
	 * Load an input instance (the input planar graph) from an input JSON file
	 * 
	 * @param filename  name of the file storing the input instance
	 */
	public static GridLayout loadInputFromJSON(String filename){
		GridPoint[][] bendPoints=null;
		GridPoint[] points=null;
		AdjacencyListGraph g=new AdjacencyListGraph();
		
		String name=filename.replaceAll(".json", "");
		System.out.print("Reading JSON input file: "+filename+"...");
		JSONObject json;
		
		json = readJSON(filename);
		System.out.println("ok");
		
		int width = json.getInt("width");
		int height = json.getInt("height");
		int nBends = json.getInt("bends");
		
		JSONArray nodes = json.getJSONArray("nodes");
		JSONArray edges = json.getJSONArray("edges");
		
		int n=nodes.size();
		int e=edges.size();
		
		System.out.println("\t name= "+name);
		System.out.println("\t n= "+n);
		System.out.println("\t e= "+e);
		System.out.println("\t bends= "+nBends);
		
		System.out.print("Reading vertices...");
		points=new GridPoint[n];
		for(int i=0;i<n;i++) {
			JSONObject coordinates=nodes.getJSONObject(i);
			int id=coordinates.getInt("id");
			int x=coordinates.getInt("x");
			int y=coordinates.getInt("y");
			points[i]=new GridPoint(x, y);
			
    		Color color=null; // no color defined for this application
    		g.addNode(new Node(id, color));
			//System.out.println("point "+id+" "+points[i]);
		}
		System.out.println("done");

		System.out.print("Reading edges...");
		bendPoints=new GridPoint[e][];
		for(int i=0;i<e;i++) {
			JSONObject edge=edges.getJSONObject(i);
			int index1=edge.getInt("source");
			int index2=edge.getInt("target");
			JSONArray bends=edge.getJSONArray("bends");
			Node v1=g.getNode(index1);
			Node v2=g.getNode(index2);

			if(v1==null || v2==null) {
				throw new Error("Error: wrong vertex indices "+index1+" "+index2);
			}
			if(v1!=v2 && g.adjacent(v1, v2)==false && g.adjacent(v2, v1)==false) { // loops and multiple edges are not allowed
				g.addEdge(v1, v2); // addEdge already adds the two edges (v1, v2) and (v2, v1)
				if(bends!=null) {
					int edgeIndex=g.getEdgeIndex(v1, v2);
					//System.out.println("edge "+i+" "+bends.size()+" bends");
					bendPoints[i]=new GridPoint[bends.size()];
					for(int j=0;j<bends.size();j++) {
						JSONObject bendPoint=bends.getJSONObject(j);
						int x=bendPoint.getInt("x");
						int y=bendPoint.getInt("y");
						bendPoints[i][j]=new GridPoint(x, y);
					}
				}
				else // no bends defined for this edge
					bendPoints[i]=null;
			}
    	}
    	System.out.println("done");

		System.out.println("Input instance loaded from file\n------------------");
		System.out.println(g.sizeVertices()+" vertices, "+g.sizeEdges()+" edges");
		
		GridLayout result=new GridLayout(name, g, points, bendPoints, nBends, width, height);
		return result;
	}
	
	/**
	 * Load an input instance (the input planar graph) from an input OFF file storing a planar mesh <br>
	 * 
	 * Remark: bends are not defined when loading the graph from OFF file
	 * 
	 * @param filename  name of OFF file storing a planar mesh
	 * @param maxBends  maximal number of bends per edge
	 * @param useCoordinates  use geometric coordinates of the input embedding (if TRUE); 
	 * otherwise do not load geometric coordinates
	 */
	public static GridLayout loadInputFromOFF(String filename, int maxBends, boolean useCoordinates){
		GridPoint[][] bendPoints=null;
		GridPoint[] points=null;
		AdjacencyListGraph g=new AdjacencyListGraph();
		
		String name=filename.replaceAll(".off", "");
		System.out.println("Reading OFF input file: "+filename+"...");
		Polyhedron_3<Point_3> mesh=MeshLoader.getSurfaceMesh(filename);
		
		int n=mesh.sizeOfVertices();
		int e=mesh.sizeOfHalfedges()/2;
		
		int width = n*4;
		int height = width;
		int nBends = maxBends;
		
		System.out.println("\t name= "+name);
		System.out.println("\t n= "+n);
		System.out.println("\t e= "+e);
		System.out.println("\t bends= "+nBends);
		
		System.out.print("Setting vertices...");
		points=new GridPoint[n];
		int x, y, id;
		for(int i=0;i<n;i++) {
			id=i;
			x=0;
			y=0;
			points[i]=new GridPoint(x, y);
			
    		Color color=null; // no color defined for this application
    		g.addNode(new Node(id, color));
			//System.out.println("point "+id+" "+points[i]);
		}
		System.out.println("done");

		System.out.print("Setting edges...");
		bendPoints=new GridPoint[e][];
		for(Halfedge h: mesh.halfedges) { // iterate over all half-edges
			int index1=h.getOpposite().getVertex().index;
			int index2=h.getVertex().index;
			Node v1=g.getNode(index1);
			Node v2=g.getNode(index2);

			if(v1==null || v2==null) {
				throw new Error("Error: wrong vertex indices "+index1+" "+index2);
			}
			if(index1<index2 && g.adjacent(v1, v2)==false && g.adjacent(v2, v1)==false) { // loops and multiple edges are not allowed
				g.addEdge(v1, v2); // addEdge already adds the two edges (v1, v2) and (v2, v1)
				int edgeIndex=g.getEdgeIndex(v1, v2);
				bendPoints[edgeIndex]=null; // bends are not defined
			}
    	}
    	System.out.println("done");

		System.out.println("Input instance loaded from file\n------------------");
		System.out.println(g.sizeVertices()+" vertices, "+g.sizeEdges()+" edges");
		
		return new GridLayout(name, g, points, bendPoints, nBends, width, height);
	}

	/**
	 * Load a JSON object from input file
	 * 
	 * @param filename name of the input file
	 */
	private static JSONObject readJSON(String filename) {
		JSONObject outgoing=null;
		BufferedReader reader = null;
		FileReader fr = null;

		try {
			fr = new FileReader(filename);
			reader = new BufferedReader(fr);
			outgoing = new JSONObject(reader);
		} catch (IOException e) {
			System.err.format("IOException: %s%n", e);
		} finally {
			try {
				if (reader != null)
					reader.close();

				if (fr != null)
					fr.close();
			} catch (IOException ex) {
				System.err.format("IOException: %s%n", ex);
			}
		}
		
		return outgoing;
	}

    /**
     * Output a grid layout of a planar graph (with polylines) to a JSON file
     * 
     */		   
    public static void saveLayoutToJSON(GridLayout layout, String output) {
    	System.out.print("Saving the grid layout of a planar graph to Json file: "+output+" ...");
    	
    	TC.ecritureDansNouveauFichier(output);
    	
    	int n=layout.n;
    	int nE=layout.e; // number of arcs
    	
    	TC.println("{"); // first line
    	
    	// write nodes
    	TC.println("    \"nodes\": [");
    	int i=0;
    	for(Node u: layout.g.nodes) {
    		GridPoint p=layout.points[u.index];
    		TC.println("\t{"); // start encoding a new node
    		TC.println("\t    \"id\": "+u.index+",");
    		TC.println("\t    \"x\": "+p.getX()+",");
    		TC.println("\t    \"y\": "+p.getY());
    		
    		if(i!=n-1)
    			TC.println("\t},"); // the node is not the last one
    		else
    			TC.println("\t}"); // last node
    		
    		i++;
    	}
    	TC.println("    ],");

    	// write edges
    	i=0;
    	TC.println("    \"edges\": [");
    	for(Node u: layout.g.nodes) { // iterate over all nodes
    		for(Node v: u.neighbors) { // iterate over all neighbors of 'u'
    			if(u.index<v.index) { // write an edge only once, when u<v
    				TC.println("\t{"); // start encoding a new edges
    				TC.println("\t    \"source\": "+u.index+",");
    				TC.println("\t    \"target\": "+v.index);

    				if(i!=nE-1)
    					TC.println("\t},"); // the edge is not the last one
    				else
    					TC.println("\t}"); // last edge

    				i++;
    			}
    		}
    	}
    	TC.println("    ],");
    	
    	// output the width and height
    	TC.println("    \"width\": "+layout.width+",");
    	TC.println("    \"height\": "+layout.height+",");
    	TC.println("    \"bends\": "+layout.maxBends);
    	TC.println("}");
    	
    	TC.ecritureSortieStandard();
    	System.out.println("done ("+n+" vertices, "+i+" edges)");
    }
	
}
