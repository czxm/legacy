<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="html" encoding="UTF-8" indent="yes"></xsl:output>
    <xsl:decimal-format decimal-separator="." grouping-separator=","></xsl:decimal-format>
    <xsl:param name="groupName"></xsl:param>
    <xsl:param name="detailType"></xsl:param>
    <xsl:param name="footnote"></xsl:param>
    <xsl:template match="/">
<!-- 
		<html>
		<head>
			<title>Test Detail</title>
			<script type="text/javascript" language="JavaScript">
				function toggleDivVisibility(_div){
				    if(_div.style.display=="none"){
				        _div.style.display="block";
				    }else{
				        _div.style.display="none";
				    }
				}
			</script>
			<link rel="stylesheet" type="text/css" href="cruisecontrol.css"/>
		</head>
	-->
        <body>
            <xsl:choose>
                <xsl:when test="($groupName='')">
                    <h2>Details for All TestGroups</h2>
                </xsl:when>
                <xsl:otherwise>
                    <h2>Details for <xsl:value-of select="$groupName"></xsl:value-of>
                    </h2>
                </xsl:otherwise>
            </xsl:choose>
            <table border="0" cellpadding="0" cellspacing="0" width="95%">
                <colgroup>
                    <col width="10%"></col>
                    <col width="45%"></col>
                    <col width="25%"></col>
                    <col width="10%"></col>
                    <col width="10%"></col>
                </colgroup>
                <tr class="unittests-sectionheader" valign="top" align="left">
                    <th colspan="3">Name</th>
                    <th>Status</th>
                    <th nowrap="nowrap">Time(s)</th>
                </tr>
                <xsl:choose>
                    <xsl:when test="($groupName='')">
                        <xsl:apply-templates select="//testsuite">
                            <xsl:sort select="count(testcase/error)" data-type="number" order="descending"></xsl:sort>
                            <xsl:sort select="count(testcase/failure)" data-type="number" order="descending"></xsl:sort>
                            <xsl:sort select="@package"></xsl:sort>
                            <xsl:sort select="@name"></xsl:sort>
                            <xsl:with-param name="type" select="$detailType"></xsl:with-param>
                        </xsl:apply-templates>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:apply-templates select="//testsuites[@name=$groupName]/testsuite">
                            <xsl:with-param name="type" select="$detailType"></xsl:with-param>
                        </xsl:apply-templates>
                    </xsl:otherwise>
                </xsl:choose>
            </table>
            <hr></hr>
            <p>
                <xsl:if test="$footnote">
                    <em>
                        <xsl:value-of select="$footnote"></xsl:value-of>
                    </em>
                </xsl:if>
            </p>
        </body>
<!-- </html> -->
    </xsl:template>
    <xsl:template match="testsuite">
        <xsl:param name="type"></xsl:param>
        <tr>
            <xsl:attribute name="class">
                <xsl:choose>
                    <xsl:when test="testcase/error">unittests-error-title</xsl:when>
                    <xsl:when test="testcase/failure">unittests-failure-title</xsl:when>
                    <xsl:otherwise>unittests-title</xsl:otherwise>
                </xsl:choose>
            </xsl:attribute>
<!-- 
			<td colspan="5"><xsl:value-of select="concat(@package, '.', @name)"/></td>
			-->
            <td colspan="5">
                <xsl:value-of select="./@name"></xsl:value-of>
            </td>
        </tr>
        <xsl:choose>
            <xsl:when test="$type=''">
                <xsl:apply-templates select="testcase"></xsl:apply-templates>
