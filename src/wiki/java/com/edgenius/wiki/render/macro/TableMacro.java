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
package com.edgenius.wiki.render.macro;

import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import com.edgenius.core.util.AuditLogger;
import com.edgenius.wiki.gwt.client.html.HTMLNode;
import com.edgenius.wiki.gwt.client.server.utils.GwtUtils;
import com.edgenius.wiki.gwt.client.server.utils.NameConstants;
import com.edgenius.wiki.gwt.client.server.utils.NumberUtil;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.edgenius.wiki.render.GroupProcessor;
import com.edgenius.wiki.render.GroupProcessorMacro;
import com.edgenius.wiki.render.Macro;
import com.edgenius.wiki.render.MacroParameter;
import com.edgenius.wiki.render.MalformedMacroException;
import com.edgenius.wiki.render.RenderContext;
import com.edgenius.wiki.render.RenderUtil;
import com.edgenius.wiki.render.filter.TableFilter;

/**
 *  {grid} 
 *  
 *  OR
 *  
 * {table:hastitle=yes|bgcolor=foo|bordercolor=foo|borderwidth=10|width=200|height=100}
 * {cell}foo{cell}
 * {rowdiv}
 * {cell}foo{cell}
 * {table}
 * @author Dapeng.Ni
 */
public class TableMacro extends BaseMacro implements GroupProcessorMacro{

	static final String NAME = "table";
	static final String GRID_NAME = "grid";
	//must lower case as MacroParameter require
	public static final String FIRST_LINE_AS_TITLE = "hastitle";
	public static final String CELL_STYLE = "cellstyle";
	public static final String IS_GRID_LAYOUT = "gridlayout";
	
	private static final String BG_COLOR = "bgcolor";
	private static final String BORDER_COLOR = "bordercolor";
	private static final String BORDER_WIDTH = "borderwidth";
	
	public String[] getName() {
		return new String[]{NAME, GRID_NAME};
	}


	public void execute(StringBuffer buffer, MacroParameter params) throws MalformedMacroException {
		//style for cells
		boolean layout = GRID_NAME.equals(params.getMacroName());
		StringBuilder styleBuf = new StringBuilder();
		StringBuilder tableAttrsBuf = new StringBuilder();
		
		//put cells style into context for TableCellMacro uses
		Map<String, String> cellParams = new HashMap<String, String>(); 
		int[] key = TableGroupProcessor.getTableInfo(params);
		if(key != null && key.length == 1){
			int tableId = key[0]; 
			RenderContext context = params.getRenderContext();
			Map<Integer,Map<String,String>> tableParams = (Map<Integer, Map<String, String>>) context.getGlobalParam(TableMacro.class.getName());
			if(tableParams == null){
				//init
				tableParams = new HashMap<Integer, Map<String,String>>();
				context.putGlobalParam(TableMacro.class.getName(), tableParams);
			}
			tableParams.put(tableId, cellParams);
		}
		if(layout){
			styleBuf.append("width:100%;border-width:0px;");
			//!!!please note: this size has same value in MceInsertTableMacro.java, please keep consist.
			tableAttrsBuf.append("cellpadding=\"5\" cellspacing=\"5\"");
			cellParams.put(IS_GRID_LAYOUT, Boolean.TRUE.toString());
		}else{ //table
				
			String borderColor = params.getParam(BORDER_COLOR);
			String borderWidth = params.getParam(BORDER_WIDTH);
			if(!StringUtils.isBlank(borderWidth)){
				borderWidth = GwtUtils.removeUnit(borderWidth);
			}
			String cellStyle = StringUtils.isBlank(borderColor)?"":("border-color:"+borderColor);
			cellStyle = (StringUtils.isBlank(cellStyle)?"":cellStyle+";") + (StringUtils.isBlank(borderWidth)?"":"border-width:"+borderWidth+"px");
			cellStyle = StringUtils.isBlank(cellStyle)?"":("style='"+cellStyle+"'");
			
			cellParams.put(CELL_STYLE, cellStyle);
			cellParams.put(FIRST_LINE_AS_TITLE, params.getParam(FIRST_LINE_AS_TITLE));
			cellParams.put(IS_GRID_LAYOUT, Boolean.FALSE.toString());
			
			//table style
			String bgColor = params.getParam(BG_COLOR);
			String width = params.getParam(NameConstants.WIDTH);
			String height = params.getParam(NameConstants.HEIGHT);
			
			
			if(!StringUtil.isBlank(borderColor)){
				styleBuf.append("border-color:").append(borderColor).append(";");
			}
			if(!StringUtil.isBlank(bgColor)){
				styleBuf.append("background-color:").append(bgColor).append(";");
			}
			if(!StringUtil.isBlank(width)){
				styleBuf.append("width:").append(width).append(";");;
			}
			if(!StringUtil.isBlank(height)){
				styleBuf.append("height:").append(height).append(";");;
			}
			if(!StringUtil.isBlank(borderWidth)){
				styleBuf.append("border-width:").append(borderWidth).append("px;");;
			}
		}	
		
		if(styleBuf.length() > 0){
			styleBuf.insert(0, "style=\"");
			styleBuf.append("\"");
		}
		
		String content = params.getContent();
		buffer.append("<table class=").append(layout?"\"macroGrid\"":"\"macroTable\"")
			.append(styleBuf.length() ==0?"":" ").append(styleBuf)
			.append(tableAttrsBuf.length() ==0?"":" ").append(tableAttrsBuf)
			.append("><tr>").append(content).append("</tr></table>");
	}

