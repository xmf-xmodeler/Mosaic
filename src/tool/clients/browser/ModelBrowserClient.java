package tool.clients.browser;

import java.io.PrintStream;
import java.util.Hashtable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import tool.clients.Client;
import tool.clients.menus.MenuClient;
import tool.xmodeler.XModeler;
import xos.Message;
import xos.Value;

public class ModelBrowserClient extends Client implements MouseListener, Listener, KeyListener {

  public static CTabFolder getTabFolder() {
    return tabFolder;
  }

  public static void start(CTabFolder tabFolder, int style) {
    ModelBrowserClient.tabFolder = tabFolder;
  }

  public static ModelBrowserClient theClient() {
    return theClient;
  }

  static Font                        labelFont    = new Font(XModeler.getXModeler().getDisplay(), new FontData("Courier New", 12, SWT.NONE));

  final static int                   RIGHT_BUTTON = 3;
  static CTabFolder                  tabFolder;
  static ModelBrowserClient          theClient;

  static Hashtable<String, TreeItem> items        = new Hashtable<String, TreeItem>();
  static Hashtable<String, CTabItem> tabs         = new Hashtable<String, CTabItem>();
  static Hashtable<String, Tree>     trees        = new Hashtable<String, Tree>();
  static Hashtable<String, String>   images       = new Hashtable<String, String>();
  static Hashtable<Tree, TreeItem>   selections   = new Hashtable<Tree, TreeItem>();

  public ModelBrowserClient() {
    super("com.ceteva.modelBrowser");
    theClient = this;
  }

  private void addNodeWithIcon(Message message) {
    Value parentId = message.args[0];
    Value nodeId = message.args[1];
    Value text = message.args[2];
    Value editable = message.args[3];
    Value icon = message.args[4];
    int index = -1;
    if (message.arity == 6) index = message.args[5].intValue;
    if (trees.containsKey(parentId.strValue()))
      addRootNodeWithIcon(parentId.strValue(), nodeId.strValue(), text.strValue(), editable.boolValue, false, icon.strValue(), index);
    else addNodeWithIcon(parentId.strValue(), nodeId.strValue(), text.strValue(), editable.boolValue, false, icon.strValue(), index);
  }

  private void addNodeWithIcon(final String parentId, final String nodeId, final String text, boolean editable, final boolean expanded, final String icon, final int index) {
    if (items.containsKey(parentId)) {
      runOnDisplay(new Runnable() {
        public void run() {
          TreeItem parent = items.get(parentId);
          String iconFile = "icons/" + icon + ".gif";
          ImageData data = new ImageData(iconFile);
          Image image = new Image(XModeler.getXModeler().getDisplay(), data);
          TreeItem item = new TreeItem(parent, SWT.NONE, (index == -1) ? parent.getItemCount() : index);
          images.put(nodeId, icon);
          items.put(nodeId, item);
          item.setText(text);
          item.setImage(image);
          item.setExpanded(expanded);
          item.setFont(labelFont);
        }
      });
    } else System.out.println("Cannot find node " + parentId);
  }

  private void addRootNodeWithIcon(final String parentId, final String nodeId, final String text, boolean editable, final boolean expanded, final String icon, final int index) {
    Display.getDefault().syncExec(new Runnable() {
      public void run() {
        String iconFile = "icons/" + icon + ".gif";
        ImageData data = new ImageData(iconFile);
        Image image = new Image(XModeler.getXModeler().getDisplay(), data);
        Tree tree = trees.get(parentId);
        TreeItem item = new TreeItem(tree, SWT.NONE, (index == -1) ? tree.getItemCount() : index);
        images.put(nodeId, icon);
        items.put(nodeId, item);
        item.setText(text);
        item.setImage(image);
        item.setExpanded(expanded);
        item.setFont(labelFont);
      }
    });
  }

