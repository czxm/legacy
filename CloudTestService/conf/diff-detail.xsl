<?xml version="1.0" ?>
<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<xsl:output method="html" indent="yes" encoding="US-ASCII"/>
	
	<xsl:param name="groupName"/>
	
	<xsl:param name="diffType"/>
	
	<xsl:template match="/diffs">
		<body>
			<xsl:choose>
				<xsl:when test="$groupName=''">
					<!-- <xsl:apply-templates select="./TestGroup"/> -->
					<xsl:for-each select="./TestGroup[count(.//testcase)>0]">
						<xsl:if test="(@failedToRun='false')">
							<xsl:apply-templates select=".">
								<xsl:with-param name="type" select="$diffType"/>
							</xsl:apply-templates>
						</xsl:if>
					</xsl:for-each>
				</xsl:when>
				<xsl:otherwise>
					<xsl:apply-templates select="./TestGroup[@name=$groupName][@failedToRun='false'][count(.//testcase)>0]">
						<xsl:with-param name="type" select="$diffType"/>
					</xsl:apply-templates>
				</xsl:otherwise>
			</xsl:choose>
		</body>
	</xsl:template>
	
	<xsl:template match="TestGroup">
		<xsl:param name="type"/>
		<xsl:choose>
			<xsl:when test="($type='')">
				<xsl:call-template name="AllTypes"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="SingleType">
					<xsl:with-param name="diffType" select="$type"/>
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template> 
	
	<xsl:template name="SingleType">
		<xsl:param name="diffType"/>
		<h2>
			<xsl:value-of select="@name"/>
		</h2>
		<xsl:if test="($diffType='newFailedTests')">
			<table class="detail" border="0" cellpadding="5" cellspacing="2" width="95%">
				<tr valign="top" class="junit-test-info">
					<th>New Failed Tests</th>
				</tr>
				<xsl:for-each select="newFailedTests/testcase">
					<tr valign="top" class="junit-test-info" style="color:red">
						<td>
							<xsl:value-of select="concat(@classname, '.', @name)"/>
						</td>
					</tr>
				</xsl:for-each>
			</table>
		</xsl:if>
		<xsl:if test="($diffType='newPassedTests')">
			<table class="detail" border="0" callpadding="5" cellspacing="2" width="95%">
				<tr valign="top" class="junit-test-info">
					<th>New Passed Tests</th>
				</tr>
				<xsl:for-each select="newPassedTests/testcase">
					<tr valign="top" class="junit-test-info" style="color:green">
						<td>
							<xsl:value-of select="concat(@classname, '.', @name)"/>
						</td>
					</tr>
				</xsl:for-each>
			</table>
		</xsl:if>
		<xsl:if test="($diffType='testsAdded')">
			<table class="detail" border="0" callpadding="5" cellspacing="2" width="95%">
				<tr valign="top" class="junit-test-info">
					<th>New Added Tests</th>
				</tr>
				<xsl:for-each select="testsAdded/testcase">
					<tr valign="top" class="junit-test-info">
						<td>
							<xsl:value-of select="concat(@classname, '.', @name)"/>
						</td>
					</tr>
				</xsl:for-each>
			</table>
		</xsl:if>
		<xsl:if test="($diffType='testsRemoved')">
			<table class="detail" border="0" callpadding="5" cellspacing="2" width="95%">
				<tr valign="top" class="junit-test-info">
					<th>Removed Tests</th>
				</tr>
				<xsl:for-each select="testsRemoved/testcase">
					<tr valign="top" class="junit-test-info">
						<td>
							<xsl:value-of select="concat(@classname, '.', @name)"/>
						</td>
					</tr>
				</xsl:for-each>
			</table>
		</xsl:if>
	</xsl:template>
	
	<xsl:template name="AllTypes">
		<xsl:variable name="newFailedNum" select="count(newFailedTests/testcase)"/>
		<xsl:variable name="newPassedNum" select="count(newPassedTests/testcase)"/>
		<xsl:variable name="newAddedNum" select="count(testsAdded/testcase)"/>
		<xsl:variable name="removedNum" select="count(testsRemoved/testcase)"/>
		<xsl:variable name="diffNum" 
			select="$newFailedNum + $newPassedNum + $newAddedNum + $removedNum"/>
			
		<h2>
			<xsl:value-of select="@name"/>
		</h2>
		<xsl:if test="count(newFailedTests/testcase)">
			<table class="detail" border="0" cellpadding="5" cellspacing="2" width="95%">
				<tr valign="top" class="junit-test-info">
					<th>New Failed Tests</th>
				</tr>
				<xsl:for-each select="newFailedTests/testcase">
					<tr valign="top" class="junit-test-info" style="color:red">
						<td>
							<xsl:value-of select="concat(@classname, '.', @name)"/>
						</td>
					</tr>
				</xsl:for-each>
			</table>
		</xsl:if>
		<xsl:if test="count(newPassedTests/testcase)">
			<table class="detail" border="0" callpadding="5" cellspacing="2" width="95%">
				<tr valign="top" class="junit-test-info">
					<th>New Passed Tests</th>
				</tr>
				<xsl:for-each select="newPassedTests/testcase">
					<tr valign="top" class="junit-test-info" style="color:green">
						<td>
							<xsl:value-of select="concat(@classname, '.', @name)"/>
						</td>
					</tr>
				</xsl:for-each>
			</table>
		</xsl:if>
		<xsl:if test="count(testsAdded/testcase)">
			<table class="detail" border="0" callpadding="5" cellspacing="2" width="95%">
				<tr valign="top" class="junit-test-info">
					<th>New Added Tests</th>
				</tr>
				<xsl:for-each select="testsAdded/testcase">
					<tr valign="top" class="junit-test-info">
						<td>
							<xsl:value-of select="concat(@classname, '.', @name)"/>
						</td>
					</tr>
				</xsl:for-each>
			</table>
		</xsl:if>
		<xsl:if test="count(testsRemoved/testcase)">
			<table class="detail" border="0" callpadding="5" cellspacing="2" width="95%">
				<tr valign="top" class="junit-test-info">
					<th>Removed Tests</th>
				</tr>
				<xsl:for-each select="testsRemoved/testcase">
					<tr valign="top" class="junit-test-info">
						<td>
							<xsl:value-of select="concat(@classname, '.', @name)"/>
						</td>
					</tr>
				</xsl:for-each>
			</table>
		</xsl:if>
		
		<xsl:if test="$diffNum">
			<hr align="left" width="95%"></hr>
		</xsl:if>
	</xsl:template>
</xsl:stylesheet>
