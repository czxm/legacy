<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" exclude-result-prefixes="xsi xsl" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <xsl:output method="html" encoding="UTF-8" indent="yes"></xsl:output>
    <xsl:template match="tasklets">
        <table id="details">
            <xsl:for-each select="tasklet">
                <tr>
                    <td>
                        <xsl:value-of select="@id"></xsl:value-of>
                    </td>
                    <td>
                        <xsl:if test="count(items/item) &gt; 0">
                            <table id="details">
                                <tbody>
                                    <tr>
                                        <th>Item</th>
                                        <th>Result</th>
                                        <th>Volume</th>
                                        <th>Path</th>
                                        <th>Machine</th>
                                        <th>FailureMessage</th>
                                    </tr>
                                    <xsl:for-each select="items/item">
                                        <tr>
                                            <td>
                                                <a href="{url}">
                                                    <xsl:value-of select="value"></xsl:value-of>
                                                </a>
                                            </td>
                                            <td>
                                                <xsl:value-of select="result"></xsl:value-of>
                                            </td>
                                            <td>
                                                <xsl:value-of select="path/@volume"></xsl:value-of>
                                            </td>
                                            <td>
                                                <xsl:value-of select="path"></xsl:value-of>
                                            </td>
                                            <td>
                                                <xsl:value-of select="host"></xsl:value-of>
                                            </td>
                                            <td>
                                                <xsl:if test="failure">
                                                    <xsl:value-of select="failure"></xsl:value-of>
                                                </xsl:if>
                                            </td>
                                        </tr>
                                    </xsl:for-each>
                                </tbody>
                            </table>
                        </xsl:if>
                    </td>
                </tr>
            </xsl:for-each>
        </table>
    </xsl:template>
</xsl:stylesheet>

