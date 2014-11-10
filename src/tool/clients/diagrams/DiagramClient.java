package tool.clients.diagrams;

import java.io.PrintStream;
import java.util.Hashtable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Text;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import tool.clients.Client;
import tool.xmodeler.XModeler;
import xos.Message;
import xos.Value;

public class DiagramClient extends Client {

  public static void start(CTabFolder tabFolder) {
    DiagramClient.tabFolder = tabFolder;
  }

  public static DiagramClient theClient() {
    return theClient;
  }

  static DiagramClient               theClient;
  static CTabFolder                  tabFolder;
  static Hashtable<String, CTabItem> tabs        = new Hashtable<String, CTabItem>();
  static Hashtable<String, Diagram>  diagrams    = new Hashtable<String, Diagram>();
  static Font                        diagramFont = new Font(XModeler.getXModeler().getDisplay(), new FontData("Courier New", 12, SWT.NO));

  public DiagramClient() {
    super("com.ceteva.diagram");
    theClient = this;
  }

  public Value callMessage(Message message) {
    if (message.hasName("getTextDimension"))
      return getTextDimension(message);
    else return super.callMessage(message);
  }

  private void delete(final Message message) {
    runOnDisplay(new Runnable() {
      public void run() {
        Value id = message.args[0];
        for (Diagram diagram : diagrams.values()) {
          diagram.delete(id.strValue());
        }
      }
    });
  }

  private void editText(Message message) {
    final Value id = message.args[0];
    runOnDisplay(new Runnable() {
      public void run() {
        for (Diagram diagram : diagrams.values())
          diagram.editText(id.strValue());
      }
    });
  }

  private Diagram getSelectedDiagram() {
    CTabItem item = tabFolder.getSelection();
    for (String id : tabs.keySet()) {
      if (tabs.get(id) == item) { return diagrams.get(id); }
    }
    throw new Error("cannot find the current diagram");
  }

  private Value getTextDimension(final Message message) {
    final Value[] result = new Value[1];
    runOnDisplay(new Runnable() {
      public void run() {
        Value text = message.args[0];
        Value italics = message.args[1];
        Diagram diagram = getSelectedDiagram();
        Point extent = textDimension(text.strValue(), diagramFont);
        Value width = new Value(extent.x);
        Value height = new Value(extent.y);
        result[0] = new Value(new Value[] { width, height });
      }
    });
    return result[0];
  }

  private void globalRenderOff() {
    for (Diagram diagram : diagrams.values())
      diagram.renderOff();
  }

  private void globalRenderOn() {
    for (Diagram diagram : diagrams.values())
      diagram.renderOn();
  }

  private void inflateBox(String parentId, Node node) {
    String id = XModeler.attributeValue(node, "id");
    int x = Integer.parseInt(XModeler.attributeValue(node, "x"));
    int y = Integer.parseInt(XModeler.attributeValue(node, "y"));
    int width = Integer.parseInt(XModeler.attributeValue(node, "width"));
    int height = Integer.parseInt(XModeler.attributeValue(node, "height"));
    int curve = Integer.parseInt(XModeler.attributeValue(node, "curve"));
    boolean top = XModeler.attributeValue(node, "top").equals("true");
    boolean right = XModeler.attributeValue(node, "right").equals("true");
    boolean bottom = XModeler.attributeValue(node, "bottom").equals("true");
    boolean left = XModeler.attributeValue(node, "left").equals("true");
    int lineRed = Integer.parseInt(XModeler.attributeValue(node, "lineRed"));
    int lineGreen = Integer.parseInt(XModeler.attributeValue(node, "lineGreen"));
    int lineBlue = Integer.parseInt(XModeler.attributeValue(node, "lineBlue"));
    int fillRed = Integer.parseInt(XModeler.attributeValue(node, "fillRed"));
    int fillGreen = Integer.parseInt(XModeler.attributeValue(node, "fillGreen"));
    int fillBlue = Integer.parseInt(XModeler.attributeValue(node, "fillBlue"));
    newBox(parentId, id, x, y, width, height, curve, top, right, bottom, left, lineRed, lineGreen, lineBlue, fillRed, fillGreen, fillBlue);
    NodeList children = node.getChildNodes();
    for (int i = 0; i < children.getLength(); i++)
      inflateNodeElement(id, children.item(i));
  }

