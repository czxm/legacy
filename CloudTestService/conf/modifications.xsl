<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" exclude-result-prefixes="xsi xsl" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <xsl:output method="html" encoding="UTF-8" indent="yes"></xsl:output>
    <xsl:template match="/">
        <!-- modifications section -->
        <xsl:if test="modifications/modification">
        	<xsl:variable name="modification.list" select="modifications/modification"/>
        	<h2>
				<xsl:text>Modifications (</xsl:text>
				<xsl:value-of select="count($modification.list)"/>
				<xsl:text>)</xsl:text>
			</h2>
			<table id="sourceModifications" class="modifications" width="95%">
				<col class="modification-info"/>
				<col class="modification-files"/>
				
				<xsl:for-each select="$modification.list">
					<xsl:call-template name="display-file-info">
						<xsl:with-param name="modification" select="."/>
					</xsl:call-template>
				</xsl:for-each>
			</table>
			<hr/>
        </xsl:if>
    </xsl:template>
	<xsl:template name="display-file-info">
		<xsl:param name="modification"/>
		<tbody>
			<tr>
				<td class="modification-info" style="with:10em">
					<table with="100%" class="modification-info">
						<col with="100%"/>
						<tbody>
							<tr>
								<td>
									<xsl:value-of select="$modification/@revision"/>
								</td>
							</tr>
							<tr>
								<td>
									<xsl:value-of select="$modification/author"/>
								</td>
							</tr>
							<tr>
								<td>
									<xsl:value-of select="$modification/date"/>
								</td>
							</tr>
						</tbody>
					</table>
				</td>
				
				<td class="modification-files">
					<p class="modification-comment">
						<xsl:call-template name="newlineToHTML">
							<xsl:with-param name="line">
								<xsl:value-of select="msg"/>
							</xsl:with-param>
						</xsl:call-template>
					</p>
					<table width="100%" class="modification-files">
						<col class="modification-action"/>
						<col class="modification-file"/>
						<tbody>
							<xsl:for-each select="$modification/paths/path">
								<tr>
									<xsl:if test="position() mod 2 = 0">
										<xsl:attribute name="class">odd-row</xsl:attribute>
									</xsl:if>
									<xsl:if test="position() mod 2 != 0">
										<xsl:attribute name="class">even-row</xsl:attribute>
									</xsl:if>
									<td class="modification-action" style="width:5em">
										<xsl:value-of select="@action"/>
									</td>
									<td class="modifcation-file">
										<xsl:value-of select="."/>
									</td>
								</tr>
							</xsl:for-each>
						</tbody>
					</table>
				</td>
			</tr>
		</tbody>
	</xsl:template>
    <xsl:template name="newlineToHTML">
        <xsl:param name="line"></xsl:param>
        <xsl:choose>
            <xsl:when test="contains($line, '&#xA;')">
                <xsl:value-of select="substring-before($line, '&#xA;')"></xsl:value-of>
                <br></br>
                <xsl:call-template name="newlineToHTML">
                    <xsl:with-param name="line">
                        <xsl:value-of select="substring-after($line, '&#xA;')"></xsl:value-of>
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$line"></xsl:value-of>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>

