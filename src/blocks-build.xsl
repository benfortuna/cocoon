<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

   <xsl:output method="xml" indent="yes" />

   <xsl:template match="/">
      <project default="compile" basedir="." name="blocks">
         <description>Autogenerated Ant build file that builds blocks.</description>

         <path id="classpath">
            <fileset dir="{string('${lib.core}')}">
               <include name="*.jar"/>
            </fileset>
            <fileset dir="{string('${lib.endorsed}')}">
               <include name="*.jar"/>
            </fileset>    
            <!-- Currently, we have no JVM dependent libraries               
              <fileset dir="{string('${lib.core}/jvm${target.vm}')}">
                 <include name="*.jar"/>
              </fileset>
            -->
            <fileset dir="{string('${lib.optional}')}">
               <include name="*.jar"/>
            </fileset>
            <fileset dir="{string('${build.blocks}')}">
               <include name="*.jar"/>
            </fileset>
            <path location="{string('${build.mocks}')}"/>
            <path location="{string('${build.dest}')}"/>
         </path>

         <path id="test.classpath">
            <fileset dir="{string('${tools.lib}')}">
               <include name="*.jar"/>
            </fileset>
         </path>

         <target name="init">
           <xsl:for-each select="module/project[contains(@name,'cocoon-block-')]">
             <xsl:variable name="block-name" select="substring-after(@name,'cocoon-block-')" />
             <condition property="unless.exclude.block.{$block-name}">
               <istrue value="{string('${exclude.block.')}{$block-name}{string('}')}"/>
             </condition> 
           </xsl:for-each>
         </target>

         <xsl:apply-templates select="module" />
      </project>
   </xsl:template>

   <xsl:template match="module">
      <target name="compile">
        <xsl:attribute name="depends">init<xsl:for-each select="project[contains(@name,'cocoon-block-')]"><xsl:text>,</xsl:text><xsl:value-of select="@name"/>-compile</xsl:for-each></xsl:attribute>
      </target>

      <target name="samples">
        <xsl:attribute name="depends">init<xsl:text>,</xsl:text>patch-samples<xsl:for-each select="project[contains(@name,'cocoon-block-')]"><xsl:text>,</xsl:text><xsl:value-of select="@name"/>-samples</xsl:for-each></xsl:attribute>
      </target>

      <target name="lib">
        <xsl:attribute name="depends">init<xsl:for-each select="project[contains(@name,'cocoon-block-')]"><xsl:text>,</xsl:text><xsl:value-of select="@name"/>-lib</xsl:for-each></xsl:attribute>
      </target>

      <target name="tests">
        <xsl:attribute name="depends">init<xsl:for-each select="project[contains(@name,'cocoon-block-')]"><xsl:text>,</xsl:text><xsl:value-of select="@name"/>-tests</xsl:for-each></xsl:attribute>
      </target>

      <!-- Check if javadocs have to be generated -->
      <target name="javadocs-check">
        <mkdir dir="{string('${build.javadocs}')}"/>
        <condition property="javadocs.notrequired" value="true">
          <or>
            <uptodate targetfile="{string('${build.javadocs}')}/packages.html" >
              <srcfiles dir="{string('${java}')}" includes="**/*.java,**/package.html"/>
              <srcfiles dir="{string('${deprecated.src}')}" includes="**/*.java,**/package.html"/>
              <xsl:for-each select="project[contains(@name,'cocoon-block-')]">
                <srcfiles dir="{string('${blocks}')}/{substring-after(@name,'cocoon-block-')}/java" includes="**/*.java,**/package.html"/>             
              </xsl:for-each>
            </uptodate>
            <istrue value="{string('${unless.exclude.javadocs}')}"/>
          </or>
        </condition>
      </target>

      <!-- Creates Javadocs -->
      <target name="javadocs" 
              unless="javadocs.notrequired">
        <xsl:attribute name="depends">init, javadocs-check<xsl:for-each select="project[contains(@name,'cocoon-block-')]"><xsl:text>,</xsl:text><xsl:value-of select="substring-after(@name,'cocoon-block-')"/>-prepare</xsl:for-each></xsl:attribute>

        <condition property="javadoc.additionalparam" value="-breakiterator -tag todo:all:Todo:">
          <equals arg1="1.4" arg2="{string('${ant.java.version}')}"/>
        </condition>
        <condition property="javadoc.additionalparam" value="">
          <not><equals arg1="1.4" arg2="{string('${ant.java.version}')}"/></not>
        </condition>

        <javadoc destdir="{string('${build.javadocs}')}"
	             author="true"
	             version="true"
	             use="true"
	             noindex="false"
	             splitindex="true"
	             windowtitle="{string('${Name}')} API {string('${version}')} [{string('${TODAY}')}]"
	             doctitle="{string('${Name}')} API {string('${version}')}"
	             bottom="Copyright &#169; {string('${year}')} Apache Software Foundation. All Rights Reserved."
	             stylesheetfile="{string('${resources.javadoc}')}/javadoc.css"
	             useexternalfile="yes"
	             additionalparam="{string('${javadoc.additionalparam}')}"
	             maxmemory="128m">
		
	      <link offline="true" href="http://avalon.apache.org/api"                  packagelistloc="${resources.javadoc}/avalon"/>
	      <link offline="true" href="http://xml.apache.org/xerces2-j/javadocs/api"  packagelistloc="${resources.javadoc}/xerces"/>
	      <link offline="true" href="http://xml.apache.org/xalan-j/apidocs"         packagelistloc="${resources.javadoc}/xalan"/>
	      <link offline="true" href="http://java.sun.com/j2se/1.4.1/docs/api"       packagelistloc="${resources.javadoc}/j2se"/>
	      <link offline="true" href="http://java.sun.com/j2ee/sdk_1.3/techdocs/api" packagelistloc="${resources.javadoc}/j2ee"/>
		
	      <packageset dir="{string('${java}')}">
	        <include name="**"/>
	      </packageset>
	      <packageset dir="{string('${deprecated.src}')}">
	        <include name="**"/>
	      </packageset>
          <xsl:for-each select="project[contains(@name,'cocoon-block-')]">
            <packageset dir="{string('${blocks}')}/{substring-after(@name,'cocoon-block-')}/java">
  	          <include name="**"/>
            </packageset>
          </xsl:for-each>
          <classpath refid="classpath"/>
          <xsl:for-each select="project[contains(@name,'cocoon-block-')]">
	        <classpath refid="{substring-after(@name,'cocoon-block-')}.classpath"/>
          </xsl:for-each>
	    </javadoc>
      </target>

      <xsl:apply-templates select="project[contains(@name,'-block')]" />

      <target name="patch-roles" depends="init">
         <xpatch file="{string('${build.dest}/org/apache/cocoon/cocoon.roles')}"
                 srcdir="{string('${blocks}')}">
            <xsl:for-each select="project[contains(@name,'cocoon-block-')]">
               <xsl:variable name="block-name" select="substring-after(@name,'cocoon-block-')"/>
               <include name="{$block-name}/conf/**/*.xroles" unless="unless.exclude.block.{$block-name}"/>
            </xsl:for-each>
         </xpatch>
      </target>

      <target name="patch-conf" depends="init">
         <xpatch file="{string('${build.webapp}')}/sitemap.xmap"
                 srcdir="{string('${blocks}')}">
            <xsl:for-each select="project[contains(@name,'cocoon-block-')]">
               <xsl:variable name="block-name" select="substring-after(@name,'cocoon-block-')"/>
               <include name="{$block-name}/conf/**/*.xmap" unless="unless.exclude.block.{$block-name}"/>
               <include name="{$block-name}/conf/**/*.xpipe" unless="unless.exclude.block.{$block-name}"/>
            </xsl:for-each>
         </xpatch>
         <!-- This is much slower, but preserves the dependencies -->
         <xsl:for-each select="project[contains(@name,'cocoon-block-')]">
            <xsl:variable name="block-name" select="substring-after(@name,'cocoon-block-')"/>
            <xpatch file="{string('${build.webapp}')}/WEB-INF/cocoon.xconf"
                 srcdir="{string('${blocks}')}"
                 addcomments="true">
              <include name="{$block-name}/conf/**/*.xconf" unless="unless.exclude.block.{$block-name}"/>
            </xpatch>
         </xsl:for-each>
         <xpatch file="{string('${build.webapp}')}/WEB-INF/logkit.xconf"
                 srcdir="{string('${blocks}')}">
            <xsl:for-each select="project[contains(@name,'cocoon-block-')]">
               <xsl:variable name="block-name" select="substring-after(@name,'cocoon-block-')"/>
               <include name="{$block-name}/conf/**/*.xlog" unless="unless.exclude.block.{$block-name}"/>
            </xsl:for-each>
         </xpatch>
         <xpatch file="{string('${build.webapp}')}/WEB-INF/web.xml"
                 srcdir="{string('${blocks}')}">
            <xsl:for-each select="project[contains(@name,'cocoon-block-')]">
               <xsl:variable name="block-name" select="substring-after(@name,'cocoon-block-')"/>
               <include name="{$block-name}/conf/**/*.xweb" unless="unless.exclude.block.{$block-name}"/>
            </xsl:for-each>
         </xpatch>
      </target>

      <target name="patch-samples" depends="init">
         <xpatch file="{string('${build.webapp}')}/samples/block-samples.xml"
                 srcdir="{string('${blocks}')}">
            <xsl:for-each select="project[contains(@name,'cocoon-block-')]">
               <xsl:variable name="block-name" select="substring-after(@name,'cocoon-block-')"/>
               <include name="{$block-name}/conf/**/*.xsamples" unless="unless.exclude.block.{$block-name}"/>
            </xsl:for-each>
         </xpatch>
         <xpatch file="{string('${build.webapp}')}/samples/sitemap.xmap"
                 srcdir="{string('${blocks}')}">
            <xsl:for-each select="project[contains(@name,'cocoon-block-')]">
               <xsl:variable name="block-name" select="substring-after(@name,'cocoon-block-')"/>
               <include name="{$block-name}/conf/**/*.samplesxpipe" unless="unless.exclude.block.{$block-name}"/>
            </xsl:for-each>
         </xpatch>
         <xpatch file="{string('${build.webapp}')}/WEB-INF/cocoon.xconf"
                 srcdir="{string('${blocks}')}">
            <xsl:for-each select="project[contains(@name,'cocoon-block-')]">
               <xsl:variable name="block-name" select="substring-after(@name,'cocoon-block-')"/>
               <include name="{$block-name}/conf/**/*.samplesxconf" unless="unless.exclude.block.{$block-name}"/>
            </xsl:for-each>
         </xpatch>
      </target>

   </xsl:template>

   <xsl:template match="project">
      <xsl:variable name="block-name" select="substring-after(@name,'cocoon-block-')" />

      <target name="{@name}-excluded" if="exclude.block.{$block-name}">
           <echo message="-----------------------------------------------"/>
           <echo message="ATTENTION: {$block-name} is excluded from the build."/>
           <echo message="-----------------------------------------------"/>
      </target>
      
      <target name="{@name}" unless="unless.exclude.block.{$block-name}"/>
      
      <target name="{@name}-compile" unless="unless.exclude.block.{$block-name}">
         <xsl:if test="depend">
            <xsl:attribute name="depends"><xsl:value-of select="@name"/>,<xsl:value-of select="@name"/>-excluded<xsl:for-each select="depend[contains(@project,'cocoon-block-')]"><xsl:text>,</xsl:text><xsl:value-of select="@project"/>-compile</xsl:for-each></xsl:attribute>
         </xsl:if>

         <!-- Test if this block has special build -->
         <available property="{$block-name}.has.build" file="{string('${blocks}')}/{$block-name}/build.xml"/>

         <!-- Test if this block has mocks -->
         <available property="{$block-name}.has.mocks" type="dir" file="{string('${blocks}')}/{$block-name}/mocks/"/>

         <xsl:if test="@status='unstable'">
           <echo message="-----------------------------------------------"/>
           <echo message="ATTENTION: {$block-name} is marked unstable."/>
           <echo message="It should be considered alpha quality"/>
           <echo message="which means that its API might change without notice."/>
           <echo message="-----------------------------------------------"/>
         </xsl:if>

         <antcall target="{$block-name}-compile"/>
      </target>

      <target name="{@name}-samples" unless="unless.exclude.block.{$block-name}">
         <xsl:if test="depend">
            <xsl:attribute name="depends"><xsl:value-of select="@name"/><xsl:for-each select="depend[contains(@project,'cocoon-block-')]"><xsl:text>,</xsl:text><xsl:value-of select="@project"/>-samples</xsl:for-each></xsl:attribute>
         </xsl:if>

         <!-- Test if this block has samples -->
         <available property="{$block-name}.has.samples" file="{string('${blocks}')}/{$block-name}/samples/sitemap.xmap"/>

         <antcall target="{$block-name}-samples"/>
      </target>
      
      <target name="{@name}-lib" unless="unless.exclude.block.{$block-name}">
         <xsl:if test="depend">
            <xsl:attribute name="depends"><xsl:value-of select="@name"/><xsl:for-each select="depend[contains(@project,'cocoon-block-')]"><xsl:text>,</xsl:text><xsl:value-of select="@project"/>-lib</xsl:for-each></xsl:attribute>
         </xsl:if>

         <!-- Test if this block has libraries -->
         <available property="{$block-name}.has.lib" type="dir">
             <xsl:attribute name="file">${blocks}/<xsl:value-of select="$block-name"/>/lib/</xsl:attribute>
         </available>

         <!-- Test if this block has global WEB-INF files -->
         <available property="{$block-name}.has.webinf" type="dir">
             <xsl:attribute name="file">${blocks}/<xsl:value-of select="$block-name"/>/WEB-INF/</xsl:attribute>
         </available>

         <antcall target="{$block-name}-lib"/>
         <antcall target="{$block-name}-webinf"/>
      </target>

      <target name="{$block-name}-prepare">

         <!-- Test if this block has mocks -->
         <available property="{$block-name}.has.mocks" type="dir" file="{string('${blocks}')}/{$block-name}/mocks/"/>

         <mkdir dir="{string('${build.blocks}')}/{$block-name}/dest"/>

         <mkdir dir="{string('${build.blocks}')}/{$block-name}/conf"/>
         <copy filtering="on" todir="{string('${build.blocks}')}/{$block-name}/conf">
            <fileset dir="{string('${blocks}')}/{$block-name}/conf">
               <include name="**/*.x*" />
            </fileset>
         </copy>

         <path id="{$block-name}.classpath">
            <path refid="classpath"/>
            <fileset dir="{string('${blocks}')}/{$block-name}/lib">
               <include name="*.jar"/>
            </fileset>
            <pathelement location="{string('${build.blocks}')}/{$block-name}/mocks"/>
         </path>
      </target>

      <target name="{$block-name}-compile" depends="{$block-name}-build,{$block-name}-prepare,{$block-name}-mocks">

         <copy filtering="on" todir="{string('${build.blocks}')}/{$block-name}/dest">
            <fileset dir="{string('${blocks}')}/{$block-name}/java">
               <include name="**/*.xsl"/>
               <include name="**/*.js"/>
            </fileset>
         </copy>

         <copy filtering="off" todir="{string('${build.blocks}')}/{$block-name}/dest">
            <fileset dir="{string('${blocks}')}/{$block-name}/java">
               <include name="**/Manifest.mf" />
               <include name="META-INF/**" />
            </fileset>
         </copy>

         <!-- This is a little bit tricky:
              As the javac task checks, if a src directory is available and
              stops if its not available, we use the following property
              to either point to a jdk dependent directory or - if not
              available - to the usual java source directory.
              If someone knows a better solution...
         -->
         <condition property="dependend.vm" value="{string('${target.vm}')}">
	          <available file="{string('${blocks}')}/{$block-name}/java{string('${target.vm}')}"/>
         </condition>
         <condition property="dependend.vm" value="">
            <not>
	             <available file="{string('${blocks}')}/{$block-name}/java{string('${target.vm}')}"/>
            </not>
         </condition>

         <javac destdir="{string('${build.blocks}')}/{$block-name}/dest"
                debug="{string('${compiler.debug}')}"
                optimize="{string('${compiler.optimize}')}"
                deprecation="{string('${compiler.deprecation}')}"
                target="{string('${target.vm}')}"
                nowarn="{string('${compiler.nowarn}')}"
                compiler="{string('${compiler}')}">
            <src path="{string('${blocks}')}/{$block-name}/java"/>
            <src path="{string('${blocks}')}/{$block-name}/java{string('${dependend.vm}')}"/>
            <classpath refid="{$block-name}.classpath" />
            <exclude name="**/samples/**/*.java"/> 
         </javac>
         
         <jar jarfile="{string('${build.blocks}')}/{$block-name}-block.jar">
            <fileset dir="{string('${build.blocks}')}/{$block-name}/dest">
               <include name="org/**"/>
               <include name="META-INF/**"/>
            </fileset>
         </jar>

         <!-- exclude sample classes from the block package -->
         <mkdir dir="{string('${build.blocks}')}/{$block-name}/samples"/>
         <javac destdir="{string('${build.blocks}')}/{$block-name}/samples"
                debug="{string('${compiler.debug}')}"
                optimize="{string('${compiler.optimize}')}"
                deprecation="{string('${compiler.deprecation}')}"
                target="{string('${target.vm}')}"
                nowarn="{string('${compiler.nowarn}')}"
                compiler="{string('${compiler}')}">
            <src path="{string('${blocks}')}/{$block-name}/java"/>
            <src path="{string('${blocks}')}/{$block-name}/java{string('${dependend.vm}')}"/>
            <classpath refid="{$block-name}.classpath" />
            <include name="**/samples/**/*.java"/>
         </javac>
      </target>

      <target name="{$block-name}-build" if="{$block-name}.has.build">
         <ant inheritAll="true"
              inheritRefs="false"
              target="main"
              antfile="{string('${blocks}')}/{$block-name}/build.xml">
            <property name="block.dir" value="{string('${blocks}')}/{$block-name}"/>
         </ant>
      </target>

      <target name="{$block-name}-mocks" depends="{$block-name}-prepare" if="{$block-name}.has.mocks">

         <mkdir dir="{string('${build.blocks}')}/{$block-name}/mocks"/>

         <javac srcdir="{string('${blocks}')}/{$block-name}/mocks"
                destdir="{string('${build.blocks}')}/{$block-name}/mocks"
                debug="{string('${compiler.debug}')}"
                optimize="{string('${compiler.optimize}')}"
                deprecation="{string('${compiler.deprecation}')}"
                target="{string('${target.vm}')}"
                nowarn="{string('${compiler.nowarn}')}"
                compiler="{string('${compiler}')}">
            <classpath refid="{$block-name}.classpath" />
         </javac>
      </target>

      <target name="{$block-name}-lib" if="{$block-name}.has.lib">
         <copy filtering="off" todir="{string('${build.webapp.lib}')}">
            <fileset dir="{string('${blocks}')}/{$block-name}/lib">
               <include name="*.jar"/>
            </fileset>
         </copy>
      </target>

      <target name="{$block-name}-webinf" if="{$block-name}.has.webinf">
         <copy filtering="on" todir="{string('${build.webapp.webinf}')}">
            <fileset dir="{string('${blocks}')}/{$block-name}/WEB-INF/">
               <include name="**"/>
            </fileset>
         </copy>
      </target>

      <target name="{$block-name}-samples" if="{$block-name}.has.samples">
         <copy filtering="on" todir="{string('${build.webapp}')}/samples/{$block-name}">
            <fileset dir="{string('${blocks}')}/{$block-name}/samples"/>
         </copy>

         <!-- copy sample classes -->
         <copy todir="{string('${build.webapp.classes}')}" filtering="off">
           <fileset dir="{string('${build.blocks}')}/{$block-name}/samples"/>
         </copy> 
      </target>

      <target name="{@name}-tests" unless="unless.exclude.block.{$block-name}">
         <xsl:if test="depend">
            <xsl:attribute name="depends"><xsl:value-of select="@name"/><xsl:for-each select="depend[contains(@project,'cocoon-block-')]"><xsl:text>,</xsl:text><xsl:value-of
