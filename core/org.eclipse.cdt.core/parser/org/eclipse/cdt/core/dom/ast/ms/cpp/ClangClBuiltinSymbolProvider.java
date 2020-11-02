/*******************************************************************************
 * Copyright (c) 2020 Marc-Andre Laperle and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.ms.cpp;

import java.util.HashMap;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.parser.IBuiltinBindingsProvider;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.internal.core.dom.parser.GCCBuiltinSymbolProvider;

/**
 * This is the IBuiltinBindingsProvider used to implement built-ins that Clang-cl supports.
 * It aggregates both the GCC-like configuration as well as the MSVC.
 */
public class ClangClBuiltinSymbolProvider implements IBuiltinBindingsProvider {

	private GCCBuiltinSymbolProvider fGccBuiltinSymbolProvider;
	private MSVCBuiltinSymbolProvider fMsvcBuiltinSymbolProvider;

	public ClangClBuiltinSymbolProvider(ParserLanguage lang, boolean supportGnuSymbols) {
		fGccBuiltinSymbolProvider = new GCCBuiltinSymbolProvider(lang, supportGnuSymbols);
		fMsvcBuiltinSymbolProvider = new MSVCBuiltinSymbolProvider(lang);
	}

	@Override
	public IBinding[] getBuiltinBindings(IScope scope) {
		HashMap<String, IBinding> bindings = new HashMap<>();
		for (IBinding b : fGccBuiltinSymbolProvider.getBuiltinBindings(scope)) {
			bindings.put(b.getName(), b);
		}
		for (IBinding b : fMsvcBuiltinSymbolProvider.getBuiltinBindings(scope)) {
			bindings.put(b.getName(), b);
		}
		return bindings.values().toArray(new IBinding[0]);
	}

	@Override
	public boolean isKnownBuiltin(char[] builtinName) {
		return fMsvcBuiltinSymbolProvider.isKnownBuiltin(builtinName)
				|| fGccBuiltinSymbolProvider.isKnownBuiltin(builtinName);
	}
}
