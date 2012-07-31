		<%-- JQuery, to avoid conflict with prototype, put it here, but hope it replace prototype feature as system level JS library --%>
		<script language='javascript' type="text/javascript" src='${ctxPath}/widgets/jquery/jquery.min.js?v=@TOKEN.SITE.VERSION@'></script>
		<script language='javascript' type="text/javascript" src='${ctxPath}/widgets/jquery/qtip/jquery.qtip-1.0.0-rc3.min.js?v=@TOKEN.SITE.VERSION@'></script>
		<script language='javascript' type="text/javascript" src='${ctxPath}/widgets/jquery/jquery-lightbox/js/jquery.lightbox.pack.js?v=@TOKEN.SITE.VERSION@'></script>
		<script language='javascript' type="text/javascript" src='${ctxPath}/widgets/jquery/autoresize-textarea/autoresize.jquery.min.js?v=@TOKEN.SITE.VERSION@'></script>
		<script language='javascript' type="text/javascript" src='${ctxPath}/widgets/jquery/scrollto/jquery.scrollto-min.js?v=@TOKEN.SITE.VERSION@'></script>
		<link type="text/css" rel="stylesheet" href="${ctxPath}/widgets/jquery/jquery-lightbox/css/jquery.lightbox.css?v=@TOKEN.SITE.VERSION@" />
		
		<script language='javascript' src="${ctxPath}/static/scripts/pages.js?v=@TOKEN.SITE.VERSION@" /></script>
		<%-- dp syntax highlighter --%>
		<link type="text/css" rel="stylesheet" href="${ctxPath}/widgets/dp_syntaxhighlighter/styles/shCoreEclipse.css?v=@TOKEN.SITE.VERSION@">

   		<script language="javascript" type="text/javascript" src="${ctxPath}/widgets/dp_syntaxhighlighter/scripts/shCore.js?v=@TOKEN.SITE.VERSION@"></script>
   		<script language="javascript" type="text/javascript" src="${ctxPath}/widgets/dp_syntaxhighlighter/scripts/shBrushCSharp.js?v=@TOKEN.SITE.VERSION@"></script>
		<script language="javascript" type="text/javascript" src="${ctxPath}/widgets/dp_syntaxhighlighter/scripts/shBrushCpp.js?v=@TOKEN.SITE.VERSION@"></script>
		<script language="javascript" type="text/javascript" src="${ctxPath}/widgets/dp_syntaxhighlighter/scripts/shBrushColdFusion.js?v=@TOKEN.SITE.VERSION@"></script>
		<script language="javascript" type="text/javascript" src="${ctxPath}/widgets/dp_syntaxhighlighter/scripts/shBrushBash.js?v=@TOKEN.SITE.VERSION@"></script>
		<script language="javascript" type="text/javascript" src="${ctxPath}/widgets/dp_syntaxhighlighter/scripts/shBrushAS3.js?v=@TOKEN.SITE.VERSION@"></script>
		<script language="javascript" type="text/javascript" src="${ctxPath}/widgets/dp_syntaxhighlighter/scripts/shBrushAppleScript.js?v=@TOKEN.SITE.VERSION@"></script>
		<script language="javascript" type="text/javascript" src="${ctxPath}/widgets/dp_syntaxhighlighter/scripts/shBrushXml.js?v=@TOKEN.SITE.VERSION@"></script>
		<script language="javascript" type="text/javascript" src="${ctxPath}/widgets/dp_syntaxhighlighter/scripts/shBrushVb.js?v=@TOKEN.SITE.VERSION@"></script>
		<script language="javascript" type="text/javascript" src="${ctxPath}/widgets/dp_syntaxhighlighter/scripts/shBrushSql.js?v=@TOKEN.SITE.VERSION@"></script>
		<script language="javascript" type="text/javascript" src="${ctxPath}/widgets/dp_syntaxhighlighter/scripts/shBrushScala.js?v=@TOKEN.SITE.VERSION@"></script>
		<script language="javascript" type="text/javascript" src="${ctxPath}/widgets/dp_syntaxhighlighter/scripts/shBrushSass.js?v=@TOKEN.SITE.VERSION@"></script>
		<script language="javascript" type="text/javascript" src="${ctxPath}/widgets/dp_syntaxhighlighter/scripts/shBrushRuby.js?v=@TOKEN.SITE.VERSION@"></script>
		<script language="javascript" type="text/javascript" src="${ctxPath}/widgets/dp_syntaxhighlighter/scripts/shBrushPython.js?v=@TOKEN.SITE.VERSION@"></script>
		<script language="javascript" type="text/javascript" src="${ctxPath}/widgets/dp_syntaxhighlighter/scripts/shBrushPowerShell.js?v=@TOKEN.SITE.VERSION@"></script>
		<script language="javascript" type="text/javascript" src="${ctxPath}/widgets/dp_syntaxhighlighter/scripts/shBrushPhp.js?v=@TOKEN.SITE.VERSION@"></script>
		<script language="javascript" type="text/javascript" src="${ctxPath}/widgets/dp_syntaxhighlighter/scripts/shBrushPerl.js?v=@TOKEN.SITE.VERSION@"></script>
		<script language="javascript" type="text/javascript" src="${ctxPath}/widgets/dp_syntaxhighlighter/scripts/shBrushJScript.js?v=@TOKEN.SITE.VERSION@"></script>
		<script language="javascript" type="text/javascript" src="${ctxPath}/widgets/dp_syntaxhighlighter/scripts/shBrushJava.js?v=@TOKEN.SITE.VERSION@"></script>
		<script language="javascript" type="text/javascript" src="${ctxPath}/widgets/dp_syntaxhighlighter/scripts/shBrushJavaFX.js?v=@TOKEN.SITE.VERSION@"></script>
		<script language="javascript" type="text/javascript" src="${ctxPath}/widgets/dp_syntaxhighlighter/scripts/shBrushGroovy.js?v=@TOKEN.SITE.VERSION@"></script>
		<script language="javascript" type="text/javascript" src="${ctxPath}/widgets/dp_syntaxhighlighter/scripts/shBrushErlang.js?v=@TOKEN.SITE.VERSION@"></script>
		<script language="javascript" type="text/javascript" src="${ctxPath}/widgets/dp_syntaxhighlighter/scripts/shBrushDiff.js?v=@TOKEN.SITE.VERSION@"></script>
		<script language="javascript" type="text/javascript" src="${ctxPath}/widgets/dp_syntaxhighlighter/scripts/shBrushDelphi.js?v=@TOKEN.SITE.VERSION@"></script>
		<script language="javascript" type="text/javascript" src="${ctxPath}/widgets/dp_syntaxhighlighter/scripts/shBrushCss.js?v=@TOKEN.SITE.VERSION@"></script>
		<script language="javascript" type="text/javascript" src="${ctxPath}/widgets/dp_syntaxhighlighter/scripts/shBrushCommon.js?v=@TOKEN.SITE.VERSION@"></script>

		<script language="javascript">
			<%-- This method is call once after GWT complete initilised. Note The $(document).ready() will trigger before GWT initialised! --%>
			function documentReadyCallback() {

				<%-- Bind sidebar buttons trigger div in /static/scripts/pages.js --%>
				bindSidebarButtons();
				
			}
			
			<%-- Call on every render completed --%>
			function renderCallback(renderPanelID){
				<%-- only FF (test in 3.5) automatically eval script when inserts script in render panel.
				To avoid duplicated execute same script in FF, it won't run this piece code. 
				If runs duplciated, some script won't work. For example, datepicker, it won't display.
				jQuery.support.scriptEval only return false for IE 
				2012/04/15 - after FF 4, above is not true anymore, so here also check version.
				--%>
				if( !jQuery.browser.mozilla || parseFloat(jQuery.browser.version) >= 2.0){
					$("#"+renderPanelID).find("script").each(function(){
						if ( $(this).attr("src") ) {
							jQuery.ajax({
								url: $(this).attr("src"),
								async: false,
								dataType: "script"
							});
						} else {
							jQuery.globalEval( $(this).html());
						}
					});
				}
				$("#"+renderPanelID +" .macroGallery").each(function(){
			        $('#'+this.id + ' a').lightBox({
			        	overlayBgColor: '#445',
			        	overlayOpacity: 0.6,
			        	imageBlank: '${ctxPath}/widgets/jquery/jquery-lightbox/images/lightbox-blank.gif',
			        	imageLoading: '${ctxPath}/widgets/jquery/jquery-lightbox/images/lightbox-ico-loading.gif',
			        	imageBtnClose: '${ctxPath}/widgets/jquery/jquery-lightbox/images/lightbox-btn-close.gif',
			        	imageBtnPrev: '${ctxPath}/widgets/jquery/jquery-lightbox/images/lightbox-btn-prev.gif',
			        	imageBtnNext: '${ctxPath}/widgets/jquery/jquery-lightbox/images/lightbox-btn-next.gif',
			        	containerResizeSpeed: 350,
			        	txtImage: 'Image'
			           })
			   	});
			   	
			   	$("#"+renderPanelID +" a.renderImage").each(function(){
			        $(this).lightBox({
			        	overlayBgColor: '#445',
			        	overlayOpacity: 0.6,
			        	imageBlank: '${ctxPath}/widgets/jquery/jquery-lightbox/images/lightbox-blank.gif',
			        	imageLoading: '${ctxPath}/widgets/jquery/jquery-lightbox/images/lightbox-ico-loading.gif',
			        	imageBtnClose: '${ctxPath}/widgets/jquery/jquery-lightbox/images/lightbox-btn-close.gif',
			        	imageBtnPrev: '${ctxPath}/widgets/jquery/jquery-lightbox/images/lightbox-btn-prev.gif',
			        	imageBtnNext: '${ctxPath}/widgets/jquery/jquery-lightbox/images/lightbox-btn-next.gif',
			        	containerResizeSpeed: 350,
			        	txtImage: 'Image'
			           })
			   	});

				if( $("#"+renderPanelID +" span.renderError")!=undefined){
					$("#"+renderPanelID +" span.renderError").each(function(){
						$(this).qtip({
							content: {
							text: $(this).attr("hint")
						},
						position: {
							corner: {
							target: 'bottomMiddle',
							tooltip: 'topLeft'
							}
						},
						style: 'red' 
						})
					});
				}
				
				$("#"+renderPanelID +" div.macroPanelExpander").click(function (){
					var id = $(this).attr("name");
					if($(this).hasClass("collapse")){
						$("#"+id).show("slow");
						$(this).removeClass("collapse")
					}else{
						$("#"+id).slideUp();
						$(this).addClass("collapse")
					}
					
				});
				<%-- !! This method can be call multiple times as renderCallback() will be execute on view/preview pannel etc. So far, looks that not harmful except performance --%>
				SyntaxHighlighter.highlight();
				
			}
			
			<%-- Call from GWT by ShellDialog that opened from space admin panel, Shell jsp.  --%>
			function themeChanged(){
				var elems = document.getElementsByName('URLPanel');

				for(var i = 0; i < elems.length; i++){
					var cUid = elems[i].contentWindow.document.getElementById("urlPanelChildUid").value;
					if(cUid == 'shellAdmin'){
						elems[i].contentWindow.themeChanged();
					    break;
					}
				}
			}
			
			
		</script>