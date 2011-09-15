<%@ include file="/common/taglibs.jsp"%>
  
<html>
	<head>
        <%@ include file="/common/meta.jsp"%>
		<title><edgenius:title/></title>
		<link rel="stylesheet" type="text/css" media="all" href="${ctxPath}/static/styles/setup.css?v=@TOKEN.SITE.VERSION@" />
		<script type="text/javascript" src="${ctxPath}/static/scripts/prototype.js"></script>
		<script type="text/javascript">
			function bsb(){
				//valid username, password, email, fullname can not be null
				var er = ""
				if(isBlank("username")){
					if(er != "") er += "<br>";
					er += "<fmt:message key='username.not.blank'/>";
				} 
				if(isBlank("password")){
					if(er != "") er += "<br>";
					er += "<fmt:message key='password.not.blank'/>";
				} 
				if(isBlank("email")){
					if(er != "") er += "<br>";
					er += "<fmt:message key='email.not.blank'/>";
				} 
				if(isBlank("fullname")){
					if(er != "") er += "<br>";
					er += "<fmt:message key='fullname.not.blank'/>"
				}
				if($("password").value != $("password1").value){
					if(er != "") er += "<br>";
					er += "<fmt:message key='password.same'/>"
				}
				if(!er.blank()){
					$("innerErrorDiv").innerHTML = "<div class=\"error\" style=\"width:95%\">"+er+"</div>";
				}else{
					 Form.Element.disable("submit-btn");
				}
				return er.blank();
			}
			function isBlank(id){
				var v = $(id).value;
				if( v== null)
					return true;
				
				return v.blank();
				
			}
			function nextStep(){
				if(confirm("<fmt:message key='skip.admin.confirm'/>")){
					location.href = '<c:url value="/install?step=done"/>';
				}
			}
			
		</script>
	</head>

	<body>
	<br>
		<div class="main">
		<table width="750" align="center" cellpadding="5" cellspacing="0" border="0" class="setup-main">
			<tr><td colspan="2">
				<div class="title"><fmt:message key="step.6"/></div>
				<div class="desc"><fmt:message key="step.6.tip"/>
				</div>
			</td></tr>
			<tr><td colspan="2">
				<div class="message-panel" id="innerErrorDiv"></div>
				<%@ include file="/common/messages.jsp"%>
			</td></tr>

		<form action="<c:url value='/install'/>" method="post" id="fm" onsubmit="return bsb()">
			<input type="hidden" name="step" value="cadmin">

			<tr>
				<td width="150"><fmt:message key="user.name"/></td>
				<td>  <input type="text" name="username" id="username" value="${username}">  </td>
			</tr>
			<tr>
				<td><fmt:message key="password"/></td>
				<td>  <input type="password" name="password" id="password" value="${password}">  </td>
			</tr>
			<tr>
				<td><fmt:message key="re.password"/></td>
				<td> <input type="password" name="password1" id="password1" value="${password}"> </td>
			</tr>
			<tr>
				<td><fmt:message key="email"/></td>
				<td>  <input type="text" name="email" id="email" value="${email}">  </td>
			</tr>
			<tr>
				<td><fmt:message key="fullname"/></td>
				<td> <input type="text" name="fullname" id="fullname" value="${fullname}">  </td>
			</tr>
			<tr>
				<td colspan="2">
					<c:choose>
						<c:when test="${not empty error}">
							<input type="submit" id="submit-btn" value="<fmt:message key='retry'/>">  <fmt:message key='or'/> <a href='javascript:;' onclick="nextStep()"><fmt:message key="ignore.step"/></a>
						</c:when>
						<c:otherwise>
							<input type="submit" id="submit-btn" value="<fmt:message key='submit'/>">  <fmt:message key='or'/> <a href='javascript:;' onclick="nextStep()"><fmt:message key="skip.step"/></a>
						</c:otherwise>
					</c:choose>
				 </td>
			</tr>
		</form>
	</table>
	</div>
	</body>
</html>