<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	version="1.0">
	<xsl:output method="xml"/>
	
	<xsl:template match="/">
		<groups>
			<xsl:apply-templates select="//testsuites"/>
		</groups>
	</xsl:template>
	
	<xsl:template match="testsuites">
		<group>
			<xsl:attribute name="name">
				<xsl:value-of select="@name"/>
			</xsl:attribute>
		</group>
	</xsl:template>
</xsl:stylesheet>