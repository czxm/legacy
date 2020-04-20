<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" extension-element-prefixes="redirect" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:lxslt="http://xml.apache.org/xslt" xmlns:redirect="http://xml.apache.org/xalan/redirect" xmlns:stringutils="xalan://org.apache.tools.ant.util.StringUtils">
    <xsl:output method="html" indent="yes" encoding="US-ASCII"></xsl:output>
    <xsl:decimal-format decimal-separator="." grouping-separator=","></xsl:decimal-format>
    <xsl:param name="diffDocUrl"></xsl:param>
    <xsl:param name="reportServerBase"></xsl:param>
    <xsl:param name="testDetailHtml"></xsl:param>
    <xsl:param name="diffDetailHtml"></xsl:param>
    <xsl:param name="codeCoverageUrl"></xsl:param>
    <xsl:param name="packageLink"></xsl:param>
    <xsl:param name="title"></xsl:param>
    <xsl:param name="footnote"></xsl:param>
    <xsl:variable name="diffDoc" select="document($diffDocUrl)"></xsl:variable>
    <xsl:template match="/">
        <xsl:apply-templates select="/TestReport" mode="summary"></xsl:apply-templates>
    </xsl:template>
    <xsl:template match="/TestReport" mode="summary">
        <body>
<!-- TestResultUrl now is the HTTP URL resource -->
<!--
        <xsl:variable name="codeCoverageUrl" select="./host/CodeCoverageUrl"/>
        -->
            <xsl:variable name="testSuiteNum" select="count(./testsuites)"></xsl:variable>
<!-- Build Result Section -->
            <xsl:if test="./build/info">
                <div class="cruiseBuildInfo">
                    <h2>Build Results</h2>
<!--
	        	<p id="buildResult">
	        		<xsl:choose>
	        			<xsl:when test="./build/error">
	        				<xsl:attribute name="class">
	        					<xsl:text>FAILED</xsl:text>
	        				</xsl:attribute>
	        				<xsl:text>BUILD FAILED</xsl:text>
	        			</xsl:when>
	        			<xsl:otherwise>
	        				<xsl:attribute name="class">
	        					<xsl:text>OK</xsl:text>
	        				</xsl:attribute>
	        				<xsl:text>BUILD COMPLETE</xsl:text>
	        			</xsl:otherwise>
	        		</xsl:choose>
	        	</p>
	        	-->
                    <table id="buildResults">
                        <col class="type"></col>
                        <col></col>
                        <tr>
                            <th style="width: 10em">Build Result</th>
                            <td>
                                <xsl:choose>
                                    <xsl:when test="./build/error">
                                        <xsl:attribute name="class">
                                            <xsl:text>FAILED</xsl:text>
                                        </xsl:attribute>
                                        <xsl:text>FAILED</xsl:text>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:attribute name="class">
                                            <xsl:text>OK</xsl:text>
                                        </xsl:attribute>
                                        <xsl:text>COMPLETE</xsl:text>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </td>
                        </tr>
                        <tr>
                            <th style="width: 10em">Machine IP</th>
                            <td>
                                <xsl:value-of select="/NightlyReport/build/info/BuildMachine/@value"></xsl:value-of>
                            </td>
                        </tr>
                        <tr>
                            <th style="width: 10em">Date of Build</th>
                            <td>
                                <xsl:value-of select="/NightlyReport/build/info/BuildDateTime/@value"></xsl:value-of>
                            </td>
                        </tr>
                        <tr>
                            <th style="width: 10em">Time for Build</th>
                            <td>
                                <xsl:value-of select="/NightlyReport/build/info/BuildDuration/@value"></xsl:value-of>
                            </td>
                        </tr>
                        <tr>
                            <th style="width: 10em">Last Changed</th>
                            <td>
                                <xsl:value-of select="/NightlyReport/build/info/LastChangeTime/@value"></xsl:value-of>
                            </td>
                        </tr>
                        <tr>
                            <th style="width: 10em">SVN Url</th>
                            <td>
                                <xsl:value-of select="/NightlyReport/build/info/SVNUrl/@value"></xsl:value-of>
                            </td>
                        </tr>
                        <tr>
                            <th style="width: 10em">Head Revision</th>
                            <td>
                                <xsl:value-of select="/NightlyReport/build/info/HeadRevision/@value"></xsl:value-of>
                            </td>
                        </tr>
                        <xsl:if test="not(./build/error)">
                            <tr>
                                <th style="width: 10em">Package</th>
                                <td>
                                    <a title="packageLocation">
                                        <xsl:attribute name="href">
                                            <xsl:value-of select="$packageLink"></xsl:value-of>
                                        </xsl:attribute>
                                        <xsl:value-of select="'Download...'"></xsl:value-of>
                                    </a>
                                </td>
                            </tr>
                        </xsl:if>
                    </table>
                </div>
                <hr></hr>
            </xsl:if>
