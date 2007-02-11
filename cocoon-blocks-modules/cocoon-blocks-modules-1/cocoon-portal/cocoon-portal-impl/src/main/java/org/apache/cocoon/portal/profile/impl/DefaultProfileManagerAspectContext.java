/*
 * Copyright 2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.portal.profile.impl;

import java.util.Iterator;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.profile.ProfileManagerAspect;
import org.apache.cocoon.portal.profile.ProfileManagerAspectContext;
import org.apache.cocoon.portal.scratchpad.Profile;

/**
 * The aspect context is passed to every aspect.
 * @since 2.2
 * @version $Id$
 */
public final class DefaultProfileManagerAspectContext
    implements ProfileManagerAspectContext {

    private final PortalService service;
    private final Iterator iterator;
    private final Iterator configIterator;
    private Parameters config;
    private Profile profile;
 
    public DefaultProfileManagerAspectContext(ProfileManagerAspectChain chain,
                                              PortalService             service) {
        this.service = service;
        this.iterator = chain.getIterator();
        this.configIterator = chain.getConfigIterator();
    }

	/**
	 * @see org.apache.cocoon.portal.profile.ProfileManagerAspectContext#invokeNext(org.apache.cocoon.portal.scratchpad.Profile)
	 */
	public void invokeNext(Profile portalProfile) {
        this.profile = portalProfile;
        if (this.iterator.hasNext()) {
            this.config = (Parameters)this.configIterator.next();
            final ProfileManagerAspect aspect = (ProfileManagerAspect) iterator.next();
            aspect.prepare(this, this.profile);
        }
    }

    /**
     * @see org.apache.cocoon.portal.profile.ProfileManagerAspectContext#getAspectParameters()
     */
    public Parameters getAspectParameters() {
        return this.config;
    }

    /**
     * @see org.apache.cocoon.portal.profile.ProfileManagerAspectContext#getPortalService()
     */
    public PortalService getPortalService() {
        return this.service;
    }

    public Profile getProfile() {
        return this.profile;
    }
}