Set objArgs = WScript.Arguments
disk = objArgs(0)
folder = objArgs(1)

Set objShell = CreateObject("WScript.Shell")
Set objDiskPart = objShell.Exec("diskpart")
Set objStdIn = objDiskPart.StdIn
Set objStdOut = objDiskPart.StdOut
letter = Replace(folder, ":", "")
objStdIn.WriteLine "SELECT DISK=" & disk
objStdIn.WriteLine "SELECT PARTITION 1"
objStdIn.WriteLine "REMOVE"
objStdIn.WriteLine "EXIT"
objStdIn.Close
objStdOut.readall
