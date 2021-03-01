package jdg.graph;

import java.util.*;

import Jcg.geometry.PointCloud_3;
import Jcg.geometry.Point_3;

/**
 * Pointer based implementation of an Adjacency List Representation of a graph.
 * The graph is assumed to be undirected (with no multiple edges and loops).
 * 
 * @author Luca Castelli Aleardi (Ecole Polytechnique, INF562)
 *
 */
public class AdjacencyListGraph {
	
	/** store the nodes of the graph */
	public ArrayList<Node> nodes;
	/** the edges of the graph, stored in a hash map as pairs of nodes */
	public HashMap<Edge,Integer> edges;
	
	/** 
	 * Initialize an empty graph
	 **/
	public AdjacencyListGraph() {
		this.nodes=new ArrayList<Node>(); // empty list of nodes
		this.edges=new HashMap<Edge,Integer>(); // empty collection of edges
	}
	
	/** 
	 * Add a new node to the graph. <br>
	 * Remark: <br>
	 * -) if the label of 'v' is not defined, add the node (without any check) <br>
	 * -) otherwise, before adding the node the function check whether it already exists (expensive test).
	 **/
	public void addNode(Node v) {
		if(v.label==null) {
			this.nodes.add(v);
		}
		else {
			if(this.nodes.contains(v)==false) {
				this.nodes.add(v);
			}
		}
	}
	
	/** 
	 * Return the index of the edge having extremities (a, b) if any.
	 * 
	 * @return the index of edge (a, b). <br> Return -1 if the edge (a, b) does not exist in the graph.
	 **/
	public int getEdgeIndex(Node a, Node b) {
		Edge e=new Edge(a, b, 0);
		
		if(this.edges.containsKey(e)==true) {
			return this.edges.get(e);
		}
		else
			return -1;
	}
	
	/** 
	 * Return the node given its index (between [0..n-1])
	 **/
	public Node getNode(int index) {
		if(index>=0 && index<this.nodes.size()) {
			return this.nodes.get(index);
		}
		return null;
	}
	
	public void removeNode(Node v) {
		throw new Error("To be updated/implemented");
	}

	/** 
	 * Add an edge between two nodes
	 **/
   public void addEdge(Node a, Node b) {
    	if(a==null || b==null)
    		return;
    	
		Edge e=new Edge(a, b, 0);
		if(this.edges.containsKey(e)==false) { // check whether the edge already exists
			int nE=this.edges.size();
			a.addNeighbor(b);
			b.addNeighbor(a);
			this.edges.put(e, nE); // add the edge in the hashmap
		}
    }

	/** 
	 * Remove the edge between two nodes
	 **/
    public void removeEdge(Node a, Node b){
    	if(a==null || b==null)
    		return;
    	a.removeNeighbor(b);
    	b.removeNeighbor(a);
    }
    
	/** 
	 * Check whether two nodes are adjacent (slow implementation, without hash maps)
	 **/
    public boolean adjacent(Node a, Node b) {
    	if(a==null || b==null)
    		throw new Error("Graph error: vertices not defined");
    	return a.adjacent(b);
    }
    
	/** 
	 * Return the degree of a node
	 * 
	 * @return the degree (number of neighbors) of the input node. If the node is not defined (null)
	 * return 0.
	 **/
    public int degree(Node v) {
    	if(v!=null)
    		return v.degree();
    	else return 0;
    }
    
    public Collection<Node> getNeighbors(Node v) {
    	return v.neighborsList();
    }
        
	/**
     * Return the number of nodes
     */		
    public int sizeVertices() {
    	return this.nodes.size();
    }
    
	/**
     * Return the number of arcs
     * 
     * Remark: arcs are not counted twice
     */		
    public int sizeEdges() {
    	int result=0;
    	for(Node v: this.nodes)
    		result=result+getNeighbors(v).size();
    	return result/2;
    }
    
