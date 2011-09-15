<%@ include file="/common/taglibs.jsp"%>
  
<html>
	<head>
        <%@ include file="/common/meta.jsp"%>
		<title><edgenius:title/></title>
		<link rel="stylesheet" type="text/css" media="all" href="${ctxPath}/static/styles/setup.css?v=@TOKEN.SITE.VERSION@" />
		<script type="text/javascript" src="${ctxPath}/static/scripts/prototype.js"></script>
		<script type="text/javascript">
			function editURL(){
				var ed = $("urlEdited").value;
				
				if( ed == "false"){
					var url = $("urlDIV").innerHTML;
					$("urlDIV").innerHTML = "<input type='text' name='urlbox' id='urlbox' style='width:500px' value='"+trim(url)+"'>";
					$("edDiv").innerHTML = " <a href='javascript:;' onclick='cancelURLEditing()'>cancel</a></div>";
					$("urlEdited").value = "true";
					$("host").disabled="disabled";
					$("dbname").disabled="disabled";
				}
			}
			function cancelURLEditing(){
				var ed = $("urlEdited").value;
				
				if( ed == "true"){
					var url = $("urlDIV").innerHTML;
					$("urlDIV").innerHTML = getURL();
					$("edDiv").innerHTML = "<a href='javascript:;' onclick='editURL()'>edit</a></div>";
					$("urlEdited").value = "false";
					$("host").disabled="";
					$("dbname").disabled="";
				}
			}
			function bsb(){
				if($("dbType").value != "mysql"){
					return true;
				}
				var ed = $("urlEdited").value;
				var er = ""
				if(isBlank("host") && ed == "false"){
					if(er != "") er += "<br>";
					er += "<fmt:message key='host.not.blank'/>";
				} 
				if(isBlank("dbname") && ed == "false"){
					if(er != "") er += "<br>";
					er += "<fmt:message key='db.not.blank'/>";
				} 
				if(isBlank("rootUser")){
					if(er != "") er += "<br>";
					er += "<fmt:message key='root.not.blank'/>";
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

				if(!er.blank()){
					$("innerErrorDiv").innerHTML = "<div class=\"error\" style=\"width:95%\">"+er+"</div>";
				}else{
					 Form.Element.disable("submit-btn");
				}
				
				return er.blank();
			}
			function confirmReset(){
				if(confirm("<fmt:message key='confirm.delete.db'/>")){
					$("confirmed").value = true;
					if(bsb()){
						$("fm").submit();
					}
				}
			}

			var map = {};
			<c:forEach var="item" items="${adminUrlP}">
			map['${item.key}'] = "${item.value}";
			</c:forEach>
			
			function getURL(){
				var pattern = map[$("dbType").value];
				var h = $("host").value;
				var d = $("dbname").value;
				var u = pattern.replace("@HOST@",h);
				return u.replace("@DBNAME@",u);
			}
			
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
			function dbChange(t){
				cancelURLEditing();
				if(t == "mysql" ){
					$("urlDIV").innerHTML = getURL();
					Element.show("createDIV");
					Element.show("createDIV2");
					Element.hide("createDIVOtherDB");
				}else{
					Element.hide("createDIV");
					Element.hide("createDIV2");
					Element.show("createDIVOtherDB");
				}
				
			}
		</script>
	</head>

	<body onload="change()">
		<br>
		<div class="main">
		<table align="center" cellpadding="5" cellspacing="0" border="0" class="setup-main">
			<tr><td colspan="2">
				<div class="title"><fmt:message key="step.4"/></div>
				<div class="desc"><fmt:message key="step.4.tip"/>
				</div>
			</td></tr>
			<tr><td colspan="2">
				 <div class="message-panel" id="innerErrorDiv"></div>
				 <%@ include file="/common/messages.jsp"%>
			</td></tr>

		<form action="<c:url value='/install'/>" method="post" id="fm" onsubmit="return bsb()">
			<input type="hidden" name="step" value="cdb">
			<input type="hidden" name="urlEdited" id="urlEdited"  value="false">
			<input type="hidden" name="adminDBUrl" id="url">
			<input type="hidden" name="confirmed" id="confirmed" value="false">
			<tr>
				<td width="150"><fmt:message key="database"/></td>
				<td>
					<select name="dbType" id="dbType" onchange="dbChange(this.value)">
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
				<td colspan="2">
					<div id="createDIV">
						<div class="desc">
							<fmt:message key="mysql.desc1"/>
							<a href="<c:url value='/install?script=db&type=mysql'/>" target="_blank"><font color="blue"><b><u><fmt:message key="mysql.desc2"/></u></b></font></a>.
							<fmt:message key="mysql.desc3"/>
							<br><fmt:message key="mysql.desc4"/>
						</div>
						<table align="center" cellpadding="5" cellspacing="0" border="0" class="setup-main-sub">
							<tr>
								<td width="150"><fmt:message key="host"/></td>
								<td> <input type="text" name="host" id="host" onkeyup="change()" value="${host}" > </td>
							</tr>
							<tr>
								<td width="150"><fmt:message key="db.name"/> </td>
								<td><input type="text" name="dbname" id="dbname" onkeyup="change()" value="${dbname}"> </td>
							</tr>
							<%-- MySQL current no support for SCHEMA 
							<tr>
								<td width="150"><fmt:message key='db.schema'/> </td>
								<td><input type="text" name="dbschema" id="dbschema" value="${dbschema}"> </td>
							</tr>
							--%>
							<tr>
								<td width="150"><fmt:message key="root.username"/> </td>
								<td><input type="text" name="rootUser" id="rootUser" value="${rootUser}"> </td>
							</tr>
							<tr>
								<td width="150"><fmt:message key="root.password"/></td>
								<td><input type="password" name="rootPassword" value="${rootPassword}"></td>
							</tr>
							<tr>
								<td width="150"><fmt:message key="user.name"/> </td>
								<td><input type="text" name="username" id="username" value="${username}"> </td>
							</tr>
							<tr>
								<td width="150"><fmt:message key="password"/></td>
								<td><input type="password" name="password" id="password" value="${password}"> </td>
							</tr>
							<tr>
								<td width="150"><fmt:message key="re.password"/></td>
								<td><input type="password" name="password1" id="password1" value="${password}"> </td>
							</tr>
							<tr>
								<td colspan="2"><br></td>
							</tr>
							<tr>
								<td width="150"><fmt:message key="recommended.url"/></td>
								<td><div id="urlDIV" style="display:inline">${adminDBUrl}</div>
									<div id="edDiv" style="display:inline"> <a href="javascript:;" onclick="editURL()">  <fmt:message key="edit"/></a></div></td>
							</tr>
						</table>
					</div>
					<div id="createDIVOtherDB" style="display:none">
						<b><fmt:message key="step.4.needdb"/></b>
					</div>
				</td>
			</tr>
			
			<tr>
				<td colspan="2">
					<c:choose>
						<c:when test="${existed}">
							<input type="button" id="submit-btn" value="<fmt:message key='yes.overwrite'/>" onclick="confirmReset()"> <fmt:message key='or'/> <a href='<c:url value="/install?step=tables"/>'><fmt:message key="skip.step"/></a>
						</c:when>
						<c:when test="${not empty error}">
							<input type="submit" id="submit-btn" value="<fmt:message key='retry'/>"> <fmt:message key='or'/> <a href='<c:url value="/install?step=tables"/>'><fmt:message key='ignore.step'/></a>
						</c:when>
						<c:otherwise>
							<input type="submit" id="submit-btn" value="<fmt:message key='submit'/>">
							<span id="createDIV2"> or <a href='<c:url value="/install?step=tables"/>'><fmt:message key="skip.step"/></a> </span>
						</c:otherwise>
					</c:choose>
				</td>
			</tr>
		</form>
	</table>
	</div>
	
	<script>
		<c:if test="${urlEdited}">
			editURL();
		</c:if>
		
		dbChange($("dbType").value);	
		
	</script>
	</body>
</html>