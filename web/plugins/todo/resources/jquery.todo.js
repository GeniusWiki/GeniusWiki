//** All methods are able to process multiple todos in same page.

function addItem(todoKey){
	if(jQuery.trim($('#content-'+todoKey).val()) == ''){
		return;
	}
	var data = $('#todoAddForm-'+todoKey).serialize();
	$('#addBtn-'+todoKey).attr("disabled","disabled");
	$("#todo-"+todoKey+" .dvloading").show();
	$.post(saveURL,data,function(response){
		//update whole list
		$('#todo-'+todoKey+' .todoList').html(response);
		
		//clean text box
		$('#content-'+todoKey).val('');
		//enable "add" button again
		$('#addBtn-'+todoKey).removeAttr("disabled");
		
		//display default todo status list, i.e. the new todo item will be there
		todoShow(todoKey, $('#defaultStatus-'+todoKey).val());
		$("#todo-"+todoKey+" .dvloading").hide();
	});
}
function todoShow(todoKey, statusSeq){

	//hide all first
	$("#todo-"+todoKey+" .todoItem").hide();
	$("#todo-"+todoKey+" .todoStatusTag").removeClass("todoCurrStatus");
	
	//show give class
	var line=1;
	$("#todo-"+todoKey+ " .item" + statusSeq).each(function(){
		$(this).removeClass("todoOdd");
		if(line%2 > 0){
			$(this).addClass("todoOdd");
		}
		line++;
		$(this).show();
	});
	
	$("#todoStatusTag"+todoKey + statusSeq).addClass("todoCurrStatus")
	
	$('#selectedStatus-'+todoKey).val(statusSeq);
	
	//update count
	$("#todo-"+todoKey+" .countDiv input").each(function(){
		var tgt = $(".todoStatusCount"+$(this).attr("name"));
		tgt.html($(this).val());
	});

}

function updateStatus(todoKey, todoUid, status){
	$('#todoUpdateForm-'+todoKey +' .todoItemID').val(todoUid);
	$('#todoUpdateForm-'+todoKey +' .todoStatus').val(status);
	
	$("#todo-"+todoKey+" .dvloading").show();
	var data = $('#todoUpdateForm-'+todoKey).serialize();
	$.post(updateURL,data,function(response){
		//error - no update
		if(response == ""){
			alert("Udpate todo status failed. Please refresh page and try again.")
			return;
		}
		
		//TODO: update whole list - may update minor list later
		$('#todo-'+todoKey+' .todoList').html(response);
		//display current selected
		todoShow(todoKey, $('#selectedStatus-'+todoKey).val());
		$("#todo-"+todoKey+" .dvloading").hide();
	});
}

function deleteItem(todoKey, todoUid){
	//TODO: i18n
	if(confirm("Do you want to delete this todo?")){
		$('#todoUpdateForm-'+todoKey +' .todoItemID').val(todoUid);
		var data = $('#todoUpdateForm-'+todoKey).serialize();
		$.post(deleteURL,data,function(response){
			//response maybe empty if no todo item available... so need a good way to send back error.
			
			//TODO: update whole list - may update minor list later
			$('#todo-'+todoKey+' .todoList').html(response);
			//display current selected
			todoShow(todoKey, $('#selectedStatus-'+todoKey).val());
		});
	}
}
function showStatusMarker(visible, todoKey, todoUid){
	
	if(visible){
		$('#todoStatusPanel-'+todoKey+"-"+todoUid).show();
	}else{
		$('#todoStatusPanel-'+todoKey+"-"+todoUid).hide();
	}
	
}

