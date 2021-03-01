package jdg.graph;

/**
 * A class for representing an undirected of a graph, as an unordered pair of nodes
 *
 * @author Luca Castelli Aleardi (Ecole Polytechnique, feb 2021)
 */
public class Edge {
	/** an integer useful for indexing this edge */
	public int index;

	/** extremities of the edge */
	public Node first, second;

	/** Initialize an empty edge */
	public Edge() {}

	/** Initialize an edge (a, b) */
	public Edge(Node a, Node b, int index) { 
		this.first=a; 
		this.second=b;
	}

	/** Check whether two points have equal coordinates */
	public boolean equals(Object o) {
		if (o instanceof Edge) {
			Edge e = (Edge) o;
			if(this.first==e.first && this.second==e.second)
				return true;
			else if(this.first==e.second && this.second==e.first)
				return true;
			else
				return false;
		}
		throw new RuntimeException ("Method equals: comparing Edge with object of type " + o.getClass());  	
	}

	public int hashCode () {
		return this.first.index*this.second.index;
	}

}
