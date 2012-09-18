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
package com.edgenius.wiki.gwt.client.server.constant;


/**
 *  draft could have 3 type so far: manual(user click "save draft" button) :1, auto (system saving periodically):2, 
 *  Offline upload has version conflict, then save it as draft:3
 *  draft type:
 * 
 * @author Dapeng.Ni
 */
public enum PageType {
    NONE_DRAFT,  //could be history or current page
    MANUAL_DRAFT, 
    AUTO_DRAFT,
    OFFLINE_CONFLICT_DRAFT;
    

    /**
     * @return
     */
    public boolean isDraft() {
        return this.ordinal() > 0;
    }

    /**
     * @param object
     * @return
     */
    public static PageType fromOrdial(int ordial) {
        for (PageType type : PageType.values()){
            if(type.ordinal() == ordial) return type;
        }
        return null;
    }

    /**
     * @return
     */
    public int value() {
        return this.ordinal();
    }
}
