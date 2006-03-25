/*
 * Copyright 2006 The Apache Software Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.core.container.spring;

import javax.servlet.ServletConfig;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.ProcessingUtil;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.core.Settings;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.servlet.CocoonServlet;
import org.apache.excalibur.source.SourceResolver;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * @see BeanFactoryFactory
 * @since 2.2
 * @version $Id$
 */
public class BeanFactoryFactoryImpl
    implements BeanFactoryFactory, BeanFactoryAware {

    protected ConfigurableListableBeanFactory beanFactory;

    public void setBeanFactory(BeanFactory factory) throws BeansException {
        if ( !(factory instanceof ConfigurableListableBeanFactory) ) {
            throw new BeanInitializationException("BeanFactory is not a configurable listable bean factory: " + factory);
        }
        this.beanFactory = (ConfigurableListableBeanFactory)factory;
    }

    /**
     * @see org.apache.cocoon.core.container.spring.BeanFactoryFactory#createBeanFactory(org.apache.avalon.framework.logger.Logger, org.apache.avalon.framework.configuration.Configuration, org.apache.avalon.framework.context.Context, org.apache.excalibur.source.SourceResolver)
     */
    public ConfigurableListableBeanFactory createBeanFactory(Logger         sitemapLogger,
                                                             Configuration  config,
                                                             Context        sitemapContext,
                                                             SourceResolver resolver)
    throws Exception {
        // setup spring container
        // first, get the correct parent
        ConfigurableListableBeanFactory parentFactory = this.beanFactory;
        final Request request = ContextHelper.getRequest(sitemapContext);
        if (request.getAttribute(CocoonBeanFactory.BEAN_FACTORY_REQUEST_ATTRIBUTE, Request.REQUEST_SCOPE) != null) {
            parentFactory = (ConfigurableListableBeanFactory) request
                    .getAttribute(CocoonBeanFactory.BEAN_FACTORY_REQUEST_ATTRIBUTE, Request.REQUEST_SCOPE);
        }

        if ( config != null ) {
            final AvalonEnvironment ae = new AvalonEnvironment();
            ae.context = sitemapContext;
            if ( sitemapLogger != null ) {
                ae.logger = sitemapLogger;
            } else {
                ae.logger = (Logger)parentFactory.getBean(ProcessingUtil.LOGGER_ROLE);
            }
            ae.servletContext = ((ServletConfig) sitemapContext.get(CocoonServlet.CONTEXT_SERVLET_CONFIG))
                    .getServletContext();
            ae.settings = (Settings) this.beanFactory.getBean(Settings.ROLE);
            final ConfigurationInfo parentConfigInfo = (ConfigurationInfo) parentFactory
                    .getBean(ConfigurationInfo.class.getName());
            final ConfigurationInfo ci = ConfigReader.readConfiguration(config, parentConfigInfo, ae, resolver);
    
            return BeanFactoryUtil.createBeanFactory(ae, ci, parentFactory, false);
        }
        return parentFactory;
    }
}
