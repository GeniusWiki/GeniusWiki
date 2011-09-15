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
package com.edgenius.wiki.security.strategy;

import static com.edgenius.core.SecurityValues.PAGE_WIKIOPER_SIZE;
import static com.edgenius.wiki.security.strategy.InstancePatternFactory.DEFAULT_URL_POLICIES;
import static com.edgenius.wiki.security.strategy.InstancePatternFactory.FIX_PERMISSION_URL_POLICIES;
import static com.edgenius.wiki.security.strategy.InstancePatternFactory.I_ADMIN_METHOD_POLICIES;
import static com.edgenius.wiki.security.strategy.InstancePatternFactory.I_ADMIN_URL_POLICIES;
import static com.edgenius.wiki.security.strategy.InstancePatternFactory.I_READ_METHOD_POLICIES;
import static com.edgenius.wiki.security.strategy.InstancePatternFactory.I_READ_URL_POLICIES;
import static com.edgenius.wiki.security.strategy.InstancePatternFactory.I_WRITE_METHOD_POLICIES;
import static com.edgenius.wiki.security.strategy.PagePatternFactory.P_COMMENT_READ_METHOD_PATTERNS;
import static com.edgenius.wiki.security.strategy.PagePatternFactory.P_COMMENT_WRITE_METHOD_PATTERNS;
import static com.edgenius.wiki.security.strategy.PagePatternFactory.P_READ_METHOD_PATTERNS;
import static com.edgenius.wiki.security.strategy.PagePatternFactory.P_REMOVE_METHOD_PATTERNS;
import static com.edgenius.wiki.security.strategy.PagePatternFactory.P_WRITE_METHOD_PATTERNS;
import static com.edgenius.wiki.security.strategy.SpacePatternFactory.S_COMMENT_READ_METHOD_PATTERNS;
import static com.edgenius.wiki.security.strategy.SpacePatternFactory.S_COMMENT_WRITE_METHOD_PATTERNS;
import static com.edgenius.wiki.security.strategy.SpacePatternFactory.S_EXPORT_METHOD_PATTERNS;
import static com.edgenius.wiki.security.strategy.SpacePatternFactory.S_OFFLINE_METHOD_PATTERNS;
import static com.edgenius.wiki.security.strategy.SpacePatternFactory.S_READ_PAGE_METHOD_PATTERNS;
import static com.edgenius.wiki.security.strategy.SpacePatternFactory.S_READ_URL_PATTERNS;
import static com.edgenius.wiki.security.strategy.SpacePatternFactory.S_REMOVE_PAGE_METHOD_PATTERNS;
import static com.edgenius.wiki.security.strategy.SpacePatternFactory.S_RESTRICT_PAGE_METHOD_PATTERNS;
import static com.edgenius.wiki.security.strategy.SpacePatternFactory.S_SPACE_ADMIN_METHDO_PATTERNS;
import static com.edgenius.wiki.security.strategy.SpacePatternFactory.S_WRITE_PAGE_METHOD_PATTERNS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.ConfigAttribute;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import com.edgenius.core.SecurityValues.OPERATIONS;
import com.edgenius.core.SecurityValues.RESOURCE_TYPES;
import com.edgenius.core.dao.ResourceDAO;
import com.edgenius.core.model.Resource;
import com.edgenius.core.model.Role;
import com.edgenius.core.model.User;
import com.edgenius.core.service.UserReadingService;
import com.edgenius.core.util.AuditLogger;
import com.edgenius.wiki.WikiConstants;
import com.edgenius.wiki.dao.PageDAO;
import com.edgenius.wiki.dao.SpaceDAO;
import com.edgenius.wiki.model.Page;
import com.edgenius.wiki.security.MethodValueProvider;
import com.edgenius.wiki.security.Policy;
import com.edgenius.wiki.security.WikiSecurityValues.WikiOPERATIONS;
import com.edgenius.wiki.security.acegi.URLValueProvider;
import com.edgenius.wiki.security.service.PolicyCache;



/**
 * @author Dapeng.Ni
 */
public class PatternStrategy implements InitializingBean{

	public static final int BEFORE_METHOD = 1;
	public static final int AFTER_METHOD = 2;
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	//               fields 
	private static final Logger log = LoggerFactory.getLogger(PatternStrategy.class);

	private ResourceDAO resourceDAO;
	private PageDAO pageDAO;
	private SpaceDAO spaceDAO;
	private UserReadingService userReadingService;
	private PatternFactoryFactory patternFactoryFactory; 
	private Map<String,WikiOPERATIONS> methodPatternMap;
	private Map<String,WikiOPERATIONS> urlPatternMap;
	private List<MethodValueProvider> methodValueProviderList;
	private List<URLValueProvider> urlValueProviderList;
	
