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
package com.edgenius.wiki.gwt.client.html;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * @author Dapeng.Ni
 */
public class HTMLNodeContainer implements Iterable<HTMLNode>{

	List<HTMLNode> container = new ArrayList<HTMLNode>();
	
	/**
	 * Return HTML text from container
	 */
	public String toString(){
		StringBuilder buf = new StringBuilder();
		for (HTMLNode node : container) {
			buf.append(node.getText());
		}
		
		return buf.toString();
	}
	/**
	 * Append HTMLNode to tail of list and set previous for this node if there is node before it
	 * @param node
	 */
	public void add(HTMLNode node){
		if(container.size() > 0){
			node.previous = container.get(container.size()-1);
			node.previous.next = node;
		}
		container.add(node);
	}

	/**
	 * @return
	 */
	public int size() {
		return container.size();
	}
	public ListIterator<HTMLNode> listIterator() {
		return this.listIterator(0);
	}
	/**
	 * @return
	 */
	public ListIterator<HTMLNode> listIterator(final int idx) {
		return new ListIterator<HTMLNode>(){
			ListIterator<HTMLNode> iter = container.listIterator(idx);
			private HTMLNode cursor;
			public void add(HTMLNode e) {
				iter.add(e);
				e.previous = cursor;
				e.next = cursor.next;
				if(cursor.next != null){
					cursor.next.previous = e;
					cursor.next = e;
				}
				//this will take care multiple consequence add(): the next add will always be append, rather than insert
				cursor = e;
			}
			public void remove() {
				iter.remove();
				if(cursor.previous != null)
					cursor.previous.next = cursor.next;
				if(cursor.next != null)
					cursor.next.previous = cursor.previous;
				//does it necessary to disable cursor?
				cursor.next = null;
				cursor.previous = null;

			}

			public void set(HTMLNode e) {
				iter.set(e);
				e.next = cursor.next;
				e.previous = cursor.previous;
				//does it necessary to disable cursor?
				cursor.next = null;
				cursor.previous = null;
			}

			public HTMLNode next() {
				cursor = iter.next();
				return cursor;
			}
			public HTMLNode previous() {
				cursor = iter.previous();
				return cursor;
			}

			public boolean hasNext() {
				return iter.hasNext();
			}

			public boolean hasPrevious() {
				return iter.hasPrevious();
			}

			public int nextIndex() {
				return iter.nextIndex();
			}

		
			public int previousIndex() {
				return iter.previousIndex();
			}

		};
	}
	public Iterator<HTMLNode> iterator() {
		return new Iterator<HTMLNode>(){
			Iterator<HTMLNode> iter = container.iterator();
			private HTMLNode cursor;
			public boolean hasNext() {
				return iter.hasNext();
			}

			public HTMLNode next() {
				cursor = iter.next();
				return cursor;
			}

			public void remove() {
				//reset cursor.next() and cursor.previous();
				if(cursor.previous != null){
					cursor.previous.next = cursor.next;
				}
				if(cursor.next != null){
					cursor.next.previous = cursor.previous;
				}
					
				iter.remove();
			}
			
		};
	}

	/**
	 * @return
	 */
	public HTMLNode first() {
		if(container.size() > 0){
			container.get(0);
		}
		return null;
	}

	/**
	 * 
	 */
	public void clear() {
		container.clear();
	}
}