  private void inflateButton(String diagramId, String groupId, Node button) {
    String buttonName = XModeler.attributeValue(button, "name");
    String buttonAction = XModeler.attributeValue(button, "tool");
    String icon = XModeler.attributeValue(button, "icon");
    boolean isEdge = XModeler.attributeValue(button, "isEdge").equals("true");
    newTool(diagramId, groupId, buttonName, buttonAction, isEdge, icon);
  }

  private void inflateDiagram(Node diagram) {
    String id = XModeler.attributeValue(diagram, "id");
    String label = XModeler.attributeValue(diagram, "label");
    int zoom = Integer.parseInt(XModeler.attributeValue(diagram, "zoom", "100"));
    newDiagram(id, label);
    Diagram d = diagrams.get(id);
    d.renderOff();
    d.setZoom(zoom);
    NodeList children = diagram.getChildNodes();
    for (int i = 0; i < children.getLength(); i++)
      inflateDiagramElement(id, children.item(i));
    d.renderOn();
  }

  private void inflateDiagramEdge(String parentId, Node edge) {
    String id = XModeler.attributeValue(edge, "id");
    int refx = Integer.parseInt(XModeler.attributeValue(edge, "refx"));
    int refy = Integer.parseInt(XModeler.attributeValue(edge, "refy"));
    String source = XModeler.attributeValue(edge, "source");
    String target = XModeler.attributeValue(edge, "target");
    String sourcePort = XModeler.attributeValue(edge, "sourcePort");
    String targetPort = XModeler.attributeValue(edge, "targetPort");
    int sourceHead = Integer.parseInt(XModeler.attributeValue(edge, "sourceHead"));
    int targetHead = Integer.parseInt(XModeler.attributeValue(edge, "targetHead"));
    int lineStyle = Integer.parseInt(XModeler.attributeValue(edge, "lineStyle"));
    int red = Integer.parseInt(XModeler.attributeValue(edge, "red"));
    int green = Integer.parseInt(XModeler.attributeValue(edge, "green"));
    int blue = Integer.parseInt(XModeler.attributeValue(edge, "blue"));
    newEdge(parentId, id, sourcePort, targetPort, refx, refy, sourceHead, targetHead, lineStyle, red, green, blue);
    NodeList children = edge.getChildNodes();
    for (int i = 0; i < children.getLength(); i++)
      inflateEdgeElement(id, children.item(i));
  }

  private void inflateDiagramElement(String id, Node node) {
    if (node.getNodeName().equals("Palette"))
      inflatePalette(id, node);
    else if (node.getNodeName().equals("Node"))
      inflateDiagramNode(id, node);
    else if (node.getNodeName().equals("Edge"))
      inflateDiagramEdge(id, node);
    else System.out.println("Unknown type of diagram node " + node.getNodeName());
  }

  private void inflateDiagramNode(String diagramId, Node node) {
    String nodeId = XModeler.attributeValue(node, "id");
    int x = Integer.parseInt(XModeler.attributeValue(node, "x"));
    int y = Integer.parseInt(XModeler.attributeValue(node, "y"));
    int width = Integer.parseInt(XModeler.attributeValue(node, "width"));
    int height = Integer.parseInt(XModeler.attributeValue(node, "height"));
    boolean selectable = XModeler.attributeValue(node, "selectable").equals("true");
    newNode(diagramId, nodeId, x, y, width, height, selectable);
    NodeList children = node.getChildNodes();
    for (int i = 0; i < children.getLength(); i++)
      inflateNodeElement(nodeId, children.item(i));
  }

