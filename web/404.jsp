<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ include file="/common/taglibs.jsp"%>
<html>
	<head>
        <%@ include file="/common/meta.jsp"%>

		<title><fmt:message key="page.title.404"/></title>
	</head>

	<body>
	 <div id="page">
	 	 <div id="header">
	 	 	<%--  This part useless for 404  page
 			<c:choose>
				<c:when test="${pageContext.request.remoteUser != null}">
					<strong><authz:authentication property="principal.fullname" /></strong>
					&nbsp;
					<a href="<c:url value="/j_spring_security_logout"/>"><fmt:message key="label.logout" /></a>
				</c:when>
				<c:otherwise>
					<a href="<c:url value="/signin"/>"><fmt:message key="label.login" /></a>
				</c:otherwise>
			</c:choose>
	 	 	 --%>
			</div>
			<div id="content">
				<h1><fmt:message key="page.title.404"/></h1>
				<p>
				    <fmt:message key="page.message.404">
				        <fmt:param><c:url value="/"/></fmt:param>
				    </fmt:message>
				</p>
			</div>
			<jsp:include page="/common/footer.jsp"/>
	</div>
	</body>
</html>