package org.eclipse.cdt.internal.ui.editor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.IWorkbenchPage;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.core.model.ext.ICElementHandle;

import org.eclipse.cdt.internal.ui.viewsupport.EditorOpener;
import org.eclipse.cdt.internal.ui.viewsupport.IndexUI;

public class CImplementationHyperlink extends CElementHyperlink {

	IWorkbenchPage workbenchPage = null;
	IASTName name = null;
	
	public CImplementationHyperlink(IRegion region, IAction openAction, IASTName selectedName, IWorkbenchPage page) {
		super(region, openAction);
		workbenchPage = page;
		name = selectedName;
	}

	@Override
	public void open() {
		boolean onlyOneFound = false;
		
		IIndex index = name.getTranslationUnit().getIndex();
		ICElementHandle[] cElementHandles;
		try {
			index.acquireReadLock();
			cElementHandles = IndexUI.findAllDefinitions(index, name.resolveBinding());
			if(cElementHandles.length == 1) {
				onlyOneFound = true;
				EditorOpener.open(workbenchPage, cElementHandles[0]);
			}
		} catch (CoreException e) {
			CUIPlugin.log(e);
		} catch (InterruptedException e) {
			CUIPlugin.log(e);
		} finally {
			index.releaseReadLock();
		}
		
		if(!onlyOneFound) {
			fOpenAction.run();	
		}
	}

}
