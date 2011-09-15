<#if todos??>
<#list todos as todo>
	 <div class="todoItem item${todo.statusObj.sequence}" style="display:none">
		<#if !readonly>
	 		<input type="checkbox" name="status" onclick="showStatusMarker($(this).attr('checked'),'${todoKey}', '${todo.uid}')">
		</#if>
	 	${todo.content!''}
		<#if !readonly>
		 	<div id="todoStatusPanel-${todoKey}-${todo.uid}" style="display:none">
				<div class="todoStatusMarker">
					<label>Change to: </label>
					<#list statuslist as ss>
						<#if ss.persisted>
							<div class="todoStatusTag">
								<#if ss.deleteAction>
									<a href="javascript:;" onclick="deleteItem('${todoKey}', '${todo.uid}')">${ss.text}</a>
								<#else>
									<a href="javascript:;" onclick="updateStatus('${todoKey}', '${todo.uid}', '${ss.text}')">${ss.text}</a>
								</#if> 
							</div>
						</#if>
					</#list>
				</div>
			</div>
		</#if>
	</div>
</#list>
<#-- Tricky here: every Ajax call(delete, update status, add) will refresh TodoList part,i.e., this FTL, however, I also want to 
update counter in todo title part which is located in macro.ftl. So here put count as hidden element, in todoShow() method, 
it will refresh title counter by these hidden variables.  -->
<div class="countDiv">
	<#list statuslist as ss>
		<#if !ss.deleteAction>
			<input type="hidden" name="${todoKey}${ss.sequence}" value="${ss.itemsCount}">
		</#if> 
	</#list>
</div>
</#if>
