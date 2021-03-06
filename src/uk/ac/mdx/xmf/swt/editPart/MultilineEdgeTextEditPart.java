package uk.ac.mdx.xmf.swt.editPart;

import java.beans.PropertyChangeEvent;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.tools.DirectEditManager;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.graphics.RGB;

import uk.ac.mdx.xmf.swt.diagram.tracker.EdgeTextDragTracker;
import uk.ac.mdx.xmf.swt.figure.EdgeFigure;
import uk.ac.mdx.xmf.swt.figure.MultilineEdgeTextFigure;
import uk.ac.mdx.xmf.swt.misc.ColorManager;
import uk.ac.mdx.xmf.swt.model.MultilineEdgeText;

// TODO: Auto-generated Javadoc
/**
 * The Class MultilineEdgeTextEditPart.
 */
public class MultilineEdgeTextEditPart extends CommandEventEditPart {

	/** The manager. */
	private DirectEditManager manager = null;
	
	/** The model. */
	private MultilineEdgeText model = null;

	/* (non-Javadoc)
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
	 */
	protected IFigure createFigure() {
		model = (MultilineEdgeText) getModel();
		String text = model.getText();
		Point position = model.getLocation();
		RGB forecolor = getForeColor();
		MultilineEdgeTextFigure mulilinetext = new MultilineEdgeTextFigure(
				position, forecolor);
		mulilinetext.setText(text);
		return mulilinetext;
	}

	/**
	 * Gets the fore color.
	 *
	 * @return the fore color
	 */
	public RGB getForeColor() {
		RGB foreColor = model.getColor();
		if (foreColor != null)
			return foreColor;
		return null;
		// IPreferenceStore preferences =
		// DiagramPlugin.getDefault().getPreferenceStore();
		// return
		// PreferenceConverter.getColor(preferences,IPreferenceConstants.UNSELECTED_FONT_COLOR);
	}

	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		String prop = evt.getPropertyName();
		if (prop.equals("startRender"))
			refresh();
		if (prop.equals("locationSize"))
			refreshVisuals();
		if (prop.equals("textChanged"))
			refreshVisuals();
		if (prop.equals("color"))
			refreshColor();
		if (prop.equals("visibilityChange")) {
			refreshVisuals();
			this.getViewer().deselectAll();
		}
	}

	/**
	 * Refresh color.
	 */
	public void refreshColor() {
		getFigure().setForegroundColor(ColorManager.getColor(getForeColor()));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.editparts.AbstractEditPart#refreshVisuals()
	 */
	protected void refreshVisuals() {
		MultilineEdgeTextFigure figure = (MultilineEdgeTextFigure) getFigure();
		String string = model.getText();
		Point offset = model.getLocation();
		String position = model.getPosition();
		figure.setText(string);
		figure.setFont(model.getFont());
		figure.setVisible(!model.hidden());
		EdgeEditPart parent = (EdgeEditPart) getParent();
		MultilineEdgeTextConstraint constraint = new MultilineEdgeTextConstraint(
				this, string, parent.getFigure(), parent, position, offset);
		parent.setLayoutConstraint(this, getFigure(), constraint);
		refreshColor();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
	 */
	protected void createEditPolicies() {
		// installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE,
		// new MultilineEdgeTextMovePolicy());
		// installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE,
		// new MultilineEdgeTextSelectionPolicy());
		// if (model.isEditable())
		// installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE,
		// new MultilineEdgeTextEditPolicy());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.editparts.AbstractEditPart#performRequest(org.eclipse.gef.Request)
	 */
	public void performRequest(Request request) {
		Object type = request.getType();
		if (type == RequestConstants.REQ_DIRECT_EDIT
				|| type == RequestConstants.REQ_OPEN) {
			if (model.isEditable())
				performDirectEdit();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#getDragTracker(org.eclipse.gef.Request)
	 */
	public DragTracker getDragTracker(Request request) {
		return new EdgeTextDragTracker(this, (EdgeEditPart) getParent());
	}

	/**
	 * Gets the edge edit part.
	 *
	 * @return the edge edit part
	 */
	public EdgeEditPart getEdgeEditPart() {
		return (EdgeEditPart) getParent();
	}

	/**
	 * Gets the edge position.
	 *
	 * @return the edge position
	 */
	public Point getEdgePosition() {
		EdgeEditPart parent = (EdgeEditPart) getParent();
		EdgeFigure edgeFigure = (EdgeFigure) parent.getFigure();
		String position = model.getPosition();
		if (position.equals("start"))
			return edgeFigure.getStart();
		else if (position.equals("end"))
			return edgeFigure.getEnd();
		else
			return edgeFigure.getPoints().getMidpoint();
	}

	/**
	 * Perform direct edit.
	 */
	private void performDirectEdit() {
		if (manager == null)
			manager = new MultilineEdgeEditManager(this, TextCellEditor.class,
					new MultilineEdgeCellEditorLocator(
							(MultilineEdgeTextFigure) getFigure()));
		manager.show();
	}

	/* (non-Javadoc)
	 * @see uk.ac.mdx.xmf.swt.editPart.CommandEventEditPart#preferenceUpdate()
	 */
	public void preferenceUpdate() {
		refreshColor();
		MultilineEdgeTextFigure figure = (MultilineEdgeTextFigure) getFigure();
		figure.preferenceUpdate();
		figure.repaint();
	}
}
