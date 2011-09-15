<%@ include file="/common/taglibs.jsp"%>

<div class="detail">
	<table cellpadding="0" cellspacing="0" border="0" width="100%">
		<tr>
			<td width="250px">
				<span class="func"><fmt:message key="logo"/></span><div class="separator"></div>
			</td>
			<td>
				<span class="func"><fmt:message key="info"/></span><div class="separator"></div>
			</td>
		</tr>
		<tr>
			<td valign="top">
				<img src="${dto.largeLogoUrl}" title="${dto.space.name}"/>
			</td>
			<td valign="top">
				<table cellpadding="0" cellspacing="0" border="0" width="100%">
					<tr>
						<td width="150px" class="form-label"><fmt:message key="space.uname"/></td>
						<td>${dto.space.unixName}</td>
					</tr>
					<tr>
						<td class="form-label"><fmt:message key="title"/></td>
						<td>${dto.space.name}</td>
					</tr>
					<tr>
						<td class="form-label"><fmt:message key="tags"/></td>
						<td>${dto.space.tagString}</td>
					</tr>
					<tr>
						<td class="form-label"><fmt:message key="last.modify.page"/></td>
						<td>${dto.lastUpdatePageTitle}</td> 
					</tr>
					<tr>
						<td class="form-label"><fmt:message key="last.modify.date"/></td>
						<td>${dto.lastUpdatePageModifiedDate}</td> 
					</tr>
					<tr>
						<td class="form-label"><fmt:message key="attach.quota"/></td>
						<td>
							<div id="quotaView-${dto.space.uid}">
								<jsp:include page="spacequota.jsp"/>
							</div>
							<div id="quotaEdit-${dto.space.uid}" style="display:none" >
								<input type="text" value="${dto.quotaNum}" id="quoteValue-${dto.space.uid}" style="width:60px"> M 
								 <a href="javascript:editQuota(${dto.space.uid},false);"><fmt:message key="cancel"/></a>
								 <a href="javascript:saveQuota(${dto.space.uid});"><fmt:message key="done"/></a> 
								 (<fmt:message key="zero.unlimited"/>)
							</div>
						</td> 
					</tr>
					<tr>
						<td class="form-label"><fmt:message key="private.space"/></td>
						<td>${dto.privateSpace}</td> 
					</tr>
					<tr>
						<td class="form-label"><fmt:message key="description"/></td>
						<td>${dto.space.description}</td> 
					</tr>
				</table>
		</td>
		</tr>
		<tr>
			<td colspan="2">
				<span class="func"><fmt:message key="actions"/></span><div class="separator"></div>
				<div class="func-area" id="func-area-${dto.space.uid}">
					<jsp:include page="spacefunc.jsp"/>
				</div>
			</td>
		</tr>
	</table>
</div>