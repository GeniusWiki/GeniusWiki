var DEFAULT_CONTENT_MARGIN = 25;
var DEFAULT_LEFT_SIDEBAR_SPACING = 5;
var DEFAULT_RIGHT_SIDEBAR_SPACING = 5+5;
var SIDE_MENU_WIDTH = 225;
var DEFAULT_MENU_TOP = 38 + 40;
var LENGHT_MIN_SIDE_BTN = '20px';
var LENGHT_MAX_SIDE_BTN = '45px';

// This method is call once after GWT complete initialized. Note The $(document).ready() will trigger before GWT initialized! 
function documentReadyCallback() {
	bindSidebarButtons();

	History.Adapter.bind(window,'statechange',function(){ 
		var state = History.getState(); 
		alert("state change:" + state.url);
	});
}

function bindSidebarButtons(){
	$('div.left-sidebar-btn').click(function(){
		toggleSidebar(0);
	});
	$('div.right-sidebar-btn').click(function(){
		toggleSidebar(1);
	});
	
	$('div.left-sidebar-btn').hover(function(){
		$(this).animate({opacity: 1,width: LENGHT_MAX_SIDE_BTN},100);
	}, function(){
		$(this).animate({opacity: 0.25,width: LENGHT_MIN_SIDE_BTN},100);
	});
	$('div.right-sidebar-btn').hover(function(){
		$(this).animate({opacity: 1,width: LENGHT_MAX_SIDE_BTN},100);
	}, function(){
		$(this).animate({opacity: 0.25,width: LENGHT_MIN_SIDE_BTN},100);
	});
}

function showSidebarButton(left,visible){
	if(left == 0){
		if(visible)
			$('div.left-sidebar-btn').show();
		else
			$('div.left-sidebar-btn').hide();
	}else{
		if(visible)
			$('div.right-sidebar-btn').show();
		else
			$('div.right-sidebar-btn').hide();
	}

}
/** Call by GWT **/
function toggleSidebar(left){
	var visible;
	var panelID;
	if(left == 0){
		visible = !$('#leftmenu').is(':visible');
		//8 - SharedConstants.TAB_TYPE_LEFT_SIDEBAR
		panelID = 8;
	}else{
		visible = !$('#rightmenu').is(':visible');
		//16 - SharedConstants.TAB_TYPE_RIGHT_SIDEBAR
		panelID = 16;
	}
	
	showSidebar(left, visible);
	
	//tell gwt - this may trigger server save side bar toggle status.
	gwtNotifyPinPanelStatus(panelID,visible);
	
	return visible;
}

function showSidebar(left, visible){
	
	if(left == 0){
		//left
		if(visible){
			//show
			$('#content').css('margin-left',(SIDE_MENU_WIDTH+DEFAULT_LEFT_SIDEBAR_SPACING)+'px');
			$('#leftmenu').css('width',SIDE_MENU_WIDTH+'px');
			$('#leftmenu').show();
			$('div.left-sidebar').show();
		}else{
			//hide
			$('#content').css('margin-left',DEFAULT_CONTENT_MARGIN +'px');
			$('#leftmenu').css('width','0px');
			$('#leftmenu').hide();
			$('div.left-sidebar').hide();
		}
	}else{
		//right
		if(visible){
			//show
			$('#content').css('margin-right',(SIDE_MENU_WIDTH+DEFAULT_RIGHT_SIDEBAR_SPACING)+'px');
			$('#rightmenu').css('width',SIDE_MENU_WIDTH+'px');
			$('#rightmenu').show();
			$('div.right-sidebar').show();
		}else{
			//hide
			$('#content').css('margin-right',DEFAULT_CONTENT_MARGIN +'px');
			$('#rightmenu').css('width','0px');
			$('#rightmenu').hide();
			$('div.right-sidebar').hide();
		}
	}

}