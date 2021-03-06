package tool.clients.diagrams;

import java.io.PrintStream;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;

import tool.xmodeler.XModeler;

public class Shape implements Display {

  String  id;
  int     x;
  int     y;
  int     width;
  int     height;
  boolean showOutline;
  int     lineRed;
  int     lineGreen;
  int     lineBlue;
  int     fillRed;
  int     fillGreen;
  int     fillBlue;
  int[] points;

  public Shape(String id, int x, int y, int width, int height, boolean showOutline, int lineRed, int lineGreen, int lineBlue, int fillRed, int fillGreen, int fillBlue, int[] points) {
    super();
    this.id = id;
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
    this.showOutline = showOutline;
    this.lineRed = lineRed == -1 ? 0 : lineRed % 256;
    this.lineGreen = lineGreen == -1 ? 0 : lineGreen % 256;
    this.lineBlue = lineBlue == -1 ? 0 : lineBlue % 256;
    this.fillRed = fillRed == -1 ? 255 : fillRed % 256;
    this.fillGreen = fillGreen == -1 ? 255 : fillGreen % 256;
    this.fillBlue = fillBlue == -1 ? 255 : fillBlue % 256;
    this.points = points;
  }

  public String getId() {
    return id;
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public boolean isShowOutline() {
    return showOutline;
  }

  public int getLineRed() {
    return lineRed;
  }

  public int getLineGreen() {
    return lineGreen;
  }

  public int getLineBlue() {
    return lineBlue;
  }

  public int getFillRed() {
    return fillRed;
  }

  public int getFillGreen() {
    return fillGreen;
  }

  public int getFillBlue() {
    return fillBlue;
  }

  public int[] getPoints() {
	    return points;
  }
  
  public void paint(GC gc, int x, int y) {
//    if (width > 0 && height > 0) {
	  int[] shiftedPoints = new int[points.length];
	  
	  for (int i = 0; i < points.length; i++) {
		 if(i%2 == 0){
			 shiftedPoints[i] = points[i]+x;
		 }else{
			 shiftedPoints[i] = points[i]+y;
		 }
		 
	  }
	  
      Color fillColor = gc.getBackground();
      gc.setBackground(new Color(XModeler.getXModeler().getDisplay(), getFillRed(), getFillGreen(), getFillBlue()));
      gc.fillPolygon(shiftedPoints);
      gc.setBackground(fillColor);
      Color lineColor = gc.getForeground();
      gc.setForeground(new Color(XModeler.getXModeler().getDisplay(), getLineRed(), getLineGreen(), getLineBlue()));
      gc.drawPolygon(shiftedPoints);
      gc.setForeground(lineColor);
      
//    }
  }

  public void newText(String parentId, String id, String text, int x, int y, boolean editable, boolean underline, boolean italicise, int red, int green, int blue) {

  }

  public void newBox(String parentId, String id, int x, int y, int width, int height, int curve, boolean top, boolean right, boolean bottom, boolean left, int lineRed, int lineGreen, int lineBlue, int fillRed, int fillGreen, int fillBlue) {

  }
  
  public void newNestedDiagram(String parentId, String id, int x, int y, int width, int height, org.eclipse.swt.widgets.Composite canvas) {}

  public void resize(String id, int width, int height) {
    if (getId().equals(id)) {
      this.width = width;
      this.height = height;
    }
  }

  public void editText(String id) {

  }

  public void setText(String id, String text) {

  }

  public void move(String id, int x, int y) {
    if (id.equals(getId())) {
      this.x = x;
      this.y = y;
    }
  }

  public void paintHover(GC gc, int x, int y, int dx, int dy) {

  }

  public void remove(String id) {

  }

  public void doubleClick(GC gc, Diagram diagram, int dx, int dy, int mouseX, int mouseY) {

  }

  public void writeXML(PrintStream out) {
    out.print("<Shape id='" + getId() + "'");
    out.print(" x='" + x + "'");
    out.print(" y='" + y + "'");
    out.print(" width='" + width + "'");
    out.print(" height='" + height + "'");
    out.print(" showOutline='" + showOutline + "'");
    out.print(" lineRed='" + lineRed + "'");
    out.print(" lineGreen='" + lineGreen + "'");
    out.print(" lineBlue='" + lineBlue + "'");
    out.print(" fillRed='" + fillRed + "'");
    out.print(" fillGreen='" + fillGreen + "'");
    out.print(" fillBlue='" + fillBlue + "'");
    out.print(" points='" + points + "'/>");
  }

  public void newMultilineText(String parentId, String id, String text, int x, int y, int width, int height, boolean editable, int lineRed, int lineGreen, int lineBlue, int fillRed, int fillGreen, int fillBlue, String font) {

  }

  public void setFillColor(String id, int red, int green, int blue) {
    if (id.equals(getId())) {
      fillRed = red == -1 ? 255 : red % 256;
      fillGreen = green == -1 ? 255 : green % 256;
      fillBlue = blue == -1 ? 255 : blue % 256;
    }
  }

  public void italicise(String id, boolean italics) {

  }

  public String toString() {
    return "Shape(" + id + "," + x + "," + y + "," + width + "," + height + ")";
  }

  public void newEllipse(String parentId, String id, int x, int y, int width, int height, boolean showOutline, int lineRed, int lineGreen, int lineBlue, int fillRed, int fillGreen, int fillBlue) {

  }

  public void newImage(String parentId, String id, String fileName, int x, int y, int width, int height) {

  }

  public void setFont(String id, String fontData) {

  }

public void setEditable(String id, boolean editable) {
	
}

public void showEdges(String id, boolean top, boolean bottom, boolean left,
		boolean right) {
	
}

public void newShape(String parentId, String id, int x, int y, int width, int height, boolean showOutline, int lineRed,
		int lineGreen, int lineBlue, int fillRed, int fillGreen, int fillBlue, int[] points) {
	
}

}