	public boolean isPaired() {
		return true;
	}

	public String[] hasChildren(){
		return new String[]{TableRowMacro.NAME,TableCellMacro.NAME};
	}

	public GroupProcessor newGroupProcessor(Macro macro, int start, int end) {
		
		return new TableGroupProcessor(macro,start, end);
	}
	
	@Override
	public String getHTMLIdentifier() {
		return "<table>";
	}
	protected void replaceHTML(HTMLNode node, ListIterator<HTMLNode> iter, RenderContext context) {
		HTMLNode pair = node.getPair();
		if(pair == null){
			log.error("Unexpected: no close table tag.");
			return;
		}
		Map<String, String> atts = node.getAttributes();
		//this special check for Firefox 3.5.5 in Mac, in rich editor, insert Attachment macro, 
		//TinyMCE just remove <div> tag which suppose holding aid="attachments". So we check here over table.
		if(atts != null && "attachments".equalsIgnoreCase(atts.get(NameConstants.NAME))){
			return;
		}
		
		boolean grid = node.getAttributes() != null
				&& node.getAttributes().get(NameConstants.CLASS) != null
				&& node.getAttributes().get(NameConstants.CLASS).toLowerCase().indexOf("macrogrid") != -1;
		if(!grid){
			String support = isSimpleTableSupport(node, pair);
			if(support == null){
				//this table can be render as simple table, so just skip it
				return;
			}else{
				log.info("Complex table will process as unsupport feature of simple table:" + support);
			}
		}		
		
		//ok, do complex table/grid macro replacement
		
		//replace td/th/tr
		boolean firstRow = true;
		boolean hasTitle = false;
		HTMLNode subnode = node.next();
		while(subnode != null && subnode != pair){
			if(subnode.isTextNode()){
				subnode = subnode.next();
				continue;
			}
			
			if(StringUtils.equalsIgnoreCase("tr", subnode.getTagName())){
				if(firstRow){
					subnode.reset("", true);
					if(subnode.getPair() != null){
						subnode.getPair().reset("", true);
					}
					firstRow = false;
				}else{
					subnode.reset("\n{rowdiv}\n", true);
					if(subnode.getPair() != null){
						subnode.getPair().reset("", true);
					}
				}
			}else if(StringUtils.equalsIgnoreCase("th", subnode.getTagName())){
				//there is possible align attributes in th, so get it out
				String[] surr = getCellAlignAtts(subnode);
				String attr = getCellAtts(subnode);
				if(subnode.getPair() != null){
					subnode.reset("{cell"+attr+"}" + surr[0], true);
					subnode.getPair().reset(surr[1] + "{cell}", true);
				}else{
					subnode.reset("", true);
					AuditLogger.error("No matched th");
				}
				hasTitle = true;
			}else if(StringUtils.equalsIgnoreCase("td", subnode.getTagName())){
				//there is possible align attributes in td, so get it out
				String[] surr = getCellAlignAtts(subnode);
				String attr = getCellAtts(subnode);
				if(subnode.getPair() != null){
					subnode.reset("{cell"+attr+"}" + surr[0], true);
					subnode.getPair().reset(surr[1] + "{cell}", true);
				}else{
					subnode.reset("", true);
					AuditLogger.error("No matched td");
				}
			}else if(StringUtils.equalsIgnoreCase("tbody", subnode.getTagName())
					||StringUtils.equalsIgnoreCase("caption", subnode.getTagName())){
				//no process now
				subnode.reset("", true);
				if(subnode.getPair() != null){
					subnode.getPair().reset("", true);
				}
			}
			
			subnode = subnode.next();
		}
		
		if(grid){
			resetMacroMarkup(TIDY_STYLE_BLOCK, node, iter, "{grid}", "{grid}");
		}else{
			//parse table attribute and reset table macro
			StringBuffer attr = new StringBuffer();
			if(hasTitle){
				attr.append("hasTitle=yes");
			}
			
			if(atts != null){ 
				//width
				String width = GwtUtils.removeUnit(atts.get(NameConstants.WIDTH));
				if(!StringUtils.isBlank(width)){
					if(attr.length() >0)attr.append("|");
					attr.append("width=").append(width);
				}
				//height
				String height = GwtUtils.removeUnit(atts.get(NameConstants.HEIGHT));
				if(!StringUtils.isBlank(height)){
					if(attr.length() >0)attr.append("|");
					attr.append("height=").append(height);
				}
				if(atts.get(NameConstants.STYLE) != null){
					Map<String, String> style = node.getStyle();
					//width
					width = GwtUtils.removeUnit(style.get(NameConstants.WIDTH));
					if(!StringUtils.isBlank(width)){
						if(attr.length() >0)attr.append("|");
						attr.append("width=").append(width);
					}
					//height
					height = GwtUtils.removeUnit(style.get(NameConstants.HEIGHT));
					if(!StringUtils.isBlank(height)){
						if(attr.length() >0)attr.append("|");
						attr.append("height=").append(height);
					}
					//background color
					String bgcolor = style.get("background-color");
					if(!StringUtils.isBlank(bgcolor)){
						if(attr.length() >0)attr.append("|");
						attr.append("bgColor=").append(bgcolor);
					}
					//border color
					String bordercolor = style.get("border-color");
					if(!StringUtils.isBlank(bordercolor)){
						if(attr.length() >0)attr.append("|");
						attr.append("borderColor=").append(bordercolor);
					}
					//border width
					String borderW = GwtUtils.removeUnit(style.get("border-width"));
					if(!StringUtils.isBlank(borderW)){
						if(attr.length() >0)attr.append("|");
						attr.append("borderWidth=").append(borderW);
					}
					//maybe borderWidith, borderColor mixed into same border style
					String border = style.get("border");
					if(!StringUtils.isBlank(border)){
						//return width,style,color of border
						String[] borderAttr = parseBorder(border);
						if(borderAttr[0] != null){
							if(attr.length() >0)attr.append("|");
							attr.append("borderWidth=").append(GwtUtils.removeUnit(borderAttr[0]));
						}
						
						//ignore borderStyle now...
						
						if(borderAttr[2] != null){
							if(attr.length() >0)attr.append("|");
							attr.append("borderColor=").append(borderAttr[2]);
						}
					}
				}
			}
			//add separator
			if(attr.length() > 0){
				attr.insert(0, ":");
			}
			resetMacroMarkup(TIDY_STYLE_BLOCK, node, iter, "{table"+attr+"}", "{table}");
		}
			
	}

