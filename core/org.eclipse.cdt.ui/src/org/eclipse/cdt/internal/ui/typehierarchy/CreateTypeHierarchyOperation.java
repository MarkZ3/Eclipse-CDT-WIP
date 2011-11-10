package org.eclipse.cdt.internal.ui.typehierarchy;

import org.eclipse.ui.progress.IWorkbenchSiteProgressService;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;

import org.eclipse.cdt.internal.core.model.CModelOperation;

public class CreateTypeHierarchyOperation extends CModelOperation {

	public CreateTypeHierarchyOperation(ICElement input, ICElement member) {
		super(new ICElement[]{ input, member });
	}
	
	@Override
	public boolean isReadOnly() {
		return true;
	}

	private class THModelPresenter implements ITHModelPresenter {
		THHierarchyModel model;
		
		public THModelPresenter() {
			model = new THHierarchyModel(this, null, true);
			model.setInput(fElementsToProcess[0], fElementsToProcess[1]);
		}

		public void computGraph() {
			model.setCustomProgressMonitor(fMonitor);
			model.computeGraph();
		}
		
		public void setMessage(String msg) {
		}

		public void onEvent(int event) {
			if (event == THHierarchyModel.END_OF_COMPUTATION && model != null) {
				Object[] hierarchyRootElements = model.getHierarchyRootElements();
//				if (hasMultipleImplementors(hierarchyRootElements)) {
//					// open quick type hierarchy
//				} else {
//					// open editor
//				}
				System.out.println("done!!!");
				fMonitor.done();
			}
		}

//		private boolean hasMultipleImplementors() {
//			if (fRootNodes != null) {
//				for (THNode node : fRootNodes) {
//					updateImplementors(node);
//				}
//			}
//			return false;
//		}

		public IWorkbenchSiteProgressService getProgressService() {
			return null;
		}
	}
	
	@Override
	protected void executeOperation() throws CModelException {
		beginTask("Searching for implementors", 10);
		THModelPresenter modelPresenter = new THModelPresenter();
		try {
			//fMonitor.beginTask("Searching!", 10);
			Thread.sleep(2);
//			fMonitor.setTaskName("Searching no really1");
//			fMonitor.worked(3);
//			fMonitor.setTaskName("Searching no really2");
//			Thread.sleep(2000);
//			fMonitor.setTaskName("Searching no really3");
//			fMonitor.worked(3);
//			fMonitor.setTaskName("Searching no really4");
//			Thread.sleep(2000);
//			fMonitor.setTaskName("Searching no really5");
//			fMonitor.worked(3);
//			fMonitor.setTaskName("Searching no really6");
			modelPresenter.computGraph();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

//	@Override
//	protected ICModelStatus commonVerify() {
//		if (fElementsToProcess == null || fElementsToProcess.length == 0 || (fElementsToProcess[0] == null && fElementsToProcess[1] == null)) {
//			return new CModelStatus(ICModelStatusConstants.NO_ELEMENTS_TO_PROCESS);
//		}
//		
//		return CModelStatus.VERIFIED_OK;
//	}
//
//	@Override
//	public ICModel getCModel() {
//		ICElement element = fElementsToProcess[0] == null ? fElementsToProcess[1] : fElementsToProcess[0];
//		return element.getCModel();
//	}

}
