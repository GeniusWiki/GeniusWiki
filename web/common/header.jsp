<%@ include file="/common/taglibs.jsp"%>
<table id="header" width="100%" cellpadding="0" cellspacing="2">
	<tr>
		<td><div id="navbar"></div></td>
		<td align="right">
			<table cellpadding="0" cellspacing="0">
				<tr><td><div id="search"></div></td>
				<td nowrap="nowrap"><div id="login"></div></td></tr>
			</table>
		</td>
	</tr>
</table>
<%-- safari browser "location.href" redirect does not work if the call happens inside ajax returned call. 
 use this form replace normal redirect, redir parameter is key for after login jump   --%>
<form id="authForm" name="authForm" method="post">
	<input type="hidden" name="redir" id="jumpURL"/>
</form>
