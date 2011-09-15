<%@ include file="/common/taglibs.jsp"%>

<html>
	<head>
		<script type="text/javascript">
			function cancelUpload(){
				parent.logoDone();
			}
			
			var done = <c:choose><c:when test="${param.done}">true</c:when><c:otherwise>false</c:otherwise></c:choose>;
			if(done){
				parent.logoDone();
			}
			
		</script>
	</head>
	<body>
		<form action="<c:url value='/instance/general!upload.do'/>" id="uploadForm" method="post" enctype="multipart/form-data" >
			<input id="logo" name="file" type="file">
			<input type="submit" value="<fmt:message key='upload'/>">
			<input type="button" value="<fmt:message key='cancel'/>" onclick="cancelUpload()">
		</form>
	</body>
</html>