<!--
				<xsl:apply-templates select="current()" mode="details"/>
				-->
            </xsl:when>
            <xsl:otherwise>
                <xsl:if test="$type='tests'">
                    <xsl:apply-templates select="testcase"></xsl:apply-templates>
                </xsl:if>
                <xsl:if test="$type='errors'">
                    <xsl:apply-templates select="testcase[error]"></xsl:apply-templates>
                </xsl:if>
                <xsl:if test="$type='failures'">
                    <xsl:apply-templates select="testcase[failure]"></xsl:apply-templates>
                </xsl:if>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template match="testcase">
        <tr>
            <xsl:attribute name="class">
                <xsl:choose>
                    <xsl:when test="error">unittests-error</xsl:when>
                    <xsl:when test="failure">unittests-failure</xsl:when>
                    <xsl:otherwise>unittests-data</xsl:otherwise>
                </xsl:choose>
            </xsl:attribute>
            <xsl:if test="position() mod 2 =0">
                <xsl:attribute name="bgcolor">#EEEEEE</xsl:attribute>
            </xsl:if>
            <td> </td>
            <td colspan="2">
                <xsl:value-of select="concat(@classname, '.', @name)"></xsl:value-of>
            </td>
            <td>
                <xsl:choose>
                    <xsl:when test="error">
<!-- AVT -->
                        <a href="javascript:void(0)" onclick="toggleDivVisibility(document.getElementById('{concat('error.', ../@package, '.', ../@name, '.', @name)}'))">Error »</a>
                    </xsl:when>
                    <xsl:when test="failure">
<!-- AVT -->
                        <a href="javascript:void(0)" onclick="toggleDivVisibility(document.getElementById('{concat('failure.', ../@package, '.', ../@name, '.', @name)}'))">Failure »</a>
                    </xsl:when>
                </xsl:choose>
            </td>
            <xsl:choose>
                <xsl:when test="not(error|failure)">
                    <td>
                        <xsl:value-of select="format-number(@time, '0.000')"></xsl:value-of>
                    </td>
                </xsl:when>
                <xsl:otherwise>
                    <td></td>
                </xsl:otherwise>
            </xsl:choose>
        </tr>
<!-- error division -->
        <xsl:if test="error">
            <tr>
                <td colspan="5">
                    <div id="{concat('error.', ../@package, '.', ../@name, '.', @name)}" class="testresults-output-div" style="display:none;">
                        <span style="font-weight:bold">Error:</span>
                        <br></br>
                        <xsl:apply-templates select="error/text()" mode="newline-to-br"></xsl:apply-templates>
                    </div>
                </td>
            </tr>
        </xsl:if>
