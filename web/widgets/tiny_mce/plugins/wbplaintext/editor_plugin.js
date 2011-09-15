/**
 *
 * @author Dapeng Ni
 * @copyright Copyright 2007-2008, Edgenius, All rights reserved.
 */

(function() {
	tinymce.PluginManager.requireLangPack('wbplaintext');
	var DOM = tinymce.DOM;
	tinymce.create('tinymce.plugins.WbPlainText', {
		init : function(ed, url) {
			// Register the command so that it can be invoked by using tinyMCE.activeEditor.execCommand('mceExample');
			ed.addCommand('mceWbPlainText', function() {
				if (ed.getParam('fullscreen_is_enabled')) {
					//directly call gwtGotoPlainEditor() will cause error if it is full screen mode. At moment, I can not find a way to auto return
					alert("Please quit from fullscreen mode first.");
					return;
				}

				gwtGotoPlainEditor(ed.id);
			});


		},

		createControl : function(n, cm) {
			var t = this, c, ed = t.editor;
			
			if (n == 'wbplaintext') {
				c = cm.createButton(n, {title : 'wbplaintext.desc',label : 'wbplaintext.label', cmd : 'mceWbPlainText', scope : t});
				//overwrite this method so that even noEditable is active, this button is still enabled.
				c.setDisabled = function(status){
				};
				return c;
			}

		},

		getInfo : function() {
			return {
				longname : 'GeniusWikiPlainText',
				author : 'Dapeng Ni',
				authorurl : 'http://geniuswiki.com',
				infourl : 'http://geniuswiki.com/',
				version : "1.0"
			};
		}
	});

	// Register plugin
	tinymce.PluginManager.add('wbplaintext', tinymce.plugins.WbPlainText);
})();