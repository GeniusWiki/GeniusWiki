/**
 * Display context menu and allow delete a non-editable region.
 * @author Dapeng Ni
 * @copyright Copyright 2007-2008, Edgenius, All rights reserved.
 */

(function() {
	tinymce.create('tinymce.plugins.WbRemoveNonEdit', {

		init : function(ed, url) {
			// Register the command so that it can be invoked by using tinyMCE.activeEditor.execCommand('mceExample');
			ed.addCommand('mceWbRemoveNonEdit', function() {
				var nonEditClass = ed.getParam("noneditable_noneditable_class", "mceNonEditable");
				var sc, ec;

				ed.dom.getParent(ed.selection.getStart(), function(n) {
					if(ed.dom.hasClass(n, nonEditClass))
						ed.dom.remove(n, false);
				});

				ed.dom.getParent(ed.selection.getEnd(), function(n) {
					if(ed.dom.hasClass(n, nonEditClass))
						ed.dom.remove(n, false);
				});
			});

			// Register example button
//			ed.addButton('image', {
//				title : 'wbimage.desc',
//				cmd : 'mceWbImage'
//			});

			// Add a node change handler, selects the button in the UI when a image is selected
			/**
			ed.onNodeChange.add(function(ed, cm, n) {
				cm.setActive('example', n.nodeName == 'IMG');
			});
			**/
		},
		getInfo : function() {
			return {
				longname : 'GeniusWikiImage',
				author : 'Dapeng Ni',
				authorurl : 'http://geniuswiki.com',
				infourl : 'http://geniuswiki.com/',
				version : "1.0"
			};
		}
	});

	// Register plugin
	tinymce.PluginManager.add('wbremovenonedit', tinymce.plugins.WbRemoveNonEdit);
})();