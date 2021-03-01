package sw;

import Jcg.polyhedron.*;

import java.util.LinkedList;
import java.util.List;

import Jcg.geometry.Point_;
import Jcg.util.*;

/**
 * @author Luca Castelli Aleardi (2017, Ecole Polytechnique)
 * 
 * This class allows to compute a Schnyder wood of a planar triangulation.
 * It implements a "cut border": a simple cycle of edges defining a
 * topological disk, and provides basic operations such as vertex removal
 * (the vertex to be removed must belong to the cut border)
 * It works for planar triangle meshes without boundaries.
 * Faces are assumed to be ccw oriented (only for combinatorics)
 */
public class PlanarTriSchnyderWood extends EdgeOrientation {
	public int verbosity=0;
	
    // definition of the root face and root vertices v0, v1, v2
    /** half-edge (v_0, v_1) oriented toward v_1, assuming the ccw orientation of faces */
    public Halfedge<Point_> rootEdge;
    public Vertex<Point_> v0, v1, v2; // vertices on the root face (the outer face, in the plane)

    // auxiliary information needed for computing a Schnyder wood
    protected DLinkedList<Halfedge<Point_>> outerCycle; // edges defining the outer cycle of the planar map   
    protected boolean[] isChord; // for half-edges, says whether is a chord
    protected boolean[] isOnCutBorder; // say whether a vertex is lying on the cut-border

    /**
     * Initialize the edge coloring/orientation
     */
    public PlanarTriSchnyderWood(int e) {
    	//System.out.print("Initialize Schnyder wood computation");   	
    	this.edgeColor=new byte[e];
    	this.isWellOriented=new boolean[e];
    	
    	for(int i=0;i<edgeColor.length;i++)
    		this.edgeColor[i]=-1;
   }

    /**
     * Construct the cut-border starting from the root edge (v0, v1)
     * At the beginning the cut-border contains edges (v2, v0) and (v1, v2)
     * Edges are ccw oriented around faces
     */
    public PlanarTriSchnyderWood(Polyhedron_3<Point_> polyhedron, Halfedge<Point_> rootEdge) {
    	System.out.print("Initialize Schnyder wood computation: ");
    	if(polyhedron==null)
    		throw new Error("error: null polyhedron");
    	if(rootEdge==null)
    		throw new Error("error: root edge null");
    	if(polyhedron.genus()>0)
    		throw new Error("error: non planar mesh");
    	
    	int i=0;
    	for(i=0;i<polyhedron.sizeOfVertices();i++) {
    		Vertex<Point_> v=(Vertex<Point_>)polyhedron.vertices.get(i);
    		v.index=i;
    	}
    	
    	i=0;
    	for(Object o: polyhedron.halfedges) {
    		Halfedge<Point_> e=(Halfedge<Point_>)o;
    		e.index=i;
    		i++;
    	}
    	
    	this.polyhedron=polyhedron;
    	this.rootEdge=rootEdge;    	
        this.v0=this.rootEdge.getOpposite().getVertex();
        this.v1=this.rootEdge.getVertex();
    	
    	this.outerCycle=new DLinkedList<Halfedge<Point_>>();

    	this.edgeColor=new byte[this.polyhedron.sizeOfHalfedges()];
    	for(i=0;i<edgeColor.length;i++)
    		this.edgeColor[i]=-1;
    	
    	this.isChord=new boolean[this.polyhedron.sizeOfHalfedges()];
    	this.isWellOriented=new boolean[this.polyhedron.sizeOfHalfedges()];
    	this.isOnCutBorder=new boolean[this.polyhedron.sizeOfVertices()];
    	
    	Halfedge<Point_> edge10=this.rootEdge.getOpposite();
    	Halfedge<Point_> edge02=edge10.getNext();
    	Halfedge<Point_> edge21=edge02.getNext();
    	
    	this.v2=edge02.getVertex();
    	
    	// setting information concerning the root-edge
    	this.outerCycle.add(edge02.getOpposite());	
    	this.outerCycle.add(edge21.getOpposite());
    	this.isOnCutBorder[v1.index]=true;
    	this.isOnCutBorder[v0.index]=true; // LCA newly added
    	this.isOnCutBorder[v2.index]=true; // LCA newly added
    	
    	// set the orientation of the root edge
    	this.isWellOriented[rootEdge.index]=false;
    	this.isWellOriented[rootEdge.getOpposite().index]=true;
    	// set the color of the root edge
    	this.edgeColor[rootEdge.index]=0;
    	this.edgeColor[rootEdge.getOpposite().index]=0;
    	System.out.print("\t root face (v"+v0.index+", v"+v1.index+", v"+v2.index+")");
    	System.out.println("\t root edge e"+rootEdge.index+" (v"+this.rootEdge.getOpposite().getVertex().index+", v"+this.rootEdge.getVertex().index+")");
   }

    /**
     * Make a copy of the Schnyder Wood
     */
    public PlanarTriSchnyderWood getCopy() {
    	PlanarTriSchnyderWood result=new PlanarTriSchnyderWood(this.polyhedron, this.rootEdge);
    	
    	int i;
    	for(i=0;i<result.edgeColor.length;i++) {
    		result.edgeColor[i]=this.edgeColor[i];
    		result.isWellOriented[i]=this.isWellOriented[i];
    	}
    	
    	return result;
    }

    /**
     * Reset edge colors
     */
    public void resetEdgeColors() {
    	for(int i=0;i<edgeColor.length;i++)
    		this.edgeColor[i]=-1;
   }
    
