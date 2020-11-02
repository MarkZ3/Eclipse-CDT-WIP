/*******************************************************************************
 * Copyright (c) 2005, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Anton Leherbauer (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.ms.cpp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBasicType.Kind;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.parser.IBuiltinBindingsProvider;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.util.CharArraySet;
import org.eclipse.cdt.internal.core.dom.parser.IntegralValue;
import org.eclipse.cdt.internal.core.dom.parser.c.CArrayType;
import org.eclipse.cdt.internal.core.dom.parser.c.CBasicType;
import org.eclipse.cdt.internal.core.dom.parser.c.CBuiltinParameter;
import org.eclipse.cdt.internal.core.dom.parser.c.CBuiltinVariable;
import org.eclipse.cdt.internal.core.dom.parser.c.CFunctionType;
import org.eclipse.cdt.internal.core.dom.parser.c.CImplicitFunction;
import org.eclipse.cdt.internal.core.dom.parser.c.CImplicitTypedef;
import org.eclipse.cdt.internal.core.dom.parser.c.CPointerType;
import org.eclipse.cdt.internal.core.dom.parser.c.CQualifierType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPArrayType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBuiltinParameter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBuiltinVariable;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunctionType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPImplicitFunction;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPImplicitTypedef;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPPointerType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPQualifierType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPReferenceType;

/**
 * This is the IBuiltinBindingsProvider used to implement the "Other" built-in GCC symbols defined:
 * http://gcc.gnu.org/onlinedocs/gcc/Other-Builtins.html#Other-Builtins
 */
@SuppressWarnings("nls")
public class MSVCBuiltinSymbolProvider implements IBuiltinBindingsProvider {
	/**
	 * {@code BUILTIN_GCC_SYMBOL} is a built-in GCC symbol.
	 */
	public static final ASTNodeProperty BUILTIN_GCC_SYMBOL = new ASTNodeProperty(
			"GCCBuiltinSymbolProvider.BUILTIN_GCC_SYMBOL - built-in GCC symbol"); //$NON-NLS-1$

	private static final Map<String, char[]> CHAR_ARRAYS = new HashMap<>();

	private IBinding[] fBindings;
	private IScope fScope;
	private final boolean fCpp;

	private Map<String, IType> fTypeMap;
	private List<IBinding> fBindingList;

	private CharArraySet fKnownBuiltins = new CharArraySet(50);

	public MSVCBuiltinSymbolProvider(ParserLanguage lang) {
		fCpp = lang == ParserLanguage.CPP;
	}

	@Override
	public IBinding[] getBuiltinBindings(IScope scope) {
		fScope = scope;
		initialize();
		return fBindings;
	}

	private void initialize() {
		// Symbols for all parsers
		fTypeMap = new HashMap<>();
		fBindingList = new ArrayList<>();
		variable("const char[1]", "__func__");
		variable("const char[1]", "__FUNCTION__");
		variable("const char[1]", "__FUNCDNAME__");
		variable("const char[1]", "__FUNCSIG__");
		function("void*", "_alloca", "size_t");

		fBindings = fBindingList.toArray(new IBinding[fBindingList.size()]);
		for (IBinding binding : fBindings) {
			fKnownBuiltins.put(binding.getNameCharArray());
		}
		fTypeMap = null;
		fBindingList = null;
	}

	private void variable(String type, String name) {
		IBinding b = fCpp ? new CPPBuiltinVariable(toType(type), toCharArray(name), fScope)
				: new CBuiltinVariable(toType(type), toCharArray(name), fScope);
		fBindingList.add(b);
	}

	private void typedef(String type, String name) {
		IBinding b = fCpp ? new CPPImplicitTypedef(toType(type), toCharArray(name), fScope)
				: new CImplicitTypedef(toType(type), toCharArray(name), fScope);
		fBindingList.add(b);
	}

	private void cfunction(String returnType, String name, String... parameterTypes) {
		if (!fCpp) {
			function(returnType, name, parameterTypes);
		}
	}

	private void function(String returnType, String name, String... parameterTypes) {
		int len = parameterTypes.length;
		boolean varargs = len > 0 && parameterTypes[len - 1].equals("...");
		if (varargs)
			len--;

		IType[] pTypes = new IType[len];
		IParameter[] theParms = fCpp ? new ICPPParameter[len] : new IParameter[len];
		for (int i = 0; i < len; i++) {
			IType pType = toType(parameterTypes[i]);
			pTypes[i] = pType;
			theParms[i] = fCpp ? new CPPBuiltinParameter(pType) : new CBuiltinParameter(pType);
		}
		IType rt = toType(returnType);
		IFunctionType ft = fCpp ? new CPPFunctionType(rt, pTypes, null) : new CFunctionType(rt, pTypes);

		IBinding b = fCpp
				? new CPPImplicitFunction(toCharArray(name), fScope, (ICPPFunctionType) ft, (ICPPParameter[]) theParms,
						false, varargs)
				: new CImplicitFunction(toCharArray(name), fScope, ft, theParms, varargs);
		fBindingList.add(b);
	}

