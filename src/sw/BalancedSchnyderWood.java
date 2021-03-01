package sw;

import java.util.LinkedList;
import java.util.List;

import Jcg.geometry.Point_;
import Jcg.polyhedron.Halfedge;
import Jcg.polyhedron.Polyhedron_3;
import Jcg.polyhedron.Vertex;
import Jcg.util.DLinkedList;
import Jcg.util.DListNode;

/**
 * @author Luca Castelli Aleardi (2019, Ecole Polytechnique)
 * 
 * Computation of a "balanced" Schnyder wood: the number of incoming edges is "balanced"
 * with respect to the 3 colors.
 */
public class BalancedSchnyderWood extends PlanarTriSchnyderWood {
	
	public int verbosity=0;
	
    // auxiliary information needed for computing a balanced Schnyder wood
    /*protected LinkedList<DListNode<Halfedge<Point_>>> nodes0; // nodes with 0 ingoing red/blue edges 
    protected LinkedList<DListNode<Halfedge<Point_>>> nodes1; // nodes with 1 ingoing red/blue edges 
    protected LinkedList<DListNode<Halfedge<Point_>>> nodes2; // nodes with 2 ingoing red/blue edges
    protected LinkedList<DListNode<Halfedge<Point_>>> nodes3; // nodes with 3 ingoing red/blue edges
    protected LinkedList<DListNode<Halfedge<Point_>>> nodes4; // nodes with >3 ingoing red/blue edges*/
    protected ArrayBasedQueue<DListNode<Halfedge<Point_>>> nodes0; // nodes with 0 ingoing red/blue edges 
    protected ArrayBasedQueue<DListNode<Halfedge<Point_>>> nodes1; // nodes with 1 ingoing red/blue edges 
    protected ArrayBasedQueue<DListNode<Halfedge<Point_>>> nodes2; // nodes with 2 ingoing red/blue edges
    protected ArrayBasedQueue<DListNode<Halfedge<Point_>>> nodes3; // nodes with 3 ingoing red/blue edges
    protected ArrayBasedQueue<DListNode<Halfedge<Point_>>> nodes4; // nodes with >3 ingoing red/blue edges
    protected ArrayBasedQueue<DListNode<Halfedge<Point_>>> nodes5; // nodes with >3 ingoing red/blue edges
    protected ArrayBasedQueue<DListNode<Halfedge<Point_>>> nodes6; // nodes with >3 ingoing red/blue edges
    protected int[] ingoing;

    /**
     * Construct the cut-border starting from the root edge (v0, v1)
     * At the beginning the cut-border contains edges (v2, v0) and (v1, v2)
     * Edges are ccw oriented around faces
     */
    public BalancedSchnyderWood(Polyhedron_3<Point_> polyhedron, Halfedge<Point_> rootEdge) {
    	super(polyhedron, rootEdge);
    	
    	int n=this.polyhedron.sizeOfVertices();
    	/*this.nodes0=new LinkedList<DListNode<Halfedge<Point_>>>();
    	this.nodes1=new LinkedList<DListNode<Halfedge<Point_>>>();
    	this.nodes2=new LinkedList<DListNode<Halfedge<Point_>>>();
    	this.nodes3=new LinkedList<DListNode<Halfedge<Point_>>>();
    	this.nodes4=new LinkedList<DListNode<Halfedge<Point_>>>();*/
    	this.nodes0=new ArrayBasedQueue<DListNode<Halfedge<Point_>>>(n);
    	this.nodes1=new ArrayBasedQueue<DListNode<Halfedge<Point_>>>(n);
    	this.nodes2=new ArrayBasedQueue<DListNode<Halfedge<Point_>>>(n);
    	this.nodes3=new ArrayBasedQueue<DListNode<Halfedge<Point_>>>(n);
    	this.nodes4=new ArrayBasedQueue<DListNode<Halfedge<Point_>>>(n);
    	this.nodes5=new ArrayBasedQueue<DListNode<Halfedge<Point_>>>(n/2);
    	this.nodes6=new ArrayBasedQueue<DListNode<Halfedge<Point_>>>(n/2);
    	this.ingoing=new int[this.polyhedron.sizeOfVertices()];
    	for(int i=0;i<this.ingoing.length;i++) 
    		this.ingoing[i]=-1;
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
    	
    	// v is not anymore on the cut-border
    	this.isOnCutBorder[rightEdge.getVertex().index]=false;
    	
    	// add new edges to the cut-border
    	//this.addToCutBorder(rightEdge.getPrev().getOpposite(), i-1);
    	this.setToCutBorder(rightEdge.getPrev().getOpposite());
    	node.setElement(rightEdge.getPrev().getOpposite());
    	this.addToQueue(node); // add to the queue the (new) rightmost vertex on the path, from v_l to v_r
    	
    	DListNode<Halfedge<Point_>> previousNode=node.getPrev(); // the boundary edge preceding
    	DListNode<Halfedge<Point_>> nextNode=node.getNext(); // the boundary edge following
    	
    	this.addToQueue(nextNode); // for balanced Schnyder woods: incoming blue edge (right)
   	
    	Halfedge<Point_> pEdge=rightEdge.getNext().getOpposite();
    	while(pEdge!=leftEdge.getOpposite()) {
    		this.setToCutBorder(pEdge.getPrev().getOpposite());
    		this.outerCycle.insertAfter(previousNode, pEdge.getPrev().getOpposite());
    		this.setIngoingEdge2(pEdge);
    		
        	this.addToQueue(previousNode.getNext()); // for balanced Schnyder woods

    		pEdge=pEdge.getNext().getOpposite();
    	}
    	DListNode<Halfedge<Point_>> result=previousNode.getNext();
    	this.outerCycle.delete(previousNode);
    	
    	return result;
    }

