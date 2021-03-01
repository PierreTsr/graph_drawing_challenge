package sw;

import Jcg.geometry.Point_;
import Jcg.geometry.Point_2;
import Jcg.geometry.Point_3;
import Jcg.polyhedron.Face;
import Jcg.polyhedron.Halfedge;
import Jcg.polyhedron.Vertex;

public class SchnyderDrawing {
	public int verbosity=1;
	
	/** Schnyder wood (edge) orientation */
	public PlanarTriSchnyderWood sw=null;
	/** number of vertices */
	int n;
	
	/** store the 2d coordinates of the Schnyder drawing: 
	 *  coord2D[i][0] is the x-coordinate of the vertex 'v_i'
	 *  coord2D[i][1] is the y-coordinate of the vertex 'v_i'
	 */
	public int[][] coord2D=null;
	
	/**
	 * A table of size 8*n storing all (vertex) data involved in the computation of the Schnyder drawing <br>
	 * -) the first 3 columns, 0-2, store the height of nodes in the trees T0, T1 and T2 <br>
	 * -) columns 3, 4 and 5 store the cumulative size of the sub-trees rooted along the paths to the root s<br>
	 * -) columns 6 and 7 store the size of the subtrees rooted on T0 and T2 (red and black trees)
	 * */
	public int[] data;
	
	/** store, for each inner vertex 'v', the cumulative size of sub-trees of color black (in T2) rooted on the path P1[v] */
	public int[] dataP1T2;
	/** store, for each inner vertex 'v', the cumulative size of sub-trees of color red (in T0) rooted on the path P2[v] */
	public int[] dataP2T0;
	
	public SchnyderDrawing(PlanarTriSchnyderWood sw) {
		if(sw==null) {
			System.out.println("Error: first compute the Shnyder wood");
			return;
		}
		
		this.sw=sw;
		
		// measure memory usage (before and after memory allocation)
        Runtime runtime = Runtime.getRuntime();
        //runtime.gc();
        long usedMemory1 = runtime.totalMemory() - runtime.freeMemory();
    	long startTime=System.nanoTime(), endTime; // for evaluating time performances

		// initialize data for processing the Schnyder drawing
		this.n=sw.polyhedron.sizeOfVertices();
		this.data=new int[8*this.n];
		this.dataP1T2=new int[n];
		this.dataP2T0=new int[n];
		
		long usedMemory2 = runtime.totalMemory() - runtime.freeMemory();
		long memory=(usedMemory2-usedMemory1); // Bytes
		long memoryMB=(usedMemory2-usedMemory1)/(1024L*1024L); // MBytes
		//if(this.verbosity>=1)
		//	System.out.println("Schnyder drawing initialization...done (memory usage: "+memoryMB+" MBytes)");
	}
	
	public void init(PlanarTriSchnyderWood sw) {
		this.sw=sw;
		
		for(int i=0;i<8*this.n;i++)
			this.data[i]=0;
	}
	
	public void reset(PlanarTriSchnyderWood sw) {
		this.sw=sw;
		
		for(int i=0;i<8*this.n;i++)
			this.data[i]=0;
		for(int i=0;i<this.n;i++) {
			this.dataP1T2[i]=0;
			this.dataP2T0[i]=0;
		}
	}

	/**
	 * Perform the computation of the Schnyder drawing
	 */
	public void computeSchnyderDrawing() {
		if(sw==null) {
			System.out.println("\n\t---- Error: the Schnyder wood undefined ----");
			return;
		}
		if(this.data==null) {
			System.out.println("\n\t---- Error: the Schnyder drawing is not initialized ----");
			return;
		}

		this.computeSubtreeSizeT0T2(); // compute the subtree sizes, in a single pass for T0 and T2
		this.computeHeightT0();
		//sd.computeHeightT1Fast();
		this.computeHeightT1();
		this.computeHeightT2();
		//this.sd.minSeparatorStat();
		//System.out.println("Average defect: "+this.sw.countAverageDefect());
		//this.sd.boundaryStat();
		//System.out.println(sd.toString());
		
		this.compute2DEmbedding();
	}
	