    /**
     * It removes a vertex from the cut-border (vertex conquest)
     * It updates the cut-border, assigning color and orientation to edges
     * 
     * @param node The node in the list storing the given half-edge (whose vertex will be removed)
     * @return result return the list node containing the preceding halfedge (on the boundary cycle)
     */
    public DListNode<Halfedge<Point_>> vertexRemoval(DListNode<Halfedge<Point_>> node) {
    	if(node==null || this.outerCycle.isEmpty()) {
    		System.out.println("no more vertex to remove ");
    		return null;
    	}
    	
    	Halfedge<Point_> rightEdge=node.getElement(); // the vertex v of rightEdge will be removed
    	if(rightEdge==null) {
    		throw new Error("null reference: rightEdge");
    	}
    	if(rightEdge.getVertex()==this.v0) // vertex v0 cannot be removed
    		return node.getNext();
    	
    	Halfedge<Point_> leftEdge=node.getPrev().getElement();
    	if(leftEdge==null) {
    		throw new Error("null reference: leftEdge");
    	}
    	
    	// the vertex is incident to a triangle
    	/*if(rightEdge.getNext()==leftEdge) {
    		//count++;
    		return triangleRemoval(node);
    	}*/
    	// the vertex is incident to exactly 2 triangles
    	//if(rightEdge.getNext().getOpposite().getNext()==leftEdge)
    		//count++;

    	// if the vertex has still incident chords it cannot be removed
    	// then return next vertex on cut-border
    	/*if(this.hasIncidentChords[rightEdge.getVertex().index]>0)
    		return i+1; */
    	if(this.hasIncidentChords(node)==true)
    		return node.getNext();
    	
    	// general case: the vertex is incident to more than one triangle
    	
    	// process left and right edges incident to v on the cut-border
    	this.setOutgoingEdge1(rightEdge);
    	this.setOutgoingEdge0(leftEdge);
    	
    	// for computing balanced Schnyder woods
    	int leftNode=leftEdge.getVertex().index;
    	int rightNode=rightEdge.getOpposite().getVertex().index;
    	
    	// v is not anymore on the cut-border
    	this.isOnCutBorder[rightEdge.getVertex().index]=false;
    	
    	// add new edges to the cut-border
    	//this.addToCutBorder(rightEdge.getPrev().getOpposite(), i-1);
    	this.setToCutBorder(rightEdge.getPrev().getOpposite());
    	node.setElement(rightEdge.getPrev().getOpposite());
    	DListNode<Halfedge<Point_>> previousNode=node.getPrev(); // the boundary edge preceding
   	
    	Halfedge<Point_> pEdge=rightEdge.getNext().getOpposite();
    	while(pEdge!=leftEdge.getOpposite()) {
    		//this.addToCutBorder(pEdge.getPrev().getOpposite(), i-1); // old version
    		this.setToCutBorder(pEdge.getPrev().getOpposite());
    		this.outerCycle.insertAfter(previousNode, pEdge.getPrev().getOpposite());
    		this.setIngoingEdge2(pEdge);
    		
    		pEdge=pEdge.getNext().getOpposite();
    	}
    	DListNode<Halfedge<Point_>> result=previousNode.getNext();
    	this.outerCycle.delete(previousNode);
    	
    	return result;
    }

    /**
     * It removes a vertex incident to one triangle from the boundary cycle
     * @param node the list node storing the halfedge whose vertex will be removed
     */
    private DListNode<Halfedge<Point_>> triangleRemoval(DListNode<Halfedge<Point_>> node) {
    	Halfedge<Point_> rightEdge=node.getElement();
    	Halfedge<Point_> leftEdge=node.getPrev().getElement();

    	this.setOutgoingEdge1(rightEdge);
    	this.setOutgoingEdge0(leftEdge);
    	//this.boundary.remove(i);
    	Halfedge<Point_> e=rightEdge.getPrev().getOpposite();
    	node.setElement(e);
    	this.outerCycle.delete(node.getPrev());

    	this.isOnCutBorder[rightEdge.getVertex().index]=false;
    	//this.addToCutBorder(rightEdge.getPrev().getOpposite(), i-1);
    	//this.boundary.add(position, e);

    	this.edgeColor[e.index]=3;
    	this.edgeColor[e.getOpposite().index]=3;
    	
    	this.isChord[e.index]=false;
    	this.isChord[e.getOpposite().index]=false;
    	
    	this.isOnCutBorder[e.getVertex().index]=true;
    	this.isOnCutBorder[e.getOpposite().getVertex().index]=true;

    	return node;
    }
    
    /**
     * Check whether a vertex has incident chords
     * In that case it cannot be removed from the cut-border
     */
    protected boolean hasIncidentChords(DListNode<Halfedge<Point_>> node) {
    	Halfedge<Point_> rightEdge=node.getElement();
    	Halfedge<Point_> leftEdge=node.getPrev().getElement();

    	Halfedge<Point_> pEdge=rightEdge.getNext().getOpposite();
    	while(pEdge!=leftEdge.getOpposite()) {
    		Vertex<Point_> v=pEdge.getOpposite().getVertex();
    		if(this.isOnCutBorder[v.index]==true)
    			return true;
    		pEdge=pEdge.getNext().getOpposite();
    	}
    	return false;
    }

    protected void setIngoingEdge2(Halfedge<Point_> e) {
    	this.edgeColor[e.index]=2;
    	this.edgeColor[e.getOpposite().index]=2;
    	
    	this.isWellOriented[e.index]=true;
    	this.isWellOriented[e.getOpposite().index]=false;
    }

    protected void setOutgoingEdge1(Halfedge<Point_> e) {
    	this.edgeColor[e.index]=1;
    	this.edgeColor[e.getOpposite().index]=1;
    	
    	this.isWellOriented[e.index]=false;
    	this.isWellOriented[e.getOpposite().index]=true;
    }

    protected void setOutgoingEdge0(Halfedge<Point_> e) {
    	this.edgeColor[e.index]=0;
    	this.edgeColor[e.getOpposite().index]=0;
    	
    	this.isWellOriented[e.index]=true;
    	this.isWellOriented[e.getOpposite().index]=false;
    }

    /**
     * Add a half-edge to the cut-border, at a given position (in the cut-border)
     * Update all concerned information (colors, existent chords, boundary vertices, ...)
     */
    public void addToCutBorder(Halfedge<Point_> e, DListNode<Halfedge<Point_>> node) {
    	//System.out.println("edge added to cut-border");
    	if(e==null)
    		throw new Error("halfedge not defined");
    	if(node==null)
    		throw new Error("error null reference: list node");
    	
    	this.outerCycle.insertBefore(node, e);
    	this.setToCutBorder(e);
    }