<!-- Split Point Nightly Test Summary Section -->
            <xsl:if test="./testsuites">
                <h2>
                    <xsl:choose>
                        <xsl:when test="$title">
                            <xsl:value-of select="$title"></xsl:value-of>
                        </xsl:when>
                        <xsl:otherwise>Test Summary</xsl:otherwise>
                    </xsl:choose>
                </h2>
                <table class="detail" border="0" cellpadding="5" cellspacing="2" width="95%">
                    <tr valign="top" class="junit-test-info">
                        <th>Test Group</th>
                        <th>Tests</th>
                        <th>Failures</th>
                        <th>Errors</th>
                        <th>Success rate</th>
                        <th>Time (s)</th>
                    </tr>
                    <xsl:if test="$testSuiteNum &gt; 0">
                        <xsl:call-template name="junitTestsuites"></xsl:call-template>
                    </xsl:if>
                </table>
                <a title="testdetail">
                    <xsl:attribute name="href">
                        <xsl:value-of select="concat($reportServerBase, '/', $testDetailHtml)"></xsl:value-of>
                    </xsl:attribute>
                    <xsl:value-of select="'detailed info...'"></xsl:value-of>
                </a>
                <xsl:if test="$diffDoc/diffs">
                    <xsl:text disable-output-escaping="yes">&amp;nbsp;&amp;nbsp;</xsl:text>
                    <a title="diffDetail">
                        <xsl:attribute name="href">
                            <xsl:value-of select="concat($reportServerBase, '/', $diffDetailHtml)"></xsl:value-of>
                        </xsl:attribute>
                        <xsl:value-of select="'compare results...'"></xsl:value-of>
                    </a>
                </xsl:if>
                <hr></hr>
            </xsl:if>
<!-- Split Point Nightly Report Differentiation
            <xsl:if test="$diffDoc">
                <h2>Regression or Boost</h2>
                <table class="detail" border="0" cellpadding="5" cellspacing="2" width="95%">
                    <tr valign="top" class="junit-test-info">
                        <th>Test Group</th>
                        <th>New Failed Cases</th>
                        <th>New Passed Cases</th>
                        <th>New Added Cases</th>
                        <th>Removed Cases</th>
                    </tr>
                    <xsl:call-template name="showDiffs">
                        <xsl:with-param name="diffs" select="$diffDoc/diffs"></xsl:with-param>
                    </xsl:call-template>
                </table>
                <a title="diffDetail">
                    <xsl:attribute name="href">
                        <xsl:value-of select="concat($reportServerBase, '/', $diffDetailHtml)"></xsl:value-of>
                    </xsl:attribute>
                    <xsl:value-of select="'detailed info...'"></xsl:value-of>
                </a>
                <p>*The group highlighted in red indicates fatal error.</p>
                <hr></hr>
            </xsl:if>
