<%@ include file="/common/taglibs.jsp"%>
  
<html>
	<head>
        <%@ include file="/common/meta.jsp"%>
		<title><edgenius:title/></title>
		<link rel="stylesheet" type="text/css" media="all" href="${ctxPath}/static/styles/setup.css?v=@TOKEN.SITE.VERSION@" />
		<script type="text/javascript" src="${ctxPath}/static/scripts/prototype.js"></script>
		<script type="text/javascript">
			function nextStep(){
				if(confirm("<fmt:message key='confirm.skip.step'/>")){
					location.href = '<c:url value="/install?step=admin"/>';
				}
			}
			function editURL(){
				var ed = $("urlEdited").value;
				
				if( ed == "false"){
					var url = $("urlDIV").innerHTML;
					$("urlDIV").innerHTML = "<input type='text' name='urlbox' id='urlbox' style='width:500px' value='"+trim(url)+"'>";
					$("edDiv").innerHTML = " <a href='javascript:;' onclick='cancelURLEditing()'>cancel</a></div>";
					$("urlEdited").value = "true";
					$("host"). disabled="disabled";
					$("dbname"). disabled="disabled";
				}
			}
			function cancelURLEditing(){
				var ed = $("urlEdited").value;
				
				if( ed == "true"){
					var url = $("urlDIV").innerHTML;
					$("urlDIV").innerHTML = getURL();
					$("edDiv").innerHTML = "<a href='javascript:;' onclick='editURL()'>edit</a></div>";
					$("urlEdited").value = "false";
					$("host"). disabled="";
					$("dbname"). disabled="";
				}
			}
			
			function bsb(){
				var er = ""
				if($("connectType").value == "jdbc"){				
					var ed = $("urlEdited").value;
					if(isBlank("host") && ed == "false"){
						if(er != "") er += "<br>";
						er += "<fmt:message key='host.not.blank'/>";
					} 
					if(isBlank("dbname")  && ed == "false"){
						if(er != "") er += "<br>";
						er += "<fmt:message key='db.not.blank'/>";
					} 
	
					if(isBlank("username")){
						if(er != "") er += "<br>";
						er += "<fmt:message key='username.not.blank'/>"
					}
					if(isBlank("password")){
						if(er != "") er += "<br>";
						er += "<fmt:message key='password.not.blank'/>"
					}
					if($("password").value != $("password1").value){
						if(er != "") er += "<br>";
						er += "<fmt:message key='password.same'/>"
					}
					if(ed == "true"){
						$("url").value = $("urlbox").value;
					}
				}else{
					if(isBlank("jndi")){
						if(er != "") er += "<br>";
						er += "<fmt:message key='datasource.not.blank'/>"
					}
				}
				if(!er.blank()){
					$("innerErrorDiv").innerHTML = "<div class=\"error\" style=\"width:95%\">"+er+"</div>";
				}else{
					 Form.Element.disable("submit-btn");
				}
				return er.blank();
			}
			function confirmReset(){
				if(confirm("<fmt:message key='confirm.delete.tables'/>")){
					$("confirmed").value = "true";
					bsb();
					$("fm").submit();
				}
			}

			var map = {};
			<c:forEach var="item" items="${userUrlP}">
			map['${item.key}'] = "${item.value}";
			</c:forEach>
			
			function getURL(driverType){
				var dt = ""
				if(Element.visible("dth")){
					driverType = $$('input:checked[type="radio"][name="driverType"]').pluck('value');
				}
				if(driverType != null && driverType != ""){
					dt = "." + driverType;
				}
				
				var pattern = map[$("dbType").value + dt];
				var h = $("host").value;
				var d = $("dbname").value;
				var u = pattern.replace("@HOST@",h);
				return u.replace("@DBNAME@",d);
			}
			
			//user/pass/host etc change will affect to URL
			function change(){
				var ed = $("urlEdited").value;
				if(ed == "false"){
					$("urlDIV").innerHTML = getURL(); 
				}
			}
			function isBlank(id){
				var v = $(id).value; 
				if( v== null)
					return true;
				
				return v.blank();
				
			}
			function dbChange(){
				cancelURLEditing();
				$("urlDIV").innerHTML = getURL();
				if($("dbType").value == "sqlserver"){
					showDriverType(true);
				}else{
					showDriverType(false);
				}
			}
			function changeDriverType(){
				cancelURLEditing();
				$("urlDIV").innerHTML = getURL();
			}
			function connTypeChange(t){
				if(t == "datasource"){
					Element.hide("jdbcDiv");
					Element.show("dsDiv");
				}else{
					Element.show("jdbcDiv");
					Element.hide("dsDiv");
				}
				
			}
			
			function downloadScript(){
				window.open("<c:url value='/install?script=tables&type='/>"+$("dbType").value);
			}

			function showDriverType(show){
				if(show){
					Element.show("dth");
					Element.show("dtc");
				}else{
					Element.hide("dth");
					Element.hide("dtc");
				}
			}
		</script>
	</head>

	<body>
		<br>
		<div class="main">
			<table align="center" cellpadding="5" cellspacing="0" border="0" class="setup-main">
				<tr><td colspan="2">
					<div class="title"><fmt:message key='step.5'/></div>
					<div class="desc"><fmt:message key='step.5.tip'/>
					</div>
				</td></tr>
				<tr><td colspan="2">
					<div class="message-panel" id="innerErrorDiv"></div>
					<%@ include file="/common/messages.jsp"%>
				</td></tr>
	
				<form action="<c:url value='/install'/>" method="post" id="fm" onsubmit="return bsb()">
					<input type="hidden" name="step" value="ctables">
					<input type="hidden" name="urlEdited" id="urlEdited"  value="false">
					<input type="hidden" name="userDBUrl" id="url">
					<input type="hidden" name="confirmed" id="confirmed" value="false">
		
					<tr>
						<td width="150"><fmt:message key='database'/></td>
						<td align="left">
							<select name="dbType" id="dbType" onchange="dbChange()">
								<option value="mysql" <c:if test="${dbType=='mysql' || empty dbType}">selected</c:if>>MYSQL</option>
								<option value="postgresql" <c:if test="${dbType=='postgresql'}">selected</c:if>>PostgreSQL</option>
								<option value="oracle9i" <c:if test="${dbType=='oracle9i'}">selected</c:if>>Oracle9i+</option>
								<option value="db2" <c:if test="${dbType=='db2'}">selected</c:if>>DB2</option>
								<option value="sqlserver" <c:if test="${dbType=='sqlserver'}">selected</c:if>>SQL Server</option>
								<option value="hsqldb" <c:if test="${dbType=='hsqldb'}">selected</c:if>>HSQLDB</option>
							</select> 
						</td>
					</tr>
					<tr>
						<td width="150"><fmt:message key='connect.type'/></td>
						<td align="left">
							<select name="connectType"  id="connectType" onchange="connTypeChange(this.value)">
								<option value="jdbc" <c:if test="${connectType=='jdbc'}">selected</c:if>>JDBC</option>
								<option value="datasource" <c:if test="${connectType=='datasource'}">selected</c:if>>Datasource</option>
							</select> 
						</td>
					</tr>
					<tr>
						<td width="150"><fmt:message key='no.delete.tables'/></td>
						<td><input type="checkbox" name="connonly" id="connonly" value="true"> </td>
					</tr>					
					<tr>
						<td colspan="2">
						<div id="jdbcDiv">
							<table align="center" cellpadding="5" cellspacing="0" border="0" class="setup-main-sub">
								<tr>
									<td width="150"><fmt:message key='host'/> </td>
									<td><input type="text" name="host" id="host" onkeyup="change()" value="${host}"> </td>
								</tr>
								<tr>
									<td width="150"><fmt:message key='db.name'/> </td>
									<td><input type="text" name="dbname" id="dbname" onkeyup="change()" value="${dbname}"> </td>
								</tr>
								<tr>
									<td width="150"><fmt:message key='db.schema'/> </td>
									<td><input type="text" name="dbschema" id="dbschema" value="${dbschema}"> </td>
								</tr>
								<tr>
									<td width="150"><fmt:message key='user.name'/> </td>
									<td><input type="text" name="username" id="username" value="${username}"> </td>
								</tr>
								<tr>
									<td width="150"><fmt:message key='password'/> </td>
									<td> <input type="password" name="password" id="password" value="${password}"> </td>
								</tr>
								<tr>
									<td width="150"><fmt:message key='re.password'/> </td>
									<td> <input type="password" name="password1" id="password1" value="${password}"> </td>
								</tr>
								<tr>
									<td><div id="dth"><fmt:message key='driver.type'/></div></td>
									<td><div id="dtc">
										<c:set var="jtdsCk" value="checked='checked'"/>
										<c:set var="msCk" value=""/>
										<c:if test="${driverType=='ms'}">
											<c:set var="jtdsCk" value=""/>
											<c:set var="msCk" value="checked='checked'"/>
										</c:if>
										<input type="radio" name="driverType" value="" onclick="changeDriverType()"  ${jtdsCk}>jTDS  
										<input type="radio" name="driverType" value="ms" onclick="changeDriverType()" ${msCk}>Microsoft
										</div>
									</td>
								</tr>
								<tr>
									<td><fmt:message key='recommended.url'/> </td>
									<td><div id="urlDIV" style="display:inline">${userDBUrl}</div> </div>
										<div id="edDiv" style="display:inline"> <a href="javascript:;" onclick="editURL()">  <fmt:message key='edit'/> </a></div></td>
								</tr>
							</table>
						</div>
						<div id="dsDiv">
							<table align="center" cellpadding="5" cellspacing="0" border="0" class="setup-main-sub">
								<tr>
									<td width="150"><fmt:message key='jndi.name'/></td>
									<td><input type="text" name="jndi" id="jndi" value="${jndi}"> </td>
								</tr>
							</table>
						</div>								
						</td>
					</tr>
					<tr>
						<td colspan="2">
							<c:choose>
								<c:when test="${existed}">
									<input type="button" id="submit-btn" value="<fmt:message key='confirm'/>" onclick="confirmReset()"> <%-- or <a href='javascript:;' onclick="nextStep()">skip to next step</a> --%>
								</c:when>
								<c:when test="${not empty error}">
									<input type="submit" id="submit-btn" value="<fmt:message key='retry'/>"> <%-- or <a href='javascript:;' onclick="nextStep()">ignore error and goto to next step</a> --%>
								</c:when>
								<c:otherwise>
									<input type="submit" id="submit-btn" value="<fmt:message key='submit'/>">
 									<%--  or <a href='javascript:;' onclick="nextStep()">skip to next step</a> --%>
								</c:otherwise>
							</c:choose>
						</td>
					</tr>
				</form>
			</table>
		</div>
		<script>
			<c:if test="${!urlEdited}">
				dbChange();
			</c:if>	
			<c:if test="${urlEdited}">
				editURL();
				if($("dbType").value == "sqlserver"){
					showDriverType(true);
				}else{
					showDriverType(false);
				}
			</c:if>	
			connTypeChange("${connectType}");	
		</script>
	</body>
</html>