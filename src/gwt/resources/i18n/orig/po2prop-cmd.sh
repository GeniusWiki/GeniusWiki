#!/bin/sh
#It means there are duplicated properties if you got error :'NoneType' object has no attribute 'isfuzzy' 
po2prop -t ../Messages.properties GeniusWiki_gwt_messages_po_tr_TR.po ../Messages_tr_TR.properties
po2prop -t ../ParamsMessages.properties GeniusWiki_paramsmessagespo_tr_TR.po ../ParamsMessages_tr_TR.properties

po2prop -t ../Messages.properties GeniusWiki_gwt_messages_po_ar.po ../Messages_ar.properties
po2prop -t ../ParamsMessages.properties GeniusWiki_paramsmessages_po_ar.po ../ParamsMessages_ar.properties
