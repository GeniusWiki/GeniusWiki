<%@ include file="/common/taglibs.jsp"%>
<%@ page import="com.edgenius.core.model.User" %>  
		<c:set var="evenRow" value="false"/> 
		<c:set var="sortByFullName"><%=User.SORT_BY_FULL_NAME%></c:set>
		<c:set var="sortByLoginName"><%=User.SORT_BY_USERNAME%></c:set>
		<c:set var="sortByEmail"><%=User.SORT_BY_EMAIL%></c:set>
		<c:set var="sortByRegisterDate"><%=User.SORT_BY_CREATED_DATE%></c:set>
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
			<c:when test="${sortBy==sortByLoginName}">
				<c:set var="s1" value="${sort}"/>
				<c:set var="sd1" value="${!sortByDesc}"/>
			</c:when>
			<c:when test="${sortBy==sortByFullName}">
				<c:set var="s2" value="${sort}"/>
				<c:set var="sd2" value="${!sortByDesc}"/>
			</c:when>
			<c:when test="${sortBy==sortByEmail}">
				<c:set var="s3" value="${sort}"/>
				<c:set var="sd3" value="${!sortByDesc}"/>
			</c:when>
			<c:when test="${sortBy==sortByRegisterDate}">
				<c:set var="s4" value="${sort}"/>
				<c:set var="sd4" value="${!sortByDesc}"/>
			</c:when>
		</c:choose>
		 
<table  align="center" class="list">
	<tr>
		<th class="${s1}"  width="200px"><a href="<c:url value='/instance/users.do'><c:param name='sortBy'>${sortByLoginName}</c:param><c:param name='sortByDesc'>${sd1}</c:param><c:param name='filter'>${filter}</c:param></c:url>"><fmt:message key="user.name"/></a></th>
		<th class="${s2}"  width="400px"><a href="<c:url value='/instance/users.do'><c:param name='sortBy'>${sortByFullName}</c:param><c:param name='sortByDesc'>${sd2}</c:param><c:param name='filter'>${filter}</c:param></c:url>"><fmt:message key="full.name"/></a></th>
		<th class="${s3}"  width="400px"><a href="<c:url value='/instance/users.do'><c:param name='sortBy'>${sortByEmail}</c:param><c:param name='sortByDesc'>${sd3}</c:param><c:param name='filter'>${filter}</c:param></c:url>"><fmt:message key="email"/></a></th>
		<th class="${s4}"  width="280px"><a href="<c:url value='/instance/users.do'><c:param name='sortBy'>${sortByRegisterDate}</c:param><c:param name='sortByDesc'>${sd4}</c:param><c:param name='filter'>${filter}</c:param></c:url>"><fmt:message key="registered.date"/></a></th>
		<th class="nowrap"></th>
	</tr>
	<c:forEach var="dto" items="${users}">
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
			<td>${dto.user.username}</td> 
			<td>${dto.user.fullname}</td> 
			<td>${dto.user.contact.email}</td> 
			<td>${dto.createdDate}</td> 
			<td class="nowrap"><a href="#"  onclick="showDetail(${dto.user.uid})" id="link-${dto.user.uid}"><fmt:message key="detail"/></a></td> 
		</tr>
		<tr id="row-${dto.user.uid}" style="display:none" class="detailrow">
			<td colspan="7" class="detailrow">
				<div class="detail" id="detail-${dto.user.uid}">
					<img id="loading-${dto.user.uid}" src="${ctxPath}/static/images/large-loading.gif">
				</div>
			</td>
		</tr>
	</c:forEach>
</table>
<div class="pagination">
	<edgenius:page totalPage="${pagination.totalPage}" currentPage="${pagination.currentPage}" sortBy="${sortBy}" sortByDesc="${sortByDesc}" filter="${filter}" displayPage="10"/>
</div>
<div class="total">
	<fmt:message key="total.users"><fmt:param value="${total}"/></fmt:message> 
</div>