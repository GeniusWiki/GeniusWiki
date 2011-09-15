<%@ include file="/common/taglibs.jsp"%>
<%@ page import="com.edgenius.wiki.gwt.client.server.utils.SharedConstants" %>  
  
<html>
	<head>
        <%@ include file="/common/meta.jsp"%>
		<title><edgenius:title/></title>
		<link rel="stylesheet" type="text/css" media="all" href="${ctxPath}/static/styles/setup.css?v=@TOKEN.SITE.VERSION@" />
		<script type="text/javascript" src="${ctxPath}/static/scripts/prototype.js"></script>
		<script type="text/javascript">
		</script>
	</head>

	<body>
		<br>
		<div class="main">
		<table width="750" align="center" cellpadding="5" cellspacing="0" border="0" class="setup-main">
			<tr><td>
				<div class="title">
				<fmt:message key='setup.lower.appversion.title'><fmt:param><%=SharedConstants.APP_NAME%></fmt:param></fmt:message> 
				</div>
			</td></tr>
			<tr><td>
				<div>
				 <fmt:message key='setup.lower.appversion.desc'>
				 	<fmt:param>${appVer}</fmt:param>
				 	<fmt:param>${installVer}</fmt:param>
				</fmt:message>
				<br><br>
				<br>
				</div>
			</td></tr>
		</table>
		</div>
	</body>
</html>