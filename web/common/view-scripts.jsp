		<%-- JQuery, to avoid conflict with prototype, put it here, but hope it replace prototype feature as system level JS library --%>
		<script type="text/javascript" src='${ctxPath}/widgets/jquery/jquery.min.js?v=@TOKEN.SITE.VERSION@'></script>
		
		<script type="text/javascript" src='${ctxPath}/widgets/jquery/qtip/jquery.qtip-1.0.0-rc3.min.js?v=@TOKEN.SITE.VERSION@'></script>
		<script type="text/javascript" src='${ctxPath}/widgets/jquery/jquery-lightbox/js/jquery.lightbox.pack.js?v=@TOKEN.SITE.VERSION@'></script>
		<script type="text/javascript" src='${ctxPath}/widgets/jquery/autoresize-textarea/autoresize.jquery.min.js?v=@TOKEN.SITE.VERSION@'></script>
		<script type="text/javascript" src='${ctxPath}/widgets/jquery/scrollto/jquery.scrollto-min.js?v=@TOKEN.SITE.VERSION@'></script>
		<link type="text/css" rel="stylesheet" href="${ctxPath}/widgets/jquery/jquery-lightbox/css/jquery.lightbox.css?v=@TOKEN.SITE.VERSION@" />
		
		<script src="${ctxPath}/static/scripts/pages.js?v=@TOKEN.SITE.VERSION@" /></script>

   		<script type="text/javascript" src="${ctxPath}/widgets/dp_syntaxhighlighter/scripts/shCore.js?v=@TOKEN.SITE.VERSION@"></script>
   		<script type="text/javascript" src="${ctxPath}/widgets/dp_syntaxhighlighter/scripts/shBrushCSharp.js?v=@TOKEN.SITE.VERSION@"></script>
		<script type="text/javascript" src="${ctxPath}/widgets/dp_syntaxhighlighter/scripts/shBrushCpp.js?v=@TOKEN.SITE.VERSION@"></script>
		<script type="text/javascript" src="${ctxPath}/widgets/dp_syntaxhighlighter/scripts/shBrushColdFusion.js?v=@TOKEN.SITE.VERSION@"></script>
		<script type="text/javascript" src="${ctxPath}/widgets/dp_syntaxhighlighter/scripts/shBrushBash.js?v=@TOKEN.SITE.VERSION@"></script>
		<script type="text/javascript" src="${ctxPath}/widgets/dp_syntaxhighlighter/scripts/shBrushAS3.js?v=@TOKEN.SITE.VERSION@"></script>
		<script type="text/javascript" src="${ctxPath}/widgets/dp_syntaxhighlighter/scripts/shBrushAppleScript.js?v=@TOKEN.SITE.VERSION@"></script>
		<script type="text/javascript" src="${ctxPath}/widgets/dp_syntaxhighlighter/scripts/shBrushXml.js?v=@TOKEN.SITE.VERSION@"></script>
		<script type="text/javascript" src="${ctxPath}/widgets/dp_syntaxhighlighter/scripts/shBrushVb.js?v=@TOKEN.SITE.VERSION@"></script>
		<script type="text/javascript" src="${ctxPath}/widgets/dp_syntaxhighlighter/scripts/shBrushSql.js?v=@TOKEN.SITE.VERSION@"></script>
		<script type="text/javascript" src="${ctxPath}/widgets/dp_syntaxhighlighter/scripts/shBrushScala.js?v=@TOKEN.SITE.VERSION@"></script>
		<script type="text/javascript" src="${ctxPath}/widgets/dp_syntaxhighlighter/scripts/shBrushSass.js?v=@TOKEN.SITE.VERSION@"></script>
		<script type="text/javascript" src="${ctxPath}/widgets/dp_syntaxhighlighter/scripts/shBrushRuby.js?v=@TOKEN.SITE.VERSION@"></script>
		<script type="text/javascript" src="${ctxPath}/widgets/dp_syntaxhighlighter/scripts/shBrushPython.js?v=@TOKEN.SITE.VERSION@"></script>
		<script type="text/javascript" src="${ctxPath}/widgets/dp_syntaxhighlighter/scripts/shBrushPowerShell.js?v=@TOKEN.SITE.VERSION@"></script>
		<script type="text/javascript" src="${ctxPath}/widgets/dp_syntaxhighlighter/scripts/shBrushPhp.js?v=@TOKEN.SITE.VERSION@"></script>
		<script type="text/javascript" src="${ctxPath}/widgets/dp_syntaxhighlighter/scripts/shBrushPerl.js?v=@TOKEN.SITE.VERSION@"></script>
		<script type="text/javascript" src="${ctxPath}/widgets/dp_syntaxhighlighter/scripts/shBrushJScript.js?v=@TOKEN.SITE.VERSION@"></script>
		<script type="text/javascript" src="${ctxPath}/widgets/dp_syntaxhighlighter/scripts/shBrushJava.js?v=@TOKEN.SITE.VERSION@"></script>
		<script type="text/javascript" src="${ctxPath}/widgets/dp_syntaxhighlighter/scripts/shBrushJavaFX.js?v=@TOKEN.SITE.VERSION@"></script>
		<script type="text/javascript" src="${ctxPath}/widgets/dp_syntaxhighlighter/scripts/shBrushGroovy.js?v=@TOKEN.SITE.VERSION@"></script>
		<script type="text/javascript" src="${ctxPath}/widgets/dp_syntaxhighlighter/scripts/shBrushErlang.js?v=@TOKEN.SITE.VERSION@"></script>
		<script type="text/javascript" src="${ctxPath}/widgets/dp_syntaxhighlighter/scripts/shBrushDiff.js?v=@TOKEN.SITE.VERSION@"></script>
		<script type="text/javascript" src="${ctxPath}/widgets/dp_syntaxhighlighter/scripts/shBrushDelphi.js?v=@TOKEN.SITE.VERSION@"></script>
		<script type="text/javascript" src="${ctxPath}/widgets/dp_syntaxhighlighter/scripts/shBrushCss.js?v=@TOKEN.SITE.VERSION@"></script>
		<script type="text/javascript" src="${ctxPath}/widgets/dp_syntaxhighlighter/scripts/shBrushCommon.js?v=@TOKEN.SITE.VERSION@"></script>
		
		<%-- This comment extract from page-view.js for renderCallback()
			 only FF (test in 3.5) automatically eval script when inserts script in render panel.
			To avoid duplicated execute same script in FF, it won't run this piece code. 
			If runs duplciated, some script won't work. For example, datepicker, it won't display.
			jQuery.support.scriptEval only return false for IE 
			2012/04/15 - after FF 4, above is not true anymore, so here also check version.
	
		 --%>
		<script type="text/javascript" src="${ctxPath}/static/scripts/page-view.js?v=@TOKEN.SITE.VERSION@"></script>

