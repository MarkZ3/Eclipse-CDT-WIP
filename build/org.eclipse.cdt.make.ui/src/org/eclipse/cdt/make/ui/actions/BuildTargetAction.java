/*
 * Created on 25-Jul-2003
 *
 * Copyright (c) 2002,2003 QNX Software Systems Ltd.
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.make.ui.actions;

import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.make.ui.dialogs.BuildTargetDialog;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.window.Window;

public class BuildTargetAction extends AbstractTargetAction {

	public void run(IAction action) {
		IContainer container = getSelectedContainer();
		if (container != null) {
			BuildTargetDialog dialog = new BuildTargetDialog(getShell(), container);
			String name = null;
			try {
				name = (String) container.getSessionProperty(new QualifiedName(MakeUIPlugin.getUniqueIdentifier(), "lastTarget"));
			} catch (CoreException e) {
			}
			if ( name != null) {
				IPath path = new Path(name);
				name = path.segment(path.segmentCount() - 1);
				IContainer targetContainer;
				if ( path.segmentCount() > 1) {
					path = path.removeLastSegments(1);
					targetContainer = (IContainer) container.findMember(path);
				} else {
					targetContainer = container;
				}
				IMakeTarget target = MakeCorePlugin.getDefault().getTargetManager().findTarget(targetContainer, name);
				if (target != null)
					dialog.setTarget(target);
			}
			if (dialog.open() == Window.OK) {
				IMakeTarget target = dialog.getTarget();
				if (target != null) {
					try {
						IPath path = target.getContainer().getProjectRelativePath().removeFirstSegments(container.getProjectRelativePath().segmentCount());
						path = path.append(target.getName());
						container.setSessionProperty(
							new QualifiedName(MakeUIPlugin.getUniqueIdentifier(), "lastTarget"),
							path.toString());
					} catch (CoreException e1) {
					}
				}
			}
		}
	}



}