  private void inflateEdgeElement(String id, Node node) {
    if (node.getNodeName().equals("Waypoint"))
      inflateWaypoint(id, node);
    else if (node.getNodeName().equals("Label"))
      inflateLabel(id, node);
    else System.out.println("Unknown type of edge element " + node.getNodeName());
  }

  private void inflateGroup(String id, Node group) {
    String name = XModeler.attributeValue(group, "name");
    newGroup(id, name);
    NodeList buttons = group.getChildNodes();
    for (int i = 0; i < buttons.getLength(); i++)
      inflateButton(id, name, buttons.item(i));
  }

  private void inflateLabel(String edgeId, Node node) {
    String id = XModeler.attributeValue(node, "id");
    String text = XModeler.attributeValue(node, "text");
    String pos = XModeler.attributeValue(node, "pos");
    int x = Integer.parseInt(XModeler.attributeValue(node, "x"));
    int y = Integer.parseInt(XModeler.attributeValue(node, "y"));
    boolean editable = XModeler.attributeValue(node, "editable", "true").equals("true");
    boolean underline = XModeler.attributeValue(node, "underline").equals("true");
    boolean condense = XModeler.attributeValue(node, "condense").equals("true");
    int red = Integer.parseInt(XModeler.attributeValue(node, "red"));
    int green = Integer.parseInt(XModeler.attributeValue(node, "green"));
    int blue = Integer.parseInt(XModeler.attributeValue(node, "blue"));
    String font = XModeler.attributeValue(node, "font");
    newLabel(edgeId, id, text, pos, x, y, editable, underline, condense, red, green, blue, font);
  }

  private void inflateNodeElement(String id, Node node) {
    if (node.getNodeName().equals("Port"))
      inflatePort(id, node);
    else if (node.getNodeName().equals("Box"))
      inflateBox(id, node);
    else if (node.getNodeName().equals("Text"))
      inflateText(id, node);
    else System.out.println("Unknown type of node element " + node.getNodeName());
  }

  private void inflatePalette(String id, Node node) {
    NodeList groups = node.getChildNodes();
    for (int i = 0; i < groups.getLength(); i++)
      inflateGroup(id, groups.item(i));
  }

  private void inflatePort(String parentId, Node node) {
    String id = XModeler.attributeValue(node, "id");
    int x = Integer.parseInt(XModeler.attributeValue(node, "x"));
    int y = Integer.parseInt(XModeler.attributeValue(node, "y"));
    int width = Integer.parseInt(XModeler.attributeValue(node, "width"));
    int height = Integer.parseInt(XModeler.attributeValue(node, "height"));
    newPort(parentId, id, x, y, width, height);
  }

  private void inflateText(String parentId, Node node) {
    String id = XModeler.attributeValue(node, "id");
    String text = XModeler.attributeValue(node, "text");
    int x = Integer.parseInt(XModeler.attributeValue(node, "x"));
    int y = Integer.parseInt(XModeler.attributeValue(node, "y"));
    boolean editable = XModeler.attributeValue(node, "editable").equals("true");
    boolean underline = XModeler.attributeValue(node, "underline").equals("true");
    boolean italicise = XModeler.attributeValue(node, "italicise").equals("true");
    int red = Integer.parseInt(XModeler.attributeValue(node, "red"));
    int green = Integer.parseInt(XModeler.attributeValue(node, "green"));
    int blue = Integer.parseInt(XModeler.attributeValue(node, "blue"));
    newText(parentId, id, text, x, y, editable, underline, italicise, red, green, blue);
  }

  private void inflateWaypoint(String edgeId, Node node) {
    String id = XModeler.attributeValue(node, "id");
    int index = Integer.parseInt(XModeler.attributeValue(node, "index"));
    int x = Integer.parseInt(XModeler.attributeValue(node, "x"));
    int y = Integer.parseInt(XModeler.attributeValue(node, "y"));
    newWaypoint(edgeId, id, index, x, y);
  }

