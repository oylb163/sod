<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="xml"/>
    <xsl:include href="copyAll.xslt"/>
    <xsl:template match="waveformArm">
        <waveformVectorArm>
            <xsl:apply-templates/>
        </waveformVectorArm>
    </xsl:template>
</xsl:stylesheet>
