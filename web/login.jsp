<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ include file="/common/taglibs.jsp"%>
<html>
	<head>
        <%@ include file="/common/meta.jsp"%>
       	<script language='javascript' type="text/javascript" src='${ctxPath}/widgets/jquery/jquery.min.js'></script>
        <meta name="gwt:property" content="locale=${PreferredLocale}">
        
		<title><edgenius:title/> <fmt:message key="login"/></title>
 		<script language='javascript' src='gwtlogin/gwtlogin.nocache.js?v=@TOKEN.SITE.VERSION@'></script> 		
	</head>

	<body>
	 <div id="page">
        <jsp:include page="/common/header.jsp"/>
			<div id="content"></div>
        <jsp:include page="/common/footer.jsp"/>
	</div>
	
	<hidden id="redirURLForLogin"  name="redir" value="${redir}"></hidden>
	<hidden id="regisgerDiv"  name="regisgerDiv" value="${register}"></hidden>
	
	<iframe src="javascript:''" id="__gwt_historyFrame" tabIndex='-1' style="position:absolute;width:0;height:0;border:0"></iframe>
	<div id="offlineAttachmentDiv" style="display: none"></div>
	</body>
</html>
