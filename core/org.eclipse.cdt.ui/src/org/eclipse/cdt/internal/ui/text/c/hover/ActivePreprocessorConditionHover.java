package org.eclipse.cdt.internal.ui.text.c.hover;

import java.util.Stack;

import org.eclipse.cdt.core.dom.ast.IASTPreprocessorEndifStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfdefStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.core.model.ASTCache.ASTRunnable;
import org.eclipse.cdt.internal.ui.editor.ASTProvider;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IWorkingCopyManager;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;

public class ActivePreprocessorConditionHover extends AbstractCEditorTextHover {

	protected static class SingletonRule implements ISchedulingRule {
		public static final ISchedulingRule INSTANCE = new SingletonRule();

		@Override
		public boolean contains(ISchedulingRule rule) {
			return rule == this;
		}

		@Override
		public boolean isConflicting(ISchedulingRule rule) {
			return rule == this;
		}
	}

	@Override
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		IEditorPart editor = getEditor();
		if (editor != null) {
			IEditorInput input = editor.getEditorInput();
			IWorkingCopyManager manager = CUIPlugin.getDefault().getWorkingCopyManager();
			IWorkingCopy workingCopy = manager.getWorkingCopy(input);
			try {
				if (workingCopy == null || !workingCopy.isConsistent()) {
					return null;
				}
			} catch (CModelException e) {
				return null;
			}

			try {

				int line = textViewer.getDocument().getLineOfOffset(hoverRegion.getOffset());

				// Try with the indexer.
				String source = searchInIndex(workingCopy, line);

				if (source == null || source.trim().isEmpty())
					return null;

				return source;
			} catch (BadLocationException e) {
			}
		}
		return null;
	}

	protected String searchInIndex(final ITranslationUnit tUnit, int line) {
		final ComputeSourceRunnable computer = new ComputeSourceRunnable(tUnit, line);
		Job job = new Job("Computing Source") { //$NON-NLS-1$
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					return ASTProvider.getASTProvider().runOnAST(tUnit, ASTProvider.WAIT_ACTIVE_ONLY, monitor,
							computer);
				} catch (Throwable t) {
					CUIPlugin.log(t);
				}
				return Status.CANCEL_STATUS;
			}
		};
		// If the hover thread is interrupted this might have negative
		// effects on the index - see http://bugs.eclipse.org/219834
		// Therefore we schedule a job to decouple the parsing from this thread.
		job.setPriority(Job.DECORATE);
		job.setSystem(true);
		job.setRule(SingletonRule.INSTANCE);
		job.schedule();
		try {
			job.join();
		} catch (InterruptedException e) {
			job.cancel();
			return null;
		}
		return computer.getSource();
	}

	private static class ComputeSourceRunnable implements ASTRunnable {
		private String fSource;
		private int fHoverLineNum;

		/**
		 * @param tUnit the translation unit
		 * @param textRegion the selected region
		 * @param selection the text of the selected region without
		 */
		public ComputeSourceRunnable(ITranslationUnit tUnit, int hoverLineNum) {
			fSource = null;
			fHoverLineNum = hoverLineNum;
		}

		@Override
		public IStatus runOnAST(ILanguage lang, IASTTranslationUnit ast) {
			if (ast != null) {
				Stack<String> activeConditions = new Stack<>();
				for (IASTPreprocessorStatement stmt : ast.getAllPreprocessorStatements()) {
					if (stmt.getFileLocation().getStartingLineNumber() > fHoverLineNum) {
						if (!activeConditions.empty()) {
							fSource = activeConditions.pop();
						} else {
							fSource = null;
						}
						return Status.OK_STATUS;
					}

					if (stmt instanceof IASTPreprocessorIfdefStatement) {
						IASTPreprocessorIfdefStatement ifdefStatement = (IASTPreprocessorIfdefStatement) stmt;
						if (ifdefStatement.taken()) {
							activeConditions.add(new String(ifdefStatement.getCondition()));
						}
					} else if (stmt instanceof IASTPreprocessorIfStatement) {
						IASTPreprocessorIfStatement ifStatement = (IASTPreprocessorIfStatement) stmt;
						if (ifStatement.taken()) {
							activeConditions.add(new String(ifStatement.getCondition()));
						}
					} else if (stmt instanceof IASTPreprocessorEndifStatement) {
						if (!activeConditions.empty()) {
							activeConditions.pop();
						}
					}
				}
			}
			return Status.CANCEL_STATUS;
		}

		/**
		 * @return the computed source or {@code null}, if no source could be computed
		 */
		public String getSource() {
			return fSource;
		}
	}

}
