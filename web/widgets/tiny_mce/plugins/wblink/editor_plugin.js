/**
 * @author Dapeng Ni
 * @copyright Copyright 2007-2008, Edgenius, All rights reserved.
 */

(function() {
	// Load plugin specific language pack
	
	tinymce.PluginManager.requireLangPack('wblink');
	
	tinymce.create('tinymce.plugins.WbLink', {

		init : function(ed, url) {
			ed.addCommand('mceWbLink', function() {
				gwtWbLinkDialog(ed.id);
			});

			// IMPORTANT: the last "," must be removed, otherwise, IE won't works.
			ed.addButton('link', {
				title : 'wblink.desc',
				cmd : 'mceWbLink'
			});

		},

		/**
		 * Creates control instances based in the incomming name. This method is normally not
		 * needed since the addButton method of the tinymce.Editor class is a more easy way of adding buttons
		 * but you sometimes need to create more complex controls like listboxes, split buttons etc then this
		 * method can be used to create those.
		 *
		 * @param {String} n Name of the control to create.
		 * @param {tinymce.ControlManager} cm Control manager to use inorder to create new control.
		 * @return {tinymce.ui.Control} New control instance or null if no control was created.
		 */
		createControl : function(n, cm) {
			return null;
		},

		/**
		 * Returns information about the plugin as a name/value array.
		 * The current keys are longname, author, authorurl, infourl and version.
		 *
		 * @return {Object} Name/value array containing information about the plugin.
		 */
		getInfo : function() {
			return {
				longname : 'GeniusWikiLink',
				author : 'Dapeng Ni',
				authorurl : 'http://geniuswiki.com',
				infourl : 'http://geniuswiki.com/',
				version : "1.0"
			};
		}
	});

	// Register plugin
	tinymce.PluginManager.add('wblink', tinymce.plugins.WbLink);
})();