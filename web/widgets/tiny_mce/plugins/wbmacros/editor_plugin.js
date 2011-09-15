/**
 * @author Dapeng Ni
 * @copyright Copyright 2007-2008, Edgenius, All rights reserved.
 */

(function() {
	// Load plugin specific language pack
	
	tinymce.PluginManager.requireLangPack('wbmacros');
	var list, btnImg;
	tinymce.create('tinymce.plugins.WbMacros', {
		
		init : function(ed, url) {
			btnImg = url+"/img/macro.png";
			 //image : 'some.gif'
			list = new Array();
			
			//quote,pre
			list[0] = {title : 'Information', onclick : function() {
				gwtOpenMessageMacroDialog(ed.id, 'Info');
	        }};
			list[1] = {title : 'Warning', onclick : function() {
				gwtOpenMessageMacroDialog(ed.id, 'Warning');
			}};
			list[2] = {title : 'Error', onclick : function() {
				gwtOpenMessageMacroDialog(ed.id, 'Error');
			}};
			list[3] = {title : 'User', onclick : function() {
				gwtOpenMacroDialog(ed.id, 'user');
			}};
			list[4] = {title : 'HTML', onclick : function() {
				gwtOpenMacroDialog(ed.id, 'HTML');
			}};
			list[5] = {title : 'Gallery', onclick : function() {
				gwtOpenMacroDialog(ed.id, 'gallery');
			}};
			list[6] = {title : 'Attachment list', onclick : function() {
				gwtOpenMacroDialog(ed.id, 'attach');
			}};
			list[7] = {title : 'Table of Contents', onclick : function() {
				gwtOpenMacroDialog(ed.id, 'toc');
			}};
			list[8] = {title : 'Panel', onclick : function() {
				gwtOpenMacroDialog(ed.id, 'panel');
			}};
			//code,logo, include, piece, portal,visible, comment, signup, feedback, saveme,pageinfo,  
		},
		createControl : function(n, cm) {
			if (n == 'wbmacros') {
				var c = cm.createMenuButton('MacrosButton', {
                   title : 'wbmacros.desc',
                   image : btnImg
               });

               c.onRenderMenu.add(function(c, m) {
            		m.add({title : 'wbmacros.title', 'class' : 'mceMenuItemTitle'}).setDisabled(1);
    				
            	   for(var idx=0;idx<list.length;idx++)
            		   m.add(list[idx]);
                   
             });

             return c;
			}
		},

		getInfo : function() {
			return {
				longname : 'GeniusWikiMacros',
				author : 'Dapeng Ni',
				authorurl : 'http://geniuswiki.com',
				infourl : 'http://geniuswiki.com/',
				version : "1.0"
			};
		}
	});

	tinymce.PluginManager.add('wbmacros', tinymce.plugins.WbMacros);
})();