  public void inflateXML(Document doc) {
    NodeList diagramClients = doc.getElementsByTagName("Diagrams");
    if (diagramClients.getLength() == 1) {
      Node diagramClient = diagramClients.item(0);
      NodeList diagrams = diagramClient.getChildNodes();
      for (int i = 0; i < diagrams.getLength(); i++) {
        Node diagram = diagrams.item(i);
        inflateDiagram(diagram);
      }
    } else System.out.println("expecting exactly 1 diagram client got: " + diagramClients.getLength());
  }

  private void move(Message message) {
    Value id = message.args[0];
    Value x = message.args[1];
    Value y = message.args[2];
    for (Diagram diagram : diagrams.values())
      diagram.move(id.strValue(), x.intValue, y.intValue);
  }

  private void newBox(Message message) {
    final String parentId = message.args[0].strValue();
    final String id = message.args[1].strValue();
    final int x = message.args[2].intValue;
    final int y = message.args[3].intValue;
    final int width = message.args[4].intValue;
    final int height = message.args[5].intValue;
    final int curve = message.args[6].intValue;
    final boolean top = message.args[7].boolValue;
    final boolean right = message.args[8].boolValue;
    final boolean bottom = message.args[9].boolValue;
    final boolean left = message.args[10].boolValue;
    final int lineRed = message.args[11].intValue;
    final int lineGreen = message.args[12].intValue;
    final int lineBlue = message.args[13].intValue;
    final int fillRed = message.args[14].intValue;
    final int fillGreen = message.args[15].intValue;
    final int fillBlue = message.args[16].intValue;
    newBox(parentId, id, x, y, width, height, curve, top, right, bottom, left, lineRed, lineGreen, lineBlue, fillRed, fillGreen, fillBlue);

  }

  private void newBox(String parentId, String id, int x, int y, int width, int height, int curve, boolean top, boolean right, boolean bottom, boolean left, int lineRed, int lineGreen, int lineBlue, int fillRed, int fillGreen, int fillBlue) {
    for (Diagram diagram : diagrams.values()) {
      diagram.newBox(parentId, id, x, y, width, height, curve, top, right, bottom, left, lineRed, lineGreen, lineBlue, fillRed, fillGreen, fillBlue);
    }
  }

  private void newDiagram(Message message) {
    final Value id = message.args[0];
    final Value label = message.args[1];
    newDiagram(id.strValue(), label.strValue());
  }

  private void newDiagram(final String id, final String label) {
    runOnDisplay(new Runnable() {
      public void run() {
        Diagram diagram = new Diagram(id, tabFolder);
        CTabItem item = new CTabItem(tabFolder, SWT.BORDER);
        item.setControl(diagram.getContainer());
        item.setText(label);
        tabs.put(id, item);
        diagrams.put(id, diagram);
        tabFolder.setSelection(item);
      }
    });
  }

  private void newEdge(final Message message) {
    String parentId = message.args[0].strValue();
    String id = message.args[1].strValue();
    String sourceId = message.args[2].strValue();
    String targetId = message.args[3].strValue();
    int refx = message.args[4].intValue;
    int refy = message.args[5].intValue;
    int sourceHead = message.args[6].intValue;
    int targetHead = message.args[7].intValue;
    int lineStyle = message.args[8].intValue;
    int red = message.args[9].intValue;
    int green = message.args[10].intValue;
    int blue = message.args[11].intValue;
    newEdge(parentId, id, sourceId, targetId, refx, refy, sourceHead, targetHead, lineStyle, red, green, blue);
  }

  private void newEdge(String parentId, String id, String sourceId, String targetId, int refx, int refy, int sourceHead, int targetHead, int lineStyle, int red, int green, int blue) {
    for (Diagram diagram : diagrams.values()) {
      if (diagram.getId().equals(parentId)) {
        diagram.newEdge(id, sourceId, targetId, refx, refy, sourceHead, targetHead, lineStyle, red, green, blue);
      }
    }
  }

