<%@ include file="/common/taglibs.jsp"%>
<%@ page import="com.edgenius.wiki.gwt.client.server.utils.SharedConstants" %>  
  
<html>
	<head>
        <%@ include file="/common/meta.jsp"%>
		<title><edgenius:title/></title>
		<link rel="stylesheet" type="text/css" media="all" href="${ctxPath}/static/styles/setup.css?v=@TOKEN.SITE.VERSION@" />
		<script type="text/javascript" src="${ctxPath}/static/scripts/prototype.js"></script>
		<script type="text/javascript">
			function changeLanguage(lang){
				location.href = "<c:url value='/install?step=chglang&lang='/>"+lang;
			}
		</script>
	</head>

	<body>
		<br>
		<div class="main">
		<table width="750" align="center" cellpadding="5" cellspacing="0" border="0" class="setup-main">
			<tr><td>
				<div class="title">
				<fmt:message key='setup.welcome.title'><fmt:param><%=SharedConstants.APP_NAME%></fmt:param></fmt:message> 
				</div>
			</td></tr>
			<tr><td>
				<div class="desc"><fmt:message key='setup.desc'/></div>
			</td></tr>
			<tr><td>
				<ol>
					<li><fmt:message key='setup.step.1'/></li>
					<li><fmt:message key='setup.step.2'/></li>
					<li><fmt:message key='setup.step.3'/></li>
					<li><fmt:message key='setup.step.4'/></li>
					<li><fmt:message key='setup.step.5'/></li>
					<li><fmt:message key='setup.step.6'/></li>
				</ol>
			</td></tr>
	
			<tr><td>
				<br>
				<div><a href="<c:url value='/install?step=dataroot'/>"><fmt:message key='next.step'/></a></div>
				
				<div style="float:right;display:inline"> 
					<fmt:message key='change.lang'/>
					<%@ include file="../languages.jsp" %>
				 </div> 
				 
				<br>
				
			</td></tr>
		</table>
		</div>
	</body>
</html>