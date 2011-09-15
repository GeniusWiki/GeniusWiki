<div class="macroCalContainer" id="renderEditCal-${calID}">

    <div id="calHeader${calID}" class="calPanel">
         <div class="calHeader">
         	Calendar - ${calName}
         </div>

         <div class="calEventList">
	        <#if events?? && events?has_content>
	        <table cellpadding="3" border="0" width="100%">
			<#list events as event>
				<#if event_has_next><#assign clz="border"><#else><#assign clz=""></#if>
		        <tr class="calEvent">
				 	<td class="calSubject ${clz}">${event.subject!''}</td>
				 		<#if event.allDayEvent>
				 			<td width="160px" class="calAllDay ${clz}" colspan="4">
					 			All day event
						 	</span>
				 		<#else>
				 			<td class="form-label ${clz}">From:</td>
						 	<td class="calDate ${clz}">
						 		${event.start}
						 	</td>
				 			<td class="form-label ${clz}">To:</td>
						 	<td class="calDate ${clz}">
						 		${event.end}
						 	</td>
				 		</#if>
				 </tr>
			</#list>
			</table>
			<#else>
				No event
			</#if>
         </div>
	</div>

     
     <link href="${resourcePath}/cal-print.css" rel="stylesheet" type="text/css" /> 
</div>