  private void newGroup(final String diagramId, final String name) {
    if (diagrams.containsKey(diagramId)) {
      runOnDisplay(new Runnable() {
        public void run() {
          Diagram diagram = diagrams.get(diagramId);
          diagram.newGroup(name);
        }
      });
    } else System.out.println("cannot find diagram " + diagramId);
  }

  private void newLabel(final Message message) {
    String parentId = message.args[0].strValue();
    String id = message.args[1].strValue();
    String text = message.args[2].strValue();
    String position = message.args[3].strValue();
    int x = message.args[4].intValue;
    int y = message.args[5].intValue;
    Boolean editable = message.args[6].boolValue;
    Boolean underline = message.args[7].boolValue;
    Boolean condense = message.args[8].boolValue;
    int red = message.args[9].intValue;
    int green = message.args[10].intValue;
    int blue = message.args[11].intValue;
    String font = message.args[12].strValue();
    newLabel(parentId, id, text, position, x, y, editable, underline, condense, red, green, blue, font);
  }

  private void newLabel(String parentId, String id, String text, String position, int x, int y, Boolean editable, Boolean underline, Boolean condense, int red, int green, int blue, String font) {
    for (Diagram diagram : diagrams.values()) {
      for (Edge edge : diagram.getEdges().values()) {
        if (edge.getId().equals(parentId)) {
          edge.addLabel(id, text, position, x, y, editable, underline, condense, red, green, blue, font);
        }
      }
    }
  }

  private void newNode(Message message) {
    Value parentId = message.args[0];
    Value id = message.args[1];
    Value x = message.args[2];
    Value y = message.args[3];
    Value width = message.args[4];
    Value height = message.args[5];
    Value selectable = message.args[6];
    newNode(parentId.strValue(), id.strValue(), x.intValue, y.intValue, width.intValue, height.intValue, selectable.boolValue);
  }

  protected void newNode(String type, String id, int x, int y) {
    Message m = getHandler().newMessage("newNode", 4);
    m.args[0] = new Value(type);
    m.args[1] = new Value(id);
    m.args[2] = new Value(x);
    m.args[3] = new Value(y);
    getHandler().raiseEvent(m);
  }

  private void newNode(String parentId, String id, int x, int y, int width, int height, boolean selectable) {
    if (diagrams.containsKey(parentId)) {
      Diagram diagram = diagrams.get(parentId);
      diagram.newNode(id, x, y, width, height, selectable);
    } else System.out.println("cannot find diagram " + parentId);
  }

  private void newPort(Message message) {
    Value parentId = message.args[0];
    Value id = message.args[1];
    Value x = message.args[2];
    Value y = message.args[3];
    Value width = message.args[4];
    Value height = message.args[5];
    newPort(parentId.strValue(), id.strValue(), x.intValue, y.intValue, width.intValue, height.intValue);
  }

  private void newPort(String parentId, String id, int x, int y, int width, int height) {
    for (Diagram diagram : diagrams.values()) {
      diagram.newPort(parentId, id, x, y, width, height);
    }
  }

  private void newText(Message message) {
    final String parentId = message.args[0].strValue();
    final String id = message.args[1].strValue();
    final String text = message.args[2].strValue();
    final int x = message.args[3].intValue;
    final int y = message.args[4].intValue;
    final boolean editable = message.args[5].boolValue;
    final boolean underline = message.args[6].boolValue;
    final boolean italicise = message.args[7].boolValue;
    final int red = message.args[8].intValue;
    final int green = message.args[9].intValue;
    final int blue = message.args[10].intValue;
    newText(parentId, id, text, x, y, editable, underline, italicise, red, green, blue);
  }

  private void newText(final String parentId, final String id, final String text, final int x, final int y, final boolean editable, final boolean underline, final boolean italicise, final int red, final int green, final int blue) {
    for (Diagram diagram : diagrams.values()) {
      diagram.newText(parentId, id, text, x, y, editable, underline, italicise, red, green, blue);
    }
  }

