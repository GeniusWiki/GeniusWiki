<%@ include file="/common/taglibs.jsp"%>
<%@page import="com.edgenius.wiki.gwt.client.server.utils.SharedConstants"%>
<%--

Version @TOKEN.APP.VERSION@
&middot; Copyright &copy; @TOKEN.COPYRIGHT.YEAR@ 
&middot; <a href="http://@TOKEN.COMPANY.WEBSITE@">@TOKEN.COMPANY.NAME@</a><br />
<!-- Built on @BUILD-TIME@ -->

--%>

<div id="footer">
<div id="preload" style="display:none; position: absolute; left: -2000px; top: 0px; width: 0px; height: 0px;">
	<img src="${skinPath}/images/dlg/dlg_top_left.png">
	<img src="${skinPath}/images/dlg/dlg_top_right.png">
	<img src="${skinPath}/images/dlg/dlg_bottom_left.png">
	<img src="${skinPath}/images/dlg/dlg_bottom_right.png">
	<img src="${skinPath}/images/dlg/dlg_top.png">
	<img src="${skinPath}/images/dlg/dlg_bottom.png">
	<img src="${skinPath}/images/dlg/dlg_left.png">
	<img src="${skinPath}/images/dlg/dlg_right.png">
</div>
<p>
		
		<%-- public Search Engine entry link --%>
		<a href="${ctxPath}/">@TOKEN.APP.NAME@</a> &copy;  @TOKEN.COPYRIGHT.YEAR@  |
		<fmt:message key="power.by"/> <a href="http://www.edgenius.com" target="_blank">Edgenius</a> | 
		<a href="http://edgenius.com/support" target="_blank">Feedback</a> |
		<a href="javascript:wopen('${ctxPath}/static/privacy.html', 'Privacy Policy', 800, 600);">Privacy Policy</a>  |
		version @TOKEN.APP.VERSION@ build @TOKEN.BUILD.ID@   |
		<a href="${ctxPath}/<%=SharedConstants.URL_PAGE%>#/$CPAGE/ad"><fmt:message key="msg.to.admin"/></a> |
		<c:set var="viewpage"><%= request.getParameter("viewpage") %></c:set> 
		<c:if test="${not viewpage}">
			<span style="font-weight:bold;"><fmt:message key="press.help"/> </span>
		</c:if>
</p>
</div>
