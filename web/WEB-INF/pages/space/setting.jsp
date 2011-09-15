<%@ include file="/common/taglibs.jsp"%>
<%@ page import="com.edgenius.wiki.SpaceSetting" %>
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
			function doneFunc(){
				//validate
				var valid = true;
				var err = "";
				$("#errorDiv").html("");

				if(!$("#commentNTMax").is(":disabled")){
					if(!$("#commentNTMax").val().match(/^\d+$/)){
						err = "maximum sending must be integer";
						valid = false;
					}else if(parseInt($("#commentNTMax").val()) < 1){
						err = "maximum sending must be greater than 0";
						valid = false;
					}
				}

				
				if(valid){
					$("#generalForm").submit();
				}else{
					$("#errorDiv").html('<div class="error" style="width:95%">'+err+'</div>');
					resetURLPanelHeight();
				}
			}
			function commentNtTypeChange(idx){
				if(idx != 1){
					$("#commentNTMax").attr("disabled","disabled");  
				}else{
					$("#commentNTMax").removeAttr("disabled");  
				}
			}
			function initFunc(){
				$("#errorDiv").html("");
				$("#commentNTMax").removeAttr("disabled"); 
				<c:if test="${commentNTtype != C_NOTFI_P}">
					$("#commentNTMax").attr("disabled","disabled");  
				</c:if>
				<c:if test="${not empty message}">
					$("#errorDiv").html('<div class="info" style="width:95%">${message}</div>');
				</c:if>
				
				resetURLPanelHeight();
			}

			function styleChanged(nv){
				if(nv != 1 && nv != 0){
					$("#id-showPortrait").attr("disabled","disabled");  
				}else{
					$("#id-showPortrait").removeAttr("disabled");
				}
			}
		</script>	
	</head>

	<body onload="initFunc()">
	<input type="hidden" id="urlPanelChildUid" value="general"/>
	<%-- IE must enclose all elements in a table then resetURLPanelHeight(); can works properly --%> 
	<table width="100%">
		<tr><td>
			<div class="message-panel" id="errorDiv"></div>
		</td></tr>
		<tr><td>
			<form action="<c:url value='/space/admin!updateSetting.do'/>" id="generalForm" method="post">
				<input type="hidden" name="spaceUname" value="${spaceUname}">
				<table  align="center" class="general" width="100%">
					<tr>
						<td class="form-label"><fmt:message key="widget.display.model"/></td>
						<td width="340">
							<c:set var='ws0' value=""/>
							<c:set var='ws1' value=""/>
							<c:set var='ws2' value=""/>
							<c:choose>
								<c:when test="${widgetStyle == 0}"><c:set var='ws0' value="checked='true'"/></c:when>
								<c:when test="${widgetStyle == 1}"><c:set var='ws1' value="checked='true'"/></c:when>
								<c:when test="${widgetStyle == 2}"><c:set var='ws2' value="checked='true'"/></c:when>
								<c:otherwise><c:set var='ws0' value="checked=checked"/></c:otherwise>
							</c:choose>
							<table>
								<tr>
									<td><fmt:message key="sort.by.modify.date"/></td>
									<td><input type="radio" name="widgetStyle" value="0"  ${ws0} onclick="styleChanged(0)"></td>
								</tr>						
								<tr>
									<td><fmt:message key="sort.by.create.date"/></td>
									<td><input type="radio" name="widgetStyle" value="1" ${ws1} onclick="styleChanged(1)"></td>
								</tr>
								<tr>
									<td><fmt:message key="display.homepage"/></td>
									<td><input type="radio" name="widgetStyle" value="2" ${ws2} onclick="styleChanged(2)"></td>
								</tr>
							</table>
						</td>
						<td class="desc"><fmt:message key="widget.display.desc"/>  
						
						</td>
					</tr>
					<tr>
						<td class="form-label"><fmt:message key="widget.show.portrait"/></td>
						<td width="340">
							<c:set var='showW' value=""/>
							<c:if test="${showPortrait}">
							 	<c:set var='showW'  value='checked="checked"'/>
							</c:if>
							<input type="checkbox" name="showPortrait" id="id-showPortrait" value="true"  ${showW} >	
						</td>
						<td class="desc"><fmt:message key="widget.show.portrait.desc"/>  
						</td>
					</tr>
					<tr>
						<td colspan="3"><div class="separator"></div></td>
					</tr>
					<tr>
						<td class="form-label"><fmt:message key="comment.to"/> </td>
						<td>
							<table>
								<tr>
									<td><fmt:message key="to.creators"/></td>
									<td><input type="checkbox" name="commentNTo" value="${C_NOTFI_TO_A}" <c:if test="${commentNTo[0]>0}">checked=checked</c:if>></td>
								</tr>						
								<tr>
									<td><fmt:message key="to.contributors"/></td>
									<td><input type="checkbox" name="commentNTo" value="${C_NOTFI_TO_C}" <c:if test="${commentNTo[1]>0}">checked=checked</c:if>></td>
								</tr>						
								<tr>
									<td><fmt:message key="to.admins"/></td>
									<td><input type="checkbox" name="commentNTo" value="${C_NOTFI_TO_S}" <c:if test="${commentNTo[2]>0}">checked=checked</c:if>></td>
								</tr>
							</table>
						</td>
						<td class="desc"><fmt:message key="comment.to.desc"/></td>
					</tr>
					<tr>
						<td colspan="3"><div class="separator"></div></td>
					</tr>
					<tr>
						<td class="form-label"><fmt:message key="comment.feq"/></td>
						<td>
							<c:set var='ct0' value=""/>
							<c:set var='ct1' value=""/>
							<c:choose>
								<c:when test="${commentNTtype == C_NOTFI_P}"><c:set var='ct0' value="checked=checked"/></c:when>
								<c:when test="${commentNTtype == C_NOTFI_D}"><c:set var='ct1' value="checked=checked"/></c:when>
								<c:otherwise><c:set var='ct0' value="checked=checked"/></c:otherwise>
							</c:choose>
							<table>
								<tr>
									<td><fmt:message key="per.post"/></td>
									<td><input type="radio" name="commentNTtype" value="${C_NOTFI_P}"  ${ct0} onclick="commentNtTypeChange(1)">  
										<fmt:message key="max.per.day"/>
										<input type="text" name="commentMaxPerDay" id="commentNTMax" value="${commentMaxPerDay}" style="width:30px">
									</td>
								</tr>						
								<tr>
									<td><fmt:message key="daily.summary"/></td>
									<td><input type="radio" name="commentNTtype" value="${C_NOTFI_D}" ${ct1} onclick="commentNtTypeChange(2)"></td>
								</tr>						
							</table>
						</td>
						<td class="desc"><fmt:message key="comment.feq.desc"/></td>
					</tr>
					<tr>
						<td colspan="3"><div class="separator"></div></td>
					</tr>
					
					<tr>
						<td colspan="2" align="center" class="small-buttons">
							<input class="small" type="button" value="<fmt:message key="update"/>" id="submitBtn" onclick="doneFunc()">
						</td>
					</tr>
			</form>
		</td></tr>
		</table>
	</body>
</html>
