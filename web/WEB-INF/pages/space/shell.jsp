<%@ include file="/common/taglibs.jsp"%>
<%@ page import="com.edgenius.wiki.SpaceSetting" %>
<%@ page import="com.edgenius.wiki.Shell" %>
<c:set var="C_NOTFI_D"><%=SpaceSetting.COMMENT_NOTIFY_FEQ_DAILY%></c:set>
<c:set var="C_NOTFI_P"><%=SpaceSetting.COMMENT_NOTIFY_FEQ_EVERY_POST%></c:set>
<c:set var="C_NOTFI_TO_A"><%=SpaceSetting.COMMENT_NOTIFY_TO_AUTHOR%></c:set>
<c:set var="C_NOTFI_TO_C"><%=SpaceSetting.COMMENT_NOTIFY_TO_ALL_CONTRIBUTOR%></c:set>
<c:set var="C_NOTFI_TO_S"><%=SpaceSetting.COMMENT_NOTIFY_TO_SPACE_OWNEER%></c:set>
<html>
	<head>
	    <%@ include file="/common/meta.jsp"%>
		<title><edgenius:title/></title>
		
		<script type="text/javascript" src='${ctxPath}/widgets/jquery/jquery.min.js?v=@TOKEN.SITE.VERSION@'></script>		
		<link rel="stylesheet" type="text/css" media="all" href="${skinPath}/styles/admin.css?v=@TOKEN.SITE.VERSION@" />
		<script type="text/javascript" src="${ctxPath}/static/scripts/admin.js?v=@TOKEN.SITE.VERSION@"></script>

		<script type="text/javascript">
			function doneFunc(act){
				if(act == 'Disable' && !confirm('<fmt:message key="space.disable.shell.confirm"/>')){
					return;
				}
				if(act == 'Deploy' && !confirm('<fmt:message key="space.deploy.shell.confirm"/>')){
					return;
				}
				if(act == 'Enable' && !confirm('<fmt:message key="space.enable.shell.confirm"/>')){
					return;
				}
				if(act == 'Theme'){
					//open change theme frame and return;
					window.top.gwtOpenChangeThemeDialog("<%=Shell.getThemeBaseURL()%>","${theme.name}", "${spaceUname}");
					return;
				}
				
				var url = "<c:url value='/space/admin!shell'/>";
				url = url + act + '.do';
				
				var form = document.getElementById("shellForm");
				form.action = url;
				form.submit();
			}
			
			function themeChanged(){
				//refresh page
				location.href= "<c:url value='/space/admin!shell.do'><c:param name='spaceUname' value='${spaceUname}'/></c:url>";
			}

		</script>	
	</head>

	<body onload="resetURLPanelHeight()">
	<input type="hidden" id="urlPanelChildUid" value="shellAdmin"/>
	<%-- IE must enclose all elements in a table then resetURLPanelHeight(); can works properly --%> 
	<table width="100%">
		<tr><td>
			<%@ include file="/common/messages.jsp"%>
		</td></tr>
		<c:if test="${shellServiceEnabled}">
			<tr><td>
				<form id="shellForm" method="post">
					<input type="hidden" name="spaceUname" value="${spaceUname}">
					<c:choose>
						<c:when test="${theme.enabled}">
							<table  class="shell" width="100%">
								<tr>
									<td colspan="2">
										<h2><fmt:message key="header.linked.shell"/></h2>
										<div class="separator"></div>
									</td>
								</tr>
								<tr>
									<td width="220px">
										<a href="${theme.imageUrl}" target="_blank"><img src="${theme.imageSmallUrl}" title="${theme.name}"/></a>
									</td>
									<td>
										<div style="margin:5px;"><h2>${theme.name}</h2></div>
										<div style="margin:5px;"><a href="${theme.url}" target="_blank">${theme.url}</a></div>
										<div style="margin:15px 5px;">
											<input class="small" type="button" value="<fmt:message key="disable.shell"/>" id="submitBtn" onclick="doneFunc('Disable')">
											<input class="small" type="button" value="<fmt:message key="change.shell.theme"/>" id="submitBtn" onclick="doneFunc('Theme')">
											<input class="small" type="button" value="<fmt:message key="redeploy.shell"/>" id="submitBtn" onclick="doneFunc('Deploy')">
										</div>
									</td>
								</tr>
							</table>
						</c:when>
						<c:otherwise>
							<input class="small" type="button" value="<fmt:message key="enable.shell"/>" id="submitBtn" onclick="doneFunc('Enable')">
						</c:otherwise>
					</c:choose>
				</form>
			</td></tr>
			<tr><td>
				<div class="desc"><fmt:message key="shell.desc"/></div>
			</td></tr>
		</c:if>
		</table>
	</body>
</html>
