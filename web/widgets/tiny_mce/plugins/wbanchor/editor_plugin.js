/**
 * @author Dapeng Ni
 * @copyright Copyright 2007-2008, Edgenius, All rights reserved.
 */

(function() {
	// Load plugin specific language pack
	
	tinymce.PluginManager.requireLangPack('wbanchor');
	
	tinymce.create('tinymce.plugins.WbAnchor', {

		init : function(ed, url) {
			ed.addCommand('mceWbAnchor', function() {
				gwtWbAnchorDialog(ed.id);
			});

			ed.addButton('anchor', {
				title : 'wbanchor.desc',
				cmd : 'mceWbAnchor'
			});


		},
		createControl : function(n, cm) {
			return null;
		},

		getInfo : function() {
			return {
				longname : 'GeniusWikiAnchor',
				author : 'Dapeng Ni',
				authorurl : 'http://geniuswiki.com',
				infourl : 'http://geniuswiki.com/',
				version : "1.0"
			};
		}
	});

	tinymce.PluginManager.add('wbanchor', tinymce.plugins.WbAnchor);
})();