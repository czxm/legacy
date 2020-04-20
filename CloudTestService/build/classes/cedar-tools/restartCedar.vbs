Set objShell = CreateObject("WScript.Shell")
Set objEndTask = objShell.Exec("net stop CedarAgent")
Set objStdOut = objEndTask.StdOut
objStdOut.readall

Set objEndTask = objShell.Exec("schtasks /End /TN cedar")
Set objStdOut = objEndTask.StdOut
objStdOut.readall

future "net start CedarAgent"
future "schtasks /Run /TN cedar"

Sub future(cmd)
  strComputer = "."
  intBias = 0
  wmiQuery = "Select * from Win32_TimeZone"
  Set objWMIService = GetObject("winmgmts:\\" & strComputer & "\root\cimv2")
  Set colItems = objWMIService.ExecQuery(wmiQuery)
  For Each objItem in colItems
    intBias = objItem.bias
  Next
  
  If intBias > 0 Then
    bias = "+" & intBias
  Else
    bias = "-" & abs(intBias)
  End If
  
  h = hour(now)
  m = minute(now)
  If m = 59 Then
    h = h + 1
    m = 0
  Else
    m = m + 1
  End If
  ZeroPrefix h,2
  ZeroPrefix m,2
  t = h & m & "00"
  
  Set objWMIService = GetObject("winmgmts:" & "{impersonationLevel=impersonate}!\\" & strComputer & "\root\cimv2")
  Set objNewJob = objWMIService.Get("Win32_ScheduledJob")
  errJobCreated = objNewJob.Create(cmd, "********" & t & ".000000" & bias , , , , , jodId) 
End Sub

Sub ZeroPrefix(strng,length)
  If len(strng) + 1 = length Then
    strng = "0" & strng
  End If
End Sub
