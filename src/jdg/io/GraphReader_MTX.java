package jdg.io;

import java.awt.Color;
import java.nio.file.Files;
import java.nio.file.Paths;

import jdg.graph.AdjacencyListGraph;
import jdg.graph.Node;
import tc.TC;
import Jcg.geometry.Point_3;

/**
 * Provides methods for dealing with graphs stored in Matrix Market format.
 */		   
public class GraphReader_MTX {

    /**
     * Read a graph stored in MTX format
     * 
     * Remark: nodes have indices between 1..n
     */		   
    public static AdjacencyListGraph read(String filename) { 
    	System.out.print("Reading graph in MTX format ("+filename+")...");
    	TC.lectureDansFichier(filename);
    	String ligne=TC.lireLigne();
    	String[] tabFrom=TC.motsDeChaine(ligne);
    	
    	while(tabFrom[0].charAt(0)=='%') { // read file header (commented lines)
    		ligne=TC.lireLigne();
    		tabFrom=TC.motsDeChaine(ligne);
    	}
    	
		int n=Integer.parseInt(tabFrom[0]); // number of vertices
		
    	//System.out.println("m.vertices "+m.points.length);
    	//System.out.println("m.faces "+m.faces.length);
    	//System.out.println("m.halfedges "+m.sizeHalfedges);    	
    	
    	AdjacencyListGraph g=new AdjacencyListGraph();
    	int i=0;
    	//System.out.print("\tSetting vertices...");
    	while(i<n) { // read vertex indices
    		int index=i; // recall that vertices must have indices between 0 and n-1
    		Point_3 p=new Point_3(0., 0., 0.); // vertex coordinates are still not defined
    		Color color=null;
    		
    		g.addNode(new Node(index, color));
    		i++;
    	}
    	//System.out.println("done ("+g.sizeVertices()+")");
    	
    	//System.out.print("\tSetting edges...");
    	i=0;
    	while(TC.finEntree()==false) { // read edges
    		ligne=TC.lireLigne();
    		tabFrom=TC.motsDeChaine(ligne);
    		if(tabFrom!=null && tabFrom.length>0 && tabFrom[0].charAt(0)!='%') {
    			int index1=Integer.parseInt(tabFrom[0])-1; // recall that vertices have indices in 1..n in MTX format
    			int index2=Integer.parseInt(tabFrom[1])-1; // recall that vertices have indices in 1..n in MTX format
    			Node v1=g.getNode(index1);
    			Node v2=g.getNode(index2);
    			
    			if(v1==null || v2==null) {
    				throw new Error("Error: wrong vertex indices "+index1+" "+index2);
    			}
    			if(v1!=v2 && g.adjacent(v1, v2)==false && g.adjacent(v2, v1)==false) { // loops and multiple edges are not allowed
    				g.addEdge(v1, v2); // addEdge already adds the two edges (v1, v2) and (v2, v1)
    				i++;
    			}
    		}
    	}
    	//System.out.println("done ("+g.sizeEdges()+")");
    	
    	System.out.println("done ("+g.nodes.size()+" vertices, "+g.sizeEdges()+" edges)");
    	TC.lectureEntreeStandard();
    	return g;
    }
 
}
