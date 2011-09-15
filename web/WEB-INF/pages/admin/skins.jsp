<%@ include file="/common/taglibs.jsp"%>

<html>
<head>
    <%@ include file="/common/meta.jsp"%>
	<title><edgenius:title/></title>
	<link rel="stylesheet" type="text/css" media="all" href="${skinPath}/styles/admin.css?v=@TOKEN.SITE.VERSION@"/>
	<script type="text/javascript" src="${ctxPath}/widgets/jquery/jquery.min.js?v=@TOKEN.SITE.VERSION@"></script>
	<script type="text/javascript" src="${ctxPath}/static/scripts/admin.js?v=@TOKEN.SITE.VERSION@"></script>
	
	<script type="text/javascript">
		function download( f){
			$("#dframe").attr("src","<c:url value='/instance/theme!downloadSkin.do?name='/>"+f);
		}
		function changeStyle(oldS, newS){
			window.top.changeStyle(oldS, newS);
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
	<input type="hidden" id="urlPanelChildUid" value="skins"/>
	<iframe id="dframe" width="0" height="0" scrolling="no" frameborder="0"   src=""></iframe>

	<table border="0" cellspacing="5" width="98%" align="center">
	<tr><td>
		<%@ include file="/common/messages.jsp"%>
	</td></tr>
	<tr><td>
		<div class="uploading">
			<form action="<c:url value='/instance/theme!uploadSkin.do'/>" id="uploadForm" method="post" enctype="multipart/form-data" >
				<label class="form-label"><fmt:message key="upload.skin"/>: </label>
				<input id="skin" name="skin" type="file">
				<input type="submit" value="<fmt:message key='upload'/>">
			</form>
		</div>
		<div class="separator"></div>
		<div class="listing">
			<c:forEach items="${skins}" var="skin" varStatus ="status">
				<c:set var="floatPos" value="float:right;"/>
				<c:if test="${status.index%2==0}">
					<c:set var="floatPos" value="float:left;"/>
				</c:if> 
				<div class="item" style="${floatPos}">
					<table width="100%">
					<tr><td valign="top" width="300px">
						<div><img src="${ctxPath}/skins/${skin.name}/${skin.previewImageName}" style="max-width:300px;max-height:300px"></div>
					</td>
					<td width="5px"></td>
					<td valign="top">
						<div><h3>${skin.title}</h3></div>
						<span class="func">Info</span>
						<div class="separator"></div>
						<div style="margin:7px"><label class="form-label"><fmt:message key="version"/>: </label>${skin.version}</div>
						<div style="margin:7px"><label class="form-label"><fmt:message key="description"/>: </label>${skin.description}</div>
						<div style="margin:7px"><label class="form-label"><fmt:message key="author"/>: </label>${skin.author}</div>
						<div style="margin:7px"><label class="form-label"><fmt:message key="status"/>: </label>
							<c:choose>
								<c:when test="${skin.status == 2}">
									<fmt:message key="applied"/>
									<c:if test="${not empty oldSkinName}">
										<script type="text/javascript">
											changeStyle("${oldSkinName}","${skin.name}");
										</script>
									</c:if>	
								</c:when>
								<c:when test="${skin.status == 1}">
									<fmt:message key="deployed"/>
								</c:when>
							</c:choose>
							
						</div>
						
						<div class="func" style="margin-top:20px"><fmt:message key="actions"/></div>
						<div class="separator"></div>
						<div class="func-area">
							<c:if test="${skin.status != 2}">
								<a href="<c:url value='/instance/theme!applySkin.do?name=${skin.name}'/>"><fmt:message key="apply"/></a>
							</c:if>
							<c:if test="${skin.removable}">
								<a href="javascript:;" onclick="if(confirm('<fmt:message key="delete.skin.confirm"/>')){location.href='<c:url value='/instance/theme!deleteSkin.do?name=${skin.name}'/>';}"><fmt:message key="delete"/></a>
							</c:if>
							<a href="javascript:;" onclick="download('${skin.name}')"><fmt:message key="download"/></a>
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