	/**
	 * Return the total number of nodes in a given 'region', including boundary vertices (recall there are 3 disjoint regions)
	 * 
	 * @param v  the node index
	 * @param region  an index between 0, 1, 2
	 * @return  the number of nodes (inner nodes and boundary nodes) of a given 'region'
	 */
	public int getVertexArea(int v, int region) {
		if(v==this.sw.v0.index || v== this.sw.v1.index || v==this.sw.v2.index) // LCA: not necessary instruction (can be removed for benchmarks
			return 0;
		if(region==2)
			return this.getCumulativeSize(v, 2)+this.dataP1T2[v]+2-this.getSubTreeSize(v, 2);
		else if(region==0)
			return this.getCumulativeSize(v, 0)+this.dataP2T0[v]+2-this.getSubTreeSize(v, 0);
		else {
			int result=n-(this.getVertexArea(v, 0)+this.getVertexArea(v, 2)-(this.getNodeHeight(v, 1)+1));
			result=result+this.getBoundarySize(v, 1); // LCA: it can be improved here (for runtime performance)
			return result;
		}
	}

	/**
	 * Return the number of inner nodes in a given 'region' (recall there are 3 disjoint regions)
	 * 
	 * @param v  the node index
	 * @param region  an index between 0, 1, 2
	 * @return  the number of nodes (inner nodes and boundary nodes) of a given 'region'
	 */
	public int getInnerVertexArea(int v, int region) {
		if(v==this.sw.v0.index || v== this.sw.v1.index || v==this.sw.v2.index) // LCA: not necessary instruction (can be removed for benchmarks
			return 0;
		if(region==2)
			return (this.getCumulativeSize(v, 2)+this.dataP1T2[v]+2-this.getSubTreeSize(v, 2))-this.getBoundarySize(v, 2);
		else if(region==0)
			return (this.getCumulativeSize(v, 0)+this.dataP2T0[v]+2-this.getSubTreeSize(v, 0))-this.getBoundarySize(v, 0);
		else {
			int result=n-(this.getVertexArea(v, 0)+this.getVertexArea(v, 2)-(this.getNodeHeight(v, 1)+1));
			result=result+this.getBoundarySize(v, 1); // LCA: it can be improved here (for runtime performance)
			return result-this.getBoundarySize(v, 1);
		}
	}

	/**
	 * Return the number of boundary nodes of a given 'region' (recall there are 3 disjoint regions)
	 * 
	 * @param v  the node index
	 * @param region  an index between 0, 1, 2
	 * @return  the number of boundary nodes of a given 'region'
	 */
	public int getBoundarySize(int v, int region) {
			return this.getNodeHeight(v, (region+1)%3)+this.getNodeHeight(v, (region+2)%3)+1;
	}

	/**
	 * Return the total number of faces in a given 'region' (recall there are 3 disjoint regions)
	 * 
	 * @param v  the node index
	 * @param region  an index between 0, 1, 2
	 * @return  the number of faces inside a given 'region'
	 */
	public int getFaceArea(int v, int region) {
		return 2*this.getVertexArea(v, region)-(this.getBoundarySize(v, region)+2);
	}

	/**
	 * Store the size of the subtree of 'T_j' rooted at vertex 'v_i'
	 * 
	 * @param i  index of the vertex 'v_i'
	 * @param color  index of the tree T_j (j = 0, 1, 2)
	 * @param size  size of the subtree of a given tree T_j rooted at vertex 'v_i'
	 */
	public void setSubTreeSize(int i, int color, int size) {
			this.data[i*8+3+color]=size;
	}

	/**
	 * Return the size of the subtree of T_j rooted at vertex 'v_i'
	 * 
	 * @param i index of the vertex 'v_i'
	 * @param color  index of the tree T_j (j = 0, 1, 2)
	 */
	public int getSubTreeSize(int i,int color) {
			return this.data[i*8+3+color];
	}

	/**
	 * Store the height of a vertex 'v' in the tree 'T_j'
	 * 
	 * @param i  index of the vertex 'v_i'
	 * @param color  index of the tree T_j (j = 0, 1, 2)
	 * @param height  height of the subtree of a given tree T_j rooted at vertex 'v_i'
	 */
	public void setNodeHeight(int i, int color, int height) {
			this.data[i*8+color]=height;
	}

