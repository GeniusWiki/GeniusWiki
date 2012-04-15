// Call on every render completed 
function renderCallback(renderPanelID){
	$('a.ajaxlink').bind('click', function() {
		var token = $(this).attr('href');
		History.pushState({token:token, rand:Math.random()}, "Loading", token);
		gwtTokenChange(token);
		return false; 
	}); 

	<%-- only FF (test in 3.5) automatically eval script when inserts script in render panel.
	To avoid duplicated execute same script in FF, it won't run this piece code. 
	If runs duplciated, some script won't work. For example, datepicker, it won't display.
	jQuery.support.scriptEval only return false for IE 
	2012/04/15 - after FF 4, above is not true anymore, so here also check version.
	--%>
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
        	imageBlank: ctxPath + '/widgets/jquery/jquery-lightbox/images/lightbox-blank.gif',
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
	//dp.SyntaxHighlighter.ClipboardSwf = ctxPath+'/widgets/dp_syntaxhighlighter/scripts/clipboard.swf';
	dp.SyntaxHighlighter.HighlightAll('sourcecode');
	//to avoid duplicated render if this method is called over twice...
	var tags = document.getElementsByTagName('pre');
	for(var i = 0; i < tags.length; i++){
		if(tags[i].getAttribute('name') == 'sourcecode'){
			tags[i].setAttribute('name','sourcecode-done');
		}
	}
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