  private void addTree(final String id, final String name) {
    runOnDisplay(new Runnable() {
      public void run() {
        CTabItem tabItem = new CTabItem(tabFolder, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        tabItem.setText(name);
        tabs.put(id, tabItem);
        Tree tree = new Tree(tabFolder, SWT.VIRTUAL);
        tree.addKeyListener(ModelBrowserClient.this);
        tabItem.setControl(tree);
        trees.put(id, tree);
        tree.addMouseListener(ModelBrowserClient.this);
        tree.addListener(SWT.Expand, ModelBrowserClient.this);
        tree.addListener(SWT.Selection, ModelBrowserClient.this);
        tabFolder.setSelection(tabItem);
      }
    });
  }

  public void editText(final Tree tree, final TreeItem item) {

    // Called to create an editable area over the tree item and
    // raise a textChanged event if the text is modified...

    final Color black = XModeler.getXModeler().getDisplay().getSystemColor(SWT.COLOR_BLACK);
    final TreeItem[] lastItem = new TreeItem[1];
    final TreeEditor editor = new TreeEditor(tree);
    boolean showBorder = true;
    final Composite composite = new Composite(tree, SWT.NONE);
    if (showBorder) composite.setBackground(black);
    final Text text = new Text(composite, SWT.NONE);
    final int inset = showBorder ? 1 : 0;
    composite.addListener(SWT.Resize, new Listener() {
      public void handleEvent(Event e) {
        Rectangle rect = composite.getClientArea();
        text.setBounds(rect.x + inset, rect.y + inset, rect.width - inset * 2, rect.height - inset * 2);
      }
    });
    Listener textListener = new Listener() {
      public void handleEvent(final Event e) {
        switch (e.type) {
        case SWT.FocusOut:
          updateText(item, text.getText());
          composite.dispose();
          break;
        case SWT.Verify:
          String newText = text.getText();
          String leftText = newText.substring(0, e.start);
          String rightText = newText.substring(e.end, newText.length());
          GC gc = new GC(text);
          Point size = gc.textExtent(leftText + e.text + rightText);
          gc.dispose();
          size = text.computeSize(size.x, SWT.DEFAULT);
          editor.horizontalAlignment = SWT.LEFT;
          Rectangle itemRect = item.getBounds(),
          rect = tree.getClientArea();
          editor.minimumWidth = Math.max(size.x, itemRect.width) + inset * 2;
          int left = itemRect.x,
          right = rect.x + rect.width;
          editor.minimumWidth = Math.min(editor.minimumWidth, right - left);
          editor.minimumHeight = size.y + inset * 2;
          editor.layout();
          break;
        case SWT.Traverse:
          switch (e.detail) {
          case SWT.TRAVERSE_RETURN:
            updateText(item, text.getText());
            // fall through.
          case SWT.TRAVERSE_ESCAPE:
            composite.dispose();
            e.doit = false;
          }
          break;
        }
      }
    };
    text.addListener(SWT.FocusOut, textListener);
    text.addListener(SWT.Traverse, textListener);
    text.addListener(SWT.Verify, textListener);
    editor.setEditor(composite, item);
    text.setText(item.getText());
    text.selectAll();
    text.setFocus();
  }

  private void expand(final String id) {
    runOnDisplay(new Runnable() {
      public void run() {
        items.get(id).setExpanded(true);
        tabFolder.redraw();
      }
    });
  }

  private String getId(Tree tree) {
    for (String id : trees.keySet())
      if (trees.get(id) == tree) return id;
    return null;
  }

  private String getId(TreeItem item) {
    for (String id : items.keySet())
      if (items.get(id) == item) return id;
    return null;
  }

  public void handleEvent(Event event) {
    if (event.type == SWT.Expand) {
      Message m = getHandler().newMessage("expanded", 1);
      TreeItem item = (TreeItem) event.item;
      Value v1 = null;
      for (String id : items.keySet())
        if (items.get(id) == item) v1 = new Value(id);
      m.args[0] = v1;
      getHandler().raiseEvent(m);
    }
    if (event.type == SWT.Selection) {
      Tree tree = (Tree) event.widget;
      TreeItem item = selections.get(tree);
      if (tree.getSelectionCount() == 1) {
        if (item != null && tree.getSelection()[0] == item) {
          editText(tree, item);
        } else selections.put(tree, tree.getSelection()[0]);
      }
    }
  }

  private void inflateTree(Node treeNode) {
    final String id = XModeler.attributeValue(treeNode, "id");
    boolean selected = XModeler.attributeValue(treeNode, "selected").equals("true");
    NodeList roots = treeNode.getChildNodes();
    if (roots.getLength() == 1) {
      Node root = roots.item(0);
      String rootId = XModeler.attributeValue(root, "id");
      String text = XModeler.attributeValue(root, "text");
      String image = XModeler.attributeValue(root, "image");
      boolean expanded = XModeler.attributeValue(root, "expanded").equals("true");
      addTree(id, text);
      addRootNodeWithIcon(id, rootId, text, true, expanded, image, 0);
      inflateTreeItem(root);
      if (selected) select(id);
      if (expanded) expand(rootId);
    } else System.out.println("expecting to inflate a tree with 1 root node.");
  }

  private void inflateTreeItem(Node node) {
    String id = XModeler.attributeValue(node, "id");
    NodeList children = node.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node child = children.item(i);
      String childId = XModeler.attributeValue(child, "id");
      String text = XModeler.attributeValue(child, "text");
      String image = XModeler.attributeValue(child, "image");
      boolean expanded = XModeler.attributeValue(child, "expanded").equals("true");
      addNodeWithIcon(id, childId, text, true, expanded, image, i);
      inflateTreeItem(child);
      if (expanded) expand(childId);
    }
  }