select="@project"/>-compile</xsl:for-each></xsl:attribute>
         </xsl:if>

         <!-- Test if this block has tests -->
         <available property="{$block-name}.has.tests" file="{string('${blocks}')}/{$block-name}/test"/>

         <antcall target="{$block-name}-tests"/>
      </target>

      <target name="{$block-name}-tests" depends="{$block-name}-compile" if="{$block-name}.has.tests">
         <mkdir dir="{string('${build.blocks}')}/{$block-name}/test"/>

         <copy todir="{string('${build.blocks}')}/{$block-name}/test" filtering="on">
            <fileset dir="{string('${blocks}')}/{$block-name}/test" excludes="**/*.java"/>
         </copy>

         <javac srcdir="{string('${blocks}')}/{$block-name}/test"
                destdir="{string('${build.blocks}')}/{$block-name}/test"
                fork="true"
                debug="{string('${compiler.debug}')}"
                optimize="{string('${compiler.optimize}')}"
                deprecation="{string('${compiler.deprecation}')}"
                target="{string('${target.vm}')}"
                nowarn="{string('${compiler.nowarn}')}">
            <classpath>
               <path refid="test.classpath"/>
               <path refid="{$block-name}.classpath"/>
               <pathelement location="{string('${build.test}')}"/>
            </classpath>
         </javac>

         <junit printsummary="yes" haltonfailure="yes" fork="yes">
            <classpath>
               <path refid="test.classpath"/>
               <path refid="{$block-name}.classpath"/>
               <pathelement location="{string('${build.test}')}"/>
               <pathelement location="{string('${build.blocks}')}/{$block-name}/test"/>
            </classpath>
            <formatter type="plain" usefile="no"/>
            <batchtest>
               <fileset dir="{string('${build.blocks}')}/{$block-name}/test">
                  <include name="**/*TestCase.class"/>
                  <include name="**/*Test.class"/>
                  <exclude name="**/AllTest.class"/>
                  <exclude name="**/*$$*Test.class"/>
                  <exclude name="**/Abstract*.class"/>
               </fileset>
            </batchtest>
         </junit>
      </target>
   </xsl:template>
</xsl:stylesheet>
