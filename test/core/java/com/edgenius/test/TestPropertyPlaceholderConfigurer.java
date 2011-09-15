/* 
 * =============================================================
 * Copyright (C) 2007-2010 Edgenius (http://www.edgenius.com)
 * =============================================================
 * Edgenius, Confidential and Proprietary
 * License Information: http://www.edgenius.com/licensing/edgenius/1.0/
 *
 * This computer program contains valuable, confidential and proprietary
 * information.  Disclosure, use, or reproduction without the written
 * authorization of Edgenius is prohibited.  This unpublished
 * work by Edgenius is protected by the laws of the United States
 * and other countries.  If publication of the computer program should occur,
 * the following notice shall apply:
 *  
 * Copyright (C) 2007-2010 Edgenius.  All rights reserved.                                                              
 * ****************************************************************
 */
package com.edgenius.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import com.edgenius.core.DataRoot;

/**
 * @author Dapeng.Ni
 */
public class TestPropertyPlaceholderConfigurer extends org.springframework.beans.factory.config.PropertyPlaceholderConfigurer implements BeanFactoryPostProcessor{
	private static final Logger log = LoggerFactory.getLogger(TestPropertyPlaceholderConfigurer.class);
	
	private String dataRoot;
	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		//System.setProperty("edgenius.test.model",dataRoot);
		DataRoot.rootResource = dataRoot;
		log.info("System initial to debug model. Test root: " + dataRoot);
		
		super.postProcessBeanFactory(beanFactory);
		
	}

	public String getDataRoot() {
		return dataRoot;
	}
	public void setDataRoot(String dataRoot) {
		this.dataRoot = dataRoot;
	}
}
