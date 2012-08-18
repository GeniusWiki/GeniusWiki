<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ include file="/common/taglibs.jsp"%>
<html>
<head>
       <%@ include file="/common/meta.jsp"%>
       <link rel="stylesheet" type="text/css" media="all" href="<c:url value="/static/styles/simple-page.css?v=@TOKEN.SITE.VERSION@"/>" /> 
	<title><fmt:message key="app.name" /> - <fmt:message key="account.wait.approval"/></title>
</head>

<body>
<div id="page">
<div id="container">
	<h2><fmt:message key="app.name" /> <fmt:message key="account.wait.approval"/> </h2>
	<div class="content">
		<fmt:message key="account.wait.approval.message"/>
	</div>
</div>
</div>

</body>
</html>