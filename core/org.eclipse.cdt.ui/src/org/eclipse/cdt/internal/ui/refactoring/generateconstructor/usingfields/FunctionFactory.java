/*******************************************************************************
 * Copyright (c) 2010, 2019 Marc-Andre Laperle and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.generateconstructor.usingfields;

import org.eclipse.cdt.core.dom.ast.ASTNodeFactoryFactory;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNode.CopyStyle;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTConstructorChainInitializer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.ui.refactoring.utils.NameHelper;

public class FunctionFactory {

	static ICPPNodeFactory nodeFactory = ASTNodeFactoryFactory.getDefaultCPPNodeFactory();

	public static IASTSimpleDeclaration getConstructorDeclaration(GenerateConstructorUsingFieldsContext context) {
		IASTSimpleDeclSpecifier declSpecifier = nodeFactory.newSimpleDeclSpecifier();
		declSpecifier.setType(IASTSimpleDeclSpecifier.t_unspecified);

		IASTSimpleDeclaration constructorDeclaration = nodeFactory.newSimpleDeclaration(declSpecifier);
		constructorDeclaration.addDeclarator(getConstructorDeclarator(context));
		return constructorDeclaration;
	}

	private static ICPPASTFunctionDeclarator getConstructorDeclarator(GenerateConstructorUsingFieldsContext context) {
		IASTCompositeTypeSpecifier currentClass = context.currentClass;

		IASTName constructorAstName = nodeFactory.newName(currentClass.getName().toString().toCharArray());

		ICPPASTFunctionDeclarator declarator = nodeFactory.newFunctionDeclarator(constructorAstName);

		addParametersFromFields(context, declarator);
		return declarator;
	}

	public static IASTNode getConstructorDefinition(GenerateConstructorUsingFieldsContext context,
			IASTName constructorName) {
		IASTSimpleDeclSpecifier declSpecifier = nodeFactory.newSimpleDeclSpecifier();
		declSpecifier.setType(IASTSimpleDeclSpecifier.t_unspecified);

		ICPPASTFunctionDeclarator declarator = getConstructorDeclarator(context, constructorName);

		ICPPASTFunctionDefinition constructorDefinition = nodeFactory.newFunctionDefinition(declSpecifier, declarator,
				nodeFactory.newCompoundStatement());
		addInitializerList(context, constructorDefinition);

		if (context.isSeparateDefinition()) {
			ICPPASTTemplateDeclaration templateDeclaration = CPPVisitor.findAncestorWithType(context.currentClass,
					ICPPASTTemplateDeclaration.class);
			if (templateDeclaration != null) {
				ICPPASTTemplateDeclaration newTemplateDeclaration = nodeFactory
						.newTemplateDeclaration(constructorDefinition);
				for (ICPPASTTemplateParameter templateParameter : templateDeclaration.getTemplateParameters()) {
					newTemplateDeclaration.addTemplateParameter(templateParameter.copy(CopyStyle.withLocations));
				}

				return newTemplateDeclaration;
			}
		}

		return constructorDefinition;
	}

	private static void addInitializerList(GenerateConstructorUsingFieldsContext context,
			ICPPASTFunctionDefinition constructorDefinition) {
		// Actual constructor params
		for (GenerateConstructorInsertEditProvider field : context.getSelectedFieldsInOrder()) {

			IASTDeclarator fieldDeclarator = field.getFieldDeclarator();

			CPPASTConstructorChainInitializer chainInit = new CPPASTConstructorChainInitializer();
			chainInit.setMemberInitializerId(fieldDeclarator.getName().copy());
			String paramNameStr = fieldDeclarator.getName().toString();

			boolean sameAfterTrimmed = paramNameStr.equals(NameHelper.trimFieldName(paramNameStr));
			// Add _ before the variable name to avoid name clashes. Not needed if the trimmed name is different
			if (sameAfterTrimmed) {
				paramNameStr = "_" + paramNameStr; //$NON-NLS-1$
			} else {
				paramNameStr = NameHelper.trimFieldName(paramNameStr);
			}
			IASTName cppastName = nodeFactory.newName(paramNameStr.toCharArray());
			fieldDeclarator = nodeFactory.newDeclarator(cppastName);

			IASTIdExpression idExpr = nodeFactory.newIdExpression(nodeFactory.newName(paramNameStr.toCharArray()));

			ICPPASTConstructorInitializer initalizer = nodeFactory
					.newConstructorInitializer(new IASTIdExpression[] { idExpr });
			chainInit.setInitializer(initalizer);
			constructorDefinition.addMemberInitializer(chainInit);
		}
	}

	private static void addParametersFromFields(GenerateConstructorUsingFieldsContext context,
			ICPPASTFunctionDeclarator declarator) {
		// Member constructor params
		for (GenerateConstructorInsertEditProvider field : context.getSelectedFieldsInOrder()) {
			String paramNameStr = field.getFieldDeclarator().getName().toString();

			boolean sameAfterTrimmed = paramNameStr.equals(NameHelper.trimFieldName(paramNameStr));

			// Add _ before the variable name to avoid name clashes. Not needed if the trimmed name is different
			if (sameAfterTrimmed) {
				paramNameStr = "_" + paramNameStr; //$NON-NLS-1$
			} else {
				paramNameStr = NameHelper.trimFieldName(paramNameStr);
			}

			IASTName cppastName = nodeFactory.newName(paramNameStr.toCharArray());
			IASTDeclarator newFieldDeclarator = nodeFactory.newDeclarator(cppastName);

			ICPPASTParameterDeclaration parameterDeclaration = nodeFactory
					.newParameterDeclaration(field.getDeclSpecifier().copy(), newFieldDeclarator);

			IASTDeclSpecifier paramDeclSpecifier = parameterDeclaration.getDeclSpecifier();

			if (paramDeclSpecifier instanceof IASTCompositeTypeSpecifier
					|| paramDeclSpecifier instanceof IASTNamedTypeSpecifier) {
				paramDeclSpecifier.setConst(true);
			}

			ICPPASTDeclarator paramDeclarator = parameterDeclaration.getDeclarator();
			if ((paramDeclSpecifier instanceof IASTCompositeTypeSpecifier
					|| paramDeclSpecifier instanceof IASTNamedTypeSpecifier)) {
				paramDeclarator.addPointerOperator(nodeFactory.newReferenceOperator(true));
			}

			declarator.addParameterDeclaration(parameterDeclaration);
		}
	}

	private static ICPPASTFunctionDeclarator getConstructorDeclarator(GenerateConstructorUsingFieldsContext context,
			IASTName constructorName) {
		ICPPASTFunctionDeclarator declarator = nodeFactory.newFunctionDeclarator(constructorName);

		addParametersFromFields(context, declarator);

		return declarator;
	}
}
