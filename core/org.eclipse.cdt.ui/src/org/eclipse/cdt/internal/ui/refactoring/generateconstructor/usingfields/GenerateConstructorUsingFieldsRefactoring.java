/*******************************************************************************
 * Copyright (c) 2010, 2013 Marc-Andre Laperle and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.generateconstructor.usingfields;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;

import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassType;
import org.eclipse.cdt.internal.core.dom.rewrite.astwriter.ContainerNode;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.ClassMemberInserter;
import org.eclipse.cdt.internal.ui.refactoring.Container;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.cdt.internal.ui.refactoring.implementmethod.InsertLocation;
import org.eclipse.cdt.internal.ui.refactoring.implementmethod.MethodDefinitionInsertLocationFinder;
import org.eclipse.cdt.internal.ui.refactoring.utils.Checks;
import org.eclipse.cdt.internal.ui.refactoring.utils.NodeHelper;
import org.eclipse.cdt.internal.ui.refactoring.utils.VisibilityEnum;

public class GenerateConstructorUsingFieldsRefactoring extends CRefactoring {

	private ICPPASTVisibilityLabel currentVisibility = null;
	
	private final class CompositeTypeSpecFinder extends ASTVisitor {
		private final int start;
		private final Container<IASTCompositeTypeSpecifier> container;
		{
			shouldVisitDeclSpecifiers = true;
		}

		private CompositeTypeSpecFinder(int start, Container<IASTCompositeTypeSpecifier> container) {
			this.start = start;
			this.container = container;
		}

		@Override
		public int visit(IASTDeclSpecifier declSpec) {
			
			if (declSpec instanceof IASTCompositeTypeSpecifier) {
				IASTFileLocation loc = declSpec.getFileLocation();
				if(start > loc.getNodeOffset() && start < loc.getNodeOffset()+ loc.getNodeLength()) {
					container.setObject((IASTCompositeTypeSpecifier) declSpec);
					return ASTVisitor.PROCESS_ABORT;
				}
			}
			
			return super.visit(declSpec);
		}
		
	}
	
	private static final String MEMBER_DECLARATION = "MEMBER_DECLARATION"; //$NON-NLS-1$
	
	private GenerateConstructorUsingFieldsContext context;

	private InsertLocation definitionInsertLocation;
	
	public GenerateConstructorUsingFieldsRefactoring(ICElement element, ISelection selection,
			ICProject proj) {
		super(element, selection, proj);
		context = new GenerateConstructorUsingFieldsContext();
	}

	@Override
	protected RefactoringDescriptor getRefactoringDescriptor() {
		return null;
	}

	@Override
	protected void collectModifications(IProgressMonitor pm, ModificationCollector collector)
			throws CoreException, OperationCanceledException {
//		refactoringContext.getAST(tu, pm).accept(new 
//				
//				ASTVisitor() {
//			{
//				this.shouldVisitDeclSpecifiers = true;
//			}
//			
//			@Override
//			public int visit(IASTDeclSpecifier declSpec) {
//				
//				if (declSpec instanceof ICPPASTCompositeTypeSpecifier) {
//					if(declSpec.toString().equals(compositeTypeSpecifier.toString())){
//						context.currentClass = (ICPPASTCompositeTypeSpecifier) declSpec;
//						return ASTVisitor.PROCESS_ABORT;
//					}
//				}
//				
//				return super.visit(declSpec);
//			}
//		});
		IASTNode constructorNode = null;
		if(!context.isSeparateDefinition()) {
			constructorNode = FunctionFactory.getConstructorDefinition(context);
		} else {
			constructorNode = FunctionFactory.getConstructorDeclaration(context);
			IASTFunctionDefinition functionDef = FunctionFactory.getConstructorDefinition(context);
			addDefinition(collector, functionDef, pm);
		}
		
		ClassMemberInserter.createChange(context.currentClass, VisibilityEnum.v_public,
				constructorNode, false, collector);
	}
	
	private void addDefinition(ModificationCollector collector, IASTFunctionDefinition functionDefinition, IProgressMonitor pm)
			throws CoreException {		
		findDefinitionInsertLocation(pm);
		
		IASTTranslationUnit targetUnit = refactoringContext.getAST(definitionInsertLocation.getTranslationUnit(), null);
		IASTNode parent = definitionInsertLocation.getParentOfNodeToInsertBefore();
		ASTRewrite rewrite = collector.rewriterForTranslationUnit(targetUnit);
		IASTNode nodeToInsertBefore = definitionInsertLocation.getNodeToInsertBefore();
		
		ContainerNode cont = new ContainerNode();
		cont.addNode(functionDefinition);
		rewrite = rewrite.insertBefore(parent, nodeToInsertBefore, cont, null);
		return;
	}
	
	private void findDefinitionInsertLocation(IProgressMonitor subProgressMonitor) throws CoreException {
		if (definitionInsertLocation != null) {
			return;
		}
		
		IASTDeclarator decl = context.existingFields.get(0).getFieldDeclarator();

		MethodDefinitionInsertLocationFinder methodDefinitionInsertLocationFinder = new MethodDefinitionInsertLocationFinder();
		InsertLocation insertLocation = methodDefinitionInsertLocationFinder.find(tu, decl.getFileLocation(), decl.getParent(), refactoringContext, subProgressMonitor);

		if (insertLocation.getTranslationUnit() == null || NodeHelper.isContainedInTemplateDeclaration(decl)) {
			insertLocation.setNodeToInsertAfter(NodeHelper.findTopLevelParent(decl), tu);
		}
		
		definitionInsertLocation = insertLocation;

	}

	public GenerateConstructorUsingFieldsContext getContext() {
		return context;
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException,
			OperationCanceledException {
		SubMonitor sm = SubMonitor.convert(pm, 10);

		RefactoringStatus status = super.checkInitialConditions(sm.newChild(6));
		if(status.hasError()) {
			return status;
		}

		if(!initStatus.hasFatalError()) {

			initRefactoring(pm);

			if(context.existingFields.size() == 0) {
				initStatus.addFatalError(Messages.GenerateConstructorUsingFields_NoFields);
			}
		}	
		return initStatus;
	}
	
	private void initRefactoring(IProgressMonitor pm) throws OperationCanceledException, CoreException {
		context.currentClass = findCurrentCompositeTypeSpecifier();
		if(context.currentClass != null) {
			collectBaseContructors();
			collectFieldDeclarations();
		}else {
			initStatus.addFatalError(Messages.GenerateConstructorUsingFields_NoCassDefFound);
		}
		
	}
	
	private ICPPASTCompositeTypeSpecifier findCurrentCompositeTypeSpecifier() throws OperationCanceledException, CoreException {
		IASTTranslationUnit astRoot = refactoringContext.getAST(tu, null);
		
		final int start = selectedRegion.getOffset();
		Container<IASTCompositeTypeSpecifier> container = new Container<IASTCompositeTypeSpecifier>();
		
		astRoot.accept(new CompositeTypeSpecFinder(start, container));
		
		IASTCompositeTypeSpecifier composite = container.getObject();
		if(composite instanceof ICPPASTCompositeTypeSpecifier){
			return (ICPPASTCompositeTypeSpecifier)composite;
		}
		
		return null;
	}
	
	protected void collectFieldDeclarations() {
		context.currentClass.accept(new ASTVisitor() {

			{
				shouldVisitDeclarations = true;
			}

			@Override
			public int visit(IASTDeclaration declaration) {
				if(declaration instanceof ICPPASTVisibilityLabel) {
					currentVisibility = (ICPPASTVisibilityLabel) declaration;
				}
				if (declaration instanceof IASTSimpleDeclaration) {
					IASTSimpleDeclaration fieldDeclaration = (IASTSimpleDeclaration) declaration;
					ASTNodeProperty props = fieldDeclaration.getPropertyInParent();
					if (props.getName().contains(MEMBER_DECLARATION)) {
						final IASTDeclarator[] declarators = fieldDeclaration.getDeclarators();
						for(IASTDeclarator declarator : declarators) {
							// TODO: don't use negation
							IASTDeclSpecifier declSpecifier = fieldDeclaration.getDeclSpecifier();
							if (! (declarator instanceof IASTFunctionDeclarator) && declSpecifier.getStorageClass() != IASTDeclSpecifier.sc_static)  {
								if(currentVisibility == null) {
									GenerateConstructorInsertEditProvider generateConstructorInsertEditProvider = new GenerateConstructorInsertEditProvider(declSpecifier, declarator, ICPPASTVisibilityLabel.v_private);
									context.existingFields.add(generateConstructorInsertEditProvider);
								}
								else {
									GenerateConstructorInsertEditProvider generateConstructorInsertEditProvider = new GenerateConstructorInsertEditProvider(declSpecifier, declarator, currentVisibility.getVisibility());
									context.existingFields.add(generateConstructorInsertEditProvider);
								}
							}
						}
					}
				}
				return super.visit(declaration);
			}
		});
		
		context.treeFields.addAll(context.existingFields);
	}

	private void collectBaseContructors() {
		// find base constructors
		ICPPASTBaseSpecifier[] bases = context.currentClass.getBaseSpecifiers();
		for(ICPPASTBaseSpecifier base : bases) {
			context.baseClasses.add(base);
			IBinding binding = base.getName().resolveBinding();
			ArrayList<ICPPConstructor> constructors = new ArrayList<ICPPConstructor>();
			if(binding instanceof CPPClassType){
				CPPClassType classType = (CPPClassType)binding;
				for(ICPPConstructor constuctor : classType.getConstructors()) {
					constructors.add(constuctor);
				}
			}
			context.baseClassesConstrutors.put(base, constructors);
		}
	}
	
	private IFile[] getAllFilesToModify() {
		List<IFile> files = new ArrayList<IFile>(2);
		IFile file = (IFile) tu.getResource();
		if (file != null) {
			files.add(file);
		}
		if (definitionInsertLocation != null) {
			file = definitionInsertLocation.getFile();
			if (file != null) {
				files.add(file);
			}
		}
		return files.toArray(new IFile[files.size()]);
	}

	@Override
	protected RefactoringStatus checkFinalConditions(IProgressMonitor subProgressMonitor,
			CheckConditionsContext checkContext) throws CoreException, OperationCanceledException {
		RefactoringStatus result = new RefactoringStatus();
		if (context.isSeparateDefinition()) {
			findDefinitionInsertLocation(subProgressMonitor);
			if (definitionInsertLocation == null || tu.equals(definitionInsertLocation.getTranslationUnit())) {
				result.addInfo(Messages.GenerateConstructorUsingFieldsRefactoring_NoImplFile);
			}
		}
		Checks.addModifiedFilesToChecker(getAllFilesToModify(), checkContext);
		return result;
	}

}
