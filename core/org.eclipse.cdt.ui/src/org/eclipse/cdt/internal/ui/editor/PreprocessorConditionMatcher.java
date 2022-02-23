package org.eclipse.cdt.internal.ui.editor;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.eclipse.cdt.core.dom.ast.IASTPreprocessorEndifStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfdefStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.internal.ui.text.ICReconcilingListener;
import org.eclipse.core.runtime.IProgressMonitor;

public class PreprocessorConditionMatcher implements ICReconcilingListener {

	private Map<Integer, Integer> fStartToEnd = new HashMap<>();
	private Map<Integer, Integer> fEndToStart = new HashMap<>();
	private Stack<Integer> fStarts = new Stack<>();

	@Override
	public void aboutToBeReconciled() {
		fStartToEnd.clear();
		fEndToStart.clear();
		fStarts.clear();
	}

	@Override
	public void reconciled(IASTTranslationUnit ast, boolean force, IProgressMonitor progressMonitor) {
		if (ast == null || progressMonitor.isCanceled())
			return;

		for (IASTPreprocessorStatement stmt : ast.getAllPreprocessorStatements()) {
			if (stmt instanceof IASTPreprocessorIfdefStatement) {
				IASTPreprocessorIfdefStatement ifdefStatement = (IASTPreprocessorIfdefStatement) stmt;
				if (ifdefStatement.taken()) {
					fStarts.add(ifdefStatement.getFileLocation().getStartingLineNumber());
				}
			} else if (stmt instanceof IASTPreprocessorIfStatement) {
				IASTPreprocessorIfStatement ifStatement = (IASTPreprocessorIfStatement) stmt;
				if (ifStatement.taken()) {
					fStarts.add(ifStatement.getFileLocation().getStartingLineNumber());
				}
			} else if (stmt instanceof IASTPreprocessorEndifStatement) {
				if (!fStarts.empty()) {
					IASTPreprocessorEndifStatement endifStatement = (IASTPreprocessorEndifStatement) stmt;
					int start = fStarts.pop();
					int end = endifStatement.getFileLocation().getStartingLineNumber();
					fStartToEnd.put(start, end);
					fStartToEnd.put(end, start);
				}
			}
		}
	}

	public int getMatch(int lineNum) {
		return fStartToEnd.getOrDefault(lineNum, fEndToStart.getOrDefault(lineNum, -1));
	}
}
