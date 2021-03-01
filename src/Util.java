import java.util.Random;

import Jcg.geometry.Point_2;
import Jcg.geometry.Point_3;
import jdg.graph.AdjacencyListGraph;
import jdg.graph.Node;

public class Util {
	public static int seed=10;
	/** Random generator */	
	static Random generator = new Random(seed); // initialize random generator
	
	/**
	 * Return the vertices of a regular polygon (equally spaced on a circle of radius r)
	 * 
	 * @param r the radius of the circle
	 * @param k the number of points on the outer cycle
	 * @return Point_2[] an array of 2D points, storing the vertices of a regular polygon
	 */	
	public static Point_2[] regularPolygonVertices(int k, double r) {
		Point_2[] vertices=new Point_2[k];
		double x,y;
		
		for(int i=0;i<k;i++) {
			x=r*Math.cos((2.*Math.PI/k)*i);
			y=r*Math.sin((2.*Math.PI/k)*i);
			vertices[i]=new Point_2(x,y);
		}
		return vertices;
	}
	
	/**
	 * Generate 'n' points at random locations in the plane (in a square of given size WxH)
	 */	
	public static Point_2[] generateRandom2DPoints(int n, double w, double h) {
		Point_2[] points=new Point_2[n];
		
		double w1=w/2., h1=h/2.;
		for(int i=0;i<n;i++){
			double n1=w1-2*w1*generator.nextDouble();
			double n2=h1-2*h1*generator.nextDouble();
		    points[i] = new Point_2 (n1, n2);
		}
		return points;
	}
	
	/**
	 * Generate 'n' points at random locations in 3D (in a box of given size WxLxH)
	 */	
	public static Point_3[] generateRandom3DPoints(int n, double w, double l, double h) {
		Point_3[] points=new Point_3[n];
		double w1=w/2., l1=l/2., h1=h/2.;
		for(int i=0;i<n;i++){
			double n1=w1-2*w1*generator.nextDouble();
			double n2=h1-2*h1*generator.nextDouble();
			double n3=l1-2*l1*generator.nextDouble();
			points[i] = new Point_3 (n1, n2, n3);
		}
		return points;
	}
	
    /**
     * Compute the 2D bounding box containing all input points
     * 
     * @param points  a collection of 2D points (real coordinates)
     */    
    public static double[] compute2DBoundingBox(Point_2[] points) {
    	double 	xmin=Double.MAX_VALUE, xmax=Double.MIN_VALUE, 
    			ymin=Double.MAX_VALUE, ymax=Double.MIN_VALUE;
    	
    	double x, y;
    	for(Point_2 p: points) {
    		x=p.getX().doubleValue();
    		y=p.getY().doubleValue();
    		if (x<xmin)
    			xmin = x;
    		if (x>xmax)
    			xmax = x;
    		if (y<ymin)
    			ymin = y;
    		if (y>ymax)
    			ymax = y;
    	}
    	return new double[]{xmin, ymin, xmax, ymax};
    }

}
