Set objArgs = WScript.Arguments
newTimeZone = objArgs(0)
newDate = objArgs(1)
newTime = objArgs(2)

Set objShell = CreateObject("WScript.Shell") 
'set date on local computer 
objShell.Run("%comspec% /c date " & newDate) 
'set time on local computer 
objShell.Run("%comspec% /c time " & newTime) 
'set time zone
objShell.Run("CONTROL.EXE TIMEDATE.CPL,,/Z " & newTimeZone)
