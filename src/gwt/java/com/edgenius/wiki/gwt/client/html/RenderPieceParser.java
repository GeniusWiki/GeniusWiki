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

import com.allen_sauer.gwt.log.client.Log;
import com.edgenius.wiki.gwt.client.model.LinkModel;
import com.edgenius.wiki.gwt.client.model.MacroModel;
import com.edgenius.wiki.gwt.client.model.RenderPiece;
import com.edgenius.wiki.gwt.client.model.TextModel;
import com.edgenius.wiki.gwt.client.server.utils.NameConstants;
import com.edgenius.wiki.gwt.client.server.utils.RichTagUtil;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;

/**
 * Give richText to parse out the RenderPiece list
 * @author Dapeng.Ni
 */
public class RenderPieceParser {

	/**
	 * Convert rich html tag text to RenderPiece list. As only LinkModel is required in RenderPiece, so, only   
	 * the text contains tags like <a wajax="">, which is able to convert LinkModel. 
	 * 
	 * <br>
	 * So far, this parse method is compliance with RichTagUtil AttributeString solution. This means, the text would be
	 * <a wajax=""> 
	 * 
	 * @param richText
	 * @param skipNoRender ignore tag with attribute aid="norender"
	 * @return
	 */
	public static ArrayList<RenderPiece> parse(String richText, boolean skipNoRender) {
		if(richText ==null)
			//don't return null
			return new ArrayList<RenderPiece>();
		
		HTMLNodeContainer nodeContainer = getHTMLNodeList(richText, skipNoRender);
		
		return renderToPieceList(nodeContainer);
	}

	
	/**
	 * Parse HTML rich tag to find out <div aid="piece"> tag and return embedded RenderPiece list.
	 * @param content The rich HTML tags content. 
	 * @param pieceName
	 * @return If return null, this means pieceName not found. It is different with return empty list, which means the piece exist but nothing included.
	 */
	public static ArrayList<RenderPiece> parsePiece(String richText, String pieceName, boolean skipNoRender) {
		if(richText ==null)
			return null;
		
		if(StringUtil.isBlank(pieceName)){
			//piece is blank, return whole content RenderPiece
			return parse(richText,skipNoRender);
		}
		
		HTMLNodeContainer nodeContainer = getHTMLNodeList(richText, skipNoRender);
		HTMLNodeContainer pieceNodeContainer = new HTMLNodeContainer();
		HTMLNode endNode = null;
		boolean found = false;
		for (Iterator<HTMLNode> iter = nodeContainer.iterator();iter.hasNext();) {
			HTMLNode node = iter.next();
			if(endNode != null){
				//the start <div> tag already found - then endNode is not null
				if(node == endNode){
					endNode = null;
					//Piece is end
					break;
				}
				//put all embedded HTMLNode into another container
				pieceNodeContainer.add(node);
				continue;
			}
			
			if("div".equalsIgnoreCase(node.getTagName()) && node.getAttributes() != null 
					&& "piece".equalsIgnoreCase(node.getAttributes().get(NameConstants.AID))
					&& pieceName.equalsIgnoreCase(node.getAttributes().get(NameConstants.NAME))){
				//found the given name piece, then put all embedded RenderPiece to return list
				endNode = node.getPair();
				found = true;
				Log.info("Find piece tag for " + pieceName);
			}
		}
		
		if(!found)
			return null;
		else{
			return renderToPieceList(nodeContainer);
		}
	}
	
	//********************************************************************
	//               Private methods
	//********************************************************************
	/**
	 * @param pieces
	 * @param nodeContainer
	 * @return 
	 */
	private static ArrayList<RenderPiece> renderToPieceList(HTMLNodeContainer nodeContainer) {
		ArrayList<RenderPiece> pieces = new ArrayList<RenderPiece>();
		
		StringBuffer text = new StringBuffer();
		for (Iterator<HTMLNode> iter = nodeContainer.iterator();iter.hasNext();) {
			HTMLNode node = iter.next();
			
			if("a".equalsIgnoreCase(node.getTagName()) && node.getAttributes() != null 
				&& node.getAttributes().get(NameConstants.WAJAX) != null){
				//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
				// LinkModel
				//add text before this model object 
				if(text.length() > 0){
					pieces.add(new TextModel(text.toString()));
					text = new StringBuffer();
				}
				String enclosedText = "";
				if(node.getPair() != null){
					while(iter.hasNext()){
						HTMLNode ed = iter.next();
						if(node.getPair() == ed){
							break;
						}
						enclosedText += ed.getText();
					}
				}
				LinkModel ln = new LinkModel();
				ln.fillToObject(node.getText(), enclosedText);
				pieces.add(ln);
			}else if(MacroModel.isMacroModel(node)){
				//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
				// MacroModel
				//add text before this model object 
				if(text.length() > 0){
					pieces.add(new TextModel(text.toString()));
					text = new StringBuffer();
				}
				
				MacroModel mm = new MacroModel();
				mm.fillToObject(node.getText(), "");
				if(mm.macroName != null){
					pieces.add(mm);
				}else{
					//failed fill image macro, then just treat it as pure text
					text.append(node.getText());
				}
			}else{
				text.append(node.getText());
			}
		}
		if(text.length() > 0){
			pieces.add(new TextModel(text.toString()));
			text = new StringBuffer();
		}
		
		return pieces;
	}

	/**
	 * @param richText
	 * @param skipNoRender
	 * @return
	 */
	private static HTMLNodeContainer getHTMLNodeList(String richText, boolean skipNoRender) {
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// Parse html text into HTMLNode list
		HtmlNodeListenerImpl listener = new HtmlNodeListenerImpl();
		HtmlParser htmlParser = new HtmlParser();
		htmlParser.scan(richText, listener);
		//get HTML node list
		HTMLNodeContainer nodeContainer = listener.getHtmlNode();
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// remove all tag which has id or aid is "norender"
		if(skipNoRender){
			//remove aid="norender" tag
			RichTagUtil.removeNoRenderTag(nodeContainer);
		}
		return nodeContainer;
	}

	

}