-->
<!-- Code Coverage Section -->
            <xsl:if test="./report">
                <h2>Overall Coverage Summary</h2>
                <table class="detail" border="0" cellpadding="5" cellspacing="2" width="75%">
                    <tr valign="top" class="junit-test-info">
                        <th>name</th>
                        <th>class,%</th>
                        <th>method,%</th>
                        <th>block,%</th>
                    </tr>
                    <tr>
                        <td>all,classes</td>
                        <td>
                            <xsl:value-of select="./report/data/all/coverage[@type='class, %']/@value"></xsl:value-of>
                        </td>
                        <td>
                            <xsl:value-of select="./report/data/all/coverage[@type='method, %']/@value"></xsl:value-of>
                        </td>
                        <td>
                            <xsl:value-of select="./report/data/all/coverage[@type='block, %']/@value"></xsl:value-of>
                        </td>
                    </tr>
                </table>
                <h2>Overall Statistics</h2>
                <table class="detail" border="0" cellpadding="5" cellspacing="2" width="25%">
                    <tr>
                        <td>total packages:</td>
                        <td>
                            <xsl:value-of select="./report/stats/packages/@value"></xsl:value-of>
                        </td>
                    </tr>
                    <tr>
                        <td>total classes:</td>
                        <td>
                            <xsl:value-of select="./report/stats/classes/@value"></xsl:value-of>
                        </td>
                    </tr>
                    <tr>
                        <td>total methods:</td>
                        <td>
                            <xsl:value-of select="./report/stats/methods/@value"></xsl:value-of>
                        </td>
                    </tr>
                </table>
                <a title="codeCoverage">
                    <xsl:attribute name="href">
                        <xsl:value-of select="concat($reportServerBase, '/', $codeCoverageUrl)"></xsl:value-of>
                    </xsl:attribute>
                    <xsl:value-of select="'detailed info...'"></xsl:value-of>
                </a>
                <hr></hr>
            </xsl:if>
<!-- Single TestSuite Overview Section -->
<!-- 
        <xsl:apply-templates select="./testsuites">
        	<xsl:with-param name="resultUrl" select="./host/TestResultUrl"/>
        </xsl:apply-templates>
        -->
<!-- modifications section -->
            <xsl:if test="./build/modifications/modification">
                <xsl:variable name="modification.list" select="./build/modifications/modification"></xsl:variable>
                <h2>
                    <xsl:text>Modifications (</xsl:text>
                    <xsl:value-of select="count($modification.list)"></xsl:value-of>
                    <xsl:text>)</xsl:text>
                </h2>
                <table id="sourceModifications" class="modifications" width="95%">
                    <col class="modification-info"></col>
                    <col class="modification-files"></col>
                    <xsl:for-each select="$modification.list">
                        <xsl:call-template name="display-file-info">
                            <xsl:with-param name="modification" select="."></xsl:with-param>
                        </xsl:call-template>
                    </xsl:for-each>
                </table>
                <hr></hr>
            </xsl:if>
            <p>
                <xsl:if test="$footnote">
                    <em>
                        <xsl:value-of select="$footnote"></xsl:value-of>
                    </em>
                </xsl:if>
            </p>
        </body>
    </xsl:template>
    <xsl:template name="showDiffs">
        <xsl:param name="diffs"></xsl:param>
        <xsl:for-each select="$diffs/TestGroup">
            <tr valign="top">
                <td>
                    <xsl:attribute name="class">
                        <xsl:value-of select="'junit-test-info'"></xsl:value-of>
                    </xsl:attribute>
                    <xsl:if test="not(@failedToRun='false')">
                        <xsl:attribute name="style">
                            <xsl:value-of select="'color:black'"></xsl:value-of>
                        </xsl:attribute>
                    </xsl:if>
                    <xsl:if test="(@failedToRun='true')">
                        <xsl:attribute name="style">
                            <xsl:value-of select="'background:red;color:black'"></xsl:value-of>
                        </xsl:attribute>
                    </xsl:if>
                    <xsl:value-of select="@name"></xsl:value-of>
                </td>
