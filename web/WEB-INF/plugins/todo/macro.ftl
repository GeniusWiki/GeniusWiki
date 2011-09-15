<#-- could be multiple todo macro in same page.
So, here use ${todoKey} as unique key for each todo list.  The reason doesn't use ${todoName} as we assume
name could be some unicode character, such as Chinese words.
Input: todoWajax, readonly, pageUuid, todoName, todoParams,  todoKey(per todo), statuslist(list), todos(list) - last 3 also need by todolist.ftl
--> 
<div class="macroTodo">
	
     <div id="todo-${todoKey}" class="todoPanel">
     	<div class="todoHeader">
     		<label>TODO: </label><span class="todoTitle">${todoName}</span>
     		<div class="todoStatusList">
    			<#list statuslist as ss>
					<#if !ss.deleteAction>
						<div class="btnseparator"></div>
						<div class="todoStatusTag   <#if ss_index==0>todoCurrStatus</#if>" id="todoStatusTag${todoKey}${ss.sequence}">
							<span onclick="todoShow('${todoKey}', '${ss.sequence}')">${ss.text}(<span class="todoStatusCount${todoKey}${ss.sequence}">${ss.itemsCount}</span>)</span>
						</div>
					</#if>
				</#list>
				<div class="btnseparator"></div>
			</div>
			<div class="dvloading" style="display:none">Loading</div>
     	</div>
     	<input type="hidden" name="selectedStatus" id="selectedStatus-${todoKey}">
     	<input type="hidden" name="defaultStatus" id="defaultStatus-${todoKey}" value="${statuslist?first.sequence}">
     	
	   <form id="todoUpdateForm-${todoKey}">
	   		<input type="hidden" name="pageUuid" value="${pageUuid}">
		   	<input type="hidden" name="todoKey" value="${todoKey}">
		   	<input type="hidden" name="todoName" value="${todoName}">
		   	<input type="hidden" name="statuses" value="${statuses}">
		   	<input type="hidden" name="deleteAction" value="${deleteAction}">
		   	<input type="hidden" name="todoItemID" class="todoItemID">
		   	<input type="hidden" name="status" class="todoStatus">
	   </form>
	   <#if !readonly>
		   <div class="todoInput">
			   <form id="todoAddForm-${todoKey}">
			   	   <div style="float:left">
				   		<textarea name="content" id="content-${todoKey}"></textarea>
				   </div>
				   <input type="hidden" name="pageUuid" value="${pageUuid}">
				   <input type="hidden" name="todoKey" value="${todoKey}">
				   <input type="hidden" name="todoName" value="${todoName}">
				   <input type="hidden" name="statuses" value="${statuses}">
				   <input type="hidden" name="deleteAction" value="${deleteAction}">
				   <input type="hidden" name="priority" value="1">
				   <div style="float:left;display:block;">
				   		<input type="button" value="Add" id="addBtn-${todoKey}" onclick="addItem('${todoKey}')">
				   		<br>
				   		<span class="todoHint">Control+Enter to submit</span>
				   </div>
			   </form>
		   </div>
	   </#if>
	   
	   	<div class="todoList">
	   		<#include "todolist.ftl">
		</div>
		
		
	</div>
   
   <#-- below <link>/<script src> must under above div, which is request from IE8 -->
   <link href="${resourcePath}/todo.css" rel="stylesheet" type="text/css" /> 
   <script src="${resourcePath}/jquery.todo.js?v=1.0" type="text/javascript"></script>
      
   <script type="text/javascript">
   		<!--
   		var saveURL = "${contextPath}/ext/todo!saveItem.do";
   		var updateURL = "${contextPath}/ext/todo!status.do";
   		var deleteURL = "${contextPath}/ext/todo!delete.do";
   		
        $(document).ready(function() {
            $("#content-${todoKey}").unbind("blur").bind("blur",function(e){
        		gwtShortcutKeyCapture(true);
            });
            $("#content-${todoKey}").unbind("focus").bind("focus",function(e){
        		gwtShortcutKeyCapture(false);
            });
            <#--This is dirty fix: if user edit then save page, as it has preview and view panel, so same event will be bind 2 times.
            this cause 2 same todo item will be added when doing Ctrl+Enter submit  -->
             $("#content-${todoKey}").unbind("keyup").bind("keyup",function(ev){
	               var type = ev.type;
				   if((ev.ctrlKey || ev.metaKey) && ev.which == 13){
			    		addItem(${todoKey});
				   }
             });
         });

        todoShow('${todoKey}','${statuslist?first.sequence}');
        -->
    </script>      
</div>
