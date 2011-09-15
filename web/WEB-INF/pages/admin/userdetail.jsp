<%@ include file="/common/taglibs.jsp"%>
<%@ page import="com.edgenius.wiki.gwt.client.server.utils.SharedConstants" %>  
  
<div class="detail">
	<table cellpadding="0" cellspacing="0" border="0" width="100%">
		<tr>
			<td width="250px">
				<span class="func"><fmt:message key="portrait"/></span><div class="separator"></div>
			</td>
			<td>
				<span class="func"><fmt:message key="statistic"/></span><div class="separator"></div>
			</td>
		</tr>
		<tr>
			<td valign="top" height="200px">
				<c:choose>
					<c:when test="${not empty dto.user.portrait}">
							<c:url var='purl' value="/download">
								<c:param name="portrait">${dto.user.portrait}</c:param>
							</c:url>
					</c:when>
					<c:otherwise>
						<c:set var='purl'>
							<c:url value="/static/images/"/><%=SharedConstants.NO_PORTRAIT_IMG%>
						</c:set>
					</c:otherwise>
				</c:choose>
				<img src="${purl}" title="${dto.user.fullname}"/>
			</td>
			<td valign="top">
				<table cellpadding="0" cellspacing="0" border="0" width="100%">
					<tr>
						<td>
							<fmt:message key="page.as.author"><fmt:param value="${dto.authorSize}"/></fmt:message> 
						</td>
					</tr>
					<tr>
						<td>
							<fmt:message key="page.as.modifier"><fmt:param value="${dto.modifierSize}"/></fmt:message> 
						</td>
					</tr>
					<tr>
						<td>
							<fmt:message key="space.as.author"><fmt:param value="${dto.spaceAuthorSize}"/></fmt:message> 
						</td>
					</tr>
					<tr>
					<td>
						<fmt:message key="comment.as.author"><fmt:param value="${dto.commentSize}"/></fmt:message> 
					</td>
					</tr>
				</table>
			</td>
		</tr>
	</table>
	<br>

	<c:set var="roleSize" value="${fn:length(dto.user.roles)}"/>
	<span class="func"><fmt:message key="joined.group"><fmt:param value="${roleSize}"/></fmt:message> </span><div class="separator"></div>
	<c:forEach var="role" items="${dto.user.roles}" varStatus="s">
		${role.displayName}
		<c:if test="${s.index+1 != roleSize}">
		,
		</c:if>
	</c:forEach>
	<br>
	<br>
	<span class="func"><fmt:message key="actions"/></span><div class="separator"></div>
	<div class="func-area" id="func-area-${dto.user.uid}">
		<jsp:include page="userfunc.jsp"/>
	</div>
<br>
</div>