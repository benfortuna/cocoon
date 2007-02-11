/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2002 The Apache Software Foundation. All rights reserved.

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
package org.apache.cocoon.components.language.markup.xsp;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentException;

import org.apache.cocoon.ProcessingException;

import org.apache.cocoon.webapps.session.SessionManager;

import org.w3c.dom.DocumentFragment;

/**
 * The <code>Session-fw</code> object helper
 *
 * @author <a href="mailto:antonio@apache.org">Antonio Gallardo</a>
 * @version CVS $Id: XSPSessionFwHelper.java,v 1.4 2003/10/21 07:52:02 cziegeler Exp $
 * @since 2.1.1
 */
public class XSPSessionFwHelper {

    /** GetXML Fragment from the given session context and path
     *
     *
     * @param session The Session object
     * @param context The Session context tha define where to search
     * @param path The parameter path
     * @param defaultValue Value to substitute in absence of the required Fragment
    **/
    public static DocumentFragment getXML(ComponentManager cm, String context, String path) throws ProcessingException {

        SessionManager sessionManager = null;
        try {
            // Start looking up the manager
            sessionManager = (SessionManager)cm.lookup(SessionManager.ROLE);
            // Get the fragment
            DocumentFragment df = sessionManager.getContextFragment(context, path);
            return df;
        } catch (ComponentException ce) {
            throw new ProcessingException("Error during lookup of SessionManager component.", ce);
        } finally {
            // End releasing the sessionmanager
		    cm.release((Component)sessionManager);
	    }
     }
}
