package jdg.graph;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * A class for representing a node of a graph (implemented using adjacency lists)
 *
 * @author Luca Castelli Aleardi (Ecole Polytechnique, feb 2021)
 */
public class Node {

	/** list of neighbors of the current node */
	public ArrayList<Node> neighbors=null;
	/** label of the node: vertex label can differ from vertex index */
	public String label;
	/** tag of node: useful for "marking" a node */
	public int tag;
	/** weight of the node: useful for some applications */
	public double weight=1;
	/** index of a node: an integer value from 0..n-1 */
	public int index;
	public Color color;

	    public Node(int index) { 
	    	this.neighbors=new ArrayList<Node>();
	    	this.index=index;
	    	this.label=null; // no label defined
	    }
	    
	    public Node(int index, Color color) { 
	    	this.neighbors=new ArrayList<Node>();
	    	this.index=index;
	    	this.color=color;
	    	this.label=null; // no label defined
	    }

	    public Node(int index, Color color, String label) { 
	    	this.neighbors=new ArrayList<Node>();
	    	this.index=index;
	    	this.color=color;
	    	this.label=label;
	    }

	    public void addNeighbor(Node v) {
	    	if(v!=this && this.neighbors.contains(v)==false)
	    		this.neighbors.add(v);
	    }

	    public void removeNeighbor(Node v) {
	    	this.neighbors.remove(v);
	    }

	    public boolean adjacent(Node v) {
	    	return this.neighbors.contains(v);
	    }
	    
	    /**
	     * Check whether the current node is isolated (no neighbors)
	     * 
	     * @return true  if the current node is isolated (no neighbors)
	     */
	    public boolean isIsolated() {
	    	if(this.neighbors==null || this.neighbors.size()==0)
	    		return true;
	    	return false;
	    }
	    
	    public void setColor(int r, int g, int b) {
	    	this.color=new Color(r, g, b);
	    }
	    
	    /**
	     * Return the list of neighboring nodes
	     * 
	     * @return  the list of neighbors of the current node
	     */
	    public List<Node> neighborsList() {
	    	return this.neighbors;
	    }
	    
	    public void setTag(int tag) { 
	    	this.tag=tag;
	    }  
	    
	    public int getTag() { 
	    	return this.tag; 
	    }

	    public void setLabel(String label) {
	    	this.label=label;
	    }
	    
	    public String getLabel() {
	    	return this.label;
	    }

	    public int degree() {
	    	return this.neighbors.size();
	    }
	    
	    public String toString(){
	        return "v"+tag;
	    }
	    
	    public int hashCode() {
	    	return this.index;
	    }

}
