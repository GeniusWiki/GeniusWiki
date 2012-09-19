		<%-- tinymce  --%>
   		<script type="text/javascript" src="${ctxPath}/widgets/tiny_mce/tiny_mce.js?v=@TOKEN.SITE.VERSION@"></script>
   		<script type="text/javascript" src="${ctxPath}/widgets/tiny_mce/plugins/table/js/table.js?v=@TOKEN.SITE.VERSION@"></script>
   		<script type="text/javascript" src="${ctxPath}/widgets/tiny_mce/plugins/table/js/merge_cells.js?v=@TOKEN.SITE.VERSION@"></script>
   		<script type="text/javascript" src="${ctxPath}/widgets/tiny_mce/plugins/wbanchor/js/anchor.js?v=@TOKEN.SITE.VERSION@"></script>

		<!--  flash copy text -->
 		<script type="text/javascript" src='${ctxPath}/widgets/zeroclipboard/ZeroClipboard.js?v=@TOKEN.SITE.VERSION@'></script>
 		
 		<%-- These comment extract from page-edit.js - just for use JSP comment to avoid render to front end --%>
 		<%-- The reason use token rather put plugins here is for ant script will unzip tinyMCE from Ivy nonjava dependencies,
    	ant properties decides which plugin will be in tinyMCE plugin directory. This especially useful for offline mode, as it need 
    	download entire tinyMCE library --%>
 		
		<%--disable more colors(palette) to avoid trouble:
	    * Default color picker is already change for gwt popup dialog for table bg and border color usage.
	    * For new GWT style popup, IE can not restoreBookmark() correctly - I cannot point same solution like Link, Anchor etc.
	    * For all browsers, it won't auto hide toolbar button dropdown(default colors palette), so there is overlap popup and dropdown
	    If want to turn on this feature later- need modify themes/advance/editor_template.js and add code below "theme_advanced_more_colors"
	    	gwtPaletteDialog(t.editor.id, c.id, c.value, function(co) {
					c.setColor(co);
				}
			);
	     --%>
		<script type="text/javascript" src="${ctxPath}/static/scripts/page-edit.js?v=@TOKEN.SITE.VERSION@"></script>