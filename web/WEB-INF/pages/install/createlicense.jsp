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
			if(!$('l1').checked){
				var er = ""
				if(isBlank("license")){
					if(er != "") er += "<br>";
					er += " <fmt:message key='license.not.blank'/> ";
				} 
	
				
				if(!er.blank()){
					$("innerErrorDiv").innerHTML = "<div class=\"error\" style=\"width:95%\">"+er+"</div>";
				}
				return er.blank();
			}
			return true;
		}
		function isBlank(id){
			var v = $(id).value;
			if( v== null)
				return true;
			
			return v.blank();
			
		}
		function typeChange(){
			if($('l1').checked){
				Element.hide("license");
			}else{
				Element.show("license");
			}
		}
		</script>
	</head>

	<body>
		<br>
		<div class="main">
		<table width="750" align="center" cellpadding="5" cellspacing="0" border="0" class="setup-main">
			<tr><td colspan="2">
				<div class="title"><fmt:message key="step.2"/></div>
				<div class="desc"><fmt:message key="step.2.tip"/>  
				</div>
			</td></tr>
			<tr><td colspan="2">
				<div class="message-panel" id="innerErrorDiv"></div>
				<%@ include file="/common/messages.jsp"%>
			</td></tr>
		
		<c:choose>
			<c:when test="${not empty license}">
				<c:set var="bizLic" value="checked='checked'" />
				<c:set var="freeLic" value="" />
			</c:when>
			<c:otherwise>
				<c:set var="freeLic" value="checked='checked'" />
				<c:set var="bizLic" value="" />
			</c:otherwise>
		</c:choose>
		
		<form action="<c:url value="/install"/>" method="post"  onsubmit="return bsb()">
			<input type="hidden" name="step" value="clicense">
			<tr>
				<td><input type="radio" name="licenseType" id="l1" value="free" ${freeLic} onclick="typeChange()"> <fmt:message key='free.license'/></td>
			</tr>
			<tr>
				<td><input type="radio" name="licenseType" id="l2" value="commercial" ${bizLic} onclick="typeChange()"> <fmt:message key='input.license'/></td>
			</tr>
			<tr>
				<td><textarea name="license" id="license" style="display:none;width:450px;height:180px" >${license}</textarea></td>
			</tr>
			<tr>
				<td colspan="2">
					<input type="submit" value="<fmt:message key='submit'/> ">
				 </td>
			</tr>
		</form>
	</table>
	</div>
	
	<script  type="text/javascript">
		typeChange();
	</script>
	</body>
</html>