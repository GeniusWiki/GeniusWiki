<%-- pagination  of space list --%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ include file="/common/taglibs.jsp"%>
<html>
	<head>
       <%@ include file="/common/meta.jsp"%>
       <link rel="stylesheet" type="text/css" media="all" href="${skinPath}/styles/view.css?v=@TOKEN.SITE.VERSION@" />
       
		<title><edgenius:title/></title>
	</head>

	<body>
		<div id="content">
			<div id="home">
				${content}
			<div>
			<div id="spacelist">
				<c:forEach var="space" items="${list}">
					<p>
						<c:set var="url">${edgenius:getSpaceRedirFullURL(readonly, space.uid, space.unixName)}</c:set>
						<a href="${url}">${space.name}</a><br>
							${space.description}
					</p>
				</c:forEach>
			</div>
						
			<div id="pagination">
				<a href="${edgenius:getRootURL(readonly)}">Home</a> &nbsp; 
				<edgenius:page totalPage="${pagination.totalPage}" currentPage="${pagination.currentPage}" show="true"/>
			</div>
		</div>
	</body>
</html>
