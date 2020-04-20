Set objArgs = WScript.Arguments
disk = objArgs(0)
folder = objArgs(1)

isVista = False
isXP = False
Set dtmConvertedDate = CreateObject("WbemScripting.SWbemDateTime")
strComputer = "."
Set objWMIService = GetObject("winmgmts:" _
    & "{impersonationLevel=impersonate}!\\" & strComputer & "\root\cimv2")
Set colOperatingSystems = objWMIService.ExecQuery _
    ("Select * from Win32_OperatingSystem")
For Each objOperatingSystem in colOperatingSystems
    If Left(objOperatingSystem.Version,1) = "6" Then
      isVista = True
    End If
    If InStr(objOperatingSystem.Caption, "XP") > 0 Then
      isXP = True
    End If
Next

Set objShell = CreateObject("WScript.Shell")
Set objDiskPart = objShell.Exec("diskpart.exe")
Set objStdIn = objDiskPart.StdIn
Set objStdOut = objDiskPart.StdOut
letter = Replace(folder, ":", "")
objStdIn.WriteLine "RESCAN"
objStdIn.WriteLine "SELECT DISK=" & disk
objStdIn.WriteLine "SELECT PARTITION=1"
objStdIn.WriteLine "DELETE PARTITION"
objStdIn.WriteLine "CREATE PARTITION PRIMARY"
If isVista Then
   objStdIn.WriteLine "FORMAT FS=NTFS QUICK"
End If
objStdIn.WriteLine "ASSIGN LETTER=" & letter
objStdIn.WriteLine "EXIT"
objStdIn.Close
objStdOut.readall

strComputer = "."
'wait for finish
Set objWMIService = GetObject("winmgmts:\\" & strComputer & "\root\cimv2")
Set colProcess = objWMIService.ExecQuery("Select * from Win32_Process Where Name = 'diskpart.exe'")
Do while colProcess.count = 1
    WScript.Sleep 1000
    Set objWMIService = GetObject("winmgmts:\\" & strComputer & "\root\cimv2")
    Set colProcess = objWMIService.ExecQuery("Select * from Win32_Process Where Name = 'diskpart.exe'")
Loop
Set colProcess=nothing

If isXP Then
    Set objShell = CreateObject("WScript.Shell")
    Set objDiskPart = objShell.Exec("cmd.exe /C format " & folder & " /FS:NTFS /Q")
    Set objStdIn = objDiskPart.StdIn
    Set objStdOut = objDiskPart.StdOut
    objStdIn.WriteLine "Y"
    objStdIn.Close
    objStdOut.readall
Else
    volume = folder & "\\"
    Set objWMIService = GetObject("winmgmts:\\" & strComputer & "\root\cimv2")
    Set colVolumes = objWMIService.ExecQuery("Select * from Win32_Volume Where Name = '" & volume & "'")
    For Each objVolume in colVolumes
        errResult = objVolume.Format("NTFS")
    Next
    Set colVolumes=nothing
    Set objWMIService=nothing
End If