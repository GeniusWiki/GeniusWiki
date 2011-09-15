<%@ include file="/common/taglibs.jsp"%>
<html>
	<head>
        <%@ include file="/common/meta.jsp"%>
		<title><edgenius:title/> - <fmt:message key='invalid.license'/></title>
		<link rel="stylesheet" type="text/css" media="all" href="${ctxPath}/static/styles/setup.css?v=@TOKEN.SITE.VERSION@" />
	</head>

	<body>
		<br>
		<div class="main">
		<table width="750" align="center" cellpadding="5" cellspacing="0" border="0" class="setup-main">
			<tr><td>
				<div class="title" style="color:red"><fmt:message key='invalid.license'/></div>
			</td></tr>
			<tr><td>
				<div style="color:red"><fmt:message key='invalid.license.reason'/></div> 
			</td></tr>
			<tr><td>
				<ol>
					<li><fmt:message key='invalid.license.reason.1'/></li>
					<li><fmt:message key='invalid.license.reason.2'/></li>
				</ol>
			</td></tr>
	
			<tr><td>
				<br>
				<fmt:message key='invalid.license.action'/> <br>
			</td></tr>
			<tr><td>
				<br>
				<%@ include file="/common/messages.jsp"%>
			</td></tr>
			
			<tr><td align="center">
				<form action="<c:url value="/install"/>" method="post">
					<input type="hidden" name="step" value="ulicense">
					<input type="hidden" name="licenseType" value="commercial">
					<textarea name="license" id="license" style="width:450px;height:180px" >${license}</textarea><br>
					<input type="submit" value="<fmt:message key='submit'/> ">
				</form>
			</td></tr>
			
			
		</table>
		</div>
	</body>
</html>