ZeroClipboard.setMoviePath(ctxPath+'/widgets/zeroclipboard/ZeroClipboard.swf' );
						
tinyMCE.init({
	apply_source_formatting : false,
	forced_root_block : false,
	force_p_newlines: true,
	entity_encoding : "raw",
	content_css : skinPath+"/styles/render.css", 
    mode : "textareas",
    theme : "advanced",
    plugins : "wbimage,wbplaintext,wblink,wbanchor,wbemotions,wbremovenonedit,wbmacros,trailing,table,contextmenu@TOKEN.TINYMCE.PLUGIN@",
    theme_advanced_buttons1 : "cut,copy,paste,|,bold,italic,underline,strikethrough,sub,sup,|,bullist,numlist"+
        ",|,justifyleft,justifycenter,justifyright,|,link,unlink,anchor,|,formatselect,fontselect,fontsizeselect,forecolor,backcolor,|,removeformat",
    theme_advanced_buttons2 :  "undo,redo,|,table,delete_col,delete_row,col_after,col_before,row_after,row_before,split_cells,merge_cells,|,"+
         "image,hr,charmap,emotions,|,wbmacros,|,@TOKEN.GWT.DEBUG.TINYMCE.CODE@wbplaintext",
    theme_advanced_buttons3 : "",
    
 	theme_advanced_blockformats : "p,h1,h2,h3,h4,h5,h6,pre,blockquote", 
    theme_advanced_more_colors : 0,
    theme_advanced_toolbar_location : "top",
    theme_advanced_toolbar_align : "left",
    theme_advanced_path_location : "bottom",
	extended_valid_elements : "@[aid|wajax|name|id|class|style|title|dir<ltr?rtl|lang|xml::lang|onclick|ondblclick|"
		+ "onmousedown|onmouseup|onmouseover|onmousemove|onmouseout|onkeypress|"
		+ "onkeydown|onkeyup],a[rel|rev|charset|hreflang|tabindex|accesskey|type|"
		+ "href|target|title|class|onfocus|onblur],img[aid|longdesc|usemap|"
		+ "src|border|alt|title|hspace|vspace|width|height|align],iframe[width|height|src],div,span,pre,table[cellpadding|cellspacing]",
     setup : function(ed) {
          ed.onInit.add(function(ed) {
        	 gwtTinyMCEEventOnInit(ed.id);
          });
	      ed.onChange.add(function(ed) {
	    	  gwtTinyMCEEventOnChange(ed.id);
	      });
	      ed.onActivate.add(function(ed) {
	    	  gwtTinyMCEEventOnFocus(ed.id,true);
	      });
	      ed.onDeactivate.add(function(ed) {
	    	  gwtTinyMCEEventOnFocus(ed.id,false);
	      });
	      ed.onKeyPress.add(function(ed, e) {
	    	  var ret = gwtTinyMCEEventOnKeyPress(ed.id, e.ctrlKey,e.altKey,e.shiftKey,e.metaKey, e.keyCode, e.charCode);
	    	 if(!ret)
	    		 tinymce.dom.Event.cancel(e);
    		 return ret;
	      });
	      ed.onKeyDown.add(function(ed, e) {
	    	 var ret = gwtTinyMCEEventOnKeyDown(ed.id, e.ctrlKey,e.altKey,e.shiftKey,e.metaKey, e.keyCode, e.charCode);
	    	 if(!ret)
	    		 tinymce.dom.Event.cancel(e);
    		 return ret;
	      });
	      ed.onKeyUp.add(function(ed, e) {
	    	  var ret =  gwtTinyMCEEventOnKeyUp(ed.id, e.ctrlKey,e.altKey,e.shiftKey,e.metaKey, e.keyCode, e.charCode);
	    	  if(!ret)
		    		 tinymce.dom.Event.cancel(e);
	    	  return ret;
	      });

	}		
});

//this message will replace on GWT
var message = "Are you sure leaving?";
function beforeUnload(){
	  if (typeof evt == 'undefined') {
		 evt = window.event;
	  }
	  if (evt) {
		evt.returnValue = message;
	  }
	  return message;
}