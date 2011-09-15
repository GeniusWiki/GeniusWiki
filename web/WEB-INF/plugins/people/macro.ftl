<div aid="renderNonEdit" id="renderEditPeople-${peopleKey}" class="macroPeopleContainer">
<link href="${resourcePath}/people.css" rel="stylesheet" type="text/css" /> 

<script type="text/javascript">
<!--   

-->
</script>

<#if people??>
<#list people as person>
	<div class="person">
		<img src="${person.portrait}" width="150px" >
		<div class="profile">
			<div class="value1">${person.fullname}</div>
			<div class="value2">${person.loginname}</div>
			<div class="actions">
				<div class="action">Send message</div>
				<div class="action">Follow</div>
			</div>
		</div>
	</div>
	<#if (person_index+1)%3==0 || !person_has_next>
		<div style="clear:both"></div>
	</#if>
</#list>
</#if>

</div>
<div class="renderPeople" wajax="${peopleWajax}"> <#-- this div is for render HTML back to markup -->
	<div aid="renderForEdit" id="renderEditPeople-${peopleKey}-editdiv" class="renderMacroMarkup mceNonEditable" style="display:none">
	${peopleMarkup}
	</div>
<div>