	/**
	 * Return the height of a given node 'v' in the tree T_j
	 * 
	 * @param i index of the vertex 'v_i'
	 * @param color  index of the tree T_j (j = 0, 1, 2)
	 */
	public int getNodeHeight(int i,int color) {
			return this.data[i*8+color];
	}
	
	/**
	 * Store the cumulative size of all sub-trees (of a given 'color') rooted at the vertices along a path to the root
	 * 
	 * Warning: sub-tree size are not stored for the tree T1
	 * 
	 * @param i  index of the vertex 'v_i'
	 * @param color  color of the sub-trees (0, 1, 2)
	 * @param size  size of the subtree of a given tree T_j rooted at vertex 'v_i'
	 */
	public void setCumulativeSize(int i, int color, int size) {
		if(color==0)
			this.data[i*8+6]=size;
		else if(color==2)
			this.data[i*8+7]=size;
		else
			throw new Error("Error: cumulative sub-tree sizes are not stored for the tree T1");
	}

	/**
	 * Return the cumulative size
	 * 
	 * Warning: sub-tree size are not stored for the tree T1
	 * 
	 * @param i index of the vertex 'v_i'
	 * @param color  color of the subtrees (0, 1, 2)
	 */
	public int getCumulativeSize(int i,int color) {
		if(color==0)
			return this.data[i*8+6];
		else if(color==2)
			return this.data[i*8+7];
		else
			throw new Error("Error: cumulative sub-tree sizes are not stored for the tree T1");
	}

	/**
	 * Perform a DFS visit of the tree T0 and, for each inner vertex 'v', compute <br>
	 * -) the size of the sub-tree of color 0 (red) rooted at 'v' <br>
	 * -) the size of the sub-tree of color 2 (black) rooted at 'v' <br>
	 * 
	 * It performs the traversal of the contour of the tree, in ccw order.
	 * The traversal starts from the half-edge (u, v0) and ends at (v0, v2).
	 * (u, v0) is the half-edge oriented toward v0 corresponding to
	 * the first incoming edge incident to v0 (thus of color 0)
	 */
	public void computeSubtreeSizeT0T2() {
		if(this.verbosity>=1)
			System.out.print("Computing the size of subtrees in T0...");
		long startTime=System.nanoTime(), endTime; // for evaluating time performances
		
		for(int i=0;i<n;i++) {
			this.setSubTreeSize(i, 0, 1); // all the sub-trees in T0 have size 1 at the beginning
			this.setSubTreeSize(i, 2, 1); // all the sub-trees in T2 have size 1 at the beginning
		}
		this.setSubTreeSize(this.sw.v1.index, 0, 0); // v1 does not belong to T0
		this.setSubTreeSize(this.sw.v2.index, 0, 0); // v2 does not belong to T0
		this.setSubTreeSize(this.sw.v0.index, 2, 0); // v0 does not belong to T2
		this.setSubTreeSize(this.sw.v1.index, 2, 0); // v1 does not belong to T2

		//String treeCode=""; // add first edge (v1, v0)
		//String incomingDegrees=""; // vertex v1 has no incident incoming edges of color 2
		Halfedge<Point_> firstEdge=this.sw.rootEdge.getPrev(); // starting half-edge, of color 0, oriented toward v_0
		Halfedge<Point_> edge20=this.sw.rootEdge.getOpposite().getNext().getOpposite(); // halfedge v20, oriented toward v_0
		Halfedge<Point_> lastEdge=edge20.getOpposite();
		Halfedge<Point_> pEdge=firstEdge; 
		
		int node, size, descendant, ancestor;
    	while(pEdge!=lastEdge) { // perform the traversal of the contour of T_0
    		if(this.sw.edgeColor[pEdge.index]==0 && this.sw.isWellOriented[pEdge.index]==true) { // an edge of 'T' visited the first time
    			//if(pEdge!=edge20) { // LCA: not useful, to be removed
    				//treeCode=treeCode+"("; // do not write '(' for the edge (v2, v0)
    			//}
    			//if(pEdge!=firstEdge) {
    				//incomingDegrees=incomingDegrees+'['; // do not write '[' for the first edge oriented toward v0
    			//}
    			pEdge=pEdge.getPrev();
    		}
    		else if(this.sw.edgeColor[pEdge.index]==2 && this.sw.isWellOriented[pEdge.index]==true) { // ']', a black incoming edge
    			//incomingDegrees=incomingDegrees+']';
    			node=pEdge.getVertex().index; // destination node in T2
    			descendant=pEdge.getOpposite().getVertex().index; // origin of the black edge in T2
    			size=this.getSubTreeSize(descendant, 2); // size of the subtree in T2 (rooted at the descendant)
    			
    			this.setSubTreeSize(node, 2, this.getSubTreeSize(node, 2)+size); // increase the size, adding the size of a subtree
    			
    			pEdge=pEdge.getOpposite().getPrev();
    		}
    		else if(this.sw.edgeColor[pEdge.index]==1 && this.sw.isWellOriented[pEdge.index]==false) { // outgoing blue edge
    			pEdge=pEdge.getOpposite().getPrev();
    		}
    		else if(this.sw.edgeColor[pEdge.index]==2 && this.sw.isWellOriented[pEdge.index]==false) { // black outgoing edge
    			pEdge=pEdge.getOpposite().getPrev();
    		}
    		else if(this.sw.edgeColor[pEdge.index]==1 && this.sw.isWellOriented[pEdge.index]==true) { // a blue incoming edge
    			pEdge=pEdge.getOpposite().getPrev();
    		}
    		else if(this.sw.edgeColor[pEdge.index]==0 && this.sw.isWellOriented[pEdge.index]==false) { // an edge of T_0 encountered the second time
    			//treeCode=treeCode+")";
    			node=pEdge.getVertex().index;
    			size=this.getSubTreeSize(node, 0);
    			ancestor=pEdge.getOpposite().getVertex().index; // ancestor node in the tree T0
    			
    			this.setSubTreeSize(ancestor, 0, this.getSubTreeSize(ancestor, 0)+size); // increment the size of the subtree rooted at the ancestor node
    			
    			pEdge=pEdge.getPrev();
    		}
    		//else
    		//	throw new Error("Error: wrong edge orientation/coloration");
    	}
    	// we "must" set the subtree size at 'v2'
		this.setSubTreeSize(this.sw.v2.index, 2, n-2); // v2 has a sub-tree of size 'n-2'

    	//treeCode=treeCode+""; // add last closing parenthesis, corresponding to edge (v2, v0)
    	
    	endTime=System.nanoTime();
        double duration=(double)(endTime-startTime)/1000000000.;
        if(this.verbosity>1)
    		System.out.println("done ("+duration+" seconds)");
        else if(this.verbosity==1)
    		System.out.println("done");
	}

