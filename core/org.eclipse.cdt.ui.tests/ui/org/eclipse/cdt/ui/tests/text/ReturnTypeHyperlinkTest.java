/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Marc-Andre Laperle
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.text;

import java.io.IOException;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;

import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IWorkingCopyManager;
import org.eclipse.cdt.ui.testplugin.CTestPlugin;
import org.eclipse.cdt.ui.testplugin.EditorTestHelper;

import org.eclipse.cdt.internal.core.model.ASTCache.ASTRunnable;

import org.eclipse.cdt.internal.ui.editor.ASTProvider;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.editor.CReturnTypeHyperlinkDetector;

/**
 * This test just checks that hyperlinks are created in the right
 * places. It does not test that the hyperlinks actually take you 
 * to the right place.
 * 
 * @author Mike Kucera
 */
public class ReturnTypeHyperlinkTest extends TestCase {
	private static final String CPP_FILE_NAME_1 = "hyperlink_test_cpp.cpp";
	private static final String CPP_CODE = 
		"#include <stdio.h> \n" +
		"#define SOMEMACRO macro_token1 macro_token2 \n" +
		"// COMMENT there should not be links inside of comments \n" +
		"class Point { \n" +
		"  public: \n" +
		"    Point(); \n" +
		"    ~Point(); \n" +
		"    void set(int x, int y); \n" +
		"    int getX(); \n" +
		"    int getY(); \n" +
		"    Point operator+(Point); \n" +
		"  private: \n" +
		"    int x, y; \n" +
		"}; \n" +
		"int test(Point p) {  \n" +
		"    char* str = \"STRING LITERAL\"; \n" +
		"    p + p;" +
		"} \n";
	
	private static final String C_FILE_NAME_1 = "hyperlink_test_c_1.c";
	private static final String C_CODE_1 = 
		"int main() { \n" +
		"    int class = 99; \n" +
		"}";
	
	private static final String C_FILE_NAME_2 = "hyperlink_test_c_2.c";
	private static final String C_CODE_2 = 
		"#ifdef NOTDEF\n" +
		"    int nothere = 99; \n" +
		"#else\n" +
		"    int itworks = 100; \n" +
		"#endif\n";

	private ICProject project;
	private CEditor editor;

	public static TestSuite suite() {
		return new TestSuite(ReturnTypeHyperlinkTest.class);
	}
	
	private void setUpEditor(String fileName, String code) throws Exception {
		setUpEditor(fileName, code, true);
	}

	private void setUpEditor(String fileName, String code, boolean buildAST) throws Exception {
		super.setUp();
		project= CProjectHelper.createCCProject(super.getName(), "unused", IPDOMManager.ID_NO_INDEXER);
		ICContainer cContainer= CProjectHelper.addCContainer(project, "src");
		IFile file= EditorTestHelper.createFile((IContainer) cContainer.getResource(), fileName, code, new NullProgressMonitor());
		
		assertNotNull(file);
		assertTrue(file.exists());
		editor = (CEditor) EditorTestHelper.openInEditor(file, true);
		EditorTestHelper.joinReconciler(EditorTestHelper.getSourceViewer(editor), 10, 1000, 10);
		if (buildAST) {
			// Since CElementHyperlinkDetector doesn't wait for an AST to be created,
			// we trigger AST creation ahead of time.
			IWorkingCopyManager manager = CUIPlugin.getDefault().getWorkingCopyManager();
			IWorkingCopy workingCopy = manager.getWorkingCopy(editor.getEditorInput());
			IStatus status= ASTProvider.getASTProvider().runOnAST(workingCopy, ASTProvider.WAIT_IF_OPEN, null, new ASTRunnable() {
				public IStatus runOnAST(ILanguage lang, IASTTranslationUnit ast) {
					return Status.OK_STATUS;
				}
			});
		}
	}
	
	protected String getAboveComment() {
		return getContents(1)[0].toString();
	}
	
	protected StringBuffer[] getContents(int sections) {
		try {
			CTestPlugin plugin = CTestPlugin.getDefault();
			return TestSourceReader.getContentsForTest(plugin.getBundle(), "ui", getClass(), getName(), sections);
		} catch (IOException e) {
			fail(e.getMessage());
			return null;
		}
	}
	
	@Override
	protected void tearDown() throws Exception {
		EditorTestHelper.closeEditor(editor);
		CProjectHelper.delete(project);
	}
	
	private IHyperlink[] getHyperlinks(int mouseOffset) {
		CReturnTypeHyperlinkDetector detector = new CReturnTypeHyperlinkDetector();
		detector.setContext(editor);
		IRegion region = new Region(mouseOffset, 0);
		return detector.detectHyperlinks(EditorTestHelper.getSourceViewer(editor), region, false);
	}
	
