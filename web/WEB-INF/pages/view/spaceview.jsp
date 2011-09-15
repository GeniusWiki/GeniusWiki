<%-- pagination of page list:curent, it is all page list becuase PageService.getPageTree() is reading from cache, pagination is not necessary --%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ include file="/common/taglibs.jsp"%>
<html>
	<head>
		<meta name="keywords" content="${space.tagString}"/>
		<meta name="description" content="${space.description}"/>
      	<%@ include file="/common/meta-plain.jsp"%>
      	<link rel="stylesheet" type="text/css" media="all" href="${skinPath}/styles/view.css?v=@TOKEN.SITE.VERSION@" />

		<title><fmt:message key="app.name"/> - ${space.name}</title>
	</head>

	<body>
		<c:forEach var="page" items="${list}">
			<p>
				<%-- detect if spaceUname has invalid characters, such as chinese, japanese etc. --%>
				<c:set var="url">${edgenius:getPageRedirFullURL(readonly,page.space.unixName,page.title,page.pageUuid)}</c:set>
				<a href="${url}">${page.title}</a><br>

			</p>
		</c:forEach>

		<div>
		<a href="${edgenius:getRootURL(readonly)}">Return home</a>
		</div>

	</body>
</html>