    /**
     * Add a half-edge to the cut-border, at a given position (in the cut-border)
     * Update all concerned information (colors, existent chords, boundary vertices, ...)
     */
    public void setToCutBorder(Halfedge<Point_> e) {
    	this.edgeColor[e.index]=3;
    	this.edgeColor[e.getOpposite().index]=3;
    	
    	this.isChord[e.index]=false;
    	this.isChord[e.getOpposite().index]=false;
    	
    	this.isOnCutBorder[e.getVertex().index]=true;
    	this.isOnCutBorder[e.getOpposite().getVertex().index]=true;
    }

    /**
     * Perform all steps of the vertex conquest, computing the Schnyder wood
     */
    public void performTraversal() {
    	System.out.print("Computing Schnyder wood (for planar triangulations)...");
    	//System.out.println("Cut border: \n"+this.toString());
    	long startTime=System.nanoTime(), endTime; // for evaluating time performances
    	
    	DListNode<Halfedge<Point_>> node=this.outerCycle.getFirst().getNext();
    	if(node==null) {
    		throw new Error("error null reference: first boundary node not defined");
    	}
    	while(this.outerCycle.size()>1) {
    		//System.out.println("\nCut border: \n"+this.toString());
    		//System.out.println("Processing vertex "+node.getElement().getVertex().index);
    		node=this.vertexRemoval(node);
    	}
    	// set the color of the root edge, which is of color 0 (oriented toward v0)
    	this.edgeColor[rootEdge.index]=0;
    	this.edgeColor[rootEdge.getOpposite().index]=0;
        
    	endTime=System.nanoTime();
        double duration=(double)(endTime-startTime)/1000000000.;
    	System.out.println("done");
    	if(this.verbosity>0)
    		System.out.println(" ("+duration+" seconds)");
    	//System.out.println("Triangle removals: "+count);
    }
        
    /**
     * Return an array of indices representing the original vertex ordering
     */
    public int[] getOriginalVertexOrdering() {
    	int[] result=new int[this.polyhedron.sizeOfVertices()];
    	
    	int i=0;
    	for(Object o: this.polyhedron.vertices) {
    		Vertex<Point_> v=(Vertex<Point_>)o;
    		result[i]=v.index;
    		i++;
    	}
    	return result;    	
    }

	/**
	 * Return the first (half)edge oriented toward vertex v and having color 0 (red)
	 * Edges (v0, v1) and (v2, v0) are assumed to be red colored
	 * Return null, if such an edge does not exist
	 */
	public Halfedge<Point_> getFirstIncomingRedEdge(Vertex<Point_> v) {
		if(v==this.v0) {
			return this.rootEdge.getOpposite(); // half-edge (v1, v0), oriented toward v0
		}
		if(v==this.v1) // no incoming red edges
			return null;
		if(v==this.v2) // no incoming red edges
			return null;
		
    	// visit all (half)-edges incident to v, and the return the first incoming edge of color 0
		Halfedge<Point_> e=v.getHalfedge();
		if(this.edgeColor[e.index]==0 && this.isWellOriented[e.index]==true && this.edgeColor[e.getNext().index]==1)
			return e;
    	Halfedge<Point_> pEdge=e.getOpposite().getPrev();
    	while(pEdge!=e) { // turn around vertex v
    		if(this.edgeColor[pEdge.index]==0 && this.isWellOriented[pEdge.index]==true && this.edgeColor[pEdge.getNext().index]==1)
    			return pEdge;
    		pEdge=pEdge.getOpposite().getPrev();
    	}
    	return null;
	}

	/**
	 * Return the next edge of color 0, after edge e (turning in ccw direction), oriented toward its target vertex (denoted by v).
	 * Half-edge e is assumed to have v as target vertex
	 * Edges (v0, v1) and (v2, v0) are assumed to be red colored (and oriented toward v0)
	 * Return null, if such an edge does not exist
	 */
	public Halfedge<Point_> getNextIncomingRedEdge(Halfedge<Point_> e) {
		if(e==null) return null; 
		Halfedge<Point_> e20=this.rootEdge.getOpposite().getPrev(); // halfedge e=(v2, v0), having v0 as target
		if(e==e20) {
			return null; // e20 is the last ingoing edge (of color 0) incident to v0
		}
		
    	Halfedge<Point_> pEdge=e.getOpposite().getPrev();
    	if(this.edgeColor[pEdge.index]==0 && this.isWellOriented[pEdge.index]==true)
    			return pEdge;
    	return null;
	}

