<c:if test="${not empty error}">
	<div class="message-panel" id="errorDiv"><div class="error" style="width:95%">${error}</div></div>
</c:if>
<c:if test="${not empty warning}">
	<div class="message-panel" id="warnDiv"><div class="warning" style="width:95%">${warning}</div></div>  
</c:if>
<c:if test="${not empty message}">
	<div class="message-panel" id="infoDiv"><div class="info" style="width:95%">${message}</div></div>  
</c:if>
