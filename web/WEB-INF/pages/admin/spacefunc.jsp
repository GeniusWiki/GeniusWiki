<%@ include file="/common/taglibs.jsp"%>

<c:choose>
	<c:when test="${dto.space.removed}">
		<a href="#" onclick="removeSpace(${dto.space.uid},false)"><fmt:message key="undo.remove"/></a>
	</c:when>
	<c:otherwise>
		<a href="#" onclick="removeSpace(${dto.space.uid},true)"><fmt:message key="remove"/></a>
	</c:otherwise>
</c:choose>
&nbsp;&nbsp;

<c:if test="${dto.globalAdSense}">
	<c:choose>
		<c:when test="${dto.spaceAdSense}">
			<a href="#" onclick="enableSpaceAd(${dto.space.uid},false)"><fmt:message key="disable.space.ad"/></a>
		</c:when>
		<c:otherwise>
			<a href="#" onclick="enableSpaceAd(${dto.space.uid},true)"><fmt:message key="enable.space.ad"/></a>
		</c:otherwise>
	</c:choose>
</c:if>
&nbsp;&nbsp;

<%-- use hidden variable rather than pass spaceUname into JS method to avoid unexpected characters --%>
<input type="hidden" id="sname-${dto.space.uid}" value="<edgenius:encodeToken value='${dto.space.unixName}'/>">

<a href="#" onclick="gotoSpaceHome(${dto.space.uid})"><fmt:message key="goto.space"/></a>
&nbsp;&nbsp;

<a href="#" onclick="gotoSpaceAdmin(${dto.space.uid})"><fmt:message key="goto.space.admin"/></a>

&nbsp;&nbsp;
<c:if test="${dto.space.removed}">
		<div style="color:red"><fmt:message key="remove.warn"><fmt:param value="${dto.delayRemoveHours}"></fmt:param> </fmt:message></div>
</c:if>