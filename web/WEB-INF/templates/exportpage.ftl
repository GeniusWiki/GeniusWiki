	<#-- This ftl shared by print and export -->
	<div class="print-body">
		<#if isHistory>
			<div class="print-history-warn">This is history version. Version number is ${page.version}.</div>
		</#if>
		
		<#if !isHistory>
			<#if page.tagString != "">
				<div class="print-tags">Tags: ${page.tagString?html}</div>
			</#if>
		</#if>
		
		<div class="print-title">${page.title?html}</div>
		<br>
		
		<div class="render-content">
			${text}
		</div>
		
		<br>
	</div>		
	<div class="print-author">${page.authorInfo?html}</div>
	