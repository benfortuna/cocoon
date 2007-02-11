<?xml version="1.0"?>

<!-- $Id: changeuser.xsl,v 1.2 2003/05/06 14:12:55 vgritsenko Exp $ -->

<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="password">
	<xsl:choose>
		<xsl:when test="normalize-space(new) = normalize-space(old)">
			<password><xsl:value-of select="normalize-space(old)"/></password>
		</xsl:when>
		<xsl:otherwise>
			<password><xsl:value-of select="normalize-space(hashed)"/></password>
		</xsl:otherwise>
	</xsl:choose>
</xsl:template>

<!-- Copy all and apply templates -->
<xsl:template match="@*|node()">
	<xsl:copy>
		<xsl:apply-templates select="@*|node()" />
	</xsl:copy>
</xsl:template>

</xsl:stylesheet>