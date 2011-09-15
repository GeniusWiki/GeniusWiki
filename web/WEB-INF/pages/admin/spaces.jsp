<%@ include file="/common/taglibs.jsp"%>
<%@ page import="com.edgenius.wiki.gwt.client.server.utils.SharedConstants" %>  
<%@ page import="com.edgenius.wiki.model.Space" %>  
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
			
			var url = '<c:url value="/instance/spaces!filter.do?sortBy=${sortBy}&sortByDesc=${sortByDesc}"/>';
			var parms = 'filter='+escape(filter);
			//ajax call detail
			new Ajax.Updater(
					{success:"spacelist"},
					url,
					{method: 'get', 
					  parameters: parms, 
					  onComplete: function(){
						  resetURLPanelHeight();
					 }
					}
				);
			}
			<%--The reason use spaceUid rather than spaceUname, the latter may contain special character, such ' --%>
			
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
					
					var url = '<c:url value="/instance/spaces!detail.do"/>';
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
			function removeSpace(uid, remove){
				var divId = "func-area-"+uid;
				var url = '<c:url value="/instance/spaces!restore.do"/>';
				if(remove)
					url = '<c:url value="/instance/spaces!remove.do"/>';
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
			function gotoSpaceHome(uid){
				if(confirm("<fmt:message key='sysadmin.to.home'/>")){
					var uh = "sname-"+uid;
					var suname = $(uh).value;
					var url = "<c:url value='/'/><%=SharedConstants.URL_PAGE%>#/"+suname;
					window.top.location.href = url;
				}
			}
			function gotoSpaceAdmin(uid){
				if(confirm("<fmt:message key='sysadmin.to.spaceadmin'/>")){
					var uh = "sname-"+uid;
					var suname = $(uh).value;
					var url = "<c:url value='/'/><%=SharedConstants.URL_PAGE%>#<%=SharedConstants.TOKEN_CPAGE%>/<%=SharedConstants.CPAGE_SPACEADMIN%>/"+suname;
					window.top.location.href = url;
				}
			}

			function createSpace(){
				//invoke GWT dialog
				window.top.gwtCreateSpaceDialog();
			}
			//call by GWT!!!
			function spaceCreated(unixName){
				var url= "<c:url value='/instance/spaces!created.do'/>";
				location.href=url;
			}
			function editQuota(uid, editing){
				if(editing){
					Element.hide("quotaView-"+uid);
					Element.show("quotaEdit-"+uid);
				}else{
					Element.hide("quotaEdit-"+uid);
					Element.show("quotaView-"+uid);
				}
			}
			function saveQuota(uid){
				var divId = "quotaView-"+uid;
				var quota = document.getElementById('quoteValue-'+uid).value;
				if(!quota.match(/^\d+$/)){
					alert("Number only");
					return;
				}
				var url = '<c:url value="/instance/spaces!changeQuota.do"/>';
				var params = new Hash();
				params.set('uid', uid);
				params.set('quota',quota);
				//ajax call detail
				new Ajax.Updater(
						{success:divId},
						url,
						{method: 'get', 
						  parameters: params, 
						  onComplete: function(){
							editQuota(uid, false);
						 }
						}
					);
			}
		</script>
	</head>

	<body onload="resetURLPanelHeight()">
		<input type="hidden" id="urlPanelChildUid" value="spaces"/>
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
				<button onclick="createSpace()" class="button positive">
					<img src="${skinPath}/images/admin/create.png" style="padding-bottom:4px"><fmt:message key="create.space"/>
				</button>
			</div>
		</td></tr>		
		<tr><td>
			<div id="spacelist">
				<jsp:include page="spacelist.jsp"/>
			</div>
		</td></tr>
		</table>
	</body>
</html>