<!-- <td class="junit-test-info" style="font-weight:bold;color:red"> -->
                <td class="junit-test-info">
                    <xsl:choose>
                        <xsl:when test="(@failedToRun='true')">
                            <span style="font-weight:bold;color:black">
                                <xsl:value-of select="'--'"></xsl:value-of>
                            </span>
                        </xsl:when>
                        <xsl:when test="count(./newFailedTests/testcase)">
                            <a style="font-weight:bold;color:red">
                                <xsl:attribute name="href">
                                    <xsl:value-of select="concat($reportServerBase, '/', 'diffs', '/', @name, '-newFailedTests.html')"></xsl:value-of>
                                </xsl:attribute>
                                <xsl:value-of select="count(./newFailedTests/testcase)"></xsl:value-of>
                            </a>
                        </xsl:when>
                        <xsl:otherwise>
                            <span style="font-weight:bold;color:black">
                                <xsl:value-of select="'0'"></xsl:value-of>
                            </span>
                        </xsl:otherwise>
                    </xsl:choose>
                </td>
<!-- <td class="junit-test-info" style="font-weight:bold;color:green"> -->
                <td class="junit-test-info">
                    <xsl:choose>
                        <xsl:when test="(@failedToRun='true')">
                            <span style="font-weight:bold;color:black">
                                <xsl:value-of select="'--'"></xsl:value-of>
                            </span>
                        </xsl:when>
                        <xsl:when test="count(./newPassedTests/testcase)">
                            <a style="font-weight:bold;color:green">
                                <xsl:attribute name="href">
                                    <xsl:value-of select="concat($reportServerBase, '/', 'diffs', '/', @name, '-newPassedTests.html')"></xsl:value-of>
                                </xsl:attribute>
                                <xsl:value-of select="count(./newPassedTests/testcase)"></xsl:value-of>
                            </a>
                        </xsl:when>
                        <xsl:otherwise>
                            <span style="font-weight:bold;color:black">
                                <xsl:value-of select="'0'"></xsl:value-of>
                            </span>
                        </xsl:otherwise>
                    </xsl:choose>
                </td>
                <td class="junit-test-info" style="font-weight:bold;">
                    <xsl:choose>
                        <xsl:when test="(@failedToRun='true')">
                            <xsl:value-of select="'--'"></xsl:value-of>
                        </xsl:when>
                        <xsl:when test="count(./testsAdded/testcase)">
                            <a>
                                <xsl:attribute name="href">
                                    <xsl:value-of select="concat($reportServerBase, '/', 'diffs', '/', @name, '-testsAdded.html')"></xsl:value-of>
                                </xsl:attribute>
                                <xsl:value-of select="count(./testsAdded/testcase)"></xsl:value-of>
                            </a>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="'0'"></xsl:value-of>
                        </xsl:otherwise>
                    </xsl:choose>
                </td>
                <td class="junit-test-info" style="font-weight:bold;">
                    <xsl:choose>
                        <xsl:when test="(@failedToRun='true')">
                            <xsl:value-of select="'--'"></xsl:value-of>
                        </xsl:when>
                        <xsl:when test="count(./testsRemoved/testcase)">
                            <a>
                                <xsl:attribute name="href">
                                    <xsl:value-of select="concat($reportServerBase, '/', 'diffs', '/', @name, '-testsRemoved.html')"></xsl:value-of>
                                </xsl:attribute>
                                <xsl:value-of select="count(./testsRemoved/testcase)"></xsl:value-of>
                            </a>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="'0'"></xsl:value-of>
                        </xsl:otherwise>
                    </xsl:choose>
                </td>
            </tr>
        </xsl:for-each>
    </xsl:template>
    <xsl:template match="testsuites">
        <xsl:param name="resultUrl"></xsl:param>
        <h4>
            <xsl:value-of select="@name"></xsl:value-of>
        </h4>
        <table class="detail" border="0" cellpadding="5" cellspacing="2" width="95%">
            <xsl:call-template name="testsuite.test.header"></xsl:call-template>
            <xsl:for-each select="testsuite[not(./@package = preceding-sibling::testsuite/@package)]">
                <xsl:sort select="@package" order="ascending"></xsl:sort>
<!-- get the node set containing all testsuites that have the same package -->
                <xsl:variable name="insamepackage" select="/NightlyReport/testsuites/testsuite[./@package = current()/@package]"></xsl:variable>
                <tr valign="top">
