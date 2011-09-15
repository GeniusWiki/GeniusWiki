<#-- This part only include tab name list. The tab content body will be rendered inside TabMacro. -->
<div name="macroTabNames">
	<#if tabMap?? && tabMap?has_content>
		<div class="renderMacroMarkup mceNonEditable">
			<span>{tabs}: </span>
			<#list tabMap?keys as key>
				<span styles="margin: 0px 5px 0px 5px">${tabMap[key]}</span>
			</#list>
		</div>
	</#if>
</div>
