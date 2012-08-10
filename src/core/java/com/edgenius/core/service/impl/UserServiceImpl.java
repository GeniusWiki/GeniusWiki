/* 
 * =============================================================
 * Copyright (C) 2007-2011 Edgenius (http://www.edgenius.com)
 * =============================================================
 * License Information: http://www.edgenius.com/licensing/edgenius/2.0/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2.0
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *
 * http://www.gnu.org/licenses/gpl.txt
 *  
 * ****************************************************************
 */
package com.edgenius.core.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import net.sf.ehcache.Element;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.edgenius.core.Global;
import com.edgenius.core.Version;
import com.edgenius.core.model.User;
import com.edgenius.core.repository.FileNode;
import com.edgenius.core.repository.ITicket;
import com.edgenius.core.repository.RepositoryException;
import com.edgenius.core.repository.RepositoryQuotaException;
import com.edgenius.core.repository.RepositoryService;
import com.edgenius.core.repository.RepositoryTiemoutExcetpion;
import com.edgenius.core.service.UserExistsException;
import com.edgenius.core.service.UserOverLimitedException;
import com.edgenius.core.service.UserReadingService;
import com.edgenius.core.service.UserRemoveException;
import com.edgenius.core.service.UserService;
import com.edgenius.core.util.CodecUtil;
import com.edgenius.core.util.WebUtil;
import com.edgenius.wiki.WikiConstants;
/**
 * @author Dapeng.Ni
 */
@Service(UserService.SERVICE_NAME)
@Transactional
public class UserServiceImpl extends AbstractUserService implements UserService {
	
	private RepositoryService repositoryService;
	private MailMQProducer mailService;
	private UserReadingService userReadingService;
	
	@Transactional(readOnly= false, propagation = Propagation.REQUIRED,rollbackFor={UserExistsException.class})
	public User saveUser(User user) throws UserExistsException, UserOverLimitedException{
        try {
        	if(Version.LEFT_USERS == 0){
        		//Don't mark current license to invalid - to make system still working. Otherwise, it will redirect all
        		//request to invalid license page.
        		//Version.LICENSE_STATUS = 1;
        		throw new UserOverLimitedException();
        	}
        	//system default value, any user can not give default name "anonymous"
        	if(User.ANONYMOUS_USERNAME.equalsIgnoreCase(user.getUsername()))
        		throw new DataIntegrityViolationException("Can not register as anonymous user");
        			
    		userDAO.saveUser(user);
    		log.info("User created:" + user);
    		putUserToCache(user);
        } catch (DataIntegrityViolationException e) {
            throw new UserExistsException("Please check if user/email ["+user.getUsername()+"/"+user.getContact().getEmail()
            		+"] is already existed.", e);
        }
        if(Version.LEFT_USERS != -1){
	        Version.LEFT_USERS--;
	        //failure tolerance: to avoid LEFT_USERS==-1 accidently, which means unlimited!
	        Version.LEFT_USERS = Version.LEFT_USERS < 0? 0: Version.LEFT_USERS;
	        
	        if(Version.LEFT_USERS == 0){
        		SimpleMailMessage msg = new SimpleMailMessage();
        		msg.setFrom(Global.DefaultNotifyMail);
        		Set<String> bcc = userReadingService.getSystemAdminMailList();
        		if(bcc != null && bcc.size() > 0){
					msg.setBcc(bcc.toArray(new String[bcc.size()]));
					HashMap<String, String> model = new HashMap<String, String>();
					model.put("weburl", WebUtil.getHostAppURL());
					mailService.sendPlainMail(msg,  WikiConstants.MAIL_TEMPL_USER_VOLUME_EXCEED, model);
        		}
	        }
        }
        return user;

	}
	public User removeUser(Integer uid) throws UserRemoveException {
		if(uid == null || uid == 0 || uid== -1)
			return getAnonymousUser();
		
		User user = userDAO.get(uid);
		try{
			userDAO.removeObject(user);
			userCache.remove(user.getUsername());
			
		}catch(Exception e){
			throw new UserRemoveException(e);
		}
		if(Version.LEFT_USERS != -1){
			Version.LEFT_USERS++;
		}
		return user; 
	}
	
	@Transactional(readOnly= false, propagation = Propagation.REQUIRED)
	public User updateUser(User user) {
		
		log.info("User updated {}", user);
		
		userDAO.saveOrUpdate(user);
		putUserToCache(user);
		
		return user;
	}
	//JDK1.6 @Override
	@Transactional(readOnly= false, propagation = Propagation.REQUIRED)
	public void resetPassword(User user, String password) {
		if(Global.EncryptPassword){
            String algorithm = Global.PasswordEncodingAlgorithm;
    
            if (algorithm == null) { 
                algorithm = "MD5";
            }
            user.setPassword(CodecUtil.encodePassword(password, algorithm));
        }else
        	user.setPassword(password);
		
		userDAO.saveOrUpdate(user);
	}
	