  private void newTool(Message message) {
    final Value diagramId = message.args[0];
    final Value groupId = message.args[1];
    final Value label = message.args[2];
    final Value toolId = message.args[3];
    final Value isEdge = message.args[4];
    final Value icon = message.args[5];
    newTool(diagramId.strValue(), groupId.strValue(), label.strValue(), toolId.strValue(), isEdge.boolValue, icon.strValue());
  }

  private void newTool(final String diagramId, final String groupId, final String label, final String toolId, final boolean isEdge, final String icon) {
    if (diagrams.containsKey(diagramId)) {
      runOnDisplay(new Runnable() {
        public void run() {
          Diagram diagram = diagrams.get(diagramId);
          diagram.newTool(groupId, label, toolId, isEdge, icon);
        }
      });
    } else System.out.println("cannot find diagram " + diagramId);
  }

  private void newToolGroup(Message message) {
    final Value diagramId = message.args[0];
    final Value name = message.args[1];
    newGroup(diagramId.strValue(), name.strValue());
  }

  private void newWaypoint(final Message message) {
    String parentId = message.args[0].strValue();
    String id = message.args[1].strValue();
    int index = message.args[2].intValue;
    int x = message.args[3].intValue;
    int y = message.args[4].intValue;
    newWaypoint(parentId, id, index, x, y);
  }

  private void newWaypoint(String parentId, String id, int index, int x, int y) {
    for (Diagram diagram : diagrams.values()) {
      diagram.newWaypoint(parentId, id, index, x, y);
    }
  }

  public boolean processMessage(Message message) {
    System.out.println(this + " <- " + message);
    return false;
  }

  private void resize(Message message) {
    final Value id = message.args[0];
    final Value width = message.args[1];
    final Value height = message.args[2];
    runOnDisplay(new Runnable() {
      public void run() {
        for (Diagram diagram : diagrams.values())
          diagram.resize(id.strValue(), width.intValue, height.intValue);
      }
    });
  }

  public void sendMessage(final Message message) {
    // System.err.println(message);
    if (message.hasName("newDiagram"))
      newDiagram(message);
    else if (message.hasName("newToolGroup"))
      newToolGroup(message);
    else if (message.hasName("newTool"))
      newTool(message);
    else if (message.hasName("newNode"))
      newNode(message);
    else if (message.hasName("newPort"))
      newPort(message);
    else if (message.hasName("newBox"))
      newBox(message);
    else if (message.hasName("newText"))
      newText(message);
    else if (message.hasName("resize"))
      resize(message);
    else if (message.hasName("move"))
      move(message);
    else if (message.hasName("editText"))
      editText(message);
    else if (message.hasName("setText"))
      setText(message);
    else if (message.hasName("setName"))
      setName(message);
    else if (message.hasName("globalRenderOff"))
      globalRenderOff();
    else if (message.hasName("globalRenderOn"))
      globalRenderOn();
    else if (message.hasName("startRender"))
      startRender(message);
    else if (message.hasName("stopRender"))
      stopRender(message);
    else if (message.hasName("setFocus"))
      setFocus(message);
    else if (message.hasName("newEdge"))
      newEdge(message);
    else if (message.hasName("setRefPoint"))
      setRefPoint(message);
    else if (message.hasName("setEdgeStyle"))
      setEdgeStyle(message);
    else if (message.hasName("setColor"))
      setEdgeColor(message);
    else if (message.hasName("newEdgeText"))
      newLabel(message);
    else if (message.hasName("newWaypoint"))
      newWaypoint(message);
    else if (message.hasName("delete"))
      delete(message);
    else if (message.hasName("setEdgeTarget"))
      setEdgeTarget(message);
    else if (message.hasName("setEdgeSource"))
      setEdgeSource(message);
    else super.sendMessage(message);
  }

