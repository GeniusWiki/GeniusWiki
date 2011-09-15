<%-- Page Content HTML render: object is Page --%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ include file="/common/taglibs.jsp"%>
<html>
	<head>
		<meta name="keywords" content="${page.title},${page.tagString}"/>
		<meta name="description" content="${page.title}"/>
		
        <%@ include file="/common/meta-plain.jsp"%>
        <%@ include file="/common/view-scripts.jsp"%>
        <link rel="stylesheet" type="text/css" media="all" href="${skinPath}/styles/render.css?v=@TOKEN.SITE.VERSION@" />
        <link rel="stylesheet" type="text/css" media="all" href="${skinPath}/styles/view.css?v=@TOKEN.SITE.VERSION@" />
        
		<title><fmt:message key="app.name"/> - ${page.title}</title>
	</head>

	<body>
		<c:if test="${adsense}">
			<script type="text/javascript"><!--
			google_ad_client = "ca-pub-0505357875049629";
			/* GeniusWiki */
			google_ad_slot = "4737491016";
			google_ad_width = 728;
			google_ad_height = 90;
			//-->
			</script>
			<script type="text/javascript" src="http://pagead2.googlesyndication.com/pagead/show_ads.js"></script>
			<table height="100px"><tr><td></td></tr></table>
		</c:if>
		<table class="content-container" cellpadding="0" border="0" width="820px" align="center">
			<tr><td>
			<div class="render-title" style="float:none;margin:10px;">${page.title}</div>
		
			<div class="render-content">
				${page.content.content}
			</div>
			<div class="render-author">
				Create by ${page.creator.fullname} on ${page.createdDate}<br/>
				Last updated by ${page.modifier.fullname} on ${page.modifiedDate} 
			</div>
			<div class="render-orig-link">
				<div>This page is from <a href="${origLink}">${origLink}</a></div>
				<c:if test="${not empty shellLink}">
					<div>Shell URL is <a href="${shellLink}">${shellLink}</a></div>
				</c:if>
			</div>
			 <jsp:include page="/common/footer.jsp">
			 	<jsp:param name="viewpage" value="true"/> 
			 </jsp:include>
			<%-- TODO: page tag, author and comment --%>
			
			</td></tr>
		</table>
	</body>	
</html>
