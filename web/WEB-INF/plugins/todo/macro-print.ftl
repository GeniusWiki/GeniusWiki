<div id="renderEditTodo-${todoKey}" class="macroTodoContainer">
	
     <div id="todo-${todoKey}" class="todoPanel">
     	<div class="todoHeader">
     		<label>TODO: </label><span class="todoTitle">${todoName}</span>
     	</div>

	   	<div class="todoList">
		<#if todos?? && todos?has_content>
			<#list todos as todo>
				<div class="todoItem">
				 	<div class="todoSubject">${todo.content!''}</div>
				 	<div class="todoStatus">
				 		${todo.statusObj.text}
				 	</div>
				 </div>
			</#list>
		<#else>
			No TODO item
		</#if>
		</div>
	</div>
   
   <link href="${resourcePath}/todo-print.css" rel="stylesheet" type="text/css" /> 
        
</div>