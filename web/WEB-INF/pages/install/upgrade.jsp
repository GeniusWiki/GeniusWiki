<%@ include file="/common/taglibs.jsp"%>
  
<html>
	<head>
        <%@ include file="/common/meta.jsp"%>
		<title><edgenius:title/></title>
		<link rel="stylesheet" type="text/css" media="all" href="${ctxPath}/static/styles/setup.css?v=@TOKEN.SITE.VERSION@" />
		<script type="text/javascript" src="${ctxPath}/static/scripts/prototype.js"></script>
		<script type="text/javascript">
			function focusDone(){
				if(confirm("<fmt:message key='confirm.skip.to.done'/>")){
					location.href="<c:url value='/install?step=done'/>";
				}
			}
			function upgrade(){
				//hide other function links.
				document.getElementById("upgradeBtn").innerHTML = "<fmt:message key='wait.patient'/>";
				location.href="<c:url value='/install?step=upgrade'/>";
			}
		</script>
	</head>

	<body>
	<br>
	<div class="main">
		<table width="750" align="center" cellpadding="5" cellspacing="0" border="0" class="setup-main">
			<c:choose>
				<c:when test="${not empty error}">
					<tr><td>
						<div class="title"><fmt:message key='upgrade.error.1'/></div>
						<div class="desc"><fmt:message key='upgrade.error.1.desc'/></div>
						<div class="message-panel" id="errorDiv"><div class="error" style="width:95%">${error}</div></div>
					</td></tr>
					<tr><td>
						<a href="<c:url value='/install?step=mq'/>"><fmt:message key='upgrade.to.next'/></a> &nbsp;&nbsp;&nbsp;
						<a href="javascript:;" onclick="focusDone()"><fmt:message key='upgrade.to.done'/></a>			  
					</td></tr>
				</c:when>
				<c:otherwise>
					<tr><td>
						<div class="title"><fmt:message key='upgrade'/>?</div>
						<div class="desc">
						<fmt:message key='upgrade.desc'>
							<fmt:param value="${newVer}"/>
							<fmt:param value="${existVer}"/>
							<fmt:param value="${root}"/>
						</fmt:message>
						</div>
					</td></tr>
					<tr><td>
						<div id="upgradeBtn" style="display:inline"><a href="#" onclick="upgrade()"><fmt:message key='upgrade.go'/></a>
						&nbsp;&nbsp;&nbsp; <a href="<c:url value='/install?step=mq'/>"><fmt:message key='upgrade.from.start'/></a>			  
						</div> 
					</td></tr>
				</c:otherwise>
			</c:choose>
		</table>

		</div>
	</body>
</html>