<!-- display a failure if there is any failure/error in the package -->
                    <xsl:attribute name="class">
                        <xsl:choose>
                            <xsl:when test="sum($insamepackage/@errors) &gt; 0">Error</xsl:when>
                            <xsl:when test="sum($insamepackage/@failures) &gt; 0">Failure</xsl:when>
                            <xsl:otherwise>Pass</xsl:otherwise>
                        </xsl:choose>
                    </xsl:attribute>
                    <td>
<!--<a href="./{translate(@package,'.','/')}/package-summary.html">
                    <xsl:value-of select="@package"/>
                    <xsl:if test="@package = ''">&lt;none&gt;</xsl:if>
                </a>-->
                        <a>
                            <xsl:attribute name="href">
                                <xsl:value-of select="$resultUrl"></xsl:value-of>
                            </xsl:attribute>
                            <xsl:value-of select="@package"></xsl:value-of>
                            <xsl:if test="@package = ''">&lt;none&gt;</xsl:if>
                        </a>
                    </td>
                    <td>
                        <xsl:value-of select="sum($insamepackage/@tests)"></xsl:value-of>
                    </td>
                    <td>
                        <xsl:value-of select="sum($insamepackage/@errors)"></xsl:value-of>
                    </td>
                    <td>
                        <xsl:value-of select="sum($insamepackage/@failures)"></xsl:value-of>
                    </td>
                    <td>
                        <xsl:call-template name="display-time">
                            <xsl:with-param name="value" select="sum($insamepackage/@time)"></xsl:with-param>
                        </xsl:call-template>
                    </td>
                    <td>
                        <xsl:value-of select="$insamepackage/@timestamp"></xsl:value-of>
                    </td>
                    <td>
                        <xsl:value-of select="$insamepackage/@hostname"></xsl:value-of>
                    </td>
                </tr>
            </xsl:for-each>
        </table>
    </xsl:template>
    <xsl:template name="display-percent">
        <xsl:param name="value"></xsl:param>
        <xsl:value-of select="format-number($value,'0.00%')"></xsl:value-of>
    </xsl:template>
    <xsl:template name="display-time">
        <xsl:param name="value"></xsl:param>
        <xsl:value-of select="format-number($value,'0.000')"></xsl:value-of>
    </xsl:template>
<!-- class header -->
    <xsl:template name="testsuite.test.header">
        <tr valign="top" class="junit-test-info">
            <th width="80%">Name</th>
            <th>Tests</th>
            <th>Errors</th>
            <th>Failures</th>
            <th nowrap="nowrap">Time(s)</th>
            <th nowrap="nowrap">Time Stamp</th>
            <th>Host</th>
        </tr>
    </xsl:template>
    <xsl:template name="junitTestsuites">
        <xsl:apply-templates select="/TestReport" mode="junit"></xsl:apply-templates>
    </xsl:template>
    <xsl:template match="/TestReport" mode="junit">
<!-- ensure the latest log file has been computed in the report file? -->
<!--
    <xsl:variable name="latestlogfile" select="/NightlyReport/host/latestlogfile"/>   
    <xsl:variable name="latestlogfileDoc" select="document($latestlogfile)" />
    -->
        <xsl:for-each select="testsuites">
<!--xsl:sort select="@name"></xsl:sort-->
            <xsl:variable name="suitename" select="./@name"></xsl:variable>
            <xsl:variable name="testCount" select="sum(testsuite/@tests)"></xsl:variable>
<!--
	    <xsl:variable name="errorCount" select="sum(testsuite/@errors)"/>
	    -->
            <xsl:variable name="errorCount" select="count(testsuite/testcase[error])"></xsl:variable>
            <xsl:variable name="failureCount" select="sum(testsuite/@failures)"></xsl:variable>
            <xsl:variable name="timeCount" select="sum(testsuite/@time)"></xsl:variable>
            <xsl:variable name="successRate" select="($testCount - $failureCount - $errorCount) div $testCount"></xsl:variable>
    <!-- move to regression section
	    <xsl:variable name="preerrorCount" select="sum($latestlogfileDoc/cruisecontrol/testsuites[@name=$suitename]/testsuite/@errors)"/>
	    <xsl:variable name="prefailureCount" select="sum($latestlogfileDoc/cruisecontrol/testsuites[@name=$suitename]/testsuite/@failures)"/>
	    <xsl:variable name="regression" select="($failureCount + $errorCount - $preerrorCount - $prefailureCount)"/>
	-->
            <tr valign="top">
                <xsl:attribute name="class">
                    <xsl:choose>
                        <xsl:when test="$errorCount &gt; 0">Error</xsl:when>
                        <xsl:when test="$failureCount &gt; 0">Failure</xsl:when>
                        <xsl:otherwise>Pass</xsl:otherwise>
                    </xsl:choose>
                </xsl:attribute>
