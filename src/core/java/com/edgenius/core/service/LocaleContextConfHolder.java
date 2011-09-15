/* 
 * =============================================================
 * Copyright (C) 2007-2011 Edgenius (http://www.edgenius.com)
 * =============================================================
 * License Information: http://www.edgenius.com/licensing/edgenius/2.0/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2.0
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *
 * http://www.gnu.org/licenses/gpl.txt
 *  
 * ****************************************************************
 */
package com.edgenius.core.service;

import java.util.Locale;

import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.SimpleLocaleContext;
import org.springframework.core.NamedThreadLocal;

import com.edgenius.core.Global;


/**
 * This class is replacement spring default LocaleContextHolder. It will read default locale from Global.xml rather than 
 * from system. 
 * 
 * @see org.springframework.context.i18n.LocaleContextHolder
 * @author Dapeng.Ni
 */
public class LocaleContextConfHolder {
	@SuppressWarnings("rawtypes")
	private static final ThreadLocal localeContextHolder = new NamedThreadLocal("Conf Locale context");

	@SuppressWarnings("unchecked")
	public static void setLocaleContext(LocaleContext localeContext) {
		localeContextHolder.set(localeContext);
	}
	@SuppressWarnings("unchecked")
	public static void setLocale(Locale locale) {
		LocaleContext localeContext = (locale != null ? new SimpleLocaleContext(locale) : null);
		localeContextHolder.set(localeContext);
		
	}
	public static Locale getLocale() {
		LocaleContext localeContext = (LocaleContext) localeContextHolder.get();
		return (localeContext != null ? localeContext.getLocale() : Global.getDefaultLocale());
	}

}
