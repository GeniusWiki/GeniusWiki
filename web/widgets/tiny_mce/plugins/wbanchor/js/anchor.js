/**
refer from theme/advanced/js/anchor.js
**/	
function updateAnchor(aid, name) {
	var ed = tinyMCE.activeEditor;
	
	if (this.action != 'update')
		ed.selection.collapse(1);

	elm = ed.dom.getParent(ed.selection.getNode(), 'A');
	if (elm)
		elm.name = name;
	else
		ed.execCommand('mceInsertContent', 0, ed.dom.createHTML('a', {name : name, 'class' : 'mceItemAnchor'}, ''));
}