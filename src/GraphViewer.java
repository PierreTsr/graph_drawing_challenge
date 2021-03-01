import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.LinkedList;

import jdg.graph.Edge;
import jdg.graph.Node;
import processing.core.PApplet;

/**
 * Main program that takes as input a JSON or OFF file storing an input planar graph (possibly with bends)
 * 
 * @author Luca Castelli Aleardi (Ecole Polytechnique, feb 2021)
 */
public class GraphViewer extends PApplet {
	/** A grid layout storing the instance of the input problem */
	public static GridLayout layout;
	/** An algorithm for minimizing the planar polyline edge-length ratio */
	public MyBestAlgorithm algo;
	
	// color definitions
	private int[] whiteColor=new int[] {255, 255, 255};
	private int[] grayColor=new int[] {150, 150, 150};
	private int[] darkGrayColor=new int[] {100, 100, 100};
	
	/** User options */
	private static String userOptionsAll="press '-' or '+' for zooming\n"
			+ "press 'r' to run your algorithm\n"
			+ "press 'v' to check the validity of the layout\n"
			+ "press 'e' to evaluate the edge length ratio\n"
			+ "use 'right mouse button' to drag the layout (press right button, move the mouse and release)";


	// parameters of the 2d frame/canvas	
	public static int sizeX; // horizontal size of the canvas
	public static int sizeY; // vertical size of the canvas (pixels)
	private int cellSize; // size of the cell in the grid (expressed in pixels)
	private int shiftX, shiftY; // (pixels) coordinates of the bottom left vertex of the bounding box
	int[] currentMousePosition; // mouse position on the screen (pixels)
	int[] oldMousePosition; // mouse position on the screen (pixels)
	boolean mouseIsDragged=false;
	
	/** export current solution to Json output file */
	public MyButton exportJson; 

	public void settings(){
		this.size(sizeX,sizeY); // set the size of the Java Processing frame

		int max=Math.max(this.layout.width, this.layout.height)+10;
		this.cellSize=(sizeX)/max; // adjust the size of cells
		this.cellSize=Math.min(this.cellSize, ((sizeY-100))/max);
		this.cellSize=Math.max(this.cellSize, 4);
		this.shiftX=(sizeX-(this.cellSize*(max-1)))/2;
		this.shiftY=((sizeY-100)-(this.cellSize*(this.layout.height-1)))/2;
	}

	public void setup(){
		System.out.println("Initializing MotionViewer program");
		// set buttons and colors
		exportJson=new MyButton(this, "export layout to JSON", 650, 4, 145, 20);
		this.algo=new MyBestAlgorithm(this.layout);
		//this.colorMode(this.HSB, 100); // set the color mode
		this.textSize(10);
	}

	/**
	 * Deal with keyboard events
	 */
	public void keyPressed(){
		switch(key) {
		case('+'): this.cellSize+=1; break;
		case('-'): this.cellSize=Math.max(4, this.cellSize-1); break;
		case('r'): this.algo.run(); break;
		case('v'): this.layout.isValid(); break;
		case('e'): this.layout.computeEdgeLengthRatio(); break;
		}
	}

	/**
	 * Deal with keyboard events
	 */
	public void keyReleased(){
		switch(key) {
		}
	}

	/**
	 * Deal with mouse interaction
	 */
	public void mousePressed() {
		  this.currentMousePosition=new int[] {mouseX, mouseY};
		  this.oldMousePosition=new int[] {mouseX, mouseY};
		  
		  if (this.exportJson.mouseIsOver()) {
			    System.out.println("Export layout to Json output file");
			    IO.saveLayoutToJSON(this.layout, layout.name+"_output.json"); // export the solution in JSON format
		  }
		  else {
			  int[] cellCoord=this.getGridCellFromMouseLocation(mouseX, mouseY);
			  System.out.println("["+cellCoord[0]+", "+cellCoord[1]+"]"+" - "+mouseX+", "+mouseY+", size "+this.cellSize);
		  }
	}

	/**
	 * Deal with mouse interaction
	 */
	public void mouseReleased() {
		  if(mouseButton==RIGHT) { // translate the window
			  int deltaX=(mouseX-currentMousePosition[0])/2;
			  int deltaY=(currentMousePosition[1]-mouseY)/2;
			  this.shiftX=this.shiftX+deltaX;
			  this.shiftY=this.shiftY+deltaY;

			  this.currentMousePosition=new int[] {mouseX, mouseY};
		  }
		  else if(mouseButton==LEFT) {
			  System.out.println("Region selection (mouse released): ");
			  this.currentMousePosition=new int[] {mouseX, mouseY};
			  int[] p=this.getGridCellFromMouseLocation(this.oldMousePosition[0], this.oldMousePosition[1]);
			  int[] q=this.getGridCellFromMouseLocation(mouseX, mouseY);
			  if(this.oldMousePosition!=null && this.currentMousePosition!=null) {
				  System.out.println("\t first window corner ["+this.oldMousePosition[0]+", "+this.oldMousePosition[1]+"]"+" - ["+p[0]+","+p[1]+"]");
				  System.out.println("\t second window corner ["+this.currentMousePosition[0]+", "+this.currentMousePosition[1]+"]");
			  }
			  System.out.println("\t mouse coordinates ["+mouseX+", "+mouseY+"]");
			  
			  this.oldMousePosition=null;
			  this.mouseIsDragged=false;
		  }
	}