  public void inflateXML(Document doc) {
    NodeList modelBrowsers = doc.getElementsByTagName("ModelBrowser");
    if (modelBrowsers.getLength() == 1) {
      Node modelBrowser = modelBrowsers.item(0);
      NodeList treeNodes = modelBrowser.getChildNodes();
      for (int i = 0; i < treeNodes.getLength(); i++) {
        Node treeNode = treeNodes.item(i);
        inflateTree(treeNode);
      }
    } else System.out.println("expecting exactly 1 model browser got: " + modelBrowsers.getLength());
  }

  private boolean isCommand(MouseEvent event) {
    return (event.stateMask & SWT.COMMAND) != 0;
  }

  private boolean isRightClick(MouseEvent event) {
    return event.button == RIGHT_BUTTON;
  }

  public String itemId(TreeItem item) {
    for (String id : items.keySet())
      if (items.get(id) == item) return id;
    return null;
  }

  public void keyPressed(KeyEvent e) {
  }

  public void keyReleased(KeyEvent event) {
  }

  public void mouseDoubleClick(MouseEvent event) {
    Tree tree = (Tree) event.widget;
    if (tree.getSelectionCount() == 1) {
      TreeItem item = tree.getSelection()[0];
      String id = itemId(item);
      Message m = getHandler().newMessage("doubleSelected", 1);
      m.args[0] = new Value(id);
      getHandler().raiseEvent(m);
    }
  }

  public void mouseDown(MouseEvent event) {
    if (isRightClick(event) || isCommand(event)) {
      Tree tree = (Tree) event.widget;
      if (tree.getSelectionCount() == 1) {
        TreeItem item = tree.getSelection()[0];
        for (String id : items.keySet())
          if (items.get(id) == item) MenuClient.popup(id, event.x, event.y);
      }
    }
  }

  public void mouseUp(MouseEvent event) {
  }

  private void newModelBrowser(Message message) {
    final Value id = message.args[0];
    Value clientName = message.args[1];
    final Value name = message.args[2];
    if (clientName.strValue().equals("com.ceteva.browser")) {
      addTree(id.strValue(), name.strValue());
    }
  }

  public boolean processMessage(Message message) {
    System.out.println(this + " <- " + message);
    return false;
  }

  private void removeNode(Message message) {
    final Value id = message.args[0];
    if (items.containsKey(id.strValue())) {
      Display.getDefault().syncExec(new Runnable() {
        public void run() {
          TreeItem item = items.get(id.strValue());
          item.dispose();
        }
      });
    } else System.out.println("cannnot remove node with id " + id);
  }