	/**
	 * Just following very basic rule:
	 * if first char is digit, then it is border width;
	 * if first char is #, then it is color
	 * otherwise, style
	 * 
	 * @param border
	 * @return border Width, Style and Color
	 */
	private String[] parseBorder(String border) {
		String[] battr = new String[3];
		String[] parts = border.split(" ");
		for (String att : parts) {
			if(NumberUtil.isDigit(att.charAt(0))){
				battr[0] = att;
			}else if(att.charAt(0) == '#'){
				battr[2] = att;
			}else{
				battr[1] = att;
			}
		}
		if(parts.length > 3){
			AuditLogger.warn("Border CSS attribute has more than 3 parts:" + border);
		}
		return battr;
	}

	/*
	 * Get cell(td/th) attribute, return start and end macro if it has.
	 * At the moment, it detect "text-align", "colspan", "rowspan".
	 */
	private String[] getCellAlignAtts(HTMLNode subnode){
		String[] surr = new String[]{"",""};
		
		if(subnode.getAttributes() != null && subnode.getAttributes().size() > 0){
			Map<String, String> atts = subnode.getAttributes();
			if(atts.get(NameConstants.STYLE) != null){
				//text-align attribute supported
				String align = subnode.getStyle().get("text-align");
				if(align != null){
					if("center".equalsIgnoreCase(align)){
						surr[0] = "{align:align=center}";
						surr[1] = "{align}";
					}else if("right".equalsIgnoreCase(align)){
						surr[0] = "{align:align=right}";
						surr[1] = "{align}";
					}
				}
			}
		}
		
		return surr;
	}
	private String getCellAtts(HTMLNode subnode){
		StringBuffer surr = new StringBuffer();
		
		if(subnode.getAttributes() != null && subnode.getAttributes().size() > 0){
			Map<String, String> atts = subnode.getAttributes();
			String colspan = atts.get(NameConstants.COLSPAN);
			if(colspan != null && NumberUtils.toInt(colspan, -1) != -1){
				surr.append(":colspan=").append(colspan);
			}
			String rowspan = atts.get(NameConstants.ROWSPAN);
			if(rowspan != null && NumberUtils.toInt(rowspan, -1) != -1){
				if(surr.length()>0)
					surr.append("|");
				else
					surr.append(":");
				surr.append("rowspan=").append(rowspan);
			}
		}
		return surr.toString();
	}
	
	
	/**
	 * @param node
	 * @param pair
	 * @return
	 */
	private String isSimpleTableSupport(HTMLNode node, HTMLNode pair) {
		String support = null;
		int rowTextLen = 0;
		HTMLNode subnode = node;
		while(subnode != null && subnode != pair){
			if(subnode.isCloseTag()){
				subnode = subnode.next();
				continue;
			}
			
			if("table".equalsIgnoreCase(subnode.getTagName())){
				if(subnode.getAttributes() != null && subnode.getAttributes().size() > 0){
					Map<String, String> atts = subnode.getAttributes();
					boolean valid = true;
					if(!StringUtils.isBlank(atts.get(NameConstants.STYLE))){
						//only text-align attribute supported
						removeDefaultStyleValue(subnode);
						if(subnode.getStyle() != null){
							//need check style is null again, as removeDefaultStyleValue() may delete existed style if all values are default
							if(!StringUtil.isBlank(subnode.getStyle().get("border"))
								|| !StringUtil.isBlank(subnode.getStyle().get("border-width"))
								|| !StringUtil.isBlank(subnode.getStyle().get("border-color"))
								|| !StringUtil.isBlank(subnode.getStyle().get("background-color"))
								|| !StringUtil.isBlank(subnode.getStyle().get("width"))
								|| !StringUtil.isBlank(subnode.getStyle().get("height"))){
								valid = false;
							}
						}						
					}
					if(!valid){
						support = "Table has border/bgColor/width/height" + subnode.getTagName();
						break;
					}
				}
			}else if("tr".equalsIgnoreCase(subnode.getTagName())){
				rowTextLen = 0;
				removeDefaultStyleValue(subnode);
				support = isSupportAttribute(subnode);
				if(support != null)
					break;
			}else if("td".equalsIgnoreCase(subnode.getTagName()) || "th".equalsIgnoreCase(subnode.getTagName())){
				removeDefaultStyleValue(subnode);
				support = isSupportAttribute(subnode);
				if(support != null)
					break;
			}else if(!subnode.isTextNode()){
				//there are special case: if <td><p>abc</p></td>, although <p> surrounding text, but it is close to td tag,  
				//in this case, I just delete them...
				if("p".equalsIgnoreCase(subnode.getTagName())){
					if(subnode.previous() != null && subnode.getPair() != null && subnode.getPair().next() != null
						&& "td".equalsIgnoreCase(subnode.previous().getTagName()) 
						&& subnode.getPair().next() == subnode.previous().getPair()){
						//remove this <p> or <div> pair if no attribute
						if(subnode.getAttributes() == null || subnode.getAttributes().size() == 0){
							subnode.reset("", true);
							subnode.getPair().reset("", true);
						}else{
							//if they have any attribute, most possible is text-align. Then reset them into non-block tag
							//so that simple table still can process them
							subnode.resetTagName("span");
							subnode.getPair().resetTagName("span");
						}
						subnode = subnode.next();
						continue;
					}
				}
				
				//is blocked tag?
				if(RenderUtil.isBlockTag(subnode)){
					support = "Unspport tag in cell " + subnode;
					break;
				}
			}else{
				//text node
				rowTextLen += subnode.getText().length();
				if(rowTextLen > TableFilter.MAX_ROW_TEXT_LEN){
					support = "Too long row text.";
					break;
				}

			}
			subnode = subnode.next();
		}
		return support;
	}
	