	private final PathMatcher pathMatcher = new AntPathMatcher();
	
	//Space level / Widget, policy cache
	private PolicyCache policyCache;
	//********************************************************************
	//               Function methods
	//********************************************************************

	public List<Policy> getPolicies(RESOURCE_TYPES type, String ...args){
		List<Policy> policies = null;
		if(type == RESOURCE_TYPES.SPACE){
			if(args.length < 1)
				throw new SecurityException("Failed get space level policy without enough parameters : " + Arrays.toString(args));
			//need spaceUname
			policies = getSpacePolicies(args[0]);
		}else if(type == RESOURCE_TYPES.PAGE){
			if(args.length < 2)
				throw new SecurityException("Failed get page level policy without enough parameters : " + Arrays.toString(args));
			//need spaceUname, pageUuid
			policies = getPagePolicies(args[0],args[1]);
		}else if(type == RESOURCE_TYPES.INSTANCE){
			policies = getInstancePolicies();
		}else if(type == RESOURCE_TYPES.WIDGET){
			policies = getWidgetPolicies(args[0]);
		}
		
		return policies;
	}
	/**
	 * Reset cache according to given RESOURCE_TYPES and resourceName. Actually, it is just simple remove correspoding 
	 * Element from cache and cache will be initialised in next call.
	 * @param type
	 * @param resourceName
	 */
	public void resetCache(RESOURCE_TYPES type, String resourceName) {
		if(type == RESOURCE_TYPES.INSTANCE){
			//need remove all element in cache because instance setting could broadcast to all spaces, pages.
			policyCache.removeAll();
		}else if(type == RESOURCE_TYPES.WIDGET){
			policyCache.remove(resourceName);
		}else if(type == RESOURCE_TYPES.SPACE){
			policyCache.remove(resourceName);
		}else if(type == RESOURCE_TYPES.PAGE){
			//need reset its space level cache
			//just assume draft and trashed page do not have chance to call this method
			Page page = pageDAO.getCurrentByUuid(resourceName);
			if(page != null){
				policyCache.remove(page.getSpace().getUnixName());
			}
		}
		
	}
	
	/**
	 * Find runtime method pattern, which has inherent relationship.
	 */
	public WikiOPERATIONS findURLRuntimePattern(String source){

		WikiOPERATIONS wo = null;
		List<String> patterns = new ArrayList<String>(urlPatternMap.keySet());
		Collections.sort(patterns);
		Collections.reverse(patterns);
		for (String pattern : patterns) {
			if(pathMatcher.match(pattern, source)){
				wo = urlPatternMap.get(pattern);
				//for system default URL pattern, it return null, such as login.do
				if(wo != null){
					//fill value: the value could be spaceUname or PageUuid, depends on the resource type 
					Map<RESOURCE_TYPES, String> values = null;
					if(urlValueProviderList != null){
						for (URLValueProvider provider : urlValueProviderList) {
							if(provider.isSupport(wo)){
								 values = provider.getParameters(source, pattern);
							}
							
						}
					}
					if(values == null){
						if(wo.type == RESOURCE_TYPES.INSTANCE){
							//it maybe Instance scope validation
							values = new HashMap<RESOURCE_TYPES, String>();
						}else{
							//return null:
							//don't do validation anymore, no value(spaceUname or pageUuid) for space or page level security check 
							return null;
						}
					}
					//always put instance to value map
					values.put(RESOURCE_TYPES.INSTANCE, WikiConstants.CONST_INSTANCE_RESOURCE_NAME);
					wo.values = values;
				}
				break;
			}
		}
		return wo;
		
	}

