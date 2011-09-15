<%@ include file="/common/taglibs.jsp"%>
<%@ page import="com.edgenius.wiki.gwt.client.server.utils.SharedConstants" %>  
<%@ page import="com.edgenius.core.model.Role" %>
<c:set var="evenRow" value="false"/>
<table  align="center" class="list">
	<c:set var="sortByType"><%=Role.SORT_BY_TYPE%></c:set>
	<c:set var="sortByDisplayName"><%=Role.SORT_BY_DISPLAYNAME%></c:set>
	<c:set var="sortByDescription"><%=Role.SORT_BY_DESC%></c:set>
	<c:set var="sortByUsersCount"><%=Role.SORT_BY_USERS_COUNT%></c:set>
	
	<c:set var="sort" value="asc"/>
	<c:if test="${sortByDesc}">
		<c:set var="sort" value="desc"/>
	</c:if>
	<c:set var="s1" value="sortable"/>
	<c:set var="s2" value="sortable"/>
	<c:set var="s3" value="sortable"/>
	<c:set var="s4" value="sortable"/>
	<c:set var="sd1" value="false"/>
	<c:set var="sd2" value="false"/>
	<c:set var="sd3" value="false"/>
	<c:set var="sd4" value="false"/>
	<c:choose>
		<c:when test="${sortBy==sortByType}">
			<c:set var="s1" value="${sort}"/>
			<c:set var="sd1" value="${!sortByDesc}"/>
		</c:when>
		<c:when test="${sortBy==sortByDisplayName}">
			<c:set var="s2" value="${sort}"/>
			<c:set var="sd2" value="${!sortByDesc}"/>
		</c:when>
		<c:when test="${sortBy==sortByDescription}">
			<c:set var="s3" value="${sort}"/>
			<c:set var="sd3" value="${!sortByDesc}"/>
		</c:when>
		<c:when test="${sortBy==sortByUsersCount}">
			<c:set var="s4" value="${sort}"/>
			<c:set var="sd4" value="${!sortByDesc}"/>
		</c:when>
	</c:choose>
	<tr>
		<th class="${s2}" width="230px"><a href="<c:url value='/instance/roles.do'><c:param name='sortBy'>${sortByDisplayName}</c:param><c:param name='sortByDesc'>${sd2}</c:param><c:param name='filter'>${filter}</c:param></c:url>"><fmt:message key="name"/></a></th>
		<th class="${s3}" width="500px"><a href="<c:url value='/instance/roles.do'><c:param name='sortBy'>${sortByDescription}</c:param><c:param name='sortByDesc'>${sd3}</c:param><c:param name='filter'>${filter}</c:param></c:url>"><fmt:message key="description"/></a></th>
		<th class="${s4}" width="50px"><a href="<c:url value='/instance/roles.do'><c:param name='sortBy'>${sortByUsersCount}</c:param><c:param name='sortByDesc'>${sd4}</c:param><c:param name='filter'>${filter}</c:param></c:url>"><fmt:message key="users"/></a></th>
		<th class="nowrap"></th>
	</tr>
	<c:set var="roleNamePublic"><%=SharedConstants.ROLE_ANONYMOUS%></c:set>
	<c:set var="roleNameRegister"><%=SharedConstants.ROLE_REGISTERED%></c:set>
	
	<c:forEach var="dto" items="${roles}">
		<c:choose>
			<c:when test="${evenRow}">
				<c:set var="rowClass" value="class='even'"/>
				<c:set var="evenRow" value="false"/>
			</c:when>
			<c:otherwise>
				<c:set var="rowClass" value=""/>
				<c:set var="evenRow" value="true"/>
			</c:otherwise>
		</c:choose>
		<tr ${rowClass}>
			<td width="300px">${dto.role.displayName}</td> 
			<td width="500px">${dto.role.description}</td> 
			<td width="500px">${dto.usersCount}</td> 
			<td  align="center" class="nowrap">
				<c:choose>
					<c:when test="${dto.role.name == roleNamePublic || dto.role.name == roleNameRegister}">
						<span style="color: grey"><fmt:message key="not.available"/></span>
					</c:when>
					<c:otherwise>
						<a href="#" onclick="javascript:showDetail(${dto.role.uid})" id="link-${dto.role.uid}"><fmt:message key="detail"/></a>
					</c:otherwise>
				</c:choose>
			</td>
		</tr>
		<tr id="row-${dto.role.uid}" style="display:none" class="detailrow">
			<td colspan="4" class="detailrow">
				<div class="detail" id="detail-${dto.role.uid}">
					<img id="loading-${dto.role.uid}" src="${ctxPath}/static/images/large-loading.gif">
				</div>
			</td>
		</tr>
	</c:forEach>
</table>
<div class="pagination">
	<edgenius:page totalPage="${pagination.totalPage}" currentPage="${pagination.currentPage}" sortBy="${sortBy}" sortByDesc="${sortByDesc}" filter="${filter}" displayPage="10"/>
</div>
<div class="total">
	<fmt:message key="total.roles"><fmt:param value="${total}"/></fmt:message> 
</div>