    /**
     * Add each vertex to a queue, depending on its ingoing degree
     */
    protected void addToQueue(DListNode<Halfedge<Point_>> node) {
    	if(node==null || node.getElement()==null)
    		return;
    	if(node.getElement().getVertex()==this.v0 || node.getElement().getVertex()==this.v1) // LCA: this is instruction is redundant (just for debugging)
    		return;
    	
    	Halfedge e=node.getElement();
    	int vertex=e.getVertex().index;
    	this.ingoing[vertex]++;
    	    	
    	if(this.ingoing[vertex]==0) {
    		this.nodes0.add(node);
    	}
    	else if(this.ingoing[vertex]==1) {
    		this.nodes1.add(node);
    	}
    	else if(this.ingoing[vertex]==2) {
    		this.nodes2.add(node);
    	}
    	else if(this.ingoing[vertex]==3) {
    		this.nodes3.add(node);
    	}
    	else
    		this.nodes4.add(node);

    }
    
    /**
     * Heuristic computation of a balanced Schnyder wood
     */
    public void performTraversal() {
    	if(this.verbosity>0)
    		System.out.println("Computing a balanced Schnyder wood (for planar triangulations)");
    	//System.out.println("Cut border: \n"+this.toString());
    	long startTime=System.nanoTime(), endTime; // for evaluating time performances
    	
    	if(this.verbosity>0)
    		System.out.println("First phase");
    	
    	DListNode<Halfedge<Point_>> node=this.outerCycle.getFirst().getNext();
    	if(node==null) {
    		throw new Error("error null reference: first boundary node not defined");
    	}
    	int count=0;
    	while(this.outerCycle.size()>1 && count<1) {
    		//System.out.println("\nCut border: \n"+this.toString());
    		//System.out.println("Processing vertex "+node.getElement().getVertex().index);
    		node=this.vertexRemoval(node);
    		count++;
    	}

/*    	System.out.println("Intermediate phase");
    	LinkedList<DListNode> pass=this.getCutBorder();
    	node=this.outerCycle.getFirst().getNext();
    	if(node==null) {
    		throw new Error("error null reference: first boundary node not defined");
    	}
    	count=0;
    	int countRemoved=0;
    	while(pass.size()>1) {
    		//this.printCutBorderInfo();
    			node=pass.poll();
    		
    		if(node!=null && node.getElement()!=null) {
    			if(this.isOnCutBorder[node.getElement().getVertex().index]==true) {
    				this.vertexRemoval(node);
    				countRemoved++;
    			}
    		}
    		
    		if(pass.size()<3 && this.outerCycle.size()>1)
    			pass=this.getCutBorder();
    	}
    	System.out.println("Removed: "+countRemoved);*/

    	if(this.verbosity>0)
    		System.out.println("Intermediate phase");
    	
    	node=this.outerCycle.getFirst().getNext();
    	if(node==null) {
    		throw new Error("error null reference: first boundary node not defined");
    	}
    	count=0;
    	int countRemoved=0;
    	while((this.nodes6.isEmpty()==false || this.nodes5.isEmpty()==false || this.nodes4.isEmpty()==false || this.nodes3.isEmpty()==false || this.nodes2.isEmpty()==false || this.nodes1.isEmpty()==false || this.nodes0.isEmpty()==false)) {
    		//this.printCutBorderInfo();
    		
    		if(this.nodes6.isEmpty()==false)
    			node=this.nodes6.poll();
    		if(this.nodes5.isEmpty()==false)
    			node=this.nodes5.poll();
    		else if(this.nodes4.isEmpty()==false)
    			node=this.nodes4.poll();
    		else if(this.nodes3.isEmpty()==false)
    			node=this.nodes3.poll();
    		else if(this.nodes2.isEmpty()==false)
    			node=this.nodes2.poll();
    		else if(this.nodes1.isEmpty()==false)
    			node=this.nodes1.poll();
    		else
    			node=this.nodes0.poll();
    		
    		if(node!=null && node.getElement()!=null) {
    			if(this.isOnCutBorder[node.getElement().getVertex().index]==true) {
    				this.vertexRemoval(node);
    				countRemoved++;
    			}
    		}
    	}
    	if(this.verbosity>0) {
    		System.out.println("Removed: "+countRemoved);
    		this.printCutBorderInfo();
    	}

    	if(this.verbosity>0)
    		System.out.println("--- Final phase ---");
    	
    	
    	
    	node=this.outerCycle.getFirst().getNext();
    	if(node==null) {
    		throw new Error("error null reference: first boundary node not defined");
    	}
    	countRemoved=0;
    	while(this.outerCycle.size()>1) {
    		//System.out.println("\nCut border: \n"+this.toString());
    		//System.out.println("Processing vertex "+node.getElement().getVertex().index);
    		node=this.vertexRemoval(node);
    		countRemoved++;
    	}
    	if(this.verbosity>0)
    		System.out.println("Removed in final phase: "+countRemoved);
    	
    	// set the color of the root edge, which is of color 0 (oriented toward v0)
    	this.edgeColor[rootEdge.index]=0;
    	this.edgeColor[rootEdge.getOpposite().index]=0;
        
    	endTime=System.nanoTime();
        double duration=(double)(endTime-startTime)/1000000000.;
    	System.out.print("Schnyder wood computed");
    	//if(this.verbosity>0)
    	System.out.println(" ("+duration+" seconds)");
    	//System.out.println("Triangle removals: "+count);
    }

