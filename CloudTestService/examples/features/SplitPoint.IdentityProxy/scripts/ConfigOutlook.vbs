IdentityProxyURL = "https://www.cloud-express.com:8443/splat/identityproxyservice/SforceService/salesforce"
Set objArgs = WScript.Arguments
If objArgs.Count > 0 Then
    IdentityProxyURL = objArgs(0)
End If

Const HKEY_CURRENT_USER = &H80000001 
 
strComputer = "." 
  
Set oReg=GetObject("winmgmts:{impersonationLevel=impersonate}!\\" & _  
    strComputer & "\root\default:StdRegProv") 
  
strKeyPath = "SOFTWARE\Salesforce.com\SM"
strEntryName = "ServerUrl"

oReg.GetStringValue HKEY_CURRENT_USER,strKeyPath,strEntryName,strValue


If IsNull(strValue) then
                set WshShell = CreateObject("WScript.Shell")
                WshShell.Popup "Salesforce Outlook Connector is not installed!", 1
Else 
    oReg.SetStringValue HKEY_CURRENT_USER,strKeyPath,strEntryName,IdentityProxyURL
    set WshShell = CreateObject("WScript.Shell")
    REM WshShell.Popup "Setting Salesforce Outlook Connector Proxy URL!", 1
End If