<!-- failure division -->
        <xsl:if test="failure">
            <tr>
                <td colspan="5">
                    <div id="{concat('failure.', ../@package, '.', ../@name, '.', @name)}" class="testresults-output-div" style="display:none;">
                        <span style="font-weight:bold">Failure:</span>
                        <br></br>
                        <xsl:apply-templates select="failure/text()" mode="newline-to-br"></xsl:apply-templates>
                    </div>
                </td>
            </tr>
        </xsl:if>
    </xsl:template>
    <xsl:template match="testsuite" mode="details">
        <tr class="unittests-data">
            <td colspan="2">
                <xsl:if test="count(properties/property) &gt; 0">
                    <a href="javascript:void(0)" onclick="toggleDivVisibility(document.getElementById('{concat('properties.', @package, '.', @name)}'))">Properties »</a>
                </xsl:if>
				 
			</td>
            <td colspan="2">
                <xsl:if test="system-out/text()">
                    <a href="javascript:void(0)" onclick="toggleDivVisibility(document.getElementById('{concat('system_out.', @package, '.', @name)}'))">System.out »</a>
                </xsl:if>
				 
			</td>
            <td colspan="2">
                <xsl:if test="system-err/text()">
                    <a href="javascript:void(0)" onclick="toggleDivVisibility(document.getElementById('{concat('system_err.', @package, '.', @name)}'))">System.err »</a>
                </xsl:if>
				 
			</td>
        </tr>
        <tr>
            <td colspan="5">
                <xsl:apply-templates select="system-err" mode="system-err-div">
                    <xsl:with-param name="div-id" select="concat('system_err.', @package, '.', @name)"></xsl:with-param>
                </xsl:apply-templates>
                <xsl:apply-templates select="system-out" mode="system-out-div">
                    <xsl:with-param name="div-id" select="concat('system_out.', @package, '.', @name)"></xsl:with-param>
                </xsl:apply-templates>
                <xsl:apply-templates select="properties" mode="properties-div">
                    <xsl:with-param name="div-id" select="concat('properties.', @package, '.', @name)"></xsl:with-param>
                </xsl:apply-templates>
				 
			</td>
        </tr>
    </xsl:template>
    <xsl:template match="system-out" mode="system-out-div">
        <xsl:param name="div-id"></xsl:param>
        <div id="{$div-id}" class="testresults-output-div" style="display:none;">
            <span style="font-weight:bold">System out:</span>
            <br></br>
            <xsl:apply-templates select="current()" mode="newline-to-br"></xsl:apply-templates>
        </div>
    </xsl:template>
    <xsl:template match="system-err" mode="system-err-div">
        <xsl:param name="div-id"></xsl:param>
        <div id="{$div-id}" class="testresults-output-div" style="display:none;">
            <span style="font-weight:bold">System err:</span>
            <br></br>
            <xsl:apply-templates select="current()" mode="newline-to-br"></xsl:apply-templates>
        </div>
    </xsl:template>
    <xsl:template match="properties" mode="properties-div">
        <xsl:param name="div-id"></xsl:param>
        <div id="{$div-id}" class="testresults-output-div" style="display:none;">
            <span style="font-weight:bold">Properties:</span>
            <br></br>
            <table>
                <tr>
                    <th>Property</th>
                    <th>value</th>
                </tr>
                <xsl:for-each select="property">
                    <xsl:sort select="@name"></xsl:sort>
                    <tr>
                        <td>
                            <xsl:value-of select="@name"></xsl:value-of> </td>
                        <td>
                            <xsl:value-of select="@value"></xsl:value-of> </td>
                    </tr>
                </xsl:for-each>
            </table>
        </div>
    </xsl:template>
    <xsl:template match="text()" mode="newline-to-br">
<!-- 
		<xsl:value-of select="replace(current(), '(\n)|(\r)|(\r\n)', '&lt;br/&gt;')" disable-output-escaping="yes"/>
		-->
        <xsl:call-template name="string-replace-all">
            <xsl:with-param name="input" select="current()"></xsl:with-param>
            <xsl:with-param name="replacement" select="'&lt;br/&gt;'"></xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template name="string-replace-all">
        <xsl:param name="input"></xsl:param>
        <xsl:param name="replacement"></xsl:param>
        <xsl:choose>
            <xsl:when test="contains($input, '&#xD;&#xA;')">
                <xsl:value-of select="substring-before($input, '&#xD;&#xA;')" disable-output-escaping="yes"></xsl:value-of>
                <xsl:value-of select="$replacement" disable-output-escaping="yes"></xsl:value-of>
                <xsl:call-template name="string-replace-all">
                    <xsl:with-param name="input" select="substring-after($input, '&#xD;&#xA;')"></xsl:with-param>
                    <xsl:with-param name="replacement" select="$replacement"></xsl:with-param>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="contains($input, '&#xA;')">
                <xsl:value-of select="substring-before($input, '&#xA;')" disable-output-escaping="yes"></xsl:value-of>
                <xsl:value-of select="$replacement" disable-output-escaping="yes"></xsl:value-of>
                <xsl:call-template name="string-replace-all">
                    <xsl:with-param name="input" select="substring-after($input, '&#xA;')"></xsl:with-param>
                    <xsl:with-param name="replacement" select="$replacement"></xsl:with-param>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="contains($input, '&#xD;')">
                <xsl:value-of select="substring-before($input, '&#xD;')" disable-output-escaping="yes"></xsl:value-of>
                <xsl:value-of select="$replacement" disable-output-escaping="yes"></xsl:value-of>
                <xsl:call-template name="string-replace-all">
                    <xsl:with-param name="input" select="substring-after($input, '&#xD;')"></xsl:with-param>
                    <xsl:with-param name="replacement" select="$replacement"></xsl:with-param>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$input" disable-output-escaping="yes"></xsl:value-of>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>

