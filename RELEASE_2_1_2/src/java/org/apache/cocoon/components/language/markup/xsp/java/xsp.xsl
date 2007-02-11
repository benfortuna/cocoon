<?xml version="1.0"?>

<!-- $Id: xsp.xsl,v 1.3 2003/07/13 17:16:54 vgritsenko Exp $-->
<!--

 ============================================================================
                   The Apache Software License, Version 1.2
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

 4. The names "Cocoon" and  "Apache Software Foundation"  must not be used to
    endorse  or promote  products derived  from this  software without  prior
    written permission. For written permission, please contact
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

-->

<!--
 * XSP Core logicsheet for the Java language
 *
 * @author <a href="mailto:ricardo@apache.org>Ricardo Rocha</a>
 * @author <a href="sylvain.wallez@anyware-tech.com">Sylvain Wallez</a>
 * @version CVS $Revision: 1.3 $ $Date: 2003/07/13 17:16:54 $
-->

<xsl:stylesheet version="1.0"
                xmlns:xsp="http://apache.org/xsp"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:XSLTExtension="org.apache.cocoon.components.language.markup.xsp.XSLTExtension">
  <xsl:output method="text"/>

  <xsl:variable name="xsp-uri" select="'http://apache.org/xsp'"/>

  <!--
    this variable holds the instance of extension class to properly
    escape text into Java strings

    ovidiu: use the class name as the namespace to identify the
    class. This is supposedly portable across XSLT implementations.
  -->
  <xsl:variable
       name="extension"
       select="XSLTExtension:new()"
       xmlns:XSLTExtension="org.apache.cocoon.components.language.markup.xsp.XSLTExtension"/>

  <xsl:template match="/">
    <code xml:space="preserve">
      <xsl:apply-templates select="xsp:page"/>
    </code>
  </xsl:template>

  <xsl:template match="xsp:page">
    package <xsl:value-of select="translate(@file-path, '/', '.')"/>;

    import java.io.File;
    import java.io.IOException;
    import java.io.StringReader;
    //import java.net.*;
    import java.util.Date;
    import java.util.List;
    import java.util.Stack;

    //import org.w3c.dom.*;
    import org.xml.sax.InputSource;
    import org.xml.sax.SAXException;
    import org.xml.sax.helpers.AttributesImpl;

    //import org.apache.avalon.framework.*;
    import org.apache.avalon.framework.component.Component;
    import org.apache.avalon.framework.component.ComponentException;
    import org.apache.avalon.framework.component.ComponentManager;
    import org.apache.avalon.framework.component.ComponentSelector;
    import org.apache.avalon.framework.context.Context;
    //import org.apache.avalon.framework.util.*;

    import org.apache.cocoon.Constants;
    import org.apache.cocoon.ProcessingException;
    import org.apache.cocoon.generation.Generator;
    //import org.apache.cocoon.util.*;

    import org.apache.cocoon.components.language.markup.xsp.XSPGenerator;
    import org.apache.cocoon.components.language.markup.xsp.XSPObjectHelper;
    import org.apache.cocoon.components.language.markup.xsp.XSPRequestHelper;
    import org.apache.cocoon.components.language.markup.xsp.XSPResponseHelper;
    import org.apache.cocoon.components.language.markup.xsp.XSPSessionHelper;

    /* User Imports */
    <xsl:for-each select="xsp:structure/xsp:include">
      import <xsl:value-of select="."/>;
    </xsl:for-each>

    /**
     * Generated by XSP. Edit at your own risk, :-)
     */
    public class <xsl:value-of select="@file-name"/> extends XSPGenerator {

        // Files this XSP depends on
        private static File[] _dependentFiles = new File[] {
          <xsl:for-each select="//xsp:dependency">
                  new File("<xsl:value-of select="translate(., '\','/')"/>"),
                </xsl:for-each>
            };

        // Initialize attributes used by modifiedSince() (see AbstractServerPage)
        {
            this.dateCreated = <xsl:value-of select="@creation-date"/>L;
            this.dependencies = _dependentFiles;
        }

        /* Built-in parameters available for use */
        // context    - org.apache.cocoon.environment.Context
        // request    - org.apache.cocoon.environment.Request
        // response   - org.apache.cocoon.environment.Response
        // parameters - parameters defined in the sitemap
        // objectModel- java.util.Map
        // resolver   - org.apache.cocoon.environment.SourceResolver

        /* User Class Declarations */
        <xsl:apply-templates select="xsp:logic"/>

        /**
         * Generate XML data.
         */
        public void generate() throws SAXException, IOException, ProcessingException {
            <!-- Do any user-defined necessary initializations -->
            <xsl:for-each select="xsp:init-page">
              <xsl:copy-of select="."/>
            </xsl:for-each>

            this.contentHandler.startDocument();
            AttributesImpl xspAttr = new AttributesImpl();

            <!-- Generate top-level processing instructions -->
            <xsl:apply-templates select="/processing-instruction()"/>

            <!-- Process only 1st non-XSP element as generated root -->
            <xsl:call-template name="process-first-element">
              <xsl:with-param name="content" select="*[not(namespace-uri(.) = $xsp-uri)][1]"/>
            </xsl:call-template>

            this.contentHandler.endDocument();

            <!-- Do any user-defined necessary clean-ups -->
            <xsl:for-each select="xsp:exit-page">
              <xsl:copy-of select="."/>
            </xsl:for-each>
        }
    }
  </xsl:template>


  <xsl:template name="process-first-element">
    <xsl:param name="content"/>

    <!-- Generate top-level namespaces declarations -->
    <xsl:variable name="parent-element" select="$content/.."/>
    <xsl:for-each select="$content/namespace::*">
      <xsl:variable name="ns-prefix" select="local-name(.)"/>
      <xsl:variable name="ns-uri" select="string(.)"/>
        <!-- Declare namespaces that also exist on the parent (i.e. not locally declared),
             and filter out "xmlns:xmlns" namespace produced by Xerces+Saxon -->
        <xsl:if test="($ns-prefix != 'xmlns') and $parent-element/namespace::*[local-name(.) = $ns-prefix and string(.) = $ns-uri]">
          this.contentHandler.startPrefixMapping(
            "<xsl:value-of select="$ns-prefix"/>",
            "<xsl:value-of select="$ns-uri"/>"
          );
      </xsl:if>
    </xsl:for-each>

    <!-- Generate content -->
    <xsl:apply-templates select="$content"/>

    <!-- Close top-level namespaces declarations-->
    <xsl:for-each select="$content/namespace::*">
      <xsl:variable name="ns-prefix" select="local-name(.)"/>
      <xsl:variable name="ns-uri" select="string(.)"/>
      <xsl:if test="($ns-prefix != 'xmlns') and $parent-element/namespace::*[local-name(.) = $ns-prefix and string(.) = $ns-uri]">
      this.contentHandler.endPrefixMapping(
        "<xsl:value-of select="local-name(.)"/>"
      );
      </xsl:if>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="xsp:element">
    <xsl:variable name="uri">
      <xsl:call-template name="get-parameter">
        <xsl:with-param name="name">uri</xsl:with-param>
      </xsl:call-template>
    </xsl:variable>

    <xsl:variable name="prefix">
      <xsl:call-template name="get-parameter">
        <xsl:with-param name="name">prefix</xsl:with-param>
      </xsl:call-template>
    </xsl:variable>

    <xsl:if test="@name and contains(@name, ':')">
      <xsl:call-template name="error">
        <xsl:with-param name="message">[&lt;xsp:element name="<xsl:value-of select="@name"/>"&gt;]
Name can not contain ':'. If you want to create namespaced element, specify 'uri' and 'prefix'.
        </xsl:with-param>
      </xsl:call-template>
    </xsl:if>

    <xsl:variable name="name">
      <xsl:call-template name="get-parameter">
        <xsl:with-param name="name">name</xsl:with-param>
        <xsl:with-param name="required">true</xsl:with-param>
      </xsl:call-template>
    </xsl:variable>

    <xsl:variable name="raw-name">
      <xsl:if test="
        ($uri = '&quot;&quot;' and $prefix != '&quot;&quot;') or
        ($uri != '&quot;&quot;' and $prefix = '&quot;&quot;')
      ">
        <xsl:call-template name="error">
          <xsl:with-param name="message">[&lt;xsp:element&gt;]
Either both 'uri' and 'prefix' or none of them must be specified
          </xsl:with-param>
        </xsl:call-template>
      </xsl:if>

      <xsl:choose>
        <xsl:when test="$prefix = '&quot;&quot;'">
          <xsl:copy-of select="$name"/>
        </xsl:when>
        <xsl:otherwise>
          (<xsl:copy-of select="$prefix"/>)+":"+(<xsl:copy-of select="$name"/>)
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <!-- Declare namespaces that are not already present on the parent element.
         Note : we could use "../../namespace::*[...]" to get parent element namespaces
         but Xalan 2.0.1 retunrs only the first namespace (Saxon is OK).
         That's why we store the parent element in a variable -->
    <xsl:variable name="parent-element" select=".."/>
    <xsl:for-each select="namespace::*">
      <xsl:variable name="ns-prefix" select="local-name(.)"/>
      <xsl:variable name="ns-uri" select="string(.)"/>
      <xsl:if test="not($parent-element/namespace::*[local-name(.) = $ns-prefix and string(.) = $ns-uri])">
        this.contentHandler.startPrefixMapping(
          "<xsl:value-of select="local-name(.)"/>",
          "<xsl:value-of select="."/>");
      </xsl:if>
    </xsl:for-each>

    <!-- Declare namespace defined by @uri and @prefix attribute -->
    <xsl:if test="$uri != '&quot;&quot;'">
      <xsl:if test="not($parent-element/namespace::*[local-name(.) = $prefix and string(.) = $uri])">
        this.contentHandler.startPrefixMapping(
          <xsl:value-of select="$prefix"/>,
          <xsl:value-of select="$uri"/>);
      </xsl:if>
    </xsl:if>

    <xsl:apply-templates select="xsp:attribute | xsp:logic[xsp:attribute]"/>

    this.contentHandler.startElement(
      <xsl:copy-of select="$uri"/>,
      <xsl:copy-of select="$name"/>,
      <xsl:copy-of select="$raw-name"/>,
      xspAttr
    );

    xspAttr.clear();

    <xsl:apply-templates select="node()[not(
      (namespace-uri(.) = $xsp-uri and local-name(.) = 'attribute') or
      (namespace-uri(.) = $xsp-uri and local-name(.) = 'logic' and ./xsp:attribute)
      )]"/>

    this.contentHandler.endElement(
      <xsl:copy-of select="$uri"/>,
      <xsl:copy-of select="$name"/>,
      <xsl:copy-of select="$raw-name"/>);

    <!-- Declare namespace defined by @uri and @prefix attribute -->
    <xsl:if test="$uri != '&quot;&quot;'">
      <xsl:if test="not($parent-element/namespace::*[local-name(.) = $prefix and string(.) = $uri])">
        this.contentHandler.endPrefixMapping(<xsl:value-of select="$prefix"/>);
      </xsl:if>
    </xsl:if>

    <xsl:for-each select="namespace::*">
      <xsl:variable name="ns-prefix" select="local-name(.)"/>
      <xsl:variable name="ns-uri" select="string(.)"/>
      <xsl:if test="not($parent-element/namespace::*[local-name(.) = $ns-prefix and string(.) = $ns-uri])">
        this.contentHandler.endPrefixMapping("<xsl:value-of select="local-name(.)"/>");
      </xsl:if>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="xsp:attribute">
    <xsl:variable name="uri">
      <xsl:call-template name="get-parameter">
        <xsl:with-param name="name">uri</xsl:with-param>
      </xsl:call-template>
    </xsl:variable>

    <xsl:variable name="prefix">
      <xsl:call-template name="get-parameter">
        <xsl:with-param name="name">prefix</xsl:with-param>
      </xsl:call-template>
    </xsl:variable>

    <xsl:variable name="name">
      <xsl:call-template name="get-parameter">
        <xsl:with-param name="name">name</xsl:with-param>
        <xsl:with-param name="required">true</xsl:with-param>
      </xsl:call-template>
    </xsl:variable>

    <xsl:variable name="raw-name">
      <xsl:if test="
        ($uri = '&quot;&quot;' and $prefix != '&quot;&quot;') or
        ($uri != '&quot;&quot;' and $prefix = '&quot;&quot;')
      ">
        <xsl:call-template name="error">
          <xsl:with-param name="message">[&lt;xsp:attribute&gt;]