	/**
	 * @param string
	 * @param beforeAfter 
	 * @param args 
	 * @param method 
	 * @return
	 */
	public WikiOPERATIONS findMethodRuntimePattern(String clz, String method, Object[] args, int beforeAfter) {
		Map<RESOURCE_TYPES,String> values = null;
		if(methodValueProviderList != null){
			for (MethodValueProvider provider : methodValueProviderList) {
				if(provider.isSupport(clz)){
					//assume for one class/method only has one provider support, so once finish valid, just return, don't continue.
					if(beforeAfter == BEFORE_METHOD)
						values = provider.getFromInput(method, args);
					else
						values = provider.getFromOutput(method, args[0]);
					break;
				}
			}
		}
		WikiOPERATIONS oper = methodPatternMap.get(clz+"."+method);
		//maybe WikiOPERATIONS is null, since this method does not has corresponding WikiOPERATIONS. for this case,
		//just simply return, it means return null. then ask MethodInterceptor to return null
		//configureureAttribute, then cancel authorisation.
		if(oper == null)
			return null;
		
		if(values == null){
			if(oper.type == RESOURCE_TYPES.INSTANCE){
				//it is Instance scope validation, then go on 
				values = new HashMap<RESOURCE_TYPES, String>();
			}else{
				return null;
			}
		}
		
		//always put instance to value map
		values.put(RESOURCE_TYPES.INSTANCE, WikiConstants.CONST_INSTANCE_RESOURCE_NAME);
		oper.values = values;
		return oper;
	}
	public void afterPropertiesSet() throws Exception {
		urlPatternMap =  new HashMap<String, WikiOPERATIONS>();
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// instance scope  URL
		for(String pattern: DEFAULT_URL_POLICIES){
			if(urlPatternMap.get(pattern) != null){
				log.warn("Duplicated pattern instance of " + pattern);
			}
			//put value of WikiOPERATIONS as null, so that system can skip authentication.
			urlPatternMap.put(pattern,null);
		}
		for(String pattern: FIX_PERMISSION_URL_POLICIES){
			if(urlPatternMap.get(pattern) != null){
				log.warn("Duplicated pattern instance of " + pattern);
			}
			urlPatternMap.put(pattern,WikiOPERATIONS.INSTANCE_RESTRICT);
		}
		for(String pattern: I_READ_URL_POLICIES){
			if(urlPatternMap.get(pattern) != null){
				log.warn("Duplicated pattern instance of " + pattern);
			}
			urlPatternMap.put(pattern,WikiOPERATIONS.INSTANCE_READ);
		}
		for(String pattern: I_ADMIN_URL_POLICIES){
			if(urlPatternMap.get(pattern) != null){
				log.warn("Duplicated pattern instance of " + pattern);
			}
			urlPatternMap.put(pattern,WikiOPERATIONS.INSTANCE_ADMIN);
		}

		methodPatternMap = new HashMap<String, WikiOPERATIONS>();
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// instance scope   METHOD
		for(String pattern: I_READ_METHOD_POLICIES){
			if(methodPatternMap.get(pattern) != null){
				log.warn("Duplicated pattern instance of " + pattern);
			}
			methodPatternMap.put(pattern,WikiOPERATIONS.INSTANCE_READ);
		}
		for(String pattern:  I_WRITE_METHOD_POLICIES){
			if(methodPatternMap.get(pattern) != null){
				log.warn("Duplicated pattern instance of " + pattern);
			}
			methodPatternMap.put(pattern, WikiOPERATIONS.INSTANCE_WRITE);
		}
//		for(String pattern:  I_OFFLINE_METHOD_POLICIES){
//			if(methodPatternMap.get(pattern) != null){
//				log.warn("Duplicated pattern instance of " + pattern);
//			}
//			methodPatternMap.put(pattern, WikiOPERATIONS.INSTANCE_OFFLINE);
//		}
		for(String pattern:  I_ADMIN_METHOD_POLICIES){
			if(methodPatternMap.get(pattern) != null){
				log.warn("Duplicated pattern instance of " + pattern);
			}
			methodPatternMap.put(pattern, WikiOPERATIONS.INSTANCE_ADMIN);
		}
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// space scope    URL           
		for(String pattern: S_READ_URL_PATTERNS){
			if(urlPatternMap.get(pattern) != null){
				log.warn("Duplicated pattern instance of " + pattern);
			}
			urlPatternMap.put(pattern,WikiOPERATIONS.SPACE_PAGE_READ);
		}
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// space scope    Method
		for(String pattern: S_READ_PAGE_METHOD_PATTERNS){
			if(methodPatternMap.get(pattern) != null){
				log.warn("Duplicated pattern space of " + pattern);
			}
			methodPatternMap.put(pattern, WikiOPERATIONS.SPACE_PAGE_READ);
		}
		for(String pattern: S_WRITE_PAGE_METHOD_PATTERNS){
			if(methodPatternMap.get(pattern) != null){
				log.warn("Duplicated pattern space of " + pattern);
			}
			methodPatternMap.put(pattern, WikiOPERATIONS.SPACE_PAGE_WRITE);
		}
		for(String pattern: S_REMOVE_PAGE_METHOD_PATTERNS){
			if(methodPatternMap.get(pattern) != null){
				log.warn("Duplicated pattern space of " + pattern);
			}
			methodPatternMap.put(pattern, WikiOPERATIONS.SPACE_PAGE_REMOVE);
		}
		for(String pattern: S_RESTRICT_PAGE_METHOD_PATTERNS){
			if(methodPatternMap.get(pattern) != null){
				log.warn("Duplicated pattern space of " + pattern);
			}
			methodPatternMap.put(pattern, WikiOPERATIONS.SPACE_PAGE_RESTRICT);
		}
		for(String pattern: S_SPACE_ADMIN_METHDO_PATTERNS){
			if(methodPatternMap.get(pattern) != null){
				log.warn("Duplicated pattern space of " + pattern);
			}
			methodPatternMap.put(pattern, WikiOPERATIONS.SPACE_PAGE_RESTRICT);
		}
		
		for(String pattern: S_OFFLINE_METHOD_PATTERNS){
			if(methodPatternMap.get(pattern) != null){
				log.warn("Duplicated pattern space of " + pattern);
			}
			methodPatternMap.put(pattern, WikiOPERATIONS.SPACE_OFFLINE);
		}
		
		for(String pattern: S_EXPORT_METHOD_PATTERNS){
			if(methodPatternMap.get(pattern) != null){
				log.warn("Duplicated pattern space of " + pattern);
			}
			methodPatternMap.put(pattern, WikiOPERATIONS.SPACE_EXPORT);
		}
		for(String pattern: S_COMMENT_READ_METHOD_PATTERNS){
			if(methodPatternMap.get(pattern) != null){
				log.warn("Duplicated pattern space of " + pattern);
			}
			methodPatternMap.put(pattern, WikiOPERATIONS.SPACE_COMMENT_READ);
		}
		for(String pattern: S_COMMENT_WRITE_METHOD_PATTERNS){
			if(methodPatternMap.get(pattern) != null){
				log.warn("Duplicated pattern space of " + pattern);
			}
			methodPatternMap.put(pattern, WikiOPERATIONS.SPACE_COMMENT_WRITE);
		}
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// page scope               
		for(String pattern: P_READ_METHOD_PATTERNS){
			if(methodPatternMap.get(pattern) != null){
				log.warn("Duplicated pattern page of " + pattern);
			}
			methodPatternMap.put(pattern,WikiOPERATIONS.PAGE_READ);
		}
		for(String pattern: P_WRITE_METHOD_PATTERNS){
			if(methodPatternMap.get(pattern) != null){
				log.warn("Duplicated pattern page of " + pattern);
			}
			methodPatternMap.put(pattern,WikiOPERATIONS.PAGE_WRITE );
		}
		for(String pattern: P_REMOVE_METHOD_PATTERNS){
			if(methodPatternMap.get(pattern) != null){
				log.warn("Duplicated pattern page of " + pattern);
			}
			methodPatternMap.put(pattern,WikiOPERATIONS.PAGE_REMOVE );
		}
		for(String pattern: P_COMMENT_READ_METHOD_PATTERNS){
			if(methodPatternMap.get(pattern) != null){
				log.warn("Duplicated pattern page of " + pattern);
			}
			methodPatternMap.put(pattern,WikiOPERATIONS.PAGE_COMMENT_READ);
		}
		for(String pattern: P_COMMENT_WRITE_METHOD_PATTERNS){
			if(methodPatternMap.get(pattern) != null){
				log.warn("Duplicated pattern page of " + pattern);
			}
			methodPatternMap.put(pattern,WikiOPERATIONS.PAGE_COMMENT_WRITE);
		}

	}
	//********************************************************************
	//                       Private methods
	//********************************************************************
	/**
	 * Get page policy by given resource name. Page policy won't save individual element in PolicyCache, so that method 
	 * have to get space policy of this page, then find out the page policy from it. Be careful, if the page policy won't 
	 * set (means it does not exist in policy cache), an inherent policy will return from its space level.
	 */
	private List<Policy> getPagePolicies(String spaceUname,String pageUuid){
		List<Policy> policies = new ArrayList<Policy>();
		//get this page's space resource
		List<Policy> spacePolicies = getSpacePolicies(spaceUname);
		//sort from page -> space
		for (Policy policy : spacePolicies) {
			if(policies.size() == PAGE_WIKIOPER_SIZE){
				//now, PAGE type resource or policies already get full required (Page READ,WRITE,REMOVE,COMMENT READ,COMMENT WRITE)
				//we don't need to try other.
				break;
			}
			//build this page's policies for all operation. 
			if(policy.getType() == RESOURCE_TYPES.PAGE && policy.getResourceName().equals(pageUuid)){
				//find this page policy:
				policies.add(policy);
			}
		}
		for (Policy policy : spacePolicies) {
			if(policies.size() == PAGE_WIKIOPER_SIZE){
				//not PAGE type resource or policies already get full required (Page READ,WRITE,REMOVE,COMMENT READ,COMMENT WRITE)
				break;
			}
			if(policy.getType() == RESOURCE_TYPES.SPACE){
				if(isPageOperation(policy)){
					//If some operations are not exist in page level, just inherent from space level then
					boolean found = false;
					for (Policy pp : policies) {
						if(pp.getOperation() == policy.getOperation()){
							found = true;
							break;
						}
					}
					if(!found){
						Policy pageP = (Policy) policy.clone();
						pageP.setResourceName(pageUuid);
						pageP.setType(RESOURCE_TYPES.PAGE);
						policies.add(pageP);
					}
				}
			}
		}
		return policies;
	}
	/**
	 * Return space level policy by given spaceUname. If it is available in PolicyCache, just return cached value, otherwise,
	 * it will initialise the cache. Note, these policies contain this space and its relative pages policies as well. It does not
	 * contains instance policies. But the impact of instance permission setting on space/page is handled in result. For example,
	 * Instance not allow "userA" read, then space read policy is not allow "userA" read even space level permission allow it.  
	 * 
	 * 
	 * @param spaceUname
	 * @return
	 */
	private List<Policy> getSpacePolicies(String spaceUname){
		if(StringUtils.isBlank(spaceUname))
			throw new SecurityException("Failed get space policies when given spaceUname is blank.");
		
		List<Policy> policies = policyCache.getPolicies(spaceUname);
		if(policies == null){
			policies = new ArrayList<Policy>();
			//initialise space level policy for a special space:
			//its final only contain given space and its pages policies(if have)
			List<Resource> resources = new ArrayList<Resource>();

			//instance resource: will removed after policy handle is done
			Resource instanceRes = resourceDAO.getByName(WikiConstants.CONST_INSTANCE_RESOURCE_NAME);
			resources.add(instanceRes);
			
			//space resource
			Resource res = resourceDAO.getByName(spaceUname);
			//could not find this space corresponding resource, 
			//Page resource could be null most time. But for space resource, it maybe cause space already delete, 
			if(res != null){
				resources.add(res);
			}
			
			//page resource
			List<Policy> pagePolcies  = new ArrayList<Policy>();
			List<Resource> children = spaceDAO.getSpacePageResources(spaceUname);
			if(children != null && children.size() > 0)
				resources.addAll(children);
			
			//now get all resources for this space, handle them according to strategy.
			for (Resource resource : resources) {
				if(resource == null){
					log.warn("Some resource is null");
					continue;
				}
				PatternFactory strategy = patternFactoryFactory.getFactory(resource.getType());
				if(RESOURCE_TYPES.PAGE.equals(resource.getType()))
					//page permission is forbidden type, rather than instance or space, which default is allow type
					pagePolcies.addAll(strategy.getPolicies(resource));
				else
					policies.addAll(strategy.getPolicies(resource));
			}
			
			
			confilictHandle(policies);
			pagePoliciesHandle(policies,pagePolcies);
			
			//OK, remove instance resource then
			for(Iterator<Policy> iter = policies.iterator();iter.hasNext();){
				if(iter.next().getType() == RESOURCE_TYPES.INSTANCE)
					iter.remove();
			}
			
			log.info("Space " + spaceUname + " policies is initialized.");
			policyCache.setPolicies(spaceUname, policies);
		}
	
		return policies;
	}
	/**
	 * Instance scope policy. It also saved in PolicyCache. It won't contain any space and page policies.
	 * @return
	 */
	private List<Policy> getInstancePolicies() {
		List<Policy> policies = policyCache.getPolicies(WikiConstants.CONST_INSTANCE_RESOURCE_NAME);
		if(policies == null){
			policies = new ArrayList<Policy>();
			Resource resource = resourceDAO.getByName(WikiConstants.CONST_INSTANCE_RESOURCE_NAME);
			PatternFactory strategy = patternFactoryFactory.getFactory(RESOURCE_TYPES.INSTANCE);
			policies.addAll(strategy.getPolicies(resource));
			policyCache.setPolicies(WikiConstants.CONST_INSTANCE_RESOURCE_NAME, policies);
		}
		
		return policies;
	}
	private List<Policy> getWidgetPolicies(String key) {
		//to ensure widget key is not duplicated with Instance, SpaceUname
		List<Policy> policies = policyCache.getPolicies(WikiConstants.CONST_NONSPACE_RESOURCE_PREFIX+key);
		if(policies == null){
			policies = new ArrayList<Policy>();
			Resource resource = resourceDAO.getByName(key);
			if(resource != null){
				PatternFactory strategy = patternFactoryFactory.getFactory(RESOURCE_TYPES.WIDGET);
				policies.addAll(strategy.getPolicies(resource));
			}
			
			policyCache.setPolicies(WikiConstants.CONST_NONSPACE_RESOURCE_PREFIX+key, policies);
		}
		
		return policies;
	}

