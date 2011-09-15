<%@ include file="/common/taglibs.jsp"%>
<%@ page import="com.edgenius.wiki.gwt.client.server.utils.SharedConstants" %>  
<%@ page import="com.edgenius.core.model.User" %>  
  
<html>
	<head>
        <%@ include file="/common/meta.jsp"%>
		<title><edgenius:title/></title>
		<link rel="stylesheet" type="text/css" media="all" href="${skinPath}/styles/admin.css?v=@TOKEN.SITE.VERSION@" />
		<script type="text/javascript" src="${ctxPath}/static/scripts/prototype.js"></script>
		<script type="text/javascript" src="${ctxPath}/static/scripts/admin.js?v=@TOKEN.SITE.VERSION@"></script>

		<script type="text/javascript">
			function filter(){
				var filter = $("filter").value;
				
				var url = '<c:url value="/instance/users!filter.do?sortBy=${sortBy}&sortByDesc=${sortByDesc}"/>';
				var parms = 'filter='+escape(filter);
				//ajax call detail
				new Ajax.Updater(
						{success:"userlist"},
						url,
						{method: 'get', 
						  parameters: parms, 
						  onComplete: function(){
							  resetURLPanelHeight();
						 }
						}
				);
			}
			
			function showDetail(uid){
				var divId = "detail-"+uid;
				var loadingId = "loading-"+uid;
				var linkId = "link-"+uid;
				var rowId = "row-"+uid;
				
				if(Element.visible(rowId)){
					Element.hide(rowId);
					$(linkId).innerHTML="detail";
					$(divId).innerHTML="<img id=\""+loadingId+"\" src=\"${ctxPath}/static/images/large-loading.gif\">";
					resetURLPanelHeight();
				}else{
					$(linkId).innerHTML="hide";
					Element.show(rowId);
					
					var url = '<c:url value="/instance/users!detail.do"/>';
					var parms = 'uid='+uid;
					//ajax call detail
					new Ajax.Updater(
							{success:divId},
							url,
							{method: 'get', 
							  parameters: parms, 
							  onComplete: function(){
								  resetURLPanelHeight();
							 }
							}
						);
				}
			}
			function enableUser(uid, enable){
				var divId = "func-area-"+uid;
				var url = '<c:url value="/instance/users!enable.do"/>';
				var parms = 'uid='+uid+"&enable="+enable;
				new Ajax.Updater(
						{success:divId},
						url,
						{method: 'get', 
						  parameters: parms, 
						  onComplete: function(){
						  	
						 }
						}
				);
			}
			function goProfile(uid){
				if(confirm('<fmt:message key="confirm.leave.to.profile"/>')){
					var uh = "sname-"+uid;
					var fullname = $(uh).value;
					var url = "<c:url value='/'/><%=SharedConstants.URL_PAGE%>#/<%=SharedConstants.TOKEN_CPAGE%>/<%=SharedConstants.CPAGE_USER_PROFILE%>/" + fullname;
					window.top.location.href=url;
				}
			}
			function createUser(){
				//invoke GWT dialog
				window.top.gwtCreateUserDialog();
			}
			<%-- call by GWT!!! And this method is also called in gwt admin.jsp. More detail see /static/scripts/admin.js
				The process is this page call createUser() to open GWT dialog, note, this page is inside iframe, so use window.top.
				After GWT complete created user, it call back the admin.js in admin.jsp, then that JS find iframe and locates userCreated() method in this page. 
			--%>
			function userCreated(fullname){
				var url= "<c:url value='/instance/users!created.do'/>";
				location.href=url;
			}
		</script>
	</head>

	<body onload="resetURLPanelHeight()">
		
		<input type="hidden" id="urlPanelChildUid" value="users"/>
		<%-- IE must enclose all elements in a table then resetURLPanelHeight(); can works properly --%> 
		<table width="100%">
		<tr><td>
			<div id="message" class="message-panel">
				<c:if test="${not empty message}">
					<div class='info' style='width:95%'>${message}</div>
				</c:if>
			</div>
		</td></tr>
		<tr><td>
			<div style="margin-left:30px;float:left">
				<fmt:message key="filter"/>: <input type="text" name="filter" id="filter" onkeyup="filter()" value="${filter}" class="search-input">
			</div>			
			<div class="buttons" style="margin-right:30px;float:right">
				<button onclick="createUser()" class="button positive">
					<img src="${skinPath}/images/admin/create.png" style="padding-bottom:4px"><fmt:message key="create.user"/>
				</button>
			</div>
		</td></tr>		
		<tr><td>
			<div id="userlist">
				<jsp:include page="userlist.jsp"/>
			</div>
		</td></tr>
		</table>		
	
	</body>
</html>