Either both 'uri' and 'prefix' or none of them must be specified
          </xsl:with-param>
        </xsl:call-template>
      </xsl:if>

      <xsl:choose>
        <xsl:when test="$prefix = '&quot;&quot;'">
          <xsl:copy-of select="$name"/>
        </xsl:when>
        <xsl:otherwise> (<xsl:copy-of select="$prefix"/> + ":" + <xsl:copy-of select="$name"/>) </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="content">
      <xsl:for-each select="text()|xsp:expr|xsp:text">
        <xsl:if test="position() &gt; 1"> + </xsl:if>
        <xsl:choose>
          <xsl:when test="namespace-uri(.) = $xsp-uri and local-name(.) = 'expr'">
            String.valueOf(<xsl:value-of select="."/>)
          </xsl:when>
          <xsl:otherwise> "<xsl:value-of select="XSLTExtension:escape($extension,.)"/>" </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each>
      <xsl:if test="not(text()|xsp:expr|xsp:text)"> "" </xsl:if>
    </xsl:variable>

    xspAttr.addAttribute(
      <xsl:copy-of select="$uri"/>,
      <xsl:copy-of select="$name"/>,
      <xsl:copy-of select="$raw-name"/>,
      "CDATA",
      <xsl:value-of select="normalize-space($content)"/>
    );
  </xsl:template>

  <xsl:template match="xsp:expr">
    <xsl:choose>
      <xsl:when test="namespace-uri(..) = $xsp-uri and local-name(..) != 'content' and local-name(..) != 'element'">
        <!--
             Expression is nested inside another XSP tag:
             preserve its Java type
        -->
        (<xsl:value-of select="."/>)
      </xsl:when>
      <xsl:when test="string-length(.) = 0">
        <!-- Do nothing -->
      </xsl:when>
      <xsl:otherwise>
        <!-- Output the value as elements or character data depending on its type -->
        XSPObjectHelper.xspExpr(contentHandler, <xsl:value-of select="."/>);
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- FIXME: Is this _really_ necessary? -->
  <xsl:template match="xsp:content">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="xsp:logic">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="xsp:pi">
    <xsl:variable name="target">
      <xsl:call-template name="get-parameter">
        <xsl:with-param name="name">target</xsl:with-param>
      </xsl:call-template>
    </xsl:variable>

    <xsl:variable name="content">
      <xsl:for-each select="text()|xsp:expr">
        <xsl:choose>
          <xsl:when test="namespace-uri(.) = $xsp-uri and local-name(.) = 'expr'">
           String.valueOf(<xsl:value-of select="."/>)
          </xsl:when>
          <xsl:otherwise> "<xsl:value-of select="."/>" </xsl:otherwise>
        </xsl:choose>
        <xsl:text> + </xsl:text>
      </xsl:for-each>
      <xsl:text> "" </xsl:text>
    </xsl:variable>

    this.contentHandler.processingInstruction(
      <xsl:copy-of select="$target"/>,
      <xsl:value-of select="normalize-space($content)"/>
    );
  </xsl:template>

  <!-- FIXME: How to create comments in SAX? -->
  <xsl:template match="xsp:comment">
    this.comment("<xsl:value-of select="."/>");
  </xsl:template>


  <!-- $xsp-uri = 'http://apache.org/xsp' -->
  <xsl:template match="*[not(namespace-uri(.) = 'http://apache.org/xsp')]">
    <xsl:variable name="parent-element" select=".."/>
    <xsl:for-each select="namespace::*">
      <xsl:variable name="ns-prefix" select="local-name(.)"/>
      <xsl:variable name="ns-uri" select="string(.)"/>
      <xsl:if test="not($parent-element/namespace::*[local-name(.) = $ns-prefix and string(.) = $ns-uri])">
        this.contentHandler.startPrefixMapping(
          "<xsl:value-of select="local-name(.)"/>",
          "<xsl:value-of select="."/>"
        );
      </xsl:if>
    </xsl:for-each>

    <xsl:apply-templates select="@*"/>

    <xsl:apply-templates select="xsp:attribute | xsp:logic[xsp:attribute]"/>

    this.contentHandler.startElement(
      "<xsl:value-of select="namespace-uri(.)"/>",
      "<xsl:value-of select="local-name(.)"/>",
      "<xsl:value-of select="name(.)"/>",
      xspAttr
    );
    xspAttr.clear();

    <xsl:apply-templates select="node()[not(
        (namespace-uri(.) = $xsp-uri and local-name(.) = 'attribute') or
        (namespace-uri(.) = $xsp-uri and local-name(.) = 'logic' and ./xsp:attribute)
      )]"/>

    this.contentHandler.endElement(
      "<xsl:value-of select="namespace-uri(.)"/>",
      "<xsl:value-of select="local-name(.)"/>",
      "<xsl:value-of select="name(.)"/>"
    );

    <xsl:for-each select="namespace::*">
      <xsl:variable name="ns-prefix" select="local-name(.)"/>
      <xsl:variable name="ns-uri" select="string(.)"/>
      <xsl:if test="not($parent-element/namespace::*[local-name(.) = $ns-prefix and string(.) = $ns-uri])">
      this.contentHandler.endPrefixMapping(
        "<xsl:value-of select="local-name(.)"/>"
      );
      </xsl:if>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="@*">
    <!-- Filter out namespace declaration attributes -->
    <xsl:if test="not(starts-with(name(.), 'xmlns:'))">
    xspAttr.addAttribute(
      "<xsl:value-of select="namespace-uri(.)"/>",
      "<xsl:value-of select="local-name(.)"/>",
      "<xsl:value-of select="name(.)"/>",
      "CDATA",
      "<xsl:value-of select="XSLTExtension:escape($extension,.)"/>"
    );
    </xsl:if>
  </xsl:template>

  <xsl:template match="text()">
    <xsl:choose>
      <xsl:when test="namespace-uri(..) = $xsp-uri and (local-name(..) = 'logic' or local-name(..) = 'expr')">
        <xsl:value-of select="."/>
      </xsl:when>
      <xsl:otherwise>
        this.characters("<xsl:value-of select="XSLTExtension:escape($extension, .)"/>");
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="text()" mode="value">
    <xsl:value-of select="."/>
  </xsl:template>

  <xsl:template match="processing-instruction()">
    this.contentHandler.processingInstruction(
      "<xsl:value-of select="name()"/>",
      "<xsl:value-of select="."/>"
    );
  </xsl:template>

  <!-- Utility templates -->
  <xsl:template name="get-parameter">
    <xsl:param name="name"/>
    <xsl:param name="default"/>
    <xsl:param name="required">false</xsl:param>

    <xsl:choose>
      <xsl:when test="@*[name(.) = $name]">"<xsl:value-of select="@*[name(.) = $name]"/>"</xsl:when>
      <xsl:when test="xsp:param[@name = $name]">
        <xsl:call-template name="get-parameter-content">
          <xsl:with-param name="content"
                          select="(*[namespace-uri(.)=$xsp-uri and local-name(.) = 'param'])[@name = $name]"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:choose>
          <xsl:when test="string-length($default) = 0">
            <xsl:choose>
              <xsl:when test="$required = 'true'">
                <xsl:call-template name="error">
                  <xsl:with-param name="message">[Logicsheet processor]
