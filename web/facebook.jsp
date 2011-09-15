<%@ include file="/common/taglibs.jsp"%>

<fb:request-form 
	action="?start" method="POST" invite="true" type="GeniusWiki" 
	content="GeniusWiki is the best place on Facebook for sharing knowledge and blogging. It is developed by my friend. Join me on the GeniusWiki network! 
		<fb:req-choice url='http://www.facebook.com/add.php?api_key=<c:out value='${apiKey}' />' label='Check out the GeniusWiki network!' />  ">  <fb:multi-friend-selector showborder="false" actiontext="Invite your friends to the GeniusWiki network." exclude_ids="<c:out value='${appUserFriends}' />" 
	max="20" /> 
</fb:request-form>