	/**
	 * Perform a DFS visit of the tree T0: for each inner vertex 'v' compute and store in a single pass <br>
	 * -) the height of node 'v' in the tree T_0 <br>
	 * -) the cumulative size of all subtrees in 'T_2' rooted at vertex 'v' (along the path from 'v' to 'v0') <br>
	 * 
	 * <br>
	 * It performs the traversal of the contour of the tree, in ccw order.
	 * The traversal starts from the half-edge (u, v0) and ends at (v0, v2).
	 * (u, v0) is the half-edge oriented toward v0 corresponding to
	 * the first incoming edge incident to v0 (thus of color 0)
	 */
	public void computeHeightT0() {
		if(this.verbosity>=1)
			System.out.print("Computing the height of nodes in the tree T0...");
		long startTime=System.nanoTime(), endTime; // for evaluating time performances
		
		Halfedge<Point_> firstEdge=this.sw.rootEdge.getPrev(); // starting half-edge, of color 0, oriented toward v_0
		Halfedge<Point_> edge20=this.sw.rootEdge.getOpposite().getNext().getOpposite(); // halfedge v20, oriented toward v_0
		Halfedge<Point_> lastEdge=edge20.getOpposite();
		Halfedge<Point_> pEdge=firstEdge; 
		
		int height=0;
    	while(pEdge!=lastEdge) { // perform the traversal of the contour of T_0
    		if(this.sw.edgeColor[pEdge.index]==0 && this.sw.isWellOriented[pEdge.index]==true) { // an edge of 'T' visited the first time
    			if(pEdge!=edge20) {
    				//treeCode=treeCode+"("; // do not write '(' for the edge (v2, v0)
    				height++;
    				
    				int newNode=pEdge.getOpposite().getVertex().index; // index of the new node encountered during the DFS
    				int ancestor=pEdge.getVertex().index; // index of its ancestor node
    				
    				//System.out.println(newNode+", "+ancestor+", height "+height);
    				//System.out.println("height before update: "+this.getNodeHeight(newNode, 0));
    				
    				this.setNodeHeight(newNode, 0, height); // store the height of the new (descendant) node in 'T0'
    				//System.out.println("height after update: "+this.getNodeHeight(newNode, 0));
    				
    				// compute the cumulative size of sub-trees of color '2' (black) rooted on the 'red' path (from the node to the root of 'T0')
    				int cumulative=this.getCumulativeSize(ancestor, 2);
    				this.setCumulativeSize(newNode, 2, cumulative+this.getSubTreeSize(newNode, 2));
    			}
    			if(pEdge!=firstEdge) { // '['
    				//incomingDegrees=incomingDegrees+'['; // do not write '[' for the first edge oriented toward v0
    			}
    			pEdge=pEdge.getPrev();
    		}
    		else if(this.sw.edgeColor[pEdge.index]==2 && this.sw.isWellOriented[pEdge.index]==true) { // a black incoming edge ']'
    			//incomingDegrees=incomingDegrees+']';
    			pEdge=pEdge.getOpposite().getPrev();
    		}
    		else if(this.sw.edgeColor[pEdge.index]==1 && this.sw.isWellOriented[pEdge.index]==false) { // outgoing blue edge
    			pEdge=pEdge.getOpposite().getPrev();
    		}
    		else if(this.sw.edgeColor[pEdge.index]==2 && this.sw.isWellOriented[pEdge.index]==false) { // black outgoing edge
    			pEdge=pEdge.getOpposite().getPrev();
    		}
    		else if(this.sw.edgeColor[pEdge.index]==1 && this.sw.isWellOriented[pEdge.index]==true) { // a blue incoming edge
    			pEdge=pEdge.getOpposite().getPrev();
    		}
    		else if(this.sw.edgeColor[pEdge.index]==0 && this.sw.isWellOriented[pEdge.index]==false) { // ')', an edge of T_0 encountered the second time
    			//treeCode=treeCode+")";
    			height--; 
    			
    			pEdge=pEdge.getPrev();
    		}
    	}
    	
    	endTime=System.nanoTime();
        double duration=(double)(endTime-startTime)/1000000000.;
        if(this.verbosity>1)
    		System.out.println("done ("+duration+" seconds)");
        else if(this.verbosity==1)
    		System.out.println("done");
	}

