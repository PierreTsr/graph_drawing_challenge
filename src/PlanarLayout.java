import Jcg.geometry.Point_2;
import Jcg.geometry.Point_3;
import Jcg.polyhedron.Polyhedron_3;
import jdg.graph.AdjacencyListGraph;

/**
 * Planar Layout of a graph
 * 
 * @author Luca Castelli Aleardi (Ecole Polytechnique)
 * @version feb 2021
 */
public class PlanarLayout {
	/** number of vertices of the graph */
	public int n;

	/** Input graph to draw: no combinatorial embedding provided (only the 1-skeleton of the graph) */	
	public AdjacencyListGraph g;

	/** input mesh to draw: a graph provided with its combinatorial embedding */
	public Polyhedron_3<Point_3> mesh;

	/** 2D positions of the vertices of the graph defining the planar layout. <br>
	 * Remark: vertices are indexed from 0..n-1
	 * */
	public Point_2[] points;

	/**
	 * Initialize the drawing: by the default all vertices are placed at the origin (0.0, 0.0)
	 * 
	 * @param mesh  the input graph+its combinatorial embedding
	 **/	
	public PlanarLayout(Polyhedron_3<Point_3> mesh) {
		this.mesh=mesh;
		this.g=null;
		this.n=this.mesh.sizeOfVertices();
		this.points=new Point_2[n];
		
		for(int i=0;i<this.n;i++) {
			this.points[i]=new Point_2(0.0, 0.0);
		}
	}
	/**
	 * Initialize the drawing: by the default all vertices are placed at the origin (0.0, 0.0)
	 * 
	 * @param graph  the input graph (no combinatorial embedding provided)
	 **/	
	public PlanarLayout(AdjacencyListGraph g) {
		this.mesh=null; // the faces of the graph are not defined
		this.g=g;
		this.n=this.mesh.sizeOfVertices();
		this.points=new Point_2[n];
		
		for(int i=0;i<this.n;i++) {
			this.points[i]=new Point_2(0.0, 0.0);
		}
	}

    public void printCoordinates() {
    	if(this.points==null)
    		return;
    	
    	for(Point_2 p: this.points) {
    		if(p!=null)
    			System.out.println(p);
    	}
    }

}