    /**
     * Return an array storing all vertex indices, according to the order of vertices
     */		   
    public int[] getIndices() {
    	int[] result=new int[this.nodes.size()];
    	
    	int count=0;
    	for(Node u: this.nodes) {
    		if(u!=null) {
    			result[count]=u.index;
    			count++;
    		}
    	}
    	return result;
    }
    
    /**
     * Compute and return the connected component containing the vertex v
     * It performs a DFS visit of the graph starting from vertex v
     * <p>
     * Remark: the graph is assumed to be undirected
     * 
     * @param v  the starting node
     * @return the list of nodes lying in the same connected component of v
     */		   
    public List<Node> findConnectedComponent(Node v){
    	if(v==null)
    		return null;
    	
    	LinkedList<Node> component=new LinkedList<Node>(); // the connected component containing v
    	
    	HashSet<Node> visited=new HashSet<Node>(); // the set of vertices already visited
    	LinkedList<Node> stack=new LinkedList<Node>(); // stack containing the node to visit
    	
    	stack.add(v);
    	while(stack.isEmpty()==false) {
    		Node u=stack.poll(); // get and removes the node in the head of the stack
    		if(visited.contains(u)==false) {
    			visited.add(u); // mark the vertex as visited
    			component.add(u); // add the vertex to the connected component
    			for(Node neighbor: u.neighbors)
    				stack.add(neighbor); // add all neighboring vertices to the stack
    		}
    	}
    	
    	return component;
    }
        
    /**
     * Check whether the graph is connected
     * 
     * Remark: the graph is assumed to be undirected
     * 
     */		   
    public boolean isConnected(){
    	int isolatedVertices=0;
    	for(Node v: this.nodes)
    		if(v==null || v.degree()==0)
    			isolatedVertices++; // count isolated vertices
    	if(isolatedVertices>0) // is there are isolated vertices the graph cannot be connected
    		return false;
    	
    	// compute the size of the connected component containing v0
    	int sizeConnectedComponent=this.findConnectedComponent(this.nodes.get(0)).size();
    	
    	if(sizeConnectedComponent==this.sizeVertices())
    		return true;
    	return false;
    }
    
    /**
     * Compute the minimum vertex index of the graph (a non negative number)
     * 
     * Remark: vertices are allowed to have indices between 0..infty
     * This is required when graphs are dynamic: vertices can be removed
     */		   
    public int minVertexIndex() {
    	int result=Integer.MAX_VALUE;
    	for(Node v: this.nodes) {
    		if(v!=null)
    			result=Math.min(result, v.index); // compute max degree
    	}
    	return result;
    }

    /**
     * Compute the maximum vertex index of the graph (a non negative number)
     * 
     * Remark: vertices are allowed to have indices between 0..infty
     * This is required when graphs are dynamic: vertices can be removed
     */		   
    public int maxVertexIndex() {
    	int result=0;
    	for(Node v: this.nodes) {
    		if(v!=null)
    			result=Math.max(result, v.index); // compute max degree
    	}
    	return result;
    }
    
    /**
     * Return a string containing informations and parameters of the graph
     */		   
    public String info() {
    	String result=sizeVertices()+" vertices, "+sizeEdges()+" edges\n";
    	
    	int isolatedVertices=0;
    	int maxDegree=0;
    	for(Node v: this.nodes) {
    		if(v==null || v.degree()==0)
    			isolatedVertices++; // count isolated vertices
    		//if(v!=null && v.p!=null && v.p.distanceFrom(new Point_3()).doubleValue()>0.) // check geometric coordinates
    		//	geoCoordinates=true;
    		if(v!=null)
    			maxDegree=Math.max(maxDegree, v.degree()); // compute max degree
    	}
    	result=result+"isolated vertices: "+isolatedVertices+"\n";
    	result=result+"max vertex degree: "+maxDegree+"\n";
    	
    	result=result+"min and max vertex index: "+minVertexIndex();
    	result=result+"..."+maxVertexIndex()+"\n";
    	
    	if(this.isConnected()==true)
    		result=result+"the graph is connected\n";
    	else
    		result=result+"the graph is not connected\n";
    	
    	return result;
    }

}
