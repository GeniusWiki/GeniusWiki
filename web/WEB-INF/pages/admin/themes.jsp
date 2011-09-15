<%@ include file="/common/taglibs.jsp"%>

<html>
<head>
    <%@ include file="/common/meta.jsp"%>
	<title><edgenius:title/></title>
	<link rel="stylesheet" type="text/css" media="all" href="${skinPath}/styles/admin.css?v=@TOKEN.SITE.VERSION@" />
	<script type="text/javascript" src="${ctxPath}/widgets/jquery/jquery.min.js?v=@TOKEN.SITE.VERSION@"></script>
	<script type="text/javascript" src="${ctxPath}/static/scripts/admin.js?v=@TOKEN.SITE.VERSION@"></script>
	<script type="text/javascript">
		function download( f){
			$("#dframe").attr("src","<c:url value='/instance/theme!downloadTheme.do?name='/>"+f);
		}
	</script>
	<style type="text/css">
		.item{
			width:48%;
			margin-bottom:15px;
		}
	</style>
</head>

<body onload="resetURLPanelHeight()">
<input type="hidden" id="urlPanelChildUid" value="themes"/>
<iframe id="dframe" width="0" height="0" scrolling="no" frameborder="0"   src=""></iframe>

	<table border="0" cellspacing="5" width="98%" align="center">
	<tr><td>
		<%@ include file="/common/messages.jsp"%>
	</td></tr>
	<tr><td>
		<div class="uploading">
			<form action="<c:url value='/instance/theme!uploadTheme.do'/>" id="uploadForm" method="post" enctype="multipart/form-data" >
				<label class="form-label"><fmt:message key="upload.theme"/>: </label>
				<input id="theme" name="theme" type="file">
				<input type="submit" value="<fmt:message key='upload'/>">
			</form>
		</div>
		<div class="separator"></div>
		<div class="listing">
			<c:forEach items="${themes}" var="theme" varStatus ="status">
				<c:set var="floatPos" value="float:right;"/>
				<c:if test="${status.index%2==0}">
					<c:set var="floatPos" value="float:left;"/>
				</c:if> 
				<div class="item" style="${floatPos}">
					<table width="100%">
					<tr><td valign="top" width="300px">
						<div><img src="${ctxPath}/themes/${theme.name}/${theme.previewImageName}" style="max-width:300px;max-height:300px"></div>
					</td>
					<td width="5px"></td>
					<td valign="top">
						<div><h3>${theme.title}</h3></div>
						<span class="func">Info</span>
						<div class="separator"></div>
						<div style="margin:7px"><label class="form-label"><fmt:message key="version"/>: </label>${theme.version}</div>
						<div style="margin:7px"><label class="form-label"><fmt:message key="description"/>: </label>${theme.description}</div>
						<div style="margin:7px"><label class="form-label"><fmt:message key="author"/>: </label>${theme.author}</div>
						<div style="margin:7px"><label class="form-label"><fmt:message key="status"/>: </label>
							<c:choose>
								<c:when test="${theme.status == 0}">
									<fmt:message key="enabled"/>
								</c:when>
								<c:otherwise>
									<fmt:message key="disabled"/>
								</c:otherwise>
							</c:choose>
							
						</div>
						
						<div class="func" style="margin-top:20px"><fmt:message key="actions"/></div>
						<div class="separator"></div>
						<div class="func-area">
							<a href="<c:url value='/instance/theme!enableTheme.do?name=${theme.name}&enableTheme=${theme.status}'/>">
								<c:choose>
									<c:when test="${theme.status == 0}">
										<fmt:message key="disable"/></a>
									</c:when>
									<c:otherwise>
										<fmt:message key="enable"/></a>
									</c:otherwise>
								</c:choose> 
							&nbsp;&nbsp;
							<c:if test="${theme.removable}">
								<a href="javascript:;" onclick="if(confirm('<fmt:message key="delete.theme.confirm"/>')){location.href='<c:url value='/instance/theme!deleteTheme.do?name=${theme.name}'/>';}"><fmt:message key="delete"/></a>&nbsp;&nbsp;
							</c:if>
							<a href="javascript:;" onclick="download('${theme.name}')"><fmt:message key="download"/></a>&nbsp;&nbsp;
						</div>
					</td></tr>
					</table>
				</div>
			</c:forEach>
		</div>
		</td></tr>
		</table>
</body>
</html>