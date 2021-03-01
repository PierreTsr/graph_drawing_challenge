package sw;

import java.awt.Color;
import java.util.ArrayList;

import Jcg.polyhedron.Halfedge;
import Jcg.polyhedron.Polyhedron_3;
import Jcg.geometry.*;

/**
 * @author Luca Castelli Aleardi (Ecole Polytechnique, fev 2021)
 * 
 * Provides methods for representing and manipulating
 * an edge orientation of a map (also known as alpha-orientation)
 * Half-edges are assumed to be numbered, from 0.. (2e-1)
 */
public abstract class EdgeOrientation {

	/** the input triangle mesh (combinatorial map) */
    public Polyhedron_3<Point_> polyhedron;
    /** for each half-edge 'h', says whether 'h' is "oriented" toward his target vertex */
    public boolean[] isWellOriented;
    /** stores an integer representing the coloring of an edge: 0, 1, or 2 */
    public byte[] edgeColor; //

    public boolean[] getEdgeOrientation() {
    	return this.isWellOriented;
    }
    
    public byte[] getEdgeColoration() {
    	return this.edgeColor;
    }
    
	/**
	 * Return a string representing the edge coloring/orientation
	 */
    public String orientationToString() {
    	String result="";
    	
    	for(Halfedge<Point_> e: (ArrayList<Halfedge<Point_>>)this.polyhedron.halfedges) {
    		result=result+e.getOpposite().getVertex().index+","+e.getVertex().index+", color"+this.edgeColor[e.index]+", direction="+this.isWellOriented[e.index]+"\n";
    	}
    	return result;
    }
    
    public Color[] getEdgeColors() {
    	Color[] result=new Color[this.edgeColor.length];
    	
    	for(int i=0;i<this.edgeColor.length;i++) {
    		if(edgeColor[i]==0) result[i]=Color.red;
    		else if(edgeColor[i]==1) result[i]=Color.blue;
    		else if(edgeColor[i]==2) result[i]=Color.black;
    		else if(edgeColor[i]==3) result[i]=Color.orange;
    		else if(edgeColor[i]==5) result[i]=Color.lightGray;
    		
    		else result[i]=Color.gray;
    	}
    	return result;
    }

}