<!-- <td class="junit-test-info" style="color:black"> -->
                <td class="junit-test-info">
                    <xsl:choose>
                        <xsl:when test="./@name">
                            <xsl:value-of select="./@name"></xsl:value-of>
                        </xsl:when>
                        <xsl:otherwise>Junit Test</xsl:otherwise>
                    </xsl:choose>
                </td>
                <td>
                    <xsl:choose>
                        <xsl:when test="($testCount='0')">
                            <xsl:value-of select="'--'"></xsl:value-of>
                        </xsl:when>
                        <xsl:otherwise>
                            <a title="Display all tests">
                                <xsl:attribute name="href">
                                    <xsl:value-of select="concat($reportServerBase, '/', 'details', '/', ./@name, '-tests.html')"></xsl:value-of>
                                </xsl:attribute>
                                <xsl:value-of select="$testCount"></xsl:value-of>
                            </a>
                            <xsl:if test="$diffDoc/diffs">
                                <xsl:variable name="diffs" select="$diffDoc/diffs"></xsl:variable>
                                <xsl:variable name="testsAdded" select="count($diffs/TestGroup[@name = $suitename]/testsAdded/testcase)"></xsl:variable>
                                <xsl:variable name="testsRemoved" select="count($diffs/TestGroup[@name = $suitename]/testsRemoved/testcase)"></xsl:variable>
                                <xsl:variable name="newPassed" select="count($diffs/TestGroup[@name = $suitename]/newPassedTests/testcase)"></xsl:variable>
                                <xsl:variable name="newFailed" select="count($diffs/TestGroup[@name = $suitename]/newFailedTests/testcase)"></xsl:variable>
                                <xsl:if test="$testsAdded &gt; 0 or $testsRemoved &gt; 0 or $newPassed &gt; 0 or $newFailed &gt; 0">                                
                                <xsl:text disable-output-escaping="yes">&amp;nbsp;&amp;nbsp;(&amp;nbsp;</xsl:text>
                                <xsl:if test="$testsAdded &gt; 0">
                                    <a title="Added Tests" style="font-weight:bold">
                                        <xsl:attribute name="href">
                                            <xsl:value-of select="concat($reportServerBase, '/', 'diffs', '/', @name, '-testsAdded.html')"></xsl:value-of>
                                        </xsl:attribute>
                                    +<xsl:value-of select="$testsAdded"></xsl:value-of>
                                    </a>
                                    <xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
                                </xsl:if>
                                <xsl:if test="$testsRemoved &gt; 0">
                                    <a title="Removed Tests" style="font-weight:bold">
                                        <xsl:attribute name="href">
                                            <xsl:value-of select="concat($reportServerBase, '/', 'diffs', '/', @name, '-testsRemoved.html')"></xsl:value-of>
                                        </xsl:attribute>
                                    -<xsl:value-of select="$testsRemoved"></xsl:value-of>
                                    </a>
                                    <xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
                                </xsl:if>
                                <xsl:if test="$newPassed &gt; 0">
                                    <a title="New Passed" style="font-weight:bold;color:green">
                                        <xsl:attribute name="href">
                                            <xsl:value-of select="concat($reportServerBase, '/', 'diffs', '/', @name, '-newPassedTests.html')"></xsl:value-of>
                                        </xsl:attribute>
                                        <xsl:value-of select="$newPassed"></xsl:value-of>
                                    </a>
                                    <xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
                                </xsl:if>
                                <xsl:if test="$newFailed &gt; 0">
                                    <a title="New Failed" style="font-weight:bold;color:red">
                                        <xsl:attribute name="href">
                                            <xsl:value-of select="concat($reportServerBase, '/', 'diffs', '/', @name, '-newFailedTests.html')"></xsl:value-of>
                                        </xsl:attribute>
                                        <xsl:value-of select="$newFailed"></xsl:value-of>
                                    </a>
                                    <xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
                                </xsl:if>
                                <xsl:text>)</xsl:text>
                                </xsl:if>
                            </xsl:if>
                        </xsl:otherwise>
                    </xsl:choose>
                </td>
                <td>
                    <xsl:choose>
                        <xsl:when test="($testCount='0')">
                            <xsl:value-of select="'--'"></xsl:value-of>
                        </xsl:when>
                        <xsl:when test="($failureCount='0')">
                            <xsl:value-of select="'0'"></xsl:value-of>
                        </xsl:when>
                        <xsl:otherwise>
                            <a title="Display all tests">
                                <xsl:attribute name="href">
                                    <xsl:value-of select="concat($reportServerBase, '/', 'details', '/', ./@name, '-failures.html')"></xsl:value-of>
                                </xsl:attribute>
                                <xsl:value-of select="$failureCount"></xsl:value-of>
                            </a>
                        </xsl:otherwise>
                    </xsl:choose>
                </td>
                <td>
                    <xsl:choose>
                        <xsl:when test="($testCount='0')">
                            <xsl:value-of select="'--'"></xsl:value-of>
                        </xsl:when>
                        <xsl:when test="($errorCount='0')">
                            <xsl:value-of select="'0'"></xsl:value-of>
                        </xsl:when>
                        <xsl:otherwise>
                            <a title="Display all tests">
                                <xsl:attribute name="href">
                                    <xsl:value-of select="concat($reportServerBase, '/', 'details', '/', ./@name, '-errors.html')"></xsl:value-of>
                                </xsl:attribute>
                                <xsl:value-of select="$errorCount"></xsl:value-of>
                            </a>
                        </xsl:otherwise>
                    </xsl:choose>
                </td>
                <td>
                    <xsl:choose>
                        <xsl:when test="($testCount='0')">
                            <xsl:value-of select="'--'"></xsl:value-of>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:call-template name="display-percent">
                                <xsl:with-param name="value" select="$successRate"></xsl:with-param>
                            </xsl:call-template>
                        </xsl:otherwise>
                    </xsl:choose>
                </td>
                <td>
                    <xsl:choose>
                        <xsl:when test="($testCount='0')">
                            <xsl:value-of select="'--'"></xsl:value-of>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:call-template name="display-time">
                                <xsl:with-param name="value" select="$timeCount"></xsl:with-param>
                            </xsl:call-template>
                        </xsl:otherwise>
                    </xsl:choose>
                </td>
            </tr>
        </xsl:for-each>
    </xsl:template>
    <xsl:template name="display-file-info">
        <xsl:param name="modification"></xsl:param>
        <tbody>
            <tr>
                <td class="modification-info" style="with:10em">
                    <table with="100%" class="modification-info">
                        <col with="100%"></col>
                        <tbody>
                            <tr>
                                <td>
                                    <xsl:value-of select="$modification/@revision"></xsl:value-of>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <xsl:value-of select="$modification/author"></xsl:value-of>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <xsl:value-of select="$modification/date"></xsl:value-of>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </td>
                <td class="modification-files">
                    <p class="modification-comment">
                        <xsl:call-template name="newlineToHTML">
                            <xsl:with-param name="line">
                                <xsl:value-of select="msg"></xsl:value-of>
                            </xsl:with-param>
                        </xsl:call-template>
                    </p>
                    <table width="100%" class="modification-files">
                        <col class="modification-action"></col>
                        <col class="modification-file"></col>
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
                                        <xsl:value-of select="@action"></xsl:value-of>
                                    </td>
                                    <td class="modifcation-file">
                                        <xsl:value-of select="."></xsl:value-of>
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