	/**
	 * If table/tr/th etc has default value(macroTable style in CSS). just remove them.
	 * @param subnode
	 */
	private void removeDefaultStyleValue(HTMLNode subnode) {
		Map<String, String> styles = subnode.getStyle();
		if(styles == null)
			return;
		
		if(SharedConstants.TABLE_BORDER_DEFAULT_WIDHT.equals(GwtUtils.removeUnit(styles.get("border-width")))){
			subnode.removeStyle("border-width","*");
		}
		if(SharedConstants.TABLE_BORDER_DEFAULT_COLOR.equals(StringUtils.trim(styles.get("border-color")))){
			subnode.removeStyle("border-color","*");
		}
		if(!StringUtils.isBlank(styles.get("border"))){
			String[] borderAttr = parseBorder(styles.get("border").trim());
			boolean defaultV = true;
			if(borderAttr[0] != null){
				if(!SharedConstants.TABLE_BORDER_DEFAULT_WIDHT.equals(GwtUtils.removeUnit(borderAttr[0]))){
					defaultV = false;
				}
			}
			if(defaultV && borderAttr[2] != null){
				if(!SharedConstants.TABLE_BORDER_DEFAULT_COLOR.equals(borderAttr[2])){
					defaultV = false;
				}
			}
			if(defaultV){
				subnode.removeStyle("border","*");
			}
		}
		
		if("table".equalsIgnoreCase(subnode.getTagName())
			||"tr".equalsIgnoreCase(subnode.getTagName())
			|| "td".equalsIgnoreCase(subnode.getTagName())){
			if(SharedConstants.TABLE_BG_DEFAULT_COLOR.equals(StringUtils.trim(styles.get("background-color")))
				||"transparent".equalsIgnoreCase(StringUtils.trim(styles.get("background-color")))){
				subnode.removeStyle("background-color","*");
			}
		}else if("th".equalsIgnoreCase(subnode.getTagName())){
			//TODO: does not check color(font) for th, which default value is SharedConstants.TABLE_TH_DEFAULT_COLOR 
			if(SharedConstants.TABLE_TH_DEFAULT_BG_COLOR.equals(StringUtils.trim(styles.get("background-color")))){
				subnode.removeStyle("background-color","*");
			}
		}
		
		
	}


	/**
	 * @param subnode
	 * @return
	 */
	private String isSupportAttribute(HTMLNode subnode) {
		String support = null;
		if(subnode.getAttributes() != null && subnode.getAttributes().size() > 0){
			Map<String, String> atts = subnode.getAttributes();
			int validS = 0;
			if(!StringUtils.isBlank(atts.get(NameConstants.CLASS))){
				validS++;
			}
			if(!StringUtils.isBlank(atts.get(NameConstants.STYLE))){
				//only text-align attribute supported
				if(subnode.getStyle().get("text-align") != null
				 && subnode.getStyle().size() == 1){
					validS++;
				}
				
			}
			if(atts.size() != validS ){
				StringBuffer s = new StringBuffer();
				for(Entry<String, String> entry:atts.entrySet())
					s.append(entry.getKey()).append("-").append(entry.getValue());
				
				support = s+ "||"+validS +"No attribute supprot in TR, TD or TH - " + subnode ;
			}
		}

		return support;
	}
}
