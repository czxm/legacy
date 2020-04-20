' Set AccPwd.vbs
' Example VBScript to require users change passwords at next logon
' Author Guy Thomas http://computerperformance.co.uk/
' Version 2.2 - March 2004
' Tweaked by Michael Shatswell February 2010
' (added line 26 from Microsoft's site, matched to script)
' ----------------------------------------------------------------
Option Explicit
Dim objOU, objUser, objRootDSE, objGroup, objDomainGroup
Dim strContainer, strLastUser, strDNSDomain, intCounter, intAccValue, strUserIndex
Set objRootDSE = GetObject("LDAP://RootDSE")
strDNSDomain = objRootDSE.Get("DefaultNamingContext")
strContainer = "CN=Users,"
intAccValue = 66048
strContainer = strContainer & strDNSDomain

' add user to 'Manager' if the user index is odd
' add user to 'Employee' if the user index is even

set objGroup = GetObject("LDAP://" & "CN=Employee,CN=Users," & strDNSDomain)
set objDomainGroup = GetObject("LDAP://" & "CN=Manager,CN=Users," & strDNSDomain)

set objOU =GetObject("LDAP://" & strContainer )
intCounter = 0
For each objUser in objOU
    If InStr(objUser.name, "CN=user") > 0 then
    strUserIndex = Replace(objUser.name, "CN=user", "")
    objUser.SetPassword "secret@password123456"
    objUser.SetInfo
    objUser.Put "userAccountControl", intAccValue
    objUser.SetInfo
    intCounter = intCounter +1
    strLastUser = objUser.Get ("name")
    objuser.Put "pwdLastSet", CLng(0)
    objuser.SetInfo
    if strUserIndex Mod 2 = 0 then
      objGroup.add(objuser.ADsPath)
    else
      objDomainGroup.add(objuser.ADsPath)
    End if
End if
next
WScript.Echo intCounter & " Users change pwd next logon. Value " _
& intAccValue
WScript.Quit
' End of User Account example VBScript