	/**
	 * Check whether the Schnyder wood is valid: a 3-orientation + local edge coloration
	 * 
	 * @return  true if the Schnyder wood is valid
	 */    
	public boolean checkValidity() {
		System.out.println("Checking the validity of the Schnyder Wood: ");
		int n=this.polyhedron.sizeOfVertices(); // number of vertices
		int[] out=new int[n]; // store the number of outgoing edges, for each vertex
		
		int b; // bit in a binary word
		int index;
		
		// count the number of outgoing edges (around each vertex)
		for(Halfedge e: this.polyhedron.halfedges) {
			if(this.isWellOriented[e.index]==this.isWellOriented[e.getOpposite().index]) {
				System.out.println("Error: a pair of half-edges having wrong (same) orientation");
				return false;
			}
			if(this.edgeColor[e.index]!=this.edgeColor[e.getOpposite().index]) {
				System.out.println("Error: a pair of half-edges having wrong colors");
				return false;
			}
			
			if(e.getOpposite().getVertex().index<e.getVertex().index) { // count half-edges only once (in one direction)
				if(this.isWellOriented[e.index]==true) {
					index=e.getOpposite().getVertex().index;
					out[index]++;
					if(out[index]>3) {
						System.out.println("Error: vertex v"+index+" has "+out[index]+" outgoing edges");
						return false;
					}
				}
				else {
					index=e.getVertex().index;
					out[index]++;
					if(out[index]>3) {
						System.out.println("Error: vertex v"+index+" has "+out[index]+" outgoing edges");
						return false;
					}
				}
			}
		}
		
		// check validity of the 3-orientation
		System.out.print("\t Checking edge orientation...");
		for(Vertex v: this.polyhedron.vertices) {
			// first check the outer vertices v0, v1, v2
			if(v==this.v0) {
				if(out[v.index]!=0) {
					System.out.println("Error: outer vertex v0 has "+out[v.index]+" outgoing edges");
					//return false;
				}
			}
			else if(v==this.v1) {
				if(out[v.index]!=1) {
					System.out.println("Error: outer vertex v1 has "+out[v.index]+" outgoing edges");
					//return false;
				}
			}
			else if(v==this.v2) {
				if(out[v.index]!=2) {
					System.out.println("Error: outer vertex v2 has "+out[v.index]+" outgoing edges");
					//return false;
				}
			}
			// check the outdegree of inner vertices
			else {
				if(out[v.index]!=3) {
					System.out.println("Error: inner vertex v"+v.index+" has "+out[v.index]+" outgoing edges");
					return false;
				}
			}
		}
		System.out.println("ok");
		
		// check edge coloring
		System.out.print("\t Checking edge coloring...");
		Halfedge g;
		for(Halfedge e: this.polyhedron.halfedges) {
			Vertex source=e.getOpposite().getVertex();
			Vertex target=e.getVertex();
			if(source.index<target.index) { // count half-edges only once (in one direction)
				if(source!=this.v0 && source!=this.v1 && source!=this.v2 && target!=this.v0 && target!=this.v1 && target!=this.v2) {
					if(this.isWellOriented[e.index]==true) {
						g=e;
					}
					else
						g=e.getOpposite();


					int color=this.edgeColor[g.index];
					int colorLeftTop=this.edgeColor[g.getNext().index]; // color of the Top left edge (next half-edge in the same face)
					int colorRightTop=this.edgeColor[g.getOpposite().getPrev().index]; // color of the Top left edge (next half-edge in the same face)

					if(color==colorLeftTop) {
						if(this.isWellOriented[g.getNext().index]==true)
							return false;
					}
					else {
						if(this.isWellOriented[g.getNext().index]==false) // wrong orientation
							return false;
						if((color+1)%3!=colorLeftTop) // wrong color
							return false;
					}

					if(color==colorRightTop) {
						if(this.isWellOriented[g.getOpposite().getPrev().index]==false)
							return false;
					}
					else {
						if(this.isWellOriented[g.getOpposite().getPrev().index]==true) // wrong orientation
							return false;
						if((color+2)%3!=colorRightTop) // wrong color
							return false;
					}

				}
			}
		}
		
		// check validity of the 3-coloring
		for(Vertex v: this.polyhedron.vertices) {
			int c0=0, c1=0, c2=0;
				List<Halfedge> neighbors=v.getOutgoingHalfedges();
				for(Halfedge e: neighbors) {
					if(this.isWellOriented[e.index]==true && this.edgeColor[e.index]==0)
						c0++;
					else if(this.isWellOriented[e.index]==true && this.edgeColor[e.index]==1)
						c1++;
					else if(this.isWellOriented[e.index]==true && this.edgeColor[e.index]==2)
						c2++;
				}
				
			if(v!=this.v0 && v!=this.v1 && v!=this.v2) { // check colors around inner vertices
				if(c0!=1 || c1!=1 || c2!=1) {
					System.out.println("Error: vertex v"+v.index+" has wrong colored outgoing edges: "+c0+", "+c1+", "+c2);
					return false;
				}
			}
			else if(v==this.v2) {
				if(c0!=1 || c1!=1 || c2!=0) {
					System.out.println("Error: vertex v2 has wrong colored outgoing edges: "+c0+", "+c1+", "+c2);
					return false;
				}
			}
			else if(v==this.v1) {
				if(c0!=1 || c1!=0 || c2!=0) {
					System.out.println("Error: vertex v1 has wrong colored outgoing edges: "+c0+", "+c1+", "+c2);
					return false;
				}
			}
			else if(v==this.v0) {
				if(c0!=0 || c1!=0 || c2!=0) {
					System.out.println("Error: vertex v0 has wrong colored outgoing edges: "+c0+", "+c1+", "+c2);
					return false;
				}
			}

		}
		System.out.println("ok");
		
		return true;
    }
	
	/**
	 * Check whether the face incident to the edge 'e' is CCW oriented <br>
	 * 
	 * Warning: it works only for triangular faces <br>
	 * 
	 * Remark: the faces are assumed to be ccw oriented in the embedding
	 */
	public boolean isCCWOriented(Halfedge<Point_> e) {
		if(e==null) return false;
		
		if(this.isWellOriented[e.index]==false)
			return false;
		if(this.isWellOriented[e.getNext().index]==false)
			return false;
		if(this.isWellOriented[e.getNext().getNext().index]==false)
			return false;
		
		//System.out.println("f CCW ("+e.getVertex().index+", "+e.getNext().getVertex().index+", "+e.getPrev().getVertex().index+")");
		
		return true;
	}
	
	/**
	 * Check whether a separating triangle 't' is 3-colored <br>
	 * 
	 * Remark: the separating triangle 't' is assumed to be "oriented" in a valid way (the 3 edges are consecutive)
	 */
	public boolean is3Colored(Halfedge<Point_>[] t) {
		if(t==null) return false;
		
		int c0=this.edgeColor[t[0].index]; // color of the first edge
		int c1=this.edgeColor[t[1].index];
		int c2=this.edgeColor[t[2].index];
		
		if((c0+1)%3==c1 && (c0+2)%3==c2) // first case: half-edges have the same orientations as the separating cycle (oriented ccw)
			return true;
		if((c0+2)%3==c1 && (c0+1)%3==c2) // half-edges are oriented in opposite direction with respect to the separating cycle (oriented ccw)
			return true;
		
		return false;
	}

