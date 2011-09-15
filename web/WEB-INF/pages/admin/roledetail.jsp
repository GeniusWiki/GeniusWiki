<%@ include file="/common/taglibs.jsp"%>

<div class="detail">
	<table cellpadding="0" cellspacing="0" border="0" width="100%">
		<tr>
			<td>
				<%@ include file="/common/messages.jsp"%>
			</td>
		</tr>
		<tr>
			<td width="250px">
				<span class="func"><fmt:message key="users"/> (${totalUsers})</span><div class="separator"></div>
			</td>
		</tr>
		<tr>
			<td valign="top">
				<%-- list all users --%>
				<c:forEach var="user" items="${role.users}" varStatus="s">
					<a href="javascript:;" onclick="deleteUsersFromRole(${role.uid}, ${user.uid})">${user.fullname}</a>
					<c:if test="${s.index < (totalUsers -1)}">
						|
					</c:if> 
				</c:forEach>
			</td>
		</tr>
		<tr>
			<td>
				<span class="func"><fmt:message key="actions"/></span><div class="separator"></div>
				<div class="func-area" id="func-area-${role.uid}">
					<a href="javascript:;" onclick="addUserToRole(${role.uid});"><fmt:message key="add.user.to.role"/></a>
				</div>
			</td>
		</tr>
	</table>
</div>