	private char[] toCharArray(String name) {
		synchronized (CHAR_ARRAYS) {
			char[] result = CHAR_ARRAYS.get(name);
			if (result == null) {
				result = name.toCharArray();
				CHAR_ARRAYS.put(name, result);
			}
			return result;
		}
	}

	private IType toType(String type) {
		IType t = fTypeMap.get(type);
		if (t == null) {
			t = createType(type);
			fTypeMap.put(type, t);
		}
		return t;
	}

	private IType createType(final String type) {
		String tstr = type;
		if (fCpp && tstr.endsWith("&")) {
			final String nested = tstr.substring(0, tstr.length() - 1).trim();
			return new CPPReferenceType(toType(nested), false);
		}
		if (tstr.equals("FILE*")) {
			return toType("void*");
		} else if (tstr.endsWith("*")) {
			final String nested = tstr.substring(0, tstr.length() - 1).trim();
			final IType nt = toType(nested);
			return fCpp ? new CPPPointerType(nt) : new CPointerType(nt, 0);
		} else if (tstr.endsWith("[1]")) {
			final String nested = tstr.substring(0, tstr.length() - 3).trim();
			final IType nt = toType(nested);
			return fCpp ? new CPPArrayType(nt, IntegralValue.create(1)) : new CArrayType(nt);
		}

		boolean isConst = false;
		boolean isVolatile = false;
		if (tstr.startsWith("const ")) {
			isConst = true;
			tstr = tstr.substring(6);
		}
		if (tstr.endsWith("const")) {
			isConst = true;
			tstr = tstr.substring(0, tstr.length() - 5).trim();
		}
		if (tstr.startsWith("volatile ")) {
			isVolatile = true;
			tstr = tstr.substring(9);
		}
		if (tstr.endsWith("volatile")) {
			isVolatile = true;
			tstr = tstr.substring(0, tstr.length() - 8).trim();
		}
		int q = 0;
		if (tstr.startsWith("signed ")) {
			q |= IBasicType.IS_SIGNED;
			tstr = tstr.substring(7);
		}
		if (tstr.startsWith("unsigned ")) {
			q |= IBasicType.IS_UNSIGNED;
			tstr = tstr.substring(9);
		}
		if (tstr.startsWith("complex ")) {
			q |= IBasicType.IS_COMPLEX;
			tstr = tstr.substring(8);
		}
		if (tstr.startsWith("long long")) {
			q |= IBasicType.IS_LONG_LONG;
			tstr = tstr.substring(9).trim();
		}
		if (tstr.startsWith("long")) {
			q |= IBasicType.IS_LONG;
			tstr = tstr.substring(4).trim();
		}

		IType t;
		if (tstr.equals("void")) {
			Kind kind = Kind.eVoid;
			t = fCpp ? new CPPBasicType(kind, q) : new CBasicType(kind, q);
		} else if (tstr.isEmpty()) {
			Kind kind = Kind.eUnspecified;
			t = fCpp ? new CPPBasicType(kind, q) : new CBasicType(kind, q);
		} else if (tstr.equals("char")) {
			Kind kind = Kind.eChar;
			t = fCpp ? new CPPBasicType(kind, q) : new CBasicType(kind, q);
		} else if (tstr.equals("int")) {
			Kind kind = Kind.eInt;
			t = fCpp ? new CPPBasicType(kind, q) : new CBasicType(kind, q);
		} else if (tstr.equals("__int128")) {
			Kind kind = Kind.eInt128;
			t = fCpp ? new CPPBasicType(kind, q) : new CBasicType(kind, q);
		} else if (tstr.equals("float")) {
			Kind kind = Kind.eFloat;
			t = fCpp ? new CPPBasicType(kind, q) : new CBasicType(kind, q);
		} else if (tstr.equals("double")) {
			Kind kind = Kind.eDouble;
			t = fCpp ? new CPPBasicType(kind, q) : new CBasicType(kind, q);
		} else if (tstr.equals("bool")) {
			t = fCpp ? new CPPBasicType(Kind.eBoolean, q) : new CBasicType(Kind.eInt, q);
		} else if (tstr.equals("va_list")) {
			// Use 'char*(*)()'
			IType rt = toType("char*");
			t = fCpp ? new CPPPointerType(new CPPFunctionType(rt, IType.EMPTY_TYPE_ARRAY, null))
					: new CPointerType(new CFunctionType(rt, IType.EMPTY_TYPE_ARRAY), 0);
		} else if (tstr.equals("size_t")) {
			t = toType("unsigned long");
		} else if (tstr.equals("void*")) {
			// This can occur inside a qualifier type in which case it's not handled
			// by the general '*' check above.
			t = fCpp ? new CPPPointerType(new CPPBasicType(Kind.eVoid, q))
					: new CPointerType(new CBasicType(Kind.eVoid, q), 0);
		} else {
			throw new IllegalArgumentException(type);
		}

		if (isConst || isVolatile) {
			return fCpp ? new CPPQualifierType(t, isConst, isVolatile)
					: new CQualifierType(t, isConst, isVolatile, false);
		}
		return t;
	}

	@Override
	public boolean isKnownBuiltin(char[] builtinName) {
		return fKnownBuiltins.containsKey(builtinName);
	}
}
