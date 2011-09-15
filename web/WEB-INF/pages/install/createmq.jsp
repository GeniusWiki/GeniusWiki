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
			if(isBlank("url")){
				if(er != "") er += "<br>";
				er += "<fmt:message key='mq.not.blank'/> ";
			} 

			
			if(!er.blank()){
				$("innerErrorDiv").innerHTML = "<div class=\"error\" style=\"width:95%\">"+er+"</div>";
			}
			return er.blank();
		}
		function isBlank(id){
			var v = $(id).value;
			if( v== null)
				return true;
			
			return v.blank();
			
		}
		</script>
	</head>

	<body>
		<br>
		<div class="main">
		<table width="750" align="center" cellpadding="5" cellspacing="0" border="0" class="setup-main">
			<tr><td colspan="2">
				<div class="title"><fmt:message key='step.3'/></div>
				<div class="desc"><fmt:message key='step.3.tip'/>
				</div>
			</td></tr>
			<tr><td colspan="2">
				<div class="message-panel" id="innerErrorDiv"></div>
				<%@ include file="/common/messages.jsp"%>
			</td></tr>

		<form action="<c:url value="/install"/>" method="post"  onsubmit="return bsb()">
			<input type="hidden" name="step" value="cmq">
			<tr>
				<td width="220" nowrap="nowrap"><fmt:message key='embed.mq'/></td>
				<td> <input type="checkbox" name="embed" value="true" checked="checked"></td>
			</tr>
			<tr>
				<td><fmt:message key='mq.url'/></td>
				<td> <input type="text" name="url" id="url" value="${server.mqServerUrl}"></td>
			</tr>
			<tr>
				<td colspan="2">
					<c:choose>
						<c:when test="${empty error}">
							<input type="submit" value="<fmt:message key='submit'/>">
						</c:when>
						<c:otherwise>
							<input type="submit" value="<fmt:message key='retry'/>"> <fmt:message key='or'/> <a href='<c:url value="/install?step=db"/>'><fmt:message key='ignore.step'/></a>
						</c:otherwise>
					</c:choose>
				 </td>
			</tr>
		</form>
	</table>
	</div>
	</body>
</html>