	/**
	 * check policy's live condition, if live condition does not exist, this policy won't live as well.<BR>
	 * For example, if user READ space permission is off, the user WRITE/ADMIN etc permissions won't useful, remove them.
	 * @param policies
	 */
	private void confilictHandle(List<Policy> policies) {
		
		//retrieve policies, and find out corresponding item in OperationItem
		for(Iterator<Policy> iter = policies.iterator(); iter.hasNext();){
			Policy  policy = iter.next();
			//system default policy: such as /**/singup.do* won't have RESOURCE_TYPES, skip it.
			if(policy.getType() == null)
				continue;
		
			//find out, then check if its live conditions all exist in current policies list
			//if not, then it mean, this policy is useless(for instance, instance is not allow READ, but a page allow READ,
			//then page READ is uselss policy! remove it.
			WikiOPERATIONS item = getOperationItem(policy);
			//does not need live conditions, then need not check
			if(item.liveConditions == null)
				continue;
			
			checkLiveCondition(item,policy,policies);
			//all roles remove for this policy, which means there are some live conditions does not exist, this policy must be dead.
			if(policy.size() == 0){
				log.info("Polciy "+ policy +" has no live conditions, mark as dead.");
				//CAN NOT remove policy without attributes , it means no user access. 
				//If remove policy from list, it means all permission are allowed!!!
				//So, remove all attributes instead of remove it.
				policy.removeAllAttribute();
			}

		}
	}


