
var action, orgTableWidth, orgTableHeight;
/**
 * This method total maintained by Edgenius
 * @return
 */
function insertTable(input) {
	var inst = tinyMCE.activeEditor;
	var dom = inst.dom;
	var cols = 2, rows = 2, border = 0, cellpadding = -1, cellspacing = -1, align, width, height, className, style, caption,hasTitle, cpadding, cspacing;
	var html = '', capEl, thEl, elm;

	elm = dom.getParent(inst.selection.getNode(), 'table');

	// Get form data
	rows = input[0];
	cols = input[1];
	bgcolor =  input[2];
	bordercolor = input[3];
	hasTitle= input[4]=='true'?true:false;
	border = input[5];
	style =  input[6];
	cpadding =  input[7];
	cspacing =  input[8];
	className =  input[9];
	
	//Safari can not display caption correctly, so never display caption...
	caption =  false; //formObj.elements['caption'].checked;
	align="";
	
	// Update table
	if (action == "update") {
		inst.execCommand('mceBeginUndoLevel');

		dom.setAttrib(elm, 'align', align);
		dom.setAttrib(elm, 'class', className);
		dom.setAttrib(elm, 'style', style);
		if(cpadding != "")
			dom.setAttrib(elm, 'cellpadding', cpadding);
		if(cspacing != "")
			dom.setAttrib(elm, 'cellspacing', cspacing);
		
		// Remove these since they are not valid XHTML or they already set in style
		dom.setAttrib(elm, 'borderColor', '');
		dom.setAttrib(elm, 'bgColor', '');
		dom.setAttrib(elm, 'width', '');
		dom.setAttrib(elm, 'height', '');
		dom.setAttrib(elm, 'border', '');
		
		thEl = inst.dom.select('th', elm)[0];
		if (thEl && !hasTitle){
			//title removed - replace all th to td
			$(elm).find("th").each(function(){
				$(this).replaceWith("<td>"+$(this).text()+"</td>");
			});
		}
		
		if (!thEl && hasTitle) {
			//new titled
			$(elm).find("tr:first").children("td").each(function(){
				$(this).replaceWith("<th>"+$(this).text()+"</th>");
			});
		}
		//border 1 is default size, no need fill in
		if (bordercolor != "" || border != "1") {
			//update table,td,th,caption border color as well
			var st = border + "px none " 
			if(border != "0"){
				//if 0, solid must be empty, otherwise td/th won't be set correctly. 
				st = border + "px solid" + " " + bordercolor;
			}else{
				st = border + "px";
			}
			
			$(elm).css("border",st);
			$(elm).find("td,th,caption").each(function(){
				dom.setAttrib($(this), 'style', "border:" + st);
			});
			$(elm).find("td,th,caption").css("border", st);
		} else{
			$(elm).css("border",'');
			$(elm).find("td,th,caption").each(function(){
				dom.setAttrib($(this), 'style', "");
			});
			$(elm).find("td,th,caption").css("border", "");
		}
		$(elm).css("backgroundColor",bgcolor);

		inst.addVisual();

		inst.nodeChanged();
		inst.execCommand('mceEndUndoLevel');

		//this fix a weird bug of FF in MAC - border can not update accordingly.
		inst.setContent(inst.getContent());
		
		
		// Repaint if dimensions changed
		//if (formObj.width.value != orgTableWidth || formObj.height.value != orgTableHeight)
			inst.execCommand('mceRepaint');

		return true;
	}

	// Create new table
	html += '<table';
	html += makeAttrib('align', align);
	html += makeAttrib('class', className);
	html += makeAttrib('style', style);
	html += makeAttrib('_mce_new', '1');
	html += '>';

	//td/th/caption also have border color in class. so, here need overwrite them by style 
	//in table insert, table background won't impact default title color - so there is not background-color set in style
	var cellStyle = bordercolor==""?"":("border-color:"+bordercolor);
	cellStyle = (cellStyle==""?"":cellStyle+";") + (border==""?"":"border-width:"+border);
	cellStyle = cellStyle==""?"":("style='"+cellStyle+"'");
	
	var cellWidth = '';
	if(className=='renderGrid'){
		cellWidth = ' width="'+100/cols+'%" ';
	}
	if (caption) {
		if (!tinymce.isIE)
			html += '<caption '+cellStyle+'><br _mce_bogus="1"/></caption>';
		else
			html += '<caption '+cellStyle+'></caption>';
	}

	for (var y=0; y<rows; y++) {
		html += "<tr>";
		for (var x=0; x<cols; x++) {
			if(hasTitle && y == 0){
				if (!tinymce.isIE)
					html += '<th '+cellStyle+cellWidth+'><br _mce_bogus="1"/></th>';
				else
					html += '<th '+cellStyle+cellWidth+'></th>';
				st = 1;
			}else{
				if (!tinymce.isIE)
					html += '<td '+cellStyle+cellWidth+'><br _mce_bogus="1"/></td>';
				else
					html += '<td '+cellStyle+cellWidth+'></td>';
			}
		}
		html += "</tr>";
	}

	html += "</table>";

	inst.execCommand('mceBeginUndoLevel');
	
	// Move table
	if (inst.settings.fix_table_elements) {
		var patt = '';

		inst.focus();
		inst.selection.setContent('<br class="_mce_marker" />');
		
		tinymce.each('h1,h2,h3,h4,h5,h6,p'.split(','), function(n) {
			if (patt)
				patt += ',';

			patt += n + ' ._mce_marker';
		});

		tinymce.each(inst.dom.select(patt), function(n) {
			inst.dom.split(inst.dom.getParent(n, 'h1,h2,h3,h4,h5,h6,p'), n);
		});

		dom.setOuterHTML(dom.select('br._mce_marker')[0], html);
	} else
		inst.execCommand('mceInsertContent', false, html);
	tinymce.each(dom.select('table[_mce_new]'), function(node) {
		var td = dom.select('td', node);

		try {
			// IE9 might fail to do this selection
			inst.selection.select(td[0], true);
			inst.selection.collapse();
		} catch (ex) {
			// Ignore
		}

		dom.setAttrib(node, '_mce_new', '');
	});
	inst.addVisual();
	inst.execCommand('mceEndUndoLevel');

}
function makeAttrib(attrib, value) {
	if (value == "")
		return "";

	// XML encode it
	value = value.replace(/&/g, '&amp;');
	value = value.replace(/\"/g, '&quot;');
	value = value.replace(/</g, '&lt;');
	value = value.replace(/>/g, '&gt;');

	return ' ' + attrib + '="' + value + '"';
}

//call by GWT method!!!
function getProperties(){
	var bkcolor, bordercolor, cols, rows, hasCaption, hasTitle, width, height,border, clz;
	var inst = tinyMCE.activeEditor, dom = inst.dom;
	var elm = dom.getParent(inst.selection.getNode(), "table");
	
	bordercolor = convertRGBToHex($(elm).css("borderColor"));
	bkcolor = convertRGBToHex($(elm).css("backgroundColor"));
	width = $(elm).css('width');
	height =$(elm).css('height');
	border =$(elm).css('borderWidth');
	clz = $(elm).attr('class');
	
	var rowsAr = elm.rows;
	cols =0;
	for (var i=0; i<rowsAr.length; i++)
		if (rowsAr[i].cells.length > cols)
			cols = rowsAr[i].cells.length;

	rows = rowsAr.length;
	
	//hasCaption = elm.getElementsByTagName('caption').length > 0;
	hasTitle = elm.getElementsByTagName('th').length > 0;
	
	var list = new Array();
	list[0]= rows;
	list[1]= cols;
	list[2]= bkcolor;
	list[3]= bordercolor;
	//convert boolean to String for GWT return JsArrayString type 
	list[4]= hasTitle?"true":"false";
	list[5]= width;
	list[6]= height;
	list[7]= border;
	list[8]= clz;
	return list;
	
}


function convertRGBToHex(col) {
	var re = new RegExp("rgb\\s*\\(\\s*([0-9]+).*,\\s*([0-9]+).*,\\s*([0-9]+).*\\)", "gi");

	if (!col)
		return col;

	var rgb = col.replace(re, "$1,$2,$3").split(',');
	if (rgb.length == 3) {
		r = parseInt(rgb[0]).toString(16);
		g = parseInt(rgb[1]).toString(16);
		b = parseInt(rgb[2]).toString(16);

		r = r.length == 1 ? '0' + r : r;
		g = g.length == 1 ? '0' + g : g;
		b = b.length == 1 ? '0' + b : b;

		return "#" + r + g + b;
	}

	return col;
}

