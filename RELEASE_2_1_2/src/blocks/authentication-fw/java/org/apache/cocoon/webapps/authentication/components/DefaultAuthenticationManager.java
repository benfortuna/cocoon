/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.webapps.authentication.components;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.components.SitemapConfigurable;
import org.apache.cocoon.components.SitemapConfigurationHolder;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.util.ClassUtils;
import org.apache.cocoon.webapps.authentication.AuthenticationConstants;
import org.apache.cocoon.webapps.authentication.AuthenticationManager;
import org.apache.cocoon.webapps.authentication.configuration.HandlerConfiguration;
import org.apache.cocoon.webapps.authentication.user.RequestState;
import org.apache.cocoon.webapps.authentication.user.UserHandler;
import org.apache.cocoon.webapps.authentication.user.UserState;
import org.apache.cocoon.webapps.session.ContextManager;
import org.apache.cocoon.webapps.session.SessionManager;
import org.apache.cocoon.webapps.session.context.SessionContext;
import org.apache.excalibur.source.SourceParameters;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.SourceUtil;
import org.xml.sax.SAXException;

/**
 * This is the basis authentication component.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: DefaultAuthenticationManager.java,v 1.17 2003/09/24 21:22:33 cziegeler Exp $
*/
public class DefaultAuthenticationManager
extends AbstractLogEnabled
implements AuthenticationManager, 
            SitemapConfigurable, 
            Serviceable, 
            Disposable, 
            ThreadSafe, 
            Contextualizable,
            Component {

    /** The name of the session attribute storing the user status */
    public final static String SESSION_ATTRIBUTE_USER_STATUS = DefaultAuthenticationManager.class.getName() + "/UserStatus";

    /** The manager for the authentication handlers */
    protected SitemapConfigurationHolder holder;
    
    /** The Service Manager */
    protected ServiceManager manager;
    
    /** The Source Resolver */
    protected SourceResolver resolver;
    
    /** The context */
    protected Context context;
    
    /** Instantiated authenticators */
    protected Map authenticators = new HashMap();
    
    /** This is the key used to store the current request state in the request object */
    private static final String REQUEST_STATE_KEY = RequestState.class.getName();
    
    /**
     * Set the sitemap configuration containing the handlers
     */
    public void configure(SitemapConfigurationHolder holder)
    throws ConfigurationException {
        this.holder = holder;
    }

    /**
     * Get the handler configuration for the current sitemap
     */
    private Map getHandlerConfigurations() 
    throws ProcessingException {
        Map configs = (Map) this.holder.getPreparedConfiguration();
        if ( null == configs ) {
            // prepare the configs
            SourceResolver resolver = null;
            try {       
                resolver = (SourceResolver) this.manager.lookup( SourceResolver.ROLE );
                configs = DefaultHandlerManager.prepareHandlerConfiguration(resolver, 
                                                                            ContextHelper.getObjectModel(this.context), 
                                                                            this.holder);
            } catch (ServiceException se) {
                throw new ProcessingException("Unable to lookup source resolver.", se);
            } catch (ConfigurationException ce) {
                throw new ProcessingException("Configuration error.", ce);
            } finally {
                this.manager.release( resolver );
            }
        }
        return configs;
    }

    /**
     * Get the handler configuration
     * @param name The handler name
     * @return The configuration or null.
     */
    private HandlerConfiguration getHandlerConfiguration(String name) 
    throws ProcessingException {   
        final Map configs = this.getHandlerConfigurations();
        HandlerConfiguration c = null;
        if ( configs != null) {
            c = (HandlerConfiguration)configs.get( name ); 
        }
        return c;
    }
    
    private Request getRequest() {
        return ContextHelper.getRequest(this.context);
    }
    
    private Session getSession(boolean create) {        
        return this.getRequest().getSession(create);
    }
    
    private UserState getUserState() {
        final Session session = this.getSession( false );
        UserState status = null;
        if ( session != null) {
            status = (UserState) session.getAttribute(SESSION_ATTRIBUTE_USER_STATUS);
        }
        return status;
    }

    private UserState createUserState() {
        UserState status = this.getUserState();
        if ( status == null ) {
            final Session session = this.getSession(true);
            status = new UserState();
            session.setAttribute(SESSION_ATTRIBUTE_USER_STATUS, status);
        }
        return status;
    }
    
    private UserHandler getUserHandler(String name) {
        final UserState status = this.getUserState();
        if ( status != null ) {
            return status.getHandler( name );
        }
        return null;
    }
    
    private void updateUserState() {
        final Session session = this.getSession(true);
        Object status = session.getAttribute(SESSION_ATTRIBUTE_USER_STATUS);
        session.setAttribute(SESSION_ATTRIBUTE_USER_STATUS, status);
    }
    
	/* (non-Javadoc)
	 * @see org.apache.cocoon.webapps.authentication.components.Manager#authenticate(java.lang.String, java.lang.String, org.apache.excalibur.source.SourceParameters)
	 */
	public UserHandler login(String handlerName,
                             String applicationName,
                             SourceParameters parameters)
    throws ProcessingException {
        HandlerConfiguration config = this.getHandlerConfiguration( handlerName );
        if ( config == null ) {
            throw new ProcessingException("Unknown handler to authenticate: " + handlerName);
        }
        // are we already logged in?
        UserHandler handler = this.getUserHandler( handlerName );
        if ( handler != null ) {
            throw new ProcessingException("User is already authenticated using handler: " + handlerName);
        }
        
        Authenticator authenticator = this.lookupAuthenticator( config );
        try {
            handler = authenticator.authenticate( config, parameters );
        } finally {
            this.releaseAuthenticator( authenticator, config );
        }
        
        if ( handler != null ) {
            // create UserStatus
            final UserState status = this.createUserState();
        
            status.addHandler( handler );        
            this.updateUserState();
        
            // update RequestState
            RequestState state = new RequestState( handler, applicationName);
            this.setState( state );
            state.initialize( this.resolver );
            
        }
        
 		return handler;
	}

	/**
     * Release the used authenticator
     */
    protected void releaseAuthenticator(Authenticator authenticator, HandlerConfiguration config) {
        // all authenticators are released on dispose
    }

    /**
     * The authenticator used to authenticate a user 
     */
    protected Authenticator lookupAuthenticator(HandlerConfiguration config) 
    throws ProcessingException {
        final String name = config.getAuthenticatorClassName();
        Authenticator authenticator = (Authenticator) this.authenticators.get(name);
        if ( authenticator == null ) {
            synchronized (this) {
                authenticator = (Authenticator) this.authenticators.get(name);
                if ( authenticator == null ) {
                    try {
                        authenticator = (Authenticator) ClassUtils.newInstance(name);
                        ContainerUtil.enableLogging( authenticator, this.getLogger() );
                        ContainerUtil.contextualize( authenticator, this.context);
                        ContainerUtil.service( authenticator, this.manager );
                        ContainerUtil.initialize( authenticator );
                        this.authenticators.put(name, authenticator);
                        
                    } catch (Exception e ) {
                        throw new ProcessingException("Unable to initialize authenticator from class " + name, e);
                    }
                }
            }
        }
        return authenticator;
    }

    /* (non-Javadoc)
	 * @see org.apache.cocoon.webapps.authentication.components.Manager#checkAuthentication(org.apache.cocoon.environment.Redirector, java.lang.String, java.lang.String)
	 */
	public boolean checkAuthentication(Redirector redirector,
                                        String handlerName,
                                        String applicationName)
	throws IOException, ProcessingException {
        HandlerConfiguration config = this.getHandlerConfiguration( handlerName );
        if ( config == null ) {
            throw new ProcessingException("Unknown handler to check: " + handlerName);
        }
        // are we already logged in?
        UserHandler handler = this.getUserHandler( handlerName );
        final boolean authenticated = ( handler != null );
        if ( !authenticated ) {
            if (redirector != null) {
                // create parameters
                SourceParameters parameters = config.getRedirectParameters();
                if (parameters == null) parameters = new SourceParameters();
                final Request request = this.getRequest();
                String resource = request.getRequestURI();
                if (request.getQueryString() != null) {
                    resource += '?' + request.getQueryString();
                }
    
                parameters.setSingleParameterValue("resource", resource);
                final String redirectURI = config.getRedirectURI();
                redirector.globalRedirect(false, SourceUtil.appendParameters(redirectURI, parameters));
            }
        } else {
            // update state
            RequestState state = new RequestState( handler, applicationName );
            this.setState( state );
            state.initialize( this.resolver );
        }
        
		return authenticated;
	}

    public String getForwardingURI(String handlerName) throws ProcessingException {
        HandlerConfiguration config = this.getHandlerConfiguration( handlerName );
        SourceParameters parameters = config.getRedirectParameters();
        if (parameters == null) parameters = new SourceParameters();
        final Request request = this.getRequest();
        String resource = request.getRequestURI();
        if (request.getQueryString() != null) {
            resource += '?' + request.getQueryString();
        }
    
        parameters.setSingleParameterValue("resource", resource);
        final String redirectURI = config.getRedirectURI();
        return SourceUtil.appendParameters(redirectURI, parameters);
    }
	/* (non-Javadoc)
	 * @see org.apache.cocoon.webapps.authentication.components.Manager#isAuthenticated(java.lang.String)
	 */
	public UserHandler isAuthenticated(String handlerName)
    throws ProcessingException {
        return this.getUserHandler( handlerName  );
	}

	/* (non-Javadoc)
	 * @see org.apache.cocoon.webapps.authentication.components.Manager#logout(java.lang.String, java.lang.String)
	 */
	public void logout(String handlerName, int mode) 
    throws ProcessingException {
        HandlerConfiguration config = this.getHandlerConfiguration( handlerName );
        if ( config == null ) {
            throw new ProcessingException("Unknown handler to logout: " + handlerName);
        }
        // are we logged in?
        UserHandler handler = this.getUserHandler( handlerName );
        // we don't throw an exception if we are already logged out!
        if ( handler != null ) {
            List applicationContexts = handler.getApplicationContexts();
            if ( applicationContexts != null ) {
                ContextManager contextManager = null;

                try {
                    contextManager = (ContextManager)this.manager.lookup(ContextManager.ROLE);

                    Iterator i = applicationContexts.iterator();
                    while ( i.hasNext() ) {
                        final String current = (String)i.next();
                        contextManager.deleteContext( current );
                    }
                } catch (ServiceException ce) {
                    throw new ProcessingException("Unable to create session context.", ce);
                } finally {
                    this.manager.release( contextManager);
                }
            }
    
            UserState status = this.getUserState();
            status.removeHandler( handlerName );
            this.updateUserState();

            // handling of session termination
            SessionManager sessionManager = null;
            try {
                sessionManager = (SessionManager)this.manager.lookup( SessionManager.ROLE );
            
                if ( mode == AuthenticationConstants.LOGOUT_MODE_IMMEDIATELY ) {
                    sessionManager.terminateSession(true);
                } else if ( mode == AuthenticationConstants.LOGOUT_MODE_IF_UNUSED ) {
                    if ( !status.hasHandler()) {
                        sessionManager.terminateSession( false ); 
                    }
                 
                } else if ( mode == AuthenticationConstants.LOGOUT_MODE_IF_NOT_AUTHENTICATED) {
                    if ( !status.hasHandler()) {
                        sessionManager.terminateSession( true ); 
                    }
                } else {
                    throw new ProcessingException("Unknown logout mode: " + mode);
                }
            
            } catch (ServiceException se) {
                throw new ProcessingException("Unable to lookup session manager.", se);
            } finally {
                this.manager.release( sessionManager );
            }
        }
        
	}

	/**
     * Serviceable
	 */
	public void service(ServiceManager manager) 
    throws ServiceException {
        this.manager = manager;
        this.resolver = (SourceResolver) this.manager.lookup(SourceResolver.ROLE);
	}

	/* (non-Javadoc)
	 * @see org.apache.avalon.framework.activity.Disposable#dispose()
	 */
	public void dispose() {
        Iterator iter = this.authenticators.values().iterator();
        while ( iter.hasNext() ) {
            final Authenticator authenticator = (Authenticator) iter.next();
            ContainerUtil.dispose( authenticator );
        }
        if ( this.manager != null) {
            this.manager.release( this.resolver );
            this.manager = null;
            this.resolver = null;
        }
	}

    /**
     * Get the current state of authentication
     */
    public RequestState getState() {
        return getRequestState(this.context);
    }

    public static RequestState getRequestState(Context context) {
        final Request req = ContextHelper.getRequest(context);
        return (RequestState)req.getAttribute( REQUEST_STATE_KEY);
    }
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(Context context) throws ContextException {
        this.context = context;
    }

    protected void setState(RequestState status) {
        final Request req = ContextHelper.getRequest(this.context);
        if ( status != null ) {
            req.setAttribute(  REQUEST_STATE_KEY, status);
        } else {
            req.removeAttribute( REQUEST_STATE_KEY );
        }
    }
    
    /**
     * Create Application Context.
     * This context is destroyed when the user logs out of the handler
     */
    public SessionContext createApplicationContext(String name,
                                                   String loadURI,
                                                   String saveURI)
    throws ProcessingException {
        RequestState state = this.getState();
        UserHandler handler = state.getHandler();
        
        SessionContext context = null;

        if ( handler != null ) {
            ContextManager contextManager = null;
            try {
                contextManager = (ContextManager)this.manager.lookup(ContextManager.ROLE);
                // create new context
                context = contextManager.createContext(name, loadURI, saveURI);
                handler.addApplicationContext( name );

            } catch (ServiceException ce) {
                throw new ProcessingException("Unable to create session context.", ce);
            } catch (IOException ioe) {
                throw new ProcessingException("Unable to create session context.", ioe);
            } catch (SAXException saxe) {
                throw new ProcessingException("Unable to create session context.", saxe);
            } finally {
                manager.release( contextManager);
            }
        } else {
            throw new ProcessingException("No handler defined. Unable to create application context.");
        }

        return context;
    }

}