	@Transactional(readOnly= false, propagation = Propagation.REQUIRED)
	public User interalUpdateUser(User user) {
		userDAO.saveOrUpdate(user);
		putUserToCache(user);
		
		return user;
	}
	@Transactional(readOnly= false, propagation = Propagation.REQUIRED)
	public User updateUserWithIndex(User user) {
		userDAO.saveOrUpdate(user);
		putUserToCache(user);
		
		return user;
	}

	public void uploadPortrait(User my, FileNode portrait) {
		try {
			ITicket ticket = repositoryService.login(RepositoryService.DEFAULT_SPACE_NAME, 
					RepositoryService.DEFAULT_SPACE_NAME, RepositoryService.DEFAULT_SPACE_NAME);
			
			if(repositoryService.hasIdentifierNode(ticket, RepositoryService.TYPE_PORTRAIT, my.getUsername())){
				List<FileNode> items = repositoryService.getAllIdentifierNodes(ticket, RepositoryService.TYPE_PORTRAIT, my.getUsername(), false);
				if(items != null && items.size() > 0){
					//update: delete first
					for (FileNode fileNode : items) {
						repositoryService.removeFile(ticket, fileNode.getNodeUuid(), null);
					}
				}
			}else{
				//add
				repositoryService.createIdentifier(ticket, RepositoryService.TYPE_PORTRAIT, my.getUsername());
			}
			
			//Username is node identifier, which should be set outside this method, here just for ensure this
			portrait.setIdentifier(my.getUsername());
			repositoryService.saveFile(ticket, portrait, false, false);
			
			//update user portrait
			my.setPortrait(portrait.getNodeUuid());
			updateUser(my);
		} catch (RepositoryException e) {
			log.error("Repository error " , e);
		} catch (RepositoryTiemoutExcetpion e) {
			log.error("Repository error " , e);		
		} catch (RepositoryQuotaException e) {
			log.error("Repository error " , e);		
		}
		
	}
	public void removeUserFromCache(User user) {
		
		Element element = null;
		if(user == null){
			//all user must reset, here simple clean user cache, so that next time user could read from database.
			userCache.removeAll();
		}else{
			userCache.remove(user.getUsername());
		}
	}
	public void follow(User myself, User follow) {
		//don't follow self, anonymous.  Anonymous can not do follow as well.
		if(follow == null || follow.isAnonymous() || follow.equals(myself) || myself == null || myself.isAnonymous())
			return;
		
		//to ensure the user is not cloned object
		myself = reload(myself);
		follow = reload(follow);
		
		List<User> followings =  myself.getFollowings();
		if(followings == null){
			followings = new ArrayList<User>();
			myself.setFollowings(followings);
		}
		
		if(!followings.contains(follow)){
			//bi-direction 
			List<User> followers = follow.getFollowers();
			if(followings != null){
				followers = new ArrayList<User>();
				follow.setFollowers(followers);
			}
			followers.add(myself);
			followings.add(follow);
			userDAO.saveOrUpdate(myself);
		}
	}
	public void unfollow(User myself, User follow) {
		//to ensure the user is not cloned object
		myself = reload(myself);
		follow = reload(follow);

		
		List<User> followings =  myself.getFollowings();
		if(followings != null && follow != null && followings.remove(follow)){
			//bi-direction update
			List<User> followers = follow.getFollowers();
			if(followers != null){
				followers.remove(myself);
			}
			userDAO.saveOrUpdate(myself);
		}
		
	}
	
	/** This method is removed as 5/3/2008 desc on development diary. But keep it here as reference to explain how to saving user portrait...
	public String getUserPortraitNodeUuid(String username){
		try {
			ITicket ticket = repositoryService.login(RepositoryService.DEFAULT_SPACE_NAME, 
					RepositoryService.DEFAULT_SPACE_NAME, RepositoryService.DEFAULT_SPACE_NAME);
			if(repositoryService.hasIdentifierNode(ticket, RepositoryService.TYPE_PORTRAIT,username)){
				List<FileNode> items = repositoryService.getAllIdentifierNodes(ticket, RepositoryService.TYPE_PORTRAIT, username, false);
				if(items != null && items.size() > 0){
					if(items.size() > 1)
						AuditLogger.error("Unexpected case, user " + username + " has more than one portrait. The portrait number is " + items.size());
					
					//get first filenode as portrait
					FileNode fileNode = items.get(0);
					return fileNode.getNodeUuid();
				}
			}
		} catch (RepositoryException e) {
			log.error("User portrait retrieve failed: ", e);
		}
		return null;
	}
	 *  **/
	

	//********************************************************************
	//               Set / Get
	//********************************************************************
	public void setRepositoryService(RepositoryService repositoryService) {
		this.repositoryService = repositoryService;
	}

	public void setUserReadingService(UserReadingService userReadingService) {
		this.userReadingService = userReadingService;
	}
	public void setMailService(MailMQProducer mailService) {
		this.mailService = mailService;
	}
	

}
