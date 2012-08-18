<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ include file="/common/taglibs.jsp"%>
<html>
<head>
       <%@ include file="/common/meta.jsp"%>
       <link rel="stylesheet" type="text/css" media="all" href="<c:url value="/static/styles/simple-page.css?v=@TOKEN.SITE.VERSION@"/>" /> 
	<title><fmt:message key="app.name" /> - <fmt:message key="invitation"/></title>
</head>

<body>
<div id="page">
<div id="container">
	<h2><fmt:message key="app.name" /> <fmt:message key="invitation"/> </h2>
	<div class="content">
		<c:if test="${not empty msg}">
			${msg}
		</c:if>
		<c:if test="${not empty err}">
			<img src="${ctxPath}/static/images/error_large.png" style="float:left;border:0px none;">${err}<br>
		</c:if>
	</div>
	
	<c:choose>
		<c:when test="${btnStyle=='anonymous'}">
			<div class="buttonsbar">
				<c:set var="actUrl"><c:url value='/invite!accept.do'>
					<c:param name="s">${s}</c:param>
					<c:param name="i">${i}</c:param>
				</c:url></c:set>
				<p><a href="${actUrl}"><img src="<c:url value="/static/images/tick.png"/>"/> Login <c:if test="${allowsignup}">or sign-up to accept</c:if></a></p>
			</div>
		</c:when>
		<c:when test="${btnStyle=='correctUser'}">
			<div class="buttonsbar">
				<c:set var="actUrl"><c:url value='/invite!accept.do'>
					<c:param name="s">${s}</c:param>
					<c:param name="i">${i}</c:param>
				</c:url></c:set>
				<p><a href="${actUrl}"><img src="<c:url value="/static/images/tick.png"/>"/> Accept</a></p>
			</div>
		</c:when>
		<c:when test="${btnStyle=='wrongUser'}">
			<div class="buttonsbar">
					<p><a href="<c:url value="/j_spring_security_logout"/>"><img src="<c:url value="/static/images/tick.png"/>"/> Logoff then sign-up with correct email</a></p> 
			</div>
	
		</c:when>
	</c:choose>
	
	
</div>
</div>

</body>
</html>