	/**
	 * Perform a DFS visit of the tree T1: for each inner vertex 'v' compute and store in a single pass <br>
	 * -) the height of node 'v' in the tree T_1 <br>
	 * -) the cumulative size of all subtrees in 'T_2' rooted at vertex 'v' (along the path from 'v' to 'v0') <br>
	 * 
	 * <br>
	 * It performs the traversal of the contour of the tree, in ccw order.
	 * The traversal starts from the half-edge (v2, v1) and ends at (v0, v1).
	 * (v2, v1) is the half-edge oriented toward v1 corresponding to
	 * the first incoming inner edge incident to v1 (thus of color 1)
	 */
	public void computeHeightT1() {
		if(this.verbosity>=1)
			System.out.print("Computing the height of nodes in the tree T1...");
		long startTime=System.nanoTime(), endTime; // for evaluating time performances
		
		Halfedge<Point_> firstEdge=this.sw.rootEdge.getOpposite().getPrev().getOpposite().getPrev(); // starting half-edge: inner halfedge of color 1, oriented toward v_1
		Halfedge<Point_> edge01=this.sw.rootEdge; // halfedge v01, oriented toward v_1
		Halfedge<Point_> lastEdge=edge01;
		Halfedge<Point_> pEdge=firstEdge; 
		
		//System.out.println("First incoming blue edge (toward at v1): "+firstEdge.getOpposite().getVertex().index+", "+firstEdge.getVertex().index);
		
		int height=0;
		//int count=0;
    	while(pEdge!=lastEdge) { // perform the traversal of the contour of T_1
    		//count++;
    		//System.out.println("pEdge "+count+": "+pEdge.getOpposite().getVertex().index+", "+pEdge.getVertex().index);

    		if(this.sw.edgeColor[pEdge.index]==1 && this.sw.isWellOriented[pEdge.index]==true) { // an edge of 'T1' visited the first time
    			if(pEdge!=edge01) {
    				height++;
    				
    				int newNode=pEdge.getOpposite().getVertex().index; // index of the new node encountered during the DFS
    				int ancestor=pEdge.getVertex().index; // index of its ancestor node
    				
    				this.setNodeHeight(newNode, 1, height); // store the height of the new (descendant) node in 'T1'
    				
    				// compute the cumulative size of sub-trees of color '0' rooted on the 'blue' path (from the node to the root of 'T1')
    				int cumulative=this.getCumulativeSize(ancestor, 0);
    				this.setCumulativeSize(newNode, 0, cumulative+this.getSubTreeSize(newNode, 0));

    				// compute the cumulative size of sub-trees of color '2' rooted on the 'blue' path (from the node to the root of 'T1')
    				//cumulative=this.getCumulativeSize(ancestor, 2);
    				//this.setCumulativeSize(newNode, 2, cumulative+this.getSubTreeSize(newNode, 2));
    				cumulative=this.dataP1T2[ancestor];
    				this.dataP1T2[newNode]=cumulative+this.getSubTreeSize(newNode, 2);
    			}
    			pEdge=pEdge.getPrev();
    		}
    		else if(this.sw.edgeColor[pEdge.index]==0 && this.sw.isWellOriented[pEdge.index]==true) { // a black incoming edge ']'
    			//incomingDegrees=incomingDegrees+']';
    			pEdge=pEdge.getOpposite().getPrev();
    		}
    		else if(this.sw.edgeColor[pEdge.index]==2 && this.sw.isWellOriented[pEdge.index]==false) { // outgoing blue edge
    			pEdge=pEdge.getOpposite().getPrev();
    		}
    		else if(this.sw.edgeColor[pEdge.index]==0 && this.sw.isWellOriented[pEdge.index]==false) { // black outgoing edge
    			pEdge=pEdge.getOpposite().getPrev();
    		}
    		else if(this.sw.edgeColor[pEdge.index]==2 && this.sw.isWellOriented[pEdge.index]==true) { // a blue incoming edge
    			pEdge=pEdge.getOpposite().getPrev();
    		}
    		else if(this.sw.edgeColor[pEdge.index]==1 && this.sw.isWellOriented[pEdge.index]==false) { // ')', an edge of T_0 encountered the second time
    			//treeCode=treeCode+")";
    			height--; 
    			
    			pEdge=pEdge.getPrev();
    		}
    	}
    	
    	endTime=System.nanoTime();
        double duration=(double)(endTime-startTime)/1000000000.;
        if(this.verbosity>1)
    		System.out.println("done ("+duration+" seconds)");
        else if(this.verbosity==1)
    		System.out.println("done");
	}

