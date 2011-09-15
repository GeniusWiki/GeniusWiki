function resetURLPanelHeight(){

	var elems = window.top.document.getElementsByName('URLPanel');
	var myUid = document.getElementById("urlPanelChildUid").value;
	for(var i = 0; i < elems.length; i++){
		var le = elems[i].contentWindow.document.getElementById("urlPanelChildUid");
		if(le == undefined)
			continue;
		
		var cUid = le.value;
		if(cUid == myUid){
			//FF2: it won't shrink the size if frame size is maximum, so, shrink iframe height first, then 
			//change according to current page scrollHeight...
			elems[i].style.height="10px";
		    elems[i].style.height=document.body.scrollHeight+'px';
		    break;
		}
	}
}
function changeStyle(oldStyle, newStyle){
	$("link").each(function(){
		var ln = $(this).attr("href");
		if(ln.indexOf("/skins/"+oldStyle+"/") != -1){
			ln = ln.replace("/skins/"+oldStyle+"/","/skins/"+newStyle+"/");
			$(this).attr("href",ln);
		}
	});
}
function userCreated(fullname){
	var myUid = "users";
	var elems = document.getElementsByName('URLPanel');
	
	for(var i = 0; i < elems.length; i++){
		var cUid = elems[i].contentWindow.document.getElementById("urlPanelChildUid").value;
		if(cUid == myUid){
			elems[i].contentWindow.userCreated(fullname);
		    break;
		}
	}
}
function roleCreated(fullname){
	var myUid = "roles";
	var elems = document.getElementsByName('URLPanel');
	
	for(var i = 0; i < elems.length; i++){
		var cUid = elems[i].contentWindow.document.getElementById("urlPanelChildUid").value;
		if(cUid == myUid){
			elems[i].contentWindow.roleCreated(fullname);
		    break;
		}
	}
}
function spaceCreated(fullname){
	var myUid = "spaces";
	var elems = document.getElementsByName('URLPanel');
	
	for(var i = 0; i < elems.length; i++){
		var cUid = elems[i].contentWindow.document.getElementById("urlPanelChildUid").value;
		if(cUid == myUid){
			elems[i].contentWindow.spaceCreated(fullname);
		    break;
		}
	}
}

function userToRoleAdded(fullname){
	var myUid = "roles";
	var elems = document.getElementsByName('URLPanel');
	
	for(var i = 0; i < elems.length; i++){
		var cUid = elems[i].contentWindow.document.getElementById("urlPanelChildUid").value;
		if(cUid == myUid){
			elems[i].contentWindow.userToRoleAdded(fullname);
		    break;
		}
	}
}