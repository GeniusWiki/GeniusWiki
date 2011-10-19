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
package com.edgenius.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.edgenius.core.Constants;
import com.edgenius.core.SecurityValues.OPERATIONS;
import com.edgenius.core.UserSetting;
import com.edgenius.core.repository.FileNode;
import com.thoughtworks.xstream.XStream;

@Entity
@Table(name = Constants.TABLE_PREFIX+"USERS")
public class User implements UserDetails, Cloneable, GrantedAuthority{

	private static final long serialVersionUID = 4796933625094387236L;

	private static final transient Logger log = LoggerFactory.getLogger(User.class);

	public static final int SORT_BY_USERNAME = 1;
	public static final int SORT_BY_CREATED_DATE = 1<<1;
	public static final int SORT_BY_EMAIL = 1<<2;
	public static final int SORT_BY_FULL_NAME = 1<<3;
	
	public static final String ANONYMOUS_USERNAME = "anonymous";
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator="key_seq")
	@SequenceGenerator(name="key_seq", sequenceName=Constants.TABLE_PREFIX+"USERS_SEQ")
	@Column(name="PUID")
	private Integer uid;
	
	@Column(name = "FULL_NAME", nullable=false)
	@Index(name="USER_FULLNAME_INDEX")
	private String fullname;
	
	@Column(name = "USER_NAME", unique=true, nullable=false)
	private String username;

	@Column(name="PASSWORD", nullable=false)
	private String password;
	
	@Transient
	protected String confirmPassword;
	
	@Column(name="TITLE", nullable=false)
	private short title;

	@Embedded