Parameter '<xsl:value-of select="$name"/>' missing in dynamic tag &lt;<xsl:value-of select="name(.)"/>&gt;
                  </xsl:with-param>
                </xsl:call-template>
              </xsl:when>
              <xsl:otherwise>""</xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <xsl:otherwise><xsl:copy-of select="$default"/></xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="get-parameter-content">
    <xsl:param name="content"/>
    <xsl:choose>
      <xsl:when test="$content/*[namespace-uri(.)=$xsp-uri and local-name(.) != 'text']">
        <xsl:apply-templates select="$content/*[namespace-uri(.)=$xsp-uri and local-name(.) != 'text']"/>
      </xsl:when>
      <xsl:otherwise>"<xsl:value-of select="$content"/>"</xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="get-nested-content">
    <xsl:param name="content"/>
    <xsl:choose>
      <xsl:when test="$content/*">
        <xsl:apply-templates select="$content/*"/>
      </xsl:when>
      <xsl:otherwise>"<xsl:value-of select="$content"/>"</xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="error">
    <xsl:param name="message"/>
    <xsl:message terminate="yes"><xsl:value-of select="$message"/></xsl:message>
  </xsl:template>

  <!-- Ignored elements -->
  <xsl:template match="xsp:logicsheet|xsp:dependency|xsp:param"/>
</xsl:stylesheet>
