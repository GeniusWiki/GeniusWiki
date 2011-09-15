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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.WindowResizeListener;

/**
 * A collection of {@link ResizableWidget} that periodically checks the outer
 * dimensions of a widget and redraws it as necessary. Every
 * {@link ResizableWidgetCollection} uses a timer, so consider the cost when
 * adding one.
 * 
 * Typically, a {@link ResizableWidgetCollection} is only needed if you expect
 * your widgets to resize based on window resizing or other events. Fixed sized
 * Widgets do not need to be added to a {@link ResizableWidgetCollection} as
 * they cannot be resized.
 */
public class ResizableWidgetCollection implements WindowResizeListener {
  /**
   * Information about a widgets size.
   */
  private static class ResizableWidgetInfo {
    /**
     * The current clientHeight.
     */
    private int curHeight = 0;

    /**
     * The current clientWidth.
     */
    private int curWidth = 0;

    /**
     * Constructor.
     * 
     * @param widget the widget that will be monitored
     */
    public ResizableWidgetInfo(ResizableWidget widget) {
      curWidth = DOM.getElementPropertyInt(widget.getElement(), "clientWidth");
      curHeight = DOM.getElementPropertyInt(widget.getElement(), "clientHeight");
    }

    /**
     * Set the new dimensions of the widget if they changed.
     * 
     * @param width the new width
     * @param height the new height
     * @return true if the dimensions have changed
     */
    public boolean setClientSize(int width, int height) {
      if (width != curWidth || height != curHeight) {
        this.curWidth = width;
        this.curHeight = height;
        return true;
      } else {
        return false;
      }
    }
  }

  /**
   * The default delay between resize checks in milliseconds.
   */
  private static final int DEFAULT_RESIZE_CHECK_DELAY = 400;

  /**
   * A static {@link ResizableWidgetCollection} that can be used in most cases.
   */
  private static ResizableWidgetCollection staticCollection = null;

  /**
   * Get the globally accessible {@link ResizableWidgetCollection}. In most
   * cases, the global collection can be used for all {@link ResizableWidget}s.
   * 
   * @return the global {@link ResizableWidgetCollection}
   */
  public static ResizableWidgetCollection get() {
    if (staticCollection == null) {
      staticCollection = new ResizableWidgetCollection();
    }
    return staticCollection;
  }

  /**
   * The timer used to periodically compare the dimensions of elements to their
   * old dimensions.
   */
  private Timer resizeCheckTimer = new Timer() {
    public void run() {
      // Ignore changes that result from window resize events
      if (windowHeight != Window.getClientHeight()
          || windowWidth != Window.getClientWidth()) {
        windowHeight = Window.getClientHeight();
        windowWidth = Window.getClientWidth();
        schedule(resizeCheckDelay);
        return;
      }

      // Look for elements that have new dimensions
      checkWidgetSize();

      // Start checking again
      if (resizeCheckingEnabled) {
        schedule(resizeCheckDelay);
      }
    }
  };

  /**
   * A hash map of the resizable widgets this collection is checking.
   */
  private HashMap/* <ResizableWidget, ResizableWidgetInfo> */widgets = new HashMap();

  /**
   * The current window height.
   */
  private int windowHeight = 0;

  /**
   * The current window width.
   */
  private int windowWidth = 0;

  /**
   * The delay between resize checks.
   */
  private int resizeCheckDelay = DEFAULT_RESIZE_CHECK_DELAY;

  /**
   * A boolean indicating that resize checking should run.
   */
  private boolean resizeCheckingEnabled = true;

  /**
   * Create a ResizableWidget.
   */
  public ResizableWidgetCollection() {
    this(DEFAULT_RESIZE_CHECK_DELAY);
  }

  /**
   * Constructor.
   * 
   * @param resizeCheckingEnabled false to disable resize checking
   */
  public ResizableWidgetCollection(boolean resizeCheckingEnabled) {
    this(DEFAULT_RESIZE_CHECK_DELAY, false);
  }

  /**
   * Constructor.
   * 
   * @param resizeCheckDelay the delay between checks in milliseconds
   */
  public ResizableWidgetCollection(int resizeCheckDelay) {
    this(resizeCheckDelay, true);
  }

  /**
   * Constructor.
   */
  protected ResizableWidgetCollection(int resizeCheckDelay,
      boolean resizeCheckingEnabled) {
    Window.addWindowResizeListener(this);
    setResizeCheckDelay(resizeCheckDelay);
    if (resizeCheckingEnabled) {
      resizeCheckTimer.schedule(resizeCheckDelay);
    } else {
      this.resizeCheckingEnabled = false;
    }
  }

  /**
   * Add a resizable widget to the collection.
   * 
   * @param widget the resizable widget to add
   */
  public void add(ResizableWidget widget) {
    widgets.put(widget, new ResizableWidgetInfo(widget));
  }

  /**
   * Check to see if any Widgets have been resized and call their handlers
   * appropriately.
   */
  public void checkWidgetSize() {
    Iterator it = widgets.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry entry = (Map.Entry) it.next();
      ResizableWidget widget = (ResizableWidget) entry.getKey();
      ResizableWidgetInfo info = (ResizableWidgetInfo) entry.getValue();
      int curWidth = DOM.getElementPropertyInt(widget.getElement(),
          "clientWidth");
      int curHeight = DOM.getElementPropertyInt(widget.getElement(),
          "clientHeight");
  
      // Call the onResize method only if the widget is attached
      if (info.setClientSize(curWidth, curHeight)) {
        if (curWidth > 0 && curHeight > 0 && widget.isAttached()) {
          widget.onResize(curWidth, curHeight);
        }
      }
    }
  }

  /**
   * Get the delay between resize checks in milliseconds.
   * 
   * @return the resize check delay
   */
  public int getResizeCheckDelay() {
    return resizeCheckDelay;
  }
  
  /**
   * Check whether or not resize checking is enabled.
   * 
   * @return true is resize checking is enabled
   */
  public boolean isResizeCheckingEnabled() {
    return resizeCheckingEnabled;
  }

  /**
   * Called when the browser window is resized.
   * 
   * @param width the width of the window's client area.
   * @param height the height of the window's client area.
   */
  public void onWindowResized(int width, int height) {
    checkWidgetSize();
  }

  /**
   * Remove a {@link ResizableWidget} from the collection.
   * 
   * @param widget the widget to remove
   */
  public void remove(ResizableWidget widget) {
    widgets.remove(widget);
  }

  /**
   * Set the delay between resize checks in milliseconds.
   * 
   * @param resizeCheckDelay the new delay
   */
  public void setResizeCheckDelay(int resizeCheckDelay) {
    this.resizeCheckDelay = resizeCheckDelay;
  }

  /**
   * Set whether or not resize checking is enabled. If disabled, elements will
   * still be resized on window events, but the timer will not check their
   * dimensions periodically.
   * 
   * @param enabled true to enable the resize checking timer
   */
  public void setResizeCheckingEnabled(boolean enabled) {
    if (enabled && !resizeCheckingEnabled) {
      resizeCheckingEnabled = true;
      resizeCheckTimer.schedule(resizeCheckDelay);
    } else if (!enabled && resizeCheckingEnabled) {
      resizeCheckingEnabled = false;
      resizeCheckTimer.cancel();
    }
  }
}