	private void assertHyperlink(int mouseOffset, int linkStartOffset, int linkLength) {
		IHyperlink[] links = getHyperlinks(mouseOffset);
		assertNotNull(links);
		assertEquals(1, links.length);
		IRegion hyperlinkRegion = links[0].getHyperlinkRegion();
		assertEquals(linkStartOffset, hyperlinkRegion.getOffset());
		assertEquals(linkLength, hyperlinkRegion.getLength());
	}
	
	private void assertNotHyperlink(int mouseOffset) {
		IHyperlink[] links = getHyperlinks(mouseOffset);
		assertNull(links);
	}

	public void testHyperlinksCpp() throws Exception {
		// entire include highlighted
		setUpEditor(CPP_FILE_NAME_1, CPP_CODE, true);
		
		assertNotHyperlink(CPP_CODE.indexOf("#include") + 2); 
		assertNotHyperlink(CPP_CODE.indexOf("<stdio.h>") + 2);
		assertNotHyperlink(CPP_CODE.indexOf("<stdio.h>") + "<stdio.h".length());
		
		// hovering over the whitespace inside an include still results in a hyperlink
		assertNotHyperlink(CPP_CODE.indexOf("<stdio.h>") - 1);

		// no hyperlinks in macro bodies
		assertNotHyperlink(CPP_CODE.indexOf("#define") + 1);
		assertNotHyperlink(CPP_CODE.indexOf("SOMEMACRO"));
		// see bug 259015
		assertNotHyperlink(CPP_CODE.indexOf("macro_token1"));
		assertNotHyperlink(CPP_CODE.indexOf("macro_token2"));
		
		// no hyperlinks for comments
		assertNotHyperlink(CPP_CODE.indexOf("//") + 1);
		assertNotHyperlink(CPP_CODE.indexOf("COMMENT") + 1);
		
		// no hyperlinks for keywords
		assertNotHyperlink(CPP_CODE.indexOf("class") + 1); 
		assertNotHyperlink(CPP_CODE.indexOf("public") + 1); 
		assertNotHyperlink(CPP_CODE.indexOf("private") + 1); 
		assertNotHyperlink(CPP_CODE.indexOf("int x") + 1); 
		assertNotHyperlink(CPP_CODE.indexOf("char") + 1); 
		assertNotHyperlink(CPP_CODE.indexOf("void") + 1);
		
		// no hyperlinks for punctuation
		assertNotHyperlink(CPP_CODE.indexOf("{"));
		assertNotHyperlink(CPP_CODE.indexOf("}"));
		assertNotHyperlink(CPP_CODE.indexOf("(" + 1));
		assertNotHyperlink(CPP_CODE.indexOf(")"));
		assertNotHyperlink(CPP_CODE.indexOf(":"));
		assertNotHyperlink(CPP_CODE.indexOf(";"));
		
		// no hyperlinks inside strings
		assertNotHyperlink(CPP_CODE.indexOf("STRING") + 1);
		assertNotHyperlink(CPP_CODE.indexOf("STRING") + 6);
		assertNotHyperlink(CPP_CODE.indexOf("LITERAL") + 1);
		
		assertNotHyperlink(CPP_CODE.indexOf("Point {") + 1);
		assertNotHyperlink(CPP_CODE.indexOf("Point()") + 1);
		assertNotHyperlink(CPP_CODE.indexOf("~Point()"));
		assertNotHyperlink(CPP_CODE.indexOf("set(") + 1);
		assertNotHyperlink(CPP_CODE.indexOf("getX()") + 1);
		assertNotHyperlink(CPP_CODE.indexOf("getY()") + 1);
		
		// hyperlinks for overloaded operators returning type
		assertNotHyperlink(CPP_CODE.indexOf("+ p"));
	}

	public void testHyperlinksCKeywords() throws Exception {
		setUpEditor(C_FILE_NAME_1, C_CODE_1, true);
		
		// no hyperlinks for numeric literals
		assertNotHyperlink(C_CODE_1.indexOf("99") + 1);
	}

	public void testHyperlinksInactiveCode() throws Exception {
		setUpEditor(C_FILE_NAME_2, C_CODE_2, true);
		
		assertNotHyperlink(C_CODE_2.indexOf("#ifdef") + 2);
		assertNotHyperlink(C_CODE_2.indexOf("#else") + 2);
		assertNotHyperlink(C_CODE_2.indexOf("#endif") + 2);
		
		// see bug 259015
		assertNotHyperlink(C_CODE_2.indexOf("nothere") + 1);
		assertNotHyperlink(C_CODE_2.indexOf("itworks") + 1);
	}
	
	// class Test;
	// Test func();
	public void testSimpleFunctionCalls() throws Exception {
		String code = getAboveComment();
		setUpEditor(CPP_FILE_NAME_1, code, true);
		EditorTestHelper.joinBackgroundActivities();
		
		assertHyperlink(code.indexOf("func") + 1, code.indexOf("func"), "func".length());
	}
}
