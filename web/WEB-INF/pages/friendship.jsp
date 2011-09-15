<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ include file="/common/taglibs.jsp"%>
<html>
	<head>
        <%@ include file="/common/meta.jsp"%>
        <link rel="stylesheet" type="text/css" media="all" href="<c:url value="/static/styles/simple-page.css?v=@TOKEN.SITE.VERSION@"/>" /> 
		<title><fmt:message key="app.name" /> - <fmt:message key="friends"/></title>
    </head>

	<body>
	<div id="page">
		<div id="container">
			<h2>Space members visible</h2>
			<c:if test="${not empty msg}">
				${msg}
			</c:if>
			<c:if test="${empty msg}">
				<div class="content">
				Space ${sender} administrator adds your space ${receiver} into its permission list. Your space members will have 
				properly permission by that space owner assignment. However, you have chance to make your members list
				invisible to that space owner. Your permission won't have any impact in that space whatever you choose "Invisible" or 
				"Visible". 
				</div>
				<div class="buttonsbar">
					<c:set var="actUrl"><c:url value='/invite!friendship.do'>
						<c:param name="sender">${sender}</c:param>
						<c:param name="receiver">${receiver}</c:param>
					</c:url></c:set>
					<a href="${actUrl}&action=accept" ><img src="<c:url value="/static/images/tick.png"/>"/> Visible</a>
					<a href="${actUrl}&action=reject" ><img src="<c:url value="/static/images/cross.png"/>"/> Invisible</a> 
				</div>
			</c:if>
		</div>
	</div>
	  <%@ include file="/common/simple-footer.jsp"%>
	</body>
</html>