	/**
	 * Reverse the orientation of a separating triangle <br>
	 * 
	 * Warning: <br>
	 * -) the triangle is assumed to be separating: it must not be a face <br>
	 * -) half-edges are assumed to be oriented in a coherent manner (they must be consecutive in cw or ccw direction) <br>
	 * -) it works only for separating cycles of size 3 <br>
	 * 
	 * @param  t  an array storing the three half-edges defining a separating triangle
	 */
	public boolean reverse3ColoredTriangle(Halfedge<Point_>[] t) {
		if(t==null) return false;
		if(t[0].getFace()==t[1].getFace() && t[0].getFace()==t[2].getFace()) // the triangle is a face (non separating)
			return false;
		
		if(this.is3Colored(t)==false) {
			System.out.println("Warning: non 3-colored triangle");
			return false;
		}
		
		// probably the 3 conditions above are redundant
		// check whether the triangle coincide with the root face
		if(t[0].getVertex()==this.v0 || t[0].getOpposite().getVertex()==this.v0) // the root face cannot be reversed
			return false;
		if(t[1].getVertex()==this.v0 || t[1].getOpposite().getVertex()==this.v0) // the root face cannot be reversed
			return false;
		if(t[2].getVertex()==this.v0 || t[2].getOpposite().getVertex()==this.v0) // the root face cannot be reversed
			return false;
		
		byte c1=this.edgeColor[t[0].index]; // color of the first edge
		byte c2=this.edgeColor[t[1].index];
		byte c3=this.edgeColor[t[2].index];
		
		System.out.println("Performin triangle reversal: ");
		// there are 4 cases to distinguish
		if(this.isWellOriented[t[0].index]==true && (c1+1)%3==c2 && (c1+2)%3==c3) { // case 1: the half-edges have the same orientation as the cycle, oriented CCW
			// reverse the orientation of the 3 edges (in the separating triangle) and the corresponding opposite half-edges
			System.out.println("case ccw 1");
			this.isWellOriented[t[0].index]=false;
			this.isWellOriented[t[0].getOpposite().index]=true;
			this.isWellOriented[t[1].index]=false;
			this.isWellOriented[t[1].getOpposite().index]=true;
			this.isWellOriented[t[2].index]=false;
			this.isWellOriented[t[2].getOpposite().index]=true;

			// update the color of the 3 half-edges (and their opposite half-edges)
			this.edgeColor[t[0].index]=(byte)((c1+1)%3);
			this.edgeColor[t[0].getOpposite().index]=(byte)((c1+1)%3);
			this.edgeColor[t[1].index]=(byte)((c2+1)%3);
			this.edgeColor[t[1].getOpposite().index]=(byte)((c2+1)%3);
			this.edgeColor[t[2].index]=(byte)((c3+1)%3);
			this.edgeColor[t[2].getOpposite().index]=(byte)((c3+1)%3);
			
			return true;
		}
		else if(this.isWellOriented[t[0].index]==false && (c1+2)%3==c2 && (c1+1)%3==c3) { // case 2: the cycle is CCW, and the half-edges have opposite direction
			// reverse the orientation of the 3 edges (in the separating triangle) and the corresponding opposite half-edges
			System.out.println("case ccw 2");
			this.isWellOriented[t[0].index]=true;
			this.isWellOriented[t[0].getOpposite().index]=false;
			this.isWellOriented[t[1].index]=true;
			this.isWellOriented[t[1].getOpposite().index]=false;
			this.isWellOriented[t[2].index]=true;
			this.isWellOriented[t[2].getOpposite().index]=false;

			// update the color of the 3 half-edges (and their opposite half-edges)
			this.edgeColor[t[0].index]=(byte)((c1+1)%3);
			this.edgeColor[t[0].getOpposite().index]=(byte)((c1+1)%3);
			this.edgeColor[t[1].index]=(byte)((c2+1)%3);
			this.edgeColor[t[1].getOpposite().index]=(byte)((c2+1)%3);
			this.edgeColor[t[2].index]=(byte)((c3+1)%3);
			this.edgeColor[t[2].getOpposite().index]=(byte)((c3+1)%3);
			
			return true;
		}
		else if(this.isWellOriented[t[0].index]==false && (c1+1)%3==c2 && (c1+2)%3==c3) { // case 3: cycle CW oriented, the half-edges having the same orientation
			// reverse the orientation of the 3 edges (in the separating triangle) and the corresponding opposite half-edges
			System.out.println("case cw 3");
			this.isWellOriented[t[0].index]=true;
			this.isWellOriented[t[0].getOpposite().index]=false;
			this.isWellOriented[t[1].index]=true;
			this.isWellOriented[t[1].getOpposite().index]=false;
			this.isWellOriented[t[2].index]=true;
			this.isWellOriented[t[2].getOpposite().index]=false;

			// update the color of the 3 half-edges (and their opposite half-edges)
			this.edgeColor[t[0].index]=(byte)((c1+1)%3);
			this.edgeColor[t[0].getOpposite().index]=(byte)((c1+1)%3);
			this.edgeColor[t[1].index]=(byte)((c2+1)%3);
			this.edgeColor[t[1].getOpposite().index]=(byte)((c2+1)%3);
			this.edgeColor[t[2].index]=(byte)((c3+1)%3);
			this.edgeColor[t[2].getOpposite().index]=(byte)((c3+1)%3);
			
			return true;
		}
		
		System.out.println("Warning: no case:");
		return false; // redundant (never executed, if the cycle is 3-colored)
	}
	
