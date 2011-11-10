package org.eclipse.cdt.internal.ui.editor;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.IWorkbenchPage;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.core.model.ext.ICElementHandle;

import org.eclipse.cdt.internal.ui.typehierarchy.CreateTypeHierarchyOperation;
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
		
		final IIndex index = name.getTranslationUnit().getIndex();
		ICElementHandle[] cElementHandles;
		try {
			index.acquireReadLock();
			cElementHandles = IndexUI.findAllDefinitions(index, name.resolveBinding());
//			if(cElementHandles.length == 1) {
//				onlyOneFound = true;
//				EditorOpener.open(workbenchPage, cElementHandles[0]);
//			} else {
				IRunnableWithProgress runnable= new IRunnableWithProgress() {

					public void run(IProgressMonitor monitor) throws InvocationTargetException,
							InterruptedException {
						try {
							index.acquireReadLock();
							ICElementHandle cElementForName = IndexUI.getCElementForName(null /* TODO: cProject */, index, name);
							index.releaseReadLock();
							CreateTypeHierarchyOperation createTypeHierarchyOperation = new CreateTypeHierarchyOperation(cElementForName.getParent(), cElementForName);
							createTypeHierarchyOperation.setTaskName("Searching really");
							try {
								createTypeHierarchyOperation.runOperation(monitor);
							} catch (CModelException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}	
						} catch (CoreException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
					
				};
				IRunnableContext runnableContext = workbenchPage.getWorkbenchWindow();
				try {
					runnableContext.run(true, true, runnable);
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
//			}
		} catch (CoreException e) {
			CUIPlugin.log(e);
		} catch (InterruptedException e) {
			CUIPlugin.log(e);
		} finally {
			index.releaseReadLock();
		}
		
//		if(!onlyOneFound) {
//			fOpenAction.run();	
//		}
	}
	
	@Override
	public String getHyperlinkText() {
		return "Open Implementation";
	}

}
