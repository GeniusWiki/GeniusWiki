<#-- This part only include tab name list. The tab content body will be rendered inside TabMacro. -->
<div name="macroTabNames">
	<link href="${resourcePath}/tabs.css" rel="stylesheet" type="text/css" /> 
	<script src="${resourcePath}/jquery-ui-tabs.min.js?v=1.8.2" type="text/javascript"></script>
	<#if tabMap?? && tabMap?has_content>
		<ul>
			<#list tabMap?keys as key>
			<li><a href="#tab-${key}"><span class="macroTabName">${tabMap[key]}</span></a></li>
			</#list>
		</ul>
	</#if>
	<script type="text/javascript">
	<!--   
	 $(document).ready(function() {
	
		var $tabs = $("#macroTabs-${tabsID}").tabs();
		
		<#if selectTab?? && selectTab gt 0>
			$tabs.tabs({ selected: ${selectTab}});
		</#if>
	 });
		
	-->
	</script>
</div>