	/**
	 * Perform a DFS visit of the tree T2: for each inner vertex 'v' compute and store in a single pass <br>
	 * -) the height of node 'v' in the tree T_2 <br>
	 * -) the cumulative size of all subtrees in 'T_0' rooted at vertex 'v' (along the path from 'v' to 'v2') <br>
	 * 
	 * <br>
	 * It performs the traversal of the contour of the tree, in ccw order. <br>
	 * <br>
	 * Remark: 
	 * the first incoming inner edge incident to v2 (thus of color 2)
	 */
	public void computeHeightT2() {
		if(this.verbosity>=1)
			System.out.print("Computing the height of nodes in the tree T2...");
		long startTime=System.nanoTime(), endTime; // for evaluating time performances
		
		Halfedge<Point_> firstEdge=this.sw.rootEdge.getOpposite().getNext().getOpposite().getPrev(); // starting half-edge: inner halfedge of color 2, oriented toward v_2
		Halfedge<Point_> edge12=this.sw.rootEdge.getOpposite().getPrev().getPrev(); // halfedge v12, oriented toward v_2
		Halfedge<Point_> lastEdge=edge12;
		Halfedge<Point_> pEdge=firstEdge; 
		
		//System.out.println("First incoming blue edge (toward at v1): "+firstEdge.getOpposite().getVertex().index+", "+firstEdge.getVertex().index);
		
		int height=0;
		int cumulative;
		//int count=0;
    	while(pEdge!=lastEdge) { // perform the traversal of the contour of T_1
    		//count++;
    		//System.out.println("pEdge "+count+": "+pEdge.getOpposite().getVertex().index+", "+pEdge.getVertex().index);

    		if(this.sw.edgeColor[pEdge.index]==2 && this.sw.isWellOriented[pEdge.index]==true) { // an edge of 'T2' visited the first time
    			if(pEdge!=edge12) {
    				height++;
    				
    				int newNode=pEdge.getOpposite().getVertex().index; // index of the new node encountered during the DFS
    				int ancestor=pEdge.getVertex().index; // index of its ancestor node
    				
    				this.setNodeHeight(newNode, 2, height); // store the height of the new (descendant) node in 'T2'
    				
    				// compute the cumulative size of sub-trees of color '0' rooted on the 'black' path (from the node to the root of 'T2')
    				//cumulative=this.getCumulativeSize(ancestor, 0);
    				//this.setCumulativeSize(newNode, 0, cumulative+this.getSubTreeSize(newNode, 0));
    				cumulative=this.dataP2T0[ancestor];
    				this.dataP2T0[newNode]=cumulative+this.getSubTreeSize(newNode, 0);
    			}
    			pEdge=pEdge.getPrev();
    		}
    		else if(this.sw.edgeColor[pEdge.index]==1 && this.sw.isWellOriented[pEdge.index]==true) { // a black incoming edge ']'
    			//incomingDegrees=incomingDegrees+']';
    			pEdge=pEdge.getOpposite().getPrev();
    		}
    		else if(this.sw.edgeColor[pEdge.index]==0 && this.sw.isWellOriented[pEdge.index]==false) { // outgoing blue edge
    			pEdge=pEdge.getOpposite().getPrev();
    		}
    		else if(this.sw.edgeColor[pEdge.index]==1 && this.sw.isWellOriented[pEdge.index]==false) { // black outgoing edge
    			pEdge=pEdge.getOpposite().getPrev();
    		}
    		else if(this.sw.edgeColor[pEdge.index]==0 && this.sw.isWellOriented[pEdge.index]==true) { // a blue incoming edge
    			pEdge=pEdge.getOpposite().getPrev();
    		}
    		else if(this.sw.edgeColor[pEdge.index]==2 && this.sw.isWellOriented[pEdge.index]==false) { // ')', an edge of T_0 encountered the second time
    			//treeCode=treeCode+")";
    			height--; 
    			
    			pEdge=pEdge.getPrev();
    		}
    	}
    	
    	endTime=System.nanoTime();
        double duration=(double)(endTime-startTime)/1000000000.;
        if(this.verbosity>1)
    		System.out.println("done ("+duration+" seconds)");
        else if(this.verbosity==1)
    		System.out.println("done");
	}

