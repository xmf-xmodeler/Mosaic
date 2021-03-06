package com.ceteva.text.htmlviewer;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

import uk.ac.mdx.xmf.swt.client.EventHandler;
import xos.Message;
import xos.Value;

// TODO: Auto-generated Javadoc
/**
 * The Class HTMLViewer.
 */
public class HTMLViewer {

	/** The model. */
	HTMLViewerModel model;
	
	/** The identity. */
	String identity = "";
	
	/** The ignore url. */
	boolean ignoreURL = true;
	
	/** The browser. */
	Browser browser = null;
	
	/** The handler. */
	EventHandler handler = null;
	
	/** The tooltip. */
	String tooltip = "";

	/**
	 * Delete.
	 */
	public void delete() {
		// TextPlugin textManager = TextPlugin.getDefault();
		// IWorkbenchPage page =
		// textManager.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		// page.closeEditor(this,false);
	}

	/**
	 * Do save.
	 */
	public void doSave() {
	}

	/**
	 * Do save as.
	 */
	public void doSaveAs() {
	}

	/**
	 * Independent type.
	 *
	 * @param event the event
	 * @return true, if successful
	 */
	public boolean independentType(LocationEvent event) {
		if (event.location.endsWith("php")) {
			ignoreURL = true;
			return true;
		}
		return false;
	}

	// public void init(IEditorSite site, IEditorInput iInput)
	// throws PartInitException {
	// this.setSite(site);
	// this.setInput(iInput);
	// if (iInput instanceof TextEditorInput) {
	// TextEditorInput input = (TextEditorInput) iInput;
	// identity = "5";
	// model = new HTMLViewerModel(identity, null, this);
	// }
	// }

	/**
	 * Inits the.
	 *
	 * @param identity the identity
	 */
	public void init(String identity) {
        this.identity=identity;
		model = new HTMLViewerModel(identity, null, this);
	}

	/**
	 * Checks if is dirty.
	 *
	 * @return true, if is dirty
	 */
	public boolean isDirty() {
		return false;
	}

	/**
	 * Checks if is save as allowed.
	 *
	 * @return true, if is save as allowed
	 */
	public boolean isSaveAsAllowed() {
		return false;
	}

	/**
	 * Close.
	 *
	 * @param save the save
	 */
	public void close(final boolean save) {
		// Display display = getSite().getShell().getDisplay();
		// display.asyncExec(new Runnable() {
		// public void run() {
		// getSite().getPage().closeEditor(HTMLViewer.this, false);
		// }
		// });
	}

	/**
	 * Creates the part control.
	 *
	 * @param parent the parent
	 */
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		browser = new Browser(parent, SWT.NONE);
		LocationListener locationListener = new LocationListener() {
			public void changed(LocationEvent event) {
				ignoreURL = true;
			}

			public void changing(LocationEvent event) {
				if (ignoreURL && !independentType(event)) {
					try {
						String url = URLDecoder.decode(event.location, "UTF-8");
						Message m = handler.newMessage("urlRequest", 2);
						Value v1 = new Value(identity);
						Value v2 = new Value(url);
						m.args[0] = v1;
						m.args[1] = v2;
						handler.raiseEvent(m);
						event.doit = false;
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};
		browser.addLocationListener(locationListener);
	}

	/**
	 * Dispose.
	 */
	public void dispose() {
		Message m = handler.newMessage("textClosed", 1);
		Value v1 = new Value(identity);
		m.args[0] = v1;
		handler.raiseEvent(m);
		// MenuBuilder.dispose(getSite());
		model.dispose();
		// super.dispose();
	}

	/**
	 * Sets the event handler.
	 *
	 * @param handler the new event handler
	 */
	public void setEventHandler(EventHandler handler) {
		this.handler = handler;
		model.setEventHandler(handler);
	}

	/**
	 * Sets the focus internal.
	 */
	public void setFocusInternal() {
		// PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
		// .activate(this);
	}

	/**
	 * Sets the html.
	 *
	 * @param html the new html
	 */
	public void setHTML(String html) {
		ignoreURL = false;
		browser.setText(html);
	}

	/**
	 * Sets the url.
	 *
	 * @param url the new url
	 */
	public void setURL(String url) {
		if (browser != null) {
			if (!url.startsWith("<")) {
				if (url.startsWith("help://")) {
					// open the URL in help system
					url = url.replaceAll("help://", "");
					if (url.equals("/"))
						PlatformUI.getWorkbench().getHelpSystem().displayHelp();
					else
						PlatformUI.getWorkbench().getHelpSystem()
								.displayHelpResource("/com.ceteva.help/" + url);
				} else {
					ignoreURL = false;
					browser.setUrl(url);
				}
			} else {
				ignoreURL = false;
				setHTML(url);
			}
		}
	}

	/**
	 * Sets the name.
	 *
	 * @param title the new name
	 */
	public void setName(String title) {
		// setPartName(title);
	}

	/**
	 * Sets the tool tip.
	 *
	 * @param tooltip the new tool tip
	 */
	public void setToolTip(String tooltip) {
		this.tooltip = tooltip;
		// this.setTitleToolTip(tooltip);
	}

	/**
	 * Gets the title tool tip.
	 *
	 * @return the title tool tip
	 */
	public String getTitleToolTip() {
		return tooltip;
	}

	/**
	 * Sets the focus.
	 */
	public void setFocus() {
	}

	/**
	 * Sets the browser.
	 *
	 * @param browser2 the new browser
	 */
	public void setBrowser(Browser browser2) {
		browser = browser2;
	}
}