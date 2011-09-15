<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ include file="/common/taglibs.jsp"%>
<html>
<head>
	<!-- HTTP 1.1 -->
	<meta http-equiv="Cache-Control" content="no-store"/>
	<!-- HTTP 1.0 -->
	<meta http-equiv="Pragma" content="no-cache"/>
	<!-- Prevents caching at the Proxy Server -->
	<meta http-equiv="Expires" content="0"/>
	
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/> 
	<meta name="author" content="Dapeng Ni"/>
	
	<link rel="icon" href="${skinPath}/images/app.ico"/>
	<link rel="stylesheet" type="text/css" media="all" href="${skinPath}/styles/print.css?v=@TOKEN.SITE.VERSION@" />
	<link rel="stylesheet" type="text/css" media="all" href="${skinPath}/styles/render.css?v=@TOKEN.SITE.VERSION@" />
	<title><fmt:message key="app.name" /></title>
</head>

<body>
	<div class="print-page">
		${content}
	</div>
</body>
</html>

