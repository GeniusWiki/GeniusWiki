<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ include file="/common/taglibs.jsp"%>
<html>
	<head>
        <%@ include file="/common/meta.jsp"%>

		<title><fmt:message key="page.title.403"/></title>
	</head>

	<body>
	 <div id="page">
		 	 <table id="header" width="100%" cellpadding="0" cellspacing="2" height="25px">
				<tr>
					<td><div id="navbar"></div></td>
					<td align="right">
						<table cellpadding="0" cellspacing="0">
							<tr>
							<td nowrap="nowrap">
						 	  	<c:choose>
									<c:when test="${pageContext.request.remoteUser != null}">
										<span style="color:#fff;"><authz:authentication property="principal.fullname" /></span>
										&nbsp;
										<a href="<c:url value="/j_spring_security_logout"/>"><fmt:message key="logout" /></a>
									</c:when>
									<c:otherwise>
										<a href="<c:url value="/signin"/>"><fmt:message key="login" /></a>
									</c:otherwise>
								</c:choose>
							
							</td></tr>
						</table>
					</td>
				</tr>
			</table>
			<div id="content">
				<h1><fmt:message key="page.title.403"/></h1>
				<p>
				    <fmt:message key="page.message.403">
				        <fmt:param><c:url value="/"/></fmt:param>
				    </fmt:message>
				</p>
			</div>
			<jsp:include page="/common/footer.jsp"/>
	</div>
	</body>
</html>