	/**
	 * A recursive check from its direct parent(live conditions) until root(live conditions is null)
	 * @param item
	 * @param sourceAttributes 
	 * @param policies
	 * @return
	 */
	private void checkLiveCondition(WikiOPERATIONS item,Policy srcPolicy, List<Policy> policies){
		//failure tolerance: some policy can not find dependencies, it is unexpected situation
		if(item == null){
			//remove empty, this makes policy removed
			for(Iterator<ConfigAttribute> iter = srcPolicy.getMutableAttributeDefinition().iterator();iter.hasNext();)
				iter.remove();
			return;
		}
		
		//if null, means it does not need any pre-condition to live
		if(item.liveConditions == null){
			purge(item, srcPolicy, policies);
			return;
		}
		
		for (WikiOPERATIONS condition : item.liveConditions) {
			//recursive to root condition
			checkLiveCondition(condition, srcPolicy, policies);

			if(srcPolicy.size() == 0){
				//already deaded, return
				return;
			}
		}
		
		purge(item, srcPolicy, policies);
		return;
	}
	/**
	 * Remove all <code>sourceAttributes</code> attributes which does not exist in <code>existAttributes</code>. <BR> 
	 * It is a result of left outter join.
	 * 
	 * @param item
	 * @param sourceAttributes
	 * @param policies
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private void purge(WikiOPERATIONS item, Policy srcPolicy, List<Policy> policies) {
		List<String> existAttributes = new ArrayList<String>();
		//collect all roles which have same operation for same resource 
		for (Policy policy : policies){
			if(item.isSame(policy.getType(),policy.getOperation())){
				for(Iterator<ConfigAttribute> iter = policy.getMutableAttributeDefinition().iterator();iter.hasNext();)
					existAttributes.add(iter.next().getAttribute());
			}
		}
		
		Iterator<ConfigAttribute> iter = srcPolicy.getMutableAttributeDefinition().iterator();
		while(iter.hasNext()){
			String name = iter.next().getAttribute();
			//if this (attribute)role is not included in live condition(policy), then remove this role
			if(!existAttributes.contains(name)){
				if(name.startsWith(Role.ROLE_PREFIX)){
					iter.remove();
					//some role removed, record reason
					log.info("Role/user ("+ name +") on permission (" + srcPolicy.getType() + "," + srcPolicy.getOperation()
							+ ") is removed. Reason is " +item+ " does not exist.");
				}else{
//					Now it is (name.startsWith(SecurityValues.USER_PREFIX))
					boolean found = false;
					if(name.length() > Role.USER_PREFIX.length()){
						name = name.substring(Role.USER_PREFIX.length());
						User user = userReadingService.getUserByName(name);
						if (user != null){
							Set<Role> uRoles = user.getRoles();
							//handle user's live conditions. need expand its roles and compare with upper level roles.
							if(uRoles != null){
								for (Role uRole : uRoles) {
									if(existAttributes.contains(uRole.getName())){
										//find any one of roles is contained by upper level roles, means this user has such permission
										//so need not check further
										found = true;
										break;
									}
								}
							}
						}else{
							AuditLogger.error("PatternStrategy.purge can not get user name " + name);
						}
					}	
					if(!found){
						//even user's roles are not contained, just remove this USER from attribute list
						iter.remove();
						//some role removed, record reason
						log.info("Role/user ("+ name +") on permission (" + srcPolicy.getType() + "," + srcPolicy.getOperation()
								+ ") is removed. Reason is " +item+ " does not exist.");
					}
				}
			}
		}
	}


	/**
	 * Merge attributes which have same pattern. For example, \/**\/*do*.? pattern could be used over 1 URL_PATTERN, merge 
	 * their roles.
	 *  <BR>
	 *  This case does not happen so far, just for more safe and flexible pattern definition.
	 * <strong>This method requires Policy must have ONLY ONE role attributes, otherwise, some unexpected case will happen.</strong>
	 * 
	 *  Such as, role1 and role2 are marked WRITE space permission, but only role1 give READ space permission. If put role1 and role2
	 *  into same policy object, their WRITE space permission will both removed.   
	 * @param policies
	 * @return
	 */
//	private void mergePattern(List<Policy> policies) {
//		
//		//merge same patternString polices: because springframework.security(acegi) match pattern one by one, once get a matched, then end check.
//		//if there duplicated pattern, the all role should in same line and append to pattern. 
//		//This means, pattern must be unique!
//		Map<String,Policy> pSet = new HashMap<String,Policy>();
//		boolean merge = false;
//		for (Policy policy : policies) {
//			Policy exist = pSet.get(policy.getPatternString());
//			if(exist != null){
//				merge = true;
//				if(!exist.getOperation().equals(policy.getOperation())
//						|| !exist.getType().equals(policy.getType())){
//					log.error("Policy merge with different operationa or type. Operation ["
//							+exist.getOperation()+":"+policy.getOperation() +"] Type["
//							+exist.getType() + ":" + policy.getType()+ "]");
//				}
//				//merge
//				log.warn("Policy "+policy +" merge with " + exist);
//				Iterator iter = policy.getAttributeDefinition().getConfigAttributes();
//				while(iter.hasNext()){
//					ConfigAttribute att = (ConfigAttribute) iter.next();
//					if(exist.hasAttribute(att.getAttribute())){
//						continue;
//					}
//					exist.addAttribute(att.getAttribute());
//				}
//			}else
//				pSet.put(policy.getPatternString(), policy);
//		}
//		
//		//rebuild policies list and return
//		if(merge){
//			policies.clear();
//			policies.addAll(pSet.values());
//		}
//		
//	}
	/**
	 * For page, it has forbidden policy: don't allow user do an operation. This method will do:<br>
	 * <li>if this page permission method/URL is not exist, inherent from space one(which must exist in theory)</li>
	 * <li>then if this space policy includes forbidden role/user, create a new policy for page except these role/user.</li> 
	 * <li>If this space policy does not includes forbidden role/user, do nothing. 
	 * 	(could be adjusted if updating one item permission won't refresh whole policy tree).   
	 * </li>
	 * <li>Page Read policy will impact all other page policy</li>
	 * @param policies
	 * @param pagePolcies 
	 */
	private void pagePoliciesHandle(List<Policy> policies, List<Policy> pagePolcies) {
		
		//!!!Must ensure page sorted by resource name in order to same page policies could handle together.
		//currently, this conditions is only ensured by code in getPolicies(): pagePolcies.addAll(strategy.getPolicies(resource)); 
		//is it enough?
		
		String resourceName = null;
		List<Policy> allInherit = new ArrayList<Policy>(); 
		List<Policy> modifiedInherit = new ArrayList<Policy>();
		
		//handle page policy(forbidden type)
		for(Iterator<Policy> pageIter = pagePolcies.iterator(); pageIter.hasNext();){
			Policy pagePolicy = pageIter.next();
			
			//this page Permission exist in DB, but not user/role link to this permission. So, skip this useless permission
			//this case happen when user mark a permission on page security dialogue, then remark same to original status.
			//permission item will exist in database table, but no user/role-permission table contain it.
			if(pagePolicy.size() == 0)
				continue;
				
			//now, find all same page resource's policies. To do that, is performance reason: for one page
			//, only once database read to get spaceUname
			if(!pagePolicy.getResourceName().equals(resourceName)){
				resourceName = pagePolicy.getResourceName();
				Page page = pageDAO.getCurrentByUuid(pagePolicy.getResourceName());
				if(page == null){
					log.error("Could not find page by uuid:" + pagePolicy.getResourceName());
					continue;
				}
				String spaceUname = page.getSpace().getUnixName();
				
				allInherit.clear();
				//create all possible operation policy list for this page. 
				for (Policy policy : policies) {
					if(!RESOURCE_TYPES.SPACE.equals(policy.getType()))
						continue;
					if(StringUtils.equals(policy.getResourceName(),spaceUname)){
						//after mergePattern() method, all same resource and operation will be in together
						//allInherent will contains all possible (but unique) to same pattern. 
						if(isPageOperation(policy)){
							//create a new policy then
							Policy inherit = new Policy(); 
							inherit.setOperation(policy.getOperation());
							inherit.setResourceName(pagePolicy.getResourceName());
							inherit.setType(RESOURCE_TYPES.PAGE);
							//
							//inhere all attributes from space
							inherit.addAllAttribute(policy.getMutableAttributeDefinition());
							allInherit.add(inherit);
						}
					}
				}
				//first time loop, it add nothing.but after that, it will add pages (with same space name) in last loop handled  
				//all same page forbidden attribute handled, merge them into policies 
				policies.addAll(modifiedInherit);
				modifiedInherit.clear();
			}

			//retrieve all inherit policies and remove this forbidden role/user
			for (Iterator<Policy> inheritIter = allInherit.iterator();inheritIter.hasNext();) {
				Policy inherit = inheritIter.next();
				if(!OPERATIONS.READ.equals(pagePolicy.getOperation())){
					//for write, remove, commant_read, comment_write, they only modify the inherent policy which has same operation.
					if(!inherit.getOperation().equals(pagePolicy.getOperation())){
						continue;
					}
				}
				//for page read, if a user/role is forbidden, then all other polices(WRITE,REMOVE etc.) of this page 
				// will remove this user/role
				boolean dirty = false;
				for(Iterator<ConfigAttribute> attIter = inherit.getMutableAttributeDefinition().iterator();attIter.hasNext();){
					ConfigAttribute att = attIter.next();
					if(pagePolicy.getMutableAttributeDefinition().contains(att)){
						attIter.remove();
						dirty = true;
					}
				}
				if(dirty && !modifiedInherit.contains(inherit)){
					modifiedInherit.add(inherit);
				}
			}
		}
		//all same page forbidden attribute handled, merge them into policies 
		policies.addAll(modifiedInherit);
		
	}
	
