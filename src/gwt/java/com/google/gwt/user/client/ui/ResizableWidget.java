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
package com.google.gwt.user.client.ui;
import com.google.gwt.user.client.Element;

/**
 * An interface that defines the methods required to support automatic resizing
 * of the Widget element.
 */
public interface ResizableWidget {
  /**
   * Get the widget's element.
   */
  public abstract Element getElement();

  /**
   * Check if this widget is attached to the page.
   * 
   * @return true if the widget is attached to the page
   */
  public abstract boolean isAttached();

  /**
   * This method is called when the dimensions of the parent element change.
   * Subclasses should override this method as needed.
   * 
   * @param width the new client width of the element
   * @param height the new client height of the element
   */
  public abstract void onResize(int width, int height);
}

