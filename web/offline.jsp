<%@page import="com.edgenius.core.Global"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ include file="/common/taglibs.jsp"%>
<html>
	<head>
   		 <%@ include file="/common/meta.jsp"%>
        <meta name="gwt:property" content="locale=${PreferredLocale}">
   		 
        <link rel="stylesheet" type="text/css" media="all" href="${skinPath}/styles/render.css" />
		<title><edgenius:title/> - <fmt:message key="offline"/></title>
 		<script language='javascript' src='gwtpage/gwtpage.nocache.js?v=@TOKEN.SITE.VERSION@'></script>
		<script type="text/javascript" src="gwtpage/monitor.js"></script>
 		<script language='javascript' src='gwtpage/gears_init.js'></script>
    	<%@ include file="/common/view-scripts.jsp"%>
    	<%@ include file="/common/edit-scripts.jsp"%>
	</head>

	<body>
	
	 <div id="page">
        <jsp:include page="/common/header.jsp"/>
        <div id="globalMessage"></div>
		<div id="leftmenu"></div>
	    <div id="content"></div>
		<div id="rightmenu"></div>
        <jsp:include page="/common/footer.jsp"/>
	</div>	
	<div id="leftsidebarbtn" class="left-sidebar-btn" style="display:none"></div>
	<div id="rightsidebarbtn" class="right-sidebar-btn" style="display:none"></div>

	<iframe src="javascript:''" id="__gwt_historyFrame" tabIndex='-1' style="position:absolute;width:0;height:0;border:0"></iframe>
	<input type="hidden" id="offlineDiv" name="offlineDiv" value="true"/>	
	<input type="hidden" id="systemTitle" name="systemTitle" value="<edgenius:title/>"/>
		
	<div id="offlineAttachmentDiv" style="display: none"></div>

	<script  type="text/javascript">
		var monitor = new Monitor("<%=Global.SysHostProtocol%><%=Global.SysHostAddress%><%=Global.SysContextPath%>");
	    monitor.onconnectionchange = function(connected) {
	        if (connected) {
			    //online
             	document.getElementById('offlineDiv').value="false";
	        }else {
	            //offline
				document.getElementById('offlineDiv').value="true";
	        }
	    }
		//this method is called in gwt code, while system is disconnected.
		function goOffline(){				
			document.getElementById('offlineDiv').value="true";
			monitor.start();
		}
		goOffline();
	</script>
	</body>
</html>