	/**
	 * Check whether the triangle face incident to the half-edge 'e' is CW oriented <br>
	 * 
	 * Warning: it works only for triangular faces <br>
	 * 
	 * Remark: the faces are assumed to be ccw oriented in the embedding
	 */
	public boolean isCWOriented(Halfedge<Point_> e) {
		if(e==null) return false;
		
		//Face root=this.rootEdge.getOpposite().getFace(); // exterior face
		//if(e.getFace()==root)
		//	return false;
		
		if(this.isWellOriented[e.index]==true)
			return false;
		if(this.isWellOriented[e.getNext().index]==true)
			return false;
		if(this.isWellOriented[e.getNext().getNext().index]==true)
			return false;
		
		//System.out.println("f CW ("+e.getVertex().index+", "+e.getNext().getVertex().index+", "+e.getPrev().getVertex().index+")");
		
		return true;
	}

	/**
	 * Count the number of cw-oriented faces
	 */
	public int countCWOrientedFaces() {
		int countCWFaces=0;
		Halfedge e;
		for(Face f: this.polyhedron.facets) {
			e=f.getEdge();
			if(this.isCWOriented(e))
				countCWFaces++;
		}
		return countCWFaces;
	}

	/**
	 * Count the number of ccw-oriented faces
	 */
	public int countCCWOrientedFaces() {
		int countCCWFaces=0;
		Halfedge e;
		for(Face f: this.polyhedron.facets) {
			e=f.getEdge();
			if(this.isCCWOriented(e))
				countCCWFaces++;
		}
		return countCCWFaces;
	}

	/**
	 * Count "balanced" edges: edges whose two incident faces are both (ccw or cw) oriented cycles
	 */
	public int countBalancedEdges() {
		int count=0;
		
		for(Halfedge e: this.polyhedron.halfedges) {
			if(e.getOpposite().getVertex().index<e.getVertex().index) { // count edges only once
				if(this.isCCWOriented(e) && this.isCWOriented(e.getOpposite())) // the two incident faces must have opposite orientation
					count++;
				else if(this.isCWOriented(e) && this.isCCWOriented(e.getOpposite()))
					count++;
			}
		}
		return count;
	}

	/**
	 * Count the number of oriented faces (3-colored faces, oriented CW or CCW)
	 */
	public int count3ColoredFaces() {
		int countCCWFaces=0, countCWFaces=0;
		Halfedge e;
		for(Face f: this.polyhedron.facets) {
			e=f.getEdge();
			if(this.isCCWOriented(e))
				countCCWFaces++;
			if(this.isCWOriented(e))
				countCWFaces++;
		}
		return countCCWFaces+countCWFaces;
	}

	/**
	 * Reverse the orientation of a CCW triangle face <br>
	 * 
	 * Warning: it works only for triangular faces
	 */
	public boolean reverseCCWTriangle(Halfedge<Point_> e) {
		if(e==null) return false;
		if(this.isCCWOriented(e)==false)
			return false;
		
		Face root=this.rootEdge.getOpposite().getFace(); // exterior face
		if(e.getFace()==root)
			return false;
		
		// reverse the orientation of the 3 edges (in the incident face) and the corresponding opposite half-edges
		this.isWellOriented[e.index]=false;
		this.isWellOriented[e.getOpposite().index]=true;
		this.isWellOriented[e.getNext().index]=false;
		this.isWellOriented[e.getNext().getOpposite().index]=true;
		this.isWellOriented[e.getNext().getNext().index]=false;
		this.isWellOriented[e.getNext().getNext().getOpposite().index]=true;
		
		// update the color of the 3 half-edges (and their opposite half-edges)
		byte c1=this.edgeColor[e.index];
		byte c2=this.edgeColor[e.getNext().index];
		byte c3=this.edgeColor[e.getNext().getNext().index];
		this.edgeColor[e.index]=(byte)((c1+1)%3);
		this.edgeColor[e.getNext().index]=(byte)((c2+1)%3);
		this.edgeColor[e.getNext().getNext().index]=(byte)((c3+1)%3);
		this.edgeColor[e.getOpposite().index]=(byte)((c1+1)%3);
		this.edgeColor[e.getNext().getOpposite().index]=(byte)((c2+1)%3);
		this.edgeColor[e.getNext().getNext().getOpposite().index]=(byte)((c3+1)%3);
		
		return true;
	}

	/**
	 * Reverse the orientation of a CW triangle <br>
	 * 
	 * Warning: it works only for triangular faces
	 */
	public boolean reverseCWTriangle(Halfedge<Point_> e) {
		if(e==null) return false;
		if(this.isCWOriented(e)==false)
			return false;
		
		Face root=this.rootEdge.getOpposite().getFace(); // exterior face
		if(e.getFace()==root)
			return false;
		
		// reverse the orientation of the 3 edges (in the incident face) and the corresponding opposite half-edges
		this.isWellOriented[e.index]=true;
		this.isWellOriented[e.getOpposite().index]=false;
		this.isWellOriented[e.getNext().index]=true;
		this.isWellOriented[e.getNext().getOpposite().index]=false;
		this.isWellOriented[e.getNext().getNext().index]=true;
		this.isWellOriented[e.getNext().getNext().getOpposite().index]=false;
		
		// update the color of the 3 half-edges (and their opposite half-edges)
		byte c1=this.edgeColor[e.index];
		byte c2=this.edgeColor[e.getNext().index];
		byte c3=this.edgeColor[e.getNext().getNext().index];
		this.edgeColor[e.index]=(byte)((c1+2)%3);
		this.edgeColor[e.getNext().index]=(byte)((c2+2)%3);
		this.edgeColor[e.getNext().getNext().index]=(byte)((c3+2)%3);
		this.edgeColor[e.getOpposite().index]=(byte)((c1+2)%3);
		this.edgeColor[e.getNext().getOpposite().index]=(byte)((c2+2)%3);
		this.edgeColor[e.getNext().getNext().getOpposite().index]=(byte)((c3+2)%3);
		
		return true;
	}

