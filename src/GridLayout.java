import Jcg.geometry.Point_3;
import Jcg.polyhedron.Polyhedron_3;
import jdg.graph.AdjacencyListGraph;

/**
 * Planar Grid Layout of a graph: vertices are drawn as 2D points with integer coordinates (on a regular grid of size wxh). <br>
 * <br>
 * This class stores an instance of the input problem: <br>
 * -) the name of the input graph <br>
 * -) the input graph <tt>g</tt> <br>
 * -) the input parameters: <tt>width</tt>, <tt>height</tt>, maximum number of bends <br>
 *  -) It stores the 2D coordinates, of both vertices and bend points, if the drawing is provided with a planar embedding <br>
 * 
 * @author Luca Castelli Aleardi (Ecole Polytechnique)
 * @version feb 2021
 */
public class GridLayout {
	/** name of the input graph */
	public String name;
	/** number of vertices of the input graph */
	public int n;
	/** number of edges of the graph */
	public int e;
	/** width/height of the grid */
	public int width, height;
	/** maximum number of bends per edge (input of the problem) */
	public int maxBends;

	/** Input graph to draw: no combinatorial embedding provided (only the 1-skeleton of the graph) */	
	public AdjacencyListGraph g;

	/** 
	 * 2D positions of the vertices of the graph defining the planar layout. <br>
	 * Remark: vertices are indexed from 0..n-1
	 * */
	public GridPoint[] points;
	/** 
	 * 2D positions of the bend points: every edge can have a maximum of 'k' bends. <br>
	 * <b>Remark</b>: an edge having '0' bend points is drawn as a straight-line segment: <br> <br>
	 * <tt>bendPoints[j]=null</tt> if the 'j'-th edge has no bends <br>
	 * <br>
	 * Remark: given the 'j'-th edge (u, v), bendPoints[j] is an array storing the bend points
	 * [b0, b1, b2, ..., b_k], where b1 is the first bend point (close to 'u') and b_k is the last bend point (close to 'v')
	 * */
	public GridPoint[][] bendPoints;

	/**
	 * Initialize the grid layout
	 * 
	 * @param graph  	the input graph (no combinatorial embedding provided)
	 * @param points 	an array storing, for each vertex, its coordinates (x, y)
	 * @param bends  	an array storing, for each edge, the list of edge points (if any)
	 * @param width  	width of the grid
	 * @param height  	height of the grid
	 * @param maxBends	maximal number of bends per edge
	 **/	
	public GridLayout(String name, AdjacencyListGraph g, GridPoint[] points, GridPoint[][] bends, int maxBends, int width, int height) {
		this.name=name;
		this.g=g;
		this.n=g.sizeVertices();
		this.e=g.sizeEdges();
		this.points=points;
		this.bendPoints=bends;
		this.maxBends=maxBends;
		this.width=width;
		this.height=height;
	}
	
	/**
	 * Check whether the current embedding of the graph do define a valid grid drawing with polylines. <br>
	 * <br>
	 * 1) the drawing should be planar (crossing-free): no pair of crossing edges
	 * 2) the vertex coordinates should be within the prescribed bounds: on the rectangular grid [0,0]x[w,h]
	 * 3) the number of bends per edge should not exceed the prescribed bound (input of the problem)
	 **/	
	public boolean isValid() {
		// TO BE COMPLETED
		System.out.println("--------\nTo be completed\n---------");
		return false;
	}
	
	/**
	 * Compute the edge-length ratio of the graph: the ratio between the largest and the smallest edge in the layout
	 **/	
	public double computeEdgeLengthRatio() {
		// TO BE COMPLETED
		System.out.println("--------\nTo be completed\n---------");
		return -1.;
	}

	/**
	 * Print at the console the 2D coordinates of the vertices of the graph (one per line)
	 **/	
    public void printCoordinates() {
    	if(this.points==null)
    		return;
    	
    	for(GridPoint p: this.points) {
    		if(p!=null)
    			System.out.println(p);
    	}
    }

}