  private void setEdgeColor(final Message message) {
    runOnDisplay(new Runnable() {
      public void run() {
        Value id = message.args[0];
        Value red = message.args[1];
        Value green = message.args[2];
        Value blue = message.args[3];
        for (Diagram diagram : diagrams.values()) {
          for (Edge edge : diagram.getEdges().values()) {
            if (edge.getId().equals(id.strValue())) {
              edge.setRed(red.intValue);
              edge.setRed(green.intValue);
              edge.setRed(blue.intValue);
            }
          }
        }
      }
    });
  }

  private void setEdgeSource(Message message) {
    String edgeId = message.args[0].strValue();
    String portId = message.args[1].strValue();
    setEdgeSource(edgeId, portId);
  }

  private void setEdgeSource(final String edgeId, final String portId) {
    runOnDisplay(new Runnable() {
      public void run() {
        for (Diagram d : diagrams.values()) {
          d.setEdgeSource(edgeId, portId);
        }
      }
    });
  }

  private void setEdgeStyle(final Message message) {
    runOnDisplay(new Runnable() {
      public void run() {
        Value id = message.args[0];
        Value style = message.args[1];
        for (Diagram diagram : diagrams.values()) {
          for (Edge edge : diagram.getEdges().values()) {
            if (edge.getId().equals(id.strValue())) {
              edge.setLineStyle(style.intValue);
            }
          }
        }
      }
    });
  }

  private void setEdgeTarget(Message message) {
    String edgeId = message.args[0].strValue();
    String portId = message.args[1].strValue();
    setEdgeTarget(edgeId, portId);
  }

  private void setEdgeTarget(final String edgeId, final String portId) {
    runOnDisplay(new Runnable() {
      public void run() {
        for (Diagram d : diagrams.values()) {
          d.setEdgeTarget(edgeId, portId);
        }
      }
    });
  }

  private void setFocus(final Message message) {
    runOnDisplay(new Runnable() {
      public void run() {
        Value id = message.args[0];
        for (String tabId : tabs.keySet()) {
          if (tabId.equals(id.strValue())) tabFolder.setSelection(tabs.get(tabId));
        }
      }
    });
  }

  private void setName(Message message) {
    final Value id = message.args[0];
    final Value name = message.args[1];
    runOnDisplay(new Runnable() {
      public void run() {
        for (String diagramId : tabs.keySet())
          if (id.strValue().equals(diagramId)) {
            tabs.get(diagramId).setText(name.strValue());
          }
      }
    });
  }

  private void setRefPoint(final Message message) {
    runOnDisplay(new Runnable() {
      public void run() {
        Value id = message.args[0];
        Value refx = message.args[1];
        Value refy = message.args[2];
        for (Diagram diagram : diagrams.values()) {
          for (Edge edge : diagram.getEdges().values()) {
            if (edge.getId().equals(id.strValue())) {
              edge.setRefx(refx.intValue);
              edge.setRefy(refy.intValue);
            }
          }
        }
      }
    });
  }

  private void setText(Message message) {
    final Value id = message.args[0];
    final Value text = message.args[1];
    for (Diagram diagram : diagrams.values())
      diagram.setText(id.strValue(), text.strValue());
  }

  private void startRender(final Message message) {
    runOnDisplay(new Runnable() {
      public void run() {
        Value id = message.args[0];
        for (Diagram diagram : diagrams.values())
          if (diagram.getId().equals(id.strValue())) diagram.renderOn();
      }
    });
  }

  private void stopRender(Message message) {
    Value id = message.args[0];
    for (Diagram diagram : diagrams.values())
      if (diagram.getId().equals(id.strValue())) diagram.renderOff();
  }

  public Point textDimension(String text, Font font) {
    Text t = new Text(tabFolder, SWT.NONE);
    t.setFont(diagramFont);
    GC gc = new GC(t);
    Point extent = gc.textExtent(text);
    gc.dispose();
    t.dispose();
    return extent;
  }

  public void writeXML(PrintStream out) {
    out.print("<Diagrams>");
    for (Diagram diagram : diagrams.values())
      diagram.writeXML(tabs.get(diagram.getId()).getText(), out);
    out.print("</Diagrams>");
  }
}