	/**
	 * Return the parenthesis encoding of the Schnyder wood, consisting of two words
	 * balanced parenthesis words.
	 * 
	 * It performs the traversal of the contour of the tree, in ccw order.
	 * The traversal starts from the half-edge (u, v0) and ends at (v0, v2).
	 * (u, v0) is the half-edge oriented toward v0 corresponding to
	 * the first incoming edge incident to v0 (thus of color 0)
	 */
	public String[] encodeSchnyderWood() {
		System.out.print("Encoding the triangulation (endowed with a Schnyder wood)...");
		String treeCode=""; // add first edge (v1, v0)
		String incomingDegrees=""; // vertex v1 has no incident incoming edges of color 2
		Halfedge<Point_> firstEdge=this.rootEdge.getPrev(); // starting half-edge, of color 0, oriented toward v_0
		Halfedge<Point_> edge20=this.rootEdge.getOpposite().getNext().getOpposite(); // halfedge v20, oriented toward v_0
		Halfedge<Point_> lastEdge=edge20.getOpposite();
		Halfedge<Point_> pEdge=firstEdge; 
		
    	while(pEdge!=lastEdge) { // perform the traversal of the contour of T_0
    		if(this.edgeColor[pEdge.index]==0 && this.isWellOriented[pEdge.index]==true) { // an edge of T_0 visited the first time
    			if(pEdge!=edge20)
    				treeCode=treeCode+"("; // do not write '(' for the edge (v2, v0)
    			if(pEdge!=firstEdge)
    				incomingDegrees=incomingDegrees+'['; // do not write '[' for the first edge oriented toward v0
    			pEdge=pEdge.getPrev();
    		}
    		else if(this.edgeColor[pEdge.index]==2 && this.isWellOriented[pEdge.index]==true) { // a black incoming edge
    			incomingDegrees=incomingDegrees+']';
    			pEdge=pEdge.getOpposite().getPrev();
    		}
    		else if(this.edgeColor[pEdge.index]==1 && this.isWellOriented[pEdge.index]==false) { // outoing blue edge
    			pEdge=pEdge.getOpposite().getPrev();
    		}
    		else if(this.edgeColor[pEdge.index]==2 && this.isWellOriented[pEdge.index]==false) { // black outoing edge
    			pEdge=pEdge.getOpposite().getPrev();
    		}
    		else if(this.edgeColor[pEdge.index]==1 && this.isWellOriented[pEdge.index]==true) { // a blue incoming edge
    			pEdge=pEdge.getOpposite().getPrev();
    		}
    		else if(this.edgeColor[pEdge.index]==0 && this.isWellOriented[pEdge.index]==false) { // an edge of T_0 encountered the second time
    			treeCode=treeCode+")";
    			pEdge=pEdge.getPrev();
    		}
    		else
    			throw new Error("Error: wrong edge orientation/coloration");
    	}
    	treeCode=treeCode+""; // add last closing parenthesis, corresponding to edge (v2, v0)
    	
    	System.out.println("done");
    	String[] result=new String[2];
    	result[0]=treeCode;
    	result[1]=incomingDegrees;
    	
    	
    	return result;
	}

