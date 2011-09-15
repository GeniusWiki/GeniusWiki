<%@ include file="/common/taglibs.jsp"%>
<%@ page import="com.edgenius.wiki.model.Space" %>  

<c:set var="evenRow" value="false"/>
<table align="center" class="list" >
	<tr>
			<c:set var="sortBySpaceKey"><%=Space.SORT_BY_SPACEKEY%></c:set>
			<c:set var="sortByCreateBy"><%=Space.SORT_BY_CREATEBY%></c:set>
			<c:set var="sortByCreateOn"><%=Space.SORT_BY_CREATEON%></c:set>
			<c:set var="sortByPageCount"><%=Space.SORT_BY_PAGE_COUNT%></c:set>
			
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
				<c:when test="${sortBy==sortBySpaceKey}">
					<c:set var="s1" value="${sort}"/>
					<c:set var="sd1" value="${!sortByDesc}"/>
				</c:when>
				<c:when test="${sortBy==sortByCreateBy}">
					<c:set var="s2" value="${sort}"/>
					<c:set var="sd2" value="${!sortByDesc}"/>
				</c:when>
				<c:when test="${sortBy==sortByCreateOn}">
					<c:set var="s3" value="${sort}"/>
					<c:set var="sd3" value="${!sortByDesc}"/>
				</c:when>
				<c:when test="${sortBy==sortByPageCount}">
					<c:set var="s4" value="${sort}"/>
					<c:set var="sd4" value="${!sortByDesc}"/>
				</c:when>
			</c:choose>
			
		<th class="${s1}" width="400px"><a href="<c:url value='/instance/spaces.do'><c:param name='sortBy'>${sortBySpaceKey}</c:param><c:param name='sortByDesc'>${sd1}</c:param><c:param name='filter'>${filter}</c:param></c:url>"><fmt:message key="space.uname"/></a></th>
		<th class="${s2}" width="200px"><a href="<c:url value='/instance/spaces.do'><c:param name='sortBy'>${sortByCreateBy}</c:param><c:param name='sortByDesc'>${sd2}</c:param><c:param name='filter'>${filter}</c:param></c:url>"><fmt:message key="created.by"/></a></th>
		<th class="${s3}" width="300px"><a href="<c:url value='/instance/spaces.do'><c:param name='sortBy'>${sortByCreateOn}</c:param><c:param name='sortByDesc'>${sd3}</c:param><c:param name='filter'>${filter}</c:param></c:url>"><fmt:message key="created.on"/></a></th>
		<th class="${s4}" width="50px"><a href="<c:url value='/instance/spaces.do'><c:param name='sortBy'>${sortByPageCount}</c:param><c:param name='sortByDesc'>${sd4}</c:param><c:param name='filter'>${filter}</c:param></c:url>"><fmt:message key="pages"/></a></th>

		<th width="50px"> </th>
	</tr>
	<c:forEach var="dto" items="${spaces}" varStatus="status">
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
			<td>${dto.space.unixName}</td> 
			<td>${dto.space.creator.fullname}</td> 
			<td>${dto.createdDate}</td> 
			<td align="center">${dto.totalPages}</td> 

			<td  align="center" class="nowrap"><a href="#" onclick="javascript:showDetail(${dto.space.uid})" id="link-${dto.space.uid}"><fmt:message key="detail"/></a></td> 
		</tr>
		<tr id="row-${dto.space.uid}" style="display:none" class="detailrow">
			<td colspan="5" class="detailrow">
				<div class="detail" id="detail-${dto.space.uid}">
					<img id="loading-${dto.space.uid}" src="${ctxPath}/static/images/large-loading.gif">
				</div>
			</td>
		</tr>
	</c:forEach>
</table>
<div class="pagination">
	<edgenius:page totalPage="${pagination.totalPage}" currentPage="${pagination.currentPage}" sortBy="${sortBy}" sortByDesc="${sortByDesc}"  filter="${filter}" displayPage="10"/>
</div>
<div class="total">
	<fmt:message key="total.spaces"><fmt:param value="${total}"/></fmt:message> 
</div>