	/**
	 * Perform a DFS visit of the tree T0: for each inner vertex 'v' compute
	 * the length of the path to the root v0, and the size of the sub-tree of color 0 rooted at 'v'
	 * 
	 * It performs the traversal of the contour of the tree, in ccw order.
	 * The traversal starts from the half-edge (u, v0) and ends at (v0, v2).
	 * (u, v0) is the half-edge oriented toward v0 corresponding to
	 * the first incoming edge incident to v0 (thus of color 0)
	 */
	public void visitTree0() {
		if(this.verbosity>1)
			System.out.print("Computing the path length for every vertex...");
		long startTime=System.nanoTime(), endTime; // for evaluating time performances
		
		//String treeCode=""; // add first edge (v1, v0)
		//String incomingDegrees=""; // vertex v1 has no incident incoming edges of color 2
		Halfedge<Point_> firstEdge=this.sw.rootEdge.getPrev(); // starting half-edge, of color 0, oriented toward v_0
		Halfedge<Point_> edge20=this.sw.rootEdge.getOpposite().getNext().getOpposite(); // halfedge v20, oriented toward v_0
		Halfedge<Point_> lastEdge=edge20.getOpposite();
		Halfedge<Point_> pEdge=firstEdge; 
		
		int height=0;
    	while(pEdge!=lastEdge) { // perform the traversal of the contour of T_0
    		if(this.sw.edgeColor[pEdge.index]==0 && this.sw.isWellOriented[pEdge.index]==true) { // an edge of T_0 visited the first time
    			if(pEdge!=edge20) {
    				//treeCode=treeCode+"("; // do not write '(' for the edge (v2, v0)
    				height++;
    			}
    			if(pEdge!=firstEdge) {
    				//incomingDegrees=incomingDegrees+'['; // do not write '[' for the first edge oriented toward v0
    				height=height;
    			}
    			pEdge=pEdge.getPrev();
    		}
    		else if(this.sw.edgeColor[pEdge.index]==2 && this.sw.isWellOriented[pEdge.index]==true) { // a black incoming edge
    			//incomingDegrees=incomingDegrees+']';
    			pEdge=pEdge.getOpposite().getPrev();
    		}
    		else if(this.sw.edgeColor[pEdge.index]==1 && this.sw.isWellOriented[pEdge.index]==false) { // outoing blue edge
    			pEdge=pEdge.getOpposite().getPrev();
    		}
    		else if(this.sw.edgeColor[pEdge.index]==2 && this.sw.isWellOriented[pEdge.index]==false) { // black outoing edge
    			pEdge=pEdge.getOpposite().getPrev();
    		}
    		else if(this.sw.edgeColor[pEdge.index]==1 && this.sw.isWellOriented[pEdge.index]==true) { // a blue incoming edge
    			pEdge=pEdge.getOpposite().getPrev();
    		}
    		else if(this.sw.edgeColor[pEdge.index]==0 && this.sw.isWellOriented[pEdge.index]==false) { // an edge of T_0 encountered the second time
    			//treeCode=treeCode+")";
    			pEdge=pEdge.getPrev(); // move to the next edge, according to the DFS traversal
    		}
    		else
    			throw new Error("Error: wrong edge orientation/coloration");
    	}
    	//treeCode=treeCode+""; // add last closing parenthesis, corresponding to edge (v2, v0)
    	
    	endTime=System.nanoTime();
        double duration=(double)(endTime-startTime)/1000000000.;
    	if(this.verbosity>1)
    		System.out.println(" ("+duration+" seconds)");
    	else if(this.verbosity>0)
    		System.out.println("done");
    	String[] result=new String[2];
    	//result[0]=treeCode;
    	//result[1]=incomingDegrees;

	}
	
