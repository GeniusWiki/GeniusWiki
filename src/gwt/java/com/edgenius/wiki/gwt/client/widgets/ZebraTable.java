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
package com.edgenius.wiki.gwt.client.widgets;

import java.util.HashSet;
import java.util.Set;

import com.edgenius.wiki.gwt.client.Css;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.UIObject;

/**
 * @author Dapeng.Ni
 */
public class ZebraTable extends FlexTable {
	public static final int STYLE_ZEBRA = 0;
	public static final int STYLE_LIST = 1;
	private boolean hasHeader;
	private Set<Integer> noBorderHeaders = new HashSet<Integer>();
	private int style;
    public ZebraTable() {
        this(STYLE_ZEBRA, true);
    }
    public ZebraTable(int styleType, boolean firstRowAsHeader) {
    	if(styleType == STYLE_LIST){
    		this.setStyleName(Css.LIST_TABLE);
    	}else{
    		this.setStyleName(Css.ZEBRA_TABLE);
    	}
    	this.style = styleType;
    	this.hasHeader = firstRowAsHeader;
    }

    protected void prepareCell(final int row, final int col) {
        super.prepareCell(row, col);
        
        if(row == 0 && hasHeader){
        	updateHeaderStyle();
        }
    }

    public int insertRow(int beforeRow) {
        final int value = super.insertRow(beforeRow);

        // restyle all rows that were shifted up including the new row.
        this.updateRowBackgroundColour(beforeRow, this.getRowCount());
        
        if(beforeRow == 0 && hasHeader){
        	if(getRowCount() > 1)
        		removeOldHeaderStyle();
        }
        return value;
    }
    
    public void removeRow(int row) {
        super.removeRow(row);

        // restyle all rows that were shifted up.
        if(this.getRowCount() > 0){
        	this.updateRowBackgroundColour(row, this.getRowCount());
        }

    }
	/**
	 * For list style table, header has bottom border, but some cell won't expect has this kind border. For example
	 * a blank header in checkbox on history list panel. This will mark this column header won't have such border. 
	 * @param col
	 */
	public void removeHeaderBorder(int col) {
		noBorderHeaders.add(col);
	}
	
    private void updateRowBackgroundColour(final int startRow, final int endRow) {
        for (int i = startRow; i < endRow; i++) {
            this.updateRowBackgroundColour(i);
        }
    }

    private void updateRowBackgroundColour(final int row) {

    	if(row == 0 && hasHeader){
    		//header won't have odd/even style
    		return;
    	}
    	
        final boolean oddRow = (row & 1) == 0;
        final RowFormatter formatter = this.getRowFormatter();

        final String addStyle = oddRow ? Css.ODD : Css.EVEN;
        formatter.addStyleName(row, addStyle);

        final String removeStyle = oddRow ? Css.EVEN : Css.ODD;
        formatter.removeStyleName(row, removeStyle);
        
        
    }
    
	/**
	 * @param startRow
	 */
	private void updateHeaderStyle() {
    	int colSize = this.getCellCount(0);

    	for (int col=0;col < colSize; col++){
			if(style == STYLE_LIST && noBorderHeaders.contains(col)){
				//!!! can not use this.getCellFormatter().setStyleName() as it call prepareCell and cause recursive looping 
				UIObject.setStyleName(this.getCellFormatter().getElement(0, col), Css.NOBORDER_HEADER);
			}else{
				//!!! can not use this.getCellFormatter().setStyleName() as it call prepareCell and cause recursive looping 
				UIObject.setStyleName(this.getCellFormatter().getElement(0, col), Css.HEADER);
			}
		}
	}
	/**
	 * @param col
	 */
	private void removeOldHeaderStyle() {
		//this is new header, then remove the old header
		int colSize = this.getCellCount(1);
		for (int idx=0;idx < colSize; idx++){
			this.getFlexCellFormatter().removeStyleName(1, idx, Css.NOBORDER_HEADER);     
			this.getFlexCellFormatter().removeStyleName(1, idx, Css.HEADER);     
		}
	}
}
