

import Jcg.geometry.*;
import Jcg.mesh.*;
import Jcg.viewer.old.Fenetre;
import Jcg.viewer.processing3d.MeshViewerProcessing;
import sw.BalancedSchnyderWood;
import sw.PlanarTriSchnyderWood;
import sw.SchnyderDrawing;
import Jcg.io.options.*;
import Jcg.polyhedron.*;

/**
 * A class for testing the Half-edge data structure representing polyhedral surfaces.
 * 3D rendering is performed using a Mesh Viewer based on Processing (version 1.51) 
 *
 * @author Luca Castelli Aleardi (INF562, 2019)
 *
 */
public class TestSchnyderDrawing {
	
	/**
	 * Testing the computation of Schnyder drawings
	 */    
	public static void testSW(String filename) {   	
		Polyhedron_3 mesh=MeshLoader.getSurfaceMesh(filename);
		mesh.isValid(false);
		
		PlanarTriSchnyderWood sw=null;
		SchnyderDrawing sd=null;
		Halfedge root=(Halfedge)mesh.halfedges.get(0);
		sw=new BalancedSchnyderWood(mesh, root);
		sw.performTraversal(); // run the computation of the Schnyder wood (vertex shelling traversal)
		
		sd=new SchnyderDrawing(sw);
		sd.computeSchnyderDrawing(); // run the computation of the Schnyder drawing algorithm
		int[][] coord2D=sd.compute2DEmbedding();
		for(int i=0; i<coord2D.length; i++)
			System.out.println("("+coord2D[i][0]+", "+coord2D[i][0]+")");
	}

    public static void main (String[] args) {
		System.out.println("Testing Schnyder drawing for planar triangulations");
    	if (args.length == 0) {
    		System.out.println("I wait for a mesh stored in OFF format");
    		System.out.println("Usage : java TestSchnyderDrawing filename");
    		return;
    	}

    	String filename=args[0];
    	testSW(filename);
    }

}