    /**
     * Heuristic computation of a balanced Schnyder wood, using a retarding parameter 'shift'
     */
    public void performRetardedTraversal(double shift) {
    	if(this.verbosity>0)
    		System.out.println("Computing a partial balanced Schnyder wood (for planar triangulations), with retarding parameter "+shift);
    	//System.out.println("Cut border: \n"+this.toString());
    	long startTime=System.nanoTime(), endTime; // for evaluating time performances
    	
    	if(this.verbosity>0)
    		System.out.println("First phase");
    	
    	DListNode<Halfedge<Point_>> node=this.outerCycle.getFirst().getNext();
    	if(node==null) {
    		throw new Error("error null reference: first boundary node not defined");
    	}
    	
    	int count=0;
    	int retard=(int)(this.polyhedron.sizeOfVertices()*shift);
    	while(this.outerCycle.size()>1 && count<retard) { // the first removals are performed as in the minimal Schnyder wood (left-most free vertex)
    		node=this.vertexRemoval(node);
    		count++;
    	}

    	if(this.verbosity>0)
    		System.out.println("Intermediate phase");
    	
    	node=this.outerCycle.getFirst().getNext();
    	if(node==null) {
    		throw new Error("error null reference: first boundary node not defined");
    	}
    	count=0;
    	int countRemoved=0;
    	while((this.nodes4.isEmpty()==false || this.nodes3.isEmpty()==false || this.nodes2.isEmpty()==false || this.nodes1.isEmpty()==false || this.nodes0.isEmpty()==false)) {
    		//this.printCutBorderInfo();
    		
    		if(this.nodes4.isEmpty()==false)
    			node=this.nodes4.poll();
    		else if(this.nodes3.isEmpty()==false)
    			node=this.nodes3.poll();
    		else if(this.nodes2.isEmpty()==false)
    			node=this.nodes2.poll();
    		else if(this.nodes1.isEmpty()==false)
    			node=this.nodes1.poll();
    		else
    			node=this.nodes0.poll();
    		
    		if(node!=null && node.getElement()!=null) {
    			if(this.isOnCutBorder[node.getElement().getVertex().index]==true) {
    				this.vertexRemoval(node);
    				countRemoved++;
    			}
    		}
    	}
    	if(this.verbosity>0) {
    		System.out.println("Removed: "+countRemoved);
    		this.printCutBorderInfo();
    	}

    	if(this.verbosity>0)
    		System.out.println("--- Final phase ---");
    	
    	
    	
    	node=this.outerCycle.getFirst().getNext();
    	if(node==null) {
    		throw new Error("error null reference: first boundary node not defined");
    	}
    	countRemoved=0;
    	while(this.outerCycle.size()>1) {
    		//System.out.println("\nCut border: \n"+this.toString());
    		//System.out.println("Processing vertex "+node.getElement().getVertex().index);
    		node=this.vertexRemoval(node);
    		countRemoved++;
    	}
    	if(this.verbosity>0)
    		System.out.println("Removed in final phase: "+countRemoved);
    	
    	// set the color of the root edge, which is of color 0 (oriented toward v0)
    	this.edgeColor[rootEdge.index]=0;
    	this.edgeColor[rootEdge.getOpposite().index]=0;
        
    	endTime=System.nanoTime();
        double duration=(double)(endTime-startTime)/1000000000.;
    	System.out.print("Schnyder wood computed");
    	System.out.println(" ("+duration+" seconds)");
    	//System.out.println("Triangle removals: "+count);
    }

