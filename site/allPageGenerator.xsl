<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
        xmlns:redirect="org.apache.xalan.xslt.extensions.Redirect" extension-element-prefixes="redirect">
        <xsl:output method="html"/>
        <xsl:include href="pageGenerator.xsl"/>
        <xsl:template match="/pages">
                <xsl:for-each select="page">
                        <xsl:if test="contains(source/text(), '.xml')">
							<redirect:write select="concat(../baseDirectory/text(), destination/text())">
                            	    <xsl:apply-templates select="document(source/text())"/>
	                        </redirect:write>
						</xsl:if>
                </xsl:for-each>
        </xsl:template>
</xsl:stylesheet>
