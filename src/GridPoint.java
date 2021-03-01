

/**
 * A class for representing a 2D point (a cell) on an integer grid.
 * 
 * @author Luca Castelli Aleardi (jan 2021)
 *
 */
public class GridPoint {
	/** an integer useful for indexing this point: for instance, 'index' can be the number of a vertex */
	public int index;

	/** integer coordinates of the point on the grid */
	private int x, y;

	/** Initialize an empty point */
	public GridPoint() {}

	/** Initialize an integer point having coordinates (x, y) */
	public GridPoint(int x,int y) { 
		this.x=x; 
		this.y=y;
	}

	/** Make a copy of a point <tt>q</tt> */
	public GridPoint(GridPoint p) { 
		this.x=p.x; 
		this.y=p.y; 
	}

	/** Return the x-coordinate */
	public int getX() {return x; }
	/** Return the y-coordinate */
	public int getY() {return y; }

	/** Set the x-coordinate */
	public void setX(int x) {this.x=x; }
	/** Set the y-coordinate */
	public void setY(int y) {this.y=y; }

	/** Translate the current point of the vector (dx, dy) */
	public void translateOf(int dx, int dy) {
		this.x=this.x+dx;
		this.y=this.y+dy;
	}

	/** Check whether two points have equal coordinates */
	public boolean equals(Object o) {
		if (o instanceof GridPoint) {
			GridPoint p = (GridPoint) o;
			return this.x==p.x && this.y==p.y; 
		}
		throw new RuntimeException ("Method equals: comparing GridPoint with object of type " + o.getClass());  	
	}

	public int hashCode () {
		return (int)(this.x*this.x + this.y);
	}

    /**
     * Return the Euclidean distance between the current point 'this' and a point 'p'
     */
	public double euclideanDistance(GridPoint p) {
		return Math.sqrt((double)this.squareEuclideanDistance(p));
	}
    
    /**
     * Return the Manhattan distance between the current point 'this' and a point 'p'
     */
    public int manhattanDistance(GridPoint p) {
    	return Math.abs(this.x-p.x)+Math.abs(this.y-p.y);
    }

    /**
     * Return the square of the Euclidean distance between the current point 'this' and a point 'p'
     */
	public int squareEuclideanDistance(GridPoint p) {
		int dX=p.x-x;
		int dY=p.y-y;
		return dX*dX+dY*dY;
	}

	public String toString() {return "("+x+","+y+")"; }
	public int dimension() { return 2;}

	/** 
	 * Return the d-th coordinate of the point <br>
	 * 
	 * @dim dim  the d-th coordinate of the point: <br> dim=0 for x-coordinate, dim=1 for y-coordinate
	 */
	public int getCartesian(int dim) {
		if(dim==0) return x;
		else return y;
	}
	
	/** 
	 * Set the d-th coordinate of the point 
	 * 
	 * @dim dim  the d-th coordinate of the point: <br> dim=0 for x-coordinate, dim=1 for y-coordinate
	 */
	public void setCartesian(int dim, int x) {
		if(dim==0) this.x=x;
		else if(dim==1) this.y=x;
		else
			throw new Error("Error: wrong dimension d="+dim);
	}

	public void setOrigin() {
		this.x=0;
		this.y=0;
	}

}




