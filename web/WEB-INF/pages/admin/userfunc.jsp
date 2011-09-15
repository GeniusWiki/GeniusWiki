<%@ include file="/common/taglibs.jsp"%>

<c:choose>
	<c:when test="${dto.user.enabled}">
		<a href="#" onclick="enableUser(${dto.user.uid},false)"><fmt:message key="disable"/></a>
	</c:when>
	<c:otherwise>
		<a href="#" onclick="enableUser(${dto.user.uid},true)"><fmt:message key="enable"/></a>
	</c:otherwise>
</c:choose>
&nbsp;&nbsp;

<%-- use hidden variable rather than pass user fullname into JS method to avoid unexpected characters --%>
<input type="hidden" id="sname-${dto.user.uid}" value="<edgenius:encodeToken value='${dto.user.username}'/>">

<a href="#" onclick="goProfile(${dto.user.uid})"><fmt:message key="goto.profile"/></a>
&nbsp;&nbsp;

<%-- as user may link to multiple table except space, comment, page. It also possible link to tag, attachment etc, 
	so I give up to remove user functon 
<c:choose>
	<c:when test="${dto.removable}">
		<a href="#" onclick="">remove</a>
	</c:when>
	<c:otherwise>
		<a href="#" style="color: gray;" title="user have contributed pages/spaces, unable to remove">remove</span>
	</c:otherwise>
</c:choose>
--%>