	/**
	 * Deal with mouse interaction
	 */
	public void mouseDragged() {
		if(mouseButton==LEFT && this.mouseIsDragged==false) {
			System.out.print("Mouse dragged: ");
			this.mouseIsDragged=true;
		}
		  if(mouseButton==RIGHT) { // translate the window
			  int deltaX=(mouseX-currentMousePosition[0])/2;
			  int deltaY=(currentMousePosition[1]-mouseY)/2;
			  this.shiftX=this.shiftX+deltaX;
			  this.shiftY=this.shiftY+deltaY;

			  this.currentMousePosition=new int[] {mouseX, mouseY};
		  }
	}

	/**
	 * Main function for drawing all geometric objects (the grid, robots, targets, buttons, ...)
	 */
	public void draw(){
		this.background(255); // set the color of background (clean the background)

		this.drawGrid(this.layout.width, this.layout.height); // draw the grid
		this.drawLayout(this.layout); // draw the planar layout of the graph (with polylines)
		
		// draw buttons and options
		this.drawOptions();
		this.exportJson.draw();
	}

	/**
	 * Show user options on the screen
	 */
	public void drawOptions() {
		String label=this.userOptionsAll;
		int posX=0;
		int posY=0;
		int textHeight=100;
		this.textSize(12);

		//this.stroke(edgeColor, edgeOpacity);
		this.fill(grayColor[0], grayColor[1], grayColor[2]);
		this.rect((float)posX, (float)posY, this.width, textHeight); // fill a gray rectangle
		this.fill(0, 0, 0);
		this.text(label, (float)posX+2, (float)posY+10); // draw the text
		
	}

	/**
	 * Draw an integer label in a square
	 */
	public void drawLabel(String label, int x, int y) {
		int delta=1;
		if(label.length()>2)
			delta=0;

		int[] pos=this.getPixel(x-1, y-1);
		this.text(label, pos[0]+delta, pos[1]-delta); // draw the text
	}

	/**
	 * Draw a black point
	 */
	public void drawBlackPoint(int x, int y) {
		this.stroke(0);
		this.fill(0);
		int[] pos=this.getPixel(x, y);
		this.ellipse(pos[0], pos[1], 2, 2);
	}

	/**
	 * Draw a black point
	 */
	public void drawWhitePoint(int x, int y) {
		this.stroke(0);
		this.fill(255, 255, 255);
		int[] pos=this.getPixel(x, y);
		this.ellipse(pos[0], pos[1], 4, 4);
	}

	/**
	 * Draw a black segment (x1, y1) - (x2, y2) on the grid
	 */
	public void drawSegment(int x1, int y1, int x2, int y2) {
		this.stroke(0);
		this.fill(255, 255, 255);
		int[] pos1=this.getPixel(x1, y1);
		int[] pos2=this.getPixel(x2, y2);
		this.line(pos1[0], pos1[1], pos2[0], pos2[1]);
	}

	/**
	 * Draw a grid layout of the graph
	 */
	public void drawLayout(GridLayout drawing) {
		if(drawing==null || drawing.g==null)
			return;
		
		// draw the edges
		for(Node u: drawing.g.nodes) { // iterate over all nodes
			for(Node v: u.neighbors) { // iterate over the neighbors of node 'u'
				if(u.index<v.index) { // draw edges only once
					int edgeIndex=this.layout.g.getEdgeIndex(u, v);
					GridPoint pU=drawing.points[u.index]; // coordinates of 'u'
					GridPoint pV=drawing.points[v.index]; // coordinates of 'v'
					if(drawing.bendPoints[edgeIndex]==null) // the edge (u, v) has no bends
						this.drawSegment(pU.getX(), pU.getY(), pV.getX(), pV.getY()); // draw (u, v) with a straight-line segment
					else { // the edge (u, v) has bends
						int nBends=drawing.bendPoints[edgeIndex].length; // number of bends of the edge (u, v)
						GridPoint firstBend=drawing.bendPoints[edgeIndex][0];
						GridPoint lastBend=drawing.bendPoints[edgeIndex][nBends-1];
						
						this.drawSegment(pU.getX(), pU.getY(), firstBend.getX(), firstBend.getY()); // draw first bend
						this.drawBlackPoint(firstBend.getX(), firstBend.getY());
						for(int k=0;k<nBends-1;k++) { // draw all intermediate bends
							GridPoint b1=drawing.bendPoints[edgeIndex][k];
							GridPoint b2=drawing.bendPoints[edgeIndex][k+1];
							this.drawSegment(b1.getX(), b1.getY(), b2.getX(), b2.getY()); // draw first bend
							this.drawBlackPoint(b1.getX(), b1.getY());
						}
						this.drawSegment(lastBend.getX(), lastBend.getY(), pV.getX(), pV.getY()); // draw last bend
						this.drawBlackPoint(lastBend.getX(), lastBend.getY());
					}
				}
			}
		}
		// draw the vertices
		for(int i=0;i<drawing.n;i++) {
			GridPoint p=drawing.points[i]; // coordinates of the 'i'-th vertex in the graph
			this.drawWhitePoint(p.getX(), p.getY());
		}
	}

