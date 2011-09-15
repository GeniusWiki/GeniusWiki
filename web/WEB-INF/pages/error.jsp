<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ include file="/common/taglibs.jsp"%>
<html>
<head>
        <%@ include file="/common/meta.jsp"%>
        <link rel="stylesheet" type="text/css" media="all" href="<c:url value="/static/styles/simple-page.css?v=@TOKEN.SITE.VERSION@"/>" /> 
		<title><fmt:message key="app.name" /> - <fmt:message key="page.title.errorpage"/></title>
</head>

<body>
<div id="page">
<div id="container">
	<img src="${ctxPath}/static/images/error_large.png" style="float:left;border:0px none;">
	<c:choose>
        <c:when test="${param.e eq 'f'}">
             Error on feed.
        </c:when>
        <c:when test="${param.e eq 'p'}">
             Error on print.
        </c:when>
        <c:when test="${param.e eq 's'}">
             Error on space.
        </c:when>
        <c:when test="${param.e eq 'i'}">
             Invitation does not exist or this invitation already has been accepted.</a>. 
        </c:when>
        <c:otherwise>
			 Unexpected Error
        </c:otherwise>
    </c:choose>
</div>
</div>

</body>
</html>