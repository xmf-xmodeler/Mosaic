package tool.clients.editors;

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Listener;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.widgets.Shell;

import tool.xmodeler.XModeler;

public class BrowserResizeListener implements CTabFolder2Listener {
  
  
public void close(CTabFolderEvent arg0) {
  }

  public void maximize(CTabFolderEvent event) {
    XModeler.maximiseBrowser();
  }

  public void minimize(CTabFolderEvent event) {
    XModeler.minimiseBrowser();
  }

  public void restore(CTabFolderEvent arg0) {
    
  }

  public void showList(CTabFolderEvent arg0) {
    
  }

}