  private void select(final String id) {
    runOnDisplay(new Runnable() {
      public void run() {
        tabFolder.setSelection(tabs.get(id));
      }
    });
  }

  private void selectNode(Message message) {
    final Value id = message.args[0];
    Value selected = message.args[1];
    if (selected.boolValue && items.containsKey(id.strValue())) {
      Display.getDefault().syncExec(new Runnable() {
        public void run() {
          TreeItem item = items.get(id.strValue());
          for (Tree tree : trees.values()) {
            for (TreeItem treeItem : tree.getItems()) {
              if (treeItem == item) {
                tree.select(treeItem);
              }
            }
          }
        }
      });
    }
  }

  public void sendMessage(final Message message) {
    // System.err.println(message);
    if (message.hasName("newModelBrowser"))
      newModelBrowser(message);
    else if (message.hasName("addNodeWithIcon"))
      addNodeWithIcon(message);
    else if (message.hasName("removeNode"))
      removeNode(message);
    else if (message.hasName("setText"))
      setText(message);
    else if (message.hasName("setVisible"))
      setVisible(message);
    else if (message.hasName("selectNode"))
      selectNode(message);
    else if (message.hasName("setFocus"))
      setFocus(message);
    else if (message.hasName("setToolTipText"))
      setTooltipText(message);
    else super.sendMessage(message);
  }

  private void setFocus(Message message) {
    final Value id = message.args[0];
    if (tabs.containsKey(id.strValue())) {
      Display.getDefault().syncExec(new Runnable() {
        public void run() {
          CTabItem tab = tabs.get(id.strValue());
          tabFolder.setFocus();
          tabFolder.setSelection(tab);
        }
      });
    } else System.out.println("cannot find tab " + id);
  }

  private void setText(Message message) {
    final Value id = message.args[0];
    final Value text = message.args[1];
    if (items.containsKey(id.strValue())) {
      Display.getDefault().syncExec(new Runnable() {
        public void run() {
          TreeItem item = items.get(id.strValue());
          item.setText(text.strValue());
        }
      });
    } else System.out.println("cannot find tree item " + id.strValue());
  }

  private void setTooltipText(Message message) {
    // This is not currently supported.
  }

  private void setVisible(Message message) {
    final Value id = message.args[0];
    if (tabs.containsKey(id.strValue())) {
      Display.getDefault().syncExec(new Runnable() {
        public void run() {
          CTabItem item = tabs.get(id.strValue());
          tabFolder.setSelection(item);
        }
      });
    } else System.out.println("cannot select tab " + id);
  }

  public void updateText(TreeItem item, String text) {

    // Change the text associated with the label and raise the event...

    if (!item.getText().equals(text)) {
      item.setText(text);
      Message m = getHandler().newMessage("textChanged", 2);
      Value v1 = null;
      for (String id : items.keySet())
        if (items.get(id) == item) v1 = new Value(id);
      m.args[0] = v1;
      Value v2 = new Value(item.getText());
      m.args[1] = v2;
      getHandler().raiseEvent(m);
    }
  }

  public void writeXML(PrintStream out) {
    out.print("<ModelBrowser>");
    for (String id : trees.keySet()) {
      Tree tree = trees.get(id);
      CTabItem tab = tabs.get(id);
      out.print("<Tree id='" + id + "' selected='" + (tabFolder.getSelection() == tab) + "'>");
      writeXMLTreeItems(tree.getItems(), out);
      out.print("</Tree>");
    }
    out.print("</ModelBrowser>");
  }

  private void writeXMLTreeItems(TreeItem[] children, PrintStream out) {
    for (TreeItem item : children) {
      String id = null;
      for (String itemId : items.keySet()) {
        if (items.get(itemId) == item) id = itemId;
      }
      if (id == null) System.out.println("error: cannot find tree item " + item);
      String icon = images.get(id);
      out.print("<Item id='" + id + "' text='" + XModeler.encodeXmlAttribute(item.getText()) + "' image='" + icon + "' expanded='" + item.getExpanded() + "'>");
      writeXMLTreeItems(item.getItems(), out);
      out.print("</Item>");
    }
  }
}