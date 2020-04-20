<?xml version="1.0" ?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output
        method="xml"
        omit-xml-declaration="yes"
        indent="yes" />
	<xsl:param name="previous"/>
	<xsl:variable name="preDoc" select="document($previous)"/>
	<xsl:template match="/"><!-- the document root -->
		<diffs>
			<!-- ensure there is at most one test suite under test suites element -->
			<xsl:for-each select="./TestReport/testsuites">
				<!-- has not any test suite descendant -->
				<xsl:if test="not(testsuite)">
					<TestGroup>
						<xsl:attribute name="name">
							<xsl:value-of select="@name"/>
						</xsl:attribute>
						<xsl:attribute name="failedToRun">
							<xsl:value-of select="'true'"/>
						</xsl:attribute>
					</TestGroup>
				</xsl:if>
				
				<!-- has test suite descendant -->
				<xsl:if test="testsuite">
					<TestGroup>
						<xsl:variable name="groupName" select="@name"/>
						<xsl:variable name="suiteName" select="./testsuite/@name"/>
						<xsl:attribute name="name">
							<xsl:value-of select="$groupName"/>
						</xsl:attribute>
						<xsl:attribute name="failedToRun">
							<xsl:value-of select="'false'"/>
						</xsl:attribute>
						<xsl:call-template name="walkTestSuite">
							<xsl:with-param name="currTestSuite" select="testsuite"/>
                            <xsl:with-param name="prevTestSuite" select="$preDoc/TestReport/testsuites[@name=$groupName]/testsuite"/>							
						</xsl:call-template>
					</TestGroup>
				</xsl:if>
			</xsl:for-each>
			<!-- 
			<xsl:for-each select="./TestReport//testsuite">
				<xsl:variable name="suiteName" select="@name"/>
				<testsuite>
					<xsl:attribute name="name">
						<xsl:value-of select="$suiteName"/>
					</xsl:attribute>
					<xsl:call-template name="walkTestSuite">
						<xsl:with-param name="currTestSuite" select="."/>
						<xsl:with-param name="prevTestSuite" 
							select="$preDoc/TestReport//testsuite[@name=$suiteName]"/>
					</xsl:call-template>
				</testsuite>
			</xsl:for-each>
			-->
		</diffs>
	</xsl:template>
	
	<xsl:template name="walkTestSuite">
		<xsl:param name="currTestSuite"/>
		<xsl:param name="prevTestSuite"/>
		<newFailedTests>
			<xsl:variable name="currFailed" select="$currTestSuite/testcase[failure or error]"/>
			<xsl:variable name="prevFailed" select="$prevTestSuite/testcase[failure or error]"/>
			
			<xsl:for-each select="$currFailed">
				<xsl:variable name="currTestCaseName" select="@name"/>
				<xsl:variable name="currTestClassName" select="@classname"/>
				<xsl:if test="not($prevFailed[@name=$currTestCaseName and @classname=$currTestClassName])">
					<testcase>
                                                <xsl:attribute name="classname">
							<xsl:value-of select="$currTestClassName"/>
                                                </xsl:attribute>
						<xsl:attribute name="name">
							<xsl:value-of select="$currTestCaseName"/>
						</xsl:attribute>
					</testcase>
				</xsl:if>
			</xsl:for-each>
		</newFailedTests>
		
		<newPassedTests>
			<xsl:variable name="currPassed" select="$currTestSuite/testcase[not(failure or error)]"/>
			<xsl:variable name="prevPassed" select="$prevTestSuite/testcase[not(failure or error)]"/>
			
			<xsl:for-each select="$currPassed">
				<xsl:variable name="currTestCaseName" select="@name"/>
				<xsl:variable name="currTestClassName" select="@classname"/>
				<xsl:if test="not($prevPassed[@name=$currTestCaseName and @classname=$currTestClassName])">
					<testcase>
                                                <xsl:attribute name="classname">
							<xsl:value-of select="$currTestClassName"/>
                                                </xsl:attribute>
						<xsl:attribute name="name">
							<xsl:value-of select="$currTestCaseName"/>
						</xsl:attribute>
					</testcase>
				</xsl:if>
			</xsl:for-each>
		</newPassedTests>
		
		<testsAdded>
			<xsl:variable name="currTests" select="$currTestSuite/testcase"/>
			<xsl:variable name="prevTests" select="$prevTestSuite/testcase"/>
			
			<xsl:for-each select="$currTests">
				<xsl:variable name="currTestCaseName" select="@name"/>
				<xsl:variable name="currTestClassName" select="@classname"/>
				<xsl:if test="not($prevTests[@name=$currTestCaseName and @classname=$currTestClassName])">
					<testcase>
                                                <xsl:attribute name="classname">
							<xsl:value-of select="$currTestClassName"/>
                                                </xsl:attribute>
						<xsl:attribute name="name">
							<xsl:value-of select="$currTestCaseName"/>
						</xsl:attribute>
					</testcase>
				</xsl:if>
			</xsl:for-each>
		</testsAdded>
		
		<testsRemoved>
			<xsl:variable name="currTests" select="$currTestSuite/testcase"/>
			<xsl:variable name="prevTests" select="$prevTestSuite/testcase"/>
			<xsl:for-each select="$prevTests">
				<xsl:variable name="prevTestCaseName" select="@name"/>
				<xsl:variable name="prevTestClassName" select="@classname"/>
				<xsl:if test="not($currTests[@name=$prevTestCaseName and @classname=$prevTestClassName])">
					<testcase>
                                                <xsl:attribute name="classname">
							<xsl:value-of select="$prevTestClassName"/>
                                                </xsl:attribute>
						<xsl:attribute name="name">
							<xsl:value-of select="$prevTestCaseName"/>
						</xsl:attribute>
					</testcase>
				</xsl:if>
			</xsl:for-each>
		</testsRemoved>
	</xsl:template>
</xsl:stylesheet>
