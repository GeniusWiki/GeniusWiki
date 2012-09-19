// This method is call once after GWT complete initilised. Note The $(document).ready() will trigger before GWT initialised! 
function documentReadyCallback() {

	// Bind sidebar buttons trigger div in /static/scripts/pages.js 
	bindSidebarButtons();
	
}

// Call on every render completed 
function renderCallback(renderPanelID){
	if( !jQuery.browser.mozilla || parseFloat(jQuery.browser.version) >= 2.0){
		$("#"+renderPanelID).find("script").each(function(){
			if ( $(this).attr("src") ) {
				jQuery.ajax({
					url: $(this).attr("src"),
					async: false,
					dataType: "script"
				});
			} else {
				jQuery.globalEval( $(this).html());
			}
		});
	}
	$("#"+renderPanelID +" .macroGallery").each(function(){
        $('#'+this.id + ' a').lightBox({
        	overlayBgColor: '#445',
        	overlayOpacity: 0.6,
        	imageBlank: ctxPath+'/widgets/jquery/jquery-lightbox/images/lightbox-blank.gif',
        	imageLoading: ctxPath+'/widgets/jquery/jquery-lightbox/images/lightbox-ico-loading.gif',
        	imageBtnClose: ctxPath+'/widgets/jquery/jquery-lightbox/images/lightbox-btn-close.gif',
        	imageBtnPrev: ctxPath+'/widgets/jquery/jquery-lightbox/images/lightbox-btn-prev.gif',
        	imageBtnNext: ctxPath+'/widgets/jquery/jquery-lightbox/images/lightbox-btn-next.gif',
        	containerResizeSpeed: 350,
        	txtImage: 'Image'
           })
   	});
   	
   	$("#"+renderPanelID +" a.renderImage").each(function(){
        $(this).lightBox({
        	overlayBgColor: '#445',
        	overlayOpacity: 0.6,
        	imageBlank: ctxPath+'/widgets/jquery/jquery-lightbox/images/lightbox-blank.gif',
        	imageLoading: ctxPath+'/widgets/jquery/jquery-lightbox/images/lightbox-ico-loading.gif',
        	imageBtnClose: ctxPath+'/widgets/jquery/jquery-lightbox/images/lightbox-btn-close.gif',
        	imageBtnPrev: ctxPath+'/widgets/jquery/jquery-lightbox/images/lightbox-btn-prev.gif',
        	imageBtnNext: ctxPath+'/widgets/jquery/jquery-lightbox/images/lightbox-btn-next.gif',
        	containerResizeSpeed: 350,
        	txtImage: 'Image'
           })
   	});

	if( $("#"+renderPanelID +" span.renderError")!=undefined){
		$("#"+renderPanelID +" span.renderError").each(function(){
			$(this).qtip({
				content: {
				text: $(this).attr("hint")
			},
			position: {
				corner: {
				target: 'bottomMiddle',
				tooltip: 'topLeft'
				}
			},
			style: 'red' 
			})
		});
	}
	
	$("#"+renderPanelID +" div.macroPanelExpander").click(function (){
		var id = $(this).attr("name");
		if($(this).hasClass("collapse")){
			$("#"+id).show("slow");
			$(this).removeClass("collapse")
		}else{
			$("#"+id).slideUp();
			$(this).addClass("collapse")
		}
		
	});
	// !! This method can be call multiple times as renderCallback() will be execute on view/preview pannel etc. So far, looks that not harmful except performance 
	SyntaxHighlighter.highlight();
	
}

// Call from GWT by ShellDialog that opened from space admin panel, Shell jsp.  
function themeChanged(){
	var elems = document.getElementsByName('URLPanel');

	for(var i = 0; i < elems.length; i++){
		var cUid = elems[i].contentWindow.document.getElementById("urlPanelChildUid").value;
		if(cUid == 'shellAdmin'){
			elems[i].contentWindow.themeChanged();
		    break;
		}
	}
}
