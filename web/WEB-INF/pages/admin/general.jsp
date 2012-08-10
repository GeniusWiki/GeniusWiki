<%@ include file="/common/taglibs.jsp"%>

<html>
	<head>
	    <%@ include file="/common/meta.jsp"%>
		<title><edgenius:title/></title>
		<link rel="stylesheet" type="text/css" media="all" href="${skinPath}/styles/admin.css?v=@TOKEN.SITE.VERSION@" />
		<script type="text/javascript" src="${ctxPath}/static/scripts/prototype.js"></script>
		<script type="text/javascript" src="${ctxPath}/static/scripts/admin.js?v=@TOKEN.SITE.VERSION@"></script>

		<script type="text/javascript">
			//call from languages.jsp
			function changeLanguage(lang){
				var oldLang = $("sysLang").value;
				$("sysLang").value=lang;
				$("lang_"+oldLang.toLowerCase()).setStyle({
					  border:'3px solid white'
				});
				$("lang_"+lang.toLowerCase()).setStyle({
					  border:'3px solid #699ddd'
				});
			}
			function doneFunc(){
				//validate
				var valid = true;
				var err = "";
				$("errorDiv").innerHTML = "";
				
				if(!$("spaceQuota").value.match(/^\d+$/)){
					err = "<fmt:message key='space.quota.integer'/>";
					valid = false;
				}
				if(!$("removeDelay").value.match(/^\d+$/)){
					if(!valid) 
						err += "<br>";
					err = err + "<fmt:message key='space.remove.delay.integer'/>";
					valid = false;
				}
				if(!$("syncFeq").value.match(/^\d+$/)){
					if(!valid) 
						err += "<br>";
					err = err + "<fmt:message key='offline.sync.integer'/>";
					valid = false;
				}
				if($("mailTypeServer").checked && $("smtpHost").value.empty()){
					if(!valid) 
						err += "<br>";
					err = err + "<fmt:message key='smtp.host.required'/>";
					valid = false;
				}
				if($("mailTypeServer").checked && !$("smtpPort").value.match(/^\d+$/)){
					if(!valid) 
						err += "<br>";
					err = err + "<fmt:message key='smtp.port.integer'/>";
					valid = false;
				}
				if($("mailTypeJNDI").checked && $("smtpJNDI").value.empty()){
					if(!valid) 
						err += "<br>";
					err = err + "<fmt:message key='smtp.jndi.required'/>";
					valid = false;
				}
				
				if(valid){
					$("generalForm").submit();
				}else{
					$("errorDiv").innerHTML='<div class="error" style="width:95%">'+err+'</div>';
					resetURLPanelHeight();
				}

			}
			function serviceSel(){
				if($("rest").checked){
					$("shell").disabled="";
				}else{
					$("shell").disabled="disabled";
				}
			}
			function initFunc(){
				$("errorDiv").innerHTML="";
				
				Element.show("editBtn");
				Element.hide("cancelBtn");
				Element.hide("submitBtn");

				//show all text field but hide input
				if(${smtpAuth}){
					$("smtpAuth").checked=true;
				}else{
					$("smtpAuth").checked=false;
					Element.hide("smtpAuthDetailDiv");
				}

				if(${allowSE}){
					$("allowSE").checked=true;
				}else{
					$("allowSE").checked=false;
				}
				
				if(${allowPublic}){
					$("allowPublic").checked=true;
				}else{
					$("allowPublic").checked=false;
				}
				
				if(${signupNeedApproval}){
					$("signupNeedApproval").checked=true;
				}else{
					$("signupNeedApproval").checked=false;
				}
				
				if(${detectLocale}){
					$("detectLocale").checked=true;
				}else{
					$("detectLocale").checked=false;
				}
				if(${versionCheck}){
					$("versionCheck").checked=true;
				}else{
					$("versionCheck").checked=false;
				}
				
				if(${ccSysAdmins}){
					$("ccSysAdmins").checked=true;
				}else{
					$("ccSysAdmins").checked=false;
				}
				
				if(${rest}){
					$("rest").checked=true;
				}else{
					$("rest").checked=false;
				}
				if(${soap}){
					$("soap").checked=true;
				}else{
					$("soap").checked=false;
				}
				if(${shell}){
					$("shell").checked=true;
				}else{
					$("shell").checked=false;
				}
				
				serviceSel();
				
				$("lang_${sysLang}").setStyle({
					border:'3px solid #699ddd'
				});
				
				$$(".wd").each(Element.hide);
				$$(".rd").each(Element.show);

				logoDone();
				resetURLPanelHeight();
			}
			
			function editFunc(){
				Element.hide("editBtn");
				Element.show("cancelBtn");
				Element.show("submitBtn");

				$$(".rd").each(Element.hide);
				$$(".wd").each(Element.show);

				resetURLPanelHeight();
			}
			function selectSmtpAuth(){
				if($("smtpAuth").checked){
					Element.show("smtpAuthDetailDiv");
				}else{
					Element.hide("smtpAuthDetailDiv");
				}
				resetURLPanelHeight();
			}
			function changeLogo(){
				Element.show("uploadframe");
				Element.hide("logoID");
				Element.hide("logoFuncID");
			}
			function logoDone(){
				Element.hide("uploadframe");
				Element.show("logoID");
				Element.show("logoFuncID");
				$("logoID").src="${ctxPath}/download?instance=logo&refresh="+new Date().getTime();
			}
			function switchMailResourceType(src){
				if(src==1){
					//display JNDI
					Element.hide("mailResourceServer");
					Element.show("mailResourceJNDI");
				}else if(src==0){
					//display MailServer
					Element.hide("mailResourceJNDI");
					Element.show("mailResourceServer");
				}else{
					Element.hide("mailResourceServer");
					Element.hide("mailResourceJNDI");
				}
				resetURLPanelHeight();
			}
		</script>	
	</head>			
	

	<body onload="initFunc()">
	
	<input type="hidden" id="urlPanelChildUid" value="general"/>
	<%-- IE must enclose all elements in a table then resetURLPanelHeight(); can works properly --%> 
	<table width="100%">
		<tr><td>
			<div class="message-panel" id="errorDiv"></div>
			<%@ include file="/common/messages.jsp"%>
		</td></tr>
		<tr><td>
			<form action="<c:url value='/instance/general!update.do'/>" id="generalForm" method="post">
				<table  align="center" class="general">
					<%-- it is not good ask user modify his host info here, as it may break system display for incorrect setting...
					<tr>
						<td class="form-label">Web host</td>
						<td>
							<input name="host" type="text" class="wd" value="${host}" style="display:none">
							<span id="hostDiv" class="rd">${host}</span>
						</td>
						<td>Your website address, such as http://www.geniuswiki.com or http://localhost:8080/geniuswiki/</td>
					</tr>
					 --%>
					 <tr>
						<td class="form-label"><fmt:message key="logo"/></td>
						<td>
							<img src="${ctxPath}/download?instance=logo" title="system log" id="logoID">
							<a href="javascript:;" onclick="changeLogo()" id="logoFuncID"><fmt:message key="change"/></a>
							<iframe src="<c:url value='instance/uploadlogo.do'/>" id="uploadframe" style="width:400;height:60;border:0;display:none;" frameborder="0" scrolling="no"></iframe>
						</td>
						<td class="desc"><fmt:message key="logo.desc"/></td>
					</tr>
					<tr>
						<td colspan="3"><div class="separator"></div></td>
					</tr>
					 <tr>
						<td class="form-label"><fmt:message key="system.title"/></td>
						<td>
							<input type="text" id="systemTitle" name="systemTitle"  class="wd long" value="${systemTitle}" maxlength="120" style="display:none">   
							<span class="rd">${systemTitle}</span>				
						</td>
						<td class="desc"><fmt:message key="system.title.desc"/></td>
					</tr>
					<tr>
						<td colspan="3"><div class="separator"></div></td>
					</tr>
					 <tr>
						<td class="form-label"><fmt:message key="base.url"/></td>
						<td>
							<input type="text" id="baseURL" name="baseURL"  class="wd long" value="${baseURL}" maxlength="120" style="display:none">   
							<span class="rd">${baseURL}</span>				
						</td>
						<td class="desc"><fmt:message key="base.url.desc"/></td>
					</tr>
					<tr>
						<td colspan="3"><div class="separator"></div></td>
					</tr>
					<tr>
						<td class="form-label"><fmt:message key="email.smtp.host"/></td>
						<td width="400px">
							<c:choose>
								<c:when test="${mailType ==-1}"> <%-- not set --%>
									<c:set var="mailResourceNoneChecked" value='checked="checked"'/>
									<c:set var="mailResourceJNDIChecked" value=''/>
									<c:set var="mailResourceServerChecked" value=''/>
									<c:set var="mailResourceJNDI" value='style="display:none"'/>
									<c:set var="mailResourceServer" value='style="display:none"'/>
								</c:when>
								<c:when test="${mailType ==1}"> <%-- show JDNI --%>
									<c:set var="mailResourceNoneChecked" value=''/>
									<c:set var="mailResourceJNDIChecked" value='checked="checked"'/>
									<c:set var="mailResourceServerChecked" value=''/>
									<c:set var="mailResourceJNDI" value=''/>
									<c:set var="mailResourceServer" value='style="display:none"'/>
								</c:when>
								<c:otherwise> <%-- show SMTP Host --%>
									<c:set var="mailResourceNoneChecked" value=''/>
									<c:set var="mailResourceJNDIChecked" value=''/>
									<c:set var="mailResourceServerChecked" value='checked="checked"'/>
									<c:set var="mailResourceJNDI" value='style="display:none"'/>
									<c:set var="mailResourceServer" value=''/>
								</c:otherwise>
							</c:choose>
							<table cellspacing="5" border="0" class="wd">
							<tr><td>
								<input type="radio"  name="mailType" value="-1" onclick="switchMailResourceType(-1)" ${mailResourceNoneChecked}>
								<span>None</span>
							</td><td>
								<input type="radio" id="mailTypeServer" name="mailType" value="0" onclick="switchMailResourceType(0)" ${mailResourceServerChecked}>
								<span>Mail Server</span>
							</td><td>
								<input type="radio" id="mailTypeJNDI" class="wd" name="mailType" value="1" onclick="switchMailResourceType(1)" ${mailResourceJNDIChecked}>
								<span>JNDI</span>
							</td></tr>
							</table>
							<table cellspacing="5" border="0" ${mailResourceServer} id="mailResourceServer">
								<tr>
									<td><span class="form-label"><fmt:message key="host"/></span></td>
									<td>
										<input id="smtpHost" name="smtpHost" type="text" class="wd" value="${smtpHost}" style="display:none">
										<span id="smtpHostDiv" class="rd">${smtpHost}</span> 
									</td>
								</tr>
								<tr>
									<td><span class="form-label"><fmt:message key="port"/></span></td>
									<td>
										<input id="smtpPort" name="smtpPort" type="text" class="wd" value="${smtpPort}" style="display:none">
										<span id="smtpPortDiv" class="rd">${smtpPort}</span>
									</td>
								</tr>
								<tr class="wd">
									<td colspan="2">
										<span class="form-label" class="wd" ><fmt:message key="authentication.required"/></span>
										<input type="checkbox" id="smtpAuth" name="smtpAuth"  value="true" class="wd" onclick="selectSmtpAuth()" style="display:none"/>
									</td>
								</tr>
								<tr>
									<td colspan="2">
									<div id="smtpAuthDetailDiv">
										<table cellspacing="0" border="0">
											<tr>
												<td><span class="form-label"><fmt:message key="user.name"/></span></td>
												<td><input id="mailUsername" name="mailUsername" class="wd" type="text" value="${mailUsername}" style="display:none">
													<span id="mailUsernameDiv" class="rd">${mailUsername}</span>
												</td>
											<tr>
												<td><span class="form-label"><fmt:message key="password"/></span></td>
												<td><input id="mailPassword" name="mailPassword" type="text" class="wd" value="${mailPassword}" style="display:none">
												<span id="mailPasswordDiv" class="rd">${mailPassword}</span>
												</td>
											</tr>
											<tr>
												<td><span class="form-label"><fmt:message key="connection.type"/></span></td>
												<td>
													<c:choose>
														<c:when test="${smtpConnectType==1}"><c:set var="connectTypeName" value="TLS"/></c:when>
														<c:when test="${smtpConnectType==2}"><c:set var="connectTypeName" value="SSL"/></c:when>
														<c:otherwise><c:set var="connectTypeName"><fmt:message key="none.upper"/></c:set></c:otherwise>
													</c:choose>
													<input type="radio" id=smtpConnectType name="smtpConnectType"  value="0" class="wd" <c:if test="${smtpConnectType==0}">checked="checked"</c:if>/>
														<span class="wd"><fmt:message key="none.upper"/></span>
													<input type="radio" id=smtpConnectType name="smtpConnectType"  value="1" class="wd" <c:if test="${smtpConnectType==1}">checked="checked"</c:if>/>
														<span class="wd">TLS</span>
													<input type="radio" id=smtpConnectType name="smtpConnectType"  value="2" class="wd" <c:if test="${smtpConnectType==2}">checked="checked"</c:if>/>
														<span class="wd">SSL</span>
													<span id="smtpConnectTypeDiv" class="rd">
														${connectTypeName}
													</span>
													
												</td>
											</tr>
										</table>
									</div> 
									</td>
								</tr>
							</table>
							<table cellspacing="5" border="0"  ${mailResourceJNDI} id="mailResourceJNDI">
								<tr>
									<td><span class="form-label">JNDI</span></td>
									<td>
										<input id="smtpJNDI" name="smtpJNDI" type="text" class="wd" value="${smtpJNDI}" style="display:none">
										<span id="smtpJNDIDiv" class="rd">${smtpJNDI}</span>
									</td>
								</tr>
							</table>
						</td>
						<td class="desc"><fmt:message key="email.desc"/></td>
					</tr>
					<tr>
						<td colspan="3"><div class="separator"></div></td>
					</tr>
					<tr>
						<td class="form-label"><fmt:message key="out.email"/></td>
						<td>
							<input type="text" id="notifyEmail" name="notifyEmail"  class="wd long" value="${notifyEmail}" style="display:none">   
							<span class="rd">${notifyEmail}</span>
		
						</td>
						<td class="desc"><fmt:message key="out.email.desc"/></td>
					</tr>
					<tr>
						<td colspan="3"><div class="separator"></div></td>
					</tr>			
					<tr>
						<td class="form-label"><fmt:message key="in.email"/></td>
						<td>
							<table cellspacing="5" border="0">
								<tr><td>
									<input type="text" id="receiverEmail" name="receiverEmail"  class="wd long" value="${receiverEmail}" style="display:none">   
									<span class="rd">${receiverEmail}</span>
								</td></tr>
								<tr><td>
									<span class="form-label"><fmt:message key="cc.sys.admin"/></span> <input id="ccSysAdmins" name="ccSysAdmins" value="true" type="checkbox" class="wd" style="display:none"> 
									<span id="ccSysAdmins" class="rd">
										<c:choose><c:when test="${ccSysAdmins}"><fmt:message key="yes"/></c:when><c:otherwise><fmt:message key="no"/></c:otherwise></c:choose>
									</span>
								</td></tr>
							</table>
		
						</td>
						<td class="desc"><fmt:message key="in.email.desc"/></td>
					</tr>
					<tr>
						<td colspan="3"><div class="separator"></div></td>
					</tr>
					<tr>
						<td class="form-label"><fmt:message key="space.quota"/></td>
						<td>
							<input type="text" id="spaceQuota" name="spaceQuota"  class="wd" value="${spaceQuota}" style="display:none">   
							<span class="rd">${spaceQuota}</span> MB <br>
							<span class="wd" style="display:none"><input type="checkbox" name="allSpacesQuota" value="true" ><fmt:message key="update.exist.quota"/></span>
		
						</td>
						<td class="desc"><fmt:message key="quota.desc"/></td>
					</tr>
					<tr>
						<td colspan="3"><div class="separator"></div></td>
					</tr>
					<tr>
						<td class="form-label"><fmt:message key="space.remove.delay"/></td>
						<td>
							<input id="removeDelay" name="removeDelay" type="text" class="wd"  value="${removeDelay}" style="display:none"> 
							<span id="removeDelayDiv" class="rd">${removeDelay}</span> <fmt:message key="hours"/>
						</td>
						<td class="desc"><fmt:message key="space.remove.delay.desc"/></td>
					</tr>
					<tr>
						<td colspan="3"><div class="separator"></div></td>
					</tr>
					<tr>
						<td class="form-label"><fmt:message key="offline.sync.freq"/></td>
						<td>
							<input id="syncFeq" name="syncFeq" type="text" class="wd" value="${syncFeq}" style="display:none"> 
							<span id="syncFeqDiv" class="rd">${syncFeq}</span> <fmt:message key="minutes"/>
						</td>
						<td class="desc"><fmt:message key="offline.sync.desc"/></td>
					</tr>
					<tr>
						<td colspan="3"><div class="separator"></div></td>
					</tr>
					<tr>
						<td class="form-label"><fmt:message key="change.lang"/></td>
						<td>
							<div class="wd" style="display:none;"><%@ include file="../languages.jsp" %></div>
							<span id="detectLocaleDiv" class="rd">
								<c:choose>
								<c:when test="${sysLang=='zh_cn'}">
									<img src="${ctxPath}/static/images/flags/ch-t.gif" title="<fmt:message key='lang.zh.cn'/>" width="30px" height="20px">
								</c:when>
								<c:when test="${sysLang=='tr_tr'}">
									<img src="${ctxPath}/static/images/flags/tr-t.gif" title="<fmt:message key='lang.tr.tr'/>" width="30px" height="20px">
								</c:when>
								<c:otherwise>
									<img src="${ctxPath}/static/images/flags/as-t.gif" title="<fmt:message key='lang.en.au'/>" width="30px" height="20px">
								</c:otherwise>
								</c:choose>
								<input type="hidden" id="sysLang" name="sysLang" value="${sysLang}">
							</span>
						</td>
						<td class="desc"><fmt:message key="change.lang.desc"/></td>
					</tr>
					<tr>
						<td colspan="3"><div class="separator"></div></td>
					</tr>
					<tr>
						<td class="form-label"><fmt:message key="detect.locale"/></td>
						<td>
							<input id="detectLocale" name="detectLocale" type="checkbox" value="true"  class="wd" style="display:none"> 
							<span id="detectLocaleDiv" class="rd">
								<c:choose><c:when test="${detectLocale}"><fmt:message key="yes"/></c:when><c:otherwise><fmt:message key="no"/></c:otherwise></c:choose>
							</span>
						</td>
						<td class="desc"><fmt:message key="detect.locale.desc"/></td>
					</tr>
					<tr>
						<td colspan="3"><div class="separator"></div></td>
					</tr>
					<tr>
						<td class="form-label"><fmt:message key="allow.public"/></td>
						<td>
							<input id="allowPublic" name="allowPublic" value="true" type="checkbox" class="wd" style="display:none">
							<span id="allowPublicDiv" class="rd">
								<c:choose><c:when test="${allowPublic}"><fmt:message key="yes"/></c:when><c:otherwise><fmt:message key="no"/></c:otherwise></c:choose>
							</span> 
						</td>
						<td class="desc"><fmt:message key="allow.public.desc"/></td>
					</tr>
					<tr>
						<td class="form-label"><fmt:message key="signup.approval"/></td>
						<td>
							<input id="signupNeedApproval" name="signupNeedApproval" value="true" type="checkbox" class="wd" style="display:none">
							<span id="signupNeedApprovalDiv" class="rd">
								<c:choose><c:when test="${signupNeedApproval}"><fmt:message key="yes"/></c:when><c:otherwise><fmt:message key="no"/></c:otherwise></c:choose>
							</span> 
						</td>
						<td class="desc"><fmt:message key="signup.approval.desc"/></td>
					</tr>
					<tr>
						<td colspan="3"><div class="separator"></div></td>
					</tr>
					<tr>
						<td class="form-label"><fmt:message key="version.check"/></td>
						<td>
							<input id="versionCheck" name="versionCheck" type="checkbox" value="true" class="wd" style="display:none">
							<span id="versionCheckDiv" class="rd">
								<c:choose><c:when test="${versionCheck}"><fmt:message key="yes"/></c:when><c:otherwise><fmt:message key="no"/></c:otherwise></c:choose>
							</span> 
						</td>
						<td class="desc"><fmt:message key="version.check.desc"/></td>
					</tr>
					<tr>
						<td colspan="3"><div class="separator"></div></td>
					</tr>					
					<tr>
						<td class="form-label"><fmt:message key="allow.se"/></td>
						<td>
							<input id="allowSE" name="allowSE" type="checkbox" value="true" class="wd" style="display:none">
							<span id="allowSEDiv" class="rd">
								<c:choose><c:when test="${allowSE}"><fmt:message key="yes"/></c:when><c:otherwise><fmt:message key="no"/></c:otherwise></c:choose>
							</span> 
						</td>
						<td class="desc"><fmt:message key="allow.se.desc"/></td>
					</tr>
					<tr>
						<td colspan="3"><div class="separator"></div></td>
					</tr>
					<tr>
						<td class="form-label"><fmt:message key="system.services"/></td>
						<td>
							<table cellspacing="5" border="0">
							<tr>
								<td><span class="form-label"><fmt:message key="soap.service"/></span></td>
								<td>
								<input id="soap" name="soap" type="checkbox" value="true" class="wd" style="display:none">
								<span id="soapDiv" class="rd">
									<c:choose><c:when test="${soap}"><fmt:message key="yes"/></c:when><c:otherwise><fmt:message key="no"/></c:otherwise></c:choose>
								</span>
								</td> 
							</tr>
							<tr>
								<td><span class="form-label"><fmt:message key="rest.service"/></span></td>
								<td>
								<input id="rest" name="rest" type="checkbox" value="true" class="wd" style="display:none" onclick="serviceSel()">
								<span id="restDiv" class="rd">
									<c:choose><c:when test="${rest}"><fmt:message key="yes"/></c:when><c:otherwise><fmt:message key="no"/></c:otherwise></c:choose>
								</span>
								</td> 
							</tr>
							<tr>
								<td><span class="form-label"><fmt:message key="shell.service"/></span></td>
								<td>
								<input id="shell" name="shell" type="checkbox" value="true" class="wd" style="display:none">
								<span id="shellDiv" class="rd">
									<c:choose><c:when test="${shell}"><fmt:message key="yes"/></c:when><c:otherwise><fmt:message key="no"/></c:otherwise></c:choose>
								</span> 
								</td>
							</tr>
							</table>
						</td>
						<td class="desc"><fmt:message key="service.desc"/></td>
					</tr>
					<tr>
						<td colspan="3"><div class="separator"></div></td>
					</tr>
					<tr>
						<td colspan="2" align="center" class="small-buttons">
							<input class="small" type="button" value="<fmt:message key='edit'/>" id="editBtn" onclick="editFunc()">
							<input class="small" type="button" value="<fmt:message key='submit'/>" id="submitBtn" onclick="doneFunc()">
							<input class="small" type="button" value="<fmt:message key='cancel'/>" id="cancelBtn" onclick="location.href='${ctxPath}/instance/general.do'">
						</td>
					</tr>
			</form>
			</tr></td>			
		</table>
<%-- 
System default timezone
Search index optimise frequency  
--%>
	</body>
</html>
