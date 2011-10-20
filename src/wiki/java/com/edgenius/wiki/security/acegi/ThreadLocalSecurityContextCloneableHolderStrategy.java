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
package com.edgenius.wiki.security.acegi;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.util.Assert;

/**
 * Copy from org.springframework.security.context.ThreadLocalSecurityContextHolderStrategy as it is "final" class.
 * 
 * The only change createEmptyContext() return SecurityContextCloneableImpl()
 * but not* SecurityContextImpl();
 * 
 * This class is set from LogListener class.
 * 
 * @author Dapeng.Ni
 */
public class ThreadLocalSecurityContextCloneableHolderStrategy implements SecurityContextHolderStrategy {
    //~ Static fields/initializers =====================================================================================

    private static final ThreadLocal<SecurityContext> contextHolder = new ThreadLocal<SecurityContext>();

    //~ Methods ========================================================================================================

    public void clearContext() {
        contextHolder.remove();
    }

    public SecurityContext getContext() {
        SecurityContext ctx = contextHolder.get();

        if (ctx == null) {
            ctx = createEmptyContext();
            contextHolder.set(ctx);
        }

        return ctx;
    }

    public void setContext(SecurityContext context) {
        Assert.notNull(context, "Only non-null SecurityContext instances are permitted");
        contextHolder.set(context);
    }

    public SecurityContext createEmptyContext() {
    	//~~~NDPNDP
        return new SecurityContextCloneableImpl();
    }
}
