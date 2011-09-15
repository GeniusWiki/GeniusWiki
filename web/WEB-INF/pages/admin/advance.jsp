<%@ include file="/common/taglibs.jsp"%>
<%@ page import="com.edgenius.wiki.gwt.client.server.utils.SharedConstants" %>  
<%@ page import="com.edgenius.wiki.Shell" %>  
  
<html>
	<head>
        <%@ include file="/common/meta.jsp"%>
		<title><edgenius:title/></title>
		<link rel="stylesheet" type="text/css" media="all" href="${skinPath}/styles/admin.css?v=@TOKEN.SITE.VERSION@" />
		<script type="text/javascript" src="${ctxPath}/static/scripts/prototype.js"></script>
		<script type="text/javascript" src="${ctxPath}/static/scripts/admin.js?v=@TOKEN.SITE.VERSION@"></script>

		<script type="text/javascript">
			function rebuildIndex(){
				Element.show("indicator1");
				resetURLPanelHeight();
				var url = '<c:url value="/instance/advance!rebuildIndex.do"/>';
				//ajax call detail
				new Ajax.Updater(
						{success:"message1"},
						url,
						{method: 'get', 
						  onComplete: function(){
							Element.hide("indicator1");
							resetURLPanelHeight();
						 }
						}
				);
			}
			function redeployShell(){
				Element.show("indicator3");
				resetURLPanelHeight();
				var url = '<c:url value="/instance/advance!redeployShell.do"/>';
				//ajax call detail
				new Ajax.Updater(
						{success:"message3"},
						url,
						{method: 'get', 
						  onComplete: function(){
							Element.hide("indicator3");
							resetURLPanelHeight();
						 }
						}
				);
			}
			function resetCache(){
				Element.show("indicator2");
				resetURLPanelHeight();
				var url = '<c:url value="/instance/advance!resetCache.do"/>';
				//ajax call detail
				new Ajax.Updater(
						{success:"message2"},
						url,
						{method: 'get', 
						  onComplete: function(){
							Element.hide("indicator2");
							resetURLPanelHeight();
						 }
						}
				);
			}
			function printCache(){
				Element.show("indicator4");
				resetURLPanelHeight();
				var url = '<c:url value="/instance/advance!printCache.do"/>';
				//ajax call detail
				new Ajax.Updater(
						{success:"message4"},
						url,
						{method: 'get', 
						  onComplete: function(){
							Element.hide("indicator4");
							resetURLPanelHeight();
						 }
						}
				);
			}
			function showLicenseEditor(show){
				if(show){
					Element.show("licenseEdit");
					Element.hide("licenseView");
				}else{
					Element.hide("licenseEdit");
					Element.show("licenseView");
				}
				resetURLPanelHeight();
							
			}
		</script>
	</head>

	<body onload="resetURLPanelHeight()">
		<input type="hidden" id="urlPanelChildUid" value="advance"/>
		

		<table  align="center" class="general" height="220px">
			<tr><td colspan="2">
				<%@ include file="/common/messages.jsp"%>
			</td></tr>
			<tr>
				<td colspan="2">
					<div id="licenseView">
							<h3><fmt:message key="license.information"/></h3>
							<strong><fmt:message key="license.owner"/>:</strong> ${licCompany} <br>
							<strong><fmt:message key="license.expired.date"/>:</strong> ${licExpired}<br>
							<strong><fmt:message key="maximum.users"/>:</strong> ${licLimit}<br>
							<strong><fmt:message key="status"/>:</strong> ${licstatus}<br><br>
							<c:if test="${not isHosting}">
								<input type="button" onclick="showLicenseEditor(true)" value="<fmt:message key='update'/>">
							</c:if>
					</div>
					<c:if test="${not isHosting}">
					<div id="licenseEdit" style="display:none">
						<form method="get" action="<c:url value="/instance/advance!updateLicense.do"/>">
							<textarea rows="8" cols="60" id="license" name="license"></textarea><br>
							<input type="submit" value="<fmt:message key='save'/>"> &nbsp;&nbsp;
							<input type="button" onclick="showLicenseEditor(false)" value="<fmt:message key='cancel'/>">
						</form>
					</div>
					</c:if>
				</td>
			</tr>
			<tr>
				<td colspan="2"><div class="separator"></div></td>
			</tr>
			
			<tr>
				<td width="180px">
					<% if(Shell.enabled){ %>
						<a href="#" onclick="redeployShell()"><fmt:message key="redeploy.shell"/></a>
					<% }else{ %>
						<span style="color:#333;"><fmt:message key="redeploy.shell"/></span>
					<% } %>
				</td>
				<td class="desc"><fmt:message key="redeploy.shell.desc"/></td>
			</tr>
			<tr>
				<td colspan="2"><img id="indicator3" src="${ctxPath}/static/images/indicator.gif" style="display:none"><span id="message3"></span></td>
			</tr>
			<tr>
				<td colspan="2"><div class="separator"></div></td>
			</tr>
			
			<tr>
				<td width="180px">
					<a href="#" onclick="rebuildIndex()"><fmt:message key="rebuid.index"/></a>
				</td>
				<td class="desc"><fmt:message key="rebuid.index.desc"/></td>
			</tr>
			<tr>
				<td colspan="2"><img id="indicator1" src="${ctxPath}/static/images/indicator.gif" style="display:none"><span id="message1"></span></td>
			</tr>
			<tr>
				<td colspan="2"><div class="separator"></div></td>
			</tr>


			<tr>
				<td>
					<a href="#" onclick="resetCache()"><fmt:message key="clean.cache"/></a>
				</td>
				<td class="desc"><fmt:message key="clean.cache.desc"/>
					
					<%-- Policy, Space Reading,Page Reading, Page Tree,User, Tag,LoginTimes--%>
				</td>
			</tr>
			<tr>
				<td colspan="2"><img id="indicator2" src="${ctxPath}/static/images/indicator.gif" style="display:none"><span id="message2"></span></td>
			</tr>
			<tr>
				<td colspan="2"><div class="separator"></div></td>
			</tr>

			<tr>
				<td>
					<a href="#" onclick="printCache()"><fmt:message key="submit.log"/></a>
					
				</td>
				<td>
					<p class="desc"> <fmt:message key="submit.log.desc"/></p>
					<p><fmt:message key='log.dir'><fmt:param>${logDir}</fmt:param></fmt:message> </p>
				</td>
			</tr>
			<tr>
				<td colspan="2"><img id="indicator4" src="${ctxPath}/static/images/indicator.gif" style="display:none"><span id="message4"></span></td>
			</tr>
			<tr>
				<td colspan="2"><div class="separator"></div></td>
			</tr>

		</table>
	</body>
</html>