	private WikiOPERATIONS getOperationItem(Policy policy){
		for (WikiOPERATIONS item : WikiOPERATIONS.values()) {
			if(item.isSame(policy.getType(),policy.getOperation())){
				return item;
			}
		}
		
		log.error("No matched operations item in policy "+ policy.toString());
		
		return null;
	}
	private boolean isPageOperation(Policy policy) {
		return OPERATIONS.READ.equals(policy.getOperation())
			|| OPERATIONS.WRITE.equals(policy.getOperation())
			|| OPERATIONS.REMOVE.equals(policy.getOperation())
			|| OPERATIONS.OFFLINE.equals(policy.getOperation())
			|| OPERATIONS.COMMENT_READ.equals(policy.getOperation()) 
			|| OPERATIONS.COMMENT_WRITE.equals(policy.getOperation());
	}

	//********************************************************************
	//               set / get 
	//********************************************************************
	public void setResourceDAO(ResourceDAO resourceDAO) {
		this.resourceDAO = resourceDAO;
	}
	public void setPatternFactoryFactory(PatternFactoryFactory strategyFactory) {
		this.patternFactoryFactory = strategyFactory;
	}

	public void setPageDAO(PageDAO pageDAO) {
		this.pageDAO = pageDAO;
	}

	public void setPolicyCache(PolicyCache policyCache) {
		this.policyCache = policyCache;
	}
	public void setSpaceDAO(SpaceDAO spaceDAO) {
		this.spaceDAO = spaceDAO;
	}
	/**
	 * @param userReadingService the userReadingService to set
	 */
	public void setUserReadingService(UserReadingService userReadingService) {
		this.userReadingService = userReadingService;
	}
	public void setMethodValueProviderList(List<MethodValueProvider> methodValueProviderList) {
		this.methodValueProviderList = methodValueProviderList;
	}
	public void setUrlValueProviderList(List<URLValueProvider> urlValueProviderList) {
		this.urlValueProviderList = urlValueProviderList;
	}
	

}
