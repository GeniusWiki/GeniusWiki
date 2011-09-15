<%@ include file="/common/taglibs.jsp"%>
<%@ page import="com.edgenius.wiki.gwt.client.server.utils.SharedConstants" %>  
<%@ page import="com.edgenius.core.model.Role" %>    
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
				
				var url = '<c:url value="/instance/roles!filter.do?sortBy=${sortBy}&sortByDesc=${sortByDesc}"/>';
				var parms = 'filter='+escape(filter);
				//ajax call detail
				new Ajax.Updater(
						{success:"rolelist"},
						url,
						{method: 'get', 
						  parameters: parms, 
						  onComplete: function(){
							  resetURLPanelHeight();
						 }
						}
					);
			}		
			function createRole(){
				//invoke GWT dialog
				window.top.gwtCreateRoleDialog();
			}
			
			//call by GWT!! And this method is also called in gwt-admin.jsp
			function roleCreated(fullname){
				var url= "<c:url value='/instance/roles!created.do'/>";
				location.href=url;
			}

			function addUserToRole(roleUid){
				//invoke GWT dialog
				window.top.gwtAddUserToRoleDialog(roleUid);
			}
			
			//call by GWT!! And this method is also called in gwt-admin.jsp
			function userToRoleAdded(roleUid){
				//refresh current role detail page
				//hide first - then call showDetail() again which will refresh page
				Element.hide("row-"+roleUid)
				showDetail(roleUid);
			}

			function deleteUsersFromRole(roleUid, userUid){
				if(!confirm("<fmt:message key='confirm.remove.user.from.role'/>"))
					return;
				
				var divId = "detail-"+roleUid;

				var url = '<c:url value="/instance/roles!deleteUsersFromRole.do"/>';
				var params = new Hash();
				params.set('uid', roleUid);
				params.set('userUid',userUid);
				//ajax call detail
				new Ajax.Updater(
						{success:divId},
						url,
						{method: 'get', 
						  parameters: params, 
						  onComplete: function(){
							resetURLPanelHeight();
						 }
						}
					);
			}
			function showDetail(uid){
				
				var divId = "detail-"+uid;
				var linkId = "link-"+uid;
				var loadingId = "loading-"+uid;
				var rowId = "row-"+uid;
				
				if(Element.visible(rowId)){
					Element.hide(rowId);
					$(linkId).innerHTML="detail";
					$(divId).innerHTML="<img id=\""+loadingId+"\" src=\"${ctxPath}/static/images/large-loading.gif\">";
					resetURLPanelHeight();
				}else{
					$(linkId).innerHTML="hide";
					Element.show(rowId);
					
					var url = '<c:url value="/instance/roles!detail.do"/>';
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
		</script>
	</head>

	<body onload="resetURLPanelHeight()">
		<input type="hidden" id="urlPanelChildUid" value="roles"/>
		<%-- IE must enclose all elements in a table then resetURLPanelHeight(); can works properly --%> 
		<table width="100%">
		<tr><td>
			<%@ include file="/common/messages.jsp"%>
		</td></tr>
		<tr><td>
			<div style="margin-left:30px;float:left">
				<fmt:message key="filter"/>: <input type="text" name="filter" id="filter" onkeyup="filter()" value="${filter}" class="search-input">
			</div>			
			<div class="buttons" style="margin-right:30px;float:right">
				<button  href="#" onclick="createRole()" class="button positive">
					<img src="${skinPath}/images/admin/create.png" style="padding-bottom:4px"><fmt:message key="create.group"/>
				</button>
			</div>		
		</td></tr>
		<tr><td>
			<div id="rolelist">
				<jsp:include page="rolelist.jsp"/>
			</div>
		</td></tr>
		</table>
		
	</body>
</html>
