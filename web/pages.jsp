<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ include file="/common/taglibs.jsp"%>
<%@ page import="com.edgenius.wiki.Shell" %>
<html>
	<head>
        <%@ include file="/common/meta.jsp"%>
        <meta name="gwt:property" content="locale=${PreferredLocale}">
        
        <link rel="stylesheet" type="text/css" media="all" href="${skinPath}/styles/render.css" />
        
		<title><edgenius:title/></title>
		
 		<script src='gwtpage/gwtpage.nocache.js?v=@TOKEN.SITE.VERSION@'></script>
 		<%-- This is not real Javascript but a Servlet - see PageLayout servlet web.xml  --%>
 		<script src='${ctxPath}/layout.js'></script>
 		
    	<%@ include file="/common/view-scripts.jsp"%>
    	<%@ include file="/common/edit-scripts.jsp"%>
	</head>

	<body>

	<div id="page">
        <jsp:include page="/common/header.jsp"/>
        <div id="globalMessage"></div>
		<div id="leftmenu"></div>
	    <div id="content">
	    	<div id="loading"><div class="loading-indicator"><img src="${ctxPath}/static/images/loadingbar.gif"></div></div>
	    </div>
		<div id="rightmenu"></div>
        <jsp:include page="/common/footer.jsp"/>
	</div>	
	<div id="leftsidebarbtn" class="left-sidebar-btn" style="display:none"></div>
	<div id="rightsidebarbtn" class="right-sidebar-btn" style="display:none"></div>
	
	<iframe src="javascript:''" id="__gwt_historyFrame" tabIndex='-1' style="position:absolute;width:0;height:0;border:0"></iframe>
	<input type="hidden" id="systemTitle" name="systemTitle" value="<edgenius:title/>"/>
	<input type="hidden" id="shellUrl" name="shellUrl" value="<%=Shell.url%>"/>
	<div id="offlineAttachmentDiv" style="display: none"></div>
	<!--GOOGLE-ANALYSTIC-->
	</body>
</html>

