<?xml version="1.0" encoding="iso-8859-1"?>

<!--
  Copyright 1999-2004 The Apache Software Foundation

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

<!-- convert pages generated by our views to HTML -->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:variable name="title" select="//page/title"/>

    <!-- by default copy everything: our pages are almost HTML -->
    <xsl:template match="*">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <!-- HTML page skeleton -->
    <xsl:template match="page">
        <html>
            <head>
                <title><xsl:value-of select="$title"/></title>
                <link rel="stylesheet" type="text/css" href="css/tour.css"/>
                <meta content="text/html; charset=windows-1252" http-equiv="Content-Type"/>
                <meta HTTP-EQUIV="Cache-Control" CONTENT="max-age=0"/>
                <meta HTTP-EQUIV="Cache-Control" CONTENT="no-cache"/>
                <meta http-equiv="expires" content="0"/>
                <meta HTTP-EQUIV="Expires" CONTENT="Tue, 01 Jan 1980 1:00:00 GMT"/>
                <meta HTTP-EQUIV="Pragma" CONTENT="no-cache"/>
            </head>

            <body bgcolor="white">
                <xsl:apply-templates select="navigation/*"/>
                <h1><xsl:value-of select="$title"/></h1>
                <xsl:apply-templates select="content/*"/>
            </body>
        </html>
    </xsl:template>

    <!-- generate alternating styles for table rows in lists -->
    <xsl:template match="tr[@class='listRow'][position() mod 2 = 1]">
        <xsl:copy>
            <xsl:copy-of select="@*[not(self::class)]"/>
            <xsl:attribute name="class">oddRow</xsl:attribute>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <!-- raw XML source, for example -->
    <xsl:template match="source">
        <div style="background: #b9d3ee; border: thin; border-color: black; border-style: solid; padding-left: 0.8em;
                 padding-right: 0.8em; padding-top: 0px; padding-bottom: 0px; margin: 0.5ex 0px; clear: both;">
            <textarea name="context" cols="80" rows="20" readonly="true">
                <xsl:apply-templates/>
            </textarea>
        </div>
    </xsl:template>

</xsl:stylesheet>