package uk.ac.mdx.xmf.swt.command;

import org.eclipse.gef.commands.Command;

import uk.ac.mdx.xmf.swt.model.MultilineText;

public class MultilineTextChangeCommand extends Command {

	private String newName;
	private final MultilineText text;

	public MultilineTextChangeCommand(MultilineText text, String string) {
		this.text = text;
		if (string != null)
			newName = string;
		else
			newName = ""; //$NON-NLS-1$
	}

	@Override
	public void execute() {
		text.changeText(newName);
	}

	@Override
	public void undo() {
	}
}