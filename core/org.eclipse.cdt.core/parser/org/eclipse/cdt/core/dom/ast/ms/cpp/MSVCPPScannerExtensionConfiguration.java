/*******************************************************************************
 * Copyright (c) 2004, 2016 IBM Corporation and others.
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
 *     Ed Swartz (Nokia)
 *     Anton Leherbauer (Wind River Systems)
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *     Richard Eames
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.ms.cpp;

import java.util.Map;

import org.eclipse.cdt.core.dom.parser.AbstractScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.GNUScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.GPPScannerExtensionConfiguration;
import org.eclipse.cdt.core.parser.GCCKeywords;
import org.eclipse.cdt.core.parser.IGCCToken;
import org.eclipse.cdt.core.parser.IScannerInfo;

/**
 * Configures the preprocessor for c++-sources as accepted by g++.
 */
public class MSVCPPScannerExtensionConfiguration extends GNUScannerExtensionConfiguration {
	private static MSVCPPScannerExtensionConfiguration CONFIG_MSVC = new MSVCPPScannerExtensionConfiguration(
			0 /* version is ignored for now */);

	public static AbstractScannerExtensionConfiguration getInstance(IScannerInfo info) {
		if (info != null) {
			try {
				final Map<String, String> definedSymbols = info.getDefinedSymbols();

				// Clang-cl. Needs to be checked first since it pretends to be MSVC too.
				String clang = definedSymbols.get("__clang__"); //$NON-NLS-1$
				if (clang != null && Integer.valueOf(clang) > 0) {
					return GPPScannerExtensionConfiguration.getInstance(info);
				}
			} catch (Exception e) {
				// Fall-back to the default configuration.
			}
		}
		return CONFIG_MSVC;
	}

	/**
	 * @since 6.3
	 */
	@SuppressWarnings("nls")
	public MSVCPPScannerExtensionConfiguration(int version) {
		addMacro("__builtin_offsetof(T,m)",
				"(reinterpret_cast <size_t>(&reinterpret_cast <const volatile char &>(static_cast<T*> (0)->m)))"); //TODO: Keep for both?

		// As documented at
		// https://docs.microsoft.com/en-us/cpp/extensions/compiler-support-for-type-traits-cpp-component-extensions?view=vs-2017
		// For now we don't make it dependent on the version.
		addKeyword(GCCKeywords.cp__has_nothrow_assign, IGCCToken.tTT_has_nothrow_assign);
		addKeyword(GCCKeywords.cp__has_nothrow_constructor, IGCCToken.tTT_has_nothrow_constructor);
		addKeyword(GCCKeywords.cp__has_nothrow_copy, IGCCToken.tTT_has_nothrow_copy);
		addKeyword(GCCKeywords.cp__has_trivial_assign, IGCCToken.tTT_has_trivial_assign);
		addKeyword(GCCKeywords.cp__has_trivial_constructor, IGCCToken.tTT_has_trivial_constructor);
		addKeyword(GCCKeywords.cp__has_trivial_copy, IGCCToken.tTT_has_trivial_copy);
		addKeyword(GCCKeywords.cp__has_trivial_destructor, IGCCToken.tTT_has_trivial_destructor);
		addKeyword(GCCKeywords.cp__has_virtual_destructor, IGCCToken.tTT_has_virtual_destructor);
		addKeyword(GCCKeywords.cp__is_abstract, IGCCToken.tTT_is_abstract);
		addKeyword(GCCKeywords.cp__is_base_of, IGCCToken.tTT_is_base_of);
		addKeyword(GCCKeywords.cp__is_class, IGCCToken.tTT_is_class);
		addKeyword(GCCKeywords.cp__is_empty, IGCCToken.tTT_is_empty);
		addKeyword(GCCKeywords.cp__is_enum, IGCCToken.tTT_is_enum);
		addKeyword(GCCKeywords.cp__is_pod, IGCCToken.tTT_is_pod);
		addKeyword(GCCKeywords.cp__is_polymorphic, IGCCToken.tTT_is_polymorphic);
		addKeyword(GCCKeywords.cp__is_union, IGCCToken.tTT_is_union);
		// Missing from that reference page:
		// - __has_assign
		// - __has_copy
		// - __has_finalizer
		// - __has_user_destructor
		// - __is_convertible_to
		// - __is_delegate
		// - __is_interface_class
		// - __is_ref_array
		// - __is_ref_class
		// - __is_simple_value_class
		// - __is_value_class

		// These are according to:
		// http://clang.llvm.org/docs/LanguageExtensions.html#checks-for-type-trait-primitives.
		addKeyword(GCCKeywords.cp__is_final, IGCCToken.tTT_is_final);
		addKeyword(GCCKeywords.cp__underlying_type, IGCCToken.tTT_underlying_type);
		addKeyword(GCCKeywords.cp__is_trivially_constructible, IGCCToken.tTT_is_trivially_constructible);
		addKeyword(GCCKeywords.cp__is_trivially_assignable, IGCCToken.tTT_is_trivially_assignable);
		addKeyword(GCCKeywords.cp__is_constructible, IGCCToken.tTT_is_constructible);
		// Missing from that page:
		// - __is_assignable
		// - __is_destructible
		// - __is_nothrow_destructible
		// - __is_nothrow_assignable
		// - __is_nothrow_constructible

		// Found by looking at some headers
		addKeyword(GCCKeywords.cp__is_standard_layout, IGCCToken.tTT_is_standard_layout);
		addKeyword(GCCKeywords.cp__is_literal_type, IGCCToken.tTT_is_literal_type);
		addKeyword(GCCKeywords.cp__is_trivial, IGCCToken.tTT_is_trivial);
		addKeyword(GCCKeywords.cp__is_trivially_copyable, IGCCToken.tTT_is_trivially_copyable);
		// Missing:
		// - __is_trivially_destructible
	}

	@Override
	public boolean supportMinAndMaxOperators() {
		return true;
	}

	/**
	 * @since 5.5
	 */
	@Override
	public boolean supportRawStringLiterals() {
		return true;
	}

	/**
	 * User Defined Literals
	 * @since 5.10
	 */
	@Override
	public boolean supportUserDefinedLiterals() {
		return true;
	}

	@Override
	public boolean supportDigitSeparators() {
		return true;
	}
}
