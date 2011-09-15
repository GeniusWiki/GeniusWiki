<%@ include file="/common/taglibs.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html>
	<head>
		<title><edgenius:title/> - loading</title>
		<script type="text/javascript" src="gwtpage/gears_init.js"></script>
		<script type="text/javascript" src="gwtpage/monitor.js"></script>
	</head>

	<body>

	<p>GeniusWiki try to detect your network status and load your data locally if it is offline.</p>
	<p>Please manually append /${ctxPath}/page into your URL if this page doesn't redirect automatically for minutes. </p>
	

		<script  type="text/javascript">
			var monitor = new Monitor(location.href);
		    monitor.onconnectionchange = function(connected) {
		        if (connected) {
				    //online
	             	location.href="${ctxPath}/page";
		        }else {
		            //offline
					location.href="offline.do";
		        }
		    }
					
			monitor.start();
		</script>
	</body>
</html>