//	@SearchableComponent
	private Contact contact;
	@Embedded
	private Address address;
	
	@ManyToMany(targetEntity = Role.class)
	@JoinTable(name = Constants.TABLE_PREFIX+"USER_ROLE", 
		joinColumns = { @JoinColumn(name = "USER_PUID") },
		inverseJoinColumns={ @JoinColumn(name = "ROLE_PUID") })
	private Set<Role> roles = new HashSet<Role>();

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="CREATED_DATE",nullable=false)
	private Date createdDate;

	@ManyToOne
	@JoinColumn(name="CONFIGURATION_NAME")
	private Configuration configuration;
	
	@ManyToMany(targetEntity = Permission.class, 
			cascade = { CascadeType.PERSIST,CascadeType.MERGE,CascadeType.REFRESH },mappedBy="users")
	private Set<Permission> permissions = new HashSet<Permission>();
	
	@Column(name="ENABLED",nullable=false)
	private boolean enabled;
	@Column(name="ACCOUNT_EXPIRED",nullable=false)
	private boolean accountExpired;
	@Column(name="ACCOUNT_LOCKED",nullable=false)
	private boolean accountLocked;
	@Column(name="CREDENTIALS_EXPIRED",nullable=false)
	private boolean credentialsExpired;
	
	@Column(name="DESCRIPTRION")
	private String descriptrion;
	
	//portrait image NODE UUID
	@Column(name="PORTRAIT_NODE_UUID", nullable=true)
	private String portrait;
	
	//user score
	@Column(name="SCORE")
	private long score;
	
	//future usage: pay user or free user etc.
	@Column(name="POSITION_LEVEL")
	private int level;
	

	@ManyToMany(targetEntity = User.class,cascade = { CascadeType.PERSIST,CascadeType.MERGE,CascadeType.REFRESH }, 
			fetch = FetchType.LAZY)
	@JoinTable(name = Constants.TABLE_PREFIX+"USER_FOLLOW", 
		joinColumns = { @JoinColumn(name = "FOLLOWING_PUID") },
		inverseJoinColumns={ @JoinColumn(name = "FOLLOWER_PUID") })
	private List<User> followings;
	
	@ManyToMany(targetEntity = User.class, cascade = { CascadeType.ALL }, fetch = FetchType.LAZY,mappedBy="followings")
	private List<User> followers;
	
	//**************************************************************
	// DTO object
	@Transient
	private UserSetting setting;
	@Transient
	private List<FileNode> attachments;

	//it should be <WikiOPERATIONS>,  but using <OPERATIONS> instead in order to unbind with wiki jar package. sucks
	//This OPERATIONS only contains WikiOPERATIONS.INSTANCE_* part  
	@Transient
	private List<OPERATIONS> wikiPermissions;
	
	//********************************************************************
	//               function method
	//********************************************************************
	/**
	 * @return
	 */
	public User shadowClone() {
		User cUser = null;
		try {
			cUser = (User) super.clone();
		} catch (CloneNotSupportedException e) {
			log.error("Clone "+this.getClass().getName()+" object failed " , e);
		}
		return cUser;
	}
	
	/**
	 * Deep clone user object
	 */
	public Object clone(){
		User cUser = null;
		try {
			cUser = (User) super.clone();
			//deep clone
			if(permissions != null){
				Set<Permission> pSet = new HashSet<Permission>();
				for (Permission permission : permissions) {
					pSet.add((Permission) permission.clone());
				}
				cUser.setPermissions(pSet);
			}
			if(roles != null){
				Set<Role> rSet = new HashSet<Role>();
				for (Role role : roles) {
					rSet.add((Role) role.clone());
				}
				cUser.setRoles(rSet);
			}
			
			//don't clone following info
			cUser.setFollowers(null);
			cUser.setFollowings(null);
		} catch (CloneNotSupportedException e) {
			log.error("Clone "+this.getClass().getName()+" object failed " , e);
		}
		return cUser;
	}
	public boolean equals(Object obj){
		if(!(obj instanceof User)){
			return false;
		}
		return StringUtils.equalsIgnoreCase(((User)obj).username,username);
	}
	
	public int hashCode(){
		return username != null?username.hashCode():super.hashCode();
	}
	public String toString(){
		return new StringBuilder("User name:").append(username).append(". Fullname:").append(fullname).append(". Email:")
			.append(contact != null?contact.getEmail():"").append(". Role:").append(ArrayUtils.toString(roles,"NULL"))
			.toString();
			  
	}
	//JDK1.6 @Override
	public int compareTo(Object obj) {
		if(!(obj instanceof User)){
			return 1;
		}
		return ((User)obj).getUsername().compareTo(this.getUsername());
	}
	//****************************************************************************
	//Get & Set 
	//****************************************************************************
	public Integer getUid() {
		return uid;
	}

	public void setUid(Integer uid) {
		this.uid = uid;
	}


	public String getFullname() {
		return this.fullname;
	}

	public void setFullname(String name) {
		this.fullname = name;
	}


	public Address getAddress() {
		return this.address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}


	public String getUsername() {
		return username;
	}

	public void setUsername(String loginid) {
		this.username = loginid;
	}

	
	public String getPassword() {
		return password;
	}

	public void setPassword(String passwd) {
		this.password = passwd;
	}

	public List<FileNode> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<FileNode> attachments) {
		this.attachments = attachments;
	}

	public Contact getContact() {
		return contact;
	}

	public void setContact(Contact contacts) {
		this.contact = contacts;
	}

	public short getTitle() {
		return title;
	}

	public void setTitle(short title) {
		this.title = title;
	}


	public Set<Role> getRoles() {
		return roles;
	}

	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Configuration setting) {
		this.configuration = setting;
	}

	public UserSetting getSetting() {
		//singleton to improve performance: note the user cache need reset once user change setting.
		if(setting != null)
			return setting;
		
		if(configuration != null && configuration.getValue() != null){
			XStream xstream = new XStream();
			setting = (UserSetting) xstream.fromXML(configuration.getValue());
		}
		if(setting == null){
			log.warn("User " + getUsername() + " does not have personal setting, using default one instead.");
			setting = new UserSetting();
		}
			
		return setting;
	}
	@Override
	public  Collection<? extends GrantedAuthority> getAuthorities() {
		//for single user and its roles authority
		List<GrantedAuthority> auths = new ArrayList<GrantedAuthority>(roles.size() +1); 
		auths.addAll(roles);
		auths.add(this);
		
		return auths;
	}


	public Set<Permission> getPermissions() {
		return permissions;
	}

	public void setPermissions(Set<Permission> permissions) {
		this.permissions = permissions;
	}

	public String getConfirmPassword() {
		return confirmPassword;
	}

	public void setConfirmPassword(String confirmPassword) {
		this.confirmPassword = confirmPassword;
	}

	public boolean isAccountExpired() {
		return accountExpired;
	}

	public void setAccountExpired(boolean accountExpired) {
		this.accountExpired = accountExpired;
	}

	public boolean isAccountLocked() {
		return accountLocked;
	}

	public void setAccountLocked(boolean accountLocked) {
		this.accountLocked = accountLocked;
	}

	public boolean isCredentialsExpired() {
		return credentialsExpired;
	}

	public void setCredentialsExpired(boolean credentialsExpired) {
		this.credentialsExpired = credentialsExpired;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/* (non-Javadoc)
	 * @see org.springframework.security.userdetails.UserDetails#isAccountNonExpired()
	 */
	public boolean isAccountNonExpired() {
		
		return !accountExpired;
	}

	/* (non-Javadoc)
	 * @see org.springframework.security.userdetails.UserDetails#isAccountNonLocked()
	 */
	public boolean isAccountNonLocked() {
		return !accountLocked;
	}

	/* (non-Javadoc)
	 * @see org.springframework.security.userdetails.UserDetails#isCredentialsNonExpired()
	 */
	public boolean isCredentialsNonExpired() {
		return !credentialsExpired;
	}
	/**
	 * @return
	 */
	public boolean isAnonymous() {
		if(uid == -1)
			return true;
		else
			return false;
	}
	public List<OPERATIONS> getWikiPermissions() {
		return wikiPermissions;
	}
	public void setWikiPermissions(List<OPERATIONS> wikiPermissions) {
		this.wikiPermissions = wikiPermissions;
	}
	
	public String getAuthority() {
		return Role.USER_PREFIX + username;
	}
	public void setSetting(UserSetting setting) {
		this.setting = setting;
	}

	public String getDescriptrion() {
		return descriptrion;
	}

	public void setDescriptrion(String descriptrion) {
		this.descriptrion = descriptrion;
	}

	public String getPortrait() {
		return portrait;
	}

	public void setPortrait(String portrait) {
		this.portrait = portrait;
	}

	public long getScore() {
		return score;
	}

	public void setScore(long score) {
		this.score = score;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public List<User> getFollowings() {
		return followings;
	}

	public void setFollowings(List<User> followings) {
		this.followings = followings;
	}

	public List<User> getFollowers() {
		return followers;
	}

	public void setFollowers(List<User> followers) {
		this.followers = followers;
	}

}