    /**
     * @return the current number of ingoing red and blue edges at a given vertex (not yet conquested)
     */
    public int countIngoingRedBlue(Vertex v) {
    	int ingoing=0;
    	List<Halfedge> edges=v.getOutgoingHalfedges();
    	int red=0, blue=0;
    	for(Halfedge e: edges) {
    		if(this.edgeColor[e.index]==0 || this.edgeColor[e.index]==1)
    			ingoing++;
    	}
    	return ingoing;
    }

    /**
     * @return a good candidate
     */
    public DListNode<Halfedge<Point_>> findCandidate() {
    	DListNode<Halfedge<Point_>> node=this.outerCycle.getFirst().getNext();
    	if(node==null) {
    		throw new Error("error null reference: first boundary node not defined");
    	}
    	int count=0;
    	while(node!=null && node.getElement()!=null) {
    		if(this.countIngoingRedBlue(node.getElement().getVertex())>0)
    			return node;
    		else
    			node=node.getNext();
    	}
    	return null;
    }

    /**
     * Print stats about the cut-border
     */
    public void printCutBorderInfo() {
    	System.out.print("CutBorder info: \n\tsize:"+this.outerCycle.size()+" - ");
    	
    	DListNode<Halfedge<Point_>> node=this.outerCycle.getFirst().getNext();
    	if(node==null) {
    		throw new Error("error null reference: first boundary node not defined");
    	}
    	int count0=0;
    	int count1=0;
    	int count2=0;
    	int count3=0;
    	int count4=0;
    	
    	boolean printVertices=false;
    	if(this.outerCycle.size()<11)
    		printVertices=true;
    	
    	while(node!=null && node.getElement()!=null) {
    		if(printVertices==true)
    			System.out.print(" v"+node.getElement().getVertex().index);
    		
    		if(this.countIngoingRedBlue(node.getElement().getVertex())==0)
    			count0++;
    		else if(this.countIngoingRedBlue(node.getElement().getVertex())==1)
    			count1++;
    		else if(this.countIngoingRedBlue(node.getElement().getVertex())==2)
    			count2++;
    		else if(this.countIngoingRedBlue(node.getElement().getVertex())==3)
    			count3++;
    		else if(this.countIngoingRedBlue(node.getElement().getVertex())==4)
    			count4++;
    		node=node.getNext();
    	}
    	System.out.println("\n\tIngoing: "+count0+", "+count1+", "+count2+", "+count3+", "+count4);
    	
//    	System.out.println("\tQueues: "+this.nodes0.size()+", "+this.nodes1.size()+", "+this.nodes2.size()+", "+this.nodes3.size()+", "+this.nodes4.size());
//    	System.out.println("\t\tqueue 0: "+this.printQueue(this.nodes0));
//    	System.out.println("\t\tqueue 1: "+this.printQueue(this.nodes1));
//    	System.out.println("\t\tqueue 2: "+this.printQueue(this.nodes2));
    }

    /**
     * Print the contents of the queue
     */
    public String printQueue(LinkedList<DListNode<Halfedge<Point_>>> q) {
    	String result="";
    	for(DListNode<Halfedge<Point_>> node: q) {
    		result=result+" "+node.getElement().getVertex().index;
    	}
    	return result;
    }

    /**
     * Print stats about the cut-border
     */
    public LinkedList<DListNode> getCutBorder() {
    	LinkedList<DListNode>result=new LinkedList<DListNode>();
    	//System.out.print("CutBorder: "+this.outerCycle.size());
    	
    	DListNode<Halfedge<Point_>> node=this.outerCycle.getFirst().getNext();
    	if(node==null) {
    		throw new Error("error null reference: first boundary node not defined");
    	}
    	int count0=0;
    	int count1=0;
    	int count2=0;
    	int count3=0;
    	while(node!=null && node.getElement()!=null) {
    		if(this.countIngoingRedBlue(node.getElement().getVertex())==0)
    			result.addLast(node);
    		else 
    			result.addFirst(node);
    		node=node.getNext();
    	}
    	return result;
    }

}