	/**
	 * Compute and return the integer coordinates of the vertices in the 2D Schnyder embedding
	 */
	public int[][] compute2DEmbedding() {
		int[][] coord2D=new int[this.n][2];
		
		int f=2*this.n-5; // f=2n-5
		
		for(Vertex v: this.sw.polyhedron.vertices) {
			int x, y;

			if(v==this.sw.v0) {
				x=0; y=0;
			}
			else if(v==this.sw.v1) {
				x=f; y=0;
			}
			else if(v==this.sw.v2) {
				x=0; y=f;
			}
			else {
				int b0, b1, b2;

				// compute the 3 barycentric coordinates
				b0=this.getFaceArea(v.index, 0);
				b1=this.getFaceArea(v.index, 1);
				b2=this.getFaceArea(v.index, 2);
				
				/*System.out.print("v"+v.index+": "+p0+", "+p1+", "+p2+" - ");
				System.out.print("\t"+v0+", "+v1+", "+v2+"");*/
				//System.out.print("v"+v.index+":\t"+b0+", "+b1+", "+b2);
				//p=new Point_2(x0/f, x1/f);
				
				x=0*b0+1*b1+0*b2;
				y=0*b0+0*b1+1*b2;
			}
			//System.out.println("\tv"+v.index+":\t"+p);
			coord2D[v.index][0]=x;
			coord2D[v.index][1]=y;
			//System.out.println("v"+v.index+": "+v.getPoint());
		}
		return coord2D;
	}
	
	/**
	 * Compute the maximum length of the edges
	 */
	/*public double computeEdgeLengthMax(){
		if(this.coord2D==null)
			return -1.;
		
		double max=0.0;

		for(Halfedge e: this.sw.polyhedron.halfedges){ // edges are counted only once
			if(e.getVertex().index>e.getOpposite().getVertex().index) {
				Point_2 p=this.coord2D[e.getVertex().index];
				Point_2 q=this.coord2D[e.getOpposite().getVertex().index];

				max=Math.max(max, p.distanceFrom(q).doubleValue());
			}
		}
		return max;
	}*/

}