	/**
	 * Compute the path length in the tree T0, for each inner vertex
	 * 
	 * It performs the traversal of the contour of the tree, in ccw order.
	 * The traversal starts from the half-edge (u, v0) and ends at (v0, v2).
	 * (u, v0) is the half-edge oriented toward v0 corresponding to
	 * the first incoming edge incident to v0 (thus of color 0)
	 */
	public void computePathLength() {
		System.out.print("Computing the path length for every vertex...");
		long startTime=System.nanoTime(), endTime; // for evaluating time performances
		
		//String treeCode=""; // add first edge (v1, v0)
		//String incomingDegrees=""; // vertex v1 has no incident incoming edges of color 2
		Halfedge<Point_> firstEdge=this.rootEdge.getPrev(); // starting half-edge, of color 0, oriented toward v_0
		Halfedge<Point_> edge20=this.rootEdge.getOpposite().getNext().getOpposite(); // halfedge v20, oriented toward v_0
		Halfedge<Point_> lastEdge=edge20.getOpposite();
		Halfedge<Point_> pEdge=firstEdge; 
		
		int height=0;
    	while(pEdge!=lastEdge) { // perform the traversal of the contour of T_0
    		if(this.edgeColor[pEdge.index]==0 && this.isWellOriented[pEdge.index]==true) { // an edge of T_0 visited the first time
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
    		else if(this.edgeColor[pEdge.index]==2 && this.isWellOriented[pEdge.index]==true) { // a black incoming edge
    			//incomingDegrees=incomingDegrees+']';
    			pEdge=pEdge.getOpposite().getPrev();
    		}
    		else if(this.edgeColor[pEdge.index]==1 && this.isWellOriented[pEdge.index]==false) { // outoing blue edge
    			pEdge=pEdge.getOpposite().getPrev();
    		}
    		else if(this.edgeColor[pEdge.index]==2 && this.isWellOriented[pEdge.index]==false) { // black outoing edge
    			pEdge=pEdge.getOpposite().getPrev();
    		}
    		else if(this.edgeColor[pEdge.index]==1 && this.isWellOriented[pEdge.index]==true) { // a blue incoming edge
    			pEdge=pEdge.getOpposite().getPrev();
    		}
    		else if(this.edgeColor[pEdge.index]==0 && this.isWellOriented[pEdge.index]==false) { // an edge of T_0 encountered the second time
    			//treeCode=treeCode+")";
    			height--;
    			pEdge=pEdge.getPrev();
    		}
    		else
    			throw new Error("Error: wrong edge orientation/coloration");
    	}
    	//treeCode=treeCode+""; // add last closing parenthesis, corresponding to edge (v2, v0)
    	
    	endTime=System.nanoTime();
        double duration=(double)(endTime-startTime)/1000000000.;
    	System.out.print("done");
    	System.out.println(" ("+duration+" seconds)");
    	String[] result=new String[2];
    	//result[0]=treeCode;
    	//result[1]=incomingDegrees;

	}

    //----------------------------------------------
    //--- Methods for visualizing the cut-border ---
    //----------------------------------------------

	public String printNumberOrientedTriangles() {
		String result;
		int countCCWTriangles=0, countCWTriangles=0;
		for(Face f: this.polyhedron.facets) {
			Halfedge e=f.getEdge();
			if(this.isCCWOriented(e))
				countCCWTriangles++;
			if(this.isCWOriented(e))
				countCWTriangles++;
		}
		result="ccw/cw:"+countCCWTriangles+"/"+countCWTriangles;
		return result;
	}
    
    public String[] originalVertexOrderingToString() {
    	String[] result=new String[this.polyhedron.sizeOfVertices()];
    	
    	int i=0;
    	for(Object o: this.polyhedron.vertices) {
    		Vertex<Point_> v=(Vertex<Point_>)o;
    		result[i]=""+v.index;
    		i++;
    	}
    	return result;    	
    }

    /*public String toString() {
    	String result="cut-border size: "+this.outerCycle.size();
    	result=result+"\nroot edge (v0,v1)=("+v0.index+","+v1.index+")"+"   ("+v0+","+v1+")";
    	result=result+"\nroot face (v0,v1,v2)=("+v0+","+v1+","+v2+")";
    	
    	result=result+"\n boundary edges: [";
		DListNode<Halfedge<Point_>> n=this.outerCycle.getFirst();
		while(n!=this.outerCycle.getLast()) {
			result=result+n.getElement().getVertex().index+" ";
			n=n.getNext();
		}
		result=result+n.getElement().getVertex().index+" ";
		return result+"]";
    	return super.toString();
    }*/
    
	/**
	 * Return a string representing the Schnyder wood
	 */
    public String SchnyderWoodToString() {
    	String result="\nroot edge (v0,v1)=("+v0.index+","+v1.index+")"+"   ("+v0+","+v1+")";
    	result=result+"\nroot face (v0,v1,v2)=("+v0+","+v1+","+v2+")\n";
    	
    	return result+this.orientationToString();
    }
    
    /**
     * @return a real value, evaluating the balance of the edge orientation
     */
    public double getAverageDefect() {
    	double n=this.polyhedron.sizeOfVertices();
    	
    	double balance=0.;
    	for(Vertex v: this.polyhedron.vertices) {
    		if(v!=this.v0 && v!=this.v1 && v!=this.v2) {
    			int vertexDefect=defect(v);
    			balance=balance+vertexDefect;
    		}
    	}
    	return balance/(n-3); // we do not count the 3 vertices on the root outer face
    }
    
    /**
     * @return a real value, evaluating the balance of the edge orientation
     */
    public double statsDefect() {
    	double n=this.polyhedron.sizeOfVertices();
    	
    	double balance=0.;
    	
    	int countNotDefecting=0;
    	int countDefect1=0;
    	int countDefect2=0;
    	int countDefect3=0;
    	for(Vertex v: this.polyhedron.vertices) {
    		if(v!=this.v0 && v!=this.v1 && v!=this.v2) {
    			int vertexDefect=defect(v);
    			if(vertexDefect==0)
    				countNotDefecting++; // count vertices whose defect is not zero
    			if(vertexDefect==1)
    				countDefect1++; // count vertices whose defect is not zero
    			if(vertexDefect==2)
    				countDefect2++; // count vertices whose defect is not zero
    			if(vertexDefect==3)
    				countDefect3++; // count vertices whose defect is not zero
    			
    			balance=balance+vertexDefect;
    		}
    	}
    	System.out.println("Number of non-defecting vertices: "+countNotDefecting+", defect 1: "+countDefect1+", defect 2: "+countDefect2+", defect 3: "+countDefect3);
    	return balance/(n-3); // we do not count the 3 vertices on the root outer face
    }

    /**
     * @return  true if the vertex 'v' is balanced
     */
    public boolean isBalanced(Vertex v) {
    	if(v!=this.v0 && v!=this.v1 && v!=this.v2) {
    		if(this.defect(v)==0)
    			return true;
    		else
    			return false;
    	}
    	else
    		return false; 
    }

    /**
     * @return  the proportion of vertices having a given defect 'd'
     */
    public double countDefect(int d) {
    	double n=this.polyhedron.sizeOfVertices();
    	
    	int count=0;
    	for(Vertex v: this.polyhedron.vertices) {
    		if(v!=this.v0 && v!=this.v1 && v!=this.v2) {
    			int vertexDefect=defect(v);
    			if(vertexDefect==d)
    				count++; // count vertices whose defect is 'd'
    		}
    	}
    	return count/n;
    }

    /**
     * @return  the proportion of balanced vertices
     */
    public double countBalancedVertices() {
    	double n=this.polyhedron.sizeOfVertices();
    	
    	int count=0;
    	for(Vertex v: this.polyhedron.vertices) {
    		if(v!=this.v0 && v!=this.v1 && v!=this.v2) {
    			int vertexDefect=defect(v);
    			if(vertexDefect==0)
    				count++; // count vertices whose defect is 'd'
    		}
    	}
    	return count/n; // LCA: corriger ici: n-3 (v0, v1, v2 ne devraient pas compter)
    }

    /**
     * @return  an integer (0 or 1), the best possible defect for vertex 'v'
     */
    public int bestDefect(Vertex v) {
    	int degree=this.polyhedron.vertexDegree(v);
    	
    	if(degree%3==0)
    		return 0;
    	return 1;
    }

    /**
     * @return  the defect for vertex 'v'
     */
    public int defect(Vertex v) {
    	int min=0, max=0;
    	if(v!=this.v0 && v!=this.v1 && v!=this.v2) {
    		List<Halfedge> edges=v.getOutgoingHalfedges();
    		int red=0, blue=0, black=0;
    		for(Halfedge e: edges) {
    			if(this.isWellOriented[e.index]==false && this.edgeColor[e.index]==0)
    				red++;
    			else if(this.isWellOriented[e.index]==false && this.edgeColor[e.index]==1)
    				blue++;
    			else if(this.isWellOriented[e.index]==false && this.edgeColor[e.index]==2)
    				black++;
    		}
    		min=Math.min(Math.min(red, blue), black);
    		max=Math.max(Math.max(red, blue), black);
    	}
    	return (max-min)-bestDefect(v);
    }

}
