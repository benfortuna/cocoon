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
package org.apache.cocoon.components.language.programming;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.parameters.ParameterException;

import org.apache.cocoon.components.language.LanguageException;
import org.apache.cocoon.components.language.generator.CompiledComponent;
import org.apache.cocoon.util.ClassUtils;

import java.io.File;

/**
 * Base implementation of <code>ProgrammingLanguage</code>. This class sets the
 * <code>CodeFormatter</code> instance and deletes source program files after
 * unloading.
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id: AbstractProgrammingLanguage.java,v 1.1 2003/03/09 00:08:59 pier Exp $
 */
public abstract class AbstractProgrammingLanguage extends AbstractLogEnabled
        implements ProgrammingLanguage, Parameterizable {

    /** The source code formatter */
    protected Class codeFormatter;

    protected String languageName;

    /**
     * Set the configuration parameters. This method instantiates the
     * sitemap-specified source code formatter
     *
     * @param params The configuration parameters
     * @exception ParameterException If the language compiler cannot be loaded
     */
    public void parameterize(Parameters params) throws ParameterException {
        String className = params.getParameter("code-formatter", null);

        try {
            if (className != null) {
                this.codeFormatter = ClassUtils.loadClass(className);
            }
        } catch (Exception e) {
            getLogger().error("Error with \"code-formatter\" parameter", e);
            throw new ParameterException("Unable to load code formatter: " + className, e);
        }
    }

    /**
     * Return this language's source code formatter. A new formatter instance is
     * created on each invocation.
     *
     * @return The language source code formatter
     */
    public CodeFormatter getCodeFormatter() {
        if (this.codeFormatter != null) {
            try {
                CodeFormatter formatter = (CodeFormatter) this.codeFormatter.newInstance();
                if (formatter instanceof LogEnabled) {
                    ((LogEnabled) formatter).enableLogging(this.getLogger());
                }
                return formatter;
            } catch (Exception e) {
                getLogger().error("Error instantiating CodeFormatter", e);
            }
        }

        return null;
    }

    /**
     * Unload a previously loaded program
     *
     * @param program A previously loaded object program
     * @exception LanguageException If an error occurs during unloading
     */
    protected abstract void doUnload(Object program, String filename,
                                     File baseDirectory)
            throws LanguageException;

    public final void unload(Object program, String filename, File baseDirectory)
            throws LanguageException {

        File file = new File(baseDirectory,
                             filename + "." + getSourceExtension());
        file.delete();
        this.doUnload(program, filename, baseDirectory);
    }

    public final void setLanguageName(String name) {
        this.languageName = name;
    }

    public final String getLanguageName() {
        return this.languageName;
    }

    /**
     * Create a new instance for the given class
     *
     * @param program The Java class
     * @return A new class instance
     * @exception LanguageException If an instantiation error occurs
     */
    public CompiledComponent instantiate(Program program) throws LanguageException {
        try {
            return program.newInstance();
        } catch (Exception e) {
            getLogger().warn("Could not instantiate program instance", e);
            throw new LanguageException("Could not instantiate program instance due to a " + e.getClass().getName() + ": " + e.getMessage());
        }
    }
}
