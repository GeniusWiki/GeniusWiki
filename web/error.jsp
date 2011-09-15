<%@ page language="java" isErrorPage="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
	"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<head>
<title><fmt:message key="page.title.errorpage"/></title>
<link rel="stylesheet" type="text/css" media="all" href="<c:url value="/static/styles/simple-page.css"/>" /> 
<script type="text/javascript" src="<c:url value="/widgets/jquery/jquery.min.js"/>"></script>

<script type="text/javascript">
	function showErr(){
    	if($("#detail").is(":visible")){
        	//hide
        	$("#funMsg").val("Show error detail");
        	$("#detail").hide();
    	}else{
        	//show
        	$("#funMsg").val("Hide error detail");
        	$("#detail").show();
    	}
    	
	}
</script>
<style type="text/css">
#footer {
	width: 98%;
	height: 10px;
	clear: both;
}

#footer p {
	padding: 10px 0px 0px 10px;
	text-align: center;
	font-size: .9em;
	color: #999;
}

#footer a {
	color: #85aacd;
	text-decoration:none;
	border-bottom: 1px solid #85aacd;
}
</style>
</head>

<body>
<div id="page">
    <div id="container">
  	<h2><fmt:message key="page.title.errorpage"/></h2>

 	<img src="${pageContext.request.contextPath}/static/images/error_large.png" style="float:left;border:0px none;"/>
 	
 	Some unexpected errors occur, please make sure you are requesting a valid URL and try again. If problems persist, please send issue report to
 	<a href="http://www.edgenius.com/support" target="_blank">GeniusWiki feedback</a>. <br/>
 	<a href="javascript:;" onclick="showErr()"><span id="funMsg">Show error detail</span></a> <br/>
 	<div id="detail" style="display:none">   
		 <% if (exception != null) { %>
		    <pre><% exception.printStackTrace(new java.io.PrintWriter(out)); %></pre>
		 <% } else if ((Exception)request.getAttribute("javax.servlet.error.exception") != null) { %>
		    <pre><% ((Exception)request.getAttribute("javax.servlet.error.exception"))
		                           .printStackTrace(new java.io.PrintWriter(out)); %></pre>
		 <% } else { %>
		 	No more details.
		 <% } %>
	</div>
	</div>		 
</div>
  <%@ include file="/common/simple-footer.jsp"%>
</body>
</html>
