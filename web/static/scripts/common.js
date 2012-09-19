function setCookie(value,days){
	var expires = "";
	if (days) {
		var date = new Date();
		date.setTime(date.getTime()+(days*24*60*60*1000));
		expires = "; expires="+date.toGMTString();
	}
		
	document.cookie = 'edgenius-offline='+value+'; PATH=/'+expires;
}

function callLogin(loginUrl,redirUrl){
	var f = document.getElementById('authForm');
	var r = document.getElementById('jumpURL');
	f.action=loginUrl;
	r.value=redirUrl;
	f.submit();
}

// safari browser "location.href" redirect does not work if the call happens inside ajax returned call. 
// use this method replace normal redirect 
function redirForm(redirUrl){
	var f = document.getElementById('authForm');
	f.action=redirUrl;
	f.submit();
}

function wopen(url, name, w, h)
{
  // Fudge factors for window decoration space.
  // In my tests these work well on all platforms & browsers.
  w += 32;
  h += 96;
  wleft = (screen.width - w) / 2;
  wtop = (screen.height - h) / 2;
  // IE5 and other old browsers might allow a window that is
  // partially offscreen or wider than the screen. Fix that.
  // (Newer browsers fix this for us, but let's be thorough.)
  if (wleft < 0) {
    w = screen.width;
    wleft = 0;
  }
  if (wtop < 0) {
    h = screen.height;
    wtop = 0;
  }
  var win = window.open(url, name,
    'width=' + w + ', height=' + h + ', ' +
    'left=' + wleft + ', top=' + wtop + ', ' +
    'location=no, menubar=no, ' +
    'status=no, toolbar=no, scrollbars=no, resizable=no');
  // Just in case width and height are ignored
  win.resizeTo(w, h);
  // Just in case left and top are ignored
  win.moveTo(wleft, wtop);
  win.focus();
  }

function getWindowHeight() {
	  var myHeight = 0;
	  if( typeof( window.innerWidth ) == 'number' ) {
	    //Non-IE
	    myHeight = window.innerHeight;
	  } else if( document.documentElement && ( document.documentElement.clientWidth || document.documentElement.clientHeight ) ) {
	    //IE 6+ in 'standards compliant mode'
	    myHeight = document.documentElement.clientHeight;
	  } else if( document.body && ( document.body.clientWidth || document.body.clientHeight ) ) {
	    //IE 4 compatible
	    myHeight = document.body.clientHeight;
	  }
	  return myHeight;
	}
function getWindowWidth() {
	var myWidth = 0;
	if( typeof( window.innerWidth ) == 'number' ) {
		//Non-IE
		myWidth = window.innerWidth;
	} else if( document.documentElement && ( document.documentElement.clientWidth || document.documentElement.clientHeight ) ) {
		//IE 6+ in 'standards compliant mode'
		myWidth = document.documentElement.clientWidth;
	} else if( document.body && ( document.body.clientWidth || document.body.clientHeight ) ) {
		//IE 4 compatible
		myWidth = document.body.clientWidth;
	}
	return myWidth;
}

function trim(str){
	if(str == null)
		return str;
	return str.replace(/^\s+|\s+$/g, "");
}
