import processing.core.PApplet;

public class MyButton {
	static private int[] darkGrayColor=new int[] {100, 100, 100};
	
	PApplet parent;
  String label;
  float x;    // top left corner x position
  float y;    // top left corner y position
  float w;    // width of button
  float h;    // height of button
  
  public MyButton(PApplet parent, String labelB, float xpos, float ypos, float widthB, float heightB) {
    this.parent=parent;
	  label = labelB;
    x = xpos;
    y = ypos;
    w = widthB;
    h = heightB;
  }
  
  public void draw() {
    parent.fill(darkGrayColor[0], darkGrayColor[1], darkGrayColor[2]);
    parent.stroke(141);
    parent.rect(x, y, w, h, 6);
    parent.textAlign(parent.CENTER, parent.CENTER);
    parent.fill(0);
    parent.text(label, x + (w / 2), y + (h / 2));
    parent.textAlign(parent.BASELINE, parent.BASELINE);
  }
  
  public boolean mouseIsOver() {
    if (parent.mouseX > x && parent.mouseX < (x + w) && parent.mouseY > y && parent.mouseY < (y + h)) {
      return true;
    }
    return false;
  }
  
}

