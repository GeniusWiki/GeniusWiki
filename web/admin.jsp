<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ include file="/common/taglibs.jsp"%>
<html>
	<head>
        <%@ include file="/common/meta.jsp"%>
        <meta name="gwt:property" content="locale=${PreferredLocale}">
        
		<title><edgenius:title/> <fmt:message key="system.admin"/></title>
 		<script language='javascript' src='gwtadmin/gwtadmin.nocache.js?v=@TOKEN.SITE.VERSION@'></script>
		<%-- admin of default dash board may need this editor widget --%>
    	<%@ include file="/common/view-scripts.jsp"%>
    	<%@ include file="/common/edit-scripts.jsp"%>
	
 		<script type="text/javascript" src="${ctxPath}/static/scripts/admin.js"></script>
	</head>

	<body>
	 <div id="page">
    	    <jsp:include page="/common/header.jsp"/>
            <div id="globalMessage"></div>
   	   		<div id="leftmenu"></div>
		    <div id="content"></div>
			<div id="rightmenu"></div>
   			<jsp:include page="/common/footer.jsp"/>
	</div>
	<div id="leftsidebarbtn" class="left-sidebar-btn" style="display:none"></div>
	<div id="rightsidebarbtn" class="right-sidebar-btn" style="display:none"></div>		
	<iframe src="javascript:''" id="__gwt_historyFrame" tabIndex='-1' style="position:absolute;width:0;height:0;border:0"></iframe>
	<div id="offlineAttachmentDiv" style="display: none"></div>
	</body>
</html>
