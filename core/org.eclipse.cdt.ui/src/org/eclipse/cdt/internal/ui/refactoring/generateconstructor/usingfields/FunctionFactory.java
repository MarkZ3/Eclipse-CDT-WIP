/*******************************************************************************
 * Copyright (c) 2011 Marc-Andre Laperle and others.
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

import org.eclipse.cdt.core.dom.ast.ASTNodeFactoryFactory;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.parser.Keywords;

import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTConstructorChainInitializer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPParameter;

import org.eclipse.cdt.internal.ui.refactoring.utils.NameHelper;

public class FunctionFactory {
	
	static ICPPNodeFactory nodeFactory = ASTNodeFactoryFactory.getDefaultCPPNodeFactory();

	private static ICPPASTFunctionDeclarator getConstructorDeclarator(
			GenerateConstructorUsingFieldsContext context) {
		IASTCompositeTypeSpecifier currentClass = context.currentClass;
		
		IASTName constructorAstName = nodeFactory.newName(currentClass.getName().toString().toCharArray());

		ICPPASTFunctionDeclarator declarator = nodeFactory.newFunctionDeclarator(constructorAstName);

		addFieldsToDeclarator(context, declarator);
		return declarator;
	}

	public static IASTSimpleDeclaration getConstructorDeclaration(
			GenerateConstructorUsingFieldsContext context) {
		IASTSimpleDeclSpecifier declSpecifier = nodeFactory.newSimpleDeclSpecifier();
		declSpecifier.setType(IASTSimpleDeclSpecifier.t_unspecified);
		
		IASTSimpleDeclaration constructorDeclaration = nodeFactory.newSimpleDeclaration(declSpecifier);
		constructorDeclaration.addDeclarator(getConstructorDeclarator(context));
		return constructorDeclaration;
	}

	public static IASTFunctionDefinition getConstructorDefinition(
			GenerateConstructorUsingFieldsContext context) {
		IASTSimpleDeclSpecifier declSpecifier = nodeFactory.newSimpleDeclSpecifier();
		declSpecifier.setType(IASTSimpleDeclSpecifier.t_unspecified);
		
		IASTName constructorName;
		if(!context.isImplementationInHeader()) {
			constructorName = getClassname(context); 
		} else {
			constructorName = nodeFactory.newName(context.currentClass.getName().toString().toCharArray());
		}
		
		ICPPASTFunctionDeclarator declarator = getConstructorDeclarator(context, constructorName);
	
		
		ICPPASTFunctionDefinition constructorDefinition = nodeFactory.newFunctionDefinition(declSpecifier, declarator, getConstructorBody(context));
		addInitializerList(context, constructorDefinition);

		return constructorDefinition;
	}

	private static IASTStatement getConstructorBody(GenerateConstructorUsingFieldsContext context) {
		IASTCompoundStatement compound = nodeFactory.newCompoundStatement();
		
		if(context.initializeMembers && context.initializeMembersMethod == GenerateConstructorUsingFieldsContext.INITMEMBERS_ASSIGNEMENT) {
			addConstructorAssignments(context, compound);
		}

		return compound;
	}
	
	private static void addInitializerList(GenerateConstructorUsingFieldsContext context,
			ICPPASTFunctionDefinition constructorDefinition) {
		
		// Initialization list
		
		// Base classes
		for (ICPPASTBaseSpecifier baseClass : context.baseClasses) {
			ICPPConstructor baseConstructor = context.selectedbaseClassesConstrutors.get(baseClass);
			if (baseConstructor != null) {
					CPPASTConstructorChainInitializer chainInit = new CPPASTConstructorChainInitializer();
					chainInit.setMemberInitializerId(baseClass.getName().copy());
					ArrayList<IASTInitializerClause> clauses = new ArrayList<IASTInitializerClause>();
					for (ICPPParameter baseConstrutorParam : baseConstructor.getParameters()) {
						CPPParameter paramTest = (CPPParameter) baseConstrutorParam;
						
						IASTNode physicalNode = paramTest.getPhysicalNode();
						
						// A normal parameter
						if(physicalNode != null) {
							IASTNode baseConstructorParamNode = physicalNode.getParent().getParent();
							if (baseConstructorParamNode instanceof IASTParameterDeclaration) {
								IASTParameterDeclaration fieldDeclaration = (IASTParameterDeclaration) baseConstructorParamNode;
								IASTIdExpression idExpr = nodeFactory.newIdExpression(fieldDeclaration.getDeclarator().getName().copy());
								clauses.add(idExpr);
							}
						} 
						// An unamed, implicit constructor parameter
						else {
							if (paramTest.getType() instanceof ICPPReferenceType) {
								ICPPReferenceType referenceType = (ICPPReferenceType) paramTest.getType();
								if (referenceType.getType() instanceof IQualifierType) {
									IQualifierType qualifier = (IQualifierType)referenceType.getType();
									IType type = qualifier.getType();
									
									String typeToString = type.toString();
									String firstLowered = typeToString.substring(0, 1).toLowerCase();
									if(typeToString.length() > 1) {
										firstLowered += typeToString.substring(1);
									}
									
									String paramName = NameHelper.trimFieldName(firstLowered);
									IASTIdExpression idExpr = nodeFactory.newIdExpression(nodeFactory.newName(paramName.toCharArray()));
									clauses.add(idExpr);
								}
							}
						}
						
					}
					
					IASTInitializerClause[] clausesArray = new IASTInitializerClause[]{};
					ICPPASTConstructorInitializer initalizer = nodeFactory.newConstructorInitializer(clauses.toArray(clausesArray));
					chainInit.setInitializer(initalizer);
					constructorDefinition.addMemberInitializer(chainInit);
			}
		}
		
		boolean initializeInInitList = context.initializeMembers
				&& context.initializeMembersMethod == GenerateConstructorUsingFieldsContext.INITMEMBERS_INITLIST;
		if (initializeInInitList) {
			// Actual constructor params
			for (GenerateConstructorInsertEditProvider field : context.getSelectedFieldsInOrder()) {
				
				IASTDeclarator fieldDeclarator = field.getFieldDeclarator();
				
				CPPASTConstructorChainInitializer chainInit = new CPPASTConstructorChainInitializer();
				chainInit.setMemberInitializerId(fieldDeclarator.getName().copy());
				String paramNameStr = fieldDeclarator.getName().toString();
				
				
				boolean sameAfterTrimmed = paramNameStr.equals(NameHelper.trimFieldName(paramNameStr));
				// Add _ before the variable name to avoid name clashes. Not needed if the trimmed name is different
				if(sameAfterTrimmed) {
					paramNameStr = "_" + paramNameStr; //$NON-NLS-1$
				} else {
					paramNameStr = NameHelper.trimFieldName(paramNameStr);
				}
				IASTName cppastName = nodeFactory.newName(paramNameStr.toCharArray());
				fieldDeclarator = nodeFactory.newDeclarator(cppastName);
				
				IASTIdExpression idExpr = nodeFactory.newIdExpression(nodeFactory.newName(paramNameStr.toCharArray()));
				
				ICPPASTConstructorInitializer initalizer = nodeFactory.newConstructorInitializer(new IASTIdExpression[] { idExpr });
				chainInit.setInitializer(initalizer);
				constructorDefinition.addMemberInitializer(chainInit);
			}
		}
	}

	private static void addFieldsToDeclarator(GenerateConstructorUsingFieldsContext context,
			ICPPASTFunctionDeclarator declarator) {
		
		//Base constructor params
		for (ICPPASTBaseSpecifier baseClass : context.baseClasses) {
			ICPPConstructor baseConstructor = context.selectedbaseClassesConstrutors.get(baseClass);
			if (baseConstructor != null) {
					for (ICPPParameter baseConstrutorParam : baseConstructor.getParameters()) {
						CPPParameter paramTest = (CPPParameter) baseConstrutorParam;
						
						IASTNode physicalNode = paramTest.getPhysicalNode();
						
						// A normal parameter
						if(physicalNode != null) {
							IASTNode baseConstructorParamNode = physicalNode.getParent().getParent();
							if (baseConstructorParamNode instanceof IASTParameterDeclaration) {
								IASTParameterDeclaration fieldDeclaration = (IASTParameterDeclaration) baseConstructorParamNode;
								ICPPASTParameterDeclaration parameterDeclaration = nodeFactory.newParameterDeclaration(
										fieldDeclaration.getDeclSpecifier().copy(), 
										fieldDeclaration.getDeclarator().copy());
								declarator.addParameterDeclaration(parameterDeclaration);
							}
						} 
						// An unamed, implicit constructor parameter
						else {
							if (paramTest.getType() instanceof ICPPReferenceType) {
								ICPPReferenceType referenceType = (ICPPReferenceType) paramTest.getType();
								if (referenceType.getType() instanceof IQualifierType) {
									IQualifierType qualifier = (IQualifierType) referenceType.getType();
									IType type = qualifier.getType();
									IASTName astName = nodeFactory.newName(type.toString().toCharArray());

									IASTDeclSpecifier declSpec = nodeFactory.newTypedefNameSpecifier(astName);
									declSpec.setConst(qualifier.isConst());
									
									String typeToString = type.toString();
									String firstLowered = typeToString.substring(0, 1).toLowerCase();
									if(typeToString.length() > 1) {
										firstLowered += typeToString.substring(1);
									}
									
									String paramNameStr = NameHelper.trimFieldName(firstLowered);
									IASTName astParamName = nodeFactory.newName(paramNameStr.toCharArray());
									IASTDeclarator declaratorTest = nodeFactory.newDeclarator(astParamName);
									declaratorTest.addPointerOperator(nodeFactory.newReferenceOperator(true));
									
									ICPPASTParameterDeclaration parameterDeclaration = nodeFactory.newParameterDeclaration(declSpec, declaratorTest);
									declarator.addParameterDeclaration(parameterDeclaration);
								}
							}
						}
						
					}
			}
		}
		 
		// Actual constructor params
		for (GenerateConstructorInsertEditProvider field : context.getSelectedFieldsInOrder()) {
			String paramNameStr = field.getFieldDeclarator().getName().toString();
			
			boolean initializeInInitList = context.initializeMembers
			&& context.initializeMembersMethod == GenerateConstructorUsingFieldsContext.INITMEMBERS_INITLIST;
			boolean sameAfterTrimmed = paramNameStr.equals(NameHelper.trimFieldName(paramNameStr));
			
			// Add _ before the variable name to avoid name clashes. Not needed if the trimmed name is different
			if(sameAfterTrimmed && initializeInInitList) {
				paramNameStr = "_" + paramNameStr; //$NON-NLS-1$
			} else {
				paramNameStr = NameHelper.trimFieldName(paramNameStr);
			}
			
			IASTName cppastName = nodeFactory.newName(paramNameStr.toCharArray());
			IASTDeclarator newFieldDeclarator = nodeFactory.newDeclarator(cppastName);
			
			ICPPASTParameterDeclaration parameterDeclaration = nodeFactory.newParameterDeclaration(field.getDeclSpecifier().copy(), newFieldDeclarator);
	
			IASTDeclSpecifier paramDeclSpecifier = parameterDeclaration.getDeclSpecifier();
	
			if (context.addConstToObjects) {
				if (paramDeclSpecifier instanceof IASTCompositeTypeSpecifier
						|| paramDeclSpecifier instanceof IASTNamedTypeSpecifier) {
					paramDeclSpecifier.setConst(true);
				}
			}
	
			if (context.passObjectsByReference) {
				ICPPASTDeclarator paramDeclarator = parameterDeclaration.getDeclarator();
				if ((paramDeclSpecifier instanceof IASTCompositeTypeSpecifier || paramDeclSpecifier instanceof IASTNamedTypeSpecifier)) {
					paramDeclarator.addPointerOperator(nodeFactory.newReferenceOperator(true));
				}
			}
	
			declarator.addParameterDeclaration(parameterDeclaration);
		}
	}

	private static void addConstructorAssignments(GenerateConstructorUsingFieldsContext context,
			IASTCompoundStatement compound) {
		for (GenerateConstructorInsertEditProvider field : context.getSelectedFieldsInOrder()) {
			IASTDeclarator fieldDeclarator = field.getFieldDeclarator();

			// Add 'this'
			ICPPASTLiteralExpression litExpr = nodeFactory.newLiteralExpression(IASTLiteralExpression.lk_this, new String(Keywords.cTHIS));

			// Add member
			IASTName iastName = fieldDeclarator.getName().copy();
			ICPPASTFieldReference fieldRef = nodeFactory.newFieldReference(iastName, litExpr);
			fieldRef.setIsPointerDereference(true);
			
			// Add value
			String paramNameStr = fieldDeclarator.getName().toString();
			paramNameStr = NameHelper.trimFieldName(paramNameStr);
			IASTName cppastName = nodeFactory.newName(paramNameStr.toCharArray());
			IASTIdExpression idExpr = nodeFactory.newIdExpression(cppastName);
			
			// this->member = value;
			IASTBinaryExpression binExpr = nodeFactory.newBinaryExpression(IASTBinaryExpression.op_assign, fieldRef, idExpr);

			IASTExpressionStatement exprStmt = nodeFactory.newExpressionStatement(binExpr);
			compound.addStatement(exprStmt);
		}
	}

	private static ICPPASTFunctionDeclarator getConstructorDeclarator(
			GenerateConstructorUsingFieldsContext context, IASTName constructorName) {
		ICPPASTFunctionDeclarator declarator = nodeFactory.newFunctionDeclarator(constructorName);

		addFieldsToDeclarator(context, declarator);
		
		return declarator;
	}

	private static ICPPASTQualifiedName getClassname(GenerateConstructorUsingFieldsContext context) {
		IASTNode n = context.existingFields.get(0).getFieldDeclarator().getParent();
		while (!(n instanceof IASTCompositeTypeSpecifier)) {
			n = n.getParent();
		}
		IASTCompositeTypeSpecifier comp = (IASTCompositeTypeSpecifier) n;

		ICPPASTQualifiedName qname = nodeFactory.newQualifiedName();
		qname.addName(comp.getName().copy());
		qname.addName(comp.getName().copy());
		return qname;
	}
}