	/**
	 * Draw the entire grid
	 */
	public void drawGrid(int xmax, int ymax) {
		this.stroke(200); // grid color
		this.strokeWeight(1);

		int min=-shiftX/this.cellSize;
		int max=Math.max(this.width/this.cellSize, Math.abs(shiftX))+1;
		for(int i=min;i<=max;i++) { // draw vertical lines
			int[] pixel=this.getPixel(i, 0);
			this.line(pixel[0], 0, pixel[0], this.height);
		}
		min=-shiftY/this.cellSize;
		max=Math.max(this.height/this.cellSize, Math.abs(shiftY))+1;
		for(int j=min;j<=max;j++) { // draw horizontal lines
			int[] pixel=this.getPixel(0, j);
			this.line(0, pixel[1], this.width, pixel[1]);
		}

		this.stroke(0, 0, 0); // border color (black)
		this.strokeWeight(1);
	}

	/**
	 * Draw the entire grid
	 */
	public void drawBoundingBox(int xmin, int ymin, int xmax, int ymax) {
		int[] pixel00=this.getPixel(xmin, ymin);
		int[] pixel10=this.getPixel(xmax, ymin);
		int[] pixel11=this.getPixel(xmax, ymax);
		int[] pixel01=this.getPixel(xmin, ymax);
		this.line(pixel00[0], pixel00[1], pixel10[0], pixel10[1]);
		this.line(pixel01[0], pixel01[1], pixel11[0], pixel11[1]);
		this.line(pixel00[0], pixel00[1], pixel01[0], pixel01[1]);
		this.line(pixel10[0], pixel10[1], pixel11[0], pixel11[1]);
	}

	/**
	 * Given a pixel (i, j) on the screen return the coordinates of the corresponding grid cell on the board
	 */
	public int[] getGridCellFromMouseLocation(int i, int j) {
		int x, y;
		if(i>=this.shiftX)
			x=1+(i-this.shiftX)/this.cellSize;
		else if(i<this.shiftX-this.cellSize)
			x=(i-this.shiftX)/this.cellSize;
		else
			x=0;
		
		int J=this.height-j; // reverse coordinates on y-axis
		if(J>=this.shiftY)
			y=1+(J-this.shiftY)/this.cellSize;
		else if(J<this.shiftY-this.cellSize)
			y=(J-this.shiftY)/this.cellSize;
		else
			y=0;
		
		//System.out.println("\t J="+J+", shiftY="+this.shiftY+", y="+y);
		
		return new int[] {x, y};
	}

	/**
	 * Given a cell (x, y) on a regular integer grid whose cells have a given 'cellSize', returns the corresponding pixel on the screen
	 */
	public int[] getPixel(int x, int y) {
		int i=this.shiftX+(x*this.cellSize);
		int j=this.height-(this.shiftY+(y*this.cellSize));
		
		return new int[] {i, j};
	}

	public static void main(String[] args) {
		System.out.println("Tools for the \"Graph Drawing Contest 2021: Live Challenge\"");
		if(args.length<1) {
			System.out.println("Error: one argument required: input file in JSON or OFF format");
			System.exit(0);
		}

		/** input file storing the instance of the problem */
		String inputFile;
		GridLayout layout=null;
		
		inputFile=args[0];
		System.out.println("Input file: "+inputFile);
		
		if(inputFile.endsWith(".json")==true) {
			layout=IO.loadInputFromJSON(inputFile); // read the input Json file (problem instance)
		}
		else if(inputFile.endsWith(".off")==true) {
			layout=IO.loadInputFromOFF(inputFile, 1, false);
		}
		else {
			System.out.println("Error: wrong input format");
			System.out.println("Supported input format: JSON format");
			System.exit(0);
		}

		
		// Initialize the Processing viewer
		GraphViewer.sizeX=800;
		GraphViewer.sizeY=600;
		GraphViewer.layout=layout;
		System.out.println("Grid size: "+layout.width+"x"+layout.height);
		System.out.println("Maximal number of bends: "+layout.maxBends);

		PApplet.main(new String[] { "GraphViewer" }); // launch the Processing viewer
	}
	
}
